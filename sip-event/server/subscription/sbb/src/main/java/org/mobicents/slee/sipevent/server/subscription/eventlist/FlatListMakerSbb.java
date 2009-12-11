package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.FlatListMakerParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.FlatListMaker;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.mobicents.slee.xdm.server.XDMClientControlParentSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlSbbLocalObject;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryRefType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ExternalType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.key.XcapUriKey;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;

/**
 * 
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class FlatListMakerSbb implements Sbb,
		FlatListMaker {

	private static final Logger logger = Logger
			.getLogger(FlatListMakerSbb.class);
	
	// --- parent sbb
	
	public abstract void setParentSbbCMP(FlatListMakerParentSbbLocalObject sbbLocalObject);
	public abstract FlatListMakerParentSbbLocalObject getParentSbbCMP();
	
	public void setParentSbb(FlatListMakerParentSbbLocalObject sbbLocalObject) {
		setParentSbbCMP(sbbLocalObject);
	}
	
	// --- lists
	
	public abstract void setFlatList(FlatList value);
	public abstract FlatList getFlatList();
	
	public abstract void setLists(SerializableListType[] value);
	public abstract SerializableListType[] getLists();
	
	public abstract void setCurrentListType(SerializableListType value);
	public abstract SerializableListType getCurrentListType();
	
	// --- sbb logic
	
	/**
	 * flats a tree of {@link ListType}
	 */
	private ArrayList<SerializableListType> addNestedLists(ArrayList<SerializableListType> lists, SerializableListType list) {
		for (Iterator i=list.getPojo().getListOrExternalOrEntry().iterator(); i.hasNext();) {
			JAXBElement element = (JAXBElement) i.next();
			if (element.getValue() instanceof ListType) {
				addNestedLists(lists, new SerializableListType((ListType)element.getValue()));
				i.remove();
			}
		}
		lists.add(list);
		return lists;
	}
	
	private void processList(FlatList flatList, SerializableListType[] lists, SerializableListType currentListType) {
		
		/*
		 * At this point, the RLS has a <list> element in its possession. The
		 * next step is to obtain a flat list of URIs from this element. To do
		 * that, it traverses the tree of elements rooted in the <list> element.
		 * Before traversal begins, the RLS initializes two lists: the "flat
		 * list", which will contain the flat list of the URI after traversal,
		 * and the "traversed list", which contains a list of HTTP URIs in
		 * <external> elements that have already been visited. Both lists are
		 * initially empty. Next, tree traversal begins. A server can use any
		 * tree-traversal ordering it likes, such as depth-first search or
		 * breadth-first search. The processing at each element in the tree
		 * depends on the name of the element:
		 */
		
		for (Iterator i=currentListType.getPojo().getListOrExternalOrEntry().iterator(); i.hasNext();) {
			JAXBElement element = (JAXBElement) i.next();
			
			// we remove it before processing, so we never get it again
			i.remove();
			
			if (element.getValue() instanceof EntryType) {
				/* o If the element is <entry>, the URI in the "uri" attribute of the
				 * element is added to the flat list if it is not already present (based
				 * on case-sensitive string equality) in that list, and the URI scheme
				 * represents one that can be used to service subscriptions, such as SIP
				 * [4] and pres [15].
				 */
				flatList.putEntry(new SerializableEntryType((EntryType) element.getValue()));
			}
			
			else if (element.getValue() instanceof EntryRefType) {
				/* o If the element is an <entry-ref>, the relative path reference
				 * making up the value of the "ref" attribute is resolved into an
				 * absolute URI. This is done using the procedures defined in Section
				 * 5.2 of RFC 3986 [7], using the XCAP root of the RLS services document
				 * as the base URI. This absolute URI is resolved. If the result is not
				 * a 200 OK containing a <entry> element, the SUBSCRIBE request SHOULD
				 * be rejected with a 502 (Bad Gateway). Otherwise, the <entry> element
				 * returned is processed as described in the previous step.
				 */
				
				// we need to derefer the entry, which is async, so we need to store current list
				setCurrentListType(currentListType);
				setFlatList(flatList);
				setLists(lists);
				
				EntryRefType entryRefType = (EntryRefType) element.getValue();
				String resourceList = entryRefType.getRef();
				if (!dereferenceResourceList(resourceList, flatList,false)) {
					return;
				}
			}
			
			else if (element.getValue() instanceof ExternalType) {
				
				 /* o If the element is an <external> element, the absolute URI making up
				 * the value of the "anchor" attribute of the element is examined. If
				 * the URI is on the traversed list, the server MUST cease traversing
				 * the tree, and SHOULD reject the SUBSCRIBE request with a 502 (Bad
				 * Gateway). If the URI is not on the traversed list, the server adds
				 * the URI to the traversed list, and dereferences the URI. If the
				 * result is not a 200 OK containing a <list> element, the SUBSCRIBE
				 * request SHOULD be rejected with a 502 (Bad Gateway). Otherwise, the
				 * RLS replaces the <external> element in its local copy of the tree
				 * with the <list> element that was returned, and tree traversal
				 * continues.
				 * 
				 * 
				 * Because the <external> element is used to dynamically construct the
				 * tree, there is a possibility of recursive evaluation of references.
				 * The traversed list is used to prevent this from happening.
				 * 
				 * Once the tree has been traversed, the RLS can create virtual
				 * subscriptions to each URI in the flat list, as defined in [14]. In
				 * the processing steps outlined above, when an <entry-ref> or
				 * <external> element contains a reference that cannot be resolved,
				 * failing the request is at SHOULD strength. In some cases, an RLS may
				 * provide better service by creating virtual subscriptions to the URIs
				 * in the flat list that could be obtained, omitting those that could
				 * not. Only in those cases should the SHOULD recommendation be ignored.
				 * 
				 * 
				 */
				
				//FIXME add support to really external uris
				
				ExternalType externalType = (ExternalType)element.getValue();
				String resourceList = externalType.getAnchor();
				
				// we need to derefer the entry, which is async, so we need to store current list
				setCurrentListType(currentListType);
				setFlatList(flatList);
				setLists(lists);
				
				if (!dereferenceResourceList(resourceList, flatList,true)) {
					return;
				}
			}
		}
		
		// if we get here this list is fully processed, so move to the next one
		if (lists.length != 0) {
			processList(flatList, Arrays.copyOfRange(lists, 0, lists.length-1),lists[lists.length-1]);
		}
		else {
			returnFlatListToParent(flatList);
		}
	}
	
	/**
	 * 
	 * @param resourceList
	 * @param flatList
	 * @param absoluteURI indicates if the uri is absolute or not
	 * @return true if the make of the flat list should continue, false otherwise
	 */
	private boolean dereferenceResourceList(String resourceList, FlatList flatList, boolean absoluteURI) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Dereferencing resource list "+resourceList);
		}
		
		XcapUriKey key = null;
		DocumentSelector documentSelector = null;
		try {
			if (absoluteURI) {
				String shemeAndAuthorityURI = getSchemeAndAuthorityURI();
				if (resourceList.startsWith(shemeAndAuthorityURI)) {
					resourceList = resourceList.substring(shemeAndAuthorityURI.length());
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("The resource list (to dereference) uri "+resourceList+" does not starts with server scheme and authority uri "+shemeAndAuthorityURI);
					}
					return true;
				}
			}
			else {
				resourceList = "/" + resourceList;
			}
			ResourceSelector resourceSelector = null;
			int queryComponentSeparator = resourceList.indexOf('?');
			if (queryComponentSeparator > 0) {
				resourceSelector = Parser
						.parseResourceSelector(
								getLocalXcapRoot(),
								resourceList
										.substring(0,
												queryComponentSeparator),
								resourceList
										.substring(
												queryComponentSeparator + 1));
			} else {
				resourceSelector = Parser
						.parseResourceSelector(
								getLocalXcapRoot(),
								resourceList, null);
			}
			
			documentSelector = Parser.parseDocumentSelector(resourceSelector.getDocumentSelector());
			if (!documentSelector.getAUID().equals("resource-lists")) {
				logger.error("Unable to make flat list, invalid or not supported resource list uri: "+resourceList);
				flatList.setStatus(Response.BAD_GATEWAY);
				returnFlatListToParent(flatList);
				return false;
			}
			else {
				flatList.getResourceLists().add(documentSelector);
				setFlatList(flatList);
			}
			key = new XcapUriKey(resourceSelector);
		}
		catch (Exception e) {
			logger.error("Failed to parse resource list (to dereference) "+resourceList,e);
			flatList.setStatus(Response.BAD_GATEWAY);
			returnFlatListToParent(flatList);
			return false;
		}
		XDMClientControlSbbLocalObject xdmClientSbb = getXDMClientControlSbb();
		xdmClientSbb.get(key,null);
		return false;
	}
	
	private void makeFlatList(FlatList flatList, SerializableListType listType) {
		// the specs don't refer it but a list can contain inner lists, so lets
		// build a queue of lists to process
		ArrayList<SerializableListType> lists = addNestedLists(new ArrayList<SerializableListType>(), listType);
		SerializableListType currentListType = lists.remove(lists.size()-1);
		// kick off the final process
		processList(flatList, lists.toArray(new SerializableListType[lists.size()]), currentListType);
	}
	
	public void makeFlatList(ServiceType serviceType) {
		
		// create flat list and store it in cmp
		FlatList flatList = new FlatList(new SerializableServiceType(serviceType));
		
		/*
		 * 
		 * If the <service> element had a <list> element, it is extracted. If
		 * the <service> element had a <resource-list> element, its URI content
		 * is dereferenced. The result should be a <list> element. If it is not,
		 * the request SHOULD be rejected with a 502 (Bad Gateway). Otherwise,
		 * that <list> element is extracted.
		 * 
		 */
		ListType listType = serviceType.getList();
		if (listType != null) {
			makeFlatList(flatList, new SerializableListType(listType));
		}
		else {
			String resourceList = serviceType.getResourceList().trim();
			setFlatList(flatList);
			dereferenceResourceList(resourceList, flatList,true);
		}
		
	}
	
	// --- XDM call backs
	
	public void getResponse(XcapUriKey key, int responseCode, String mimetype,
			String content, String tag) {

		FlatList flatList = getFlatList();
		SerializableListType[] lists = getLists();
		SerializableListType currentListType = getCurrentListType();
		
		if (responseCode == 200) {
			// unmarshall content
			Object o = null;
			StringReader stringReader = new StringReader(content);
			try {				
				o = jaxbContext.createUnmarshaller().unmarshal(stringReader);				
			} catch (JAXBException e) {
				logger.error("failed to unmarshall content for key "+key,e);
				// if it was deferring an entry ref continue
				flatList.setStatus(Response.BAD_GATEWAY);
				if (currentListType != null) {					
					processList(flatList, lists, currentListType);
				}
				else {
					returnFlatListToParent(flatList);					
				}				
				return;
			}
			finally {
				stringReader.close();
			}

			// check what type of object we got
			if (o instanceof ListType) {
				// we are deferring a resource list
				if (lists == null) {
					makeFlatList(flatList,new SerializableListType((ListType) o));
				}
				else {
					// restart procedure to make flat list
					processList(flatList, lists, new SerializableListType((ListType) o));
				}								
			}
			else if (o instanceof EntryType) {
				// we are deferring a entry ref
								
				// add entry 
				flatList.putEntry(new SerializableEntryType((EntryType) o));
				// restart procedure to make flat list
				processList(flatList, lists, currentListType);
			}
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("xdm get request didn't returned sucess code. key: "+key);
			}
			flatList.setStatus(Response.BAD_GATEWAY);
			if (getCurrentListType() != null) {					
				processList(flatList, lists, currentListType);
			}
			else {
				returnFlatListToParent(flatList);					
			}				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#attributeUpdated(org.openxdm.xcap.common.uri.DocumentSelector, org.openxdm.xcap.common.uri.NodeSelector, org.openxdm.xcap.common.uri.AttributeSelector, java.util.Map, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void attributeUpdated(DocumentSelector documentSelector,
			NodeSelector nodeSelector, AttributeSelector attributeSelector,
			Map<String, String> namespaces, String oldETag, String newETag,
			String documentAsString, String attributeValue) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#deleteResponse(org.openxdm.xcap.common.key.XcapUriKey, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteResponse(XcapUriKey key, int responseCode,
			String responseContent, String eTag) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#documentUpdated(org.openxdm.xcap.common.uri.DocumentSelector, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void documentUpdated(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#elementUpdated(org.openxdm.xcap.common.uri.DocumentSelector, org.openxdm.xcap.common.uri.NodeSelector, java.util.Map, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void elementUpdated(DocumentSelector documentSelector,
			NodeSelector nodeSelector, Map<String, String> namespaces,
			String oldETag, String newETag, String documentAsString,
			String elementAsString) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#putResponse(org.openxdm.xcap.common.key.XcapUriKey, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void putResponse(XcapUriKey key, int responseCode,
			String responseContent, String eTag) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void returnFlatListToParent(FlatList flatList) {
		getParentSbbCMP().flatListMade(flatList);
		sbbContext.getSbbLocalObject().remove();
	}
	
	// --- aux public getter that reuires jboss running, publi so junit tests overwrite it
	
	public String getLocalXcapRoot() {
		return ServerConfiguration.getInstance().getXcapRoot();
	}
	
	public String getSchemeAndAuthorityURI() {
		return ServerConfiguration.getInstance().getSchemeAndAuthority();
	}
	
	// --- XDM CLIENT CHILD SBB
	
	public abstract ChildRelation getXDMClientControlChildRelation();

	public abstract XDMClientControlSbbLocalObject getXDMClientControlChildSbbCMP();

	public abstract void setXDMClientControlChildSbbCMP(
			XDMClientControlSbbLocalObject value);

	public XDMClientControlSbbLocalObject getXDMClientControlSbb() {
		XDMClientControlSbbLocalObject childSbb = getXDMClientControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (XDMClientControlSbbLocalObject) getXDMClientControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}
			setXDMClientControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((XDMClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
	}
	
	// ---- JAXB
	
	/*
	 * JAXB context is thread safe
	 */
	private static final JAXBContext jaxbContext = initJAXBContext();

	private static JAXBContext initJAXBContext() {
		try {
			return JAXBContext
					.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb"
							+ ":org.openxdm.xcap.client.appusage.resourcelists.jaxb");
		} catch (JAXBException e) {
			logger.error("failed to create jaxb context");
			return null;
		}
	}
	
	// ----------- SBB OBJECT's LIFE CYCLE

	private SbbContext sbbContext;
	
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
	}
	
	public void sbbActivate() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {
	}

	public void sbbLoad() {
	}

	public void sbbPassivate() {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbRemove() {
	}

	public void sbbRolledBack(RolledBackContext arg0) {
	}

	public void sbbStore() {
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}
}
package org.mobicents.slee.sipevent.examples;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.TimerPreserveMissed;
import javax.slee.facilities.Tracer;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;
import javax.xml.bind.JAXBContext;

import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.mobicents.slee.sippresence.client.PresenceClientControlParentSbbLocalObject;
import org.mobicents.slee.sippresence.client.PresenceClientControlSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlParentSbbLocalObject;
import org.mobicents.slee.xdm.server.XDMClientControlSbbLocalObject;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType.DisplayName;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ObjectFactory;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.PackagesType;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.RlsServices;
import org.openxdm.xcap.client.appusage.rlsservices.jaxb.ServiceType;
import org.openxdm.xcap.common.key.UserDocumentUriKey;
import org.openxdm.xcap.common.key.XcapUriKey;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;

/**
 * 
 * @author Eduardo Martins
 * 
 */
public abstract class RLSExampleSubscriberSbb implements javax.slee.Sbb,
	RLSExampleSubscriber {

	String presenceDomain = System.getProperty("bind.address","127.0.0.1");
	String subscriber = "sip:carol@"+presenceDomain;
	String notifier = "sip:carol@"+presenceDomain+";pres-list=Default";
	String eventPackage = "presence";
	String contentType = "application";
	String contentSubType = "pidf+xml";
	int expires = 300;

	// --- PRESENCE CLIENT CHILD SBB

	public abstract ChildRelation getPresenceClientControlSbbChildRelation();

	public abstract PresenceClientControlSbbLocalObject getPresenceClientControlSbbCMP();

	public abstract void setPresenceClientControlSbbCMP(
			PresenceClientControlSbbLocalObject value);

	private PresenceClientControlSbbLocalObject getPresenceClientControlSbb() {
		PresenceClientControlSbbLocalObject childSbb = getPresenceClientControlSbbCMP();
		if (childSbb == null) {
			try {
			childSbb = (PresenceClientControlSbbLocalObject) getPresenceClientControlSbbChildRelation()
					.create();
			} catch (Exception e) {
				tracer.severe("Failed to create child sbb", e);
				return null;
			}
			setPresenceClientControlSbbCMP(childSbb);
			childSbb
					.setParentSbb((PresenceClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
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
				tracer.severe("Failed to create child sbb", e);
				return null;
			}
			setXDMClientControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((XDMClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
	}
	
	// --- CMPs

	public abstract void setParentSbbCMP(RLSExampleSubscriberParentSbbLocalObject value);

	public abstract RLSExampleSubscriberParentSbbLocalObject getParentSbbCMP();
	
	// --- SBB LOCAL OBJECT
	
	public void setParentSbb(RLSExampleSubscriberParentSbbLocalObject parentSbb) {
		setParentSbbCMP(parentSbb);
	}
	
	private EntryType createEntryType(String uri) {
		EntryType entryType = new EntryType();
		entryType.setUri(uri);
		DisplayName displayName = new EntryType.DisplayName();
		displayName.setValue(uri);
		entryType.setDisplayName(displayName);
		return entryType;
	}
	
	private String getRlsServices(String[] entryURIs) {
		StringWriter stringWriter = new StringWriter();
		try {			
			JAXBContext context = JAXBContext.newInstance("org.openxdm.xcap.client.appusage.rlsservices.jaxb");
			ListType listType = new ListType();
			for (String entryURI : entryURIs) {
				listType.getListOrExternalOrEntry().add(createEntryType(entryURI));
			}
			ServiceType serviceType = new ServiceType();
			serviceType.setList(listType);
			PackagesType packagesType = new PackagesType();
			packagesType.getPackageAndAny().add(new ObjectFactory().createPackagesTypePackage(eventPackage));
			serviceType.setPackages(packagesType);
			serviceType.setUri(notifier);
			RlsServices rlsServices = new RlsServices();
			rlsServices.getService().add(serviceType);
			context.createMarshaller().marshal(rlsServices, stringWriter);
			return stringWriter.toString();			
		} catch (Exception e) {
			tracer.severe("failed to read rls-services.xml",e);
		}
		finally {		
			try {
				stringWriter.close();
			} catch (IOException e) {
				tracer.severe(e.getMessage(),e);
			}
		}
		return null;
	}
		
	public void start(String[] entryURIs) {
		try {
			XDMClientControlSbbLocalObject xdm = getXDMClientControlSbb();
			// insert the document
			xdm.put(new UserDocumentUriKey("rls-services",notifier,"index"), "application/rls-services+xml", getRlsServices(entryURIs).getBytes("UTF-8"),null);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);
			getParentSbbCMP().subscriberNotStarted();
		}
	}

	private String getSubscriptionId() {
		return "rls-example~"+subscriber+"~"+notifier+"~"+eventPackage;
	}
	
	public void putResponse(XcapUriKey key, int responseCode, String responseContent, String tag) {
		tracer.info("Response to the insertion of the rls services document: status="+responseCode+",content="+responseContent);
		if (responseCode != 200 && responseCode != 201) {			
			getParentSbbCMP().subscriberNotStarted();
		}
		else {			
			// now subscribe the presence of it
			getPresenceClientControlSbb().newSubscription(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires);			
		}
	}
		
	public void newSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {
		
		tracer.info("subscribe ok: responseCode=" + responseCode + ",expires="
				+ expires);
		try {
			// let's set a periodic timer in a null activity to refresh the
			// publication
			TimerOptions timerOptions = new TimerOptions();
			timerOptions.setPreserveMissed(TimerPreserveMissed.ALL);

			NullActivity nullActivity = nullActivityFactory.createNullActivity();
			ActivityContextInterface aci = nullACIFactory.getActivityContextInterface(nullActivity);
			aci.attach(this.sbbContext.getSbbLocalObject());
			timerFacility.setTimer(aci, null, System.currentTimeMillis() + (expires-1)
					* 1000, (expires-1) * 1000, 0, timerOptions);

			getParentSbbCMP().subscriberStarted();
		}
		catch (Exception e) {
			tracer.severe(e.getMessage(),e);
		}
		
	}
	
	private void deleteRlsServices() {
		try {
			getXDMClientControlSbb().delete(new UserDocumentUriKey("rls-services",notifier,"index"),null);			
		} catch (Exception e) {
			tracer.severe(e.getMessage(), e);			
		}
	}
	
	public void newSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on subscribe: error=" + error);
		deleteRlsServices();		
	}
	
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Event terminationReason, Status status, String content,
			String contentType, String contentSubtype) {
		String notification = "\nNOTIFY EVENT:" + "\n+-- Subscriber: "
		+ subscriber + "\n+-- Notifier: " + notifier
		+ "\n+-- EventPackage: " + eventPackage
		+ "\n+-- SubscriptionId: " + subscriptionId				
		+ "\n+-- Subscription status: " + status
		+ "\n+-- Subscription terminationReason: " + terminationReason
		+ "\n+-- Content Type: " + contentType + '/' + contentSubtype
		+ "\n+-- Content:\n\n" + content;
		tracer.info(notification);
		if (status == Subscription.Status.terminated && terminationReason != null && terminationReason == Subscription.Event.deactivated) {
			tracer.info("The subscription was deactivated, re-subscribing");
			// re-subscribe
			getPresenceClientControlSbb().newSubscription(subscriber, "...", notifier, eventPackage, getSubscriptionId(), expires);
		}
	}
	
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		// refresh subscription
		getPresenceClientControlSbb().refreshSubscription(subscriber, notifier, eventPackage, getSubscriptionId(), expires);
	}

	public void refreshSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		tracer.info("resubscribe Ok : expires=" + expires);

	}

	public void refreshSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on resubscribe: error=" + error);
		deleteRlsServices();
	}

	public void stop() {
		getPresenceClientControlSbb().removeSubscription(subscriber, notifier, eventPackage, getSubscriptionId());
		deleteRlsServices();
	}
	
	public void removeSubscriptionError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		tracer.info("error on unsubscribe: error=" + error);		
	}
	
	public void removeSubscriptionOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		tracer.info("unsubscribe Ok");		
	}
	
	public void deleteResponse(XcapUriKey key, int responseCode, String responseContent, String tag) {
		getParentSbbCMP().subscriberStopped();		
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
	 * @see org.mobicents.slee.xdm.server.XDMClientControlParent#getResponse(org.openxdm.xcap.common.key.XcapUriKey, int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void getResponse(XcapUriKey key, int responseCode, String mimetype,
			String content, String eTag) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#modifyPublicationError(java.lang.Object, int)
	 */
	@Override
	public void modifyPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#modifyPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void modifyPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#newPublicationError(java.lang.Object, int)
	 */
	@Override
	public void newPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#newPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void newPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#refreshPublicationError(java.lang.Object, int)
	 */
	@Override
	public void refreshPublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#refreshPublicationOk(java.lang.Object, java.lang.String, int)
	 */
	@Override
	public void refreshPublicationOk(Object requestId, String eTag, int expires) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#removePublicationError(java.lang.Object, int)
	 */
	@Override
	public void removePublicationError(Object requestId, int error) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.slee.sippresence.client.PresenceClientControlParent#removePublicationOk(java.lang.Object)
	 */
	@Override
	public void removePublicationOk(Object requestId) {
		// TODO Auto-generated method stub
		
	}
		
	
	// --- SBB OBJECT

	private SbbContext sbbContext = null; // This SBB's context

	private TimerFacility timerFacility = null;
	private NullActivityContextInterfaceFactory nullACIFactory;
	private NullActivityFactory nullActivityFactory;

	/**
	 * Called when an sbb object is instantied and enters the pooled state.
	 */
	public void setSbbContext(SbbContext sbbContext) {

		this.sbbContext = sbbContext;
		tracer = sbbContext.getTracer("RLSExampleSubscriberSbb");
		try {
			Context context = (Context) new InitialContext()
					.lookup("java:comp/env");
			timerFacility = (TimerFacility) context
				.lookup("slee/facilities/timer");
			nullACIFactory = (NullActivityContextInterfaceFactory) context
				.lookup("slee/nullactivity/activitycontextinterfacefactory");
			nullActivityFactory = (NullActivityFactory) context
				.lookup("slee/nullactivity/factory");
		} catch (Exception e) {
			tracer.severe("Unable to retrieve factories, facilities & providers",
					e);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
	}

	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
	}

	private Tracer tracer;

}
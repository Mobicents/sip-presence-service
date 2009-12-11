package org.mobicents.slee.sippresence.server.publication;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.address.URI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SLEEException;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.TransactionRequiredLocalException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.java.slee.resource.sip.SleeSipProvider;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl;
import org.mobicents.slee.sipevent.server.publication.pojo.ComposedPublication;
import org.mobicents.slee.sipevent.server.publication.pojo.Publication;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbbLocalObject;
import org.mobicents.slee.sippresence.pojo.pidf.Basic;
import org.mobicents.slee.sippresence.pojo.pidf.Presence;
import org.mobicents.slee.sippresence.pojo.pidf.Status;
import org.mobicents.slee.sippresence.pojo.pidf.Tuple;

/**
 * Publication control implementation child sbb that transforms the sip event
 * framework in the PUBLISH interface of a SIP Presence Server.
 * 
 * @author eduardomartins
 * 
 */
public abstract class PresencePublicationControlSbb implements Sbb,
		ImplementedPublicationControl {

	private static Logger logger = Logger
			.getLogger(PresencePublicationControlSbb.class);
	private final static String[] eventPackages = { "presence" };

	/**
	 * SbbObject's sbb context
	 */
	private SbbContext sbbContext;

	/**
	 * SbbObject's context setting
	 */
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
	}

	private HeaderFactory headerFactory;

	private HeaderFactory getHeaderFactory() throws NamingException {
		if (headerFactory == null) {
			headerFactory = ((SleeSipProvider) new InitialContext()
					.lookup("java:comp/env/slee/resources/jainsip/1.2/provider"))
					.getHeaderFactory();
		}
		return headerFactory;
	}

	public abstract ChildRelation getChildRelation();

	public abstract SubscriptionControlSbbLocalObject getChildSbbCMP();

	public abstract void setChildSbbCMP(SubscriptionControlSbbLocalObject value);

	private SubscriptionControlSbbLocalObject getChildSbb()
			throws TransactionRequiredLocalException, SLEEException,
			CreateException {
		SubscriptionControlSbbLocalObject childSbb = getChildSbbCMP();
		if (childSbb == null) {
			childSbb = (SubscriptionControlSbbLocalObject) getChildRelation()
					.create();
			setChildSbbCMP(childSbb);
		}
		return childSbb;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#getEventPackages()
	 */
	public String[] getEventPackages() {
		return eventPackages;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#notifySubscribers(org.mobicents.slee.sipevent.server.publication.pojo.ComposedPublication)
	 */
	public void notifySubscribers(ComposedPublication composedPublication) {
		try {
			SubscriptionControlSbbLocalObject childSbb = getChildSbb();
			ContentTypeHeader contentTypeHeader = (composedPublication
					.getContentType() == null || composedPublication
					.getContentSubType() == null) ? null : getHeaderFactory()
					.createContentTypeHeader(
							composedPublication.getContentType(),
							composedPublication.getContentSubType());
			if (composedPublication.getDocument() != null
					&& composedPublication.getUnmarshalledContent() == null) {
				// content needs to unmarshalled
				StringReader stringReader = new StringReader(
						composedPublication.getDocument());
				composedPublication
						.setUnmarshalledContent((JAXBElement) getUnmarshaller()
								.unmarshal(stringReader));
				stringReader.close();
			}
			childSbb.notifySubscribers(composedPublication
					.getComposedPublicationKey().getEntity(),
					composedPublication.getComposedPublicationKey()
							.getEventPackage(), composedPublication
							.getUnmarshalledContent(), contentTypeHeader);
		} catch (Exception e) {
			logger.error("failed to notify subscribers for "
					+ composedPublication.getComposedPublicationKey(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#authorizePublication(java.lang.String, javax.xml.bind.JAXBElement)
	 */
	public boolean authorizePublication(String requestEntity,
			JAXBElement unmarshalledContent) {
		// returns true if request uri matches entity (stripped from pres:
		// prefix if found) inside pidf doc
		String entity = ((JAXBElement<Presence>) unmarshalledContent)
				.getValue().getEntity();
		if (entity != null) {
			if (entity.startsWith("pres:") && entity.length() > 5) {
				entity = entity.substring(5);
			}
			return entity.equals(requestEntity);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#acceptsContentType(java.lang.String, javax.sip.header.ContentTypeHeader)
	 */
	public boolean acceptsContentType(String eventPackage,
			ContentTypeHeader contentTypeHeader) {
		// FIXME
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.slee.sipevent.server.publication.ImplementedPublicationControl#getAcceptsHeader(java.lang.String)
	 */
	public Header getAcceptsHeader(String eventPackage) {
		// FIXME
		if (eventPackage.equals("presence")) {
			try {
				return getHeaderFactory().createAcceptHeader("application",
						"pidf+xml");
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return null;
	}

	
	/*
	 * JAXB context is thread safe
	 */
	private static final JAXBContext jaxbContext = initJAXBContext();

	private static JAXBContext initJAXBContext() {
		try {
			return JAXBContext
					.newInstance("org.mobicents.slee.sippresence.pojo.pidf"
							+ ":org.mobicents.slee.sippresence.pojo.pidf.oma"
							+ ":org.mobicents.slee.sippresence.pojo.rpid"
							+ ":org.mobicents.slee.sippresence.pojo.datamodel"
							+ ":org.mobicents.slee.sippresence.pojo.commonschema");
		} catch (JAXBException e) {
			logger.error("failed to create jaxb context");
			return null;
		}
	}

	public Unmarshaller getUnmarshaller() {
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create unmarshaller", e);
			return null;
		}
	}

	public Marshaller getMarshaller() {
		try {
			return jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create unmarshaller", e);
			return null;
		}
	}

	private static final PresenceCompositionPolicy presenceCompositionPolicy = new PresenceCompositionPolicy();
	
	private JAXBElement unmarshall(String marshalledContent, Unmarshaller unmarshaller) {
		StringReader stringReader = new StringReader(marshalledContent);
		JAXBElement jaxbElement = null;
		try {
			jaxbElement = (JAXBElement) unmarshaller.unmarshal(stringReader);
		} catch (JAXBException e) {
			logger.error("failed to unmarshall presence content", e);
		}
		stringReader.close();
		return jaxbElement;		
	}
	
	private String marshall(Presence presence, Marshaller marshaller) {
		StringWriter stringWriter = new StringWriter();
		String result = null;
		try {
			marshaller.marshal(presence, stringWriter);
			result = stringWriter.toString();
		} catch (JAXBException e) {
			logger.error("failed to marshall presence content", e);
		}
		try {
			stringWriter.close();
		} catch (IOException e) {
			logger.error("failed to close string writer", e);
		}
		return result;		
	}
	
	public ComposedPublication combinePublication(Publication publication,
			ComposedPublication composedPublication) {
		
		Unmarshaller unmarshaller = null;
		Marshaller marshaller = null;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller = jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			logger.error("failed to create unmarshaller", e);
			return null;
		}
		
		JAXBElement publicationUnmarshalledContent = publication.getUnmarshalledContent();
		if (publicationUnmarshalledContent == null) {
			publicationUnmarshalledContent = unmarshall(publication.getDocument(), unmarshaller);
			publication.setUnmarshalledContent(publicationUnmarshalledContent);
		}
		JAXBElement composedpublicationUnmarshalledContent = composedPublication.getUnmarshalledContent();
		if (composedpublicationUnmarshalledContent == null) {
			composedpublicationUnmarshalledContent = unmarshall(composedPublication.getDocument(), unmarshaller);			
		}
		if (publicationUnmarshalledContent != null && composedpublicationUnmarshalledContent != null) {
			Presence composedPresence = presenceCompositionPolicy.compose((Presence)publicationUnmarshalledContent.getValue(), (Presence)composedpublicationUnmarshalledContent.getValue());
			composedpublicationUnmarshalledContent.setValue(composedPresence);
			composedPublication.setUnmarshalledContent(composedpublicationUnmarshalledContent);
			composedPublication.setDocument(marshall(composedPresence, marshaller));
			
		}
		return composedPublication;
	}

	public Publication getAlternativeValueForExpiredPublication(
			Publication publication) {
		JAXBElement element = publication.getUnmarshalledContent();
		if (element == null) {
			StringReader stringReader = new StringReader(publication
					.getDocument());
			try {
				element = (JAXBElement) getUnmarshaller().unmarshal(
						stringReader);
			} catch (Exception e) {
				logger.error("failed to unmarshall publication", e);
				return null;
			} finally {
				stringReader.close();
			}
			publication.setUnmarshalledContent(element);
		}
		if (element.getValue() instanceof Presence) {
			Presence presence = (Presence) element.getValue();
			for (Tuple tuple : presence.getTuple()) {
				tuple.getAny().clear();
				tuple.getNote().clear();
				tuple.setTimestamp(null);
				Status status = new Status();
				status.setBasic(Basic.CLOSED);
				tuple.setStatus(status);
			}
			presence.getAny().clear();
			presence.getNote().clear();
			// marshall new content
			StringWriter stringWriter = new StringWriter();
			try {
				getMarshaller().marshal(element, stringWriter);
				publication.setDocument(stringWriter.toString());
			} catch (Exception e) {
				logger.error("failed to marshall alternative publication", e);
				return null;
			} finally {
				try {
					stringWriter.close();
				} catch (IOException e) {
					// ignore
				}
			}
			return publication;
		} else {
			return null;
		}
	}

	public boolean isResponsibleForResource(URI uri) {
		return true;
	}

	// ----------- SBB OBJECT's LIFE CYCLE

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

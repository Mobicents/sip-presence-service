package org.mobicents.slee.xdm.server.subscription;

import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;

import javax.sip.ServerTransaction;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.SbbLocalObject;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.NotifyContent;
import org.mobicents.slee.sipevent.server.subscription.data.Notifier;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;
import org.mobicents.slee.xdm.server.ServerConfiguration;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.EntryType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ListType;
import org.openxdm.xcap.client.appusage.resourcelists.jaxb.ResourceLists;
import org.openxdm.xcap.common.datasource.Document;
import org.openxdm.xcap.common.error.InternalServerErrorException;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.Parser;
import org.openxdm.xcap.common.uri.ResourceSelector;
import org.openxdm.xcap.common.xcapdiff.DocumentType;
import org.openxdm.xcap.common.xcapdiff.ObjectFactory;
import org.openxdm.xcap.common.xcapdiff.XcapDiff;
import org.openxdm.xcap.server.slee.resource.datasource.AppUsageActivity;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceActivityContextInterfaceFactory;
import org.openxdm.xcap.server.slee.resource.datasource.DataSourceSbbInterface;
import org.openxdm.xcap.server.slee.resource.datasource.DocumentActivity;

/**
 * Logic for {@link XcapDiffSubscriptionControlSbb}
 * 
 * @author martins
 * 
 */
public class XcapDiffSubscriptionControl {

	private static final String[] xcapDiffEventPackages = { "xcap-diff" };

	public static String[] getEventPackages() {
		return xcapDiffEventPackages;
	}

	private ContentTypeHeader xcapDiffContentTypeHeader = null;

	public ContentTypeHeader getXcapDiffContentTypeHeader(XcapDiffSubscriptionControlSbbInterface sbb) {
		if (xcapDiffContentTypeHeader == null) {
			try {
				xcapDiffContentTypeHeader = sbb
						.getHeaderFactory()
						.createContentTypeHeader("application", "xcap-diff+xml");
			} catch (ParseException e) {
				// ignore
				e.printStackTrace();
			}
		}
		return xcapDiffContentTypeHeader;
	}

	public void isSubscriberAuthorized(String subscriber,
			String subscriberDisplayName, Notifier notifier, SubscriptionKey key,
			int expires, String content, String contentType,
			String contentSubtype, boolean eventList, ServerTransaction serverTransaction, XcapDiffSubscriptionControlSbbInterface sbb) {

		StringReader stringReader = null;

		try {
			stringReader = new StringReader(content);
			ResourceLists object = (ResourceLists) sbb.getUnmarshaller()
					.unmarshal(stringReader);
			stringReader.close();
			stringReader = null;
			// ok, resource-lists parsed, let's process it's lists elements
			HashSet<String> appUsagesToSubscribe = new HashSet<String>();
			HashSet<DocumentSelector> documentsToSubscribe = new HashSet<DocumentSelector>();
			for (ListType listType : object.getList()) {
				for (Object listOrExternalOrEntry : listType
						.getListOrExternalOrEntry()) {
					if (listOrExternalOrEntry instanceof JAXBElement<?>) {
						JAXBElement<?> jAXBElement = (JAXBElement<?>) listOrExternalOrEntry;
						if (jAXBElement.getValue() instanceof EntryType) {
							EntryType entryType = (EntryType) jAXBElement
									.getValue();
							// process it
							ResourceSelector resourceSelector = null;
							try {
								int queryComponentSeparator = entryType
										.getUri().indexOf('?');
								if (queryComponentSeparator > 0) {
									resourceSelector = Parser
											.parseResourceSelector(
													ServerConfiguration.getInstance().getXcapRoot(),
													entryType
															.getUri()
															.substring(0,
																	queryComponentSeparator),
													entryType
															.getUri()
															.substring(
																	queryComponentSeparator + 1));
								} else {
									resourceSelector = Parser
											.parseResourceSelector(
													ServerConfiguration.getInstance().getXcapRoot(),
													entryType.getUri(), null);
								}
								if (resourceSelector.getDocumentSelector()
										.indexOf('/') < 0) {
									// trying to subscribe app usage
									String auid = resourceSelector
											.getDocumentSelector();
									if (logger.isInfoEnabled()) {
										logger.info("subscribing auid " + auid);
									}
									// FIXME authorize and allow it in case authorization succeeds, right now forbidden this kind of subscription
									sbb.getParentSbbCMP().newSubscriptionAuthorization(subscriber, subscriberDisplayName, notifier, key, expires, Response.FORBIDDEN, eventList, serverTransaction);
									return;
									//appUsagesToSubscribe.add(auid);
								} else {
									// trying to subscribe a document or part of
									// it
									final DocumentSelector documentSelector = DocumentSelector.valueOf(resourceSelector
													.getDocumentSelector());
									// FIXME add proper authorization
									if (!documentSelector.isUserDocument()) {
										// right now we don't support an external PS or RLS so it is secure to forbidden subscription to all global docs
										sbb.getParentSbbCMP().newSubscriptionAuthorization(subscriber, subscriberDisplayName, notifier, key, expires, Response.FORBIDDEN, eventList, serverTransaction);
										return;
									}
									else {
										if (!documentSelector.getDocumentParent().substring("users/".length()).startsWith(subscriber)) {
											// right now, due to security, do not allow any subscription where the subscriber is different than the XUI
											sbb.getParentSbbCMP().newSubscriptionAuthorization(subscriber, subscriberDisplayName, notifier, key, expires, Response.FORBIDDEN, eventList, serverTransaction);
											return;
										}
									}
									/* TODO support subscription to element or attribute
									NodeSelector nodeSelector = null;
									TerminalSelector terminalSelector = null;
									if (resourceSelector.getNodeSelector() != null) {
										nodeSelector = Parser
												.parseNodeSelector(resourceSelector
														.getNodeSelector());
										if (nodeSelector.getTerminalSelector() != null) {
											// parse terminal selector
											terminalSelector = Parser
													.parseTerminalSelector(nodeSelector
															.getTerminalSelector());
										}
									}*/
									if (logger.isInfoEnabled()) {
										logger
												.info("subscribing document (or part of it) "
														+ documentSelector);
									}
									documentsToSubscribe.add(documentSelector);
								}
							} catch (Exception e) {
								logger
										.error(
												"failed to parse entry uri to subscribe",
												e);
							}
						}
					}
				}
			}

			// create subscriptions object
			Subscriptions subscriptions = new Subscriptions(key,subscriber,
					appUsagesToSubscribe, documentsToSubscribe);
			// get subscriptions map cmp
			SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
			if (subscriptionsMap == null) {
				subscriptionsMap = new SubscriptionsMap();
			}
			// build set of other documents and app usages already subscribed by
			// this entity
			HashSet<DocumentSelector> documentSelectorsAlreadySubscribed = new HashSet<DocumentSelector>();
			HashSet<String> appUsagesAlreadySubscribed = new HashSet<String>();
			for (Subscriptions s : subscriptionsMap.getSubscriptions()) {
				for (DocumentSelector ds : s.getDocumentSelectors()) {
					documentSelectorsAlreadySubscribed.add(ds);
				}
				for (String auid : s.getAppUsages()) {
					appUsagesAlreadySubscribed.add(auid);
				}
			}
			// save subscriptions object on cmp
			subscriptionsMap.put(subscriptions);
			sbb.setSubscriptionsMap(subscriptionsMap);
			// let's subscribe all documents and/or app usages
			DataSourceActivityContextInterfaceFactory dataSourceActivityContextInterfaceFactory = sbb.getDataSourceActivityContextInterfaceFactory();
			DataSourceSbbInterface dataSourceSbbInterface = sbb.getDataSourceSbbInterface();
			SbbLocalObject sbbLocalObject = sbb.getSbbContext().getSbbLocalObject();
			for (DocumentSelector documentSelector : documentsToSubscribe) {
				if (!documentSelectorsAlreadySubscribed
						.contains(documentSelector)
						&& !appUsagesAlreadySubscribed
								.contains(documentSelector.getAUID())) {
					// app usages already subscribed does not match this
					// document selector's app usage,
					// and this document selector is not subscribed already due
					// to another
					// subscription in the same entity, so subscribe the doc
					DocumentActivity activity = dataSourceSbbInterface.createDocumentActivity(documentSelector);
					ActivityContextInterface aci = dataSourceActivityContextInterfaceFactory.getActivityContextInterface(activity);
					aci.attach(sbbLocalObject);					
				}
			}
			for (String auid : appUsagesToSubscribe) {
				if (!appUsagesAlreadySubscribed.contains(auid)) {
					// app usages already subscribed does not match this app
					// usage,
					// so subscribe it
					AppUsageActivity activity = dataSourceSbbInterface.createAppUsageActivity(auid);
					ActivityContextInterface aci = dataSourceActivityContextInterfaceFactory.getActivityContextInterface(activity);
					aci.attach(sbbLocalObject);
				}
			}

			// continue new subscription process
			sbb.getParentSbbCMP().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires, Response.OK,eventList,serverTransaction);
		} catch (JAXBException e) {
			logger.error("failed to parse resource-lists in initial subscribe",
					e);
			if (stringReader != null) {
				stringReader.close();
			}
			sbb.getParentSbbCMP().newSubscriptionAuthorization(subscriber,
					subscriberDisplayName, notifier, key, expires,
					Response.FORBIDDEN,eventList,serverTransaction);
		}

	}

	public void removingSubscription(Subscription subscription,XcapDiffSubscriptionControlSbbInterface sbb) {

		// get subscriptions map and remove subscription terminating
		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		if (subscriptionsMap != null) {
			Subscriptions subscriptions = subscriptionsMap
					.remove(subscription.getKey());

			// build set of other documents and app usages already subscribed by
			// this entity
			HashSet<DocumentSelector> documentSelectorsSubscribedByOthers = new HashSet<DocumentSelector>();
			HashSet<String> appUsagesSubscribedByOthers = new HashSet<String>();
			for (Subscriptions s : subscriptionsMap.getSubscriptions()) {
				for (DocumentSelector ds : s.getDocumentSelectors()) {
					documentSelectorsSubscribedByOthers.add(ds);
				}
				for (String auid : s.getAppUsages()) {
					appUsagesSubscribedByOthers.add(auid);
				}
			}

			// now unsubscribe each that was subscribed only by the subscription
			// terminating
			SbbLocalObject sbbLocalObject = sbb.getSbbContext().getSbbLocalObject();
			for(ActivityContextInterface aci : sbb.getSbbContext().getActivities()) {
				Object activity = aci.getActivity();
				if (activity instanceof DocumentActivity) {
					String aciDS = ((DocumentActivity)activity).getDocumentSelector();
					for (DocumentSelector ds : subscriptions.getDocumentSelectors()) {
						if (ds.toString().equals(aciDS) && !documentSelectorsSubscribedByOthers.contains(ds)) {
							// safe to unsubscribe this document
							aci.detach(sbbLocalObject);
						}
					}
				}
				else if (activity instanceof AppUsageActivity) {
					String aciAUID = ((AppUsageActivity)activity).getAUID();
					for (String auid : subscriptions.getAppUsages()) {
						if (auid.toString().equals(aciAUID) && !appUsagesSubscribedByOthers.contains(auid)) {
							// safe to unsubscribe this app usage
							aci.detach(sbbLocalObject);
						}
					}
				}
			}
		} else {
			logger
					.warn("Removing subscription but map of subscriptions is null");
		}
	}

	public NotifyContent getNotifyContent(Subscription subscription,XcapDiffSubscriptionControlSbbInterface sbb) {
		// let's gather all content this subscription
		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		Subscriptions subscriptions = subscriptionsMap
				.get(subscription.getKey());
		if (subscriptions != null) {
			HashMap<DocumentSelector, String> documentEtags = new HashMap<DocumentSelector, String>();
			// let's process first app usages
			for (String auid : subscriptions.getAppUsages()) {
				// get documents that exist in this app usage
				try {
					for (Document document : sbb.getDataSourceSbbInterface().getDocuments(auid)) {
						DocumentSelector documentSelector = new DocumentSelector(
								auid, document.getDocumentParent(), document.getDocumentName());
						if (document != null) {
							// TODO authorize inclusion of the document
							documentEtags.put(documentSelector, document
									.getETag());
						}						
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
			for (DocumentSelector documentSelector : subscriptions
					.getDocumentSelectors()) {
				Document document = null;
				try {
					document = sbb.getDataSourceSbbInterface().getDocument(
							documentSelector);
				} catch (InternalServerErrorException e) {
					logger.error(e);
				}
				if (document != null) {
					// borrow app usage object from cache
					// TODO AppUsage appUsage = appUsageCache.borrow(auid);
					// get auth policy
					// TODO AuthorizationPolicy authorizationPolicy =
					// appUsage.getAuthorizationPolicy();
					// TODO authorize inclusion of the document
					documentEtags.put(documentSelector, document.getETag());
				}
			}
			// build notify content
			XcapDiff xcapDiff = new XcapDiff();
			xcapDiff.setXcapRoot(ServerConfiguration.getInstance().getSchemeAndAuthority()
					+ ServerConfiguration.getInstance().getXcapRoot() + "/");
			ObjectFactory objectFactory = new ObjectFactory();
			for (DocumentSelector documentSelector : documentEtags.keySet()) {
				DocumentType documentType = objectFactory.createDocumentType();
				documentType.setSel(documentSelector.toString());
				documentType.setNewEtag(documentEtags.get(documentSelector));
				xcapDiff.getDocumentOrElementOrAttribute().add(documentType);
			}

			return new NotifyContent(xcapDiff, getXcapDiffContentTypeHeader(sbb));

		} else {

			return null;
		}
	}

	public Object filterContentPerSubscriber(Subscription subscription, Object unmarshalledContent, XcapDiffSubscriptionControlSbbInterface sbb) {
		return unmarshalledContent;
	}
	
	public void documentUpdated(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString,XcapDiffSubscriptionControlSbbInterface sbb) {
		XcapDiff xcapDiff = null;
		// for all subscriptions in this entity that have interest on the
		// document
		SubscriptionsMap subscriptionsMap = sbb.getSubscriptionsMap();
		if (subscriptionsMap != null) {

			for (Subscriptions s : subscriptionsMap.getSubscriptions()) {
				boolean doNotify = false;
				// document selector matches?
				for (DocumentSelector ds : s.getDocumentSelectors()) {
					if (ds.equals(documentSelector)) {
						doNotify = true;
						break;
					}
				}
				if (!doNotify) {
					// perhaps document selector has the same auid as one
					// subscribed?
					for (String auid : s.getAppUsages()) {
						if (auid.equals(documentSelector.getAUID())) {
							doNotify = true;
							break;
						}
					}
				}
				if (doNotify) {
					// TODO check if subscriber has authorization to read
					// document in case it is a subscription to an app usage
					//if (s.getAppUsages().contains(documentSelector.getAUID())) {					
					//}
					if (xcapDiff == null) {
						// lazy build of xcap diff
						xcapDiff = buildDocumentXcapDiff(documentSelector,
								newETag, oldETag);
					}
					// tell underlying sip event framework to notify subscriber
					sbb.getParentSbbCMP().notifySubscriber(s.getKey(),
							xcapDiff, getXcapDiffContentTypeHeader(sbb));
				}
			}

		}
	}

	private XcapDiff buildDocumentXcapDiff(DocumentSelector documentSelector,
			String newETag, String oldETag) {
		// build notify content
		XcapDiff xcapDiff = new XcapDiff();
		xcapDiff.setXcapRoot(ServerConfiguration.getInstance().getSchemeAndAuthority()
				+ ServerConfiguration.getInstance().getXcapRoot() + "/");
		ObjectFactory objectFactory = new ObjectFactory();
		DocumentType documentType = objectFactory.createDocumentType();
		documentType.setSel(documentSelector.toString());
		if (oldETag == null && newETag != null) {
			// document created
			documentType.setNewEtag(newETag);
		} else if (oldETag != null && newETag == null) {
			// document deleted
			documentType.setPreviousEtag(oldETag);
		} else if (oldETag != null && newETag != null) {
			// document replaced
			documentType.setNewEtag(newETag);
			documentType.setPreviousEtag(oldETag);
			// FIXME provide patch ops content
		}
		xcapDiff.getDocumentOrElementOrAttribute().add(documentType);
		return xcapDiff;
	}

	private static Logger logger = Logger
			.getLogger(XcapDiffSubscriptionControl.class);

}

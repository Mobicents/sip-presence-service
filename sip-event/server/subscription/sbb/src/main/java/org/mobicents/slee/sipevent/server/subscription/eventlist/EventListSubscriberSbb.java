package org.mobicents.slee.sipevent.server.subscription.eventlist;

import java.util.Set;

import gov.nist.javax.sip.Utils;

import javax.persistence.EntityManager;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;

import org.apache.log4j.Logger;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriberParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.EventListSubscriber;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlParentSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionClientControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.pojo.Subscription;
import org.mobicents.slee.sipevent.server.subscription.pojo.SubscriptionKey;
import org.mobicents.slee.sipevent.server.subscription.pojo.Subscription.Event;
import org.mobicents.slee.sipevent.server.subscription.pojo.Subscription.Status;

/**
 * 
 * Sbb that acts as the back end subscriber to the entries in a resource list.
 *  
 * @author Eduardo Martins
 * 
 */
public abstract class EventListSubscriberSbb implements Sbb,
		EventListSubscriber {

	private static final Logger logger = Logger
			.getLogger(EventListSubscriberSbb.class);
	
	// --- CMPs
	
	public abstract void setNotificationData(NotificationData value);
	public abstract NotificationData getNotificationData();
	
	public abstract void setFlatList(String uri);
	public abstract String getFlatList();
	
	public abstract void setSubscriptionKey(SubscriptionKey subscriptionKey);
	public abstract SubscriptionKey getSubscriptionKey();
	
	public abstract void setSubscriber(String subscriber);
	public abstract String getSubscriber();
	
	public abstract void setParentSbbCMP(EventListSubscriberParentSbbLocalObject parentSbb);
	public abstract EventListSubscriberParentSbbLocalObject getParentSbbCMP();
	
	// --- sbb logic
	
	public void setParentSbb(EventListSubscriberParentSbbLocalObject parentSbb) {
		setParentSbbCMP(parentSbb);
	}
	
	private String getVirtualSubscriptionId(SubscriptionKey originalSubscriptionKey,String virtualSubscriptionNotifier) {
		return originalSubscriptionKey.toString() + ":list:" +virtualSubscriptionNotifier;
	}
	
	public void subscribe(Subscription subscription, FlatList flatList, ActivityContextInterface flatListACI) {
		if (logger.isDebugEnabled()) {
			logger.debug("creating backend subscriptions for rls subscription "+subscription.getKey());
		}
		// store subscription data in cmp
		setSubscriptionKey(subscription.getKey());
		setFlatList(flatList.getServiceType().getUri());
		setSubscriber(subscription.getSubscriber());
		flatListACI.attach(sbbContext.getSbbLocalObject());
		// set notification data object, when a notification comes and this
		// object exists the notification data will be added, otherwise a new
		// NotificationData object for a single entry is created and the parent
		// is notified with the resulting multipart
		setNotificationData(new NotificationData(subscription.getNotifierWithParams(),subscription.getVersion(),flatList,Utils.getInstance().generateTag(),Utils.getInstance().generateTag()));
		// get subscription client child
		SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
		// create "virtual" subscriptions
		for (SerializableEntryType entryType : flatList.getEntries().values()) {
			subscriptionClient.subscribe(subscription.getSubscriber(), subscription.getSubscriberDisplayName(), entryType.getPojo().getUri(), subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryType.getPojo().getUri()), subscription.getExpires(), null, null, null);
		}
	}
	
	public void onFlatListRemovedEvent(FlatListRemovedEvent event, ActivityContextInterface aci) {
		if (logger.isDebugEnabled()) {
			logger.debug("flat list activity ending, terminating subscription "+getSubscriptionKey());
		}
		// time to remove the subscription
		unsubscribe(getSubscriber(), getSubscriptionKey(),event.getFlatList());
	}
	
	public void onFlatListUpdatedEvent(FlatListUpdatedEvent event, ActivityContextInterface aci) {
		Subscription subscription = getParentSbbCMP().getSubscription(getSubscriptionKey());
		if (subscription != null) {
			resubscribe(subscription, event.getFlatList(),event.getNewEntries(),event.getOldEntries(),event.getRemovedEntries());
		}
	}
	
	public void resubscribe(Subscription subscription, FlatList flatList) {
		resubscribe(subscription, flatList, null, flatList.getEntries().keySet(), null);
	}
	
	private void resubscribe(Subscription subscription, FlatList flatList, Set<String> newEntries, Set<String> oldEntries, Set<String> removedEntries) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("refreshing backend subscriptions for rls subscription "+subscription.getKey());
		}
		// version is incremented
		subscription.incrementVersion();
		// prepare for a full state notification
		setNotificationData(new NotificationData(subscription.getNotifierWithParams(),subscription.getVersion(),flatList,Utils.getInstance().generateTag(),Utils.getInstance().generateTag()));
		// get subscription client child
		SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
		// update "virtual" subscriptions
		if (removedEntries != null) {
			for (String entryUri : removedEntries) {
				subscriptionClient.unsubscribe(subscription.getSubscriber(), entryUri, subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryUri));
			}
		}
		if (oldEntries != null) {
			for (String entryUri : oldEntries) {
				subscriptionClient.resubscribe(subscription.getSubscriber(), entryUri, subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryUri), subscription.getExpires());
			}
		}
		if (newEntries != null) {
			for (String entryUri : newEntries) {
				subscriptionClient.subscribe(subscription.getSubscriber(), subscription.getSubscriberDisplayName(), entryUri, subscription.getKey().getEventPackage(), getVirtualSubscriptionId(subscription.getKey(),entryUri), subscription.getExpires(),null,null,null);
			}
		}
	}
	
	public void unsubscribe(Subscription subscription, FlatList flatList) {
		unsubscribe(subscription.getSubscriber(), subscription.getKey(), flatList);
	}
	
	private void unsubscribe(String subscriber, SubscriptionKey key, FlatList flatList) {
		if (logger.isDebugEnabled()) {
			logger.debug("removing backend subscriptions for rls subscription "+key);
		}
		for (ActivityContextInterface aci : sbbContext.getActivities()) {
			aci.detach(sbbContext.getSbbLocalObject());
		}
		// let's set the key as null so there are no further notifications from back end subscriptions
		setSubscriptionKey(null);
		// get subscription client child
		SubscriptionClientControlSbbLocalObject subscriptionClient = getSubscriptionClientControlSbb();
		// remove "virtual" subscriptions
		for (SerializableEntryType entryType : flatList.getEntries().values()) {
			subscriptionClient.unsubscribe(subscriber, entryType.getPojo().getUri(), key.getEventPackage(), getVirtualSubscriptionId(key,entryType.getPojo().getUri()));
		}
	}
	
	private Subscription getSubscription(EventListSubscriberParentSbbLocalObject parentSbb, SubscriptionKey key, String subscriber) {
		Subscription subscription = parentSbb.getSubscription(key);
		if (subscription == null && getSubscriptionKey() != null) {
			logger.warn("Unable to get subscription "+key+" from parent sbb, it does not exists anymore! Removing all virtual subscriptions");
			unsubscribe(subscriber, key,parentSbb.getFlatList(getFlatList()));
		}
		return subscription;
	}
	
	// --- SIP EVETN CHILD SBB CALL BACKS
	
	private NotificationData createPartialStateNotificationData(EventListSubscriberParentSbbLocalObject parentSbb, SubscriptionKey subscriptionKey, String subscriber, String notifier) {
		// get subscription
		Subscription subscription = getSubscription(parentSbb, subscriptionKey, subscriber);
		if (subscription != null) {
			// increment subscription version
			subscription.incrementVersion();
			EntityManager entityManager = subscription.getEntityManager();
			try {
				entityManager.flush();
				entityManager.close();
			}
			catch (Exception e) {				
				if (logger.isDebugEnabled()) {
					logger.debug("failed to update rls subscription", e);
				}
				return null;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("(before partial notification) flat list: "+getFlatList());
			}
			// create notification data for a single resource
			FlatList flatList = parentSbb.getFlatList(getFlatList());
			if (flatList != null) {
				return new NotificationData(subscription.getNotifierWithParams(),subscription.getVersion(),flatList.getEntries().get(notifier),Utils.getInstance().generateTag(), Utils.getInstance().generateTag());
			}			
		}
		return null;
	}
	
	public void notifyEvent(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			Event terminationReason, Status status, String content,
			String contentType, String contentSubtype) {
		
		SubscriptionKey subscriptionKey = getSubscriptionKey();
		
		// if key is null we are removing subscriptions and have no interest in further notifications
		
		if (subscriptionKey != null) {
		
			
			if (logger.isDebugEnabled()) {
				logger.debug("notification for rls subscription "+subscriptionKey+" from " + notifier);
			}

			EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
			NotificationData notificationData = getNotificationData();

			if (notificationData == null) {
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				if (notificationData == null) {
					// null then abort notification
					return;
				}
			}

			// add notification data
			String id = notifier;
			String cid = content != null ? id : null;
			MultiPart multiPart = null;
			try {
				multiPart = notificationData.addNotificationData(notifier, cid, id, content, contentType, contentSubtype, status.toString(), (terminationReason == null ? null : terminationReason.toString()));
			}
			catch (IllegalStateException e) {
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				if (notificationData == null) {
					// null then abort notification
					return;
				}
				multiPart = notificationData.addNotificationData(notifier, cid, id, content, contentType, contentSubtype, status.toString(), (terminationReason == null ? null : terminationReason.toString()));
			}
			// notify parent?
			if (multiPart != null) {
				setNotificationData(null);
				parentSbb.notifyEventListSubscriber(subscriptionKey, multiPart);
			}
		}
	}
	
	public void resubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("resubscribeError: sid="+subscriptionId+", error="+error);
		}
		
		EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
		SubscriptionKey key = getSubscriptionKey();
		
		switch (error) {
		case Response.CONDITIONAL_REQUEST_FAILED:
			// perhaps virtual subscription died, lets try to subscribe again
			Subscription subscription = getSubscription(parentSbb, key, subscriber);
			subscription.getEntityManager().close();
			if (subscription != null) {
				getSubscriptionClientControlSbb().subscribe(subscriber, subscription.getSubscriberDisplayName(), notifier, eventPackage, subscriptionId, subscription.getExpires(), null, null, null);
			}
			break;
	
		default:
			break;
		}
	}
	
	public void resubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires) {
		// ignore
		if (logger.isDebugEnabled()) {
			logger.debug("resubscribeOk: sid="+subscriptionId);
		}		
	}
	
	public void subscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		if (logger.isDebugEnabled()) {
			logger.debug("subscribeError: sid="+subscriptionId+", error="+error);
		}
		
		EventListSubscriberParentSbbLocalObject parentSbb = getParentSbbCMP();
		SubscriptionKey subscriptionKey = getSubscriptionKey();
		NotificationData notificationData = getNotificationData();
		
		if (notificationData == null) {
			notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
		}
		
		String cid = notifier;
		MultiPart multiPart = null;
		// add notification data
		switch (error) {
		case Response.FORBIDDEN:
			try {
				multiPart = notificationData.addNotificationData(notifier, null, cid, null, null, null, "terminated", "rejected");
			}
			catch (IllegalStateException e) {
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				multiPart = notificationData.addNotificationData(notifier, null, cid, null, null, null, "terminated", "rejected");
			}			
			break;
	
		default:
			try {
				multiPart = notificationData.notificationDataNotNeeded(notifier);
			}
			catch (IllegalStateException e) {
				// there is a chance that on a full state update concurrent backend subscriptions may try add notification data after multipart was built, if that happens we will get this exception and do a partial notification 
				notificationData = createPartialStateNotificationData(parentSbb, subscriptionKey, subscriber, notifier);
				multiPart = notificationData.notificationDataNotNeeded(notifier);
			}		
			break;
		}
		
		// notify parent?
		if (multiPart != null) {
			setNotificationData(null);
			parentSbb.notifyEventListSubscriber(subscriptionKey, multiPart);
		}
	}
	
	public void subscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int expires,
			int responseCode) {
		// ignore
		if (logger.isDebugEnabled()) {
			logger.debug("subscribeOk: sid="+subscriptionId);
		}
	}
	
	public void unsubscribeError(String subscriber, String notifier,
			String eventPackage, String subscriptionId, int error) {
		if (logger.isDebugEnabled()) {
			logger.debug("unsubscribeError: sid="+subscriptionId+", error="+error);
		}
	}
	
	public void unsubscribeOk(String subscriber, String notifier,
			String eventPackage, String subscriptionId) {
		if (logger.isDebugEnabled()) {
			logger.debug("unsubscribeOk: sid="+subscriptionId);
		}
	}
	
	// --- SIP EVENT CLIENT CHILD SBB
	
	public abstract ChildRelation getSubscriptionClientControlChildRelation();

	public abstract SubscriptionClientControlSbbLocalObject getSubscriptionClientControlChildSbbCMP();

	public abstract void setSubscriptionClientControlChildSbbCMP(
			SubscriptionClientControlSbbLocalObject value);

	public SubscriptionClientControlSbbLocalObject getSubscriptionClientControlSbb() {
		SubscriptionClientControlSbbLocalObject childSbb = getSubscriptionClientControlChildSbbCMP();
		if (childSbb == null) {
			try {
				childSbb = (SubscriptionClientControlSbbLocalObject) getSubscriptionClientControlChildRelation()
						.create();
			} catch (Exception e) {
				logger.error("Failed to create child sbb", e);
				return null;
			}
			setSubscriptionClientControlChildSbbCMP(childSbb);
			childSbb
					.setParentSbb((SubscriptionClientControlParentSbbLocalObject) this.sbbContext
							.getSbbLocalObject());
		}
		return childSbb;
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
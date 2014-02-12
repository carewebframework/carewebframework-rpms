/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gov.ihs.cwf.mbroker.BrokerSession;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.cal.api.domain.IPatient;
import org.carewebframework.common.StrUtil;

/**
 * Data access services for notifications.
 */
public class NotificationService {
    
    // Mail group lookup screen.
    private static final String MG_SCREEN = "I $P(^(0),U,2)'=\"PR\"!$D(^XMB(3.8,\"AB\",DUZ,Y))";
    
    private final BrokerSession broker;
    
    public NotificationService(BrokerSession broker) {
        this.broker = broker;
    }
    
    /**
     * Returns a bolus of mail groups.
     * 
     * @param startFrom Starting entry.
     * @param forward Direction of traversal.
     * @param result Result of lookup.
     */
    public void getGroups(String startFrom, boolean forward, Collection<Recipient> result) {
        List<String> lst = broker.callRPCList("CIAURPC FILGET", null, 3.8, startFrom, forward ? 1 : -1, MG_SCREEN, 40);
        toRecipients(lst, true, startFrom, result);
    }
    
    /**
     * Returns a bolus of users.
     * 
     * @param startFrom Starting entry.
     * @param forward Direction of traversal.
     * @param result Result of lookup.
     */
    public void getUsers(String startFrom, boolean forward, Collection<Recipient> result) {
        List<String> lst = broker.callRPCList("BEHOUSCX NEWPERS", null, startFrom, forward ? 1 : -1, null, null, "A");
        toRecipients(lst, false, startFrom, result);
    }
    
    /**
     * Returns notifications for the current user.
     * 
     * @param patient If not null, only notifications associated with the current user are returned.
     *            Otherwise, all notifications for the current user are returned.
     * @param result The list to receive the results.
     */
    public void getNotifications(IPatient patient, Collection<Notification> result) {
        List<String> lst = null;
        result.clear();
        
        if (patient == null) {
            lst = broker.callRPCList("BEHOXQ ALRLIST", null);
        } else if (patient != null) {
            lst = broker.callRPCList("BEHOXQ ALRLIST", null, patient.getDomainId());
        }
        
        if (lst != null) {
            for (String item : lst) {
                result.add(new Notification(item));
            }
        }
    }
    
    /**
     * Delete a notification.
     * 
     * @param notification The notification to delete.
     * @return True if the operation was successful.
     */
    public boolean deleteNotification(Notification notification) {
        boolean result = notification.canDelete();
        
        if (result) {
            broker.callRPC("BEHOXQ ALRPP", notification.getAlertId());
        }
        
        return result;
    }
    
    /**
     * Forward multiple notifications.
     * 
     * @param notifications List of notifications to forward.
     * @param recipients List of recipients.
     * @param comment Comment to attach to forwarded notification.
     */
    public void forwardNotifications(Collection<Notification> notifications, Collection<Recipient> recipients, String comment) {
        List<String> lst1 = new ArrayList<String>();
        
        for (Notification notification : notifications) {
            lst1.add(notification.getAlertId());
        }
        
        List<Long> lst2 = prepareRecipients(recipients);
        
        if (!lst1.isEmpty() && !lst2.isEmpty()) {
            broker.callRPC("BEHOXQ FORWARD", lst1, lst2, comment);
        }
    }
    
    /**
     * Prepares a recipient list for passing to an RPC.
     * 
     * @param recipients List of recipients.
     * @return List of recipients to pass to RPC.
     */
    private List<Long> prepareRecipients(Collection<Recipient> recipients) {
        List<Long> lst = new ArrayList<Long>();
        
        for (Recipient recipient : recipients) {
            lst.add(recipient.getIen());
        }
        
        return lst;
    }
    
    /**
     * Creates a list of recipients from a list of raw data.
     * 
     * @param recipientData List of raw data as returned by lookup.
     * @param isGroup If true, the list represents mail groups. If false, it represents users.
     * @param filter The text used in the lookup. It will be used to limit the returned results.
     * @param result List of recipients.
     */
    private void toRecipients(List<String> recipientData, boolean isGroup, String filter, Collection<Recipient> result) {
        result.clear();
        
        for (String data : recipientData) {
            Recipient recipient = new Recipient(data, isGroup);
            
            if (StringUtils.startsWithIgnoreCase(recipient.getName(), filter)) {
                result.add(recipient);
            } else {
                break;
            }
        }
    }
    
    /**
     * Returns the message associated with a notification, fetching it from the server if necessary.
     * 
     * @param notification A notification.
     * @return Message associated with the notification.
     */
    public List<String> getNotificationMessage(Notification notification) {
        List<String> message = notification.getMessage();
        
        if (message == null) {
            message = broker.callRPCList("BEHOXQ ALRMSG", null, notification.getAlertId());
            notification.setMessage(message);
        }
        
        return message;
    }
    
    /**
     * Returns all scheduled notifications for the current user.
     * 
     * @param result The list to receive the results.
     */
    public void getScheduledNotifications(Collection<ScheduledNotification> result) {
        List<String> lst = broker.callRPCList("BEHOXQ SCHLIST", null, "BEHOXQ");
        result.clear();
        
        for (String data : lst) {
            result.add(new ScheduledNotification(data));
        }
    }
    
    /**
     * Delete a scheduled notification.
     * 
     * @param notification Scheduled notification to delete.
     * @return True if the operation was successful.
     */
    public boolean deleteScheduledNotification(ScheduledNotification notification) {
        return broker.callRPCBool("BEHOXQ SCHDEL", notification.getIen());
    }
    
    /**
     * Returns a list of recipients associated with a scheduled notification.
     * 
     * @param notification A scheduled notification.
     * @param result Recipients associated with the notification.
     */
    public void getScheduledNotificationRecipients(ScheduledNotification notification, Collection<Recipient> result) {
        List<String> lst = broker.callRPCList("BEHOXQ SCHRECIP", null, notification.getIen());
        result.clear();
        
        for (String data : lst) {
            result.add(new Recipient(data));
        }
    }
    
    /**
     * Returns the message associated with a scheduled notification.
     * 
     * @param notification A scheduled notification.
     * @return Message associated with the scheduled notification.
     */
    public List<String> getScheduledNotificationMessage(ScheduledNotification notification) {
        return broker.callRPCList("BEHOXQ SCHMSG", null, notification.getIen());
    }
    
    /**
     * Creates a schedule notification. If the notification is replacing an existing one, the
     * existing one will be first deleted and a new one created in its place.
     * 
     * @param notification Notification to be scheduled.
     * @param message The associated message, if any.
     * @param recipients The target recipients.
     * @return True if the notification was successfully scheduled.
     */
    public boolean scheduleNotification(ScheduledNotification notification, List<String> message,
                                        Collection<Recipient> recipients) {
        if (notification.getIen() > 0) {
            deleteScheduledNotification(notification);
        }
        
        String extraInfo = StrUtil.fromList(Arrays.asList(notification.getExtraInfo()), StrUtil.U);
        return broker.callRPCBool("BEHOXQ SCHALR", notification.getDeliveryDate(), "BEHOXQ", notification.getSubject(),
            extraInfo, message, prepareRecipients(recipients));
    }
}

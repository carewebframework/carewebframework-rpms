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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;
import org.carewebframework.cal.api.domain.IPatient;
import org.carewebframework.common.NumUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.icons.IconUtil;
import org.carewebframework.ui.sharedforms.CaptionedForm;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.MessageWindow.MessageInfo;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.util.StringUtils;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

/**
 * Controller for main notification display.
 */
public class MainController extends CaptionedForm implements IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final String BOLD = "font-weight:bold";
    
    private static final String NO_BOLD = "color:lightgray";
    
    public static final String ICON_INFO = IconUtil.getIconPath("silk:16x16:information.png");
    
    public static final String ICON_ACTIONABLE = IconUtil.getIconPath("silk:16x16:bullet_go.png");
    
    public static final String ICON_TYPE = IconUtil.getIconPath("silk:16x16:help.png");
    
    public static final String ICON_PRIORITY = IconUtil.getIconPath("silk:16x16:bullet_error.png");
    
    public static final String ICON_INDICATOR = IconUtil.getIconPath("silk:16x16:asterisk_orange.png");
    
    // This is the renderer for the notification display.
    private final AbstractListitemRenderer<Notification, Object> renderer = new AbstractListitemRenderer<Notification, Object>() {
        
        @Override
        protected void renderItem(Listitem item, Notification notification) {
            createCell(item, null);
            createCell(item, null).setImage(notification.getPriority().getImage());
            createCell(item, null).setImage(notification.isActionable() ? ICON_ACTIONABLE : ICON_INFO);
            createCell(item, notification.getPatientName());
            createCell(item, notification.getSubject());
            createCell(item, notification.getDeliveryDate());
            item.setDisabled(isProcessing);
            service.getNotificationMessage(notification);
            item.setTooltiptext(notification.getDisplayText());
            item.addForward(Events.ON_DOUBLE_CLICK, item.getListbox(), "onProcessItem");
        }
    };
    
    // This is the listener for notification action messages.
    private final IGenericEvent<String> actionListener = new IGenericEvent<String>() {
        
        @Override
        public void eventCallback(String eventName, String eventData) {
            Action action = Action.valueOf(StrUtil.piece(eventName, ".", 2));
            Notification notification;
            
            switch (action) {
                case ADD:
                    addNotification(eventData);
                    break;
                
                case INFO:
                    notification = findNotification(eventData);
                    
                    if (notification != null) {
                        highlightNotification(notification);
                        getContainer().bringToFront();
                    }
                    
                    break;
                
                case REFRESH:
                    refresh();
                    break;
                
                case DELETE:
                    notification = findNotification(eventData);
                    
                    if (notification != null) {
                        model.remove(notification);
                    }
                    
                    break;
            }
        }
        
    };
    
    /**
     * Response types for information-only message processing.
     */
    private enum Response {
        YES, NO, ALL, CANCEL;
        
        @Override
        public String toString() {
            return Labels.getLabel("behnotification.response.label." + name());
        }
    }
    
    /**
     * Recognized notification actions.
     */
    private enum Action {
        CHECK, ADD, SCHEDULE, INFO, REFRESH, DELETE, MONITOR;
    }
    
    private Listbox lstNotification;
    
    private Radiogroup rgFilter;
    
    private Radio radAll;
    
    private Radio radPatient;
    
    private Button btnAll;
    
    private Button btnSelected;
    
    private Button btnInfoAll;
    
    private Button btnForward;
    
    private Button btnDelete;
    
    private Image imgIndicator;
    
    private NotificationService service;
    
    private ProcessingController processingController;
    
    private final ListModelList<Notification> model = new ListModelList<Notification>();
    
    private boolean showAll = true;
    
    private Priority alertThreshold = Priority.HIGH;
    
    private int alertDuration = 30;
    
    private boolean isProcessing;
    
    private IPatient patient;
    
    /**
     * Expose icon urls for auto-wiring.
     */
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
        comp.setAttribute("iconInfoOnly", ICON_INFO);
        comp.setAttribute("iconActionable", ICON_ACTIONABLE);
        comp.setAttribute("iconPriority", ICON_PRIORITY);
        comp.setAttribute("iconType", ICON_TYPE);
        comp.setAttribute("iconIndicator", ICON_INDICATOR);
        comp.setAttribute("iconPriorityHigh", Priority.HIGH.getImage());
        comp.setAttribute("iconPriorityMedium", Priority.MEDIUM.getImage());
        comp.setAttribute("iconPriorityLow", Priority.LOW.getImage());
    }
    
    /**
     * Set up display.
     */
    @Override
    public void init() {
        super.init();
        getContainer().registerProperties(this, "showAll", "alertDuration", "alertThreshold");
        rgFilter.setSelectedItem(showAll ? radAll : radPatient);
        processingController = ProcessingController.create(this);
        lstNotification.setItemRenderer(renderer);
        RowComparator.autowireColumnComparators(lstNotification.getListhead().getChildren());
        model.setMultiple(true);
        updatePatient(true);
        subscribe(true);
    }
    
    @Override
    public void cleanup() {
        subscribe(false);
    }
    
    /**
     * Refresh the display.
     */
    @Override
    public void refresh() {
        lstNotification.setModel((ListModel<?>) null);
        
        if (radAll.isChecked() || patient != null) {
            service.getNotifications(radAll.isChecked() ? null : patient, model);
        } else {
            model.clear();
        }
        
        lstNotification.setModel(model);
        Clients.resize(lstNotification);
        updateControls(false);
    }
    
    /**
     * Update controls to reflect the current selection state.
     * 
     * @param processingUpdate If true, a processing status update has occurred.
     */
    private void updateControls(boolean processingUpdate) {
        btnAll.setDisabled(isProcessing || model.isEmpty());
        btnDelete.setDisabled(isProcessing || !canDeleteSelected());
        btnInfoAll.setDisabled(isProcessing || !hasInfoOnly());
        btnSelected.setDisabled(isProcessing || model.getSelection().isEmpty());
        btnForward.setDisabled(isProcessing || btnSelected.isDisabled());
        radAll.setStyle(radAll.isChecked() ? BOLD : NO_BOLD);
        radPatient.setStyle(radPatient.isChecked() ? BOLD : NO_BOLD);
        
        if (processingUpdate) {
            lstNotification.setDisabled(isProcessing);
            ZKUtil.disableChildren(lstNotification, isProcessing);
        }
    }
    
    /**
     * Returns true if any selected notification may be deleted.
     * 
     * @return True if any selected notification may be deleted.
     */
    private boolean canDeleteSelected() {
        for (Notification notification : model.getSelection()) {
            if (notification.canDelete()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if any selected notification is information only.
     * 
     * @return True if any selected notification is information only.
     */
    private boolean hasInfoOnly() {
        for (Notification notification : model) {
            if (!notification.isActionable()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Prompts user for input, returning the response.
     * 
     * @param prompt Text prompt.
     * @param responses Valid responses.
     * @return Selected response.
     */
    private Response getResponse(String prompt, Response... responses) {
        return PromptDialog.show(prompt, null, responses);
    }
    
    /**
     * Creates a notification from raw data. Will be added to the model unless filtered. Will
     * generate a slide-down message alert if its priority exceeds the set threshold.
     * 
     * @param data Raw notification data.
     */
    private void addNotification(String data) {
        Notification notification = new Notification(data);
        service.getNotificationMessage(notification);
        
        if (radAll.isChecked()
                || (notification.hasPatient() && patient != null && notification.getDfn() == patient.getDomainId())) {
            model.add(notification);
        }
        
        if (alertThreshold != null && notification.getPriority().ordinal() <= alertThreshold.ordinal()) {
            MessageInfo mi = new MessageInfo(notification.getDisplayText(), "New Notification", notification.getPriority()
                    .getColor(), alertDuration * 1000, null, "cwf.fireLocalEvent('ALERT.INFO', '"
                    + notification.getAlertId() + "');");
            getEventManager().fireLocalEvent(MessageWindow.EVENT_SHOW, mi);
        }
    }
    
    /**
     * Locates and returns a notification based on its unique alert id.
     * 
     * @param alertId Alert id.
     * @return Notification with a matching alert id, or null if not found in the current model.
     */
    private Notification findNotification(String alertId) {
        for (Notification notification : model) {
            if (alertId.equals(notification.getAlertId())) {
                return notification;
            }
        }
        
        return null;
    }
    
    /**
     * Places the highlight indicator next to the specified notification. If the notification is not
     * found or is null, the indicator is hidden.
     * 
     * @param notification Notification to highlight.
     */
    protected void highlightNotification(Notification notification) {
        int i = notification == null ? -1 : model.indexOf(notification);
        
        if (i >= 0) {
            Listitem item = lstNotification.getItemAtIndex(i);
            imgIndicator.setParent(item.getFirstChild());
            imgIndicator.setVisible(true);
            Clients.scrollIntoView(item);
        } else {
            imgIndicator.setVisible(false);
        }
    }
    
    /**
     * Clears all selections.
     */
    private void clearSelection() {
        lstNotification.clearSelection();
        updateControls(false);
    }
    
    /**
     * Update controls when the selection changes.
     */
    public void onSelect$lstNotification() {
        updateControls(false);
    }
    
    /**
     * Refresh the display.
     */
    public void onClick$btnRefresh() {
        refresh();
    }
    
    /**
     * Delete selected notifications.
     */
    public void onClick$btnDelete() {
        boolean silent = false;
        
        LOOP: for (Notification notification : model.getSelection()) {
            String s = notification.getDisplayText();
            
            if (notification.canDelete()) {
                if (!silent) {
                    String msg = ZKUtil.getLabel("behnotification.main.delete.confirm.prompt", s);
                    
                    switch (getResponse(msg, Response.YES, Response.NO, Response.ALL, Response.CANCEL)) {
                        case NO:
                            continue;
                            
                        case ALL:
                            silent = true;
                            break;
                        
                        case CANCEL:
                            break LOOP;
                    }
                }
                service.deleteNotification(notification);
            } else {
                String msg = ZKUtil.getLabel("behnotification.main.delete.unable.prompt", s);
                
                if (getResponse(msg, Response.YES, Response.CANCEL) != Response.YES) {
                    break;
                }
            }
        }
    }
    
    /**
     * Refresh the display when the filter changes.
     */
    public void onCheck$radAll() {
        refresh();
    }
    
    /**
     * Refresh the display when the filter changes.
     */
    public void onCheck$radPatient() {
        refresh();
    }
    
    /**
     * Invoke the scheduled notification management dialog.
     */
    public void onClick$btnSchedule() {
        SchedulingController.show();
    }
    
    /**
     * Process all notifications.
     */
    public void onClick$btnAll() {
        processingController.process(model);
    }
    
    /**
     * Process all information-only notifications.
     */
    public void onClick$btnInfoAll() {
        processingController.process(getNotificationsToProcess(true));
    }
    
    /**
     * Process selected notifications.
     */
    public void onClick$btnSelected() {
        processingController.process(getNotificationsToProcess(false));
    }
    
    /**
     * Process a double-clicked notification.
     * 
     * @param event
     */
    public void onProcessItem$lstNotification(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Listitem item = (Listitem) event.getTarget();
        Notification notification = (Notification) item.getValue();
        processingController.process(Collections.singleton(notification));
    }
    
    /**
     * Forward selected notifications.
     */
    public void onClick$btnForward() {
        Set<Recipient> recipients = new HashSet<Recipient>();
        String comment = RecipientsController.showWithComment(recipients);
        
        if (comment != null && !recipients.isEmpty()) {
            service.forwardNotifications(getNotificationsToProcess(false), recipients, comment);
            clearSelection();
        }
    }
    
    /**
     * Return notifications to be processed.
     * 
     * @param infoOnly If true, return all information-only notifications. If false, return only
     *            selected notifications.
     * @return List of notifications to be processed.
     */
    private List<Notification> getNotificationsToProcess(boolean infoOnly) {
        List<Notification> list = new ArrayList<Notification>();
        
        for (Notification notification : model) {
            if (!infoOnly && model.isSelected(notification)) {
                list.add(notification);
            } else if (infoOnly && !notification.isActionable()) {
                list.add(notification);
            }
        }
        
        return list;
    }
    
    /**
     * Conditionally suppress patient context changes.
     */
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    /**
     * Update display when patient context changes.
     */
    @Override
    public void committed() {
        updatePatient(radPatient.isChecked());
    }
    
    /**
     * Update display for currently selected patient.
     * 
     * @param refresh If true, force a refresh before returning.
     */
    private void updatePatient(boolean refresh) {
        patient = PatientContext.getActivePatient();
        
        radPatient.setLabel(patient == null ? Labels.getLabel("behnotification.main.patient.not.selected") : patient
                .getFullName());
        
        if (refresh) {
            refresh();
        }
    }
    
    @Override
    public void canceled() {
    }
    
    /**
     * Subscribe to/unsubscribe from selected events.
     * 
     * @param doSubscribe If true, subscribe. If false, unsubscribe.
     */
    private void subscribe(boolean doSubscribe) {
        for (Action action : Action.values()) {
            String eventName = "ALERT." + action.name();
            
            if (doSubscribe) {
                getEventManager().subscribe(eventName, actionListener);
            } else {
                getEventManager().unsubscribe(eventName, actionListener);
            }
        }
    }
    
    /**
     * Allows IOC container to inject notification service.
     * 
     * @param service Notification service.
     */
    public void setNotificationService(NotificationService service) {
        this.service = service;
    }
    
    /**
     * Returns show all setting. If true, all notifications are displayed, regardless of any patient
     * association. If false, only notifications associated with the selected patient are displayed.
     * 
     * @return The show all setting.
     */
    public boolean getShowAll() {
        return showAll;
    }
    
    /**
     * Sets the show all setting. If true, all notifications are displayed, regardless of any
     * patient association. If false, only notifications associated with the selected patient are
     * displayed.
     * 
     * @param value The show all setting.
     */
    public void setShowAll(boolean value) {
        showAll = value;
        
        if (rgFilter != null) {
            rgFilter.setSelectedItem(showAll ? radAll : radPatient);
            refresh();
        }
    }
    
    /**
     * Returns the alert threshold as a string value. This threshold determines which newly arriving
     * notifications cause a slide-down message alert to be displayed.
     * 
     * @return The alert threshold.
     */
    public String getAlertThreshold() {
        return alertThreshold == null ? "" : alertThreshold.name();
    }
    
    /**
     * Sets the alert threshold as a string value. This threshold determines which newly arriving
     * notifications cause a slide-down message alert to be displayed.
     * 
     * @param value The alert threshold.
     */
    public void setAlertThreshold(String value) {
        this.alertThreshold = StringUtils.isEmpty(value) ? null : Priority.fromString(value);
    }
    
    /**
     * Returns the duration, in seconds, of any slide-down message alert.
     * 
     * @return Alert duration in seconds.
     */
    public int getAlertDuration() {
        return alertDuration;
    }
    
    /**
     * Sets the duration, in seconds, of any slide-down message alert.
     * 
     * @param alertDuration Alert duration in seconds.
     */
    public void setAlertDuration(int alertDuration) {
        this.alertDuration = NumUtil.enforceRange(alertDuration, 1, 999999);
    }
    
    public void setProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
        updateControls(true);
    }
}

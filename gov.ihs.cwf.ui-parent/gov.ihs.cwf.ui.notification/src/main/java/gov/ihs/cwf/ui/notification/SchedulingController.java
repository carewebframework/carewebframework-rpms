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

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 * Controller for viewing scheduled notifications.
 */
public class SchedulingController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(SchedulingController.class) + "scheduling.zul";
    
    /**
     * Renders the scheduled notifications.
     */
    private final AbstractListitemRenderer<ScheduledNotification, Object> renderer = new AbstractListitemRenderer<ScheduledNotification, Object>() {
        
        @Override
        protected void renderItem(Listitem item, ScheduledNotification notification) {
            createCell(item, null).setImage(notification.getPriority().getImage());
            createCell(item, notification.getDeliveryDate());
            createCell(item, notification.getPatientName());
            createCell(item, notification.getSubject());
            item.addForward(Events.ON_DOUBLE_CLICK, btnModify, Events.ON_CLICK);
        }
    };
    
    /**
     * Listens to events related to scheduled notifications.
     */
    private final IGenericEvent<String> alertEventListener = new IGenericEvent<String>() {
        
        @Override
        public void eventCallback(String eventName, String eventData) {
            refresh();
        }
        
    };
    
    private Listbox lstScheduled;
    
    private Button btnModify;
    
    private Button btnDelete;
    
    private NotificationService service;
    
    private final ListModelList<ScheduledNotification> model = new ListModelList<ScheduledNotification>();
    
    /**
     * Displays the scheduling controller modally.
     */
    public static void show() {
        PopupDialog.popup(DIALOG, true, false);
    }
    
    /**
     * Update controls to reflect the current selection state.
     */
    private void updateControls() {
        btnModify.setDisabled(lstScheduled.getSelectedItem() == null);
        btnDelete.setDisabled(btnModify.isDisabled());
    }
    
    /**
     * Adds a new scheduled notification.
     */
    public void onClick$btnAdd() {
        ScheduleController.show(null);
    }
    
    /**
     * Modifies an existing scheduled notification.
     */
    public void onClick$btnModify() {
        ScheduleController.show(getSelected());
    }
    
    /**
     * Refreshes the list.
     */
    public void onClick$btnRefresh() {
        refresh();
    }
    
    /**
     * Update controls when the selection changes.
     */
    public void onSelect$lstScheduled() {
        updateControls();
    }
    
    /**
     * Delete the selected scheduled notification.
     */
    public void onClick$btnDelete() {
        if (PromptDialog.confirm("@behnotification.scheduling.delete.confirm.prompt")) {
            service.deleteScheduledNotification(getSelected());
        }
    }
    
    /**
     * Returns the currently selected notification.
     * 
     * @return The currently selected notification.
     */
    private ScheduledNotification getSelected() {
        return (ScheduledNotification) lstScheduled.getSelectedItem().getValue();
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
        comp.setAttribute("iconPriority", MainController.ICON_PRIORITY);
    }
    
    /**
     * Initialize the dialog.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        lstScheduled.setItemRenderer(renderer);
        refresh();
        getEventManager().subscribe("ALERT.SCHEDULE", alertEventListener);
    }
    
    /**
     * Refresh the display.
     */
    @Override
    public void refresh() {
        lstScheduled.setModel((ListModel<?>) null);
        service.getScheduledNotifications(model);
        lstScheduled.setModel(model);
        updateControls();
    }
    
    /**
     * Unsubscribe on dialog closure.
     */
    public void onClose() {
        getEventManager().unsubscribe("ALERT.SCHEDULE", alertEventListener);
    }
    
    /**
     * Allows IOC container to inject notification service.
     * 
     * @param service Notification service.
     */
    public void setNotificationService(NotificationService service) {
        this.service = service;
    }
    
}

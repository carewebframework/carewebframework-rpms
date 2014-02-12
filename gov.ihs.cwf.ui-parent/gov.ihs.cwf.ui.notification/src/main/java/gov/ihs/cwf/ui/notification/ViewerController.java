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

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for individual notification display.
 */
public class ViewerController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(ViewerController.class) + "viewer.zul";
    
    protected static class ActionEvent extends Event {
        
        private static final long serialVersionUID = 1L;
        
        private final Notification notification;
        
        public ActionEvent(Notification notification, Action action) {
            super("onAction", null, action);
            this.notification = notification;
        }
        
        public Notification getNotification() {
            return notification;
        }
        
        public Action getAction() {
            return (Action) getData();
        }
    }
    
    public enum Action {
        DELETE, DELETE_ALL, SKIP, SKIP_ALL, CANCEL, VIEW
    };
    
    private NotificationService service;
    
    private Notification notification;
    
    private EventListener<ActionEvent> actionListener;
    
    private String defaultTitle;
    
    private Label lblHeader;
    
    private Button btnDelete;
    
    private Button btnDeleteAll;
    
    private Button btnSkipAll;
    
    private Button btnView;
    
    private Textbox txtMessage;
    
    private Caption caption;
    
    /**
     * Create an amodal instance of the viewer dialog.
     * 
     * @param actionListener Listener to respond to viewer action events.
     * @return The controller associated with the viewer dialog.
     */
    protected static ViewerController create(EventListener<ActionEvent> actionListener) {
        Window dlg = PopupDialog.popup(DIALOG, false, false, false);
        ViewerController infoOnlyController = (ViewerController) FrameworkController.getController(dlg);
        infoOnlyController.actionListener = actionListener;
        return infoOnlyController;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        defaultTitle = caption.getLabel();
    }
    
    /**
     * Process a single notification.
     * 
     * @param notification Notification to display.
     * @param message Optional message text to display. If null, the message text associated with
     *            the notification is displayed.
     */
    public void process(Notification notification, String message) {
        if (notification != null) {
            this.notification = notification;
            lblHeader.setValue(notification.getSubject());
            txtMessage.setText(message != null ? message : StrUtil.fromList(service.getNotificationMessage(notification)));
            txtMessage.setVisible(!txtMessage.getText().isEmpty());
            btnDelete.setDisabled(!notification.canDelete());
            btnDeleteAll.setDisabled(notification.isActionable() || btnDelete.isDisabled());
            btnSkipAll.setDisabled(notification.isActionable());
            btnView.setDisabled(!notification.hasPatient());
            caption.setLabel(notification.hasPatient() ? notification.getPatientName() : defaultTitle);
            root.setVisible(true);
        } else {
            onAction(null);
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
     * Delete the notification.
     */
    public void onClick$btnDelete() {
        if (PromptDialog.confirm(StrUtil.formatMessage("@behnotification.viewer.delete.confirm.prompt",
            notification.getSubject()))) {
            onAction(Action.DELETE);
        }
    }
    
    /**
     * Delete this and all remaining notifications.
     */
    public void onClick$btnDeleteAll() {
        if (PromptDialog.confirm(StrUtil.formatMessage("@behnotification.viewer.delete.all.confirm.prompt"))) {
            onAction(Action.DELETE_ALL);
        }
    }
    
    /**
     * Skip this notification.
     */
    public void onClick$btnSkip() {
        onAction(Action.SKIP);
    }
    
    /**
     * Skip this and all remaining notifications.
     */
    public void onClick$btnSkipAll() {
        onAction(Action.SKIP_ALL);
    }
    
    /**
     * Cancel notification processing.
     */
    public void onClick$btnCancel() {
        onAction(Action.CANCEL);
    }
    
    /**
     * Change context to patient associated with the notification.
     */
    public void onClick$btnView() {
        onAction(Action.VIEW);
    }
    
    /**
     * Forward the action to the listener.
     * 
     * @param action Action to forward.
     */
    protected void onAction(Action action) {
        root.setVisible(action == Action.VIEW);
        
        if (action != null && actionListener != null) {
            try {
                actionListener.onEvent(new ActionEvent(notification, action));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

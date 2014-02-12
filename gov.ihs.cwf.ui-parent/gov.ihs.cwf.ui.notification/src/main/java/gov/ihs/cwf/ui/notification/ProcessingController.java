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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import gov.ihs.cwf.domain.DomainObjectFactory;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.ui.notification.ViewerController.Action;
import gov.ihs.cwf.ui.notification.ViewerController.ActionEvent;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Window;

/**
 * Controller for processing notifications.
 */
public class ProcessingController extends FrameworkController implements IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(ProcessingController.class) + "processing.zul";
    
    private NotificationService service;
    
    private Iterator<Notification> iterator;
    
    private boolean requestingContextChange;
    
    private int currentIndex;
    
    private int total;
    
    private Action viewerAction;
    
    private ViewerController viewer;
    
    private MainController mainController;
    
    private Caption caption;
    
    /**
     * Listens and responds to action events originating from the viewer dialog.
     */
    private final EventListener<ActionEvent> actionListener = new EventListener<ActionEvent>() {
        
        @Override
        public void onEvent(ActionEvent event) throws Exception {
            viewerAction = event.getAction();
            processAction(viewerAction, event.getNotification());
        }
        
    };
    
    /**
     * Creates an amodal instance of the processing dialog.
     * 
     * @param mainController The requesting controller.
     * @return The controller associated with the newly created dialog.
     */
    protected static ProcessingController create(MainController mainController) {
        Window dlg = PopupDialog.popup(DIALOG, false, false, false);
        ProcessingController controller = (ProcessingController) FrameworkController.getController(dlg);
        controller.mainController = mainController;
        return controller;
    }
    
    /**
     * Creates a notification viewer instance for use by this controller.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        viewer = ViewerController.create(actionListener);
    }
    
    /**
     * Process the next notification.
     */
    public void onClick$btnNext() {
        doNext();
    }
    
    /**
     * Cancel all processing.
     */
    public void onClick$btnStop() {
        cancelProcessing();
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
     * Closes (hides) the processing dialog.
     */
    private void close() {
        root.setVisible(false);
        iterator = null;
        mainController.setProcessing(false);
    }
    
    /**
     * Returns true if notification processing is underway.
     * 
     * @return True if notification processing is underway.
     */
    private boolean isProcessing() {
        return iterator != null;
    }
    
    /**
     * Cancel all processing.
     */
    public void cancelProcessing() {
        close();
        viewer.onAction(null);
        mainController.highlightNotification(null);
    }
    
    /**
     * Process the specified notifications.
     * 
     * @param notifications The notifications to process.
     */
    public void process(Collection<Notification> notifications) {
        List<Notification> lst = new ArrayList<Notification>(notifications);
        currentIndex = 0;
        total = lst.size();
        iterator = lst.iterator();
        viewerAction = null;
        mainController.setProcessing(true);
        doNext();
    }
    
    /**
     * Handle a notification action.
     * 
     * @param action The action specified by the user.
     * @param notification The notification to be acted upon.
     */
    private void processAction(Action action, Notification notification) {
        switch (action) {
            case SKIP:
            case SKIP_ALL:
                break;
            
            case DELETE:
            case DELETE_ALL:
                service.deleteNotification(notification);
                break;
            
            case CANCEL:
                close();
                break;
            
            case VIEW:
                changePatient(notification);
                return;
        }
        
        doNext();
    }
    
    /**
     * Process the next notification. If there are no more notifications, cancel processing. If this
     * is the last notification, hide this dialog before processing the notification.
     */
    private void doNext() {
        if (iterator == null || !iterator.hasNext()) {
            cancelProcessing();
            return;
        }
        
        Notification notification = iterator.next();
        caption.setLabel(ZKUtil.getLabel("behnotification.processing.caption", ++currentIndex, total));
        caption.setTooltiptext(notification.getDisplayText());
        
        if (!iterator.hasNext()) {
            close();
        } else {
            root.setVisible(true);
        }
        
        process(notification);
    }
    
    /**
     * Process a single notification. If the notification is actionable, an event of the appropriate
     * type is fired. If the event is information-only, it is displayed in the notification viewer.
     * 
     * @param notification The notification to process.
     */
    private void process(Notification notification) {
        mainController.highlightNotification(notification);
        
        if (!notification.isActionable()) {
            if (viewerAction == Action.SKIP_ALL || viewerAction == Action.DELETE_ALL) {
                processAction(viewerAction, notification);
            } else {
                viewer.process(notification, null);
            }
        } else {
            String eventName = "NOTIFY." + notification.getType();
            
            if (getEventManager().hasSubscribers(eventName)) {
                viewer.onAction(null);
                String service = notification.getParam("SRV");
                
                if (service != null) {
                    SpringUtil.getBean(service);
                }
                
                if (changePatient(notification)) {
                    getEventManager().fireLocalEvent(eventName, notification);
                }
            } else {
                viewer.process(notification, ZKUtil.getLabel("behnotification.processing.nohandler", notification.getType()));
            }
        }
    }
    
    /**
     * Changes the patient context to the patient associated with the notification, if any.
     * 
     * @param notification A notification.
     * @return False if a context change was requested and rejected. Otherwise, true.
     */
    private boolean changePatient(Notification notification) {
        if (notification.hasPatient()) {
            Patient patient = DomainObjectFactory.get(Patient.class, notification.getDfn());
            
            try {
                requestingContextChange = true;
                PatientContext.changePatient(patient);
            } finally {
                requestingContextChange = false;
            }
            return PatientContext.getActivePatient() == patient;
        }
        return true;
    }
    
    /**
     * Disallow a patient context change while actively processing notifications, unless the context
     * change request originated from this controller.
     */
    @Override
    public String pending(boolean silent) {
        if (!requestingContextChange && !silent && isProcessing()
                && !PromptDialog.confirm("@behnotification.processing.cancel.confirm.prompt")) {
            return StrUtil.formatMessage("@behnotification.processing.cancel.rejected.message");
        }
        
        return null;
    }
    
    /**
     * Cancel processing when the patient context changes, unless the context change request
     * originated from this controller.
     */
    @Override
    public void committed() {
        if (!requestingContextChange) {
            cancelProcessing();
        }
    }
    
    @Override
    public void canceled() {
    }
}

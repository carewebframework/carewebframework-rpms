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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for adding recipients.
 */
public class RecipientsController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(RecipientsController.class) + "recipients.zul";
    
    /**
     * Renderer for each recipient list box. A double click target must be specified to which all
     * list item double click events will be forwarded as single click events.
     */
    private class ItemRenderer extends AbstractListitemRenderer<Recipient, Object> {
        
        private final Component doubleClickTarget;
        
        ItemRenderer(Component doubleClickTarget) {
            this.doubleClickTarget = doubleClickTarget;
        }
        
        @Override
        protected void renderItem(Listitem item, Recipient recipient) {
            item.addForward(Events.ON_DOUBLE_CLICK, doubleClickTarget, Events.ON_CLICK);
            item.setLabel(recipient.getName());
        }
    };
    
    private Listbox lstRecipients;
    
    private Listbox lstUsers;
    
    private Listbox lstGroups;
    
    private Textbox txtComment;
    
    private Button btnAdd;
    
    private Button btnRemove;
    
    private Button btnRemoveAll;
    
    private NotificationService service;
    
    private final ListModelSet<Recipient> modelRecipients = new ListModelSet<Recipient>();
    
    private final ListModelSet<Recipient> modelUsers = new ListModelSet<Recipient>();
    
    private final ListModelSet<Recipient> modelGroups = new ListModelSet<Recipient>();
    
    private Listbox lstActive;
    
    private Collection<Recipient> recipients;
    
    /**
     * Display the dialog modally, hiding the comment input element.
     * 
     * @param recipients Recipient list to update.
     * @return True if the recipient list was updated.
     */
    protected static boolean show(Collection<Recipient> recipients) {
        return show(recipients, false) != null;
    }
    
    /**
     * Display the dialog modally, showing the comment input element.
     * 
     * @param recipients Recipient list to update.
     * @return The comment text, or null if the dialog was cancelled.
     */
    protected static String showWithComment(Collection<Recipient> recipients) {
        return (String) show(recipients, true);
    }
    
    /**
     * Display the dialog modally.
     * 
     * @param recipients Recipient list to update.
     * @param showComment If true, display the comment input element. If false, hide it.
     * @return The value returned by the dialog or null if the dialog was cancelled.
     */
    private static Object show(Collection<Recipient> recipients, boolean showComment) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("recipients", recipients);
        args.put("showComment", showComment);
        return PopupDialog.popup(DIALOG, args, false, false, true).getAttribute("ok");
    }
    
    /**
     * Retrieve passed arguments. Initialize listbox renderers and models.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        recipients = (Collection<Recipient>) arg.get("recipients");
        txtComment.setVisible((Boolean) arg.get("showComment"));
        lstRecipients.setItemRenderer(new ItemRenderer(btnRemove));
        lstRecipients.setModel(modelRecipients);
        lstGroups.setItemRenderer(new ItemRenderer(btnAdd));
        lstGroups.setModel(modelGroups);
        lstUsers.setItemRenderer(new ItemRenderer(btnAdd));
        lstUsers.setModel(modelUsers);
        modelRecipients.addAll(recipients);
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
     * Update controllers when list box selection changes.
     */
    public void onSelect$lstUsers() {
        updateControls();
    }
    
    /**
     * Update controllers when list box selection changes.
     */
    public void onSelect$lstGroups() {
        updateControls();
    }
    
    /**
     * Update controllers when list box selection changes.
     */
    public void onSelect$lstRecipients() {
        updateControls();
    }
    
    /**
     * Set the active candidate list as the input focus changes.
     */
    public void onFocus$lstUsers() {
        setActiveList(lstUsers);
    }
    
    /**
     * Set the active candidate list as the input focus changes.
     */
    public void onFocus$lstGroups() {
        setActiveList(lstGroups);
    }
    
    /**
     * Set the active candidate list as the input focus changes.
     */
    public void onFocus$txtUsers() {
        setActiveList(lstUsers);
    }
    
    /**
     * Set the active candidate list as the input focus changes.
     */
    public void onFocus$txtGroups() {
        setActiveList(lstGroups);
    }
    
    /**
     * Perform user search based in text input.
     * 
     * @param event
     */
    public void onChanging$txtUsers(InputEvent event) {
        String text = event.getValue().trim().toUpperCase();
        
        if (text.length() >= 3) {
            service.getUsers(text, true, modelUsers);
        } else {
            modelUsers.clear();
        }
    }
    
    /**
     * Perform mail group search based in text input.
     * 
     * @param event
     */
    public void onChanging$txtGroups(InputEvent event) {
        String text = event.getValue().trim().toUpperCase();
        
        if (text.length() >= 3) {
            service.getGroups(text, true, modelGroups);
        } else {
            modelGroups.clear();
        }
    }
    
    /**
     * Sets the active candidate list and updates the controls.
     * 
     * @param lst A list box.
     */
    private void setActiveList(Listbox lst) {
        lstActive = lst;
        updateControls();
    }
    
    /**
     * Returns the recipient selected in the specified list, or null if no selection.
     * 
     * @param lst A list box.
     * @return The selected recipient, or null if no selection.
     */
    private Recipient getSelected(Listbox lst) {
        Listitem selItem = lst == null ? null : lst.getSelectedItem();
        return selItem == null ? null : (Recipient) selItem.getValue();
    }
    
    /**
     * Update controls to reflect the current selection state.
     */
    private void updateControls() {
        btnAdd.setDisabled(getSelected(lstActive) == null);
        btnRemove.setDisabled(getSelected(lstRecipients) == null);
        btnRemoveAll.setDisabled(modelRecipients.isEmpty());
    }
    
    /**
     * Add a recipient from the active candidate list.
     */
    public void onClick$btnAdd() {
        modelRecipients.add(getSelected(lstActive));
    }
    
    /**
     * Remove a recipient from the current recipient list.
     */
    public void onClick$btnRemove() {
        modelRecipients.remove(getSelected(lstRecipients));
    }
    
    /**
     * Remove all recipients from the current recipient list.
     */
    public void onClick$btnRemoveAll() {
        modelRecipients.clear();
    }
    
    /**
     * Update the original recipient list, set the response attribute, and close the dialog
     */
    public void onClick$btnOK() {
        recipients.clear();
        recipients.addAll(modelRecipients);
        root.setAttribute("ok", txtComment.isVisible() ? txtComment.getText() : true);
        root.detach();
    }
}

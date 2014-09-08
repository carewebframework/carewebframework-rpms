/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.chat;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for an individual chat session.
 */
public class SessionController extends FrameworkController implements IGenericEvent<String> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(SessionController.class) + "session.zul";
    
    private final AbstractListitemRenderer<Participant, Object> renderer = new AbstractListitemRenderer<Participant, Object>() {
        
        @Override
        protected void renderItem(Listitem item, Participant participant) {
            item.setLabel(participant.getName());
        }
        
    };
    
    private String sessionId;
    
    private ChatService chatService;
    
    private Listbox lstParticipants;
    
    private Component pnlDialog;
    
    private Textbox txtMessage;
    
    private Button btnSendMessage;
    
    private String eventName;
    
    private final ListModelSet<Participant> model = new ListModelSet<Participant>();
    
    /**
     * Creates a chat session bound to the specified session id.
     * 
     * @param sessionId The chat session id.
     * @return The controller for the chat session.
     */
    protected static SessionController create(String sessionId) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("id", sessionId);
        args.put("title", StrUtil.formatMessage("@vcchat.session.title", sessionId));
        Window dlg = PopupDialog.popup(DIALOG, args, true, true, false);
        dlg.doOverlapped();
        return (SessionController) FrameworkController.getController(dlg);
    }
    
    /**
     * Initialize the dialog.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        sessionId = (String) arg.get("id");
        eventName = "VCCHAT.SESSION." + sessionId;
        lstParticipants.setItemRenderer(renderer);
        doSubscribe(true);
        refresh();
        clearMessage();
    }
    
    /**
     * Refreshes the participant list.
     */
    @Override
    public void refresh() {
        lstParticipants.setModel((ListModel<?>) null);
        chatService.getParticipants(model, eventName);
        lstParticipants.setModel(model);
    }
    
    /**
     * Subscribe to / unsubscribe from events of interest.
     * 
     * @param subscribe If true, subscribe; false, unsubscribe.
     */
    private void doSubscribe(boolean subscribe) {
        if (subscribe) {
            getEventManager().subscribe(eventName, this);
            getEventManager().subscribe("SUBSCRIBE." + eventName, this);
            getEventManager().subscribe("UNSUBSCRIBE." + eventName, this);
        } else {
            getEventManager().unsubscribe(eventName, this);
            getEventManager().unsubscribe("SUBSCRIBE." + eventName, this);
            getEventManager().unsubscribe("UNSUBSCRIBE." + eventName, this);
        }
    }
    
    /**
     * Clear any text in the message text box.
     */
    private void clearMessage() {
        txtMessage.setText(null);
        updateControls(true);
    }
    
    /**
     * Updates control status.
     * 
     * @param disableSend If true, disable the send button.
     */
    private void updateControls(boolean disableSend) {
        btnSendMessage.setDisabled(disableSend);
        txtMessage.setFocus(true);
    }
    
    /**
     * Clear the message text.
     */
    public void onClick$btnClearMessage() {
        clearMessage();
    }
    
    /**
     * Send the message text.
     */
    public void onClick$btnSendMessage() {
        chatService.sendMessage(eventName, txtMessage.getText().trim());
        clearMessage();
    }
    
    /**
     * Clear the dialog panel.
     */
    public void onClick$btnClearDialog() {
        ZKUtil.detachChildren(pnlDialog);
    }
    
    /**
     * Refresh the display.
     */
    public void onClick$btnRefresh() {
        refresh();
    }
    
    /**
     * Invokes the participate invitation dialog.
     */
    public void onClick$btnInvite() {
        InviteController.show(sessionId, model);
    }
    
    /**
     * Enables the send button when text is present in the message text box.
     * 
     * @param event The onChanging event.
     */
    public void onChanging$txtMessage(InputEvent event) {
        updateControls(event.getValue().trim().isEmpty());
    }
    
    /**
     * Adds a dialog fragment to the dialog panel.
     * 
     * @param header Header for the fragment.
     * @param text Text for the fragment.
     */
    private void addDialog(String header, String text) {
        newLabel(header, "vcchat-dialog-header");
        newLabel(text, "vcchat-dialog-text");
    }
    
    /**
     * Adds a text entry to the dialog panel.
     * 
     * @param text Text to add.
     * @param sclass SClass of the added text.
     */
    private void newLabel(String text, String sclass) {
        Label lbl = new Label(text);
        lbl.setSclass(sclass);
        lbl.setMultiline(true);
        lbl.setPre(true);
        pnlDialog.appendChild(lbl);
        Clients.scrollIntoView(lbl);
    }
    
    /**
     * Adds a newly joined participant to the active participant list.
     * 
     * @param data Raw participant data.
     */
    private void addParticipant(String data) {
        Participant participant = new Participant(data);
        
        if (model.add(participant)) {
            addDialog(StrUtil.formatMessage("@vcchat.session.event.join", participant), null);
        }
    }
    
    /**
     * Remove a participant from the list;
     * 
     * @param data Raw participant data.
     */
    private void removeParticipant(String data) {
        Participant participant = new Participant(data);
        
        if (model.remove(participant)) {
            addDialog(StrUtil.formatMessage("@vcchat.session.event.leave", participant), null);
        }
    }
    
    /**
     * Returns the id of the session to which this controller is bound.
     * 
     * @return The session id.
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Catch the close event.
     */
    public void onClose() {
        close();
    }
    
    /**
     * Closes the chat dialog. Unsubscribes from all events and notifies the chat service.
     */
    public void close() {
        doSubscribe(false);
        root.detach();
        chatService.onSessionClosed(this);
    }
    
    /**
     * Allows IOC container to inject chat service.
     * 
     * @param chatService The chat service.
     */
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Handles all subscribed events.
     */
    @Override
    public void eventCallback(String eventName, String eventData) {
        eventName = StrUtil.piece(eventName, ".");
        
        if ("SUBSCRIBE".equals(eventName)) {
            addParticipant(eventData);
        } else if ("UNSUBSCRIBE".equals(eventName)) {
            removeParticipant(eventData);
        } else if ("VCCHAT".equals(eventName)) {
            String[] pcs = StringUtils.split(eventData, "\r\n", 2);
            addDialog(pcs[0], pcs[1]);
        }
    }
    
}

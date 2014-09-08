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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

/**
 * Controller for inviting participants to a chat session.
 */
public class InviteController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(InviteController.class) + "invite.zul";
    
    /**
     * Renderer for participant list.
     */
    private final AbstractListitemRenderer<Participant, Object> renderer = new AbstractListitemRenderer<Participant, Object>() {
        
        @Override
        protected void renderItem(Listitem item, Participant participant) {
            createCell(item, null);
            createCell(item, participant.getName());
            createCell(item, participant.getStation());
            createCell(item, participant.getSession());
            createCell(item, participant.getProcess());
            createCell(item, participant.getLoginTime());
            
            if (exclusions != null && exclusions.contains(participant)) {
                item.setCheckable(false);
                item.setDisabled(true);
                item.setSclass("vcchat-participant-active");
            }
            
            if (participant.getSession().equals(chatService.getSessionRoot())) {
                item.setSclass("vcchat-participant-self");
            }
        }
        
    };
    
    private Listbox lstSessions;
    
    private Button btnOK;
    
    private final ListModelSet<Participant> model = new ListModelSet<Participant>();
    
    private Collection<Participant> exclusions;
    
    private String sessionId;
    
    private ChatService chatService;
    
    /**
     * Displays the participant invitation dialog.
     * 
     * @param sessionId The id of the chat session making the invitation request.
     * @param exclusions List of participants that should be excluded from user selection.
     */
    public static void show(String sessionId, Collection<Participant> exclusions) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("sessionId", sessionId);
        args.put("exclusions", exclusions);
        PopupDialog.popup(DIALOG, args, true, true, true);
    }
    
    /**
     * Initialize the dialog.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        sessionId = (String) arg.get("sessionId");
        exclusions = (Collection<Participant>) arg.get("exclusions");
        model.setMultiple(lstSessions.isMultiple());
        lstSessions.setItemRenderer(renderer);
        RowComparator.autowireColumnComparators(lstSessions.getListhead().getChildren());
        refresh();
    }
    
    /**
     * Refresh the participant list.
     */
    @Override
    public void refresh() {
        lstSessions.setModel((ListModel<?>) null);
        chatService.getParticipants(model, null);
        lstSessions.setModel(model);
        updateControls();
    }
    
    /**
     * Updates controls to reflect the current selection state.
     */
    private void updateControls() {
        btnOK.setDisabled(model.isSelectionEmpty());
    }
    
    /**
     * Update control states when the selection state changes.
     */
    public void onSelect$lstSessions() {
        updateControls();
    }
    
    /**
     * Send invitations to the selected participants, then close the dialog.
     */
    public void onClick$btnOK() {
        chatService.invite(sessionId, model.getSelection());
        root.detach();
    }
    
    /**
     * Allows IOC container to inject chat service.
     * 
     * @param chatService The chat service.
     */
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }
}

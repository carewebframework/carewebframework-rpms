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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.messaging.Recipient;
import org.carewebframework.api.messaging.Recipient.RecipientType;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.MessageWindow.MessageInfo;
import org.carewebframework.vista.mbroker.BrokerSession;

/**
 * Chat service.
 */
public class ChatService implements IGenericEvent<String> {
    
    private static final String EVENT_CHAT_SERVICE = "VCCHAT.SERVICE";
    
    private final IEventManager eventManager;
    
    private final BrokerSession brokerSession;
    
    private final List<SessionController> sessions = new ArrayList<SessionController>();
    
    private boolean listening;
    
    private IUser user;
    
    private static final AtomicInteger lastId = new AtomicInteger();
    
    /**
     * Returns an instance of the chat service.
     * 
     * @return The chat service.
     */
    public static ChatService getInstance() {
        return SpringUtil.getBean("vcChatService", ChatService.class);
    }
    
    /**
     * Creates the chat service, supplying the broker and event manager instances.
     * 
     * @param brokerSession The broker session.
     * @param eventManager The event manager.
     */
    public ChatService(BrokerSession brokerSession, IEventManager eventManager) {
        this.brokerSession = brokerSession;
        this.eventManager = eventManager;
    }
    
    /**
     * Initialization of service.
     */
    public void init() {
        user = UserContext.getActiveUser();
        doSubscribe(true);
        ActionRegistry.register(false, "vcchat.create.session", "@vcchat.action.create.session",
            "zscript:" + ChatService.class.getName() + ".getInstance().createSession();");
    }
    
    /**
     * Tear-down of service. Closes any open sessions.
     */
    public void destroy() {
        doSubscribe(false);
        
        for (SessionController session : new ArrayList<SessionController>(sessions)) {
            session.close();
        }
    }
    
    /**
     * Subscribe to / unsubscribe from all events of interest.
     * 
     * @param subscribe If true, subscribe to events. If false, unsubscribe.
     */
    private void doSubscribe(boolean subscribe) {
        if (subscribe) {
            eventManager.subscribe(EVENT_CHAT_SERVICE, this);
        } else {
            eventManager.unsubscribe(EVENT_CHAT_SERVICE, this);
        }
    }
    
    /**
     * Returns the root identifier for sessions created by this service.
     * 
     * @return Session root.
     */
    public String getSessionRoot() {
        return Integer.toString(brokerSession.getId());
    }
    
    /**
     * Creates a new session id.
     * 
     * @return New session id.
     */
    private String newSessionId() {
        return getSessionRoot() + "-" + lastId.incrementAndGet();
    }
    
    /**
     * Creates a new session with a new session id.
     * 
     * @return The newly created session.
     */
    public SessionController createSession() {
        return createSession(newSessionId());
    }
    
    /**
     * Creates a new session with the specified session id.
     * 
     * @param sessionId The session id to associate with the new session.
     * @return The newly created session.
     */
    public SessionController createSession(String sessionId) {
        SessionController controller = SessionController.create(sessionId);
        sessions.add(controller);
        return controller;
    }
    
    /**
     * Called by a session controller when it closes.
     * 
     * @param session Session being closed.
     */
    protected void onSessionClosed(SessionController session) {
        sessions.remove(session);
    }
    
    /**
     * Respond to events:
     * <p>
     * VCCHAT.SERVICE.INVITE - An invitation has been received to join a dialog. Event stub format
     * is: Chat Session ID^Requester name
     * <p>
     * VCCHAT.SERVICE.ACCEPT - This client has accepted the invitation to join. Event stub format
     * is: Chat Session ID
     */
    @Override
    public void eventCallback(String eventName, String eventData) {
        String action = StrUtil.piece(eventName, ".", 3);
        
        if ("INVITE".equals(action)) {
            String[] pcs = StrUtil.split(eventData, StrUtil.U);
            MessageInfo mi = new MessageInfo(StrUtil.formatMessage("@vcchat.invitation.message", pcs[1]),
                    StrUtil.formatMessage("@vcchat.invitation.caption"), null, 999999, null,
                    "cwf.fireLocalEvent('VCCHAT.SERVICE.ACCEPT', '" + pcs[0] + "'); return true;");
            eventManager.fireLocalEvent(MessageWindow.EVENT_SHOW, mi);
            ;
        } else if ("ACCEPT".equals(action)) {
            createSession(eventData);
        }
    }
    
    /**
     * Returns true if the service is actively listening for events.
     * 
     * @return True if the service is actively listening.
     */
    public boolean isListening() {
        return listening;
    }
    
    /**
     * Sets the listening state of the service. When set to false, the service stops listening to
     * all chat-related events.
     * 
     * @param listening The listening state.
     */
    public void setListening(boolean listening) {
        this.listening = listening;
        doSubscribe(listening);
    }
    
    /**
     * Returns a list of participants. This is really a list of subscribers to a specified event.
     * 
     * @param participants Collection to receive the participant list.
     * @param eventName The name of the event whose subscribers are sought. This will normally be
     *            the event that a session controller uses to monitor chat-related activity. If
     *            null, all chat service subscribers are returned.
     * @return Same as the participants argument.
     */
    public Collection<Participant> getParticipants(Collection<Participant> participants, String eventName) {
        participants.clear();
        List<String> lst = brokerSession.callRPCList("RGNETBEV GETSUBSC", null,
            eventName == null ? EVENT_CHAT_SERVICE : eventName);
        
        for (String data : lst) {
            participants.add(new Participant(data));
        }
        
        return participants;
    }
    
    /**
     * Sends a message via the specified event.
     * 
     * @param eventName Event to use to deliver the message.
     * @param text The message text.
     */
    public void sendMessage(String eventName, String text) {
        if (text != null && !text.isEmpty()) {
            eventManager.fireRemoteEvent(eventName,
                user.getFullName() + " @ " + DateUtil.formatDate(brokerSession.getHostTime()) + "\r\n" + text);
        }
    }
    
    /**
     * Sends an invitation request to the specified invitees.
     * 
     * @param sessionId The id of the chat session making the invitation.
     * @param invitees The list of invitees. This will be used to constraint delivery of the
     *            invitation event to only those subscribers.
     */
    public void invite(String sessionId, Collection<Participant> invitees) {
        if (invitees == null || invitees.isEmpty()) {
            return;
        }
        
        List<Recipient> recipients = new ArrayList<>();
        
        for (Participant invitee : invitees) {
            recipients.add(new Recipient(RecipientType.SESSION, invitee.getSession()));
        }
        
        eventManager.fireRemoteEvent("VCCHAT.SERVICE.INVITE", sessionId + StrUtil.U + user.getFullName(),
            (Recipient[]) recipients.toArray());
    }
    
}

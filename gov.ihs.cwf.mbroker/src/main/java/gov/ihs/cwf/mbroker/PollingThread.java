/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.mbroker;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;

import gov.ihs.cwf.mbroker.Request.Action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class PollingThread extends Thread {
    
    private static final Log log = LogFactory.getLog(PollingThread.class);
    
    /**
     * Callback interface for handling background polling events.
     */
    public interface IHostEventHandler {
        
        /**
         * Called when the server signals an event to the client.
         * 
         * @param name Name of the event.
         * @param data Data associated with the event.
         */
        void onHostEvent(String name, Object data);
        
    }
    
    private boolean enabled;
    
    private boolean terminated;
    
    private int pollingInterval = 3000;
    
    private final WeakReference<BrokerSession> sessionRef;
    
    private final Object monitor = new Object();
    
    private Request query;
    
    private Request ping;
    
    public PollingThread(BrokerSession session) {
        super();
        setName("MBrokerPollingDaemon-" + getId());
        this.sessionRef = new WeakReference<BrokerSession>(session);
        ExecutorService executor = session.getExecutorService();
        
        if (executor != null) {
            executor.execute(this);
        } else {
            start();
        }
    }
    
    public void terminate() {
        enabled = false;
        terminated = true;
        wakeup();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    private Request getRequest(Action action, BrokerSession session) {
        switch (action) {
            case QUERY:
                if (query == null) {
                    query = new Request(action);
                    query.addParameter("UID", session.getId());
                }
                
                return query;
                
            case PING:
                if (ping == null) {
                    ping = new Request(action);
                }
                
                return ping;
                
            default:
                return null;
        }
    }
    
    private void pollHost(BrokerSession session) {
        Request request = !enabled ? null : getRequest(session.pollingAction(), session);
        
        if (request == null) {
            return;
        }
        
        try {
            Response response = session.netCall(request, 1000);
            String results[] = response.getData().split(Constants.LINE_SEPARATOR, 2);
            String params[] = StrUtil.split(results[0], StrUtil.U, 2);
            
            switch (response.getResponseType()) {
                case ACK:
                    int i = StrUtil.toInt(params[0]);
                    
                    if (i > 0) {
                        pollingInterval = i * 1000;
                    }
                    
                    FMDate hostTime = new FMDate(params[1]);
                    session.setHostTime(hostTime);
                    break;
                
                case ASYNC:
                    int asyncHandle = StrUtil.toInt(params[0]);
                    int asyncError = StrUtil.toInt(params[1]);
                    
                    if (asyncHandle > 0) {
                        if (asyncError != 0) {
                            session.onRPCError(asyncHandle, asyncError, results[1]);
                        } else {
                            session.onRPCComplete(asyncHandle, results[1]);
                        }
                    }
                    break;
                
                case EVENT:
                    List<IHostEventHandler> hostEventHandlers = session.getHostEventHandlers();
                    
                    if (hostEventHandlers != null) {
                        try {
                            String eventName = results[0];
                            String result = results[1];
                            Object eventData = null;
                            
                            if (result.startsWith(Constants.JSON_PREFIX)) {
                                eventData = JSONUtil.deserialize(result.substring(Constants.JSON_PREFIX.length()));
                            } else {
                                eventData = result;
                            }
                            
                            for (IHostEventHandler hostEventHandler : hostEventHandlers) {
                                try {
                                    hostEventHandler.onHostEvent(eventName, eventData);
                                } catch (Throwable e) {
                                    log.error("Host event subscriber threw an exception", e);
                                }
                            }
                        } catch (Throwable e) {
                            log.error("Error processing host event", e);
                        }
                    }
                    break;
            }
        } catch (Throwable e) {
            log.error("Error processing polling response.", e);
            terminate();
        }
    }
    
    /**
     * Wakes up the background thread.
     * 
     * @return
     */
    private synchronized boolean wakeup() {
        try {
            synchronized (monitor) {
                monitor.notify();
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    
    @Override
    public void run() {
        synchronized (monitor) {
            while (!terminated) {
                try {
                    BrokerSession session = this.sessionRef.get();
                    
                    if (session == null) {
                        break;
                    } else {
                        pollHost(session);
                    }
                    session = null;
                    monitor.wait(pollingInterval);
                } catch (InterruptedException e) {}
            }
        }
        
        log.debug(getName() + " has exited.");
    }
}

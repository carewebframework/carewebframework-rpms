/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.ui.common;

import java.util.Iterator;

import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.ui.mbroker.AsyncRPCAbortEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCCompleteEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCErrorEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCEventDispatcher;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

public class BgoBaseController<T> extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private BrokerSession broker;
    
    protected T result;
    
    private boolean canceled;
    
    private AsyncRPCEventDispatcher asyncDispatcher;
    
    public Component getRoot() {
        return root;
    }
    
    /**
     * Returns an iterator for passed parameters.
     * 
     * @return Iterator for passed parameters.
     */
    public Iterator<Object> getParameters() {
        return arg instanceof Params ? ((Params) arg).iterator() : new Params().iterator();
    }
    
    public T getResult() {
        return result;
    }
    
    public boolean canceled() {
        return canceled;
    }
    
    public void close(boolean canceled) {
        this.canceled = canceled;
        Events.postEvent("onDeferredClose", root, null);
    }
    
    public void onDeferredClose() {
        root.detach();
    }
    
    /**
     * Called in main event thread when async RPC has completed. Override to handle the event.
     * 
     * @param event The async completion event.
     */
    public void onAsyncRPCComplete(AsyncRPCCompleteEvent event) {
    }
    
    /**
     * Called in main event thread when async RPC has encountered an error. Override to handle the
     * event.
     * 
     * @param event The async error event.
     */
    public void onAsyncRPCError(AsyncRPCErrorEvent event) {
    }
    
    /**
     * Called in main event thread when async RPC has been aborted. Override to handle the event.
     * 
     * @param event The async abort event.
     */
    public void onAsyncRPCAbort(AsyncRPCAbortEvent event) {
    }
    
    public AsyncRPCEventDispatcher getAsyncDispatcher() {
        if (asyncDispatcher == null) {
            asyncDispatcher = new AsyncRPCEventDispatcher(getBroker(), root);
        }
        
        return asyncDispatcher;
    }
    
    public BrokerSession getBroker() {
        if (broker == null) {
            broker = VistAUtil.getBrokerSession();
        }
        
        return broker;
    }
    
    public void setBroker(BrokerSession broker) {
        this.broker = broker;
    }
    
}

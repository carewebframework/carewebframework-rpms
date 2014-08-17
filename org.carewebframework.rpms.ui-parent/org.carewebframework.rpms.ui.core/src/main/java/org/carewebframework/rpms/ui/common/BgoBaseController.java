/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.common;

import java.util.Iterator;

import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;

import org.zkoss.zk.ui.Component;

public class BgoBaseController<T> extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private Component root;
    
    private BrokerSession broker;
    
    protected T result;
    
    private boolean canceled;
    
    public Component getRoot() {
        return root;
    }
    
    public Iterator<Object> getParameters() {
        return ((Params) arg).iterator();
    }
    
    public T getResult() {
        return result;
    }
    
    public boolean canceled() {
        return canceled;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
    }
    
    public void close(boolean canceled) {
        this.canceled = canceled;
        root.detach();
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

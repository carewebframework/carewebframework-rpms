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

import java.util.ArrayList;
import java.util.List;

public class RPCParameters {
    
    private final List<RPCParameter> params = new ArrayList<RPCParameter>();
    
    public RPCParameters() {
        
    }
    
    public RPCParameters(Object... args) {
        int i = 0;
        
        for (Object arg : args) {
            RPCParameter param = get(i++);
            
            if (arg instanceof Iterable<?>) {
                param.assign((Iterable<?>) arg);
            } else if (arg != null && arg.getClass().isArray()) {
                param.assignArray(arg);
            } else {
                param.setValue(arg);
            }
        }
    }
    
    public int add() {
        int result = params.size();
        params.add(new RPCParameter());
        return result;
    }
    
    public RPCParameter get(int index) {
        expand(index + 1);
        return params.get(index);
    }
    
    public int getCount() {
        return params.size();
    }
    
    public void clear() {
        params.clear();
    }
    
    public void assign(RPCParameters source) {
        clear();
        
        for (RPCParameter param : source.params) {
            params.add(new RPCParameter(param));
        }
    }
    
    public void put(int index, RPCParameter param) {
        expand(index);
        
        if (index < params.size()) {
            params.set(index, param);
        } else {
            params.add(param);
        }
    }
    
    private void expand(int size) {
        while (size > params.size()) {
            add();
        }
    }
}

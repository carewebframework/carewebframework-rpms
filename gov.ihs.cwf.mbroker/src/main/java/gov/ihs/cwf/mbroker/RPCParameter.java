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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RPCParameter implements Iterable<String> {
    
    public static enum HasData {
        NONE, SCALAR, VECTOR, BOTH
    };
    
    private final Map<String, Object> values = new HashMap<String, Object>();
    
    public RPCParameter() {
        
    }
    
    public RPCParameter(RPCParameter source) {
        assign(source);
    }
    
    public int getCount() {
        return values.size();
    }
    
    public HasData hasData() {
        boolean scalar = values.containsKey("");
        boolean vector = scalar ? getCount() > 1 : getCount() > 0;
        return scalar && vector ? HasData.BOTH : scalar ? HasData.SCALAR : vector ? HasData.VECTOR : HasData.NONE;
    }
    
    public Object getValue() throws Exception {
        return get("");
    }
    
    public Object get(String subscript) {
        if (!values.containsKey(subscript)) {
            throw new RuntimeException("Subscript not found");
        }
        
        return values.get(subscript);
    }
    
    public void assign(RPCParameter source) {
        clear();
        values.putAll(source.values);
    }
    
    public void assign(Iterable<?> source) {
        clear();
        int i = 0;
        
        for (Object value : source) {
            values.put(Integer.toString(++i), value);
        }
    }
    
    public void assignArray(Object source) {
        int len = Array.getLength(source);
        List<Object> list = new ArrayList<Object>(len);
        
        for (int i = 0; i < len; i++) {
            list.add(Array.get(source, i));
        }
        
        assign(list);
    }
    
    public void clear() {
        values.clear();
    }
    
    public void delete(String subscript) {
        values.remove(subscript);
    }
    
    public void put(String subscript, Object value) {
        if (value == null) {
            values.remove(subscript);
        } else {
            values.put(subscript, value);
        }
    }
    
    public void put(Object[] subscript, Object value) {
        put(BrokerUtil.buildSubscript(subscript), value);
    }
    
    public void put(Iterable<Object> subscript, Object value) {
        put(BrokerUtil.buildSubscript(subscript), value);
    }
    
    public void setValue(Object value) {
        put("", value);
    }
    
    @Override
    public Iterator<String> iterator() {
        return values.keySet().iterator();
    }
}

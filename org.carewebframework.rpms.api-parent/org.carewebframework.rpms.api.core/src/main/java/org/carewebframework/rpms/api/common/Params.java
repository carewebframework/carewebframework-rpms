/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.common;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Simple means to package parameters for passing to a form controller.
 */
public class Params extends HashMap<Object, Object> implements Iterable<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private class ParamsIterator implements Iterator<Object> {
        
        private int index;
        
        @Override
        public boolean hasNext() {
            return index < size();
        }
        
        @Override
        public Object next() {
            return !hasNext() ? null : get(index++);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public Params(Object... params) {
        int i = 0;
        
        for (Object param : params) {
            put(i++, param);
        }
        
    }
    
    @Override
    public Iterator<Object> iterator() {
        return new ParamsIterator();
    }
    
}

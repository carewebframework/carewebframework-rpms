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

import org.apache.commons.lang.StringUtils;

public class RPCException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private int code = 0;
    
    private static class ParsedException {
        
        private int code;
        
        private String text;
        
        private ParsedException(String text) {
            int i = text.indexOf(Constants.LINE_SEPARATOR);
            String first = i < 1 ? "" : text.substring(0, i);
            
            if (StringUtils.isNumeric(first)) {
                code = Integer.parseInt(first);
                this.text = text.substring(++i);
            } else {
                this.text = text;
            }
        }
    }
    
    public RPCException(String text) {
        this(parseText(text));
    }
    
    private RPCException(ParsedException value) {
        super(value.text);
        this.code = value.code;
    }
    
    private static ParsedException parseText(String text) {
        return new ParsedException(text);
    }
    
    public int getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return code + ": " + super.toString();
    }
}

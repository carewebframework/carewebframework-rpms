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

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class BrokerUtil {
    
    private static final String QT = "\"";
    
    private static final String QT2 = "\"\"";
    
    public static String toString(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }
        
        if (value instanceof Double) {
            String val = value.toString();
            return val.startsWith("0.") ? val.substring(1) : val.startsWith("-0.") ? "-" + val.substring(2) : val;
        }
        
        if (value instanceof FMDate) {
            return ((FMDate) value).getFMDate();
        }
        
        if (value instanceof Date) {
            return new FMDate((Date) value).getFMDate();
        }
        
        return value == null ? "" : value.toString();
    }
    
    public static String buildSubscript(Object[] subscripts) {
        return buildSubscript(Arrays.asList(subscripts));
    }
    
    public static String buildSubscript(Iterable<Object> subscripts) {
        StringBuilder sb = new StringBuilder();
        
        for (Object subscript : subscripts) {
            String value = toString(subscript);
            
            if (value.isEmpty()) {
                throw new RuntimeException("Null subscript not allowed.");
            }
            
            if (sb.length() > 0) {
                sb.append(",");
            }
            
            if (StringUtils.isNumeric(value)) {
                sb.append(value);
            } else {
                sb.append(QT);
                sb.append(value.replaceAll(QT, QT2));
                sb.append(QT);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Enforces static class.
     */
    private BrokerUtil() {
    };
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.plugin.problemlist.util;

import org.carewebframework.rpms.api.domain.Problem;

public enum ProblemFilter {
    NONE, ACTIVE, INACTIVE, PERSONAL, FAMILY, ACTIVE_PERSONAL;
    
    public boolean include(Problem problem) {
        char status = ProblemUtil.getStatus(problem).charAt(0);
        
        switch (this) {
            case NONE:
                return true;
                
            case ACTIVE:
                return status == 'A';
                
            case ACTIVE_PERSONAL:
                return status == 'A' || status == 'P';
                
            case INACTIVE:
                return status == 'I';
                
            case PERSONAL:
                return status == 'P';
                
            case FAMILY:
                return status == 'F';
        }
        
        return false;
    }
    
    public static ProblemFilter valueOf(String value, ProblemFilter dflt) {
        try {
            return ProblemFilter.valueOf(value);
        } catch (Throwable t) {
            return dflt;
        }
    }
}

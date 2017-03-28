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

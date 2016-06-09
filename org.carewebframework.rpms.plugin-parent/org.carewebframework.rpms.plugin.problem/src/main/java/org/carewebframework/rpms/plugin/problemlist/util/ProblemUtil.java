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

public class ProblemUtil {
    
    /**
     * Returns the problem status for display.
     * 
     * @param problem The problem.
     * @return Displayable problem status.
     */
    public static String getStatus(Problem problem) {
        String cls = problem.getProblemClass();
        String status = problem.getStatus();
        return "P".equals(cls) ? "Personal History" : "F".equals(cls) ? "Family History" : "A".equals(status) ? "Active"
                : "Inactive";
    }
    
    /**
     * Enforces static class.
     */
    private ProblemUtil() {
    };
}

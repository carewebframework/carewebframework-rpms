/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.terminology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermSubset {
    
    private final String systemId;
    
    private final Map<String, String> members = new HashMap<>();
    
    public TermSubset(String systemId, List<String> data) {
        this.systemId = systemId;
        
        for (int i = 1; i < data.size(); i++) {
            String[] pcs = data.get(i).split("\\^");
            members.put(pcs[0], pcs[1]);
        }
    }
    
    public String getSystemId() {
        return systemId;
    }
    
    public Map<String, String> getMembers() {
        return members;
    }
    
    public String getMemberName(String id) {
        return members.get(id);
    }
}

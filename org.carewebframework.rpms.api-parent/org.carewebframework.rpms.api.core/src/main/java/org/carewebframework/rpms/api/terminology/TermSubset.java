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

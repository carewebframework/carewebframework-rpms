/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.JSONUtil;

public class PropertyDefinition extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("PropDefinition", PropertyDefinition.class);
    }
    
    private static final Map<String, PropertyDefinition> cache = new HashMap<String, PropertyDefinition>();
    
    private String name;
    
    private String displayName;
    
    private boolean multiValued;
    
    private String dataType;
    
    private boolean readOnly;
    
    private final List<String> description = new ArrayList<String>();
    
    public static synchronized PropertyDefinition get(String name) {
        PropertyDefinition def = cache.get(name);
        
        if (def == null) {
            def = PropertyUtil.getPropertyDAO().getDefinition(name);
            cache.put(name, def);
        }
        
        return def;
    }
    
    public static synchronized void clearCache() {
        cache.clear();
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    protected void setDescription(List<String> description) {
        this.description.clear();
        this.description.addAll(description);
    }
    
    protected void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }
    
    protected void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isMultiValued() {
        return multiValued;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
}

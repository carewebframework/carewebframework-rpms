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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;

public class Property {
    
    private String instanceId;
    
    private final List<String> values = new ArrayList<String>();
    
    private PropertyDefinition definition;
    
    private String entityList;
    
    private String format;
    
    private boolean modified;
    
    private boolean fetched;
    
    private final IPropertyDAO propertyDAO = PropertyUtil.getPropertyDAO();
    
    public Property(String name) {
        init(name, null, null, null);
    }
    
    public Property(String name, String instanceId) {
        init(name, instanceId, null, null);
    }
    
    public Property(String name, String instanceId, String entityList) {
        init(name, instanceId, entityList, null);
    }
    
    public Property(String name, String instanceId, String entityList, String format) {
        init(name, instanceId, entityList, format);
    }
    
    private void init(String name, String instanceId, String entityList, String format) {
        this.instanceId = instanceId;
        this.entityList = entityList;
        this.format = format;
        definition = PropertyDefinition.get(name);
        reset();
    }
    
    public int getValueListSize() {
        fetch();
        return values.size();
    }
    
    public List<String> getValues() {
        fetch();
        return values;
    }
    
    public String getValue() {
        fetch();
        return StrUtil.fromList(values);
    }
    
    public void setValues(Iterable<String> values) {
        checkReadOnly();
        clearValues();
        
        if (values != null) {
            for (String value : values) {
                this.values.add(value);
            }
        }
    }
    
    public void setValue(String value) {
        checkReadOnly();
        clearValues();
        
        if (value != null) {
            values.add(value);
        }
    }
    
    public String getName() {
        return definition.getName();
    }
    
    public String getDisplayName() {
        return definition.getDisplayName();
    }
    
    public boolean isReadOnly() {
        return definition.isReadOnly();
    }
    
    public String getDescription() {
        return StrUtil.fromList(definition.getDescription());
    }
    
    public boolean isMultiValued() {
        return definition.isMultiValued();
    }
    
    public String getDataType() {
        return definition.getDataType();
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        reset();
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setEntityList(String entityList) {
        this.entityList = entityList;
        reset();
    }
    
    public String getEntityList() {
        return entityList;
    }
    
    public int getEntityPriority(String entity) {
        if (StringUtils.isEmpty(entity) || StringUtils.isEmpty(entityList)) {
            return -1;
        }
        
        return StrUtil.toList(entityList, ";").indexOf(entity);
    }
    
    public void setFormat(String format) {
        this.format = format;
        reset();
    }
    
    public String getFormat() {
        return format;
    }
    
    public void clearValues() {
        values.clear();
        modified = true;
        fetched = true;
    }
    
    public void reset() {
        values.clear();
        modified = false;
        fetched = false;
    }
    
    private void fetch() {
        if (!fetched) {
            reset();
            fetched = true;
            propertyDAO.fetchValue(this, values);
        }
    }
    
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    public boolean isDefined() {
        return definition.getDomainId() > 0;
    }
    
    public void saveValues() {
        saveValues(entityList == null ? null : StrUtil.piece(entityList, StrUtil.U));
    }
    
    public void saveValues(String entity) {
        checkReadOnly();
        
        if (modified) {
            propertyDAO.saveValue(this, entity);
            modified = false;
        }
    }
    
    private void checkReadOnly() {
        if (definition.isReadOnly()) {
            throw new RuntimeException("Property is readonly.");
        }
    }
}

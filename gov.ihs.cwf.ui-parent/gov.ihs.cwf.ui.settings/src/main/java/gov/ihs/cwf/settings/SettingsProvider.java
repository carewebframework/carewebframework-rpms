/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.property.Property;
import gov.ihs.cwf.property.PropertyService;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.settings.ISettingsProvider;

/**
 * Settings provider that supports editing parameters via templates.
 * 
 * 
 */
public class SettingsProvider implements ISettingsProvider {
    
    interface IPropertyTransform<T> {
        
        String toSource(T value);
        
        T fromSource(String value);
        
    }
    
    private static final IPropertyTransform<Boolean> TX_BOOLEAN = new IPropertyTransform<Boolean>() {
        
        @Override
        public String toSource(Boolean value) {
            return value == null ? null : value ? "1" : "0";
        }
        
        @Override
        public Boolean fromSource(String value) {
            return value == null ? null : StrUtil.toBoolean(value);
        }
        
    };
    
    private static final IPropertyTransform<String> TX_POINTER = new IPropertyTransform<String>() {
        
        @Override
        public String toSource(String value) {
            return value == null ? null : "`" + value;
        }
        
        @Override
        public String fromSource(String value) {
            return value;
        }
        
    };
    
    private static final IPropertyTransform<Date> TX_DATE = new IPropertyTransform<Date>() {
        
        @Override
        public String toSource(Date value) {
            return new FMDate(value).getFMDate();
        }
        
        @Override
        public Date fromSource(String value) {
            return StringUtils.isEmpty(value) ? null : new FMDate(value);
        }
        
    };
    
    @SuppressWarnings("rawtypes")
    private static enum PropertyType {
        D("date", TX_DATE), F("text"), N("integer"), S("choice"), Y("boolean", TX_BOOLEAN), W("text"), P("choice",
                TX_POINTER), COLOR("color");
        
        private final String type;
        
        private final IPropertyTransform transform;
        
        PropertyType(String type) {
            this(type, null);
        }
        
        PropertyType(String type, IPropertyTransform transform) {
            this.type = type;
            this.transform = transform;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private static class PropertyInfoEx extends PropertyInfo {
        
        private final String entity;
        
        private final PropertyType type;
        
        PropertyInfoEx(String entity, PropertyType type) {
            super();
            this.entity = entity;
            this.type = type;
            setType(type.type);
        }
        
        public String toSource(Object value) {
            return value == null ? null : type.transform == null ? value.toString() : type.transform.toSource(value);
        }
        
        public Object fromSource(String value) {
            return value == null || type.transform == null ? value : type.transform.fromSource(value);
        }
    }
    
    private BrokerSession broker;
    
    private PropertyService propertyService;
    
    /**
     * Fetches specified parameter template. RPC return format is:
     * 
     * <pre>
     * 98^CIAVM SITE PARAMETERS^VueCentric Site Parameters^SYS
     * 49^CIAVM DEFAULT SOURCE^Default object source path^F^^Default path to the object repository.
     * 36^CIAVM DEFAULT TEMPLATE^Default login template^P^19930.3^Name of a template that becomes the login default."
     * 316^CIANB POLLING INTERVAL^Host polling interval^N^^Number of seconds (1-60) between polls."
     * 4740^CIAVM PRIMARY TIMEOUT^Primary inactivity timeout^N^30:999999^Number of seconds (30+) of inactivity before locking application."
     * 89^CIAVM DISABLE CCOW^Disable CCOW support^Y^^Disables connection to a CCOW-compliant context manager."
     * </pre>
     * 
     * @param id Id of parameter template.
     */
    @Override
    public PluginDefinition fetch(String id) {
        List<String> tmpl = broker.callRPCList("CIAVUTPR GETTPL", null, id);
        PluginDefinition def = new PluginDefinition();
        def.setId(id);
        String entity = null;
        
        for (String s : tmpl) {
            String[] pcs = StrUtil.split(s, StrUtil.U, 6);
            
            if (entity == null) {
                entity = pcs[3];
                def.setName(pcs[2]);
                
                if (entity.isEmpty()) {
                    throw new IllegalArgumentException("Parameter template not found: " + id);
                }
            } else {
                PropertyInfo pi = toPropertyInfo(entity, pcs);
                
                if (pi != null) {
                    def.getProperties().add(pi);
                }
            }
        }
        
        return def;
    }
    
    private PropertyInfoEx toPropertyInfo(String entity, String[] pcs) {
        PropertyType type = toPropertyType(pcs);
        
        if (type == null) {
            return null;
        }
        
        PropertyInfoEx pi = new PropertyInfoEx(entity, type);
        pi.setId(pcs[1]);
        pi.setName(pcs[2]);
        pi.setDescription(pcs[5]);
        Properties config = pi.getConfig();
        
        switch (type) {
            case S:
                getSetEntries(config, pcs[4]);
                break;
            
            case P:
                getFileEntries(config, pcs[4]);
                break;
            
            case F:
            case N:
                extractConfigValues(config, pcs[4], "min", "max");
                break;
        }
        return pi;
    }
    
    private PropertyType toPropertyType(String[] pcs) {
        try {
            PropertyType type = PropertyType.valueOf(pcs[3]);
            return type == PropertyType.F && pcs[1].toUpperCase().contains("COLOR") ? PropertyType.COLOR : type;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void extractConfigValues(Properties config, String value, String... keys) {
        String[] values = value.split("\\:");
        int max = Math.min(values.length, keys.length);
        
        for (int i = 0; i < max; i++) {
            if (!values[i].isEmpty()) {
                config.put(keys[i], values[i]);
            }
        }
    }
    
    private void getSetEntries(Properties config, String values) {
        getArrayEntries(config, values.split("\\;"), "\\:");
    }
    
    private void getFileEntries(Properties config, String file) {
        List<String> entries = broker.callRPCList("CIAURPC FILENT", null, file);
        getArrayEntries(config, entries.toArray(new String[entries.size()]), "\\^");
    }
    
    private void getArrayEntries(Properties config, String[] entries, String delim) {
        List<String[]> values = new ArrayList<String[]>();
        
        for (String entry : entries) {
            values.add(entry.split(delim, 2));
        }
        Collections.sort(values, new Comparator<String[]>() {
            
            @Override
            public int compare(String[] s1, String[] s2) {
                return s1[1].compareToIgnoreCase(s2[1]);
            }
            
        });
        
        StringBuilder sb = new StringBuilder();
        
        for (String[] value : values) {
            sb.append(sb.length() > 0 ? "," : "").append(value[1]).append(":").append(value[0]);
        }
        
        config.put("values", sb.toString());
        config.put("delimiter", ":");
    }
    
    private Property toProperty(PropertyInfoEx propInfo) {
        return new Property(propInfo.getId(), null, propInfo.entity);
    }
    
    @Override
    public void setPropertyValue(PropertyInfo propInfo, Object value) {
        PropertyInfoEx propInfoEx = (PropertyInfoEx) propInfo;
        Property prop = toProperty(propInfoEx);
        prop.setValue(propInfoEx.toSource(value));
        propertyService.saveValue(prop, propInfoEx.entity);
    }
    
    @Override
    public Object getPropertyValue(PropertyInfo propInfo) {
        PropertyInfoEx propInfoEx = (PropertyInfoEx) propInfo;
        List<String> val = new ArrayList<String>();
        propertyService.fetchValue(toProperty(propInfoEx), val);
        return val.isEmpty() ? null : propInfoEx.fromSource(val.get(0));
    }
    
    public BrokerSession getBroker() {
        return broker;
    }
    
    public void setBroker(BrokerSession broker) {
        this.broker = broker;
    }
    
    public PropertyService getPropertyService() {
        return propertyService;
    }
    
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }
    
}

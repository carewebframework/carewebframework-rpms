/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import gov.ihs.cwf.property.Property;
import gov.ihs.cwf.property.PropertyCollection;

import org.carewebframework.api.spring.SpringUtil;

/**
 * Class for managing boolean system and user preferences affecting patient selection.
 * 
 * @author dmartin
 */
public class Features {
    
    /**
     * Used to allow EL expression to directly access property settings.
     * 
     * @author dmartin
     */
    private class FeatureMap implements Map<String, Boolean> {
        
        @Override
        public Collection<Boolean> values() {
            return null;
        }
        
        @Override
        public Boolean put(String key, Boolean value) {
            return null;
        }
        
        @Override
        public Set<String> keySet() {
            return null;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public void putAll(Map<? extends String, ? extends Boolean> t) {
        };
        
        @Override
        public void clear() {
        }
        
        @Override
        public boolean containsValue(Object value) {
            return false;
        }
        
        @Override
        public Boolean remove(Object key) {
            return null;
        }
        
        @Override
        public boolean containsKey(Object key) {
            return false;
        }
        
        @Override
        public Set<java.util.Map.Entry<String, Boolean>> entrySet() {
            return null;
        }
        
        @Override
        public Boolean get(Object obj) {
            return obj instanceof String ? isEnabled((String) obj) : null;
        }
    }
    
    private final String propertyPrefix;
    
    private final PropertyCollection properties = new PropertyCollection();
    
    private final FeatureMap featureMap = new FeatureMap();
    
    /**
     * Return the user's feature settings.
     * 
     * @return User's feature settings.
     */
    public static Features getInstance() {
        return SpringUtil.getBean("patientSelectionFeatures", Features.class);
    }
    
    public Features(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
        properties.loadProperties(propertyPrefix);
    };
    
    /**
     * Returns true if the specified feature is enabled.
     * 
     * @param featureName Name of the feature. This maps to the property that is prefixed by the
     *            current property prefix.
     * @return True if the feature is enabled.
     */
    public boolean isEnabled(String featureName) {
        return isEnabled(featureName, true);
    }
    
    /**
     * Returns true if the specified feature is enabled.
     * 
     * @param featureName Name of the feature. This maps to the property that is prefixed by the
     *            current property prefix.
     * @param deflt The default value if the feature state is not found.
     * @return True if the feature is enabled.
     */
    public boolean isEnabled(String featureName, boolean deflt) {
        String value = getValue(featureName, "").toUpperCase();
        return value.isEmpty() ? deflt : !value.startsWith("N");
    }
    
    /**
     * Returns the value of the specified feature.
     * 
     * @param featureName Name of the feature. This maps to the property that is prefixed by the
     *            current property prefix.
     * @param deflt The default value if the feature state is not found.
     * @return The feature's value.
     */
    public String getValue(String featureName, String deflt) {
        Property prop = properties.getProperty(propertyPrefix + featureName.toUpperCase());
        return prop == null ? deflt : prop.getValue();
    }
    
    /**
     * Returns an instance of the feature map used in EL to determine if a feature should be
     * enabled.
     * 
     * @return The feature map.
     */
    public FeatureMap getFeatureMap() {
        return featureMap;
    }
}

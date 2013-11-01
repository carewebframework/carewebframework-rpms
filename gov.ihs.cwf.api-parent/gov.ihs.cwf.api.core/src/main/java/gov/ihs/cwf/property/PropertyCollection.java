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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a map of property values, given one or more prefixes. Properties are loaded and indexed by
 * the property name.
 * 
 * 
 * @author afranken
 */
public class PropertyCollection {
    
    private static final Log log = LogFactory.getLog(PropertyCollection.class);
    
    private final Map<String, Property> properties = new HashMap<String, Property>();
    
    /**
     * Instantiates an empty collection Reference {@link #loadProperties(String)} to populate
     * collection
     */
    public PropertyCollection() {
    }
    
    /**
     * Load property objects with the given array of prefixes
     * 
     * @param propertyPrefixes
     */
    public PropertyCollection(String... propertyPrefixes) {
        for (String prefix : propertyPrefixes) {
            loadProperties(prefix);
        }
    }
    
    /**
     * Loads all property objects beginning with the specified prefix. The property objects are
     * stored in a map indexed by the property name for easy retrieval.
     * 
     * @param prefix
     */
    public void loadProperties(String prefix) {
        List<Property> props = null;
        
        try {
            props = getListByPrefix(prefix);
        } catch (Exception e) {
            log.error("Error retrieving properties with prefix: " + prefix, e);
            return;
        }
        
        for (Property prop : props) {
            if (prop.getName() != null) {
                this.properties.put(prop.getName(), prop);
            }
        }
    }
    
    private List<Property> getListByPrefix(String prefix) {
        List<Property> result = new ArrayList<Property>();
        List<String> props = PropertyUtil.getPropertyDAO().getMatching(prefix);
        
        for (String prop : props) {
            result.add(new Property(prop));
        }
        
        return result;
    }
    
    /**
     * Clears current Map of properties
     */
    public void clear() {
        this.properties.clear();
    }
    
    /**
     * Returns the property with the specified name, or null if not found.
     * 
     * @param name Name of property to find.
     * @return Property object with the specified name, or null if not found.
     */
    public Property getProperty(String name) {
        return this.properties.get(name);
    }
    
    /**
     * Checks to see if current Map contains this propertyName as a key
     * 
     * @param propertyName Property name to look for
     * @return boolean
     */
    public boolean containsProperty(String propertyName) {
        return this.properties.containsKey(propertyName);
    }
    
    /**
     * Returns the index of a property value as it occurs in the choice list.
     * 
     * @param name Property name.
     * @param choices Array of possible choice values. The first entry is assumed to be the default.
     * @return Index of the property value in the choices array. Returns 0 if not found.
     */
    public int getValue(String name, String[] choices) {
        String val = getValue(name, "");
        int index = Arrays.asList(choices).indexOf(val);
        return index == -1 ? 0 : index;
    }
    
    /**
     * Returns a boolean property value.
     * 
     * @param name Property name.
     * @param dflt Default value if a property value is not found.
     * @return Property value or default value if property value not found.
     */
    public boolean getValue(String name, boolean dflt) {
        try {
            String val = getValue(name, Boolean.toString(dflt)).toLowerCase();
            return val.startsWith("y") ? true : Boolean.parseBoolean(val);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Returns an integer property value.
     * 
     * @param name Property name.
     * @param dflt Default value if a property value is not found.
     * @return Property value or default value if property value not found.
     */
    public int getValue(String name, int dflt) {
        try {
            return Integer.parseInt(getValue(name, Integer.toString(dflt)));
        } catch (Exception e) {
            return dflt;
        }
    }
    
    /**
     * Returns a string property value.
     * 
     * @param name Property name.
     * @param dflt Default value if a property value is not found.
     * @return Property value or default value if property value not found.
     */
    public String getValue(String name, String dflt) {
        Property prop = getProperty(name);
        return prop == null ? dflt : prop.getValue();
    }
    
    /**
     * Returns the property map.
     * 
     * @return The property map.
     */
    public Map<String, Property> getProperties() {
        return this.properties;
    }
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.patientlist;

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter manager for personal patient lists.
 */
public class PersonalPatientListFilterManager extends AbstractPatientListFilterManager {
    
    private static final Log log = LogFactory.getLog(PersonalPatientListFilterManager.class);
    
    private Property filterProperty;
    
    public PersonalPatientListFilterManager(PersonalPatientList patientList) {
        super(patientList, Util.createImmutableSet(FilterCapability.values()));
    }
    
    /**
     * Returns the property used to store the filter list.
     * 
     * @return
     */
    private Property getFilterProperty() {
        if (filterProperty == null) {
            filterProperty = new Property(((PersonalPatientList) getPatientList()).getPropertyName(), " ");
        }
        
        return filterProperty;
    }
    
    /**
     * Initialize the filter list. This is pulled from the same property using an application
     * instance id of " ".
     */
    @Override
    protected List<AbstractPatientListFilter> initFilters() {
        if (filters == null) {
            log.debug("Retrieving personal list names...");
            filters = new ArrayList<AbstractPatientListFilter>();
            Property prop = getFilterProperty();
            List<String> values = prop.getValues();
            
            if (values != null) {
                for (String value : values) {
                    filters.add(new PersonalPatientListFilter(value));
                }
            }
        }
        return filters;
    }
    
    /**
     * Save filters to the filter property.
     */
    @Override
    protected void saveFilters() {
        Property prop = getFilterProperty();
        ArrayList<String> list = new ArrayList<String>();
        
        for (AbstractPatientListFilter filter : initFilters()) {
            list.add(filter.serialize());
        }
        
        try {
            prop.setValues(list);
            prop.saveValues();
        } catch (Exception e) {
            log.error("Error saving personal list filters.", e);
        }
    }
    
    /**
     * Force reload of filter property upon filter refresh.
     */
    @Override
    protected void refreshFilters() {
        filterProperty = null;
        super.refreshFilters();
    }
    
    /**
     * Creates a filter wrapping the specified entity.
     */
    @Override
    protected AbstractPatientListFilter createFilter(Object entity) {
        return new PersonalPatientListFilter(entity);
    }
    
    /**
     * Creates a filter from its serialized form.
     */
    @Override
    protected AbstractPatientListFilter deserializeFilter(String serializedEntity) {
        return createFilter(serializedEntity);
    }
    
    /**
     * Deletes the associated personal list when a filter is removed.
     */
    @Override
    public void removeFilter(AbstractPatientListFilter filter) {
        ((PersonalPatientList) getPatientList()).deleteList(filter);
        super.removeFilter(filter);
    }
    
}

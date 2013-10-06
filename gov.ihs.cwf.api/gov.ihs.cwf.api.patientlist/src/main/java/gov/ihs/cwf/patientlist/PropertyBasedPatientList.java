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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for patient lists that are stored in properties. The filter associated with
 * this list type is typically the name of a stored list (though this default behavior can be
 * overridden). This name becomes the application instance value when storing list contents to and
 * retrieving from the associated property. If the derived class does not implement filters, the
 * null application instance value will be used.
 * 
 * @author dmartin
 */
public abstract class PropertyBasedPatientList extends AbstractPatientList {
    
    private static final Log log = LogFactory.getLog(PropertyBasedPatientList.class);
    
    private final String propertyName;
    
    private Property pplProperty;
    
    private List<PatientListItem> pplList;
    
    private boolean changed;
    
    /**
     * Creates an instance of this list using the specified parameters.
     * 
     * @param name The list name.
     * @param entityName The name of the entity type associated with any filters.
     * @param propertyName The name of the property where list contents are stored.
     */
    protected PropertyBasedPatientList(String name, String entityName, String propertyName) {
        super(name, entityName);
        this.propertyName = propertyName;
    }
    
    /**
     * Copy constructor.
     * 
     * @param list
     */
    protected PropertyBasedPatientList(PropertyBasedPatientList list) {
        super(list);
        this.propertyName = list.propertyName;
    }
    
    /**
     * Removes all occurrences of the specified patient from the list.
     * 
     * @param patient
     */
    protected void removePatient(Patient patient) {
        getListItems();
        PatientListItem item;
        
        while ((item = Util.findListItem(patient, pplList)) != null) {
            removeItem(item);
        }
    }
    
    /**
     * Removes a patient list item.
     * 
     * @param item
     */
    protected void removeItem(PatientListItem item) {
        pplList.remove(item);
        changed = true;
    }
    
    /**
     * Adds the specified patient to the list. Any existing occurrences of that patient will first
     * be removed (i.e., duplicates are automatically eliminated).
     * 
     * @param patient Patient to be added.
     * @param top If true, the patient is added to the beginning of the list. If false, the patient
     *            is added to the end.
     */
    protected void addPatient(Patient patient, boolean top) {
        int max = getListSizeMax();
        
        if (max > 0 && patient != null) {
            removePatient(patient);
            trimList(max - 1);
            addItem(new PatientListItem(patient), top);
        }
    }
    
    /**
     * Adds a patient list item.
     * 
     * @param item The item to add.
     * @param top If true, the item is added to the beginning of the list. If false, the item is
     *            added to the end.
     */
    protected void addItem(PatientListItem item, boolean top) {
        getListItems();
        
        if (pplList.contains(item)) {
            return;
        }
        
        if (top) {
            pplList.add(0, item);
        } else {
            pplList.add(item);
        }
        
        changed = true;
    }
    
    /**
     * Returns the name of the property under which patient lists are stored.
     * 
     * @return
     */
    protected String getPropertyName() {
        return propertyName;
    }
    
    /**
     * Returns the patient list.
     * 
     * @return Patient list.
     */
    @Override
    public Collection<PatientListItem> getListItems() {
        if (this.pplList == null) {
            this.pplList = new ArrayList<PatientListItem>();
            
            if (!isFiltered() || getActiveFilter() != null) {
                try {
                    Property prop = getListProperty();
                    int max = getListSizeMax();
                    addPatients(pplList, prop.getValues(), max);
                } catch (final Exception e) {
                    log.error("Error while retrieving patient list.", e);
                }
            }
        }
        
        return Collections.unmodifiableList(this.pplList);
    }
    
    /**
     * Forces a refresh of the list.
     */
    @Override
    public void refresh() {
        super.refresh();
        pplList = null;
        pplProperty = null;
        changed = false;
    }
    
    /**
     * Sets the active filter which, for personal lists, determines the list name.
     */
    @Override
    public void setActiveFilter(AbstractPatientListFilter filter) {
        refresh();
        super.setActiveFilter(filter);
    }
    
    /**
     * Trim the list to the specified maximum size.
     * 
     * @param maxSize
     */
    private void trimList(int maxSize) {
        maxSize = maxSize < 0 ? 0 : maxSize;
        
        while (this.pplList.size() > maxSize) {
            this.pplList.remove(this.pplList.size() - 1);
        }
    }
    
    /**
     * Saves the patient list for the user. First, trims the list if it exceeds the maximum
     * allowable length.
     * 
     * @param sort If true, the list is sorted before saving.
     */
    protected void saveList(boolean sort) {
        if (!changed) {
            return;
        }
        
        changed = false;
        getListItems();
        
        if (sort) {
            Collections.sort(pplList);
        }
        
        try {
            final ArrayList<String> patids = new ArrayList<String>();
            trimList(getListSizeMax());
            
            for (final PatientListItem item : pplList) {
                Patient pat = item.getPatient();
                
                if (pat != null) {
                    final String strPatId = Long.toString(pat.getDomainId());
                    patids.add(strPatId);
                }
            }
            
            Property prop = getListProperty();
            prop.setValues(patids);
            prop.saveValues();
            
        } catch (final Exception e) {
            log.error("Error while saving patient list.", e);
        }
    }
    
    /**
     * Deletes the list represented by the specified filter.
     * 
     * @param filter
     */
    protected void deleteList(AbstractPatientListFilter filter) {
        if (getActiveFilter() != filter) {
            setActiveFilter(filter);
        }
        
        getListItems();
        pplList.clear();
        saveList(false);
        refresh();
    }
    
    /**
     * Returns the name of the personal list.
     * 
     * @return
     */
    protected String getListName() {
        return getActiveFilter() == null ? null : getActiveFilter().getName();
    }
    
    /**
     * Returns the property used to store the patient list. Note that its creation is deferred to
     * insure that the user context has been properly set.
     * 
     * @return
     */
    protected Property getListProperty() {
        if (this.pplProperty == null) {
            this.pplProperty = new Property(propertyName, getListName());
        } else {
            this.pplProperty.setInstanceId(getListName());
        }
        
        return this.pplProperty;
    }
    
    /**
     * Returns the setting for the maximum list size for the list. By default, there is no effective
     * size limit.
     * 
     * @return The maximum list size.
     */
    protected int getListSizeMax() {
        return Integer.MAX_VALUE;
    }
    
}

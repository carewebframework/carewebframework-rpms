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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.factory.DomainObjectFactory;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.DateRange;

/**
 * Base class for patient list implementations.
 */
public abstract class AbstractPatientList implements IPatientList {
    
    private static final Log log = LogFactory.getLog(AbstractPatientList.class);
    
    private final String name;
    
    private final String entityName;
    
    private AbstractPatientListFilter activeFilter;
    
    private final AbstractPatientListFilterManager filterManager;
    
    private DateRange dateRange;
    
    private BrokerSession brokerSession;
    
    /**
     * Copy constructor.
     * 
     * @param list
     */
    public AbstractPatientList(AbstractPatientList list) {
        this.name = list.getName();
        this.entityName = list.getEntityName();
        this.activeFilter = list.getActiveFilter();
        this.dateRange = list.dateRange == null ? null : new DateRange(list.dateRange);
        this.filterManager = createFilterManager();
    }
    
    /**
     * Derived classes should call this constructor from their argumentless constructor.
     * 
     * @param name The display name of the list.
     * @param entityName The display name of the filter's entity type, if any.
     */
    protected AbstractPatientList(String name, String entityName) {
        this.name = name;
        this.entityName = entityName;
        this.filterManager = createFilterManager();
    }
    
    /**
     * Returns a fully instantiated patient object, given the patient id.
     * 
     * @param patientId The patient id.
     * @return A patient object, or null if not found or an error occurred.
     */
    protected final Patient getPatient(long patientId) {
        return patientId > 0 ? DomainObjectFactory.get(Patient.class, patientId) : null;
    }
    
    /**
     * Add a list of patients to the current item list.
     * 
     * @param items Current item list.
     * @param list List of patients.
     * @param max Maximum # of entries in item list (0 if no limit).
     */
    protected final void addPatients(List<PatientListItem> items, List<String> list, int max) {
        long ids[] = new long[list.size()];
        int cnt = 0;
        int i = 0;
        
        while (i < list.size() && (max == 0 || items.size() < max)) {
            String value = list.get(i);
            String[] pcs = Util.split(value, 3);
            long patientId = NumberUtils.toLong(pcs[0]);
            
            if (patientId == 0) {
                list.remove(i);
                continue;
            }
            
            if (patientId < 0) {
                throw new PatientListException(pcs[1]);
            }
            
            ids[cnt++] = patientId;
            i++;
        }
        
        if (ids.length != cnt) {
            ids = Arrays.copyOf(ids, cnt);
        }
        
        List<Patient> results = DomainObjectFactory.get(Patient.class, ids);
        i = 0;
        
        for (Patient patient : results) {
            String[] pcs = Util.split(list.get(i++), 4);
            PatientListItem item = new PatientListItem(patient, pcs[2]);
            
            if (!items.contains(item)) {
                items.add(item);
            }
        }
    }
    
    /**
     * Returns the data source.
     * 
     * @return Broker session.
     */
    public BrokerSession getBrokerSession() {
        if (brokerSession == null) {
            brokerSession = RPMSUtil.getBrokerSession();
        }
        
        return brokerSession;
    }
    
    /**
     * Override to return an implementation of a filter manager.
     * 
     * @return Filter manager.
     */
    protected AbstractPatientListFilterManager createFilterManager() {
        return null;
    }
    
    /**
     * Returns the list's name.
     * 
     * @see IPatientList#getName()
     */
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Returns the list's display name.
     * 
     * @see IPatientList#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(StringUtils.isEmpty(entityName) ? name : entityName);
        String delim = ": ";
        
        if (isFiltered() && activeFilter != null) {
            Util.append(sb, activeFilter.getName(), delim);
            delim = ", ";
        }
        
        if (isDateRangeRequired() && dateRange != null && !StringUtils.isEmpty(dateRange.getLabel())) {
            Util.append(sb, dateRange.getLabel(), delim);
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the name of the filter's associated entity, if any.
     * 
     * @see IPatientList#getEntityName()
     */
    @Override
    public String getEntityName() {
        return entityName;
    }
    
    /**
     * Instead of overriding this, override the initFilters method to return the internal instance
     * of the filter list.
     * 
     * @see IPatientList#getFilters()
     */
    @Override
    public Collection<AbstractPatientListFilter> getFilters() {
        Collection<AbstractPatientListFilter> filters = filterManager == null ? null : filterManager.initFilters();
        return filters == null ? null : Collections.unmodifiableCollection(filters);
    }
    
    /**
     * Returns true if the patient list requires a date range.
     * 
     * @see IPatientList#isDateRangeRequired()
     */
    @Override
    public boolean isDateRangeRequired() {
        return false;
    }
    
    /**
     * Override if this is a user-manageable list, returning a reference to the list's list manager.
     * 
     * @see IPatientList#getItemManager()
     */
    @Override
    public IPatientListItemManager getItemManager() {
        return this instanceof IPatientListItemManager ? (IPatientListItemManager) this : null;
    }
    
    /**
     * Override if this is a user-manageable list, returning a reference to the list's filter
     * manager.
     * 
     * @see IPatientList#getFilterManager()
     */
    @Override
    public IPatientListFilterManager getFilterManager() {
        return filterManager == null || filterManager.getCapabilities() == null ? null : filterManager;
    }
    
    /**
     * Returns true if the list can be filtered.
     * 
     * @see IPatientList#isFiltered()
     */
    @Override
    public boolean isFiltered() {
        return filterManager != null;
    }
    
    /**
     * Returns the active filter selected for this list. May be null.
     * 
     * @see IPatientList#getActiveFilter()
     */
    @Override
    public AbstractPatientListFilter getActiveFilter() {
        return activeFilter;
    }
    
    /**
     * Refreshes the list.
     * 
     * @see IPatientList#refresh()
     */
    @Override
    public void refresh() {
        if (filterManager != null) {
            filterManager.refreshFilters();
        }
    }
    
    /**
     * Sets the active filter for this list.
     * 
     * @see IPatientList#setActiveFilter
     */
    @Override
    public void setActiveFilter(AbstractPatientListFilter filter) {
        this.activeFilter = filter;
    }
    
    /**
     * Implement to retrieve list of patient items.
     * 
     * @see IPatientList#getListItems()
     */
    @Override
    public abstract Collection<PatientListItem> getListItems();
    
    /**
     * Returns the date range, if applicable.
     * 
     * @see IPatientList#getDateRange
     */
    @Override
    public DateRange getDateRange() {
        return dateRange;
    }
    
    /**
     * Sets the end date, if applicable.
     * 
     * @see IPatientList#setDateRange
     */
    @Override
    public void setDateRange(DateRange value) {
        dateRange = value;
        refresh();
    }
    
    /**
     * Returns true if this list is disabled.
     * 
     * @see IPatientList#isDisabled()
     */
    @Override
    public boolean isDisabled() {
        return false;
    }
    
    /**
     * Returns true is the list is in the process of being built. Override if list construction is
     * asynchronous to indicate completion status.
     * 
     * @see IPatientList#isPending()
     */
    @Override
    public boolean isPending() {
        return false;
    }
    
    /**
     * Returns the sorting sequence for this list. Lower numbers sort first.
     * 
     * @see IPatientList#getSequence()
     */
    @Override
    public int getSequence() {
        return 0;
    }
    
    /**
     * Returns a copy of this list.
     * 
     * @see IPatientList#copy()
     */
    @Override
    public IPatientList copy() {
        try {
            return (IPatientList) ConstructorUtils.invokeConstructor(getClass(), this);
        } catch (Exception e) {
            log.error("Error attempting to copy list.", e);
            return null;
        }
    }
    
    @Override
    public IPatientList copy(String serialized) {
        IPatientList list = copy();
        String pcs[] = Util.split(serialized, 4);
        
        if (list.isDateRangeRequired()) {
            list.setDateRange(StringUtils.isEmpty(pcs[1]) ? null : new DateRange(pcs[1]));
        }
        
        if (filterManager != null && list.isFiltered()) {
            list.setActiveFilter(StringUtils.isEmpty(pcs[2]) ? null : filterManager.deserializeFilter(pcs[2]));
        }
        
        return list;
    }
    
    /**
     * Returns the serialized form of the list.
     */
    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder(name);
        Util.append(sb, dateRange);
        Util.append(sb, activeFilter);
        return sb.toString();
    }
    
    /**
     * Returns the serialized form of the list.
     */
    @Override
    public String toString() {
        return serialize();
    }
    
    /**
     * Two lists are considered equal if their name, active filter, and date ranges are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractPatientList)) {
            return false;
        }
        
        AbstractPatientList list = (AbstractPatientList) object;
        return ObjectUtils.equals(name, list.name) && ObjectUtils.equals(activeFilter, list.activeFilter)
                && ObjectUtils.equals(dateRange, list.dateRange);
    }
}

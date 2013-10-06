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

import java.util.Collection;

import org.carewebframework.common.DateRange;

/**
 * Interface for all patient lists.
 * 
 * @author dmartin
 */
public interface IPatientList {
    
    /**
     * Returns the name assigned to this list. The name must be unique across all patient lists.
     * 
     * @return The filter's name.
     */
    String getName();
    
    /**
     * Returns the display name for this list. This should be a combination of the list's name and
     * any relevant state information associated with the list (like its current filter and date
     * range settings, if applicable).
     * 
     * @return Display name.
     */
    String getDisplayName();
    
    /**
     * Returns the display name of the entity type associated with filters (if any) for this list.
     * If a list does not support filters, this should return null.
     * 
     * @return The display name of the filters' entity type, or null if filters are not supported by
     *         the list.
     */
    String getEntityName();
    
    /**
     * The sorting sequence for this list. Lower values will cause the list to sort before other
     * lists.
     * 
     * @return The sorting sequence for the list.
     */
    int getSequence();
    
    /**
     * If true, the list supports date ranges in its retrieval logic.
     * 
     * @return True if date ranges are supported.
     */
    boolean isDateRangeRequired();
    
    /**
     * If true, the list is disabled for the current user and should not be displayed.
     * 
     * @return True if the list is disabled.
     */
    boolean isDisabled();
    
    /**
     * If true, the list supports filtering.
     * 
     * @return True if the list supports filters.
     */
    boolean isFiltered();
    
    /**
     * Returns the active filter applied to the list. If the list does not support filters, or if no
     * active filter has been set, this will return null.
     * 
     * @return The active filter.
     */
    AbstractPatientListFilter getActiveFilter();
    
    /**
     * Returns the patient list item manager associated with the list. If the list does not support
     * user management of its list items, this should return null.
     * 
     * @return The associated list item manager, or null if not applicable.
     */
    IPatientListItemManager getItemManager();
    
    /**
     * Returns the filter manager associated with the list. If the list does not support filters, or
     * if the list does not support user management of its filters, this should return null.
     * 
     * @return The associated filter manager, or null if not applicable.
     */
    IPatientListFilterManager getFilterManager();
    
    /**
     * Sets the active filter to the specified value. If the list does not support filters, this
     * request should be ignored.
     * 
     * @param filter The filter to become active.
     */
    void setActiveFilter(AbstractPatientListFilter filter);
    
    /**
     * Returns the date range associated with the list. If the list does not support date ranges, or
     * if no date range has been set, this should return null.
     * 
     * @return The current date range.
     */
    DateRange getDateRange();
    
    /**
     * Sets the date range to be applied to this patient list. If the list does not support date
     * ranges, the request will be ignored.
     * 
     * @param value
     */
    void setDateRange(DateRange value);
    
    /**
     * Returns the list of available filters for this patient list. If the list does not support
     * filters, this should return null.
     * 
     * @return A list of filters, or null if not applicable.
     */
    Collection<AbstractPatientListFilter> getFilters();
    
    /**
     * Returns a list of patient list items. The underlying logic should apply the active filter and
     * date range, if appropriate, in determining what items are returned. If any required
     * parameters are not yet set, this operation may return null.
     * 
     * @return A list of patient items, or null if the required parameters have not be set.
     */
    Collection<PatientListItem> getListItems();
    
    /**
     * Returns a fully cloned copy of this list.
     * 
     * @return A clone of the original list.
     */
    IPatientList copy();
    
    /**
     * Returns a fully cloned copy of this list, applying any serialized settings (active filter,
     * date range, etc).
     * 
     * @param serialized Serialized settings applicable for this list.
     * @return A clone of the original list.
     */
    IPatientList copy(String serialized);
    
    /**
     * Returns the serialized form of this list, including any active settings (active filter, date
     * range, etc) where applicable.
     * 
     * @return The serialized form of this list.
     */
    String serialize();
    
    /**
     * Forces a refresh of the patient items returned by this list.
     */
    void refresh();
    
    /**
     * Returns true if the list is in the process of being built.
     * 
     * @return True if list construction is underway.
     */
    boolean isPending();
    
}

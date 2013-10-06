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

import java.util.Set;

/**
 * Defines methods for managing a user-manageable list.
 * 
 * @author dmartin
 */
public interface IPatientListFilterManager {
    
    /**
     * Each member represents a specific capability that may be supported by a given filter manager.
     */
    enum FilterCapability {
        ADD, REMOVE, MOVE, RENAME
    };
    
    /**
     * Adds a new filter to the filter list.
     * 
     * @param entity The entity to be associated with the new filter.
     * @return The newly created filter.
     */
    AbstractPatientListFilter addFilter(Object entity);
    
    /**
     * Removes the specified filter from the filter list.
     * 
     * @param filter The filter to be removed.
     */
    void removeFilter(AbstractPatientListFilter filter);
    
    /**
     * Moves the specified filter to the specified position within the filter list. If the filter
     * does not exist within the list, the request is ignored.
     * 
     * @param filter The filter to be moved.
     * @param index The position within the filter list where the filter is to be moved.
     */
    void moveFilter(AbstractPatientListFilter filter, int index);
    
    /**
     * Renames a filter.
     * 
     * @param filter The filter to be renamed.
     * @param newName The new name for the filter. If the filter name does not meet the naming
     *            requirements for the filter type, a runtime exception will be raised.
     */
    void renameFilter(AbstractPatientListFilter filter, String newName);
    
    /**
     * Returns the set of capabilities supported by this filter manager.
     * 
     * @return A set of FilterCapability elements supported by this filter manager.
     */
    Set<FilterCapability> getCapabilities();
    
    /**
     * Returns true if the filter manager supports the requested capability.
     * 
     * @param capability A FilterCapability element.
     * @return True if the capability is supported by the filter manager.
     */
    boolean hasCapability(FilterCapability capability);
    
}

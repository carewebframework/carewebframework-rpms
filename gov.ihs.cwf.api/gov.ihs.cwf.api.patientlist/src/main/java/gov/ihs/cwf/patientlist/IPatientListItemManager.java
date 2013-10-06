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

/**
 * Defines methods for managing items within a user-manageable list.
 * 
 * @author dmartin
 */
public interface IPatientListItemManager {
    
    /**
     * Adds a patient list item to the list.
     * 
     * @param item Patient list item to add.
     */
    void addItem(PatientListItem item);
    
    /**
     * Removes a patient list item from the list.
     * 
     * @param item Patient list item to remove.
     */
    void removeItem(PatientListItem item);
    
    /**
     * Saves any modifications to the list of patient items.
     */
    void save();
}

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
 * Defines methods for managing a user-manageable list.
 * 
 * @author dmartin
 */
public interface IPatientListManager {
    
    void addItem(PatientListItem item);
    
    void removeItem(PatientListItem item);
    
    void swapItems(PatientListItem item1, PatientListItem item2);
    
    void sortItems();
    
    void addFilter(AbstractPatientListFilter filter);
    
    void removeFilter(AbstractPatientListFilter filter);
    
    void save();
}

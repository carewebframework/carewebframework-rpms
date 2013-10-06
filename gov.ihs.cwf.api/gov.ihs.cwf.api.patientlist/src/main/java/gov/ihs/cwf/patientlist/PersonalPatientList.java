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
 * Maintains personal patient lists.
 * 
 * @author dmartin
 */
public class PersonalPatientList extends PropertyBasedPatientList implements IPatientListItemManager {
    
    /**
     * Creates an instance of a personal list.
     * 
     * @param propertyName Name of the property where this list is to be stored.
     */
    public PersonalPatientList(String propertyName) {
        super("Personal Lists", "List", propertyName);
    }
    
    /**
     * Copy constructor.
     * 
     * @param list
     */
    public PersonalPatientList(PersonalPatientList list) {
        super(list);
    }
    
    /**
     * Creates the filter manager for this list.
     */
    @Override
    public PersonalPatientListFilterManager createFilterManager() {
        return new PersonalPatientListFilterManager(this);
    }
    
    /* ===================== IPatientListItemManager ===================== */
    
    @Override
    public void addItem(PatientListItem item) {
        super.addItem(item, false);
    }
    
    @Override
    public void removeItem(PatientListItem item) {
        super.removeItem(item);
    }
    
    @Override
    public void save() {
        saveList(true);
    }
    
}

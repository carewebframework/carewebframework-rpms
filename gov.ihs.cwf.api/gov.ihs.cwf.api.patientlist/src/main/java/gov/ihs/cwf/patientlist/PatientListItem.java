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

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;

/**
 * A list item that is associates a patient object with some additional arbitrary displayable
 * information.
 */
public class PatientListItem implements Comparable<PatientListItem> {
    
    private final Patient patient;
    
    private final String info;
    
    /**
     * Creates a patient list item with no additional information.
     * 
     * @param patient A patient object.
     */
    public PatientListItem(Patient patient) {
        this(patient, null);
    }
    
    /**
     * Creates a patient list item with the specified displayable information.
     * 
     * @param patient A patient object.
     * @param info Displayable information to be associated with the patient.
     */
    public PatientListItem(Patient patient, String info) {
        this.patient = patient;
        this.info = info;
    }
    
    /**
     * Returns the patient associated with this item.
     * 
     * @return The associated patient.
     */
    public Patient getPatient() {
        return patient;
    }
    
    /**
     * Returns the displayable information associated with this item. May be null.
     * 
     * @return Displayable information.
     */
    public String getInfo() {
        return info;
    }
    
    /**
     * Selects the associated patient into the shared context.
     */
    public void select() {
        PatientContext.changePatient(patient);
    }
    
    /**
     * Two list items are considered equal if their associated patients are equal.
     */
    @Override
    public boolean equals(Object object) {
        return !(object instanceof PatientListItem) ? false : patient.equals(((PatientListItem) object).patient);
    }
    
    /**
     * Used to sort patient list items alphabetically by patient name.
     */
    @Override
    public int compareTo(PatientListItem item) {
        return patient.getFullName().compareToIgnoreCase(item.patient.getFullName());
    }
}

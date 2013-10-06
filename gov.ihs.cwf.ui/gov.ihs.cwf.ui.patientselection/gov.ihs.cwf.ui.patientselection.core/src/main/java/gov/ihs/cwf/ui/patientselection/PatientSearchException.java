/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection;

import org.zkoss.util.resource.Labels;

/**
 * Exception class for patient search related exceptions.
 * 
 * @author dmartin
 */
public class PatientSearchException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PatientSearchException(String label) {
        this(label, null);
    }
    
    public PatientSearchException(String label, Throwable cause) {
        super(getLabel(label), cause);
    }
    
    /**
     * Returns the label associated with the specified value. If no associated label is found,
     * returns the original value.
     * 
     * @param value
     * @return
     */
    private static String getLabel(String value) {
        String label = Labels.getLabel(value);
        return label == null ? value : label;
    }
}

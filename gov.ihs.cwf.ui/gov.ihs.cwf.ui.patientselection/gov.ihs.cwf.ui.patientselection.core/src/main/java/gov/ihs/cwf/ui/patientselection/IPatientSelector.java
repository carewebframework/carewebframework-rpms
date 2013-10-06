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

import gov.ihs.cwf.domain.Patient;

/**
 * This interface must be implemented by any patient selector.
 * 
 * @author dmartin
 */
public interface IPatientSelector {
    
    /**
     * Displays the patient selection dialog.
     * 
     * @return The selected patient at the time the dialog was closed. It will be null if no patient
     *         was selected when the dialog was closed or if the selection was canceled by the user.
     */
    Patient select();
}

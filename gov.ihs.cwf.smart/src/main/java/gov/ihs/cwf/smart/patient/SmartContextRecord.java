/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.smart.patient;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;

import org.carewebframework.smart.SmartContextBase;

/**
 * Implements SMART context scope "record".
 * 
 * 
 */
public class SmartContextRecord extends SmartContextBase {
    
    /**
     * Binds patient context changes to the SMART record context scope.
     */
    public SmartContextRecord() {
        super("record", "CONTEXT.CHANGED.Patient");
    }
    
    /**
     * Populate context map with information about currently selected patient.
     * 
     * @param context Context map to be populated.
     */
    @Override
    protected void updateContext(ContextMap context) {
        Patient patient = PatientContext.getCurrentPatient();
        
        if (patient != null) {
            context.put("full_name", patient.getFullName());
            context.put("id", patient.getDomainId());
        }
    }
    
}

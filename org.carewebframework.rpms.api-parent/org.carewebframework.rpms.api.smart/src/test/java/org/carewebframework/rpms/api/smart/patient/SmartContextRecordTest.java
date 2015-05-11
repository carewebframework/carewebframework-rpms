/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.smart.patient;

import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.smart.SmartContextBase;

public class SmartContextRecordTest extends SmartContextBase {
    
    public SmartContextRecordTest() {
        super("record", "CONTEXT.CHANGED.Patient");
    }
    
    @Override
    protected void updateContext(ContextMap context) {
        Patient patient = PatientContext.getActivePatient();
        
        if (patient != null) {
            context.put("full_name", patient.getName());
            context.put("id", patient.getId().getIdPart());
        }
    }
    
}

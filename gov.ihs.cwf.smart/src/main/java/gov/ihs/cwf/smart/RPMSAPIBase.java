/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.smart;

import java.util.Map;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;

import org.carewebframework.smart.rdf.RDFAPIBase;

/**
 * Convenience base class for API's that return RDF-formatted results.
 * 
 * @author dmartin
 */
public abstract class RPMSAPIBase extends RDFAPIBase {
    
    public RPMSAPIBase(String pattern, String capability) {
        super(pattern, capability);
    }
    
    /**
     * API entry point. If a record id is specified, verifies that it is the same as the currently
     * selected patient.
     */
    @Override
    public final boolean validateRequest(Map<String, String> params) {
        String patientId = params.get("record_id");
        
        if (patientId != null) {
            Patient patient = PatientContext.getCurrentPatient();
            
            if (patient.getDomainId() != Long.parseLong(patientId)) {
                return false;
            }
        }
        
        return true;
    }
    
}

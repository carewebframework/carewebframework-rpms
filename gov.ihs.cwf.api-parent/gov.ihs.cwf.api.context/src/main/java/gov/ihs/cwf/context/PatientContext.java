/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.context;

import java.util.Map;

import gov.ihs.cwf.domain.Patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.ContextItems;
import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.cal.api.domain.IPatient;

/**
 * Wrapper for shared patient context.
 */
public class PatientContext extends org.carewebframework.cal.api.context.PatientContext {
    
    private static final Log log = LogFactory.getLog(PatientContext.class);
    
    private static final String CCOW_SYSID_MPI = "MPI";
    
    private static final String CCOW_SYSID_SSN = "SSN";
    
    /**
     * Returns the patient in the current context.
     * 
     * @return Patient object (may be null).
     */
    public static Patient getCurrentPatient() {
        return (Patient) getPatientContext().getContextObject(false);
    }
    
    /**
     * Creates a CCOW context from the specified patient object.
     */
    @Override
    public ContextItems toCCOWContext(IPatient patient) {
        super.toCCOWContext(patient);
        contextItems.setIdentifier(CCOW_ID, patient.getIdentifier(CCOW_SYSID_MPI));
        contextItems.setIdentifier(CCOW_CO, patient.getIdentifier(CCOW_SYSID_SSN));
        return contextItems;
    }
    
    /**
     * Returns a patient object based on the specified CCOW context.
     */
    @Override
    public Patient fromCCOWContext(ContextItems contextItems) {
        Patient patient = null;
        
        try {
            patient = new Patient(0);
            //patient.setName(contextItems.getItem(CCOW_NAM, Name.class));
            patient.setGender(contextItems.getItem(CCOW_SEX));
            patient.setBirthDate(contextItems.getDate(CCOW_DOB));
            patient.setIdentifier(contextItems.getIdentifier(CCOW_ID, CCOW_SYSID_MPI));
            patient.setIdentifier(contextItems.getIdentifier(CCOW_CO, CCOW_SYSID_SSN));
            Map<String, String> suffixes = contextItems.getSuffixes(CCOW_MRN);
            
            for (String suffix : suffixes.keySet()) {
                String id = suffixes.get(suffix);
                patient.setIdentifier(new EntityIdentifier(id, suffix));
            }
            
            return patient;
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }
    
    /**
     * Returns a priority value of 10.
     * 
     * @return Priority value for context manager.
     */
    @Override
    public int getPriority() {
        return 10;
    }
    
}

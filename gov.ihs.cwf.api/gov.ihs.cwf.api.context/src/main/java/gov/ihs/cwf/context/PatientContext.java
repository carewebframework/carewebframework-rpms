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

import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Person.Name;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.ContextItems;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.context.IContextEvent;
import org.carewebframework.api.context.ISharedContext;
import org.carewebframework.api.context.ManagedContext;
import org.carewebframework.api.domain.EntityIdentifier;

/**
 * Wrapper for shared patient context.
 * 
 * @author dmartin
 */
public class PatientContext extends ManagedContext<Patient> {
    
    private static final Log log = LogFactory.getLog(PatientContext.class);
    
    private static final String SUBJECT_NAME = "Patient";
    
    private static final String CCOW_ID = SUBJECT_NAME + ".Id";
    
    private static final String CCOW_MRN = CCOW_ID + ".MRN";
    
    private static final String CCOW_CO = SUBJECT_NAME + ".Co";
    
    private static final String CCOW_SEX = CCOW_CO + ".Sex";
    
    private static final String CCOW_DOB = CCOW_CO + ".DateTimeOfBirth";
    
    private static final String CCOW_NAM = CCOW_CO + ".PatientName";
    
    private static final String CCOW_SYSID_MPI = "MPI";
    
    private static final String CCOW_SYSID_SSN = "SSN";
    
    public interface IPatientContextEvent extends IContextEvent {};
    
    public PatientContext() {
        this(null);
    }
    
    public PatientContext(Patient patient) {
        super(SUBJECT_NAME, IPatientContextEvent.class, patient);
    }
    
    /**
     * Creates a CCOW context from the specified patient object. Uses the principal patient
     * registration.
     */
    @Override
    protected ContextItems toCCOWContext(Patient patient) {
        contextItems.setItem(CCOW_MRN, patient.getMedicalRecordNumber());
        contextItems.setIdentifier(CCOW_ID, patient.getIdentifier(CCOW_SYSID_MPI));
        contextItems.setIdentifier(CCOW_CO, patient.getIdentifier(CCOW_SYSID_SSN));
        contextItems.setItem(CCOW_SEX, patient.getGender());
        contextItems.setItem(CCOW_NAM, patient.getName());
        contextItems.setDate(CCOW_DOB, patient.getBirthDate());
        return contextItems;
    }
    
    /**
     * Returns a list of patient objects based on the specified CCOW context.
     */
    @Override
    protected Patient fromCCOWContext(ContextItems contextItems) {
        Patient patient = null;
        
        try {
            patient = new Patient(0);
            patient.setName(contextItems.getItem(CCOW_NAM, Name.class));
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
    
    /**
     * Returns the managed patient context.
     * 
     * @return Patient context.
     */
    @SuppressWarnings("unchecked")
    public static ISharedContext<Patient> getPatientContext() {
        return (ISharedContext<Patient>) ContextManager.getInstance().getSharedContext(PatientContext.class.getName());
    }
    
    /**
     * Returns the principal patient registration in the current context.
     * 
     * @return Patient object (may be null).
     */
    public static Patient getCurrentPatient() {
        return getPatientContext().getContextObject(false);
    }
    
    /**
     * Request a patient context change.
     * 
     * @param patient Patient registration.
     */
    public static void changePatient(Patient patient) {
        try {
            getPatientContext().requestContextChange(patient);
        } catch (Exception e) {
            log.error("Error during request context change.", e);
        }
    }
    
}

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
import gov.ihs.cwf.context.PatientContext.IPatientContextEvent;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.FrameworkUtil;

/**
 * Maintains a single list of most recently used patients that is updated dynamically as patients
 * are selected.
 * 
 * @author dmartin
 */
public class MRUPatientList extends PropertyBasedPatientList {
    
    private static final Log log = LogFactory.getLog(MRUPatientList.class);
    
    private static final String LIST_SIZE_MAX_PROPERTY = "CAREWEB.PATIENT.LIST.MRU.SIZE";
    
    private int pplListSizeMax = -1;
    
    private final IPatientContextEvent contextListener = new IPatientContextEvent() {
        
        @Override
        public void canceled() {
        }
        
        @Override
        public void committed() {
        }
        
        /**
         * Updates the list when a patient selection is pending.
         */
        @Override
        public String pending(boolean silent) {
            try {
                Patient patient = PatientContext.getPatientContext().getContextObject(true);
                
                if (patient != null) {
                    addPatient(patient, true);
                    saveList(false);
                }
            } catch (Throwable t) {
                log.error("Error updating patient list.", t);
            }
            
            return null;
        }
    };
    
    public MRUPatientList(String propertyName) {
        super("Recent Selections", null, propertyName);
        registerListener();
    }
    
    public MRUPatientList(MRUPatientList list) {
        super(list);
        registerListener();
    }
    
    /**
     * Registers the patient context change listener.
     */
    private void registerListener() {
        FrameworkUtil.getAppFramework().registerObject(contextListener);
    }
    
    /**
     * Returns the setting for the maximum list size for the list. For a MRU list, this value is
     * retrieved from a property. For a personal list, there is no effective size limit.
     * 
     * @return The maximum list size. Defaults to 5.
     */
    @Override
    protected int getListSizeMax() {
        if (this.pplListSizeMax >= 0) {
            return this.pplListSizeMax;
        }
        
        final Property prop = new Property(LIST_SIZE_MAX_PROPERTY);
        
        try {
            this.pplListSizeMax = Integer.parseInt(prop.getValue());
        } catch (final Exception e) {
            this.pplListSizeMax = 5;
        }
        
        return this.pplListSizeMax;
    }
    
    @Override
    public int getSequence() {
        return -100;
    }
    
}

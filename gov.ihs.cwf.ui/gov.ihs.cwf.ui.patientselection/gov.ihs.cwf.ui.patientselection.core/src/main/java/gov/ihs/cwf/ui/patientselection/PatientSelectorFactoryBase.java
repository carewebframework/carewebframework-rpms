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


/**
 * Base patient selector factory from which other factories should descend.
 * 
 * @author dmartin
 */
public class PatientSelectorFactoryBase implements IPatientSelectorFactory {
    
    private String factoryBeanId;
    
    private final String displayName;
    
    private final Class<? extends IPatientSelector> patientSelectorClass;
    
    protected PatientSelectorFactoryBase(String displayName, Class<? extends IPatientSelector> patientSelectorClass) {
        this.displayName = displayName;
        this.patientSelectorClass = patientSelectorClass;
    }
    
    /**
     * Creates the patient selection component.
     */
    @Override
    public IPatientSelector create() {
        try {
            return patientSelectorClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getFactoryBeanId() {
        return factoryBeanId;
    }
    
    /* package */void setFactoryBeanId(String factoryBeanId) {
        this.factoryBeanId = factoryBeanId;
    }
}

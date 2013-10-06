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

import org.carewebframework.api.AbstractGlobalRegistry;
import org.carewebframework.ui.action.ActionRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Registry for patient selectors.
 * 
 * @author dmartin
 */
public class PatientSelectorRegistry extends AbstractGlobalRegistry<String, IPatientSelectorFactory> implements BeanPostProcessor {
    
    private static final PatientSelectorRegistry instance = new PatientSelectorRegistry();
    
    public static PatientSelectorRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private PatientSelectorRegistry() {
        super(false);
        ActionRegistry.addGlobalAction("@patientselection.action.select.label",
            "zscript:gov.ihs.cwf.ui.patientselection.PatientSelection.show();");
    }
    
    @Override
    protected String getKey(IPatientSelectorFactory item) {
        return item.getFactoryBeanId();
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PatientSelectorFactoryBase) {
            PatientSelectorFactoryBase factory = (PatientSelectorFactoryBase) bean;
            factory.setFactoryBeanId(beanName);
            add(factory);
        }
        
        return bean;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
}

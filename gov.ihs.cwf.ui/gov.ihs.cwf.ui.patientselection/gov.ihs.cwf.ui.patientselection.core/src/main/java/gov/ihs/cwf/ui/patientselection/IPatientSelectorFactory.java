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
 * This interface must be implemented by any patient selection plugin.
 * 
 * @author dmartin
 */
public interface IPatientSelectorFactory {
    
    /**
     * Creates a patient selection dialog.
     * 
     * @return
     */
    IPatientSelector create();
    
    /**
     * Returns a brief descriptive name of the selector.
     * 
     * @return
     */
    String getDisplayName();
    
    /**
     * Returns the unique id of the factory bean.
     * 
     * @return
     */
    String getFactoryBeanId();
}

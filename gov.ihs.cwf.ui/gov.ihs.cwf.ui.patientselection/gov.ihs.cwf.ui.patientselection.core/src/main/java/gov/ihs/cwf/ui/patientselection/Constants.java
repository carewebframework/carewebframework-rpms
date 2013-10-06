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

import org.carewebframework.ui.zk.ZKUtil;

/**
 * Constants for patient selection.
 * 
 * @author dmartin
 */
public class Constants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
    public static final String PROP_PREFIX = Constants.class.getName() + ".";
    
    public static final String RESULT_ATTRIB = Constants.PROP_PREFIX + "result";
    
    public static final String SELECTOR_ATTRIB = PROP_PREFIX + "selector";
    
    public static final String SELECTED_PATIENT_ATTRIB = PROP_PREFIX + "patient";
    
    public static final String CANNOT_SELECT_TITLE = "patientselection.error.noselect.title";
    
    public static final String CANNOT_SELECT_MESSAGE = "patientselection.error.noselect.message";
    
    /**
     * Enforce static class.
     */
    private Constants() {};
}

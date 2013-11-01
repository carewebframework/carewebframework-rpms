/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.context.encounter;

import org.carewebframework.ui.icons.IconUtil;
import org.carewebframework.ui.zk.ZKUtil;

/**
 * Encounter context-related constants.
 * 
 * 
 */
public class Constants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
    public static final String ICON_LOCKED = IconUtil.getIconPath("lock.png");
    
    public static final String SCLASS_PRIMARY = "encounter-primary-provider";
    
    public static final int ENCOUNTER_DAYS = 366; // Days back to search for existing encounters.	
    
    public static final String TX_NO_DAT = "Encounter Date";
    
    public static final String TX_NO_LOC = "Encounter Location";
    
    public static final String TX_NO_PRI = "Primary Provider";
    
    public static final String TX_NO_PRV = "Default Provider";
    
    public static final String TX_NO_CAT = "Service Category";
    
    public static final String TX_NO_LCK = "The selected visit is locked from further changes.\n"
            + "The requested operation requires a visit that is not locked.";
    
    public static final String TX_NO_KEY = "The selected provider does not hold the PROVIDER security key.\n"
            + "The requested operation requires this.";
    
    public static final String TX_NO_FUT = "You may not create a visit with a future encounter date.";
    
    public static final String TX_MISSING = "The following items were not specified:\n";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}

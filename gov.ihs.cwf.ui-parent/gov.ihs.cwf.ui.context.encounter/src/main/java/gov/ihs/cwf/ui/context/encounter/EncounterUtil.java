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

import java.util.Set;

import gov.ihs.cwf.ui.context.encounter.EncounterSelection.EncounterFlag;

import org.carewebframework.ui.zk.PromptDialog;

/**
 * Encounter-related utilities.
 * 
 * 
 */
public class EncounterUtil {
    
    private static final String TC_NO_ACTIVE_VISIT = "Active Visit Not Selected";
    
    private static final String TX_NO_ACTIVE_VISIT = "An active visit has not been selected.";
    
    private static final Set<EncounterFlag> EF1 = EncounterFlag.flags(EncounterFlag.NOT_LOCKED, EncounterFlag.FORCE,
        EncounterFlag.VALIDATE_ONLY);
    
    public static boolean checkActiveVisit() {
        if (!EncounterSelection.ensureEncounter(EF1)) {
            PromptDialog.showError(TX_NO_ACTIVE_VISIT, TC_NO_ACTIVE_VISIT);
            return false;
        }
        
        return true;
    }
    
    /**
     * Enforces static class.
     */
    private EncounterUtil() {
    };
}

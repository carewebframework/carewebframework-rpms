/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.plugin.patiented;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.ui.common.CoverSheetBase;

/**
 * Controller for patient education cover sheet.
 */
public class MainController extends CoverSheetBase<String> {
    
    private static final long serialVersionUID = 1L;
    
    private String eventName;
    
    private final IGenericEvent<Object> eventListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            refresh();
        }
        
    };
    
    @Override
    protected void init() {
        setup("Patient Education", "Education Detail", "BGOVPED GET", null, -2, "Topic", "Date", "Understanding",
            "Provider", "Group/Individual", "Category", "Code");
        super.init();
    }
    
    @Override
    protected void requestData() {
        String evt = "PCC." + patient.getId().getIdPart() + ".PED";
        
        if (eventName == null || !evt.equals(eventName)) {
            if (eventName != null) {
                getEventManager().unsubscribe(eventName, eventListener);
            }
            
            eventName = evt;
            getEventManager().subscribe(eventName, eventListener);
        }
        
        super.requestData();
    }
    
    /*
     *   Topic Name [1] ^ Visit Date [2] ^ Level [3] ^ Provider Name [4] ^ Group/Individual [5] ^ Length [6] ^
     *   CPT [7] ^ Comment [8] ^ Topic Category [9] ^ Behavior [10] ^ Objective Met [11] ^ Visit Locked [12] ^
     *   Location Name [13] ^ VFile IEN [14] ^ Visit IEN [15] ^ Topic IEN [16] ^ Location IEN [17] ^ Provider IEN [18] ^
     *   Visit Category [19] ^ ICD9 text [20] ^ Comments [21] ^ ICD9 IEN [22] ^ CPT IEN [23] ^ Readiness to learn [24] ^
     *   ICD code [25] ^ Entry date [26]
     * 
     *        MEDICATIONS-INFORMATION 2006^12/24/2014^^ADAM,ADAM^Individual^
     *        5^^^MEDICATIONS^^^0^DEMO IHS CLINIC^1^1^335^7819^1^A^^^^^^
     *        NO CODE SELECTED^12/24/2014
     */
    @Override
    protected void render(String dao, List<Object> columns) {
        String pcs[] = split(dao, U, 18);
        columns.add(pcs[0]); // Topic name
        columns.add(VistAUtil.normalizeDate(pcs[25])); // Entry date
        columns.add(pcs[2]); // Level
        columns.add(pcs[3]); // Provider name
        columns.add(pcs[4]); // Group / individual
        columns.add(pcs[8]); // Topic category
        String code = pcs[6]; // CPT code
        String icd = pcs[24]; // ICD code
        
        if (!icd.isEmpty()) {
            code = code + (code.isEmpty() ? "" : ", ") + icd;
        }
        
        columns.add(code); // CPT/ICD code
    }
    
}

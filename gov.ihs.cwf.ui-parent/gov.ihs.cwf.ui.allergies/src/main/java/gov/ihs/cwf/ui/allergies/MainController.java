/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.allergies;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.piece;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import gov.ihs.cwf.ui.common.CoverSheetBase;

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;

/**
 * Controller for patient allergies cover sheet.
 */
public class MainController extends CoverSheetBase {
    
    private static final long serialVersionUID = 1L;
    
    private Menuitem mnuDeleteADR;
    
    private Menuitem mnuEditADR;
    
    private Menuitem mnuNewADR;
    
    private Menuitem mnuSignADR;
    
    private String statusADR;
    
    @Override
    protected void init() {
        setup("Adverse Reactions", "Adverse Reaction Detail", "BEHOARCV LIST", "BEHOARCV DETAIL", 1, "Agent", "Reaction",
            "Status");
        // TODO: Allergy service access
        super.init();
    }
    
    @Override
    protected void render(String dao, List<Object> columns) {
        String[] pcs = split(dao, U, 6);
        
        if (pcs[0].isEmpty()) {
            statusADR = pcs[1];
            return;
        }
        String stat = pcs[5];
        boolean canSign = "1".equals(pcs[4]);
        
        if ("U".equals(stat) && !canSign) {
            return;
        }
        
        columns.add(pcs[1]);
        columns.add(pcs[3]);
        columns.add((canSign ? "*" : "") + formatStatus(stat));
    }
    
    private String formatStatus(String stat) {
        return "U".equals(stat) ? "Unsigned" : "S".equals(stat) ? "Nonverified" : "V".equals(stat) ? "Verified" : "Unknown";
    }
    
    @Override
    protected void renderItem(Listitem item, String dao) {
        super.renderItem(item, dao);
        
        if ("1".equals(piece(dao, U, 5))) {
            ZKUtil.updateStyle(item, "font-color", "blue");
        }
    }
    
    public void onClick$mnuDeleteADR() {
        
    }
    
    public void onClick$mnuEditADR() {
        
    }
    
    public void onClick$mnuNewADR() {
        
    }
    
    public void onClick$mnuSignADR() {
        
    }
    
}

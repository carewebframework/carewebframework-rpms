/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.medlist;

import static org.carewebframework.common.StrUtil.*;

import gov.ihs.cwf.ui.common.CoverSheetBase;

import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Toolbar;

/**
 * Controller for medication list cover sheet.
 * 
 * @author dmartin
 */
public class MainController extends CoverSheetBase {
    
    private static final long serialVersionUID = 1L;
    
    private Radiogroup rgInOut;
    
    private Radio radAll;
    
    private Toolbar toolbar;
    
    @Override
    protected void init() {
        setup("Medications", "Medication Detail", "BEHORXCV LIST", "BEHORXCV DETAIL", 1, "Medication", "Status", "Issue Date");
        toolbar.setVisible(true);
        super.init();
    }
    
    @Override
    protected String formatData(String data) {
        String[] pcs = split(data, U, 15);
        
        if (checkStatus(pcs[8]) && checkInOut(pcs[0])) 
            return pcs[1] + U + pcs[8] + U + pcs[14];
        else
            return "";
    }
    
    private boolean checkStatus(String data) {
        return radAll.isChecked() || data.startsWith("ACTIVE");
    }
    
    private boolean checkInOut(String data) {
        String inout = piece(data, ";", 2);
        String opt = (String) rgInOut.getSelectedItem().getValue();
        return opt.contains(inout);
    }
    
    public void onCheck$rgActive() {
        renderList();
    }
    
    public void onCheck$rgInOut() {
        renderList();
    }
}

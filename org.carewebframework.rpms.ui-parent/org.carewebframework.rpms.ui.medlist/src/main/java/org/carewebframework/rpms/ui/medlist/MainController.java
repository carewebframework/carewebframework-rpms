/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.medlist;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.piece;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import org.carewebframework.vista.ui.common.CoverSheetBase;

import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Toolbar;

/**
 * Controller for medication list cover sheet.
 */
public class MainController extends CoverSheetBase<String> {
    
    private static final long serialVersionUID = 1L;
    
    private Radiogroup rgInOut;
    
    private Radio radAll;
    
    private Toolbar toolbar;
    
    @Override
    protected void init() {
        setup("Medications", "Medication Detail", "BEHORXCV LIST", "BEHORXCV DETAIL", 1, "Medication", "Status",
            "Issue Date");
        toolbar.setVisible(true);
        super.init();
    }
    
    @Override
    protected void render(String dao, List<Object> columns) {
        String[] pcs = split(dao, U, 15);
        
        if (checkStatus(pcs[8]) && checkInOut(pcs[0])) {
            columns.add(pcs[1]);
            columns.add(pcs[8]);
            columns.add(pcs[14]);
        }
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
        renderData();
    }
    
    public void onCheck$rgInOut() {
        renderData();
    }
}

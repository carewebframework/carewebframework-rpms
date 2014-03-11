/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.consultorders;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import gov.ihs.cwf.ui.common.CoverSheetBase;

/**
 * Controller for consult orders cover sheet. Displays summary and detail views of consult orders
 * for cover sheet.
 */
public class MainController extends CoverSheetBase<String> {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void init() {
        setup("Consult Orders", "Consult Order Detail", "BEHOCNCV LIST", "BEHOCNCV DETAIL", 1, "Service", "Date", "Status");
        super.init();
    }
    
    @Override
    protected void render(String dao, List<Object> columns) {
        String pcs[] = split(dao, U, 4);
        columns.add(pcs[1]);
        columns.add(pcs[2]);
        columns.add(pcs[3]);
    }
    
}

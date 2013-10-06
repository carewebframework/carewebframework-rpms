/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.alerts;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.piece;

import gov.ihs.cwf.ui.common.CoverSheetBase;

/**
 * Controller for user alerts cover sheet.
 * 
 * @author dmartin
 */
public class MainController extends CoverSheetBase {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void init() {
        setup("Alerts", "Alert Detail", "BEHOXQCV LIST", "BEHOXQCV DETAIL", 1, "Alert");
        super.init();
    }
    
    @Override
    protected String formatData(String data) {
        if (piece(data, U).isEmpty()) {
            return "";
        }
        return piece(data, U, 2);
    }
    
}

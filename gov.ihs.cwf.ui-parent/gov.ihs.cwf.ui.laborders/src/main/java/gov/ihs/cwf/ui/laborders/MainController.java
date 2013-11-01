/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.laborders;

import static org.carewebframework.common.StrUtil.*;

import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.ui.common.CoverSheetBase;

/**
 * Controller lab order cover sheet. Displays summary and detail views of lab orders for cover
 * sheet.
 * 
 * @author dmartin
 */
public class MainController extends CoverSheetBase {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * RPC: BEHOLRCV LIST
     * <p>
     * Returns lab orders from the last n days, where n is controlled by the the BEHOLRCV DATE RANGE
     * parameter. This parameter has separate settings for inpatients and outpatients.
     * <p>
     * RPC: BEHOLRCV DETAIL
     * <p>
     * Returns results of the specified order.
     */
    @Override
    protected void init() {
        setup("Lab Orders", "Lab Order Detail", "BEHOLRCV LIST", "'BEHOLRCV DETAIL", 1, "Lab Order", "Status", "Date");
        super.init();
    }
    
    /**
     * Logic to return detail information for specified item.
     * 
     * @param data
     * @return
     */
    @Override
    protected String getDetail(String data) {
        data = piece(data, U);
        return data.isEmpty() ? null : fromList(getBroker().callRPCList(detailRPC, null, patient.getDomainId(), data, data));
    }
    
    @Override
    protected String formatData(String data) {
        String pcs[] = split(data, U, 4);
        
        if (pcs[0].isEmpty())
            return "";
        else
            return pcs[1] + U + pcs[3] + U + BgoUtil.normalizeDate(pcs[2]);
    }
    
}

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

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.fromList;
import static org.carewebframework.common.StrUtil.piece;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.ui.common.CoverSheetBase;

/**
 * Controller lab order cover sheet. Displays summary and detail views of lab orders for cover
 * sheet.
 */
public class MainController extends CoverSheetBase<String> {
    
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
    protected void render(String dao, List<Object> columns) {
        String pcs[] = split(dao, U, 4);
        columns.add(pcs[1]);
        columns.add(pcs[3]);
        columns.add(BgoUtil.normalizeDate(pcs[2]));
    }
    
}

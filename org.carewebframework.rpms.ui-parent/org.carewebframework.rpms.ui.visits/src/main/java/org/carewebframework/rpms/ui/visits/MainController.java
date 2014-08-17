/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.visits;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.split;

import java.util.List;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.ui.common.CoverSheetBase;

/**
 * Controller for visit/appointment cover sheet.
 */
public class MainController extends CoverSheetBase<String> {
    
    private static final long serialVersionUID = 1L;
    
    private String eventName;
    
    private final IGenericEvent<Object> visitListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            refresh();
        }
        
    };
    
    @Override
    protected void init() {
        setup("Appointments/Visits", "Appointment/Visit Detail", "BEHOENCV LIST", "BEHOENCV DETAIL", -2,
            "Appointment/Visit", "Date", "Status");
        super.init();
    }
    
    @Override
    protected void requestData() {
        String evt = "PCC." + patient.getLogicalId() + ".VST";
        
        if (eventName == null || !evt.equals(eventName)) {
            if (eventName != null) {
                getEventManager().unsubscribe(eventName, visitListener);
            }
            
            eventName = evt;
            getEventManager().subscribe(eventName, visitListener);
        }
        
        super.requestData();
    }
    
    @Override
    protected void render(String dao, List<Object> columns) {
        String pcs[] = split(dao, U, 4);
        
        if (!pcs[0].isEmpty()) {
            columns.add(pcs[1]);
            columns.add(VistAUtil.normalizeDate(pcs[2]));
            columns.add(pcs[3]);
        }
    }
    
}

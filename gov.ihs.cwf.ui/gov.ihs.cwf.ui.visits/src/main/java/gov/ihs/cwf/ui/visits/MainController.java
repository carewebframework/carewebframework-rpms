/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.visits;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.split;

import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.ui.common.CoverSheetBase;

import org.carewebframework.api.event.IGenericEvent;

/**
 * Controller for visit/appointment cover sheet.
 * 
 * @author dmartin
 */
public class MainController extends CoverSheetBase {
    
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
    protected void fetchList() {
        String evt = "PCC." + patient.getDomainId() + ".VST";
        
        if (eventName == null || !evt.equals(eventName)) {
            if (eventName != null) {
                getEventManager().unsubscribe(eventName, visitListener);
            }
            
            eventName = evt;
            getEventManager().subscribe(eventName, visitListener);
        }
        
        super.fetchList();
    }
    
    @Override
    protected String formatData(String data) {
        String pcs[] = split(data, U, 4);
        
        if (pcs[0].isEmpty()) {
            return "";
        } else {
            return pcs[1] + U + BgoUtil.normalizeDate(pcs[2]) + U + pcs[3];
        }
    }
    
}

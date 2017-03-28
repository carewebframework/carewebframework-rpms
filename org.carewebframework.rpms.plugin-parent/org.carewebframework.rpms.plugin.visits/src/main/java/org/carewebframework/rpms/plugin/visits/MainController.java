/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.plugin.visits;

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
        setup("Appointments/Visits", "Appointment/Visit Detail", "BEHOENCV LIST", "BEHOENCV DETAIL", -2, "Appointment/Visit",
            "Date", "Status");
        super.init();
    }
    
    @Override
    protected void requestData() {
        String evt = "PCC." + patient.getIdElement().getIdPart() + ".VST";
        
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

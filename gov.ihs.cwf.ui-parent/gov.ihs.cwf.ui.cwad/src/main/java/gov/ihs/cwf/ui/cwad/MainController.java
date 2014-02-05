/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.cwad;

import java.util.List;

import gov.ihs.cwf.mbroker.BrokerSession;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;
import org.carewebframework.cal.api.domain.IPatient;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.ReportBox;

import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 * Controller for CWAD display.
 */
public class MainController extends PluginController implements IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private boolean popupFlags;
    
    private boolean allowPrint;
    
    private String eventId;
    
    private Window dlgFlags;
    
    private Label lblPostings;
    
    private Label lblCWAD;
    
    private BrokerSession broker;
    
    private final IGenericEvent<Object> gmraListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            doUpdate(true);
        }
        
    };
    
    private void doUpdate(boolean noPopup) {
        if (dlgFlags != null) {
            dlgFlags.detach();
            dlgFlags = null;
        }
        
        IPatient patient = PatientContext.getActivePatient();
        String cwad = patient == null ? "" : broker.callRPC("BEHOCACV CWAD", patient.getDomainId());
        lblCWAD.setValue(cwad);
        lblPostings.setValue(cwad.isEmpty() ? "No Postings" : "Postings");
        
        if (!noPopup && popupFlags && cwad.contains("F")) {
            List<String> lst = broker.callRPCList("BEHOCACV PRF", null, patient.getDomainId());
            
            if (!lst.isEmpty()) {
                dlgFlags = ReportBox.amodal(lst, "Record Flags", allowPrint);
            }
        }
        
        if (eventId != null) {
            getEventManager().unsubscribe(eventId, gmraListener);
            eventId = null;
        }
        
        if (patient != null) {
            eventId = "GMRA." + patient.getDomainId();
            getEventManager().subscribe(eventId, gmraListener);
        }
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    @Override
    public void committed() {
        doUpdate(false);
    }
    
    @Override
    public void canceled() {
    }
    
    public boolean getPopupFlags() {
        return popupFlags;
    }
    
    public void setPopupFlags(boolean popupFlags) {
        this.popupFlags = popupFlags;
        
        if (popupFlags && dlgFlags == null) {
            doUpdate(false);
        }
    }
    
    public boolean getAllowPrint() {
        return allowPrint;
    }
    
    public void setAllowPrint(boolean allowPrint) {
        this.allowPrint = allowPrint;
    }
    
    public void setBrokerSession(BrokerSession broker) {
        this.broker = broker;
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "allowPrint", "popupFlags");
        doUpdate(false);
    }
    
    @Override
    public void refresh() {
        doUpdate(true);
    }
    
    public void onClick() {
        if (PatientContext.getActivePatient() != null) {
            DetailsController.show(allowPrint);
        }
    }
    
}

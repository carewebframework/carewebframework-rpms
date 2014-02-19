/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.common;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.fromList;
import static org.carewebframework.common.StrUtil.piece;
import static org.carewebframework.common.StrUtil.toList;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.BrokerSession.IAsyncRPCEvent;
import gov.ihs.cwf.mbroker.FMDate;

import org.carewebframework.ui.sharedforms.ListViewForm;
import org.carewebframework.ui.zk.ReportBox;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;

/**
 * Controller for cover sheet components.
 * 
 * @param <T> Type of model object.
 */
public abstract class CoverSheetBase extends ListViewForm<String> implements PatientContext.IPatientContextEvent, IAsyncRPCEvent {
    
    private static final long serialVersionUID = 1L;
    
    protected Label detailView;
    
    protected Patient patient;
    
    protected int asyncHandle;
    
    private String detailTitle;
    
    protected String detailRPC;
    
    protected String listRPC;
    
    private BrokerSession broker;
    
    /**
     * Callback for status update.
     */
    private final EventListener<Event> statusCallback = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            status(event.getData().toString());
        }
        
    };
    
    /**
     * Callback for list update.
     */
    private final EventListener<Event> dataCallback = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            toList(event.getData().toString(), model, "\n");
            checkError();
        }
        
    };
    
    /**
     * Converts a FM date string to a formatted date.
     * 
     * @param FMDateStr
     * @return
     */
    public String formatFMDate(String FMDateStr) {
        return FMDateStr == null || FMDateStr.isEmpty() ? "" : FMDate.fromString(FMDateStr).toString();
    }
    
    protected void setup(String title, String detailTitle, String listRPC, String detailRPC, int sortBy, String... headers) {
        this.detailTitle = detailTitle;
        this.listRPC = listRPC;
        this.detailRPC = detailRPC;
        super.setup(title, sortBy, headers);
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    @Override
    public void committed() {
        patient = PatientContext.getCurrentPatient();
        refresh();
    }
    
    @Override
    public void canceled() {
    }
    
    /**
     * Abort any async call in progress.
     */
    @Override
    protected void asyncAbort() {
        if (asyncHandle > 0) {
            broker.callRPCAbort(asyncHandle);
            asyncHandle = 0;
        }
    }
    
    /**
     * Override load list to clear display if no patient in context.
     */
    @Override
    protected void loadData() {
        if (patient == null) {
            asyncAbort();
            reset();
            status("No patient selected.");
        } else {
            super.loadData();
        }
        
        detailView.setValue(null);
    }
    
    @Override
    protected void requestData() {
        asyncHandle = getBroker().callRPCAsync(listRPC, this, patient.getDomainId());
    }
    
    /**
     * Show detail for specified list item.
     * 
     * @param li
     */
    protected void showDetail(Listitem li) {
        String s = li == null ? null : (String) li.getValue();
        String detail = s == null ? null : getDetail(s);
        detailView.setValue(detail);
        
        if (!getShowDetailPane() && detail != null) {
            ReportBox.modal(detail, detailTitle, getAllowPrint());
        }
    }
    
    /**
     * Logic to return detail information for specified item.
     * 
     * @param data
     * @return
     */
    protected String getDetail(String data) {
        data = piece(data, U);
        return detailRPC == null || data.isEmpty() ? null : fromList(getBroker().callRPCList(detailRPC, null,
            patient.getDomainId(), data));
    }
    
    protected void checkError() {
        String data = model.isEmpty() ? null : model.get(0);
        
        if (data != null && data.startsWith(U)) {
            status(data.substring(1));
            model.clear();
        } else {
            renderData();
        }
    }
    
    /**
     * Display detail when item is selected.
     */
    @Override
    protected void itemSelected(Listitem li) {
        showDetail(li);
    }
    
    @Override
    protected void init() {
        super.init();
        committed();
    }
    
    @Override
    public void onRPCComplete(int handle, String data) {
        callback(handle, dataCallback, data);
    }
    
    @Override
    public void onRPCError(int handle, int code, String text) {
        callback(handle, statusCallback, text);
    }
    
    private void callback(int handle, EventListener<Event> listener, Object data) {
        if (handle == asyncHandle) {
            asyncHandle = 0;
            Executions.schedule(desktop, listener, new Event("onCallback", null, data));
        }
    }
    
    public BrokerSession getBroker() {
        return broker;
    }
    
    public void setBroker(BrokerSession broker) {
        this.broker = broker;
    }
    
}

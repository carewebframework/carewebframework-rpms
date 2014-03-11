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

import java.util.List;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.BrokerSession.IAsyncRPCEvent;

import org.carewebframework.api.domain.IDomainObject;
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
public abstract class CoverSheetBase<T> extends ListViewForm<T> implements PatientContext.IPatientContextEvent, IAsyncRPCEvent {
    
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
            List<String> results = toList(event.getData().toString(), null, "\r");
            String error = getError(results);
            
            if (error != null) {
                status(error);
                model.clear();
            } else {
                for (String result : results) {
                    T value = parseData(result);
                    
                    if (value != null) {
                        model.add(value);
                    }
                }
                
                renderData();
            }
        }
        
    };
    
    @SuppressWarnings("unchecked")
    protected T parseData(String data) {
        return (T) data;
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
        @SuppressWarnings("unchecked")
        T value = li == null ? null : (T) li.getValue();
        String detail = value == null ? null : getDetail(value);
        detailView.setValue(detail);
        
        if (!getShowDetailPane() && detail != null) {
            ReportBox.modal(detail, detailTitle, getAllowPrint());
        }
    }
    
    protected String getDomainId(T data) {
        return data instanceof String ? piece((String) data, U) : data instanceof IDomainObject ? Long
                .toString(((IDomainObject) data).getDomainId()) : "";
        
    }
    
    /**
     * Logic to return detail information for specified item.
     * 
     * @param data
     * @return
     */
    protected String getDetail(T data) {
        String ien = getDomainId(data);
        return detailRPC == null || ien == null || ien.isEmpty() ? null : fromList(getBroker().callRPCList(detailRPC, null,
            patient.getDomainId(), ien));
    }
    
    protected String getError(List<String> list) {
        String data = list.isEmpty() ? null : list.get(0);
        
        if (data != null && data.startsWith(U)) {
            return data.substring(1);
        } else {
            return null;
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

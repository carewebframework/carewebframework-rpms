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

import static org.carewebframework.common.StrUtil.U;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.FMDate;

import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ReportBox;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

/**
 * Controller for CWAD detail display.
 */
public class DetailsController extends GenericForwardComposer<Window> implements IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(DetailsController.class) + "details.zul";
    
    private BrokerSession broker;
    
    private Listbox lstAllergies;
    
    private Listbox lstNotes;
    
    private Window root;
    
    private boolean allowPrint;
    
    private long patientId;
    
    private final AbstractListitemRenderer<String, Object> lstAllergiesRenderer = new AbstractListitemRenderer<String, Object>() {
        
        @Override
        protected void renderItem(Listitem item, String data) {
            createCell(item, StrUtil.piece(data, U, 2));
            createCell(item, StrUtil.piece(data, U, 3));
            createCell(item, StrUtil.piece(data, U, 4));
        }
        
    };
    
    private final AbstractListitemRenderer<String, Object> lstNotesRenderer = new AbstractListitemRenderer<String, Object>() {
        
        @Override
        protected void renderItem(Listitem item, String data) {
            createCell(item, StrUtil.piece(data, U, 2));
            createCell(item, StrUtil.piece(data, U, 3));
            FMDate date = FMDate.fromString(StrUtil.piece(data, U, 5));
            createCell(item, DateUtil.formatDate(date));
        }
        
    };
    
    public static void show(boolean allowPrint) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("allowPrint", allowPrint);
        PopupDialog.popup(DIALOG, args, true, true, true);
    }
    
    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
        allowPrint = (Boolean) arg.get("allowPrint");
        patientId = PatientContext.getActivePatient().getDomainId();
        List<String> lst = broker.callRPCList("BEHOARCV LIST", null, patientId);
        //MixedCaseList(lst)
        initListbox(lstAllergies, lst, lstAllergiesRenderer);
        broker.callRPCList("BEHOCACV LIST", lst, patientId);
        initListbox(lstNotes, lst, lstNotesRenderer);
    }
    
    private void initListbox(Listbox lb, List<String> data, ListitemRenderer<?> renderer) {
        lb.setItemRenderer(renderer);
        lb.setModel(new ListModelList<String>(data));
    }
    
    public void onSelect$lstAllergies() {
        Listitem item = lstAllergies.getSelectedItem();
        String s = item.getValue();
        List<String> lst = broker.callRPCList("BEHOARCV DETAIL", null, patientId, StrUtil.piece(s, U));
        ReportBox.modal(lst, code2Text('A'), allowPrint);
        
    }
    
    public void onSelect$lstNotes() {
        Listitem item = lstNotes.getSelectedItem();
        String s = item.getValue();
        char c = StrUtil.piece(s, StrUtil.U, 2).charAt(0);
        List<String> lst = null;
        long patientId = PatientContext.getActivePatient().getDomainId();
        
        switch (c) {
            case 'A':
                lst = broker.callRPCList("BEHOCACV DETAIL", null, patientId);
                break;
            
            case 'F':
                lst = broker.callRPCList("BEHOCACV PRF", null, patientId, StrUtil.piece(s, StrUtil.U));
                break;
            
            default:
                lst = broker.callRPCList("TIU GET RECORD TEXT", null, StrUtil.piece(s, StrUtil.U));
                break;
        }
        
        if (lst != null && !lst.isEmpty()) {
            ReportBox.modal(lst, code2Text(c), allowPrint);
        }
    }
    
    private String code2Text(char c) {
        return Labels.getLabel("behCWAD.code.label." + c);
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    @Override
    public void committed() {
        root.detach();
    }
    
    @Override
    public void canceled() {
    }
    
    public void setBrokerSession(BrokerSession broker) {
        this.broker = broker;
    }
    
}

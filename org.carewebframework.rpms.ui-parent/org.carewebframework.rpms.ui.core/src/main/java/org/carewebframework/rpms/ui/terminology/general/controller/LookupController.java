/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.terminology.general.controller;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.WordUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.BgoConstants;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.ColumnControl;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.Table;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class LookupController extends BgoBaseController<String> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(LookupController.class, 1) + "lookup.zul";
    
    protected LookupParams lookupParams;
    
    protected Textbox txtSearch;
    
    protected Button btnSearch;
    
    protected Button btnSelect;
    
    protected Listbox lbResults;
    
    protected Checkbox chkUseSearchText;
    
    protected boolean autoReturn;
    
    protected String screen;
    
    private final String mode;
    
    protected final BrokerSession broker = VistAUtil.getBrokerSession();
    
    public static String execute(Table refTable) {
        return execute(refTable, null);
    }
    
    public static String execute(Table refTable, String searchText) {
        return execute(refTable, searchText, false);
    }
    
    public static String execute(Table refTable, String searchText, boolean autoReturn) {
        return execute(refTable, searchText, autoReturn, null);
    }
    
    public static String execute(Table refTable, String searchText, boolean autoReturn, String screen) {
        return execute(refTable, searchText, autoReturn, screen, new LookupController(null));
    }
    
    protected static String execute(Table refTable, String searchText, boolean autoReturn, String screen,
                                    LookupController controller) {
        Params args = BgoUtil.packageParams(refTable, searchText, autoReturn, screen);
        args.put("controller", controller);
        
        if (controller.mode != null) {
            args.put(controller.mode, true);
        }
        
        PopupDialog.popup(DIALOG, args, false, false, true);
        return controller.canceled() ? null : controller.result;
    }
    
    public LookupController(String mode) {
        this.mode = mode;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Iterator<Object> params = getParameters();
        lookupParams = new LookupParams((Table) params.next());
        ((Window) comp).setTitle("Lookup " + lookupParams.getTableName());
        String searchText = (String) params.next();
        autoReturn = (Boolean) params.next();
        screen = (String) params.next();
        
        if (screen == null) {
            screen = lookupParams.getScreen();
        }
        
        txtSearch.setText(searchText);
        
        if (lookupParams.getRpc() == null) {
            close(true);
        }
        
        initHeaders();
    }
    
    private void initHeaders() {
        Listhead lh = lbResults.getListhead();
        
        if (lh == null) {
            lh = new Listhead();
            lbResults.appendChild(lh);
        }
        
        Listheader lhr = null;
        
        for (ColumnControl ctrl : lookupParams.getColControl()) {
            lhr = new Listheader(ctrl.getLabel());
            lhr.setParent(lh);
            lhr.setVisible(ctrl.isVisible());
            lhr.setWidth(ctrl.getWidth() + "px");
            lhr.setSort("auto");
        }
        
        if (lhr != null) {
            lhr.setHflex("1");
        }
    }
    
    protected void doSearch(String searchText) {
        searchText = searchText == null ? "" : StrUtil.xlate(searchText.toUpperCase(), StrUtil.U, "");
        
        if (searchText.contains(" - ")) {
            searchText = StrUtil.piece(searchText, " - ");
        }
        
        if (!searchText.isEmpty() || lookupParams.isLookupNull()) {
            List<String> sRpc = executeRPC(searchText);
            
            if (PCC.errorCheck(sRpc)) {
                btnSelect.setDisabled(true);
                //Collapse
                //RunQuery = sRpc = ""
                return;
            }
            
            btnSelect.setDisabled(false);
            //Expand
            loadResults(sRpc);
        }
    }
    
    protected List<String> executeRPC(String searchText) {
        String params = VistAUtil.concatParams(lookupParams.getFileNum(), searchText, lookupParams.getFrom(),
            lookupParams.getDirection(), lookupParams.getMaxResults(), lookupParams.getXref(), screen,
            lookupParams.getAll(), lookupParams.getFields());
        return broker.callRPCList(lookupParams.getRpc(), null, params);
    }
    
    private void loadResults(List<String> v) {
        lbResults.getItems().clear();
        
        for (String s : v) {
            Listitem item = new Listitem();
            item.addForward(Events.ON_DOUBLE_CLICK, btnSelect, Events.ON_CLICK);
            lbResults.appendChild(item);
            String pcs[] = s.split("\\^");
            StringBuilder sb = new StringBuilder();
            
            for (ColumnControl ctrl : lookupParams.getColControl()) {
                int p = ctrl.getPiece() - 1; // Piece # for column
                String t = p < 0 ? "" : p < pcs.length ? pcs[p] : "";
                
                if (ctrl.isCapitalize()) {
                    t = WordUtils.capitalizeFully(t);
                }
                
                sb.append(t).append(StrUtil.U);
                new Listcell(t).setParent(item);
            }
            
            item.setValue(sb.toString());
        }
        
        int sortCol = lookupParams.getSortCol();
        
        if (sortCol >= 0) {
            ((Listheader) lbResults.getListhead().getChildren().get(sortCol)).sort(true, true);
        }
    }
    
    public void onClick$btnSearch() {
        String text = txtSearch.getText().trim();
        
        if (text.isEmpty() && !lookupParams.isLookupNull()) {
            PromptDialog.showWarning(BgoConstants.TX_NO_SEARCH_TEXT, BgoConstants.TC_NO_SEARCH);
            return;
        }
        
        doSearch(text);
        
        if (!lbResults.isVisible()) {
            PromptDialog.showInfo(BgoConstants.TX_NO_MATCHES, "Find Record");
        }
    }
    
    public void onClick$btnSelect() {
        Listitem item = lbResults.getSelectedItem();
        
        if (item == null) {
            return;
        }
        
        result = (String) item.getValue();
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
}

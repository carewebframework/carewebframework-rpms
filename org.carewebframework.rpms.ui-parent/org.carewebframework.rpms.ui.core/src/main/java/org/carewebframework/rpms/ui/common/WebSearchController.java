/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.common;

import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;

public class WebSearchController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = BgoConstants.RESOURCE_PREFIX + "webSearch.zul";
    
    private Combobox cboSite;
    
    private Textbox txtSearch;
    
    public static void execute(String searchText) {
        Params args = BgoUtil.packageParams(searchText);
        PopupDialog.popup(DIALOG, args, true, true, true);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        txtSearch.setText((String) arg.get(0));
        loadSites();
    }
    
    private void loadSites() {
        // Site [1] ^ BGO WEB REFERENCE SITES IEN [2] ^ URL [3]
        List<String> sRpc = getBroker().callRPCList("BGOWEB GETSITES", null, "");
        
        if (!sRpc.isEmpty()) {
            for (String v : sRpc) {
                if (v.isEmpty()) {
                    break;
                }
                String[] pcs = StrUtil.split(v, StrUtil.U, 3);
                Comboitem item = new Comboitem(pcs[0]);
                item.setValue(pcs);
            }
        }
    }
    
    public void onSelect$cboSite() {
        txtSearch.setFocus(true);
    }
    
    public void onClick$btnSearch() {
        String searchText = txtSearch.getValue();
        
        if (searchText.isEmpty()) {
            PromptDialog.showWarning(BgoConstants.TX_NO_SEARCH_TERM, BgoConstants.TC_NO_SEARCH_TERM);
            return;
        }
        
        String[] pcs = (String[]) cboSite.getSelectedItem().getValue();
        String searchSite = pcs[0];
        String searchURL = pcs[2];
        /*
        With frmBrowser
        .SearchSite = SearchSite
        .SearchURL = SearchURL
        .SearchText = SearchText
        .ItemIEN = ItemIEN
        .LinkType = LinkType
        .ItemName = ItemName
        .Show vbModal
        End With
        */
    }
}

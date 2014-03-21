/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.common.bgo;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.domain.Concept;
import org.carewebframework.vista.api.util.VistAUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class BrowserController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = BgoConstants.RESOURCE_PREFIX + "browser.zul";
    
    private static final String DEFAULT_LINK = "http://www.utdol.com/application/vocab.asp?search=";
    
    private static final String SEARCH_PLACEHOLDER = "[SEARCH TEXT]";
    
    private Combobox cboSite;
    
    private Combobox cboHistory;
    
    private Listbox lstLinks;
    
    private Textbox txtSearch;
    
    private Iframe iframe;
    
    private Button btnBack;
    
    private Button btnForward;
    
    private Component pnlHistory;
    
    private Caption caption;
    
    private Concept concept;
    
    private boolean useIframe;
    
    public static void execute(String searchText, String searchSite, Concept concept, boolean allowAddressSearch) {
        Params args = BgoUtil.packageParams(searchText, searchSite, concept, allowAddressSearch);
        PopupDialog.popup(DIALOG, args, true, true, true);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        String searchText = (String) arg.get(0);
        String searchSite = (String) arg.get(1);
        concept = (Concept) arg.get(2);
        boolean allowAddressSearch = (Boolean) arg.get(3);
        loadSites();
        searchText = searchText == null ? "" : searchText.trim();
        txtSearch.setText(searchText);
        
        if (searchSite != null && !searchSite.isEmpty()) {
            cboSite.setSelectedIndex(ListUtil.findComboboxItem(cboSite, searchSite));
        }
        
        if (concept != null) {
            caption.setLabel("Reference Links for " + concept.getCode() + " - " + concept.getShortDescription());
        }
        
        pnlHistory.setVisible(allowAddressSearch);
        
        loadLinks();
        iframe.setVisible(useIframe);
        ((Window) comp).setHeight(useIframe ? "500px" : null);
        
        if (lstLinks.getItemCount() > 0) {
            lstLinks.setSelectedIndex(0);
        } else if (!searchText.isEmpty()) {
            doSearch();
        } else {
            onSelect$cboHistory();
        }
        
        updateControls();
    }
    
    public void onSelect$cboSite() {
        doSearch();
    }
    
    public void onSelect$cboHistory() {
        if (cboHistory.getSelectedItem() != null) {
            navigate(0);
        }
    }
    
    public void onClick$lstLinks(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Listitem item = (Listitem) event.getTarget();
        navigate((String) item.getValue(), true, null);
    }
    
    public void onClick$btnAddLink() {
        Comboitem item = cboSite.getSelectedItem();
        String[] pcs = item == null ? null : (String[]) item.getValue();
        
        if (pcs != null && SaveLinkController.execute(concept, pcs[0], pcs[2])) {
            loadLinks();
        }
        /*
        With frmSaveLink
        .LinkType = LinkType
        .ItemIEN = ItemIEN
        .ItemName = ItemName
        .LocationName = brwWebBrowser.LocationName
        .LocationURL = brwWebBrowser.LocationURL
        .Execute
        If .m_bAdded Then
        If LinkType = .LinkType Then
        ItemIEN = .ItemIEN
        ItemName = .ItemName
        End If
        End If
        LoadLinks
        */
    }
    
    public void onClick$btnRemove() {
        if (lstLinks.getItemCount() == 0) {
            PromptDialog.showWarning("There are no items to delete.", "No Items");
            return;
        }
        
        Listitem item = lstLinks.getSelectedItem();
        
        if (item == null) {
            PromptDialog.showWarning(BgoConstants.TX_NO_DEL_SEL, BgoConstants.TC_NO_ITEM);
            return;
        }
        
        if (!PromptDialog.confirm(BgoConstants.TX_CNFM_LINK_DEL + item.getLabel() + "?", "Confirm Delete")) {
            return;
        }
        
        // IEN to delete [1] ^ Reference to delete [2] ^ Link Type [3]
        String[] pcs = (String[]) item.getValue();
        String s = VistAUtil.concatParams(pcs[2], pcs[3], LinkType.fromConcept(concept));
        s = getBroker().callRPC("BGOWEB DEL", s);
        
        if (BgoUtil.errorCheck(s)) {
            return;
        }
        
        loadLinks();
        
        if (lstLinks.getItemCount() > 0) {
            lstLinks.setSelectedIndex(0);
        }
    }
    
    public void onClick$btnBack() {
        navigate(-1);
    }
    
    public void onClick$btnForward() {
        navigate(1);
    }
    
    public void onClick$btnPrint() {
        
    }
    
    public void onClick$btnSearch() {
        doSearch();
    }
    
    public void onClick$btnRefresh() {
        
    }
    
    public void onClick$btnHome() {
        
    }
    
    public void onClick$btnStop() {
        
    }
    
    private void updateControls() {
        int cnt = cboHistory.getItemCount() - 1;
        int idx = cboHistory.getSelectedIndex();
        btnBack.setDisabled(cnt < 0 || idx <= 0);
        btnForward.setDisabled(cnt < 0 || idx >= cnt);
    }
    
    private static final List<String> STOP_WORDS = Arrays.asList(new String[] { "NOS", "NEC", "IS", "AND", "OF", "WITH",
            "FOR", "TO", "UNSPECIFIED", "NOT", "ELSEWHERE", "CLASSIFIED", "OTHERWISE", "SPECIFIED" });
    
    private void doSearch() {
        StringTokenizer tkn = new StringTokenizer(txtSearch.getValue().toUpperCase(), " ./=-,;+");
        StringBuilder sb = new StringBuilder();
        
        while (tkn.hasMoreTokens()) {
            String s = tkn.nextToken();
            
            if (!s.isEmpty() && !STOP_WORDS.contains(s)) {
                sb.append(s).append(' ');
            }
        }
        
        if (sb.length() == 0) {
            return;
        }
        
        Comboitem item = cboSite.getSelectedItem();
        String[] pcs = item == null ? null : (String[]) item.getValue();
        String searchURL = pcs == null ? DEFAULT_LINK : pcs[2];
        
        if (searchURL.contains(SEARCH_PLACEHOLDER)) {
            searchURL = searchURL.replace(SEARCH_PLACEHOLDER, sb.toString());
        } else {
            searchURL += sb.toString();
        }
        
        navigate(searchURL, true, item);
    }
    
    private void loadLinks() {
        lstLinks.getItems().clear();
        lstLinks.setVisible(false);
        LinkType linkType = LinkType.fromConcept(concept);
        
        if (linkType == null) {
            return;
        }
        
        String param = VistAUtil.concatParams(linkType, concept.getDomainId());
        List<String> links = getBroker().callRPCList("BGOWEB GET", null, param);
        
        for (String link : links) {
            // Name [1]^ URL [2] ^ Link IEN [3] ^ Value [4] ^ Type [5]
            String[] pcs = StrUtil.split(link, StrUtil.U, 5);
            lstLinks.appendItem(pcs[0], null).setValue(pcs);
        }
        
        int rows = lstLinks.getItemCount();
        lstLinks.setRows(rows > 5 ? 5 : rows);
        lstLinks.setVisible(rows > 0);
    }
    
    private void loadSites() {
        cboSite.getItems().clear();
        List<String> sites = getBroker().callRPCList("BGOWEB GETSITES", null, "");
        
        for (String site : sites) {
            // Site [1] ^ File IEN [2] ^ URL [3]
            String[] pcs = StrUtil.split(site, StrUtil.U, 3);
            cboSite.appendItem(pcs[0]).setValue(pcs);
        }
    }
    
    private void navigate(int inc) {
        int idx = cboHistory.getSelectedIndex() + inc;
        cboHistory.setSelectedIndex(idx);
        Comboitem item = cboHistory.getSelectedItem();
        navigate(item.getLabel(), false, (Comboitem) item.getValue());
    }
    
    private void navigate(String url, boolean updateHistory, Comboitem site) {
        if (useIframe) {
            iframe.setSrc(url);
        } else {
            execution.sendRedirect(url, DIALOG);
        }
        
        if (updateHistory) {
            int idx = ListUtil.findComboboxItem(cboHistory, url);
            
            if (idx == -1) {
                idx = cboHistory.getItemCount();
                cboHistory.appendItem(url).setValue(site);
            }
            
            cboHistory.setSelectedIndex(idx);
            site = (Comboitem) cboHistory.getSelectedItem().getValue();
        }
        
        cboSite.setSelectedItem(site);
        updateControls();
    }
}

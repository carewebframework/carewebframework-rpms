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

import java.util.List;

import gov.ihs.cwf.common.bgo.LookupParams.Table;
import gov.ihs.cwf.domain.Concept;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class SaveLinkController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = BgoConstants.RESOURCE_PREFIX + "saveLink.zul";
    
    private Textbox txtLow; //txtItem(0)
    
    private Textbox txtHigh; //txtItem(1)
    
    private Textbox txtRef;
    
    private Label lblLow;
    
    private Label lblHigh;
    
    private Component pnlLow;
    
    private Component pnlHigh;
    
    private Combobox cboType;
    
    private Combobox cboCat;
    
    private Combobox cboRef;
    
    private Listbox lstLinks;
    
    private Concept concept1;
    
    private Concept concept2;
    
    private String siteName;
    
    private String siteURL;
    
    private Table refTable;
    
    public static boolean execute(Concept concept, String siteName, String siteURL) {
        Params args = BgoUtil.packageParams(concept, siteName, siteURL);
        Window window = PopupDialog.popup(DIALOG, args, true, true, true);
        @SuppressWarnings("unchecked")
        BgoBaseController<Object> controller = (BgoBaseController<Object>) BgoBaseController.getController(window);
        return !controller.canceled();
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        concept1 = (Concept) arg.get(0);
        siteName = (String) arg.get(1);
        siteURL = (String) arg.get(2);
        txtLow.setText(concept1.getShortDescription());
    }
    
    public void onSelect$cboType() {
        Comboitem item = cboType.getSelectedItem();
        
        if (item == null) {
            return;
        }
        
        lblLow.setValue(item.getLabel());
        LinkType linkType = (LinkType) item.getValue();
        boolean isRange = linkType.isRange();
        pnlHigh.setVisible(isRange);
        lblHigh.setVisible(isRange);
        lblHigh.getPreviousSibling().setVisible(isRange);
        
        switch (linkType) {
            case ICD9_RANGE:
                lblLow.setValue("Low ICD Code");
                lblHigh.setValue("High ICD Code");
                refTable = Table.rtICD;
                break;
            
            case ICD9:
                refTable = Table.rtICD;
                break;
            
            case CPT4_RANGE:
                lblLow.setValue("Low CPT Code");
                lblHigh.setValue("High CPT Code");
                refTable = Table.rtCPT;
                break;
            
            case CPT4:
                refTable = Table.rtCPT;
                break;
            
            case EDU:
                refTable = Table.rtEduTopic;
                break;
            
            case EXAM:
                refTable = Table.rtExam;
                break;
            
            case IMM:
                refTable = Table.rtImmunization;
                break;
            
            case SKIN:
                refTable = Table.rtSkinTest;
                break;
        }
    }
    
    public void onClick$btnLow() {
        doSearch(true);
    }
    
    public void onClick$btnHigh() {
        doSearch(false);
    }
    
    public void onClick$btnRemove() {
        Listitem item = lstLinks.getSelectedItem();
        
        if (item == null) {
            PromptDialog.showWarning(BgoConstants.TX_NO_DEL_SEL, BgoConstants.TC_NO_ITEM);
            return;
        }
        String s = ""; //Piece(m_colURL.Item(lstLinks.List(lstLinks.ListIndex)), "{", 2)
        
        if (!PromptDialog.confirm(BgoConstants.TX_CNFM_LINK_DEL + item.getLabel()
                + "?\nDoing so will also delete all other references for the current website.", "Confirm Delete")) {
            return;
        }
        
        if (!s.isEmpty()) {
            s = getBroker().callRPC("BGOWEB DEL", s);
            
            if (!BgoUtil.errorCheck(s)) {
                loadLinks();
            }
        }
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onClick$btnSave() {
        String name = txtRef.getText().trim();
        
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(siteURL)) {
            PromptDialog.showError("You must enter a reference name and have a valid web address to save a reference link.",
                "Missing Data");
            return;
        }
        
        String code1 = concept1 == null ? "" : concept1.getCode();
        String code2 = concept2 == null ? "" : concept2.getCode();
        int linkType = LinkType.fromConcept(concept1).ordinal();
        
        if (code1.compareTo(code2) > 0) {
            String s = code1;
            code1 = code2;
            code2 = s;
        }
        
        // Type [1] ^ Value [2] ^ Name [3] ^ URL [4] ^ User IEN [5] ^ Value 2 [6] ^ Category [7]
        
        String s = BgoUtil.concatParams(linkType, code1, name, siteURL, UserContext.getActiveUser().getDomainId(), code2,
            cboCat.getText());
        s = getBroker().callRPC("BGOWEB SET", s);
        
        if (!BgoUtil.errorCheck(s)) {
            close(false);
        }
    }
    
    private void loadRef() {
        cboRef.getItems().clear();
        Comboitem item = cboCat.getSelectedItem();
        String cat = item == null ? "" : item.getLabel();
        String s = BgoUtil.concatParams(UserContext.getActiveUser().getDomainId(), cat);
        List<String> refs = getBroker().callRPCList("BGOWEB GETREF", null, s);
        
        if (!BgoUtil.errorCheck(refs)) {
            for (String ref : refs) {
                cboRef.appendItem(StrUtil.piece(ref, StrUtil.U));
            }
        }
    }
    
    private void loadCat() {
        cboCat.getItems().clear();
        List<String> cats = getBroker().callRPCList("BGOWEB GETCATS", null, UserContext.getActiveUser().getDomainId());
        
        if (!BgoUtil.errorCheck(cats)) {
            for (String cat : cats) {
                cboCat.appendItem(StrUtil.piece(cat, StrUtil.U));
            }
        }
    }
    
    private void loadLinks() {
        lstLinks.getItems().clear();
        int linkType = LinkType.fromConcept(concept1).ordinal();
        String s = BgoUtil.concatParams(linkType, concept1.getDomainId(), null, null, UserContext.getActiveUser()
                .getDomainId());
        List<String> links = getBroker().callRPCList("BGOWEB GET", null, s);
        
        if (!BgoUtil.errorCheck(links)) {
            for (String link : links) {
                String[] pcs = StrUtil.split(link, StrUtil.U, 2);
                lstLinks.appendItem(pcs[0], pcs[1]);
            }
        }
    }
    
    private void doSearch(boolean lowValue) {
        String result = null;
        Textbox txt = lowValue ? txtLow : txtHigh;
        Concept concept = lowValue ? concept1 : concept2;
        
        switch (refTable) {
            case rtICD:
                result = ICDLookupController.execute(txt.getText(), false);
                break;
            
            case rtCPT:
                result = CPTLookupController.execute(txt.getText(), false);
                break;
            
            default:
                result = LookupController.execute(refTable, txt.getText(), false);
                break;
        }
        
        if (result != null) {
            String[] pcs = StrUtil.split(result, StrUtil.U, 3);
            
            if (concept == null) {
                concept = new Concept(concept1.getCodeSystem());
                concept2 = concept;
            }
            
            concept.setDomainId(Long.parseLong(pcs[0]));
            concept.setCode(pcs[1]);
            concept.setShortDescription(pcs[2]);
            txt.setText(pcs[1] + " - " + pcs[2]);
        }
    }
}

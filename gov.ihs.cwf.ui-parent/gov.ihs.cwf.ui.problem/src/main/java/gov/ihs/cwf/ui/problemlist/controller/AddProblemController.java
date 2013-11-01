/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.problemlist.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.icons.IconUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import gov.ihs.cwf.common.bgo.BgoBaseController;
import gov.ihs.cwf.common.bgo.BgoConstants;
import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.common.bgo.ICDLookupController;
import gov.ihs.cwf.common.bgo.Params;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Institution;
import gov.ihs.cwf.domain.ICD9Concept;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Problem;
import gov.ihs.cwf.domain.ProblemNote;
import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.ui.problemlist.util.Constants;
import gov.ihs.cwf.util.RPMSUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class AddProblemController extends BgoBaseController<Problem> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PREFIX + "addProblem.zul";
    
    private static final String DELETE_ICON = IconUtil.getIconPath("delete.png");
    
    private Radio radActive;
    
    private Radio radInactive;
    
    private Radio radPersonal;
    
    private Radio radFamily;
    
    private Label lblPrefix;
    
    private Textbox txtID;
    
    private Combobox cboPriority;
    
    private Textbox txtICD;
    
    private Button btnICD;
    
    private Textbox txtNarrative;
    
    private Datebox datOnset;
    
    private Listbox lstNotes;
    
    private Textbox txtNotes;
    
    private Caption capNotes;
    
    private Problem problem;
    
    private ICD9Concept icd;
    
    private List<ProblemNote> changedNotes = new ArrayList<ProblemNote>();
    
    public static Problem execute(Problem problem) {
        if (problem == null)
            problem = new Problem(PatientContext.getCurrentPatient());
        
        Params params = new Params(problem);
        Window dlg = PopupDialog.popup(DIALOG, params, true, true, true);
        AddProblemController controller = (AddProblemController) FrameworkController.getController(dlg);
        return controller.canceled() ? null : problem;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Params params = (Params) arg;
        this.problem = (Problem) params.get(0);
        loadForm();
    }
    
    /**
     * Loads form data from the current problem.
     */
    private void loadForm() {
        ICD9Concept icd9 = problem.getIcd9Code();
        
        if (icd9 != null)
            txtICD.setText(icd9.getCode());
        
        String narr = problem.getProviderNarrative();
        
        if (narr == null)
            narr = icd9 == null ? "" : icd9.getLongDescription();
        
        String probId = problem.getNumberCode();
        
        if (probId == null || probId.isEmpty()) 
            probId = getBroker().callRPC("BGOPROB NEXTID", PatientContext.getCurrentPatient().getDomainId());
        
        String pcs[] = probId.split("\\-", 2);
        lblPrefix.setValue(pcs[0] + " - ");
        txtID.setValue(pcs.length < 2 ? "" : pcs[1]);
        txtNarrative.setText(narr);
        datOnset.setValue(problem.getOnsetDate());
        
        if ("P".equals(problem.getProblemClass()))
            radPersonal.setSelected(true);
        else if ("F".equals(problem.getProblemClass()))
            radFamily.setSelected(true);
        else if ("I".equals(problem.getStatus()))
            radInactive.setSelected(true);
        else
            radActive.setSelected(true);
        
        int priority = NumberUtils.toInt(problem.getPriority());
        cboPriority.setSelectedIndex(priority < 0 || priority > 5 ? 0 : priority);
        loadNotes();
    }
    
    private void loadNotes() {
        if (problem.getDomainId() == 0) {
            lstNotes.setVisible(false);
            txtNotes.setVisible(true);
            capNotes.setLabel("Note (3-60 characters)");
            return;
        }
        
        lstNotes.getItems().clear();
        List<String> notes = getBroker().callRPCList("BGOPRBN GET", null, problem.getDomainId());
        
        if (BgoUtil.errorCheck(notes))
            return;
        
        
        /*
         * Location IEN [1] ^ Note IEN [2] ^ Note # [3] ^ Narrative [4] ^
         * Status [5] ^ Date Added [6] ^ Author Name [7]
         * e.g.,
         * 3987^1^1^STECWFD DEPENDENCY (LOW DOSE)^A^2960901^
        */
        for (String note : notes) 
            renderNote(new ProblemNote(note));
    }
    
    private boolean updateNotes() {
        boolean result = true;
        Iterator<ProblemNote> iter = changedNotes.iterator();
        
        while (iter.hasNext()) {
            ProblemNote pn = iter.next();
            boolean success = pn.getDomainId() == 0 ? addNote(pn) : deleteNote(pn);
            result &= success;
            
            if (success)
                iter.remove();
        }
        
        return result;
    }
    
    private boolean deleteNote(ProblemNote pn) {
        String s = BgoUtil.concatParams(problem.getDomainId(), pn.getFacility().getDomainId(), pn.getDomainId());
        s = getBroker().callRPC("BGOPRBN DEL", s);
        return !BgoUtil.errorCheck(s);
    }
    
    /**
     * Commits a note to the database.
     * 
     * @param pn The problem note to commit.
     * @return True if successful.
     */
    private boolean addNote(ProblemNote pn) {
        String s = BgoUtil.concatParams(problem.getDomainId(), null, pn.getFacility().getDomainId(), null, pn.getNarrative());
        s = getBroker().callRPC("BGOPRBN SET", s);
        
        if (BgoUtil.errorCheck(s))
            return false;
        
        // Problem IEN [1] ^ Note IEN [2] ^ Location IEN [3] ^ Note # [4] ^ Narrative [5] ^
        // Status [6] ^ Date Entered [7] ^ Author Name [8] ^ Note ID [9]
        
        String[] pcs = StrUtil.split(s, StrUtil.U, 9);
        
        pn.setDomainId(Long.parseLong(pcs[1]));
        pn.setFacility(new Institution(Long.parseLong(pcs[2])));
        pn.setNumber(pcs[3]);
        pn.setNarrative(pcs[4]);
        pn.setStatus(pcs[5]);
        pn.setDateAdded(new FMDate(pcs[6]));
        pn.setAuthor(pcs[7]);
        return true;
    }
    
    private void renderNote(ProblemNote pn) {
        Listitem item = new Listitem();
        lstNotes.appendChild(item);
        Listcell cell = addCell(item, "");
        cell.setSclass("bgo-problem-icon-cell");
        Toolbarbutton btn = new Toolbarbutton("", DELETE_ICON);
        btn.setTooltiptext("Delete this note.");
        btn.addForward(Events.ON_CLICK, lstNotes, "onDeleteNote");
        cell.appendChild(btn);
        addCell(item, pn.getNumber());  // Note #
        addCell(item, pn.getNarrative()).setHflex("1");  // Narrative
        addCell(item, DateUtil.formatDate(pn.getDateAdded()));  // Date added
        addCell(item, pn.getAuthor());  // Author
        item.setValue(pn);
    }
    
    private Listcell addCell(Listitem item, Object object) {
        Listcell cell = new Listcell(object.toString());
        cell.setTooltiptext(object.toString());
        item.appendChild(cell);
        return cell;
    }
    
    private boolean validateAll() {
        if (!NumberUtils.isDigits(txtID.getValue())) {
            PromptDialog.showError(BgoConstants.TX_NO_NUMERIC, "Not Numeric");
            txtID.setFocus(true);
            return false;
        }
        
        String txt = BgoUtil.trimNarrative(txtNarrative.getValue());
        if (txt.isEmpty()) {
            PromptDialog.showError(BgoConstants.TX_NO_NARR, BgoConstants.TC_NO_NARR);
            return false;
        }
        
        if (txt.length() > 80)
            if (PromptDialog.confirm(BgoConstants.TX_NARR_TOO_LONG, BgoConstants.TC_NARR_TOO_LONG))
                txt = txt.substring(0, 80);
            else
                return false;
        
        txtNarrative.setValue(txt);
        return true;
    }
    
    public void onDeleteNote$lstNotes(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Listitem item = (Listitem) ZKUtil.findAncestor(event.getTarget(), Listitem.class);
        ProblemNote pn = (ProblemNote) item.getValue();
        
        if (PromptDialog.confirm("Are you sure that you wish to delete this note:\n"
            + pn.getNumber() + " - " + pn.getNarrative(), "Delete Note?")) {
            item.detach();
            
            if (pn.getDomainId() > 0)
                changedNotes.add(pn);
            else
                changedNotes.remove(pn);
        }
    }
    
    public void onClick$btnSave() {
        if (!validateAll())
            return;
        
        Patient patient = PatientContext.getCurrentPatient();
        String sParam = BgoUtil.concatParams(patient.getDomainId(), txtID.getValue(), problem.getFacility().getDomainId(),
            problem.getDomainId());
        String sRpc = getBroker().callRPC("BGOPROB CKID", sParam);
        
        if (BgoUtil.errorCheck(sRpc, "Invalid ID"))
            return;
        
        Institution institution = RPMSUtil.getCurrentInstitution();
        String sNum = "1".equals(sRpc) ? "" : txtID.getValue(); // Pass only if changed
        // ICD IEN or Code [1] ^ Narrative [2] ^ Location IEN [3] ^ Date of Onset [4] ^ Class [5] ^
        // Status [6] ^ Patient IEN [7] ^ Problem IEN [8] ^ Problem # [9]
        String txtIcd = txtICD.getValue().trim();
        String txtIcd1 = StrUtil.piece(txtIcd, " - ");
        
        if (icd != null && !"0".equals(icd.getCode()))
            sParam = icd.getCode();
        else if (StringUtils.isEmpty(txtIcd))
            sParam = ".9999";
        else if (!StringUtils.isEmpty(txtIcd1))
            sParam = txtIcd1;
        else
            sParam = "";
        
        int priority = cboPriority.getSelectedIndex();
        
        // ICD IEN or Code [1] ^ Narrative [2] ^ Location IEN [3] ^ Date of Onset [4] ^ Class [5] ^
        // Status [6] ^ Patient IEN [7] ^ Problem IEN [8] ^ Problem # [9] ^ Priority [10]
        sParam = BgoUtil.concatParams(sParam, txtNarrative.getValue(), institution.getDomainId(),
            datOnset.getValue() == null ? "@" : datOnset.getValue(), radPersonal.isChecked() ? "P"
                    : radFamily.isChecked() ? "F" : "", radActive.isChecked() ? "A" : "I", patient.getDomainId(), problem.getDomainId(),
            sNum, priority <= 0 ? "@" : priority);
        sRpc = getBroker().callRPC("BGOPROB SET", sParam);
        
        if (BgoUtil.errorCheck(sRpc))
            return;
        
        problem.setDomainId(NumberUtils.toLong(sRpc));
        
        if (txtNotes.isVisible() && !StringUtils.isEmpty(txtNotes.getValue())) {
            sParam = BgoUtil.concatParams(problem.getDomainId(), null, institution.getDomainId(), null, txtNotes.getValue());
            sRpc = getBroker().callRPC("BGOPRBN SET", sParam);
            
            if (BgoUtil.errorCheck(sRpc))
                return;
        }
        
        if (!updateNotes()) 
            return;
        
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onClick$btnICD() {
        String result = ICDLookupController.execute(txtICD.getText());
        
        if (result != null) {
            String pcs[] = StrUtil.split(result, StrUtil.U, 3);
            icd = new ICD9Concept();
            icd.setDomainId(NumberUtils.toLong(pcs[0]));
            icd.setCode(pcs[1]);
            icd.setShortDescription(pcs[2]);
            txtICD.setValue(pcs[2]);
        }
    }
    
    public void onClick$btnAddNote() {
        String note = AddNoteController.execute();
        
        if (note == null || note.isEmpty())
            return;
        
        ProblemNote pn = new ProblemNote();
        IUser user = UserContext.getActiveUser();
        pn.setAuthor(user.getFullName());
        pn.setFacility(RPMSUtil.getCurrentInstitution());
        pn.setNumber("*");
        pn.setNarrative(note);
        pn.setDateAdded(new FMDate(DateUtil.stripTime(new Date())));
        changedNotes.add(pn);
        renderNote(pn);
    }
}

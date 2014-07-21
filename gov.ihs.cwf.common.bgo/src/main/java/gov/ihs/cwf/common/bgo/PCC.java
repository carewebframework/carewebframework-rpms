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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.cal.api.context.EncounterContext;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.cal.api.context.UserContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.model.resource.Encounter;
import org.carewebframework.fhir.model.resource.Location;
import org.carewebframework.fhir.model.resource.Patient;
import org.carewebframework.fhir.model.resource.Practitioner;
import org.carewebframework.fhir.model.resource.User;
import org.carewebframework.fhir.model.type.HumanName;
import org.carewebframework.fhir.model.type.Identifier;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.domain.EncounterUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

public abstract class PCC {
    
    public static String addProcedure(String cptIEN, String narrative, String qty, String mod1, String mod2, String principal) {
        //if (!checkActiveVisit())
        //    return "-1";
        
        Patient patient = PatientContext.getActivePatient();
        Encounter encounter = EncounterContext.getActiveEncounter();
        User user = UserContext.getActiveUser();
        // Visit IEN [1] ^ CPT IEN [2] ^ Patient IEN [3] ^ Event Date [4] ^ Quantity [5] ^
        // Diagnosis [6] ^ Modifier #1 [7] ^ Provider IEN [8] ^ Principal [9] ^ V File IEN [10] ^
        // Narrative [11] ^ Modifier #2 [12] ^ Location IEN [13] ^ Outside Location [14] ^
        // Historical [15] ^ ICD Procedure Flag [16] ^ No Dups [17]
        String sParam = VistAUtil.concatParams(encounter.getDomainId(), cptIEN, patient.getDomainId(), null, qty, null,
            mod1, user.getDomainId(), principal, null, narrative, mod2);
        return VistAUtil.getBrokerSession().callRPC("BGOVCPT SET", sParam);
    }
    
    public static boolean deleteProcedure(String vfIEN) {
        String s = VistAUtil.concatParams(vfIEN, "CPT");
        s = VistAUtil.getBrokerSession().callRPC("BGOVCPT DEL", s);
        return !BgoUtil.errorCheck(s);
    }
    
    public static String addProvider(String provIEN, boolean primary, boolean forcePrimary) {
        Encounter encounter = EncounterContext.getActiveEncounter();
        Patient patient = PatientContext.getActivePatient();
        String sParam = VistAUtil.concatParams(encounter.getDomainId(), patient.getDomainId(), provIEN, primary ? "P" : "S",
            forcePrimary ? "1" : "");
        return VistAUtil.getBrokerSession().callRPC("BGOVPRV SETVPRV", sParam);
    }
    
    public static String addPOV(String icdIEN, String narrative, String onset) {
        //if (!checkActiveVisit())
        //    return "-1";
        
        Patient patient = PatientContext.getActivePatient();
        Encounter encounter = EncounterContext.getActiveEncounter();
        User user = UserContext.getActiveUser();
        // VPOV IEN [1] ^ Visit IEN [2] ^ ICD Code IEN [3] ^ Patient IEN [4] ^ Narrative [5] ^
        // Stage [6] ^ Modifier [7] ^ Cause Dx [8] ^ First/Revisit [9] ^ Injury E-Code [10] ^
        // Injury Place [11] ^ Primary/Secondary [12] ^ Injury Date [13] ^ Onset Date [14] ^
        // Provider IEN [15]
        String s = VistAUtil
                .concatParams(null, encounter.getDomainId(), "`" + icdIEN, patient.getDomainId(), narrative, null, null,
                    null, null, null, null, null, null, onset, EncounterUtil.getCurrentProvider(encounter).getDomainId());
        s = VistAUtil.getBrokerSession().callRPC("BGOVPOV SET", s);
        
        if (BgoUtil.errorCode(s) == 0) {
            s = addProvider(user.getDomainId(), true, false);
        }
        
        if (BgoUtil.errorCode(s) == 1098) {
            if (PromptDialog.confirm(StrUtil.piece(s, StrUtil.U, 2) + StrUtil.CRLF
                    + "Are you the primary provider for this visit?", "Change Primary Provider?")) {
                s = addProvider(user.getDomainId(), true, true);
            } else {
                s = null;
            }
        }
        return s;
    }
    
    public static boolean deletePOV(String visitIEN, String povIEN, String icdIEN) {
        
        if (!BgoUtil.checkSecurity(false)) {
            return false;
        }
        
        if (!PromptDialog.confirm("Are you sure you want to delete the purpose of visit?", "Remove POV?")) {
            return false;
        }
        
        String s = VistAUtil.getBrokerSession().callRPC("BGOVPOV DEL", povIEN);
        
        if (BgoUtil.errorCheck(s)) {
            return false;
        }
        
        deletePatientEd(visitIEN, icdIEN);
        return true;
    }
    
    private static void deletePatientEd(String visitIEN, String icdIEN) {
        Patient patient = PatientContext.getActivePatient();
        User user = UserContext.getActiveUser();
        BrokerSession broker = VistAUtil.getBrokerSession();
        String s = VistAUtil.concatParams(patient.getDomainId(), visitIEN);
        List<String> v = broker.callRPCList("BGOVPED GET", null, s);
        
        if (BgoUtil.errorCheck(v)) {
            return;
        }
        
        String priIEN = StrUtil.piece(broker.callRPC("BGOVPRV PRIPRV", visitIEN), StrUtil.U);
        String prvIEN = user.getDomainId();
        // Topic Name [1] ^ Visit Date [2] ^ Level [3] ^ Provider Name [4] ^ Group/Individual [5] ^
        // Length [6] ^ CPT [7] ^ Comment [8] ^ Topic Category [9] ^ Behavior [10] ^ Objective Met [11] ^
        // Visit Locked [12] ^ Location Name [13] ^ VFile IEN [14] ^ Visit IEN [15] ^ Topic IEN [16] ^
        // Location IEN [17] ^ Provider IEN [18] ^ Visit Category [19] ^ ICD9 [20] ^ Comments [21] ^
        // ICD9 IEN [22] ^ CPT IEN [23]
        // e.g.,
        // CONGESTIVE HRT FAILURE UNSPEC-ANATOMY AND PHYSIOLOGY^12/06/2006^GOOD^USER,POWER^Individual^5^^^ANA
        // TOMY AND PHYSIOLOGY^GOAL MET^goal was met!^0^^21106^2020430^3262^^2779^A^CONGESTIVE HRT FAILURE UNSPEC
        // ^this is the goal set|goal was met!|^9061^
        
        s = StrUtil.CRLF;
        
        for (int i = 0; i < v.size();) {
            String val = v.get(i);
            
            if (StrUtil.piece(val, StrUtil.U, 22).equals(icdIEN)
                    && (StrUtil.piece(val, StrUtil.U, 18).equals(prvIEN) || priIEN.equals(prvIEN))) {
                i++;
                s += StrUtil.CRLF + "--> " + StrUtil.piece(val, StrUtil.U);
            } else {
                v.remove(i);
            }
        }
        
        if (v.isEmpty()) {
            return;
        }
        
        if (!PromptDialog.confirm("Do you also want to delete the following related education event"
                + (v.size() == 1 ? "?" : "s?") + s, "Remove Education Events?")) {
            return;
        }
        
        for (String val : v) {
            String sRpc = broker.callRPC("BGOVPED DEL", StrUtil.piece(val, StrUtil.U, 14));
            BgoUtil.errorCheck(sRpc);
        }
    }
    
    public static String addProblem(String sICDIEN, String narrative, String onset) {
        BrokerSession broker = VistAUtil.getBrokerSession();
        String institution = UserContext.getActiveUser().getOrganization().getDomainId();
        Patient patient = PatientContext.getActivePatient();
        
        if (onset == null) {
            onset = StrUtil.piece(broker.getHostTime().toString(), " ");
        }
        
        // ICD IEN or Code [1] ^ Narrative [2] ^ Location IEN [3] ^ Date of Onset [4] ^ Class [5] ^
        // Status [6] ^ Patient IEN [7] ^ Problem IEN [8] ^ Problem # [9]
        String s = VistAUtil.concatParams(sICDIEN, narrative, institution, onset, null, "A", patient.getDomainId(), null,
            null);
        return broker.callRPC("BGOPROB SET", s);
    }
    
    /**
     * Parse a problem ID into an entity identifier.
     *
     * @param value One of: [prefix]-[id] or [id]
     * @return An entity identifier.
     */
    public static Identifier parseProblemID(String value) {
        String s = StrUtil.piece(value, "-", 2);
        String id = "";
        String prefix = "";
        
        if (s.isEmpty()) {
            id = value;
        } else {
            id = s;
            prefix = StrUtil.piece(value, "-");
        }
        Identifier ident = new Identifier();
        ident.setValueSimple(id);
        ident.setLabelSimple(prefix);
        return ident;
    }
    
    public static int compareProblemIDs(String id1, String id2) {
        Identifier ed1 = parseProblemID(id1);
        Identifier ed2 = parseProblemID(id2);
        int i = ed1.getLabelSimple().compareToIgnoreCase(ed2.getLabelSimple());
        
        if (i == 0) {
            int v1 = NumberUtils.toInt(ed1.getValueSimple());
            int v2 = NumberUtils.toInt(ed2.getValueSimple());
            i = v1 == v2 ? 0 : v1 > v2 ? 1 : -1;
        }
        
        return i;
    }
    
    /**
     * Load CPT modifiers for the specified code and reference date.
     *
     * @param cbo
     * @param cptCode
     * @param refDate
     */
    public static void loadModifiers(Combobox cbo, String cptCode, String refDate) {
        cbo.getItems().clear();
        addModifier(cbo, "^^0");
        
        if (!StringUtils.isEmpty(cptCode)) {
            List<String> lst = VistAUtil.getBrokerSession().callRPCList("BGOVCPT GETMODS", null,
                cptCode + StrUtil.U + refDate);
            
            for (String s : lst) {
                addModifier(cbo, s); // Name ^ CPT Modifier Code ^ Modifier IEN
            }
        }
        
        cbo.setSelectedIndex(0);
    }
    
    private static void addModifier(Combobox cbo, String modifier) {
        Comboitem item = cbo.appendItem(StrUtil.piece(modifier, StrUtil.U));
        item.setValue(Integer.parseInt(StrUtil.piece(modifier, StrUtil.U, 3)));
    }
    
    public static Location parseLocation(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }
        
        return new Location();
    }
    
    public static FMDate parseDate(String value) {
        return value == null || value.isEmpty() ? null : StringUtils.isNumeric(value.replace(".", "")) ? new FMDate(value)
                : new FMDate(DateUtil.parseDate(value));
    }
    
    public static Practitioner parsePractitioner(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }
        
        String[] pcs = StrUtil.split(value, "~", 2);
        Practitioner practitioner = new Practitioner();
        practitioner.setDomainId(pcs[0]);
        practitioner.setName(new HumanName(pcs[1]));
        return practitioner;
    }
    
}

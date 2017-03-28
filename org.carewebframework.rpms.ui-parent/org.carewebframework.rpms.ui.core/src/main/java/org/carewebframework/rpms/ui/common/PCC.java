/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.ui.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoException;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.domain.PCCUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.api.encounter.EncounterContext;
import org.hspconsortium.cwf.api.encounter.EncounterParticipantContext;
import org.hspconsortium.cwf.api.patient.PatientContext;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

public class PCC {
    
    public static boolean errorCheck(List<String> data) {
        return data != null && !data.isEmpty() && errorCheck(data.get(0));
    }
    
    public static boolean errorCheck(String data) {
        try {
            BgoUtil.errorCheck(data);
            return false;
        } catch (BgoException e) {
            PromptDialog.showError(e.getMessage());
            return true;
        }
    }
    
    public static String addProcedure(String cptIEN, String narrative, String qty, String mod1, String mod2,
                                      String principal) {
        //if (!checkActiveVisit())
        //    return "-1";
        
        Patient patient = PatientContext.getActivePatient();
        Encounter encounter = EncounterContext.getActiveEncounter();
        IUser user = UserContext.getActiveUser();
        // Visit IEN [1] ^ CPT IEN [2] ^ Patient IEN [3] ^ Event Date [4] ^ Quantity [5] ^
        // Diagnosis [6] ^ Modifier #1 [7] ^ Provider IEN [8] ^ Principal [9] ^ V File IEN [10] ^
        // Narrative [11] ^ Modifier #2 [12] ^ Location IEN [13] ^ Outside Location [14] ^
        // Historical [15] ^ ICD Procedure Flag [16] ^ No Dups [17]
        String sParam = VistAUtil.concatParams(encounter.getIdElement().getIdPart(), cptIEN,
            patient.getIdElement().getIdPart(), null, qty, null, mod1, user.getLogicalId(), principal, null, narrative,
            mod2);
        return VistAUtil.getBrokerSession().callRPC("BGOVCPT SET", sParam);
    }
    
    public static boolean deleteProcedure(String vfIEN) {
        String s = VistAUtil.concatParams(vfIEN, "CPT");
        s = VistAUtil.getBrokerSession().callRPC("BGOVCPT DEL", s);
        return !errorCheck(s);
    }
    
    public static String addProvider(String provIEN, boolean primary, boolean forcePrimary) {
        Encounter encounter = EncounterContext.getActiveEncounter();
        Patient patient = PatientContext.getActivePatient();
        String sParam = VistAUtil.concatParams(encounter.getIdElement().getIdPart(), patient.getIdElement().getIdPart(),
            provIEN, primary ? "P" : "S", forcePrimary ? "1" : "");
        return VistAUtil.getBrokerSession().callRPC("BGOVPRV SETVPRV", sParam);
    }
    
    public static String addPOV(String icdIEN, String narrative, String onset) {
        //if (!checkActiveVisit())
        //    return "-1";
        
        Patient patient = PatientContext.getActivePatient();
        Encounter encounter = EncounterContext.getActiveEncounter();
        IUser user = UserContext.getActiveUser();
        Practitioner activePractitioner = (Practitioner) EncounterParticipantContext.getActivePractitioner().getIndividual()
                .getResource();
        String practitionerId = activePractitioner == null ? null : activePractitioner.getIdElement().getIdPart();
        // VPOV IEN [1] ^ Visit IEN [2] ^ ICD Code IEN [3] ^ Patient IEN [4] ^ Narrative [5] ^
        // Stage [6] ^ Modifier [7] ^ Cause Dx [8] ^ First/Revisit [9] ^ Injury E-Code [10] ^
        // Injury Place [11] ^ Primary/Secondary [12] ^ Injury Date [13] ^ Onset Date [14] ^
        // Provider IEN [15]
        String s = VistAUtil.concatParams(null, encounter.getIdElement().getIdPart(), "`" + icdIEN,
            patient.getIdElement().getIdPart(), narrative, null, null, null, null, null, null, null, null, onset,
            practitionerId);
        s = VistAUtil.getBrokerSession().callRPC("BGOVPOV SET", s);
        
        if (BgoUtil.errorCode(s) == 0) {
            s = addProvider(user.getLogicalId(), true, false);
        }
        
        if (BgoUtil.errorCode(s) == 1098) {
            if (PromptDialog.confirm(
                StrUtil.piece(s, StrUtil.U, 2) + StrUtil.CRLF + "Are you the primary provider for this visit?",
                "Change Primary Provider?")) {
                s = addProvider(user.getLogicalId(), true, true);
            } else {
                s = null;
            }
        }
        return s;
    }
    
    public static boolean deletePOV(String visitIEN, String povIEN, String icdIEN) {
        if (!PromptDialog.confirm("Are you sure you want to delete the purpose of visit?", "Remove POV?")) {
            return false;
        }
        
        String s = VistAUtil.getBrokerSession().callRPC("BGOVPOV DEL", povIEN);
        
        if (errorCheck(s)) {
            return false;
        }
        
        deletePatientEd(visitIEN, icdIEN);
        return true;
    }
    
    private static void deletePatientEd(String visitIEN, String icdIEN) {
        Patient patient = PatientContext.getActivePatient();
        IUser user = UserContext.getActiveUser();
        BrokerSession broker = VistAUtil.getBrokerSession();
        String s = VistAUtil.concatParams(patient.getIdElement().getIdPart(), visitIEN);
        List<String> v = broker.callRPCList("BGOVPED GET", null, s);
        
        if (errorCheck(v)) {
            return;
        }
        
        String priIEN = StrUtil.piece(broker.callRPC("BGOVPRV PRIPRV", visitIEN), StrUtil.U);
        String prvIEN = user.getLogicalId();
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
        
        if (!PromptDialog.confirm(
            "Do you also want to delete the following related education event" + (v.size() == 1 ? "?" : "s?") + s,
            "Remove Education Events?")) {
            return;
        }
        
        for (String val : v) {
            String sRpc = broker.callRPC("BGOVPED DEL", StrUtil.piece(val, StrUtil.U, 14));
            errorCheck(sRpc);
        }
    }
    
    public static String addProblem(String sICDIEN, String narrative, String onset) {
        BrokerSession broker = VistAUtil.getBrokerSession();
        String institution = UserContext.getActiveUser().getSecurityDomain().getLogicalId();
        Patient patient = PatientContext.getActivePatient();
        
        if (onset == null) {
            onset = StrUtil.piece(broker.getHostTime().toString(), " ");
        }
        
        // ICD IEN or Code [1] ^ Narrative [2] ^ Location IEN [3] ^ Date of Onset [4] ^ Class [5] ^
        // Status [6] ^ Patient IEN [7] ^ Problem IEN [8] ^ Problem # [9]
        String s = VistAUtil.concatParams(sICDIEN, narrative, institution, onset, null, "A",
            patient.getIdElement().getIdPart(), null, null);
        return broker.callRPC("BGOPROB SET", s);
    }
    
    public static int compareProblemIDs(String id1, String id2) {
        Identifier ed1 = PCCUtil.parseProblemID(id1);
        Identifier ed2 = PCCUtil.parseProblemID(id2);
        int i = ed1.getType().getCodingFirstRep().getCode().compareToIgnoreCase(ed2.getType().getCodingFirstRep().getCode());
        
        if (i == 0) {
            int v1 = NumberUtils.toInt(ed1.getValue());
            int v2 = NumberUtils.toInt(ed2.getValue());
            i = v1 == v2 ? 0 : v1 > v2 ? 1 : -1;
        }
        
        return i;
    }
    
    /**
     * Load CPT modifiers for the specified code and reference date.
     *
     * @param cbo The combo box.
     * @param cptCode The CPT4 code.
     * @param refDate The reference date.
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
    
    private PCC() {
    }
    
}

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
package org.carewebframework.rpms.plugin.skintest.model;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.domain.Refusal;
import org.carewebframework.rpms.api.domain.SkinTest;
import org.carewebframework.rpms.plugin.skintest.controller.SkinTestController.EventType;
import org.carewebframework.rpms.ui.common.BgoConstants;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.encounter.EncounterUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.fhir.common.FhirUtil;

public class TestItem {
    
    protected SkinTest skinTest;
    
    protected Refusal refusal;
    
    public TestItem(String value) {
        if (value.startsWith("S")) {
            skinTest = new SkinTest(value);
        } else {
            refusal = new Refusal(value);
        }
    }
    
    public boolean isLocked() {
        if (refusal != null) {
            return refusal.isLocked();
        }
        
        return skinTest.getEncounter() == null ? true : EncounterUtil.isLocked(skinTest.getEncounter());
    }
    
    public boolean isPending() {
        return skinTest == null ? false : "pending".equalsIgnoreCase(skinTest.getResult());
    }
    
    public Encounter getEncounter() {
        return skinTest != null ? skinTest.getEncounter() : null;
    }
    
    public FMDate getDate() {
        return skinTest != null ? skinTest.getEventDate() : refusal.getDate();
    }
    
    public String getTestName() {
        return skinTest != null ? skinTest.getTest().getProxiedObject().getDisplay()
                : refusal.getItem().getProxiedObject().getDisplay();
    }
    
    public String getLocationName() {
        return skinTest != null ? skinTest.getLocation().getName() : null;
    }
    
    public String getAge() {
        return skinTest != null ? skinTest.getAge() : null;
    }
    
    public String getResult() {
        return skinTest != null ? skinTest.getResult() : refusal.getReason();
    }
    
    public String getReading() {
        return skinTest != null ? skinTest.getReading() : null;
    }
    
    public FMDate getReadDate() {
        return skinTest != null ? skinTest.getReadDate() : null;
    }
    
    public Practitioner getProvider() {
        return skinTest != null ? skinTest.getProvider() : refusal.getProvider();
    }
    
    public String getProviderName() {
        Practitioner prv = getProvider();
        return prv == null ? "" : FhirUtil.formatName(prv.getName());
    }
    
    public Practitioner getReader() {
        return skinTest != null ? skinTest.getReader() : null;
    }
    
    public EventType getEventType() {
        return refusal != null ? EventType.REFUSAL
                : getEncounter() == null || "E".equals(EncounterUtil.getServiceCategory(getEncounter()))
                        ? EventType.HISTORICAL : EventType.CURRENT;
    }
    
    public void delete() {
        Practitioner provider = getProvider();
        IUser user = UserContext.getActiveUser();
        
        if (skinTest != null && provider != null && !user.equals(provider)) {
            String s = VistAUtil.getBrokerSession().callRPC("BGOVPRV PRIPRV",
                skinTest.getEncounter().getIdElement().getIdPart());
            String[] pcs = StrUtil.split(s, StrUtil.U, 2);
            
            if (!user.getLogicalId().equals(pcs[0])) {
                PromptDialog.showError("To delete the skin test, you must either be the person that entered it or be "
                        + "designated as the primary provider for the visit.\n" + BgoConstants.TC_PRI_PRV + pcs[1]
                        + "\nAdministered By: " + provider.getName(),
                    "Cannot Delete");
                return;
            }
        }
        
        if (PromptDialog.confirm("Are you sure that you wish to delete the skin test:\n" + getTestName(),
            "Delete Skin Test?")) {
            PCC.errorCheck(VistAUtil.getBrokerSession().callRPC("BGOSK DEL",
                VistAUtil.concatParams(skinTest != null ? skinTest.getId().getIdPart() : null,
                    refusal != null ? refusal.getId().getIdPart() : null)));
        }
        
    }
    
}

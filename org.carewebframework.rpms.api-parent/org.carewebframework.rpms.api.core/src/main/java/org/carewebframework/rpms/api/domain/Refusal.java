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
package org.carewebframework.rpms.api.domain;

import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.api.DomainObject;
import org.hspconsortium.cwf.fhir.common.FhirUtil;

public class Refusal extends DomainObject {

    static {
        JSONUtil.registerAlias("Refusal", Refusal.class);
    }

    private FMDate date;

    private CodingProxy type;

    private CodingProxy item;

    private Practitioner provider;

    private String reason;

    private String comment;

    private boolean locked;

    public Refusal() {
        super();
    }

    /**
     * Temporary constructor to create a problem from serialized form (will move to json).
     *
     * @param value R ^ Refusal IEN [2] ^ Type IEN [3] ^ Type Name [4] ^ Item IEN [5] ^ Item Name
     *            [6] ^ Provider IEN [7] ^ Provider Name [8] ^ Date [9] ^ Locked [10] ^ Reason [11]
     *            ^ Comment [12] e.g., R^2^8^SKIN TEST^3^COCCI^^^10/31/2006^1^REFUSED SERVICE^
     */
    public Refusal(String value) {
        String[] pcs = StrUtil.split(value, StrUtil.U, 12);
        setId(pcs[1]);
        type = parseConcept("REFUSAL TYPE", pcs[2], pcs[3]);
        item = parseConcept("REFUSAL ITEM", pcs[4], pcs[5]);
        provider = parseProvider(pcs[6], pcs[7]);
        date = PCCUtil.parseDate(pcs[8]);
        locked = "1".equals(pcs[9]);
        reason = pcs[10];
        comment = pcs[11];
    }

    private CodingProxy parseConcept(String sysId, String ien, String code) {
        if (!VistAUtil.validateIEN(ien)) {
            return null;
        }

        return new CodingProxy(ien, sysId, code, code);
    }

    private Practitioner parseProvider(String ien, String name) {
        if (!VistAUtil.validateIEN(ien)) {
            return null;
        }

        Practitioner provider = new Practitioner();
        provider.setId(ien);
        provider.addName(FhirUtil.parseName(name));
        return provider;
    }

    public FMDate getDate() {
        return date;
    }

    public void setDate(FMDate date) {
        this.date = date;
    }

    public CodingProxy getType() {
        return type;
    }

    public void setType(CodingProxy type) {
        this.type = type;
    }

    public CodingProxy getItem() {
        return item;
    }

    public void setItem(CodingProxy item) {
        this.item = item;
    }

    public Practitioner getProvider() {
        return provider;
    }

    public void setProvider(Practitioner provider) {
        this.provider = provider;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

}

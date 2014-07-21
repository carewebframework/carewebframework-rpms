/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.domain;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.model.resource.Practitioner;
import org.carewebframework.fhir.model.type.Coding;
import org.carewebframework.fhir.model.type.HumanName;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.FMDate;

public class Refusal extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Refusal", Refusal.class);
    }
    
    private FMDate date;
    
    private Coding type;
    
    private Coding item;
    
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
        setDomainId(pcs[1]);
        type = parseConcept("REFUSAL TYPE", pcs[2], pcs[3]);
        item = parseConcept("REFUSAL ITEM", pcs[4], pcs[5]);
        provider = parseProvider(pcs[6], pcs[7]);
        date = parseDate(pcs[8]);
        locked = "1".equals(pcs[9]);
        reason = pcs[10];
        comment = pcs[11];
    }
    
    private Coding parseConcept(String sysId, String ien, String code) {
        if (!VistAUtil.validateIEN(ien)) {
            return null;
        }
        
        Coding concept = new Coding();
        concept.setSystemSimple(sysId);
        concept.setDomainId(ien);
        concept.setCodeSimple(code);
        concept.setDisplaySimple(code);
        return concept;
        
    }
    
    private FMDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        return new FMDate(DateUtil.parseDate(value));
    }
    
    private Practitioner parseProvider(String ien, String name) {
        if (!VistAUtil.validateIEN(ien)) {
            return null;
        }
        
        Practitioner provider = new Practitioner();
        provider.setDomainId(ien);
        provider.setName(new HumanName(name));
        return provider;
    }
    
    public FMDate getDate() {
        return date;
    }
    
    public void setDate(FMDate date) {
        this.date = date;
    }
    
    public Coding getType() {
        return type;
    }
    
    public void setType(Coding type) {
        this.type = type;
    }
    
    public Coding getItem() {
        return item;
    }
    
    public void setItem(Coding item) {
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

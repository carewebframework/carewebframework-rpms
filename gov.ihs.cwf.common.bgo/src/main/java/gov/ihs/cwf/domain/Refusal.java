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

import gov.ihs.cwf.mbroker.FMDate;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class Refusal extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Refusal", Refusal.class);
    }
    
    private FMDate date;
    
    private Concept type;
    
    private Concept item;
    
    private Provider provider;
    
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
        setDomainId(Long.parseLong(pcs[1]));
        type = parseConcept("REFUSAL TYPE", pcs[2], pcs[3]);
        item = parseConcept("REFUSAL ITEM", pcs[4], pcs[5]);
        provider = parseProvider(pcs[6], pcs[7]);
        date = parseDate(pcs[8]);
        locked = "1".equals(pcs[9]);
        reason = pcs[10];
        comment = pcs[11];
    }
    
    private Concept parseConcept(String sysId, String ien, String code) {
        long id = NumberUtils.toLong(ien);
        
        if (id <= 0) {
            return null;
        }
        
        Concept concept = new Concept(sysId);
        concept.setDomainId(id);
        concept.setCode(code);
        concept.setShortDescription(code);
        return concept;
        
    }
    
    private FMDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        return new FMDate(DateUtil.parseDate(value));
    }
    
    private Provider parseProvider(String ien, String name) {
        long id = NumberUtils.toLong(ien);
        
        if (id <= 0) {
            return null;
        }
        
        Provider provider = new Provider();
        provider.setDomainId(id);
        provider.setFullName(name);
        return provider;
    }
    
    public FMDate getDate() {
        return date;
    }
    
    public void setDate(FMDate date) {
        this.date = date;
    }
    
    public Concept getType() {
        return type;
    }
    
    public void setType(Concept type) {
        this.type = type;
    }
    
    public Concept getItem() {
        return item;
    }
    
    public void setItem(Concept item) {
        this.item = item;
    }
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
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

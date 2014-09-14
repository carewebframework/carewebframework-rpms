/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.domain;

import org.carewebframework.cal.api.domain.DomainObject;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.FMDate;

public class Contraindication extends DomainObject {
    
    static {
        JSONUtil.registerAlias("Contraindication", Contraindication.class);
    }
    
    private String immunization;
    
    private FMDate date;
    
    private String reason;
    
    public Contraindication() {
        super();
    }
    
    /**
     * Temporary constructor to create a contraindication from serialized form (will move to json).
     *
     * @param value C ^ Contra IEN [2] ^ Imm Name [3] ^ Reason [4] ^ Date [5] e.g.,
     *            C^68^PNEUMO-PS^Fever>104F^10-Dec-2006
     */
    public Contraindication(String value) {
        String[] pcs = StrUtil.split(value, StrUtil.U, 5);
        setId(pcs[1]);
        immunization = pcs[2];
        reason = pcs[3];
        date = parseDate(pcs[4]);
    }
    
    private FMDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        return new FMDate(DateUtil.parseDate(value));
    }
    
    public FMDate getDate() {
        return date;
    }
    
    public void setDate(FMDate date) {
        this.date = date;
    }
    
    public String getImmunization() {
        return immunization;
    }
    
    public void setImmunization(String immunization) {
        this.immunization = immunization;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
}

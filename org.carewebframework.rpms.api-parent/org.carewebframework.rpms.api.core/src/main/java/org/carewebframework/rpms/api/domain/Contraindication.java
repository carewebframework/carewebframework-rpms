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
import org.carewebframework.vista.mbroker.FMDate;
import org.hspconsortium.cwf.api.DomainObject;

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
        date = PCCUtil.parseDate(pcs[4]);
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

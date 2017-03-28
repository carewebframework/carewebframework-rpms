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
import org.hspconsortium.cwf.api.DomainObject;

public class Forecast extends DomainObject {

    static {
        JSONUtil.registerAlias("Forecast", Forecast.class);
    }

    private String immunization;

    private String status;

    public Forecast() {
        super();
    }

    /**
     * Temporary constructor to create an immunization forecast from serialized form (will move to
     * json).
     *
     * @param value F ^ Imm Name [2] ^ Status [3] e.g., F^Invalid ImmServe Path; edit Site
     *            Parameter. (Go MGR-->ESP-->15) #118
     */
    public Forecast(String value) {
        String[] pcs = StrUtil.split(value, StrUtil.U, 3);
        setId(Integer.toString(hashCode()));
        immunization = pcs[1];
        status = pcs[2];
    }

    public String getImmunization() {
        return immunization;
    }

    public void setImmunization(String immunization) {
        this.immunization = immunization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

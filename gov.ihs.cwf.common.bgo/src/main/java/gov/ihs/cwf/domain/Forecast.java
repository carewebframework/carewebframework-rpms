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
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class Forecast extends DomainObject {

    private static final long serialVersionUID = 1L;

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
        setDomainId(Integer.toString(hashCode()));
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

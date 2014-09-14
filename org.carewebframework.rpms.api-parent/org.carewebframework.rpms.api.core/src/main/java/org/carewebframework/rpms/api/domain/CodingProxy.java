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

import ca.uhn.fhir.model.dstu.composite.CodingDt;

import org.carewebframework.cal.api.domain.DomainObjectProxy;

/**
 * Proxy for FHIR Coding data type.
 */
public class CodingProxy extends DomainObjectProxy<CodingDt> {
    
    public CodingProxy(String logicalId, String system, String code, String display) {
        super(logicalId, new CodingDt());
        CodingDt coding = getProxiedObject();
        coding.setSystem(system);
        coding.setCode(code);
        coding.setDisplay(display);
    }
    
    public CodingProxy(CodingProxy src) {
        super(src);
    }
    
}

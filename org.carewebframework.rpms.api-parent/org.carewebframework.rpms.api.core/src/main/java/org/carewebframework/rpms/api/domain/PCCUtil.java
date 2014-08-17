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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.model.resource.Location;
import org.carewebframework.fhir.model.resource.Practitioner;
import org.carewebframework.fhir.model.type.HumanName;
import org.carewebframework.fhir.model.type.Identifier;
import org.carewebframework.vista.mbroker.FMDate;

public class PCCUtil {
    
    /**
     * Parse a problem ID into an entity identifier.
     *
     * @param value One of: [prefix]-[id] or [id]
     * @return An entity identifier.
     */
    public static Identifier parseProblemID(String value) {
        String s = StrUtil.piece(value, "-", 2);
        String id = "";
        String prefix = "";
        
        if (s.isEmpty()) {
            id = value;
        } else {
            id = s;
            prefix = StrUtil.piece(value, "-");
        }
        Identifier ident = new Identifier();
        ident.setValueSimple(id);
        ident.setLabelSimple(prefix);
        return ident;
    }
    
    public static Location parseLocation(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }
        
        return new Location();
    }
    
    public static FMDate parseDate(String value) {
        return value == null || value.isEmpty() ? null : StringUtils.isNumeric(value.replace(".", "")) ? new FMDate(value)
        : new FMDate(DateUtil.parseDate(value));
    }
    
    public static Practitioner parsePractitioner(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }
        
        String[] pcs = StrUtil.split(value, "~", 2);
        Practitioner practitioner = new Practitioner();
        practitioner.setLogicalId(pcs[0]);
        practitioner.setName(new HumanName(pcs[1]));
        return practitioner;
    }
    
    private PCCUtil() {
    };
}

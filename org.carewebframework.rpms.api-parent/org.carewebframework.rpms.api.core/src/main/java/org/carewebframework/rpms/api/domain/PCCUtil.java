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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.fhir.common.FhirUtil;

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
        ident.setValue(id);
        //TODO: FhirUtil.setIdentifierType(ident, null, prefix);
        return ident;
    }

    public static Location parseLocation(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }

        return new Location();
    }

    public static FMDate parseDate(String value) {
        return value == null || value.isEmpty() ? null
                : StringUtils.isNumeric(value.replace(".", "")) ? new FMDate(value) : parseDate2(value);
    }

    private static FMDate parseDate2(String value) {
        Date date = DateUtil.parseDate(value);
        return date == null ? null : new FMDate(date);
    }

    public static Practitioner parsePractitioner(String value) {
        if (value == null || value.isEmpty() || "~".equals(value)) {
            return null;
        }

        String[] pcs = StrUtil.split(value, "~", 2);
        Practitioner practitioner = new Practitioner();
        practitioner.setId(pcs[0]);
        practitioner.addName(FhirUtil.parseName(pcs[1]));
        return practitioner;
    }

    private PCCUtil() {
    };
}

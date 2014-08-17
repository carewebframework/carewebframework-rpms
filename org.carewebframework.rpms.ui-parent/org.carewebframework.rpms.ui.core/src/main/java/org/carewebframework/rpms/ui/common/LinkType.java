/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.common;

import org.carewebframework.fhir.model.type.Coding;

public enum LinkType {
    CPT4, CPT4_RANGE, ICD9, ICD9_RANGE, EDU, EXAM, IMM, SKIN;
    
    public static LinkType fromConcept(Coding concept) {
        try {
            return LinkType.valueOf(concept.getSystemSimple());
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isRange() {
        return this == CPT4_RANGE || this == ICD9_RANGE;
    }
}

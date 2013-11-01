/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.patientlist;

import org.carewebframework.cal.api.patientlist.PatientListUtil;
import org.carewebframework.common.StrUtil;

public class BEHPatientListFilterEntity {
    
    private final String id;
    
    private final String name;
    
    protected BEHPatientListFilterEntity(String value) {
        String[] pcs = PatientListUtil.split(value, 3);
        id = pcs[0];
        name = pcs[1];
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return id + StrUtil.U + name;
    }
}

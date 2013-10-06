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

import org.apache.commons.lang.ObjectUtils;

/**
 * Provides serialization support for a list.
 */
public class Favorite {
    
    private String name;
    
    private final IPatientList patientList;
    
    public Favorite(String data) {
        String[] pcs = Util.split(data, 2);
        this.name = pcs[0];
        this.patientList = Util.deserializePatientList(pcs[1]);
    }
    
    public Favorite(IPatientList list) {
        this.patientList = list.copy();
        this.name = list.getDisplayName();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        Util.append(sb, patientList.serialize());
        return sb.toString();
    }
    
    public String getName() {
        return name;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    public IPatientList getPatientList() {
        return patientList;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Favorite)) {
            return false;
        }
        
        Favorite favorite = (Favorite) object;
        return ObjectUtils.equals(this.name, favorite.name) && ObjectUtils.equals(this.patientList, favorite.patientList);
    }
    
}

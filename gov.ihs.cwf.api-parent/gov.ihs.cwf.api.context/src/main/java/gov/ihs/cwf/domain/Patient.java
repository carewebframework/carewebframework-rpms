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

import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.cal.api.domain.IPatient;
import org.carewebframework.common.JSONUtil;

public class Patient extends Person implements IPatient {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Patient", Patient.class);
    }
    
    private EntityIdentifier mrn;
    
    private String bed;
    
    private boolean restricted;
    
    private Location location;
    
    protected Patient() {
        
    }
    
    public Patient(long id) {
        super(id);
    }
    
    @Override
    public EntityIdentifier getMRN() {
        return mrn;
    }
    
    public void setMedicalRecordNumber(String medicalRecordNumber) {
        mrn = new EntityIdentifier(medicalRecordNumber, "MRN");
    }
    
    public String getMedicalRecordNumber() {
        return mrn.getId();
    }
    
    public void setBed(String bed) {
        this.bed = bed;
    }
    
    public String getBed() {
        return bed;
    }
    
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
    
    @Override
    public boolean isRestricted() {
        return restricted;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Location getLocation() {
        return location;
    }
    
}

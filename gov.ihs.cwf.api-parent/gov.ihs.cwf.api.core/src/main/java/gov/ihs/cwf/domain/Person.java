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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.api.domain.IPerson;
import org.carewebframework.cal.api.domain.Name;

/**
 * Person domain class.
 */
public class Person extends DomainObject implements IPerson {
    
    private static final long serialVersionUID = 1L;
    
    private Institution institution;
    
    private String gender;
    
    private Date birthDate;
    
    private Date deathDate;
    
    private String fullName;
    
    private Name name;
    
    private final Map<String, EntityIdentifier> identifiers = new HashMap<String, EntityIdentifier>();
    
    protected Person() {
        
    }
    
    protected Person(long id) {
        super(id);
    }
    
    @Override
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
        name = null;
    }
    
    public Name getName() {
        if (name == null && fullName != null) {
            name = new Name(fullName);
        }
        return name;
    }
    
    public void setName(Name name) {
        this.name = name;
        fullName = name == null ? null : name.getFullName();
    }
    
    @Override
    public Institution getInstitution() {
        return institution;
    }
    
    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    @Override
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    @Override
    public Date getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    @Override
    public Date getDeathDate() {
        return deathDate;
    }
    
    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }
    
    @Override
    public EntityIdentifier getIdentifier(String sysId) {
        return identifiers.get(sysId);
    }
    
    public void setIdentifier(EntityIdentifier identifier) {
        identifiers.put(identifier.getSysId(), identifier);
    }
    
    public Map<String, EntityIdentifier> getIdentifiers() {
        return identifiers;
    }
    
    @Override
    public String toString() {
        return getFullName().toString();
    }
    
}

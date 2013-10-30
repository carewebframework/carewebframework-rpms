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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.api.domain.IPerson;

/**
 * Person domain class.
 */
public class Person extends DomainObject implements IPerson {
    
    private static final long serialVersionUID = 1L;
    
    public static class Name implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private String firstName = "";
        
        private String lastName = "";
        
        private String middleName = "";
        
        public Name() {
            
        }
        
        public Name(String value) {
            value = value == null ? "" : value.trim();
            String[] pcs = value.split("\\,", 2);
            lastName = pcs[0].trim();
            pcs = pcs.length == 1 ? null : pcs[1].trim().split("\\s", 2);
            firstName = pcs == null ? "" : pcs[0].trim();
            middleName = pcs == null ? "" : pcs.length == 1 ? "" : pcs[1].trim();
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getMiddleName() {
            return middleName;
        }
        
        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }
        
        public String getFullName() {
            return (lastName + ", " + firstName + " " + middleName).trim();
        }
    }
    
    private Name name;
    
    private Institution institution;
    
    private String gender;
    
    private Date birthDate;
    
    private Date deathDate;
    
    private final Map<String, EntityIdentifier> identifiers = new HashMap<String, EntityIdentifier>();
    
    protected Person() {
        
    }
    
    protected Person(long id) {
        super(id);
    }
    
    public Name getName() {
        return name;
    }
    
    public void setName(Name name) {
        this.name = name;
    }
    
    @Override
    public String getFullName() {
        return name.getFullName();
    }
    
    public void setFullName(String name) {
        this.name = new Name(name);
    }
    
    @Override
    public Institution getInstitution() {
        return institution;
    }
    
    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    @Override
    public String getGender() {
        return gender;
    }
    
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    @Override
    public Date getBirthDate() {
        return birthDate;
    }
    
    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }
    
    @Override
    public Date getDeathDate() {
        return deathDate;
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
    
    /**
     * Returns Person age as a formatted string expressed in days, months, or years, depending on
     * whether person is an infant (< 2 mos), toddler (> 2 mos, < 2 yrs), or more than 2 years old
     * 
     * @return the age display string
     */
    public String getAgeForDisplay() {
        return getAgeForDisplay(Calendar.getInstance()); // today
    }
    
    public String getAgeForDisplay(Calendar asOf) {
        final String unknown = "???";
        final Date birthDate = getBirthDate();
        
        if (birthDate == null) {
            return unknown;
        }
        
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        long birthDateInDays = (asOf.getTimeInMillis() - birth.getTimeInMillis()) / 1000 / 60 / 60 / 24;
        
        if (birthDateInDays < 0) {
            return unknown;
        }
        
        if (birthDateInDays <= 1) {
            return "newborn";
        }
        
        if (birthDateInDays <= 60) {
            return Long.toString(birthDateInDays) + " days";
        }
        
        int birthDateYear = birth.get(Calendar.YEAR);
        int birthDateMonth = birth.get(Calendar.MONTH) + 1;
        int birthDateDay = birth.get(Calendar.DATE);
        int todayYear = asOf.get(Calendar.YEAR);
        int todayMonth = asOf.get(Calendar.MONTH) + 1;
        int todayDay = asOf.get(Calendar.DATE);
        
        if (birthDateInDays <= 730) {
            // if person is more than 2 months but less than 2 years
            // then display age in months
            if ((todayMonth >= birthDateMonth) && (todayDay >= birthDateDay)) {
                // if person has had a birthday already this year
                return Integer.toString(((todayYear - birthDateYear) * 12) + (todayMonth - birthDateMonth)) + " mos";
            }
            // then age in months = # years old * 12 + months so far this year
            return Integer.toString(((todayYear - birthDateYear) * 12) + (todayMonth - birthDateMonth - 1)) + " mos";
            // if person has not yet had a birthday this year, subtract 1 month
        } else {
            // if person is more than 2 years old
            // then display age in years
            if ((todayMonth > birthDateMonth) || (todayMonth == birthDateMonth && todayDay >= birthDateDay)) {
                // if person has had a birthday already this year
                return Integer.toString(todayYear - birthDateYear) + " yrs";
            }
            return Integer.toString((todayYear - birthDateYear) - 1) + " yrs";
            // if person has not yet had a birthday this year, subtract 1
        }
    }
    
    @Override
    public String toString() {
        return getFullName();
    }
    
}

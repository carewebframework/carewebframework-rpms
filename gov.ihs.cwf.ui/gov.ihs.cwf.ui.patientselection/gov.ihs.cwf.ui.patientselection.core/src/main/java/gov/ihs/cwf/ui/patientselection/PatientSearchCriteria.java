/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.DateUtil;
import gov.ihs.cwf.domain.Person.Name;

/**
 * Represents search criteria supported by core's candidate finder API.
 */
public class PatientSearchCriteria {
    
    private static final String WARN_MIN_CHARACTERS = "patientsearch.warn.min.characters";
    
    private static final String ERROR_MRN_REQUIRED = "patientsearch.error.mrn.required";
    
    private Name name;
    
    private String mrn;
    
    private String ssn;
    
    private String gender;
    
    private Date birth;
    
    private String id;
    
    public PatientSearchCriteria() {
        
    }
    
    /**
     * Creates a criteria instance with settings parsed from search text.
     * 
     * @param searchText Search text to parse. Uses pattern matching to determine which criterion is
     *            associated with a given input component. Separate multiple input components with a
     *            semicolons.
     */
    public PatientSearchCriteria(String searchText) {
        searchText = searchText == null ? null : searchText.trim();
        
        if (!StringUtils.isEmpty(searchText)) {
            String[] pcs = searchText.split(";");
            
            for (String pc : pcs) {
                pc = pc.trim();
                Date tempDate;
                
                if (pc.isEmpty())
                    continue;
                else if (pc.startsWith("`"))
                    id = pc;
                else if (pc.equalsIgnoreCase("M") || pc.equalsIgnoreCase("F"))
                    gender = pc;
                else if (pc.length() < 3)
                    throw new PatientSearchException(WARN_MIN_CHARACTERS);
                else if (!hasNumeric(pc))
                    name = new Name(pc);
                else if (pc.matches("^\\d{3}-\\d{2}-\\d{4}$"))
                    ssn = pc;
                else if (!StringUtils.isNumeric(pc) && (tempDate = DateUtil.parseDate(pc)) != null)
                    birth = tempDate;
                else
                    mrn = pc;
            }
        }
    }
    
    private boolean hasNumeric(String value) {
        for (int i = 0; i < value.length(); i++)
            if (Character.isDigit(value.charAt(i)))
                return true;
        
        return false;
    }
    
    /**
     * Validates that the current criteria settings meet the minimum requirements for a search. If
     * not, throws a run-time exception describing the deficiency.
     */
    public void validate() {
        if (StringUtils.isEmpty(ssn) && StringUtils.isEmpty(mrn) && name == null)
            throw new PatientSearchException(ERROR_MRN_REQUIRED);
        
        if (name != null && name.getFullName().length() < 3)
            throw new PatientSearchException(WARN_MIN_CHARACTERS);
    }
    
    /**
     * Returns the patient name criterion.
     * 
     * @return Patient name criterion.
     */
    public Name getName() {
        return name;
    }
    
    /**
     * Sets the patient name criterion.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = StringUtils.isEmpty(name) ? null : new Name(name);
    }
    
    /**
     * Sets the patient name criterion.
     * 
     * @param name
     */
    public void setName(Name name) {
        this.name = name;
    }
    
    /**
     * Returns the MRN criterion.
     * 
     * @return MRN criterion.
     */
    public String getMRN() {
        return mrn;
    }
    
    /**
     * Sets the MRN criterion.
     * 
     * @param mrn
     */
    public void setMRN(String mrn) {
        this.mrn = mrn;
    }
    
    /**
     * Returns the SSN criterion.
     * 
     * @return SSN criterion.
     */
    public String getSSN() {
        return ssn;
    }
    
    /**
     * Sets the SSN criterion.
     * 
     * @param ssn
     */
    public void setSSN(String ssn) {
        this.ssn = ssn;
    }
    
    /**
     * Sets the internal identifier for the patient.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the internal identifier for the patient.
     * 
     * @return Internal identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the gender criterion.
     * 
     * @return Gender criterion.
     */
    public String getGender() {
        return gender;
    }
    
    /**
     * Sets the gender criterion.
     * 
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    /**
     * Returns the date of birth criterion.
     * 
     * @return DOB criterion.
     */
    public Date getBirth() {
        return birth;
    }
    
    /**
     * Sets the date of birth criterion.
     * 
     * @param birth
     */
    public void setBirth(Date birth) {
        this.birth = birth;
    }
    
    /**
     * Returns true if no criteria have been set.
     * 
     * @return True if no criteria have been set.
     */
    public boolean isEmpty() {
        return name == null && StringUtils.isEmpty(mrn) && StringUtils.isEmpty(ssn) && StringUtils.isEmpty(gender)
                && birth == null;
    }
}

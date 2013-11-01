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

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.JSONUtil;

public class Location extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Location", Location.class);
    }
    
    private String name;
    
    private String abbreviation;
    
    private Institution institution;
    
    public Location() {
        
    }
    
    public Location(String name, String abbreviation, Institution institution) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.institution = institution;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    protected void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }
    
    protected void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    public Institution getInstitution() {
        return institution;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}

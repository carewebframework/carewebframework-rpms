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
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.common.JSONUtil;

/**
 * Institution domain class.
 */
public class Institution extends DomainObject implements IInstitution {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Institution", Institution.class);
    }
    
    protected Institution() {
        
    }
    
    public Institution(long id) {
        super(id);
    }
    
    private String name;
    
    private String abbreviation;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getAbbreviation() {
        return abbreviation == null || abbreviation.isEmpty() ? name : abbreviation;
    }
    
    @Override
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
}

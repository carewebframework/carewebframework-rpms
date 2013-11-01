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

/**
 * Service category
 * 
 * 
 */
public class ServiceCategory extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("ServiceCategory", ServiceCategory.class);
    }
    
    private String shortDescription;
    
    private String longDescription;
    
    public ServiceCategory() {
        
    }
    
    public ServiceCategory(char id, String shortDescription, String longDescription) {
        setDomainId(id);
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public String getLongDescription() {
        return longDescription;
    }
    
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
    
    @Override
    public String toString() {
        return this.shortDescription;
    }
}

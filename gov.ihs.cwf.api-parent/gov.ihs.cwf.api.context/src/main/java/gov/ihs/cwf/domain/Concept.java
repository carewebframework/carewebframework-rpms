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

/**
 * Abstract base class for concept codes.
 * 
 * 
 */
public class Concept extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    private final String codeSystem;
    
    private String code;
    
    private String shortDescription;
    
    private String longDescription;
    
    public Concept(String codeSystem) {
        this.codeSystem = codeSystem;
    }
    
    public String getCodeSystem() {
        return codeSystem;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getShortDescription() {
        return shortDescription == null ? longDescription : shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public String getLongDescription() {
        return longDescription == null ? shortDescription : longDescription;
    }
    
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
}

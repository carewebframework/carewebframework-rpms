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

import org.carewebframework.common.JSONUtil;

/**
 * Represents a single ICD9 code.
 * 
 * @author dmartin
 */
public class ICD9Concept extends Concept {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("ICD9", ICD9Concept.class);
    }
    
    public ICD9Concept() {
        super("ICD9");
    }
}

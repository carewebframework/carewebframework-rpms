/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.security.testharness;

import java.util.List;

import gov.ihs.cwf.security.base.BaseAuthenticationProvider;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 * 
 * @author dmartin
 */
public final class AuthenticationProvider extends BaseAuthenticationProvider {
    
    public AuthenticationProvider() {
        super(true);
    }
    
    public AuthenticationProvider(List<String> grantedAuthorities) {
        super(grantedAuthorities);
    }
}

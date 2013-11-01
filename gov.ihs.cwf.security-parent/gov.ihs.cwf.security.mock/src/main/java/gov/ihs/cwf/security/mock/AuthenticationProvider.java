/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.security.mock;

import java.util.List;

import gov.ihs.cwf.domain.User;
import gov.ihs.cwf.security.base.BaseAuthenticationProvider;

import org.carewebframework.security.spring.CWFAuthenticationDetails;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 * 
 * 
 */
public final class AuthenticationProvider extends BaseAuthenticationProvider {
    
    public AuthenticationProvider() {
        super(true);
    }
    
    public AuthenticationProvider(List<String> grantedAuthorities) {
        super(grantedAuthorities);
    }
    
    /**
     * Performs a user login.
     * 
     * @param details Authentication details
     * @param username Username for the login.
     * @param password Password for the login (ignored if the user is pre-authenticated).
     * @param domain Domain for which the login is requested.
     * @return Authorization result
     */
    @Override
    protected User login(CWFAuthenticationDetails details, String username, String password, String domain) {
        User user = new User(1);
        user.setFullName("USER,MOCK");
        user.setUsername(username);
        details.setDetail("user", user);
        return user;
    }
    
}

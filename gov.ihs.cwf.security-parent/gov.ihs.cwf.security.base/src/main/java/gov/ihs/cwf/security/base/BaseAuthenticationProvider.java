/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.security.base;

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.domain.DomainObjectFactory;
import gov.ihs.cwf.domain.User;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.Security;
import gov.ihs.cwf.mbroker.Security.AuthResult;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractAuthenticationProvider;
import org.carewebframework.security.spring.AuthenticationCancelledException;
import org.carewebframework.security.spring.CWFAuthenticationDetails;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 */
public class BaseAuthenticationProvider extends AbstractAuthenticationProvider {
    
    public BaseAuthenticationProvider() {
        super(false);
    }
    
    protected BaseAuthenticationProvider(boolean debugRole) {
        super(debugRole);
    }
    
    protected BaseAuthenticationProvider(List<String> grantedAuthorities) {
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
    protected IUser login(CWFAuthenticationDetails details, String username, String password, String domain) {
        BrokerSession brokerSession = RPMSUtil.getBrokerSession();
        List<String> results = new ArrayList<String>();
        AuthResult authResult = Security.authenticate(brokerSession, username, password, domain, results);
        User user = getAuthenticatedUser(brokerSession);
        details.setDetail("user", user);
        checkAuthResult(authResult, StrUtil.piece(results.get(0), StrUtil.U, 2), user);
        return user;
    }
    
    @Override
    protected List<String> getAuthorities(IUser user) {
        return user == null ? null : RPMSUtil.getBrokerSession().callRPCList("CIAVCXUS GETPRIV", null, user.getDomainId());
    }
    
    private User getAuthenticatedUser(BrokerSession brokerSession) {
        try {
            return brokerSession.isAuthenticated() ? DomainObjectFactory.get(User.class, brokerSession.getUserId()) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("deprecation")
    private void checkAuthResult(AuthResult result, String message, User user) throws AuthenticationException {
        switch (result) {
            case SUCCESS:
                return;
                
            case CANCELED:
                throw new AuthenticationCancelledException(StringUtils.defaultIfEmpty(message,
                    "Authentication attempt was cancelled."));
                
            case EXPIRED:
                throw new CredentialsExpiredException(StringUtils.defaultIfEmpty(message, "Your password has expired."),
                        user);
                
            case FAILURE:
                throw new BadCredentialsException(StringUtils.defaultIfEmpty(message,
                    "Your username or password was not recognized."));
                
            case LOCKED:
                throw new LockedException(StringUtils.defaultIfEmpty(message,
                    "Your user account has been locked and cannot be accessed."));
                
            case NOLOGINS:
                throw new AuthenticationServiceException(StringUtils.defaultIfEmpty(message,
                    "Logins are currently disabled."));
        }
    }
    
}

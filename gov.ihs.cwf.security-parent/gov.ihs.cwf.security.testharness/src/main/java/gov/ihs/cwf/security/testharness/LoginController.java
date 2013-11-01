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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

/**
 * Controller for the mock login component. This simulates a user login. Login credentials are
 * obtained from the framework.properties file.
 * 
 * @author dmartin
 */
public class LoginController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private String username;
    
    private String password;
    
    private String domain;
    
    private Textbox j_username;
    
    private Textbox j_password;
    
    private Label lblError;
    
    private Component pnlError;
    
    /**
     * Checks to see if the form was loaded after a failed authentication attempt. If so, an error
     * message is displayed. Otherwise, loads the authentication credentials injected by the IOC
     * container and submits the form.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Session sessionLocal = Sessions.getCurrent();
        AuthenticationException authError = (AuthenticationException) sessionLocal
                .removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        
        if (authError != null) {
            lblError.setValue(authError.toString());
            pnlError.setVisible(true);
        } else {
            j_username.setText(domain + "\\" + username);
            j_password.setText(password);
            Clients.submitForm("loginForm");
        }
    }
    
    /**
     * Return the userName
     * 
     * @return the userName
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set the userName
     * 
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Return the password
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set the password
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Return the domain
     * 
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }
    
    /**
     * Set the domain
     * 
     * @param domain
     */
    public void setAuthority(String domain) {
        this.domain = domain;
    }
    
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.userheader;

import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.context.UserContext.IUserContextEvent;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.FrameworkController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Label;

/**
 * Controller for user header plugin.
 * 
 * @author dmartin
 */
public class UserHeader extends FrameworkController implements IUserContextEvent, IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(UserHeader.class);
    
    private Label userHeader;
    
    private A password;
    
    private static final String DATABASE_DISPLAY_NAME_PROPERTY = "DATABASE.NAME";
    
    private static final String DATABASE_DISPLAY_BACKGROUNDCOLOR_PROPERTY = "DATABASE.BACKGROUNDCOLOR";
    
    private IUser currentUser;
    
    private String dbRegion;
    
    private Component root;
    
    //
    
    /**
     * Event handler for logout link
     */
    public void onClick$logout() {
        CareWebUtil.getShell().logout();
    }
    
    /**
     * Event handler for lock link
     */
    public void onClick$lock() {
        CareWebUtil.getShell().lock();
    }
    
    /**
     * Event handler for change password link
     */
    public void onClick$password() {
        SecurityUtil.getSecurityService().changePassword();
    }
    
    /**
     * @see org.carewebframework.ui.FrameworkController#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
        dbRegion = getPropertyValue(DATABASE_DISPLAY_NAME_PROPERTY);
        committed();
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#canceled()
     */
    @Override
    public void canceled() {
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#committed()
     */
    @Override
    public void committed() {
        IUser user = UserContext.getActiveUser();
        
        if (log.isDebugEnabled()) {
            log.debug("user: " + user);
        }
        
        if (currentUser != null && currentUser.equals(user)) {
            return;
        }
        
        currentUser = user;
        String text = user == null ? "" : user.getFullName() + "@" + RPMSUtil.getCurrentInstitution().getAbbreviation();
        userHeader.setValue(text + (dbRegion.isEmpty() ? "" : " (" + dbRegion + ")"));
        password.setVisible(SecurityUtil.getSecurityService().canChangePassword());
        Clients.resize(root);
    }
    
    /**
     * Returns a property value.
     * 
     * @param propertyName
     * @return
     */
    private String getPropertyValue(final String propertyName) {
        try {
            return PropertyUtil.getValue(propertyName);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#pending(boolean)
     */
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        container.setColor(getPropertyValue(DATABASE_DISPLAY_BACKGROUNDCOLOR_PROPERTY));
    }
    
    @Override
    public void onActivate() {
    }
    
    @Override
    public void onInactivate() {
    }
    
    @Override
    public void onUnload() {
    }
    
}

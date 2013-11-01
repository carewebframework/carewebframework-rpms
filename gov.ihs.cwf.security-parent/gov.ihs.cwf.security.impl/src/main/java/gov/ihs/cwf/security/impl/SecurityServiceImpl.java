/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.security.impl;

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.mbroker.Security;
import gov.ihs.cwf.mbroker.Security.AuthResult;
import gov.ihs.cwf.security.base.BaseSecurityService;
import gov.ihs.cwf.security.base.Constants;
import gov.ihs.cwf.util.RPMSUtil;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.PromptDialog;

import org.zkoss.util.resource.Labels;

/**
 * Security service implementation.
 * 
 * @author dmartin
 * @author afranken
 */
public class SecurityServiceImpl extends BaseSecurityService {
    
    /**
     * Changes the user's password.
     * 
     * @param oldPassword Current password.
     * @param newPassword New password.
     * @return Null or empty if succeeded. Otherwise, displayable reason why change failed.
     */
    @Override
    public String changePassword(final String oldPassword, final String newPassword) {
        return Security.changePassword(RPMSUtil.getBrokerSession(), oldPassword, newPassword);
    }
    
    /**
     * @see org.carewebframework.api.security.ISecurityService#changePassword()
     */
    @Override
    public void changePassword() {
        if (canChangePassword()) {
            ChangePasswordController.show();
        } else {
            PromptDialog.showWarning(Labels.getLabel(Constants.LBL_CHANGE_PASSWORD_UNAVAILABLE));
        }
    }
    
    /**
     * @see org.carewebframework.api.security.ISecurityService#canChangePassword()
     */
    @Override
    public boolean canChangePassword() {
        return true;
    }
    
    /**
     * Return login disabled message.
     */
    @Override
    public String loginDisabled() {
        List<String> results = new ArrayList<String>();
        
        if (AuthResult.NOLOGINS == Security.authenticate(RPMSUtil.getBrokerSession(), "dummy", "dummy", null, results)) {
            return StrUtil.piece(results.get(0), StrUtil.U, 2);
        }
        
        return null;
    }
    
}

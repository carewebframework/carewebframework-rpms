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

import gov.ihs.cwf.domain.User;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbar;

/**
 * Controller for the login component.
 * 
 * 
 */
public class ChangePasswordController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    protected static final String DIALOG_CHANGE_PASSWORD = ZKUtil.getResourcePath(ChangePasswordController.class)
            + "changePassword.zul";
    
    private Panel panel;
    
    private Textbox j_username;
    
    private Textbox j_password;
    
    private Textbox j_password1;
    
    private Textbox j_password2;
    
    private Label lblInfo;
    
    private Label lblMessage;
    
    private Toolbar tbMessage;
    
    private IUser user;
    
    private boolean forced;
    
    private ISecurityService securityService;
    
    private final String MESSAGE_PASSWORD_RULES = Labels.getLabel("change.password.rules.label");
    
    /**
     * Form invokable via direct call.
     */
    public static void show() {
        if (PopupDialog.popup(DIALOG_CHANGE_PASSWORD, false, false) == null) {
            PromptDialog.showError(Labels.getLabel("change.password.dialog.unavailable"));
        }
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        forced = !FrameworkUtil.isInitialized();
        String title;
        String label;
        
        if (!forced) {
            user = UserContext.getActiveUser();
            title = "change.password.dialog.panel.title";
            label = "change.password.dialog.label";
        } else {
            user = (User) arg.get("user");
            title = "change.password.dialog.expired.panel.title";
            label = "change.password.dialog.expired.label";
        }
        
        if (user == null) {
            doCancel();
        } else {
            panel.setTitle(Labels.getLabel(title) + " - " + user.getFullName());
            lblInfo.setValue(Labels.getLabel(label, new String[] { MESSAGE_PASSWORD_RULES }));
        }
    }
    
    /**
     * Pressing return in the current password text box moves to the new password text box.
     */
    public void onOK$j_password() {
        j_password1.setFocus(true);
        j_password1.select();
    }
    
    /**
     * Pressing return in the new password text box moves to the confirm password text box.
     */
    public void onOK$j_password1() {
        j_password2.setFocus(true);
        j_password2.select();
    }
    
    /**
     * Pressing return in confirm password text box submits the form.
     */
    public void onOK$j_password2() {
        doSubmit();
    }
    
    /**
     * Submits the form when OK button is clicked.
     */
    public void onClick$btnOK() {
        doSubmit();
    }
    
    /**
     * Cancels the form when the Cancel button is clicked.
     */
    public void onClick$btnCancel() {
        doCancel();
    }
    
    /**
     * Cancel the password change request.
     */
    private void doCancel() {
        if (!forced) {
            panel.getRoot().detach();
        } else {
            Events.sendEvent(Events.ON_CLOSE, panel.getRoot(),
                Labels.getLabel("change.password.dialog.password.change.canceled"));
        }
    }
    
    /**
     * Submits the authentication request.
     */
    private void doSubmit() {
        showMessage("");
        String password = j_password.getValue().trim();
        String password1 = j_password1.getValue().trim();
        String password2 = j_password2.getValue().trim();
        
        if (!securityService.validatePassword(password)) {
            showMessage(Labels.getLabel("change.password.dialog.current.password.incorrect"));
        } else if (password.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            showMessage(Labels.getLabel("change.password.dialog.required.fields"));
        } else if (!password1.equals(password2)) {
            showMessage(Labels.getLabel("change.password.dialog.confirm.passwords"));
        } else {
            try {
                String result = securityService.changePassword(password, password1);
                
                if (result != null && !result.isEmpty()) {
                    showMessage(result);
                } else if (forced) {
                    String inst = ((User) user.getProxiedObject()).getInstitution().getName();
                    j_username.setValue(inst + "\\" + user.getUsername());
                    Events.sendEvent("onSubmit", panel.getRoot(), null);
                } else {
                    doCancel();
                    PromptDialog.showInfo(Labels.getLabel("change.password.dialog.password.changed"),
                        Labels.getLabel("change.password.dialog.password.changed.dialog.title"));
                }
            } catch (Exception e) {
                Throwable e1 = e.getCause() == null ? e : e.getCause();
                showMessage(Labels
                        .getLabel("change.password.dialog.password.change.error", new String[] { e1.getMessage() }));
            }
        }
        j_password.setValue("");
        j_password1.setValue("");
        j_password2.setValue("");
        j_password.setFocus(true);
    }
    
    /**
     * Displays the specified message text on the form.
     * 
     * @param text Message text to display.
     */
    private void showMessage(String text) {
        lblMessage.setValue(text);
        tbMessage.setVisible(!StringUtils.isEmpty(text));
    }
    
    /**
     * Sets the security service.
     * 
     * @param securityService
     */
    public void setSecurityService(SecurityServiceImpl securityService) {
        this.securityService = securityService;
    }
    
}

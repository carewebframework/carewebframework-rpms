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

import java.util.List;

import gov.ihs.cwf.domain.Institution;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.security.base.Constants;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for the login component.
 */
public class LoginPaneController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(LoginPaneController.class);
    
    protected static final String DIALOG_LOGIN_PANE = ZKUtil.getResourcePath(LoginPaneController.class) + "loginPane.zul";
    
    private Listbox j_domain;
    
    private Textbox j_username;
    
    private Textbox j_password;
    
    private Label lblMessage;
    
    private Label lblState;
    
    private Label lblInstitution;
    
    private Component institutionList;
    
    private Component institutionButton;
    
    private Label lblFooterText;
    
    private Html htmlFooterText;
    
    private Component loginPrompts;
    
    private Component loginRoot;
    
    private SecurityServiceImpl securityService;
    
    private SavedRequest savedRequest;
    
    private BrokerSession brokerSession;
    
    /**
     * Initialize the login form.
     * 
     * @param comp The root component
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        savedRequest = (SavedRequest) arg.get("savedRequest");
        final AuthenticationException authError = (AuthenticationException) arg.get("authError");
        
        String loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR);//reset back to default
        
        if (LoginWindowController.getExpired(authError) != null) {
            loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR_EXPIRED_USER);//override generic UserLoginException default
        }
        
        String username = (String) session.removeAttribute(Constants.DEFAULT_USERNAME);
        username = authError == null ? "" : username;
        showMessage(authError == null ? null : loginFailureMessage);
        j_username.setText(username);
        
        if (StringUtils.isEmpty(username)) {
            j_username.setFocus(true);
        } else {
            j_password.setFocus(true);
        }
        final List<Institution> institutions = securityService.getDomains();
        institutionButton.setVisible(institutions.size() > 1);
        String defaultInst = institutions.size() == 1 ? Long.toString(institutions.get(0).getDomainId()) : null;
        
        if (StringUtils.isEmpty(defaultInst)) {
            defaultInst = (String) session.getAttribute(Constants.DEFAULT_INSTITUTION);
        }
        
        if (StringUtils.isEmpty(defaultInst)) {
            SavedRequest savedRequest = (SavedRequest) session
                    .getAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST);
            
            if (savedRequest != null) {
                String params[] = savedRequest.getParameterValues(Constants.DEFAULT_INSTITUTION);
                
                if (params != null && params.length > 0) {
                    defaultInst = params[0];
                }
            } else {
                defaultInst = execution.getParameter(Constants.DEFAULT_INSTITUTION);
            }
        }
        
        if (StringUtils.isEmpty(defaultInst)) {
            allowInstitutionSelection();
        }
        
        long defaultInstId = NumberUtils.toLong(defaultInst, 1);
        
        if (log.isDebugEnabled()) {
            log.debug("Institutions:" + (institutions == null ? "null" : institutions.size()));
        }
        
        for (final Institution inst : institutions) {
            final Listitem li = new Listitem();
            li.setValue(inst);
            j_domain.appendChild(li);
            li.appendChild(new Listcell(inst.getAbbreviation()));
            
            if (inst.getDomainId() == defaultInstId) {
                li.setSelected(true);
            }
        }
        
        if (j_domain.getChildren().size() > 0) {
            if (j_domain.getSelectedIndex() == -1) {
                j_domain.setSelectedIndex(0);
            }
        } else {
            showState(Labels.getLabel(Constants.LBL_LOGIN_NO_VALID_INSTITUTIONS));
        }
        
        setFooterText(StrUtil.fromList(brokerSession.getPreLoginMessage()));
        institutionChanged();
    }
    
    /**
     * Username onOK event handler.
     */
    public void onOK$j_username() {
        j_password.setFocus(true);
    }
    
    /**
     * Password onOK event handler.
     */
    public void onOK$j_password() {
        doSubmit();
    }
    
    /**
     * Authority onSelect event handler.
     */
    public void onSelect$j_domain() {
        institutionChanged();
        j_username.setFocus(true);
    }
    
    /**
     * Login button onClick handler.
     */
    public void onClick$btnLogin() {
        doSubmit();
    }
    
    /**
     * Enable institution selection.
     */
    public void onClick$btnInstitution() {
        allowInstitutionSelection();
    }
    
    /**
     * Enables selection of the institution.
     */
    private void allowInstitutionSelection() {
        institutionList.setVisible(true);
        institutionButton.setVisible(false);
    }
    
    /**
     * Returns the selected institution, if any.
     * 
     * @return An institution object. May be null.
     */
    private Institution getSelectedInstitution() {
        Listitem item = j_domain.getSelectedItem();
        return item == null ? null : (Institution) item.getValue();
    }
    
    /**
     * Submits the authentication request.
     */
    private void doSubmit() {
        showMessage("");
        final Institution inst = getSelectedInstitution();
        String instId = inst == null ? null : Long.toString(inst.getDomainId());
        String username = j_username.getValue().trim();
        final String password = j_password.getValue();
        
        if (username.contains("\\")) {
            String[] pcs = username.split("\\\\", 2);
            instId = pcs[0];
            username = pcs[1];
        }
        
        if (!username.isEmpty() && !password.isEmpty() && !instId.isEmpty()) {
            session.setAttribute(Constants.DEFAULT_INSTITUTION, instId);
            FrameworkWebSupport.setCookie(Constants.DEFAULT_INSTITUTION, instId);
            session.setAttribute(Constants.DEFAULT_USERNAME, username);
            //FrameworkWebSupport.setCookie(Constants.DEFAULT_USERNAME, username);
            j_username.setValue(instId + "\\" + username);
            showState(Labels.getLabel(Constants.LBL_LOGIN_PROGRESS));
            session.setAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST, savedRequest);
            Events.sendEvent("onSubmit", loginRoot.getRoot(), null);
        } else {
            showMessage(Labels.getLabel(Constants.LBL_LOGIN_REQUIRED_FIELDS));
        }
    }
    
    /**
     * Displays the specified message text on the form.
     * 
     * @param text Message text to display.
     */
    private void showMessage(final String text) {
        lblMessage.setValue(text);
        lblMessage.setVisible(!StringUtils.isEmpty(text));
    }
    
    /**
     * Disable all user input elements.
     * 
     * @param text State text to display.
     */
    private void showState(final String text) {
        lblState.setValue(text);
        loginPrompts.setVisible(false);
        lblState.setVisible(true);
    }
    
    private void institutionChanged() {
        lblInstitution.setValue(getSelectedInstitution().getName());
    }
    
    /**
     * Sets the message text to the specified value. If the text starts with an html tag, it will be
     * rendered as such.
     * 
     * @param value The message text.
     * @param plainText Component to display plain text.
     * @param htmlText Component to display html.
     */
    private void setMessageText(String value, Label plainText, Html htmlText) {
        value = StringUtils.trimToEmpty(value);
        final boolean isHtml = StringUtils.startsWithIgnoreCase(value, "<html>");
        final boolean notEmpty = !value.isEmpty();
        plainText.setVisible(notEmpty && !isHtml);
        htmlText.setVisible(notEmpty && isHtml);
        
        if (isHtml) {
            htmlText.setContent(value);
        } else {
            plainText.setValue(value);
        }
    }
    
    /**
     * Sets the footer message text to the specified value. If the text starts with an html tag, it
     * will be rendered as such.
     * 
     * @param value Footer message text.
     */
    private void setFooterText(String value) {
        setMessageText(value, lblFooterText, htmlFooterText);
    }
    
    /**
     * Sets the security service.
     * 
     * @param securityService SecurityService implementation
     */
    public void setSecurityService(final SecurityServiceImpl securityService) {
        this.securityService = securityService;
    }
    
    /**
     * Sets the broker session.
     * 
     * @param brokerSession The broker session.
     */
    public void setBrokerSession(BrokerSession brokerSession) {
        this.brokerSession = brokerSession;
        brokerSession.ensureConnection();
    }
    
}

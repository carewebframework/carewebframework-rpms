/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.plugin.anticoag;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;

/**
 * Controller for anticoagulation management.
 */
public class DeleteController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(DeleteController.class) + "delete.zul";
    
    private static final String ATTR_OK = "ok";
    
    private Service service;
    
    private Textbox txtOther;
    
    private Radio radOther;
    
    private Radiogroup rgReason;
    
    private AntiCoagRecord record;
    
    public static boolean show(AntiCoagRecord record) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("record", record);
        return PopupDialog.popup(DIALOG, args, false, false, true).hasAttribute(ATTR_OK);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        record = (AntiCoagRecord) arg.get("record");
    }
    
    public void setService(Service service) {
        this.service = service;
    }
    
    public void onCheck$rgReason() {
        clearMessages();
    }
    
    public void onCheck$radOther() {
        txtOther.setFocus(true);
        clearMessages();
    }
    
    public void onFocus$txtOther() {
        radOther.setSelected(true);
        clearMessages();
    }
    
    private void clearMessages() {
        Clients.clearWrongValue(txtOther);
        Clients.clearWrongValue(rgReason);
    }
    
    public void onClick$btnOK() {
        clearMessages();
        Radio radio = rgReason.getSelectedItem();
        String reasonCode = radio == null ? null : (String) radio.getValue();
        
        if (reasonCode == null) {
            Clients.wrongValue(rgReason, "You must select a reason for the deletion.");
            return;
        }
        
        String reasonText = "3".equals(reasonCode) ? txtOther.getText().trim() : null;
        
        if (reasonText != null && reasonText.isEmpty()) {
            Clients.wrongValue(txtOther, "Please enter text describing the reason for the deletion.");
            return;
        }
        
        try {
            service.delete(record, reasonCode, reasonText);
        } catch (Exception e) {
            PromptDialog.showError(e.getMessage());
            return;
        }
        
        root.setAttribute("ok", true);
        root.detach();
    }
}

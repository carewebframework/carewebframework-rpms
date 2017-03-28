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
package org.carewebframework.rpms.plugin.problemlist.controller;

import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.plugin.problemlist.util.Constants;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.util.VistAUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AddNoteController extends BgoBaseController<String> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PREFIX + "addNote.zul";
    
    private Textbox txtNote;
    
    public static String execute() {
        Window dlg = PopupDialog.popup(DIALOG);
        AddNoteController controller = (AddNoteController) FrameworkController.getController(dlg);
        return controller.result;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }
    
    public void onClick$btnOK() {
        String text = VistAUtil.trimNarrative(txtNote.getValue());
        
        if (text.length() < 3) {
            PromptDialog.showError("Note must be at least 3 characters in length.", "Note too Short");
            txtNote.setValue(text);
            return;
        }
        
        result = text;
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
}

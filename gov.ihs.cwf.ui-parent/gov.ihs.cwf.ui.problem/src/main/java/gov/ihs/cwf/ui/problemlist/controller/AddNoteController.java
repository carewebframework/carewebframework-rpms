/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.problemlist.controller;

import gov.ihs.cwf.common.bgo.BgoBaseController;
import gov.ihs.cwf.ui.problemlist.util.Constants;

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

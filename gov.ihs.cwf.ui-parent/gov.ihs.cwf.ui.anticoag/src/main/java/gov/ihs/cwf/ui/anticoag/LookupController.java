/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.anticoag;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

/**
 * Controller for anticoagulation management.
 */
public class LookupController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(LookupController.class) + "lookup.zul";
    
    public static void show() {
        PopupDialog.popup(DIALOG, null, false, false, true);
    }
}

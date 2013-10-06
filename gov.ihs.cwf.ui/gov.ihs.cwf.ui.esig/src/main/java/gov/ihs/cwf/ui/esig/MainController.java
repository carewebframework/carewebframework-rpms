/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.esig;

import gov.ihs.cwf.esig.IESigService;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.ui.FrameworkController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;

/**
 * This is the controller for the esignature plugin.
 * 
 * @author dmartin
 */
public class MainController extends FrameworkController implements IGenericEvent<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private IESigService eSigService;
    
    private Button button;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        EventManager.getInstance().subscribe("ESIG", this);
        update();
    }
    
    private void update() {
        int i = eSigService.getCount();
        button.setDisabled(i == 0);
        button.setTooltiptext(String.format("There %s %s item%s awaiting review.", i == 1 ? "is" : "are", i == 0 ? "no" : i,
            i != 1 ? "s" : ""));
    }
    
    public void onClick$button() throws Exception {
        ESigViewer.execute(eSigService, null);
    }
    
    @Override
    public void eventCallback(String eventName, Object eventData) {
        update();
    }
    
    public void seteSigService(IESigService eSigService) {
        this.eSigService = eSigService;
    }
    
    public IESigService geteSigService() {
        return eSigService;
    }
    
}

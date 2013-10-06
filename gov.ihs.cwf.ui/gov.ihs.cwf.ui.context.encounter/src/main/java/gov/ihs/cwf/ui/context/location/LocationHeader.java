/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.context.location;

import gov.ihs.cwf.context.LocationContext;
import gov.ihs.cwf.domain.Location;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.ui.FrameworkController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;

/**
 * Controller for location header component.
 * 
 * @author dmartin
 */
public class LocationHeader extends FrameworkController implements LocationContext.ILocationContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(LocationHeader.class);
    
    private Label locationHeader;
    
    private String noSelectionMessage;
    
    private Component root;
    
    /**
     * Invoke location selection dialog when location button is clicked.
     */
    public void onClick$location() {
        LocationSelection.execute();
    }
    
    /**
     * Initialize controller.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
        noSelectionMessage = locationHeader.getValue();
        committed();
    }
    
    @Override
    public void canceled() {
    }
    
    /**
     * Update the display when the location context changes.
     */
    @Override
    public void committed() {
        Location location = LocationContext.getCurrentLocation();
        
        if (log.isDebugEnabled()) {
            log.debug("location: " + location);
        }
        
        String text = location == null ? noSelectionMessage : location.getName();
        locationHeader.setValue(text);
        Clients.resize(root);
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
}

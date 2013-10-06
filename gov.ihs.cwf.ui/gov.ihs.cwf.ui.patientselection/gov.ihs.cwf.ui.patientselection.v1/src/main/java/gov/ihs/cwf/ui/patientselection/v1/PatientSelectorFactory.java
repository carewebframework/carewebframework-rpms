/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection.v1;

import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.ui.patientselection.Constants;
import gov.ihs.cwf.ui.patientselection.IPatientSelector;
import gov.ihs.cwf.ui.patientselection.PatientSelectorFactoryBase;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

/**
 * This is the patient selection factory.
 * 
 * @author dmartin
 */
public class PatientSelectorFactory extends PatientSelectorFactoryBase {
    
    public static class PatientSelector implements IPatientSelector {
        
        private Window dlg = PopupDialog.popup(ZKUtil.getResourcePath(PatientSelectorFactory.class) + "patientSelection.zul", false,
            true, false);
        
        @Override
        public Patient select() {
            Events.sendEvent("onShow", dlg, null);
            return (Patient) dlg.getAttribute(Constants.SELECTED_PATIENT_ATTRIB);
        }
    };
    
    protected PatientSelectorFactory() {
        super("New INPC patient selector", PatientSelector.class);
    }
}

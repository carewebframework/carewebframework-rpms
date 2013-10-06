/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.ihs.cwf.domain.Patient;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;

/**
 * Default class for rendering detail view of patient in patient selection dialog. This class may be
 * overridden to provide an alternate detail view.
 */
public class PatientDetailRenderer implements IPatientDetailRenderer {
    
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(PatientDetailRenderer.class);
    
    /**
     * Render detail view for the specified patient.
     * 
     * @param patient Patient whose detail view is to be rendered.
     * @param root Root component under which detail should be constructed.
     */
    @Override
    public Component render(Component root, Patient patient, Object... supportData) {
        if (confirmAccess(patient, root)) {
            renderDemographics(patient, root);
        }
        
        return null;
    }
    
    protected void renderDemographics(Patient patient, Component root) {
        root.appendChild(new Separator());
        addDemographic(root, null, patient.getFullName(), "font-weight: bold");
        addDemographic(root, "gender", patient.getGender());
        //addDemographic(root, "race", patient.getRace());
        addDemographic(root, "age", patient.getAgeForDisplay());
        addDemographic(root, "dob", patient.getBirthDate());
        addDemographic(root, "dod", patient.getDeathDate());
        /*
        addDemographic(root, "mother", patient.getMothersFirstName());
        addDemographic(root, "home", patient.getPhoneHome());
        addDemographic(root, "home", patient.getHomeEmail());
        addDemographic(root, "work", patient.getPhoneWork());
        addDemographic(root, "work", patient.getWorkEmail());
        addDemographic(root, "fax", patient.getFaxNumber());
        Address address = patient.getAddressHome();
        
        if (address != null) {
            root.appendChild(new Separator());
            addDemographic(root, null, address.getLine1());
            addDemographic(root, null, address.getLine2());
        }*/
    }
    
    /**
     * Confirm access to patient.
     * 
     * @param patient
     * @param root
     * @return
     */
    private boolean confirmAccess(Patient patient, Component root) {
        boolean allowed = !patient.isSensitive();
        
        if (!allowed)
            addDemographic(root, null, getDemographicLabel("restricted"), "font-weight: bold");
        
        return allowed;
    }
    
    /**
     * Adds a demographic element to the demographic panel. Uses default styling.
     * 
     * @param root
     * @param labelId The id of the label to use.
     * @param object The element to be added.
     */
    protected void addDemographic(Component root, String labelId, Object object) {
        addDemographic(root, labelId, object, null);
    }
    
    /**
     * Adds a demographic element to the demographic panel.
     * 
     * @param root
     * @param labelId The id of the label to use.
     * @param object The element to be added.
     * @param style CSS styling to apply to element (may be null).
     */
    protected void addDemographic(Component root, String labelId, Object object, String style) {
        String value = object == null ? null : object.toString().trim();
        
        if (!StringUtils.isEmpty(value)) {
            Label lbl = new Label((labelId == null ? "" : getDemographicLabel(labelId) + ": ") + value);
            root.appendChild(lbl);
            
            if (style != null)
                lbl.setStyle(style);
        }
        
    }
    
    /**
     * Returns the text for the specified label id.
     * 
     * @param labelId The id of the label value to locate. If no prefix is present, the id is
     *            prefixed with "patient.selection.demographic.label." to find the associated value.
     * @return
     */
    protected String getDemographicLabel(final String labelId) {
        return Labels.getLabel(labelId.contains(".") ? labelId : "patientselection.demographic.label." + labelId);
    }
    
}

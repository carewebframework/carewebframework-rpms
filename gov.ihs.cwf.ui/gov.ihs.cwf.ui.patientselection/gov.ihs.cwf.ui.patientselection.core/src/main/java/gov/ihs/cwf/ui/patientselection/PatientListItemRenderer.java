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

import java.util.Date;

import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Person.Name;
import gov.ihs.cwf.patientlist.PatientListItem;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.DateUtil;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;

/**
 * Renderer for patient list items.
 */
public class PatientListItemRenderer extends AbstractListitemRenderer<Object, Object> {
    
    private static final PatientListItemRenderer instance = new PatientListItemRenderer();
    
    /**
     * Return singleton instance.
     * 
     * @return Patient list item renderer.
     */
    public static PatientListItemRenderer getInstance() {
        return instance;
    }
    
    /**
     * Force singleton usage.
     */
    private PatientListItemRenderer() {
        super("", null);
    }
    
    /**
     * Render a list item.
     * 
     * @param item The list item to render.
     * @param object The associated PatientListItem or Patient object.
     */
    @Override
    public void renderItem(Listitem item, Object object) {
        PatientListItem patientListItem;
        
        if (object instanceof PatientListItem) {
            patientListItem = (PatientListItem) object;
        } else if (object instanceof Patient) {
            patientListItem = new PatientListItem((Patient) object, null);
        } else {
            throw new RuntimeException("Invalid object type: " + object);
        }
        
        item.setValue(patientListItem);
        Patient patient = patientListItem.getPatient();
        // If list headers are defined, limit rendering to that number of cells.
        Listhead head = item.getListbox().getListhead();
        int max = head == null ? 0 : head.getChildren().size();
        String info = patientListItem.getInfo();
        
        if (patient != null) {
            Name name = patient.getName();
            addCell(item, name.getLastName(), max);
            String fname = name.getFirstName();
            String mname = name.getMiddleName();
            mname = StringUtils.isEmpty(mname) ? "" : " " + mname.charAt(0);
            addCell(item, fname + mname, max);
            String mrn = patient.getMedicalRecordNumber();
            addCell(item, mrn == null ? "" : mrn, max);
            
            if (StringUtils.isEmpty(info)) {
                Date dob = patient.getBirthDate();
                info = dob == null ? "" : DateUtil.formatDate(dob);
            }
        }
        
        if (!StringUtils.isEmpty(info)) {
            addCell(item, info, max);
        }
    }
    
    /**
     * Add a cell to the list item.
     * 
     * @param item List item to receive the cell.
     * @param label Text label for the cell.
     * @param max Maximum # of allowable cells.
     */
    private void addCell(Listitem item, String label, int max) {
        if (max == 0 || item.getChildren().size() < max) {
            createCell(item, label);
        }
    }
}

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

import java.util.Collection;

import gov.ihs.cwf.patientlist.IPatientList;
import gov.ihs.cwf.patientlist.PatientListItem;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.ui.FrameworkController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;

/**
 * Controller for patient list display. Recognizes the following dynamic properties:
 */
public class PatientListController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ATTR_PATIENT_LIST = "patientList";
    
    public static final String ATTR_EVENT_LISTENER = "eventListener";
    
    public static final String ATTR_MAX_ROWS = "maxRows";
    
    private Listbox patientList;
    
    private EventListener<Event> selectListener;
    
    /**
     * Set up the list box based on dynamic properties passed via the execution.
     * 
     * @param comp The top level component.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        IPatientList plist = getPatientList();
        patientList.setItemRenderer(PatientListItemRenderer.getInstance());
        Collection<PatientListItem> items = plist.getListItems();
        patientList.setModel(new ListModelList<PatientListItem>(items));
        int maxRows = getMaxRows();
        int count = items.size();
        patientList.setRows(count > maxRows ? maxRows : count);
        selectListener = (EventListener<Event>) execution.getAttribute(ATTR_EVENT_LISTENER);
    }
    
    /**
     * Returns the maximum rows from the "maxRows" dynamic property. If none specified, defaults to
     * 8.
     * 
     * @return
     */
    private int getMaxRows() {
        Object maxRows = execution.getAttribute(ATTR_MAX_ROWS);
        return maxRows == null ? 8 : NumberUtils.toInt(maxRows.toString(), 8);
    }
    
    /**
     * Returns the patient list from the "patientList" dynamic property.
     * 
     * @return
     */
    private IPatientList getPatientList() {
        Object plist = execution.getAttribute(ATTR_PATIENT_LIST);
        
        if (plist instanceof String) {
            return (IPatientList) SpringUtil.getBean((String) plist);
        } else if (plist instanceof IPatientList) {
            return (IPatientList) plist;
        } else {
            return null;
        }
    }
    
    /**
     * Pass selection event to external listener, if any.
     * 
     * @param event
     * @throws Exception
     */
    public void onSelect$patientList(Event event) throws Exception {
        if (selectListener != null) {
            selectListener.onEvent(event);
        }
    }
    
}

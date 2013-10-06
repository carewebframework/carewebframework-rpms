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

import gov.ihs.cwf.patientlist.AbstractPatientListFilter;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Renderer for filters.
 */
public class FilterRenderer implements ListitemRenderer<AbstractPatientListFilter> {
    
    private static final FilterRenderer instance = new FilterRenderer();
    
    /**
     * Return singleton instance.
     * 
     * @return
     */
    public static FilterRenderer getInstance() {
        return instance;
    }
    
    /**
     * Force singleton usage.
     */
    private FilterRenderer() {
        super();
    }
    
    /**
     * Render a list item.
     * 
     * @param item The list item to render.
     * @param filter The associated PatientListFilter object.
     * @param index
     */
    @Override
    public void render(Listitem item, AbstractPatientListFilter filter, int index) throws Exception {
        item.setValue(filter);
        addCell(item, filter.getName());
    }
    
    /**
     * Add a cell to the list item.
     * 
     * @param item List item to receive the cell.
     * @param label Text label for the cell.
     */
    private void addCell(Listitem item, String label) {
        Listcell cell = new Listcell(label);
        cell.setTooltiptext(label);
        item.appendChild(cell);
    }
}

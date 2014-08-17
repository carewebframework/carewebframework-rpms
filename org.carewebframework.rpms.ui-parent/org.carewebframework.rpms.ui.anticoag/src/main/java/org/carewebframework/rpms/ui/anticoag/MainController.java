/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.anticoag;

import java.util.List;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.context.EncounterContext;
import org.carewebframework.cal.api.context.EncounterContext.IEncounterContextEvent;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.fhir.model.resource.Encounter;
import org.carewebframework.fhir.model.resource.Patient;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.domain.EncounterUtil;
import org.carewebframework.vista.ui.common.CoverSheetBase;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Toolbar;

/**
 * Controller for anticoagulation management.
 */
public class MainController extends CoverSheetBase<AntiCoagRecord> implements IEncounterContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Toolbar toolbar;
    
    private Button btnEdit;
    
    private Button btnAdd;
    
    private Button btnDelete;
    
    private boolean visitHasEntry;
    
    private String visitIEN;
    
    private String pccEvent;
    
    private Encounter encounter;
    
    private final IGenericEvent<String> pccListener = new IGenericEvent<String>() {
        
        @Override
        public void eventCallback(String eventName, String eventData) {
            String[] pcs = eventData.split("\\^");
            AntiCoagRecord record = findRecord(pcs[0]);
            
            switch (Integer.parseInt(pcs[2])) {
                case 0: // add
                    if (record == null) {
                        refresh();
                    }
                    break;
                
                case 1: // edit
                    refresh();
                    break;
                
                case 2: // delete
                    if (record != null) {
                        model.remove(record);
                    }
                    break;
            }
        }
        
    };
    
    @Override
    protected void init() {
        setup("Anticoagulation Data", "", "BGOVCOAG GET", null, 1, "Indicated", "Visit Date", "INR Goal", "Min", "Max",
            "Duration", "Start Date", "End Date", "Entered Date", "Category", "Comment");
        toolbar.setVisible(true);
        setIcon(ZKUtil.getResourcePath(MainController.class) + "main-icon.png");
        super.init();
    }
    
    @Override
    public void refresh() {
        visitHasEntry = false;
        super.refresh();
        updateControls();
    }
    
    @Override
    public void committed() {
        if (pccEvent != null) {
            getEventManager().unsubscribe(pccEvent, pccListener);
        }
        
        encounter = EncounterContext.getActiveEncounter();
        visitIEN = encounter == null ? null : encounter.getLogicalId();
        super.committed();
        Patient patient = PatientContext.getActivePatient();
        pccEvent = patient == null ? null : "PCC." + patient.getLogicalId() + ".ACG";
        
        if (pccEvent != null) {
            getEventManager().subscribe(pccEvent, pccListener);
        }
    }
    
    @Override
    protected void render(AntiCoagRecord record, List<Object> columns) {
        columns.add(record.getIndicated() ? "Yes" : "No"); // Indicated
        columns.add(record.getVisitDate()); // Visit date
        columns.add(record.getGoalRange()); // Goal
        columns.add(record.getGoalMin()); // Min
        columns.add(record.getGoalMax()); // Max
        columns.add(record.getDuration()); // Duration
        columns.add(record.getStartDate()); // Start date
        columns.add(record.getEndDate() == null ? "Indefinitely" : record.getEndDate()); // End date
        columns.add(record.getEnteredDate()); // Entered date
        columns.add(record.getVisitCategory()); // Category
        columns.add(record.getComment()); // Comment
        visitHasEntry |= record.getVisitIEN() != null && visitIEN == record.getVisitIEN();
    }
    
    @Override
    protected void renderItem(Listitem item, AntiCoagRecord record) {
        super.renderItem(item, record);
        item.addForward(Events.ON_DOUBLE_CLICK, btnEdit, Events.ON_CLICK);
    }
    
    @Override
    protected AntiCoagRecord parseData(String data) {
        return new AntiCoagRecord(data);
    }
    
    private void updateControls() {
        btnAdd.setDisabled(encounter == null || EncounterUtil.isLocked(encounter));
        AntiCoagRecord record = getSelectedValue();
        boolean isLocked = record == null || record.getVisitLocked();
        btnEdit.setDisabled(isLocked);
        btnDelete.setDisabled(isLocked);
    }
    
    public void onClick$btnAdd() {
        AddEditController.show(null);
    }
    
    public void onClick$btnEdit() {
        if (!btnEdit.isDisabled()) {
            AntiCoagRecord oldRecord = getSelectedValue();
            AntiCoagRecord newRecord = AddEditController.show(oldRecord);
            
            if (newRecord != null) {
                model.remove(oldRecord);
                model.add(newRecord);
            }
        }
    }
    
    public void onClick$btnDelete() {
        Listitem item = getSelectedItem();
        
        if (DeleteController.show(getSelectedValue())) {
            item.setSelected(false);
            item.setVisible(false);
            updateControls();
        }
    }
    
    private AntiCoagRecord findRecord(String ien) {
        for (AntiCoagRecord record : model) {
            if (record.getLogicalId().equals(ien)) {
                return record;
            }
        }
        
        return null;
    }
    
    @Override
    public void itemSelected(Listitem item) {
        super.itemSelected(item);
        updateControls();
    }
    
}

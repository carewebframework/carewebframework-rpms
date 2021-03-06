/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.plugin.anticoag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.NumUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.wonderbar.IWonderbarServerSearchProvider;
import org.carewebframework.ui.wonderbar.Wonderbar;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.encounter.EncounterUtil;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.api.encounter.EncounterContext;
import org.hspconsortium.cwf.api.practitioner.PractitionerSearch;
import org.hspconsortium.cwf.api.practitioner.PractitionerSearchCriteria;
import org.hspconsortium.cwf.fhir.common.FhirUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for anticoagulation management.
 */
public class AddEditController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(AddEditController.class) + "add-edit.zul";
    
    private static final String ATTR_RESULT = "result";
    
    private Radiogroup rgIndicated;
    
    private Radio radYes;
    
    private Radio radNo;
    
    private Combobox cboGoal;
    
    private Combobox cboMin;
    
    private Combobox cboMax;
    
    private Combobox cboDuration;
    
    private Component pnlGoalOther;
    
    private Datebox datStart;
    
    private Textbox txtEnd;
    
    private Wonderbar<Practitioner> wbProvider;
    
    private Textbox txtComment;
    
    private Rows rows;
    
    private Service service;
    
    private Component[] inputs;
    
    private AntiCoagRecord record;
    
    private PractitionerSearch practitionerSearch;
    
    private final IWonderbarServerSearchProvider<Practitioner> providerSearch = new IWonderbarServerSearchProvider<Practitioner>() {
        
        @Override
        public List<Practitioner> getDefaultItems() {
            return Collections.singletonList(record.getProvider());
        }
        
        @Override
        public boolean getSearchResults(String search, int maxItems, List<Practitioner> hits) {
            hits.clear();
            PractitionerSearchCriteria criteria = new PractitionerSearchCriteria(search);
            criteria.setMaximum(maxItems + 1);
            hits.addAll(practitionerSearch.search(criteria));
            boolean tooMany = hits.size() > maxItems;
            
            if (tooMany) {
                hits.remove(maxItems);
            }
            
            return !tooMany;
        }
        
    };
    
    public static AntiCoagRecord show(AntiCoagRecord record) {
        Map<Object, Object> args = new HashMap<>();
        args.put("record", record);
        return (AntiCoagRecord) PopupDialog.popup(DIALOG, args, false, false, true).getAttribute(ATTR_RESULT);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        record = (AntiCoagRecord) arg.get("record");
        inputs = new Component[] { rgIndicated, cboGoal, cboMin, cboMax, cboDuration, datStart, wbProvider, txtComment };
        loadComboValues();
        Window win = (Window) root;
        win.setTitle(StrUtil.formatMessage(win.getTitle(), record == null ? "Add" : "Edit"));
        Events.postEvent("onInitDialog", comp, null);
    }
    
    /**
     * Deferring this allows combo boxes to fully initialize.
     *
     * @param event The onInitDialog event.
     */
    public void onInitDialog(Event event) {
        initDialog();
    }
    
    /**
     */
    private void initDialog() {
        if (record == null) {
            record = new AntiCoagRecord();
            record.setIndicated(true);
            record.setStartDate(DateUtil.today());
            IUser user = UserContext.getActiveUser();
            Practitioner provider = new Practitioner();
            provider.setId(user.getLogicalId());
            provider.addName(FhirUtil.parseName(user.getFullName()));
            record.setProvider(provider);
        } else {
            record = new AntiCoagRecord(record);
        }
        
        rgIndicated.setSelectedItem(record.getIndicated() ? radYes : radNo);
        selectItem(cboGoal, record.getGoalRange());
        selectItem(cboMin, formatDouble(record.getGoalMin()));
        selectItem(cboMax, formatDouble(record.getGoalMax()));
        selectItem(cboDuration, record.getDuration());
        datStart.setValue(record.getStartDate());
        txtComment.setValue(record.getComment());
        wbProvider.setSearchProvider(providerSearch);
        //wbProvider.setItemRenderer(providerRenderer);
        wbProvider.setSelectedItem(record.getProvider().getName().toString(), record.getProvider());
        updateMinMax();
        updateControls();
        updateEndDate();
    }
    
    private void selectItem(Combobox cbo, String label) {
        if (label != null) {
            ListUtil.selectComboboxItem(cbo, label);
        }
    }
    
    private String formatDouble(Double value) {
        return value == null ? "" : NumUtil.toString(value);
    }
    
    private void updateEndDate() {
        record.setDuration(cboDuration.getText());
        record.setStartDate(datStart.getValue());
        String text = record.getEndDate() == null ? "Indefinitely" : DateUtil.formatDate(record.getEndDate());
        txtEnd.setText(text);
    }
    
    private void loadComboValues() {
        cboGoal.setModel(new ListModelList<>(service.getGoalPresets()));
        cboDuration.setModel(new ListModelList<>(service.getDurationPresets()));
    }
    
    private void updateControls() {
        boolean visible = radYes.isSelected();
        boolean first = true;
        
        for (Component row : rows.getChildren()) {
            row.setVisible(first || visible);
            first = false;
        }
        
        pnlGoalOther.setVisible("other".equalsIgnoreCase(cboGoal.getValue()));
        Clients.resize(pnlGoalOther);
    }
    
    private boolean validateInputs() {
        Clients.clearWrongValue(inputs);
        
        if (radNo.isChecked()) {
            syncInputs();
            return true;
        }
        
        if (!radYes.isChecked()) {
            Clients.wrongValue(rgIndicated, "You must provide an indication.");
        } else if (cboGoal.getSelectedItem() == null) {
            Clients.wrongValue(cboGoal, "You must provide a goal.");
        } else if (pnlGoalOther.isVisible() && cboMin.getSelectedItem() == null) {
            Clients.wrongValue(cboMin, "You must provide a minimum.");
        } else if (pnlGoalOther.isVisible() && cboMax.getSelectedItem() == null) {
            Clients.wrongValue(cboMax, "You must provide a maximum.");
        } else if (cboDuration.getSelectedItem() == null) {
            Clients.wrongValue(cboDuration, "You must provide a duration.");
        } else if (datStart.getValue() == null) {
            Clients.wrongValue(datStart, "You must provide a starting date.");
        } else if (wbProvider.getSelectedData() == null) {
            Clients.wrongValue(wbProvider, "You must specify a provider.");
        } else {
            syncInputs();
            return true;
        }
        
        return false;
    }
    
    private void syncInputs() {
        boolean ignore = radNo.isChecked();
        record.setIndicated(radYes.isChecked());
        record.setGoalRange(ignore ? null : cboGoal.getText());
        record.setGoalMin(ignore ? null : NumberUtils.toDouble(cboMin.getText()));
        record.setGoalMax(ignore ? null : NumberUtils.toDouble(cboMax.getText()));
        record.setDuration(ignore ? null : cboDuration.getText());
        record.setStartDate(ignore ? null : datStart.getValue());
        record.setComment(txtComment.getText());
        record.setProvider((Practitioner) wbProvider.getSelectedData());
    }
    
    public void onCheck$rgIndicated() {
        updateControls();
    }
    
    public void onChange$datStart() {
        updateEndDate();
    }
    
    public void onSelect$cboDuration() {
        updateEndDate();
    }
    
    public void onSelect$cboGoal() {
        updateMinMax();
        updateControls();
    }
    
    public void onFocus$wbProvider() {
        wbProvider.select();
    }
    
    private void updateMinMax() {
        String value = cboGoal.getText();
        
        if (value.contains("-")) {
            String[] pcs = value.split("\\-");
            ListUtil.selectComboboxItem(cboMin, pcs[0].trim());
            ListUtil.selectComboboxItem(cboMax, pcs[1].trim());
        }
        
    }
    
    public void onClick$btnSave() {
        if (validateInputs()) {
            if (record.getVisitIEN() == null) {
                Encounter encounter = EncounterContext.getActiveEncounter();
                EncounterUtil.forceCreate(encounter);
                record.setVisitCategory(EncounterUtil.getServiceCategory(encounter));
                record.setVisitDate(encounter.getPeriod().getStart());
                record.setVisitIEN(encounter.getIdElement().getIdPart());
                record.setVisitLocked(EncounterUtil.isLocked(encounter));
            }
            
            try {
                service.update(record);
            } catch (Exception e) {
                PromptDialog.showError(e.getMessage());
                return;
            }
            
            root.setAttribute(ATTR_RESULT, record);
            root.detach();
        }
    }
    
    public void setService(Service service) {
        this.service = service;
    }
    
    public void setPractitionerSearch(PractitionerSearch practitionerSearch) {
        this.practitionerSearch = practitionerSearch;
    }
    
}

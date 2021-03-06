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
package org.carewebframework.rpms.plugin.skintest.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.common.DateUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.plugin.skintest.controller.SkinTestController.EventType;
import org.carewebframework.rpms.plugin.skintest.model.TestItem;
import org.carewebframework.rpms.plugin.skintest.util.Constants;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupController;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.Table;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.plugin.encounter.EncounterUtil;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hspconsortium.cwf.api.encounter.EncounterContext;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AddSkinTestController extends BgoBaseController<Object> {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG = Constants.RESOURCE_PREFIX + "addSkinTest.zul";

    private Textbox txtSkinTest;

    private Textbox txtAdminBy;

    private Textbox txtReadBy;

    private Textbox txtLocation;

    private Combobox cboResults;

    private Datebox datEvent;

    private Datebox datRead;

    private Radio radFacility;

    private Radio radOther;

    private Radio radCurrent;

    private Radio radHistorical;

    private Radio radRefusal;

    private Spinner spnReading;

    private Component fraHistorical;

    private Component fraCurrent;

    private TestItem test;

    public static TestItem execute(TestItem test) {
        if (test == null && !EncounterUtil.ensureEncounter()) {
            return null;
        }

        Params params = new Params(test);
        Window dlg = PopupDialog.popup(DIALOG, params, true, true, true);
        AddSkinTestController controller = (AddSkinTestController) FrameworkController.getController(dlg);
        return controller.canceled() ? null : test;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Params params = (Params) arg;
        this.test = (TestItem) params.get(0);
        radFacility.setLabel(VistAUtil.getSysParam("Caption-Facility", radFacility.getLabel(), null));
        loadForm();
    }

    /**
     * Loads form data from the current problem.
     */
    private void loadForm() {
        if (test != null) {
            setEventType(test.getEventType());
            radRefusal.setDisabled(!radRefusal.isChecked());
            radHistorical.setDisabled(!radHistorical.isChecked());
            radCurrent.setDisabled(!radCurrent.isChecked());
            datEvent.setValue(test.getDate());
            ZKUtil.disableChildren(fraHistorical, true);
            datEvent.setDisabled(true);
            int reading = NumberUtils.toInt(test.getReading(), -1);
            spnReading.setValue(reading < 0 ? null : reading);
        } else {
            onClick$btnSkinTest();

            if (txtSkinTest.getValue().isEmpty()) {
                close(true);
                return;
            }
            setEventType(EventType.CURRENT);
            Encounter visit = EncounterContext.getActiveEncounter();
            Date date = visit == null ? null : visit.getPeriod().getStart();
            datEvent.setValue(DateUtil.stripTime(date == null ? new Date() : date));
        }
    }

    private void setEventType(EventType eventType) {
        switch (eventType) {
            case HISTORICAL:
                radHistorical.setChecked(true);
                fraHistorical.setVisible(true);
                fraCurrent.setVisible(false);
                enableResultItems("", "POSITIVE", "NEGATIVE", "DOUBTFUL", "NO TAKE");
                break;

            case CURRENT:
                fraHistorical.setVisible(false);
                fraCurrent.setVisible(true);
                radCurrent.setChecked(true);
                enableResultItems("PENDING", "POSITIVE", "NEGATIVE", "DOUBTFUL", "NO TAKE");
                break;

            case REFUSAL:
                radRefusal.setChecked(true);
                fraHistorical.setVisible(false);
                fraCurrent.setVisible(false);
                enableResultItems("", "REFUSED");
                break;
        }
    }

    private void enableResultItems(String... labels) {
        List<String> enable = Arrays.asList(labels);
        Comboitem firstVisible = null;

        for (Object object : cboResults.getItems()) {
            Comboitem item = (Comboitem) object;
            boolean visible = enable.contains(item.getLabel());
            item.setVisible(visible);

            if (visible && firstVisible == null) {
                firstVisible = item;
            }
        }

        cboResults.setSelectedItem(firstVisible);
    }

    private boolean validateAll() {
        return true;
    }

    public void onClick$btnSkinTest() {
        LookupController.execute(Table.rtSkinTest, txtSkinTest.getValue());
    }

    public void onClick$btnAdminBy() {
        LookupController.execute(Table.rtProvider, txtAdminBy.getValue());
    }

    public void onClick$btnLocation() {
        LookupController.execute(Table.rtLocation, txtLocation.getValue());
    }

    public void onClick$btnReadBy() {
        LookupController.execute(Table.rtProvider, txtReadBy.getValue());
    }

    public void onClick$btnSave() {
        if (!validateAll()) {
            return;
        }

        close(false);
    }

    public void onClick$btnCancel() {
        close(true);
    }

}

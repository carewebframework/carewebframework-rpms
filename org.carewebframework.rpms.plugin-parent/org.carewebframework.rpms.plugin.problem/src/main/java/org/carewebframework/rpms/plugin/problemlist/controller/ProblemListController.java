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
package org.carewebframework.rpms.plugin.problemlist.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.BgoUtil.BgoSecurity;
import org.carewebframework.rpms.api.domain.Problem;
import org.carewebframework.rpms.plugin.problemlist.render.ProblemRenderer;
import org.carewebframework.rpms.plugin.problemlist.util.ProblemFilter;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.ui.mbroker.AsyncRPCCompleteEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCErrorEvent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hspconsortium.cwf.api.encounter.EncounterContext.IEncounterContextEvent;
import org.hspconsortium.cwf.api.patient.PatientContext;
import org.hspconsortium.cwf.api.patient.PatientContext.IPatientContextEvent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;

/**
 */
public class ProblemListController extends BgoBaseController<Object> implements IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(ProblemListController.class);
    
    private static final ProblemRenderer problemRenderer = new ProblemRenderer();
    
    private static enum Command {
        ADD, EDIT, DELETE, POV
    };
    
    private Button btnAdd;
    
    private Button btnEdit;
    
    private Button btnDelete;
    
    private Button btnPOV;
    
    private Menuitem mnuAdd;
    
    private Menuitem mnuEdit;
    
    private Menuitem mnuDelete;
    
    private Menuitem mnuPOV;
    
    private Menuitem mnuSetFilter;
    
    private Listbox lbProblems;
    
    private Combobox cboFilter;
    
    private String probEvent;
    
    private boolean allowAsync;
    
    private boolean allowAddPov = true;
    
    private boolean useLexicon = true;
    
    private boolean hideButtons;
    
    private ProblemFilter defaultFilter;
    
    private boolean m_bPersHistAndAct;
    
    private Listheader colSort;
    
    private boolean m_bNoRefresh;
    
    private BgoSecurity bgoSecurity;
    
    private final List<Problem> problemList = new ArrayList<>();
    
    private final List<Problem> selectedProblems = new ArrayList<>();
    
    private final IPatientContextEvent patientContextEventHandler = new IPatientContextEvent() {
        
        @Override
        public String pending(boolean silent) {
            return null;
        }
        
        @Override
        public void committed() {
            IEventManager eventManager = EventManager.getInstance();
            
            if (probEvent != null) {
                eventManager.unsubscribe(probEvent, genericEventHandler);
            }
            
            Patient patient = PatientContext.getActivePatient();
            probEvent = patient == null ? null : "PCC." + patient.getIdElement().getIdPart() + ".PRB";
            
            if (probEvent != null) {
                eventManager.subscribe(probEvent, genericEventHandler);
            }
            
            setFilter(defaultFilter);
            loadProblems(false);
        }
        
        @Override
        public void canceled() {
        }
        
    };
    
    private final IEncounterContextEvent encounterContextEventHandler = new IEncounterContextEvent() {
        
        @Override
        public String pending(boolean silent) {
            return null;
        }
        
        @Override
        public void committed() {
            updateControls();
        }
        
        @Override
        public void canceled() {
        }
        
    };
    
    private final IGenericEvent<Object> genericEventHandler = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            if (eventName.equals(probEvent)) {
                refresh();
            }
        }
    };
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onLoad(PluginContainer)
     */
    @Override
    public void onLoad(final PluginContainer container) {
        log.trace("Plugin Loaded");
        container.registerProperties(this, "allowAsync", "allowAddPov", "hideButtons", "useLexicon");
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onUnload()
     */
    @Override
    public void onUnload() {
        log.trace("Plugin Unloaded");
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onActivate()
     */
    @Override
    public void onActivate() {
        log.trace("Plugin Activated");
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onInactivate()
     */
    @Override
    public void onInactivate() {
        log.trace("Plugin Deactivated");
    }
    
    /**
     * @see org.carewebframework.ui.FrameworkController#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        bgoSecurity = BgoUtil.initSecurity("BGO DISABLE PROB LIST EDITING", "BGOZ PROBLEM LIST EDIT");
        lbProblems.setItemRenderer(problemRenderer);
        RowComparator.autowireColumnComparators(lbProblems.getListhead().getChildren());
        m_bPersHistAndAct = SecurityUtil.isGranted("BGO PL INCLUDE PERS HIST W ACT");
        
        for (Object item : cboFilter.getItems()) {
            Comboitem ci = (Comboitem) item;
            ProblemFilter filter = ProblemFilter.valueOf(ci.getValue().toString());
            ci.setValue(m_bPersHistAndAct && filter == ProblemFilter.ACTIVE ? ProblemFilter.ACTIVE_PERSONAL : filter);
        }
        
        setFilter(getDefaultFilter());
        getAppFramework().registerObject(patientContextEventHandler);
        getAppFramework().registerObject(encounterContextEventHandler);
        patientContextEventHandler.committed();
        log.trace("Controller composed");
    }
    
    private ProblemFilter getDefaultFilter() {
        if (defaultFilter == null) {
            int i = NumberUtils.toInt((VistAUtil.getSysParam("BGO PL DEFAULT FILTER", "0", null)));
            defaultFilter = i < 0 || i >= ProblemFilter.values().length ? ProblemFilter.NONE : ProblemFilter.values()[i];
        }
        
        return defaultFilter;
    }
    
    private void setDefaultFilter(ProblemFilter filter) {
        if (filter == ProblemFilter.ACTIVE_PERSONAL) {
            filter = ProblemFilter.ACTIVE;
        }
        
        if (filter != null) {
            VistAUtil.setSysParam("BGO PL DEFAULT FILTER", Integer.toString(filter.ordinal()));
        }
    }
    
    private void updateControls() {
        boolean b = PatientContext.getActivePatient() == null || !bgoSecurity.verifyWriteAccess(true);
        btnAdd.setDisabled(b);
        btnEdit.setDisabled(b || lbProblems.getSelectedCount() == 0);
        btnDelete.setDisabled(btnEdit.isDisabled());
        mnuAdd.setDisabled(btnAdd.isDisabled());
        mnuEdit.setDisabled(btnEdit.isDisabled());
        mnuDelete.setDisabled(btnDelete.isDisabled());
        btnPOV.setDisabled(true); //oEncounter.Prepare(ofNotLocked Or ofValidateOnly)
        btnPOV.setVisible(allowAddPov);
        mnuPOV.setDisabled(btnPOV.isDisabled());
        mnuPOV.setVisible(btnPOV.isVisible());
        mnuSetFilter.setDisabled(cboFilter.getSelectedIndex() == -1);
    }
    
    private void loadProblems(boolean noAsync) {
        lbProblems.getItems().clear();
        getAsyncDispatcher().abort();
        Patient patient = PatientContext.getActivePatient();
        
        if (patient == null) {
            return;
        }
        
        EventUtil.status("Loading Problem List Data");
        
        if (allowAsync && !noAsync) {
            getAsyncDispatcher().callRPCAsync("BGOPROB GET", patient.getIdElement().getIdPart());
        } else {
            loadProblems(getBroker().callRPCList("BGOPROB GET", null, patient.getIdElement().getIdPart()));
        }
        
        EventUtil.status();
    }
    
    private void loadProblems(List<String> data) {
        problemList.clear();
        
        try {
            if (data == null || data.isEmpty()) {
                return;
            }
            
            PCC.errorCheck(data);
            
            for (String s : data) {
                problemList.add(new Problem(s));
            }
        } finally {
            refreshList();
        }
    }
    
    private Problem problemFromRow(int row) {
        Listitem item = lbProblems.getItemAtIndex(row);
        lbProblems.renderItem(item);
        return (Problem) item.getValue();
    }
    
    private void addProblem() {
        AddProblemController.execute(null);
    }
    
    private String deleteProblem(int row) {
        Problem problem = problemFromRow(row);
        
        if (PromptDialog.confirm("Are you sure that you wish to delete the problem titled:" + StrUtil.CRLF2
                + problem.getNumberCode() + " - " + problem.getProviderNarrative(),
            "Delete Problem?")) {
            return getBroker().callRPC("BGOPROB DEL", problem.getId().getIdPart());
        }
        
        return null;
    }
    
    private String editProblem(int row) {
        AddProblemController.execute(problemFromRow(row));
        return null;
    }
    
    @Override
    public void onAsyncRPCComplete(AsyncRPCCompleteEvent event) {
        loadProblems(StrUtil.toList(event.getData(), "\r"));
    }
    
    @Override
    public void onAsyncRPCError(AsyncRPCErrorEvent event) {
        // TODO: do something with this
    }
    
    @Override
    public void refresh() {
        if (!m_bNoRefresh) {
            saveGridState();
            loadProblems(true);
            restoreGridState();
        }
    }
    
    private void refreshList() {
        ProblemFilter filter = getFilter();
        boolean bHasPriority = false;
        lbProblems.setModel((ListModelList<?>) null);
        ListModelList<Problem> model = new ListModelList<>();
        
        for (Problem problem : problemList) {
            if (filter.include(problem)) {
                model.add(problem);
                bHasPriority |= problem.getPriority().length() > 0;
            }
        }
        
        if (colSort == null) {
            colSort = (Listheader) lbProblems.getListhead().getChildren().get(bHasPriority ? 4 : 0);
        }
        
        lbProblems.setModel(model);
        sortProblems();
        updateControls();
        Events.echoEvent("onResize", lbProblems, null);
    }
    
    private ProblemFilter getFilter() {
        return (ProblemFilter) cboFilter.getSelectedItem().getValue();
    }
    
    private void setFilter(ProblemFilter filter) {
        for (Object item : cboFilter.getItems()) {
            Comboitem ci = (Comboitem) item;
            
            if (ci.getValue() == filter) {
                cboFilter.setSelectedItem(ci);
                break;
            }
        }
    }
    
    private void doCommand(Command cmd) {
        if (!bgoSecurity.verifyWriteAccess(false)) {
            return;
        }
        
        if (cmd == Command.ADD) {
            addProblem();
            return;
        }
        
        m_bNoRefresh = true;
        String status = null;
        
        try {
            for (int i = 0; i < lbProblems.getItemCount(); i++) {
                Listitem li = lbProblems.getItemAtIndex(i);
                
                if (li.isSelected()) {
                    switch (cmd) {
                        case EDIT:
                            status = editProblem(i);
                            break;
                        
                        case DELETE:
                            status = deleteProblem(i);
                            break;
                        
                        case POV:
                            //status = addPOV(...)
                            break;
                    }
                    
                    PCC.errorCheck(status);
                }
            }
        } finally {
            m_bNoRefresh = false;
            refresh();
        }
    }
    
    private void saveGridState() {
        selectedProblems.clear();
        
        for (Object object : lbProblems.getSelectedItems()) {
            Listitem item = (Listitem) object;
            lbProblems.renderItem(item);
            selectedProblems.add((Problem) item.getValue());
        }
    }
    
    private void restoreGridState() {
        lbProblems.clearSelection();
        
        for (Object object : lbProblems.getItems()) {
            Listitem item = (Listitem) object;
            lbProblems.renderItem(item);
            
            if (selectedProblems.contains(item.getValue())) {
                item.setSelected(true);
            }
        }
        
        selectedProblems.clear();
        updateControls();
    }
    
    private void sortProblems() {
        if (colSort != null) {
            boolean asc = "ascending".equals(colSort.getSortDirection());
            colSort.sort(asc, true);
        }
    }
    
    public void onClick$btnAdd() {
        doCommand(Command.ADD);
    }
    
    public void onClick$btnEdit() {
        doCommand(Command.EDIT);
    }
    
    public void onClick$btnDelete() {
        doCommand(Command.DELETE);
    }
    
    public void onClick$btnPOV() {
        doCommand(Command.POV);
    }
    
    public void onClick$mnuSetFilter() {
        setDefaultFilter(getFilter());
    }
    
    public void onDoubleClick$lbProblems() {
        if (!btnEdit.isDisabled()) {
            doCommand(Command.EDIT);
        }
    }
    
    public void onSelect$cboFilter() {
        refreshList();
    }
    
    public void onSelect$lbProblems() {
        updateControls();
    }
    
    public void onResize$lbProblems() {
        Clients.resize(lbProblems);
    }
    
    public void onSort$lbProblems(Event event) {
        event = ZKUtil.getEventOrigin(event);
        colSort = (Listheader) event.getTarget();
    }
    
    public boolean getAllowAsync() {
        return allowAsync;
    }
    
    public void setAllowAsync(boolean allowAsync) {
        this.allowAsync = allowAsync;
    }
    
    public boolean getAllowAddPov() {
        return allowAddPov;
    }
    
    public void setAllowAddPov(boolean allowAddPov) {
        this.allowAddPov = allowAddPov;
    }
    
    public boolean getUseLexicon() {
        return useLexicon;
    }
    
    public void setUseLexicon(boolean useLexicon) {
        this.useLexicon = useLexicon;
    }
    
    public boolean getHideButtons() {
        return hideButtons;
    }
    
    public void setHideButtons(boolean hideButtons) {
        this.hideButtons = hideButtons;
        btnAdd.setVisible(!hideButtons);
        btnEdit.setVisible(!hideButtons);
        btnDelete.setVisible(!hideButtons);
        btnPOV.setVisible(!hideButtons);
    }
}

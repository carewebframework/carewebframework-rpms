/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.problemlist.controller;

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.common.bgo.BgoBaseController;
import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.common.bgo.BrowserController;
import gov.ihs.cwf.common.bgo.WebSearchController;
import gov.ihs.cwf.context.EncounterContext.IEncounterContextEvent;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.context.PatientContext.IPatientContextEvent;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Problem;
import gov.ihs.cwf.mbroker.BrokerSession.IAsyncRPCEvent;
import gov.ihs.cwf.ui.problemlist.render.ProblemRenderer;
import gov.ihs.cwf.ui.problemlist.util.ProblemFilter;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;

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
    
    private int asyncHandle;
    
    private boolean allowAsync;
    
    private boolean allowAddPov = true;
    
    private boolean useLexicon = true;
    
    private boolean hideButtons;
    
    private ProblemFilter defaultFilter;
    
    private boolean m_bPersHistAndAct;
    
    private Listheader colSort;
    
    private boolean m_bNoRefresh;
    
    private final List<Problem> problemList = new ArrayList<Problem>();
    
    private final List<Problem> selectedProblems = new ArrayList<Problem>();
    
    private final IAsyncRPCEvent asyncRPCEventHandler = new IAsyncRPCEvent() {
        
        @Override
        public void onRPCComplete(int handle, String data) {
            if (handle == asyncHandle) {
                asyncHandle = 0;
                loadProblems(StrUtil.toList(data, "\r"));
            }
        }
        
        @Override
        public void onRPCError(int handle, int code, String text) {
            
        }
    };
    
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
            
            Patient patient = PatientContext.getCurrentPatient();
            probEvent = patient == null ? null : "PCC." + patient.getDomainId() + ".PRB";
            
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
        BgoUtil.initSecurity("BGO DISABLE PROB LIST EDITING", "BGOZ PROBLEM LIST EDIT");
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
            int i = NumberUtils.toInt((BgoUtil.getSysParam("BGO PL DEFAULT FILTER", "0", null)));
            defaultFilter = i < 0 || i >= ProblemFilter.values().length ? ProblemFilter.NONE : ProblemFilter.values()[i];
        }
        
        return defaultFilter;
    }
    
    private void setDefaultFilter(ProblemFilter filter) {
        if (filter == ProblemFilter.ACTIVE_PERSONAL) {
            filter = ProblemFilter.ACTIVE;
        }
        
        if (filter != null) {
            BgoUtil.setSysParam("BGO PL DEFAULT FILTER", Integer.toString(filter.ordinal()));
        }
    }
    
    private void updateControls() {
        boolean b = PatientContext.getCurrentPatient() == null || !BgoUtil.checkSecurity(true);
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
        abortAsync();
        Patient patient = PatientContext.getCurrentPatient();
        
        if (patient == null) {
            return;
        }
        
        EventUtil.status("Loading Problem List Data");
        
        if (allowAsync && !noAsync) {
            asyncHandle = getBroker().callRPCAsync("BGOPROB GET", asyncRPCEventHandler, patient.getDomainId(), true);
        } else {
            loadProblems(getBroker().callRPCList("BGOPROB GET", null, patient.getDomainId()));
        }
        
        EventUtil.status();
    }
    
    private void loadProblems(List<String> data) {
        problemList.clear();
        
        try {
            if (data == null || data.isEmpty()) {
                return;
            }
            
            BgoUtil.errorCheck(data);
            
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
        
        if (PromptDialog.confirm(
            "Are you sure that you wish to delete the problem titled:" + StrUtil.CRLF2 + problem.getNumberCode() + " - "
                    + problem.getProviderNarrative(), "Delete Problem?")) {
            return getBroker().callRPC("BGOPROB DEL", problem.getDomainId());
        }
        
        return null;
    }
    
    private String editProblem(int row) {
        AddProblemController.execute(problemFromRow(row));
        return null;
    }
    
    private void abortAsync() {
        if (asyncHandle != 0) {
            RPMSUtil.getBrokerSession().callRPCAbort(asyncHandle);
            asyncHandle = 0;
        }
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
        ListModelList<Problem> model = new ListModelList<Problem>();
        
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
        if (!BgoUtil.checkSecurity(false)) {
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
                    
                    BgoUtil.errorCheck(status);
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
    
    public void onClick$btnInfo() {
        Listitem item = lbProblems.getSelectedItem();
        
        if (item == null) {
            WebSearchController.execute("");
        } else {
            Problem problem = (Problem) item.getValue();
            BrowserController.execute(problem.getProviderNarrative(), null, problem.getIcd9Code(), true);
        }
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

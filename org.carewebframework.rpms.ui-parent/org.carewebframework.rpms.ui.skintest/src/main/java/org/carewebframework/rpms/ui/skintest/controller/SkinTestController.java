/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.skintest.controller;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.model.dstu.resource.Patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.encounter.EncounterContext.IEncounterContextEvent;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.cal.api.patient.PatientContext.IPatientContextEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.rpms.ui.skintest.model.TestItem;
import org.carewebframework.rpms.ui.skintest.render.SkinTestRenderer;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.ui.mbroker.AsyncRPCCompleteEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCErrorEvent;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;

/**
 */
public class SkinTestController extends BgoBaseController<Object> implements IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(SkinTestController.class);
    
    private static final SkinTestRenderer skinTestRenderer = new SkinTestRenderer();
    
    private static enum Command {
        ADD, EDIT, DELETE
    };
    
    public static enum EventType {
        CURRENT, HISTORICAL, REFUSAL
    }
    
    private Button btnAdd;
    
    private Button btnEdit;
    
    private Button btnDelete;
    
    private Menuitem mnuAdd;
    
    private Menuitem mnuEdit;
    
    private Menuitem mnuDelete;
    
    private Menuitem mnuVisitDetail;
    
    private Listbox lbTests;
    
    private String pccEvent;
    
    private String refusalEvent;
    
    private boolean allowAsync;
    
    private boolean hideButtons;
    
    private Listheader colSort;
    
    private boolean noRefresh;
    
    private final List<TestItem> skinTestList = new ArrayList<TestItem>();
    
    private Object selectedItem;
    
    private final IPatientContextEvent patientContextEventHandler = new IPatientContextEvent() {
        
        @Override
        public String pending(boolean silent) {
            return null;
        }
        
        @Override
        public void committed() {
            IEventManager eventManager = EventManager.getInstance();
            
            if (pccEvent != null) {
                eventManager.unsubscribe(pccEvent, genericEventHandler);
                eventManager.unsubscribe(refusalEvent, genericEventHandler);
            }
            
            Patient patient = PatientContext.getActivePatient();
            pccEvent = patient == null ? null : "PCC." + patient.getId().getIdPart() + ".SK";
            refusalEvent = patient == null ? null : "REFUSAL." + patient.getId().getIdPart() + ".SKIN TEST";
            
            if (pccEvent != null) {
                eventManager.subscribe(pccEvent, genericEventHandler);
                eventManager.subscribe(refusalEvent, genericEventHandler);
            }
            
            loadSkinTests(false);
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
            if (eventName.equals(pccEvent)) {
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
        container.registerProperties(this, "allowAsync", "hideButtons");
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
        BgoUtil.initSecurity("BGO DISABLE SK EDITING", null);
        lbTests.setItemRenderer(skinTestRenderer);
        RowComparator.autowireColumnComparators(lbTests.getListhead().getChildren());
        getAppFramework().registerObject(patientContextEventHandler);
        getAppFramework().registerObject(encounterContextEventHandler);
        patientContextEventHandler.committed();
        log.trace("Controller composed");
    }
    
    private void updateControls() {
        boolean b = PatientContext.getActivePatient() == null || !BgoUtil.checkSecurity(true);
        TestItem test = getSelectedTest();
        boolean locked = test == null ? true : test.isLocked();
        boolean pending = test == null ? false : test.isPending();
        
        btnAdd.setDisabled(b);
        btnEdit.setDisabled(b || locked || !pending);
        btnDelete.setDisabled(locked);
        mnuAdd.setDisabled(btnAdd.isDisabled());
        mnuEdit.setDisabled(btnEdit.isDisabled());
        mnuDelete.setDisabled(btnDelete.isDisabled());
        mnuVisitDetail.setDisabled(test == null || test.getEncounter() == null);
    }
    
    private void loadSkinTests(boolean noAsync) {
        lbTests.getItems().clear();
        getAsyncDispatcher().abort();
        Patient patient = PatientContext.getActivePatient();
        
        if (patient == null) {
            return;
        }
        
        EventUtil.status("Loading Skin Test Data");
        
        if (allowAsync && !noAsync) {
            getAsyncDispatcher().callRPCAsync("BGOVSK GET", patient.getId().getIdPart());
        } else {
            loadSkinTests(getBroker().callRPCList("BGOVSK GET", null, patient.getId().getIdPart()));
        }
        
        EventUtil.status();
    }
    
    private void loadSkinTests(List<String> data) {
        skinTestList.clear();
        
        try {
            if (data == null || data.isEmpty()) {
                return;
            }
            
            PCC.errorCheck(data);
            
            for (String s : data) {
                skinTestList.add(new TestItem(s));
            }
            
        } finally {
            refreshList();
        }
    }
    
    private TestItem getSelectedTest() {
        Listitem item = lbTests.getSelectedItem();
        
        if (item != null) {
            lbTests.renderItem(item);
            return (TestItem) item.getValue();
        } else {
            return null;
        }
    }
    
    private void addTest() {
        AddSkinTestController.execute(null);
    }
    
    private void deleteTest() {
        TestItem test = getSelectedTest();
        
        if (test != null) {
            test.delete();
        }
        
    }
    
    private void editTest() {
        AddSkinTestController.execute(getSelectedTest());
        return;
    }
    
    @Override
    public void refresh() {
        if (!noRefresh) {
            saveGridState();
            loadSkinTests(true);
            restoreGridState();
        }
    }
    
    @Override
    public void onAsyncRPCComplete(AsyncRPCCompleteEvent event) {
        loadSkinTests(StrUtil.toList(event.getData(), "\r"));
    }
    
    @Override
    public void onAsyncRPCError(AsyncRPCErrorEvent event) {
        // TODO: do something with this
    }
    
    private void refreshList() {
        lbTests.setModel((ListModelList<?>) null);
        ListModelList<TestItem> model = new ListModelList<TestItem>(skinTestList);
        
        if (colSort == null) {
            colSort = (Listheader) lbTests.getListhead().getChildren().get(0);
        }
        
        lbTests.setModel(model);
        sortTests();
        updateControls();
        Events.echoEvent("onResize", lbTests, null);
    }
    
    private void doCommand(Command cmd) {
        if (!BgoUtil.checkSecurity(false)) {
            return;
        }
        
        switch (cmd) {
            case ADD:
                addTest();
                break;
            
            case EDIT:
                editTest();
                break;
            
            case DELETE:
                deleteTest();
                break;
        
        }
    }
    
    private void saveGridState() {
        selectedItem = getSelectedTest();
    }
    
    private void restoreGridState() {
        if (selectedItem != null) {
            lbTests.setSelectedIndex(ListUtil.findListboxData(lbTests, selectedItem));
            selectedItem = null;
        }
        
        updateControls();
    }
    
    private void sortTests() {
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
    
    public void onClick$btnPrint() {
        String s = VistAUtil.concatParams(PatientContext.getActivePatient().getId().getIdPart(), 2);
        s = getBroker().callRPC("BGOVIMM PRINT", s);
        PromptDialog.showText(s, "Print Record");
    }
    
    public void onDoubleClick$lbTests() {
        if (!btnEdit.isDisabled()) {
            doCommand(Command.EDIT);
        }
    }
    
    public void onSelect$lbTests() {
        updateControls();
    }
    
    public void onResize$lbTests() {
        Clients.resize(lbTests);
    }
    
    public void onSort$lbTests(Event event) {
        event = ZKUtil.getEventOrigin(event);
        colSort = (Listheader) event.getTarget();
    }
    
    public boolean getAllowAsync() {
        return allowAsync;
    }
    
    public void setAllowAsync(boolean allowAsync) {
        this.allowAsync = allowAsync;
    }
    
    public boolean getHideButtons() {
        return hideButtons;
    }
    
    public void setHideButtons(boolean hideButtons) {
        this.hideButtons = hideButtons;
        btnAdd.setVisible(!hideButtons);
        btnEdit.setVisible(!hideButtons);
        btnDelete.setVisible(!hideButtons);
    }
}

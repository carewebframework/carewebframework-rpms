/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection.v1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.shell.help.HelpUtil;
import org.carewebframework.common.DateRange;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.DateRangePicker;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.patientlist.FavoritePatientList;
import gov.ihs.cwf.patientlist.IPatientList;
import gov.ihs.cwf.patientlist.IPatientListFilterManager;
import gov.ihs.cwf.patientlist.IPatientListFilterManager.FilterCapability;
import gov.ihs.cwf.patientlist.IPatientListItemManager;
import gov.ihs.cwf.patientlist.PatientListException;
import gov.ihs.cwf.patientlist.AbstractPatientListFilter;
import gov.ihs.cwf.patientlist.PatientListItem;
import gov.ihs.cwf.patientlist.IPatientListRegistry;
import gov.ihs.cwf.ui.patientselection.Constants;
import gov.ihs.cwf.ui.patientselection.IPatientDetailRenderer;
import gov.ihs.cwf.ui.patientselection.PatientDetailRenderer;
import gov.ihs.cwf.ui.patientselection.PatientListFilterRenderer;
import gov.ihs.cwf.ui.patientselection.PatientListItemRenderer;
import gov.ihs.cwf.ui.patientselection.PatientSearch;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

/**
 * Controller for patient selection dialog.
 * 
 * @author dmartin
 */
public class PatientSelectionController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(PatientSelectionController.class);
    
    private static final String ATTR_PATIENT_LIST = "list";
    
    private static final String FILTER_DROP_ID = "patientselection.filter.drop.id";
    
    private static final String[] DATE_RANGES = { "Next Year|T+365|T", "Next Month|T+30|T", "Next Week|T+7|T",
            "Tomorrow|T+1|T+1", "Today|T|T|1", "Yesterday|T-1|T-1", "Last Week|T|T-7", "Last Month|T|T-30",
            "Last Year|T|T-365" };
    
    private final String LABEL_DATE_RANGE = Labels.getLabel("patientselection.daterange.label");
    
    private final String WARN_NO_FILTERS = Labels.getLabel("patientselection.warn.no.filters");
    
    private final String WARN_NO_PATIENTS = Labels.getLabel("patientselection.warn.no.patients");
    
    private final String WARN_NO_LIST_SELECTED = Labels.getLabel("patientselection.warn.no.list.selected");
    
    private final String EAST_TITLE_DEMO = Labels.getLabel("patientselection.right.pane.title.demo");
    
    private final String EAST_TITLE_MANAGE = Labels.getLabel("patientselection.right.pane.title.manage");
    
    private final String FILTER_RENAME_TITLE = Labels.getLabel("patientselection.filter.rename.title");
    
    private final String FILTER_NEW_TITLE = Labels.getLabel("patientselection.filter.new.title");
    
    private final String FILTER_NAME_PROMPT = Labels.getLabel("patientselection.filter.name.prompt");
    
    private final String FILTER_DELETE_TITLE = Labels.getLabel("patientselection.filter.deletion.confirm.title");
    
    private final String FILTER_DELETE_PROMPT = Labels.getLabel("patientselection.filter.deletion.confirm.prompt");
    
    private final String SEARCH_MESSAGE = Labels.getLabel("patientselection.search.message");
    
    private Radiogroup rgrpLists;
    
    private Window root;
    
    private Listbox lstFilter;
    
    private Label lblDateRange;
    
    private DateRangePicker drpDateRange;
    
    private Button btnManageList;
    
    private Button btnFavorite;
    
    private Textbox edtSearch;
    
    private Listbox lstSearch;
    
    private Label lblPatientList;
    
    private Listbox lstPatientList;
    
    private Component pnlDemographics;
    
    private Component pnlDemoRoot;
    
    private Button btnDemoDetail;
    
    private Timer timer;
    
    private Component pnlManagedList;
    
    private Component pnlManagedListFilters;
    
    private Listbox lstManagedListFilter;
    
    private Button btnManagedListFilterNew;
    
    private Button btnManagedListFilterRename;
    
    private Button btnManagedListFilterDelete;
    
    private Component pnlManagedListItems;
    
    private Label lblManagedList;
    
    private Button btnManagedListAdd;
    
    private Button btnManagedListImport;
    
    private Button btnManagedListAddCurrent;
    
    private Button btnManagedListRemove;
    
    private Button btnManagedListRemoveAll;
    
    private Listbox lstManagedList;
    
    private Button btnOK;
    
    private LayoutRegion rgnEast;
    
    private IPatientListRegistry registry;
    
    private IPatientList activeList;
    
    private IPatientList managedList;
    
    private IPatientList originalList;
    
    private IPatientListItemManager itemManager;
    
    private IPatientListFilterManager filterManager;
    
    private AbstractPatientListFilter activeFilter;
    
    private FavoritePatientList favorites;
    
    private Patient activePatient;
    
    private boolean manageListMode;
    
    private DateRange defaultDateRange;
    
    private final List<PatientListItem> pendingListItem = new ArrayList<PatientListItem>();
    
    private IPatientDetailRenderer patientDetailRenderer = new PatientDetailRenderer();
    
    /**
     * Handles drag/drop events for filters in filter management mode.
     */
    private final EventListener<Event> filterDropListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            DropEvent dropEvent = (DropEvent) ZKUtil.getEventOrigin(event);
            Listitem dragged = (Listitem) dropEvent.getDragged();
            Listitem target = (Listitem) dropEvent.getTarget();
            filterManager.moveFilter((AbstractPatientListFilter) dragged.getValue(), target.getIndex());
            dragged.getListbox().insertBefore(dragged, target);
        }
    };
    
    /**
     * Initial setup.
     * 
     * @throws Exception
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = (Window) comp;
        initDateRanges();
        initRegisteredLists();
        initRenderers();
        HelpUtil.associateCSH(root, "patientSelectionNewHelp", null, null);
    }
    
    /**
     * Initialize the date ranges to be used for filtering lists.
     */
    private void initDateRanges() {
        drpDateRange.loadChoices(DATE_RANGES);
        defaultDateRange = drpDateRange.getSelectedRange();
    }
    
    /**
     * Loads the registered lists into the radio group.
     */
    private void initRegisteredLists() {
        for (IPatientList list : registry)
            if (!list.isDisabled()) {
                Radio radio = new Radio(list.getName());
                radio.setAttribute(ATTR_PATIENT_LIST, list);
                rgrpLists.appendChild(radio);
                
                if (list == favorites)
                    radio.setId("radFavorites");
            }
        
        rgrpLists.setSelectedIndex(0);
        pendingListItem.add(new PatientListItem(null, "Building list, please wait..."));
    }
    
    /**
     * Initializes renderers for list boxes.
     */
    private void initRenderers() {
        lstPatientList.setItemRenderer(PatientListItemRenderer.getInstance());
        lstSearch.setItemRenderer(PatientListItemRenderer.getInstance());
        lstManagedList.setItemRenderer(PatientListItemRenderer.getInstance());
        lstFilter.setItemRenderer(PatientListFilterRenderer.getInstance());
        lstManagedListFilter.setItemRenderer(PatientListFilterRenderer.getInstance());
    }
    
    /**
     * Returns the renderer for the patient detail view.
     * 
     * @return Patient detail renderer.
     */
    public IPatientDetailRenderer getPatientDetailRenderer() {
        return patientDetailRenderer;
    }
    
    /**
     * Sets the renderer for the patient detail view.
     * 
     * @param patientDetailRenderer
     */
    public void setPatientDetailRenderer(IPatientDetailRenderer patientDetailRenderer) {
        this.patientDetailRenderer = patientDetailRenderer;
    }
    
    /**
     * Sets the specified list as active.
     * 
     * @param list
     */
    private void setActiveList(IPatientList list) {
        activeList = list;
        activeFilter = null;
        btnFavorite.setVisible(list != this.favorites);
        boolean hasDateRange = (list != null && list.isDateRangeRequired());
        lblDateRange.setVisible(hasDateRange);
        drpDateRange.setVisible(hasDateRange);
        
        if (hasDateRange) {
            DateRange range = list.getDateRange();
            
            if (range == null) {
                range = defaultDateRange;
                list.setDateRange(range);
            }
            
            Comboitem item = drpDateRange.findMatchingItem(range);
            item = item == null ? drpDateRange.addChoice(range, true) : item;
            drpDateRange.setSelectedItem(item);
            lblDateRange.setValue(MessageFormat.format(LABEL_DATE_RANGE, list.getEntityName()));
        }
        
        refreshFilterList();
        refreshPatientList();
        updateControls();
    }
    
    private void refreshFilterList() {
        boolean hasFilter = activeList != null && activeList.isFiltered();
        lstFilter.setVisible(hasFilter);
        
        if (hasFilter) {
            activeFilter = activeList.getActiveFilter();
            Collection<AbstractPatientListFilter> filters = activeList.getFilters();
            
            if (filters == null || filters.isEmpty()) {
                lstFilter.setModel((ListModelList<?>) null);
                lstFilter.getItems().clear();
                lstFilter.appendItem(WARN_NO_FILTERS, null);
            } else {
                lstFilter.setModel(new ListModelList<AbstractPatientListFilter>(filters));
                
                if (activeFilter == null) {
                    activeFilter = filters.iterator().next();
                    activeList.setActiveFilter(activeFilter);
                }
            }
            
            selectFilter(lstFilter, activeFilter);
        }
    }
    
    /**
     * Selects the list box item corresponding to the specified filter.
     * 
     * @param lb List box to search.
     * @param filter The filter whose associated list item is to be selected.
     * @return True if the item was successfully selected.
     */
    private boolean selectFilter(Listbox lb, AbstractPatientListFilter filter) {
        if (filter != null)
            for (Object object : lb.getItems()) {
                Listitem item = (Listitem) object;
                lb.renderItem(item);
                AbstractPatientListFilter flt = (AbstractPatientListFilter) item.getValue();
                
                if (flt != null && filter.equals(flt)) {
                    lb.setSelectedItem(item);
                    Clients.scrollIntoView(item);
                    return true;
                }
            }
        
        return false;
    }
    
    private void refreshPatientList() {
        timer.stop();
        
        if (activeList != null) {
            Collection<PatientListItem> items;
            
            if (activeList.isPending()) {
                items = pendingListItem;
                timer.start();
            } else
                items = activeList.getListItems();
            
            ListModelList<PatientListItem> model = items == null ? new ListModelList<PatientListItem>()
                    : new ListModelList<PatientListItem>(items);
            
            if (model.isEmpty())
                model.add(new PatientListItem(null, WARN_NO_PATIENTS));
            
            lstPatientList.setModel(model);
            lblPatientList.setValue(activeList.getDisplayName());
        } else {
            lstPatientList.setModel((ListModel<?>) null);
            lblPatientList.setValue(WARN_NO_LIST_SELECTED);
        }
        
        setActivePatient((Patient) null);
    }
    
    private void setActiveFilter(AbstractPatientListFilter filter) {
        activeFilter = filter;
        activeList.setActiveFilter(filter);
        
        if (drpDateRange.isVisible())
            setActiveDateRange(drpDateRange.getSelectedRange());
        else
            refreshPatientList();
    }
    
    private void setActiveDateRange(DateRange range) {
        if (range != null) {
            activeList.setDateRange(range);
            refreshFilterList();
            refreshPatientList();
        }
    }
    
    /**
     * Sets the active patient based on an event.
     * 
     * @param event
     */
    public void setActivePatient(Event event) {
        PatientListItem pli = getItem(event);
        setActivePatient(pli == null ? null : pli.getPatient());
    }
    
    private void setActivePatient(Patient patient) {
        // Build the demographic display here
        activePatient = patient;
        root.setAttribute(Constants.SELECTED_PATIENT_ATTRIB, activePatient);
        ZKUtil.detachChildren(pnlDemoRoot);
        
        if (patient != null && patientDetailRenderer != null)
            patientDetailRenderer.render(pnlDemoRoot, patient, this);
        
        btnDemoDetail.setDisabled(activePatient == null);
        updateControls();
    }
    
    /**
     * Called by Spring to finish initialization.
     */
    public void init() {
    }
    
    /**
     * Search for matching patients based on user input.
     */
    private void doSearch() {
        log.trace("Start doSearch()");
        Clients.clearBusy();
        displaySearchMessage(null);
        
        try {
            List<Patient> matches = PatientSearch.execute(edtSearch.getValue(), 100);
            
            if (matches != null) 
                lstSearch.setModel(new ListModelList<Patient>(matches));

            edtSearch.setFocus(true);
            edtSearch.select();
            return;
            
        } catch (final Exception e) {
            displaySearchMessage(e.getMessage());
        }
    }
    
    private void displaySearchMessage(String message) {
        lstSearch.clearSelection();
        lstSearch.setModel((ListModelList<?>) null);
        lstSearch.getItems().clear();
        
        if (message != null)
            lstSearch.appendItem(message, null).setTooltiptext(message);
        
        Clients.scrollIntoView(lstSearch.getFirstChild());
    }
    
    /**
     * Set the patient list registry (injected by Spring).
     * 
     * @param registry
     */
    public void setPatientListRegistry(IPatientListRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Set a reference to the favorites list (injected by Spring).
     * 
     * @param list
     */
    public void setFavoritesList(FavoritePatientList list) {
        this.favorites = list;
    }
    
    /**
     * Sets list management mode.
     * 
     * @param value If true, the dialog enters list management mode. If false, the dialog reverts to
     *            patient selection mode.
     */
    private void setManageListMode(boolean value) {
        manageListMode = value;
        pnlManagedList.setVisible(value);
        pnlDemographics.setVisible(!value);
        rgnEast.setTitle(MessageFormat.format(value ? EAST_TITLE_MANAGE : EAST_TITLE_DEMO, activeList.getName()));
        
        if (originalList != null)
            originalList.refresh();
        
        if (manageListMode) {
            originalList = activeList;
            managedList = activeList.copy();
            itemManager = managedList.getItemManager();
            filterManager = managedList.getFilterManager();
            pnlManagedListFilters.setVisible(filterManager != null);
            btnManagedListFilterNew.setVisible(filterManager != null && filterManager.hasCapability(FilterCapability.ADD));
            btnManagedListFilterDelete.setVisible(filterManager != null
                    && filterManager.hasCapability(FilterCapability.REMOVE));
            btnManagedListFilterRename.setVisible(filterManager != null
                    && filterManager.hasCapability(FilterCapability.RENAME));
            
            if (filterManager != null) {
                lstManagedListFilter.setModel(new ListModelList<AbstractPatientListFilter>(managedList.getFilters()));
                
                if (filterManager.hasCapability(FilterCapability.MOVE))
                    addDragDropSupport(lstManagedListFilter, FILTER_DROP_ID, filterDropListener);
            }
            
            pnlManagedListItems.setVisible(itemManager != null);
            lblManagedList.setVisible(itemManager != null);
            lstManagedList.setModel((ListModelList<?>) null);
            
            if (selectFilter(lstManagedListFilter, managedList.getActiveFilter()))
                managedListFilterChanged();
            
            pnlManagedList.invalidate();
        } else {
            originalList = null;
            managedList = null;
            itemManager = null;
            filterManager = null;
            setActiveList(activeList);
        }
        
        updateControls();
    }
    
    /**
     * Changes the active filter for the currently managed list.
     * 
     * @param filter
     */
    private void setManagedListFilter(AbstractPatientListFilter filter) {
        if (itemManager != null)
            itemManager.save();
        
        managedList.setActiveFilter(filter);
        managedListFilterChanged();
        
    }
    
    /**
     * Adds drag/drop support to the items belonging to the specified list box.
     * 
     * @param lb The list box.
     * @param dropId The drop id to be used.
     * @param eventListener The event listener to handle the drag/drop operations.
     */
    private void addDragDropSupport(Listbox lb, String dropId, EventListener<?> eventListener) {
        for (Object object : lb.getItems()) {
            Listitem item = (Listitem) object;
            item.setDraggable(dropId);
            item.setDroppable(dropId);
            item.addEventListener(Events.ON_DROP, eventListener);
        }
    }
    
    /**
     * Update control states.
     */
    private void updateControls() {
        if (manageListMode) {
            boolean filterSelected = lstManagedListFilter.getSelectedItem() != null;
            boolean patientSelected = lstManagedList.getSelectedItem() != null;
            btnManagedListFilterRename.setDisabled(!filterSelected);
            btnManagedListFilterDelete.setDisabled(!filterSelected);
            btnManagedListAddCurrent.setDisabled(!filterSelected || PatientContext.getCurrentPatient() == null);
            btnManagedListAdd.setDisabled(!filterSelected || activePatient == null);
            btnManagedListImport.setDisabled(!filterSelected || lstPatientList.getModel() == null);
            btnManagedListRemove.setDisabled(!patientSelected);
            btnManagedListRemoveAll.setDisabled(lstManagedList.getItemCount() == 0);
            btnOK.setDisabled(false);
            btnManageList.setDisabled(true);
        } else {
            btnManageList.setVisible(activeList != null);
            btnManageList.setDisabled(activeList == null
                    || (activeList.getItemManager() == null && activeList.getFilterManager() == null));
            btnOK.setDisabled(activePatient == null);
        }
    }
    
    /**
     * Adds the specified patient to the currently selected managed list.
     * 
     * @param patient
     * @param refresh
     */
    private void managedListAdd(Patient patient, boolean refresh) {
        if (patient != null)
            managedListAdd(new PatientListItem(patient, null), refresh);
    }
    
    private void managedListAdd(PatientListItem item, boolean refresh) {
        if (item != null && item.getPatient() != null) {
            itemManager.addItem(item);
            
            if (refresh)
                managedListRefresh();
        }
    }
    
    private void managedListRemove(PatientListItem item, boolean refresh) {
        if (item != null) {
            itemManager.removeItem(item);
            
            if (refresh)
                managedListRefresh();
        }
    }
    
    private void managedListRefresh() {
        lstManagedList.setModel(new ListModelList<PatientListItem>(managedList.getListItems()));
    }
    
    private void managedListFilterChanged() {
        if (itemManager != null) {
            itemManager.save();
            lstManagedList.setModel(new ListModelList<PatientListItem>(managedList.getListItems()));
            AbstractPatientListFilter filter = managedList.getActiveFilter();
            lblManagedList.setValue(managedList.getEntityName() + (filter == null ? "" : ": " + filter.getName()));
        }
        updateControls();
    }
    
    private AbstractPatientListFilter getFilter(Event event) {
        return getFilter((Listbox) ZKUtil.getEventOrigin(event).getTarget());
    }
    
    private AbstractPatientListFilter getFilter(Listbox lb) {
        return getFilter(lb.getSelectedItem());
    }
    
    private AbstractPatientListFilter getFilter(Listitem item) {
        return item == null ? null : (AbstractPatientListFilter) item.getValue();
    }
    
    private PatientListItem getItem(Event event) {
        return getItem((Listbox) ZKUtil.getEventOrigin(event).getTarget());
    }
    
    private PatientListItem getItem(Listbox lb) {
        return getItem(lb.getSelectedItem());
    }
    
    private PatientListItem getItem(Listitem item) {
        return item == null ? null : (PatientListItem) item.getValue();
    }
    
    /**
     * Adds or renames a filter.
     * 
     * @param filter If not null, assumes we are renaming an existing filter. If null, assumes we
     *            are adding a new filter.
     */
    private void addOrRenameFilter(AbstractPatientListFilter filter) {
        String errorMessage = "";
        boolean newFilter = filter == null;
        String oldName = newFilter ? null : filter.getName();
        
        while (true)
            try {
                String name = PromptDialog.input(errorMessage + FILTER_NAME_PROMPT, newFilter ? FILTER_NEW_TITLE
                        : FILTER_RENAME_TITLE, oldName);
                
                if (!StringUtils.isEmpty(name)) {
                    if (newFilter)
                        filter = filterManager.addFilter(name);
                    else
                        filterManager.renameFilter(filter, name);
                    
                    lstManagedListFilter.setModel(new ListModelList<AbstractPatientListFilter>(managedList.getFilters()));
                    selectFilter(lstManagedListFilter, filter);
                    setManagedListFilter(filter);
                }
                break;
                
            } catch (PatientListException e) {
                errorMessage = e.getMessage() + "\n";
            }
    }
    
    private void doClose() {
        if (manageListMode) {
            if (itemManager != null)
                itemManager.save();
            
            setManageListMode(false);
            return;
        }
        
        if (activePatient == null) {
            doCancel();
            return;
        }
        
        root.setVisible(false);
    }
    
    private void doCancel() {
        if (manageListMode) {
            setManageListMode(false);
        } else {
            root.removeAttribute(Constants.SELECTED_PATIENT_ATTRIB);
            root.setVisible(false);
        }
    }
    
    /* ================== Event Handlers ================== */
    
    /* ----------------- Dialog Control ------------------- */
    
    /**
     * If in list management mode, clicking the OK button will save pending changes to the managed
     * list and revert to patient selection mode. If in patient selection mode, clicking the OK
     * button will select the current patient into the shared context and close the dialog.
     */
    public void onClick$btnOK() {
        doClose();
    }
    
    /**
     * If in list management mode, clicking the cancel button will cancel pending changes to the
     * managed list and revert to patient selection mode. If in patient selection mode, clicking the
     * cancel button will close the dialog without further action.
     */
    public void onClick$btnCancel() {
        doCancel();
    }
    
    /**
     * Handles a deferred request to show the dialog.
     * 
     * @param event
     * @throws Exception
     */
    public void onShow(Event event) throws Exception {
        root.removeAttribute(Constants.SELECTED_PATIENT_ATTRIB);
        onCheck$rgrpLists();
        Events.echoEvent(Events.ON_FOCUS, root, null);
        
        if (!root.inModal())
            root.doModal();
    }
    
    /**
     * Handles a deferred request to set the focus to the search text box.
     */
    public void onFocus() {
        edtSearch.setFocus(true);
        edtSearch.select();
    }
    
    /* ------------------ List Control -------------------- */
    
    /**
     * When a radio button is selected, its associated patient list is activated.
     */
    public void onCheck$rgrpLists() {
        Radio radio = rgrpLists.getSelectedItem();
        
        if (radio == null) {
            radio = rgrpLists.getItemAtIndex(0);
            rgrpLists.setSelectedItem(radio);
        }
        
        IPatientList list = (IPatientList) radio.getAttribute(ATTR_PATIENT_LIST);
        setActiveList(list);
    }
    
    public void onTimer$timer() {
        if (activeList == null || !activeList.isPending()) {
            timer.stop();
            refreshPatientList();
        }
    }
    
    /**
     * When a filter is selected, make it the active filter for the active patient list.
     * 
     * @param event
     */
    public void onSelect$lstFilter(Event event) {
        setActiveFilter(getFilter(event));
    }
    
    /**
     * When the date range changes, make it the current date range for the active patient list.
     */
    public void onSelectRange$drpDateRange() {
        setActiveDateRange(drpDateRange.getSelectedRange());
    }
    
    /**
     * Enter list management mode when the manage button is clicked.
     */
    public void onClick$btnManageList() {
        setManageListMode(true);
    }
    
    /**
     * Add the active list to the favorites.
     */
    public void onClick$btnFavorite() {
        favorites.addFavorite(activeList);
    }
    
    /* ---------------- Patient Selection ------------------ */
    
    /**
     * Set the active patient when selected from the list.
     * 
     * @param event
     */
    public void onSelect$lstPatientList(Event event) {
        lstSearch.clearSelection();
        setActivePatient(event);
    }
    
    /**
     * Double-clicking a patient list item is the same as selecting it and then clicking the OK
     * button.
     * 
     * @param event
     */
    public void onDoubleClick$lstPatientList(Event event) {
        setActivePatient(event);
        
        if (activePatient != null)
            if (!manageListMode)
                doClose();
            else if (itemManager != null)
                managedListAdd(activePatient, true);
    }
    
    public void onSelect$lstSearch(Event event) {
        lstPatientList.clearSelection();
        setActivePatient(event);
    }
    
    public void onDoubleClick$lstSearch(Event event) {
        onDoubleClick$lstPatientList(event);
    }
    
    /* ----------------- Patient Search ------------------- */
    
    public void onClick$btnSearch() {
        Clients.showBusy(SEARCH_MESSAGE);
        displaySearchMessage(SEARCH_MESSAGE);
        Events.echoEvent("onSearch", root, null);
    }
    
    public void onOK$edtSearch() {
        onClick$btnSearch();
    }
    
    public void onSearch() {
        Clients.clearBusy();
        doSearch();
        edtSearch.setFocus(true);
    }
    
    /* ----------------- List Management ------------------ */
    
    public void onSelect$lstManagedListFilter(Event event) {
        setManagedListFilter(getFilter(event));
    }
    
    public void onSelect$lstManagedList() {
        updateControls();
    }
    
    /**
     * Create a new filter, prompting for a name.
     */
    public void onClick$btnManagedListFilterNew() {
        addOrRenameFilter(null);
    }
    
    /**
     * Rename an existing filter, prompting for a new name.
     */
    public void onClick$btnManagedListFilterRename() {
        addOrRenameFilter(managedList.getActiveFilter());
    }
    
    public void onClick$btnManagedListFilterDelete() {
        AbstractPatientListFilter filter = managedList.getActiveFilter();
        
        if (filter != null
                && PromptDialog.confirm(FILTER_DELETE_PROMPT, MessageFormat.format(FILTER_DELETE_TITLE, filter.getName()))) {
            filterManager.removeFilter(filter);
            lstManagedListFilter.getSelectedItem().detach();
            setManagedListFilter(null);
        }
    }
    
    public void onClick$btnManagedListAddCurrent() {
        managedListAdd(PatientContext.getCurrentPatient(), true);
    }
    
    public void onClick$btnManagedListAdd() {
        managedListAdd(activePatient, true);
    }
    
    public void onClick$btnManagedListImport() {
        for (Object item : (ListModelList<?>) lstPatientList.getModel())
            managedListAdd((PatientListItem) item, false);
        
        managedListRefresh();
    }
    
    public void onClick$btnManagedListRemove() {
        managedListRemove(getItem(lstManagedList), true);
    }
    
    public void onClick$btnManagedListRemoveAll() {
        for (PatientListItem item : new ArrayList<PatientListItem>(managedList.getListItems()))
            managedListRemove(item, false);
        
        managedListRefresh();
    }
    
}

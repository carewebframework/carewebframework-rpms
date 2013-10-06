/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.context.encounter;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import gov.ihs.cwf.context.EncounterContext;
import gov.ihs.cwf.context.EncounterUtil;
import gov.ihs.cwf.context.LocationContext;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Encounter;
import gov.ihs.cwf.domain.Location;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.factory.DomainObjectFactory;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.property.Property;
import gov.ihs.cwf.ui.context.location.LocationSelection;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.DateRangePicker;
import org.carewebframework.ui.zk.DateTimebox;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Encounter selection controller. Supports selecting an existing encounter from inpatient or
 * outpatient lists or creating an ad hoc encounter.
 * 
 * @author dmartin
 */
public class EncounterSelection extends Panel implements PatientContext.IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(EncounterSelection.class);
    
    public static enum EncounterFlag {
        NOT_LOCKED, FORCE, VALIDATE_ONLY, PROVIDER;
        
        /**
         * Returns a set of flags
         * 
         * @param flags
         * @return Set of encounter flags.
         */
        public static Set<EncounterFlag> flags(EncounterFlag... flags) {
            return flags == null || flags.length == 0 ? EnumSet.noneOf(EncounterFlag.class) : EnumSet.copyOf(Arrays
                    .asList(flags));
        }
    };
    
    private Listbox lstInpatient;
    
    private Listbox lstOutpatient;
    
    private Listbox lstLocation;
    
    private Textbox txtLocation;
    
    private Combobox cboServiceCategory;
    
    private DateTimebox datEncounter;
    
    private Checkbox chkForceCreate;
    
    private Tabbox tabbox;
    
    private Tab tabInpatient;
    
    private Tab tabOutpatient;
    
    private Tab tabNew;
    
    private Button btnOK;
    
    private DateRangePicker rngDateRange;
    
    private BrokerSession broker;
    
    private Patient patient;
    
    private Include incInpatient;
    
    private Include incOutpatient;
    
    private Include incNew;
    
    private Set<EncounterFlag> flags;
    
    private final EncounterRenderer encounterRenderer = new EncounterRenderer();
    
    /**
     * Displays the encounter selection dialog.
     * 
     * @param flags
     */
    public static void execute(EncounterFlag... flags) {
        String resource = Constants.RESOURCE_PREFIX + "encounterSelection.zul";
        Window dlg = (Window) FrameworkUtil.getAttribute(resource);
        
        if (dlg == null || dlg.getPage() == null) {
            dlg = PopupDialog.popup(resource, true, true, false);
            FrameworkUtil.setAttribute(resource, dlg);
        }
        
        try {
            EncounterSelection sel = ZKUtil.findChild(dlg, EncounterSelection.class);
            sel.setEncounterFlags(EncounterFlag.flags(flags));
            dlg.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean validEncounter() {
        return validEncounter(EncounterContext.getCurrentEncounter());
    }
    
    public static boolean validEncounter(Encounter encounter) {
        return encounter != null && encounter.isPrepared();
    }
    
    public static boolean ensureEncounter() {
        return ensureEncounter(null);
    }
    
    public static boolean ensureEncounter(Set<EncounterFlag> flags) {
        Encounter encounter = EncounterContext.getCurrentEncounter();
        boolean isValid = validEncounter(encounter);
        
        if (isValid && encounter.isLocked() && hasFlag(flags, EncounterFlag.NOT_LOCKED)) {
            return false;
        }
        
        if (isValid && encounter.getDomainId() == 0 && hasFlag(flags, EncounterFlag.FORCE)) {
            return EncounterUtil.forceCreate(encounter);
        }
        
        if (isValid || hasFlag(flags, EncounterFlag.VALIDATE_ONLY)) {
            return isValid;
        }
        
        execute();
        return validEncounter();
    }
    
    public static String validEncounter(Encounter encounter, Set<EncounterFlag> flags) {
        if (encounter.isLocked() && hasFlag(flags, EncounterFlag.NOT_LOCKED)) {
            return Constants.TX_NO_LCK;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (encounter.getLocation() == null) {
            appendItem(sb, Constants.TX_NO_LOC);
        }
        
        if (encounter.getServiceCategory() == null) {
            appendItem(sb, Constants.TX_NO_CAT);
        }
        
        if (encounter.getDateTime() == null) {
            appendItem(sb, Constants.TX_NO_DAT);
        }
        
        if (encounter.getEncounterProvider().getCurrentProvider() == null) {
            appendItem(sb, Constants.TX_NO_PRV);
        }
        
        if (encounter.getEncounterProvider().getPrimaryProvider() == null && !encounter.isLocked()) {
            appendItem(sb, Constants.TX_NO_PRI);
        }
        
        if (sb.length() > 0) {
            return Constants.TX_MISSING + sb.toString();
        }
        
        if (hasFlag(flags, EncounterFlag.FORCE) && encounter.getDomainId() <= 0
                && DateUtil.stripTime(encounter.getDateTime()).after(DateUtil.today())) {
            return Constants.TX_NO_FUT;
        }
        
        if (hasFlag(flags, EncounterFlag.PROVIDER)
                && !RPMSUtil.getBrokerSession().callRPCBool("BEHOUSCX HASKEYS", "PROVIDER",
                    encounter.getEncounterProvider().getCurrentProvider().getDomainId())) {
            return Constants.TX_NO_KEY;
        }
        
        if (hasFlag(flags, EncounterFlag.FORCE) && encounter.getDomainId() == 0 && !EncounterUtil.forceCreate(encounter)) {
            return "Failed to create the visit.";
        }
        
        return null;
    }
    
    private static void appendItem(StringBuilder sb, String item) {
        sb.append("   ").append(item).append('\n');
    }
    
    private static boolean hasFlag(Set<EncounterFlag> flags, EncounterFlag flag) {
        return flags == null ? false : flags.contains(flag);
    }
    
    /**
     * Wire variables and events.
     */
    public void onCreate() {
        log.trace("onCreate");
        FrameworkUtil.getAppFramework().registerObject(this);
        ZKUtil.wireController(this, this);
        lstInpatient.setItemRenderer(encounterRenderer);
        lstOutpatient.setItemRenderer(encounterRenderer);
        rngDateRange.getItemAtIndex(0).setLabel("Default Date Range");
        rngDateRange.setSelectedIndex(0);
        broker = RPMSUtil.getBrokerSession();
        setProviderSelectionDialog(tabInpatient, incInpatient);
        setProviderSelectionDialog(tabOutpatient, incOutpatient);
        setProviderSelectionDialog(tabNew, incNew);
        Property property = new Property("BEHOENCX VISIT TYPES", "*", null, "I");
        
        for (String sc : property.getValues()) {
            String pcs[] = StrUtil.split(sc, "~", 3);
            Comboitem item = cboServiceCategory.appendItem(pcs[1]);
            item.setValue(pcs[0]);
            item.setTooltiptext(pcs[2]);
        }
        
        List<String> data = broker.callRPCList("BEHOENCX CLINLOC", null, "", 1, 9999);
        
        for (String itm : data) {
            String[] pcs = StrUtil.split(itm, StrUtil.U, 3);
            Listitem item = lstLocation.appendItem(pcs[1], pcs[0]);
            item.setAttribute("sc", pcs[2]);
            item.addForward(Events.ON_DOUBLE_CLICK, btnOK, Events.ON_CLICK);
        }
        
        flagsChanged();
        committed();
    }
    
    private void getServiceCategories() {
        
    }
    
    protected void setEncounterFlags(Set<EncounterFlag> flags) {
        this.flags = flags;
        flagsChanged();
    }
    
    private void flagsChanged() {
        if (chkForceCreate != null) {
            boolean forceVisit = hasFlag(flags, EncounterFlag.FORCE);
            chkForceCreate.setChecked(forceVisit);
            chkForceCreate.setDisabled(forceVisit);
        }
    }
    
    public void onSelect$tabbox() {
        rngDateRange.setVisible(tabbox.getSelectedTab() == tabOutpatient);
    }
    
    public void onSelectRange$rngDateRange() {
        initOutpatient();
    }
    
    public void onSelect$lstLocation() {
        Listitem item = lstLocation.getSelectedItem();
        String sc = (String) item.getAttribute("sc");
        
        if (sc.isEmpty()) {
            cboServiceCategory.setSelectedItem(null);
        } else {
            for (Object obj : cboServiceCategory.getItems()) {
                if (sc.equals(((Comboitem) obj).getValue())) {
                    cboServiceCategory.setSelectedItem((Comboitem) obj);
                    break;
                }
            }
        }
    }
    
    public void onSelect$lstInpatient() {
        loadProviders(getEncounter(lstInpatient));
    }
    
    public void onSelect$lstOutpatient() {
        loadProviders(getEncounter(lstOutpatient));
    }
    
    public void onClick$btnLocation() throws Exception {
        LocationSelection.locationLookup(txtLocation.getValue(), lstLocation, LocationContext.getCurrentLocation());
    }
    
    public void onClick$btnCancel() {
        close();
    }
    
    /**
     * Change the encounter context to the selected encounter and close the dialog.
     * 
     * @throws ClassNotFoundException
     */
    public void onClick$btnOK() throws ClassNotFoundException {
        Encounter encounter = null;
        Tab activeTab = tabbox.getSelectedTab();
        ProviderSelection providerSelection = getProviderSelectionDialog(activeTab);
        
        if (activeTab == tabInpatient) { // Inpatient encounter
            encounter = getEncounter(lstInpatient);
        } else if (activeTab == tabOutpatient) { // Outpatient encounter
            encounter = getEncounter(lstOutpatient);
        } else { // Ad hoc encounter
            Listitem item = lstLocation.getSelectedItem();
            long locid = item == null ? 0 : StrUtil.toLong((String) item.getValue());
            Location location = locid > 0 ? DomainObjectFactory.get(Location.class, locid) : null;
            Comboitem cboitem = cboServiceCategory.getSelectedItem();
            String sc = cboitem == null ? null : (String) cboitem.getValue();
            Date date = datEncounter.getDate();
            encounter = new Encounter(date, location, sc);
            
            if (chkForceCreate.isChecked()) {
                flags.add(EncounterFlag.FORCE);
            } else {
                flags.remove(EncounterFlag.FORCE);
            }
        }
        
        if (encounter != null) {
            providerSelection.updateCurrentProvider();
            encounter.setEncounterProvider(providerSelection.getEncounterProvider());
            String s = validEncounter(encounter, flags);
            
            if (s != null) {
                PromptDialog.showWarning(s);
                return;
            }
            EncounterContext.changeEncounter(encounter);
            close();
        }
    }
    
    private Encounter getEncounter(Listbox lb) {
        Listitem item = lb.getSelectedItem();
        return item == null ? null : (Encounter) item.getValue();
    }
    
    /**
     * Close the main dialog.
     */
    private void close() {
        getParent().setVisible(false);
    }
    
    /**
     * Initialize the dialog. Performs a query to return all existing encounters with the set time
     * window and populates the inpatient/outpatient lists from this.
     * 
     * @throws Exception
     */
    private void init() {
        boolean hasInpatientEncounter = initInpatient();
        boolean hasOutpatientEncounter = initOutpatient();
        initNewEncounter();
        Tab activeTab = hasOutpatientEncounter ? tabOutpatient : hasInpatientEncounter ? tabInpatient : tabNew;
        activeTab.setSelected(true);
        onSelect$tabbox();
    }
    
    private ProviderSelection getProviderSelectionDialog(Tab tab) {
        return (ProviderSelection) tab.getAttribute("psd");
    }
    
    private void setProviderSelectionDialog(Tab tab, Include inc) {
        ProviderSelection dlg = ZKUtil.findChild(inc, ProviderSelection.class);
        tab.setAttribute("psd", dlg);
    }
    
    private void loadProviders(Encounter encounter) {
        getProviderSelectionDialog(tabbox.getSelectedTab()).loadProviders(patient, encounter);
    }
    
    private void populateListbox(Listbox lb, List<String> data) {
        lb.setModel((ListModel<?>) null);
        lb.setModel(new ListModelList<String>(data));
    }
    
    private boolean initOutpatient() {
        List<String> data = broker.callRPCList("BEHOENCX VISITLST", null, patient.getDomainId(),
            rngDateRange.getStartDate(), rngDateRange.getEndDate());
        populateListbox(lstOutpatient, data);
        return data.size() > 0;
    }
    
    private boolean initInpatient() {
        List<String> data = broker.callRPCList("BEHOENCX ADMITLST", null, patient.getDomainId());
        populateListbox(lstInpatient, data);
        return data.size() > 0;
    }
    
    private void initNewEncounter() {
        datEncounter.setDate(new Date());
        lstLocation.setSelectedItem(null);
        cboServiceCategory.setSelectedItem(null);
        loadProviders(null);
    }
    
    @Override
    public void canceled() {
    }
    
    @Override
    public void committed() {
        patient = PatientContext.getCurrentPatient();
        init();
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
}

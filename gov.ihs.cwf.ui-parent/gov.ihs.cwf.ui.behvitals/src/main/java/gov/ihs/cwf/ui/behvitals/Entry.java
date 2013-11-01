/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.behvitals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.ihs.cwf.context.EncounterContext;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Encounter;
import gov.ihs.cwf.domain.Location;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.factory.DomainObjectFactory;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.zk.DateTimebox;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Row;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;

/**
 * @author dmartin
 */
public class Entry extends Panel implements PatientContext.IPatientContextEvent, EncounterContext.IEncounterContextEvent, IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final String TX_PROMPT = "Changing the patient or encounter selection will cause loss of vital data entry."
            + "\r\nDo you wish to continue?";
    
    private static final String SCLASS_UNMARKED = "behvitals-enter-unmarked";
    
    private static final String SCLASS_MARKED = "behvitals-enter-marked";
    
    public static enum DefaultUnits {
        DEFAULT, US, METRIC
    };
    
    private Grid grdVitals;
    
    private Column colTest;
    
    private Column colRange;
    
    private Combobox cboDefaultUnits;
    
    private Image imgLocked;
    
    private Button btnOK;
    
    private Button btnCancel;
    
    private Button btnNew;
    
    private DateTimebox dtmDate;
    
    private boolean useEncounterDate;
    
    private final DefaultUnits defaultUnits = DefaultUnits.DEFAULT;
    
    private boolean enabled;
    
    private boolean fetched;
    
    private FMDate lastDateTime;
    
    private final List<String> template = new ArrayList<String>();
    
    private boolean warned;
    
    private boolean modified;
    
    private boolean noValidate;
    
    private int rangeCol;
    
    private final int maxCols = 5;
    
    private int colIndex;
    
    private int rowIndex;
    
    private String val;
    
    private Patient patient;
    
    private Encounter encounter;
    
    private BrokerSession broker;
    
    public static boolean isEnabled() {
        return SecurityUtil.isGranted("PARM_BEHOVM DATA ENTRY");
    }
    
    public void onCreate() {
        ZKUtil.wireController(this, this);
        broker = RPMSUtil.getBrokerSession();
        enabled = isEnabled();
        init();
    }
    
    private void init() {
        reset();
        warned = false;
        lastDateTime = null;
        modified = false;
        patient = PatientContext.getCurrentPatient();
        encounter = EncounterContext.getCurrentEncounter();
        try {
            Location loc = DomainObjectFactory.get(Location.class, 3);
            encounter = new Encounter(new FMDate(), loc, "A");
        } catch (Exception e) {}
        loadGrid();
    }
    
    private void reset() {
        template.clear();
        fetched = false;
    }
    
    private int fetch() {
        if (enabled && !fetched && patient != null && encounter != null) {
            template.clear();
            fetched = true;
            RPMSUtil.getBrokerSession().callRPCList("BEHOVM TEMPLATE", template, patient.getDomainId(),
                encounter.getEncoded(), defaultUnits.ordinal() - 1);
        }
        
        return template.size();
    }
    
    private void refreshForm() {
        modified = false;
        noValidate = true;
        getTopToolbar().setVisible(encounter != null);
        getPanelchildren().setVisible(encounter != null);
        
        if (encounter == null) {
            return;
        }
        
        imgLocked.setVisible(encounter != null && encounter.isLocked());
        btnNew.setDisabled(!imgLocked.isVisible());
        btnCancel.setDisabled(btnNew.isDisabled());
        btnOK.setDisabled(false);
        lastDateTime = lastDateTime != null ? lastDateTime : useEncounterDate ? encounter.getDateTime() : new FMDate();
        loadGrid();
        val = getValue(colIndex, rowIndex);
        moveTo(rangeCol - 1, 1);
    }
    
    private int getRowCount() {
        return grdVitals.getRows().getChildren().size();
    }
    
    private Row getRow(int row) {
        return (Row) grdVitals.getRows().getChildren().get(row);
    }
    
    private int getColCount() {
        return grdVitals.getColumns().getChildren().size();
    }
    
    private Column getColumn(int col) {
        return (Column) grdVitals.getColumns().getChildren().get(col);
    }
    
    private int getColumnIndex(Column col) {
        return grdVitals.getColumns().getChildren().indexOf(col);
    }
    
    private Column addColumn() {
        Column col = new Column();
        grdVitals.getColumns().insertBefore(col, colRange);
        col.setSclass(SCLASS_UNMARKED);
        col.setWidth("12em");
        col.setAlign("center");
        DateTimebox dtb = new DateTimebox();
        dtb.addForward(Events.ON_CHANGE, this, "onDateChange");
        col.appendChild(dtb);
        return col;
    }
    
    private Row addRow() {
        Row row = new Row();
        grdVitals.getRows().appendChild(row);
        int colCount = getColCount();
        
        for (int i = 1; i <= colCount; i++) {
            Span span = new Span();
            row.appendChild(span);
            span.setSclass(SCLASS_UNMARKED);
            Component child;
            
            if (i == 1 || i == colCount) {
                child = new Label();
            } else {
                Textbox tb = new Textbox();
                child = tb;
                tb.addForward(Events.ON_CHANGE, this, "onDataChange");
            }
            
            span.appendChild(child);
        }
        
        return row;
    }
    
    private void initGrid(int colcount) {
        grdVitals.getRows().getChildren().clear();
        
        while (getColCount() > 2) {
            getColumn(1).detach();
        }
        
        for (int i = 0; i < colcount; i++) {
            addColumn();
        }
    }
    
    /**
     * Load the string grid with data. Calls the grid RPC to retrieve data for the grid. Data is
     * returned in the following format:
     * 
     * <pre>
     * 
     * counts: test count^date count^result count
     * tests: control ien^test ien^test name^test abbrv^units^low norm^hi norm^percentile RPC
     * dates: date id^FM date results: date id^row #^value^result ien
     * 
     * For example: 
     * 
     * 8^2^7
     * 
     * 3^3^TEMPERATURE^TMP^F^^^ 
     * 5^5^PULSE^PU^/min^60^100^
     * 15^15^RESPIRATIONS^RS^/min^^^ 
     * 4^4^BLOOD PRESSURE^BP^mmHg^90^150^
     * 1^1^HEIGHT^HT^in^^^CIAOCVVM PCTILE 
     * 2^2^WEIGHT^WT^lb^^^CIAOCVVM PCTILE
     * 21^21^PAIN^PA^^^^ 
     * 6^6^HEAD CIRCUMFERENCE^HC^in^^^CIAOCVVM PCTILE
     * 
     * 2^3041018.1446 
     * 1^3041022.1446
     * 
     * 1^2^77^^2445227 
     * 1^4^101/65^^2445224 
     * 1^5^27^^2445222 
     * 2^5^26.5^^2445220
     * 1^6^16.5^^2445223 
     * 2^6^16^^2445218 
     * 1^8^17.5^^2445225
     * </pre>
     */
    private void loadGrid() {
        if (fetch() == 0) {
            initGrid(0);
            return;
        }
        
        Iterator<String> data = template.iterator();
        String[] pcs = StrUtil.split(data.next(), StrUtil.U, 3);
        int testcnt = StrUtil.toInt(pcs[0]);
        int datecnt = StrUtil.toInt(pcs[1]);
        int datacnt = StrUtil.toInt(pcs[2]);
        
        if (datacnt == 0 || datecnt == 0) {
            // showMessage("No data available within selected range.");
            return;
        }
        
        initGrid(datecnt);
        // Populate test names and units
        for (int r = 0; r < testcnt; r++) {
            addRow();
            pcs = StrUtil.split(data.next(), StrUtil.U, 8);
            String range = pcs[5] + "-" + pcs[6];
            range = "-".equals(range) ? "" : range + " ";
            setValue(0, r, WordUtils.capitalizeFully(pcs[2]), pcs[0]);
            setValue(datecnt + 1, r, range + pcs[4], pcs[4]);
        }
        // Populate date headers
        Map<String, Integer> headers = new HashMap<String, Integer>();
        
        for (int c = 1; c <= datecnt; c++) {
            pcs = StrUtil.split(data.next(), StrUtil.U, 2);
            FMDate date = new FMDate(pcs[1]);
            Column column = getColumn(c);
            DateTimebox dtb = (DateTimebox) column.getFirstChild();
            dtb.setDate(date);
            headers.put(pcs[0], c);
        }
        // Populate data cells
        for (int i = 0; i < datacnt; i++) {
            pcs = StrUtil.split(data.next(), StrUtil.U, 3);
            int col = headers.get(pcs[0]);
            int row = StrUtil.toInt(pcs[1]) - 1;
            setValue(col, row, pcs[2], pcs[4]);
        }
    }
    
    private void moveTo(int col, int row) {
        colIndex = col;
        rowIndex = row;
        focusCell();
    }
    
    private void focusCell() {
        if (!enabled) {
            return;
        }
        
        positionIndicator(rowIndex, false);
    }
    
    public void onDataChange(Event event) {
        event = ZKUtil.getEventOrigin(event);
    }
    
    public void onDateChange(Event event) {
        event = ZKUtil.getEventOrigin(event);
    }
    
    private void positionIndicator(int row, boolean visible) {
    }
    
    private boolean validateCell() {
        if (noValidate) {
            return true;
        }
        
        try {
            noValidate = true;
            
            if (dtmDate.isVisible()) {
                Column col = (Column) dtmDate.getParent();
                
                if (!col.getValue().equals(dtmDate.getDate())) {
                    col.setValue(dtmDate.getDate());
                    col.setLabel(DateUtil.formatDate(dtmDate.getDate()));
                    int rows = grdVitals.getRows().getChildren().size();
                    int idx = getColumnIndex(col);
                    
                    for (int i = 1; i < rows; i++) {
                        mark(idx, i);
                    }
                }
                
                dtmDate.setVisible(false);
                return true;
            }
            
            String v = getValue(colIndex, rowIndex);
            String s = StringUtils.isEmpty(v) ? "" : broker.callRPC("BEHOVM VALIDATE", getObject(0, rowIndex),
                getDefaultUnits(), v);
            
            if (s.indexOf(StrUtil.U) == -1) {
                setValue(colIndex, rowIndex, v);
                mark(colIndex, rowIndex);
                return true;
            } else {
                PromptDialog.showError(StrUtil.piece(s, StrUtil.U, 2));
                return false;
            }
        } finally {
            noValidate = false;
            btnOK.setDisabled(!modified);
        }
    }
    
    private void mark(int col, int row) {
        boolean hasChanged = hasChanged(col, row);
        modified |= hasChanged;
        getCell(col, row).setSclass(hasChanged ? SCLASS_MARKED : SCLASS_UNMARKED);
    }
    
    private int getDefaultUnits() {
        return cboDefaultUnits.getSelectedIndex() - 1;
    }
    
    private void setObject(int col, int row, Object object) {
        getCell(col, row).setAttribute("object", object);
    }
    
    private Object getObject(int col, int row) {
        return getCell(col, row).getAttribute("object");
    }
    
    private void setValue(int col, int row, String value) {
        Component cmp = getCell(col, row).getFirstChild();
        cmp.setAttribute("value", value);
        
        if (cmp instanceof Label) {
            ((Label) cmp).setValue(value);
        } else {
            ((Textbox) cmp).setValue(value);
        }
    }
    
    private void setValue(int col, int row, String value, Object object) {
        setValue(col, row, value);
        setObject(col, row, object);
    }
    
    private String getValue(int col, int row) {
        Component cmp = getCell(col, row).getFirstChild();
        return cmp instanceof Label ? ((Label) cmp).getValue() : ((Textbox) cmp).getValue();
    }
    
    private String getOriginalValue(int col, int row) {
        return (String) getCell(col, row).getAttribute("value");
    }
    
    private boolean hasChanged(int col, int row) {
        return ObjectUtils.equals(getValue(col, row), getOriginalValue(col, row));
    }
    
    private Span getCell(int col, int row) {
        return (Span) grdVitals.getCell(row, col);
    }
    
    /**
     * Called if the patient context change was canceled.
     */
    @Override
    public void canceled() {
        warned = false;
    }
    
    /**
     * Called if the patient context was committed.
     */
    @Override
    public void committed() {
        init();
    }
    
    /**
     * Called when a patient context change has been requested.
     * 
     * @param silent = If true, user interaction is not permitted.
     */
    @Override
    public String pending(boolean silent) {
        if (modified && !warned) {
            if (silent || !PromptDialog.confirm(TX_PROMPT)) {
                return "Vital entry in progress.";
            }
        }
        
        return null;
    }
    
    /**
     * The CareWeb framework will call this method whenever the component becomes activated
     * (visible).
     */
    @Override
    public void onActivate() {
    }
    
    /**
     * The CareWeb framework will call this method whenever the component becomes inactivated
     * (hidden).
     */
    @Override
    public void onInactivate() {
    }
    
    /**
     * The CareWeb framework will call this method whenever the component is initially loaded.
     * 
     * @param container = Reference to the plugin's container.
     */
    @Override
    public void onLoad(PluginContainer container) {
    }
    
    /**
     * The CareWeb framework will call this method whenever the component is unloaded.
     */
    @Override
    public void onUnload() {
    }
    
}

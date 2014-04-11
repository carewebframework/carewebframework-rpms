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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.highcharts.Chart;
import org.carewebframework.ui.highcharts.DashStyle;
import org.carewebframework.ui.highcharts.DataPoint;
import org.carewebframework.ui.highcharts.Series;
import org.carewebframework.ui.highcharts.ZoomType;
import org.carewebframework.ui.zk.DateRangePicker;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.context.PatientContext;
import org.carewebframework.vista.api.domain.Patient;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;

import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Slider;
import org.zkoss.zul.Toolbar;

/**
 * 
 */
public class Graph extends Div implements PatientContext.IPatientContextEvent, IPluginEvent {
    
    private static final Log log = LogFactory.getLog(Graph.class);
    
    private static final long serialVersionUID = 1L;
    
    private static final String[] rangeSeries = new String[] { "low", "high" };
    
    private Chart chart;
    
    private Listbox lstVitals;
    
    private Listhead hdrVitals;
    
    private Paging pgVitals;
    
    private Slider sldVitals;
    
    private Checkbox chkGrid;
    
    private Checkbox chkAge;
    
    private Checkbox chkPercentiles;
    
    private Checkbox chkZoom;
    
    private Combobox cboUnits;
    
    private Button btnEnterVitals;
    
    private DateRangePicker datRange;
    
    private Toolbar tbarPaging;
    
    private Patient patient;
    
    private int selectedRow = -1;
    
    private String selectedItem;
    
    private final Date today = new Date();
    
    private final Map<String, String> percentiles = new HashMap<String, String>();
    
    private List<String> tests;
    
    private BrokerSession broker;
    
    private String gridRPC;
    
    private String detailRPC;
    
    private final int maxCols = 5;
    
    public void selectData(String test, String gridRPC, String detailRPC, List<String> tests) {
        this.gridRPC = gridRPC;
        this.detailRPC = detailRPC;
        this.tests = tests;
        committed();
        setSelectedRow(test);
    }
    
    public void onCreate() {
        ZKUtil.wireController(this);
        FrameworkUtil.getAppFramework().registerObject(this);
        // chart.getRenderer().setBaseItemLabelsVisible(true);
        //chart.setPeriod(Chart.MINUTE);
        //chart.setDateFormat("dd-MMM-yy");
        chart.getXAxis().gridLineWidth = 0;
        chart.getYAxis().gridLineWidth = 0;
        chart.options.exporting.buttons_printButton.onclick = "cwf.print(this.container);";
        chart.options.plotOptions.tooltip.xDateFormat = "dd-MMM-yyyy HH:mm";
        broker = VistAUtil.getBrokerSession();
        cboUnits.setSelectedIndex(0);
        datRange.setSelectedIndex(0);
        btnEnterVitals.setVisible(Entry.isEnabled());
        selectData("", "BEHOVM GRID", "BEHOVM DETAIL", null);
    }
    
    private Date ageToDate(double age) {
        if (chkAge.isVisible() && chkAge.isChecked()) {
            return DateUtil.addDays(patient.getBirthDate(), (int) (age * 365.25 / 12.0), true);
        } else {
            return new Date();
        }
    }
    
    private double dateToAge(Date date) {
        double diff = date.getTime() - patient.getBirthDate().getTime();
        return diff / 2592000000.0;
    }
    
    private Listcell setValue(int col, int row, String value, Object object) {
        Listcell cell = getCell(col, row);
        cell.setLabel(value);
        cell.setValue(object);
        return cell;
    }
    
    private String getValue(int col, int row) {
        return getCell(col, row).getLabel();
    }
    
    private Object getObject(int col, int row) {
        return getCell(col, row).getValue();
    }
    
    private Listcell getCell(int col, int row) {
        Listitem item;
        
        while (row >= lstVitals.getItemCount()) {
            item = new Listitem();
            item.setVisible(false);
            item.setParent(lstVitals);
        }
        
        item = lstVitals.getItemAtIndex(row);
        
        while (col >= item.getChildren().size()) {
            Listcell cell = new Listcell("");
            cell.setParent(item);
        }
        
        return (Listcell) item.getChildren().get(col);
    }
    
    private Series findSeries(String seriesName, boolean forceCreate) {
        for (Series series : chart.options.series) {
            if (seriesName.equals(series.name)) {
                return series;
            }
        }
        
        if (!forceCreate) {
            return null;
        }
        
        Series series = chart.addSeries();
        series.name = seriesName;
        return series;
    }
    
    private DataPoint plotData(double xVal, String value, String seriesName, String id) {
        try {
            double yVal = Double.parseDouble(value);
            Series series = findSeries(seriesName, true);
            DataPoint dp = series.addDataPoint(xVal, yVal);
            dp.id = id;
            return dp;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void plotRange(double xLow, double xHigh, String range) {
        final String pcs[] = StrUtil.split(range, "-", 2);
        
        for (int i = 0; i < 2; i++) {
            String seriesName = rangeSeries[i];
            Series series = findSeries(seriesName, true);
            DataPoint low = plotData(xLow, pcs[i], seriesName, null);
            DataPoint high = plotData(xHigh, pcs[i], seriesName, null);
            
            if (low == null || high == null) {
                chart.options.series.remove(series);
            } else {
                series.plotOptions.dashStyle = DashStyle.Dot;
                series.plotOptions.color = "darkgray";
                series.plotOptions.showInLegend = false;
                low.marker.enabled = false;
                high.marker.enabled = false;
            }
        }
    }
    
    private void plotPercentile(double xVal, String value, String series) {
        Series pctile = findSeries(series, true);
        DataPoint dp = plotData(xVal, value, series, series);
        
        if (pctile.plotOptions.dashStyle == null) {
            pctile.plotOptions.marker.enabled = false;
            pctile.plotOptions.enableMouseTracking = false;
            pctile.plotOptions.dashStyle = "50".equals(series) ? DashStyle.DashDot : DashStyle.Dash;
            pctile.plotOptions.lineWidth = 1;
        }
    }
    
    private void chartData() {
        Date dateHigh = null;
        Date dateLow = null;
        int row = selectedRow;
        boolean useAge = chkAge.isVisible() && chkAge.isChecked();
        chart.clear();
        int colcount = hdrVitals.getChildren().size() - 1;
        
        if (row < 0 || row >= lstVitals.getItemCount() || colcount < 0) {
            return;
        }
        
        boolean hasData = false;
        String testname = getValue(0, row);
        String testid = (String) getObject(0, row);
        boolean isBP = StringUtils.containsIgnoreCase(testname, "pressure");
        chart.getYAxis().title.text = (String) getObject(colcount, row);
        chart.getXAxis().title.text = useAge ? "age (months)" : null;
        chart.getXAxis().type = useAge ? "linear" : "datetime";
        
        for (int col = 1; col < colcount; col++) {
            Listheader hdr = (Listheader) hdrVitals.getChildren().get(col);
            FMDate date = (FMDate) hdr.getValue();
            
            if (date != null) {
                double xVal = useAge ? dateToAge(date) : date.getTime();
                String vals[] = StrUtil.split(getValue(col, row), ";");
                boolean newData = false;
                
                for (String val : vals) {
                    if (isBP) {
                        String pcs[] = StrUtil.split(val, "/");
                        
                        if (pcs.length > 0) {
                            newData |= plotData(xVal, pcs[0], "Systolic", testname) != null;
                        }
                        
                        if (pcs.length > 2) {
                            newData |= plotData(xVal, pcs[1], "Mean", testname) != null;
                            newData |= plotData(xVal, pcs[2], "Diastolic", testname) != null;
                        } else {
                            newData |= plotData(xVal, pcs[1], "Diastolic", testname) != null;
                        }
                    } else {
                        newData |= plotData(xVal, val, testname, testname) != null;
                    }
                }
                
                if (newData) {
                    hasData = true;
                    dateLow = dateLow == null ? date : date.getTime() < dateLow.getTime() ? date : dateLow;
                    dateHigh = dateHigh == null ? date : date.getTime() > dateHigh.getTime() ? date : dateHigh;
                }
            }
        }
        
        if (hasData) {
            double xLow = useAge ? dateToAge(dateLow) : dateLow.getTime();
            double xHigh = useAge ? dateToAge(dateHigh) : dateHigh.getTime();
            plotRange(xLow, xHigh, StrUtil.piece(getValue(colcount, row), " "));
            String pctileRPC = percentiles.get(testid);
            
            if (pctileRPC != null && chkPercentiles.isChecked()) {
                List<String> pctiles = broker.callRPCList(pctileRPC, null, testid, patient.getDomainId(),
                    DateUtils.addDays(dateLow, -3000), DateUtils.addDays(dateHigh, 3000), getDefaultUnits());
                
                for (String pctile : pctiles) {
                    String pcs[] = StrUtil.split(pctile, StrUtil.U, 3);
                    FMDate date = new FMDate(pcs[1]);
                    plotPercentile(useAge ? dateToAge(date) : date.getTime(), pcs[2], pcs[0]);
                }
            }
            
            chart.run();
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
     * 3^3^TEMPERATURE^TMP^F^^^ 5^5^PULSE^PU^/min^60^100^
     * 15^15^RESPIRATIONS^RS^/min^^^ 4^4^BLOOD PRESSURE^BP^mmHg^90^150^
     * 1^1^HEIGHT^HT^in^^^CIAOCVVM PCTILE 2^2^WEIGHT^WT^lb^^^CIAOCVVM PCTILE
     * 21^21^PAIN^PA^^^^ 6^6^HEAD CIRCUMFERENCE^HC^in^^^CIAOCVVM PCTILE
     * 
     * 2^3041018.1446 
     * 1^3041022.1446
     * 
     * 1^2^77^^2445227 1^4^101/65^^2445224 1^5^27^^2445222 2^5^26.5^^2445220
     * 1^6^16.5^^2445223 2^6^16^^2445218 1^8^17.5^^2445225
     * </pre>
     */
    private void loadGrid() {
        chart.clear();
        
        if (patient == null) {
            showMessage("No patient selected.");
            return;
        }
        
        Iterator<String> data = doRPC(gridRPC, datRange.getStartDate(), datRange.getEndDate(), tests).iterator();
        percentiles.clear();
        String[] pcs = StrUtil.split(data.next(), StrUtil.U, 3);
        int testcnt = StrUtil.toInt(pcs[0]);
        int datecnt = StrUtil.toInt(pcs[1]);
        int datacnt = StrUtil.toInt(pcs[2]);
        
        if (datacnt == 0 || datecnt == 0) {
            showMessage("No data available within selected range.");
            return;
        }
        
        initGrid(datecnt + 2, testcnt);
        // Populate test names and units
        for (int r = 0; r < testcnt; r++) {
            pcs = StrUtil.split(data.next(), StrUtil.U, 8);
            String range = pcs[5] + "-" + pcs[6];
            range = "-".equals(range) ? "" : range + " ";
            setValue(0, r, WordUtils.capitalizeFully(pcs[2]), pcs[0]).setStyle("font-weight:bold");
            setValue(datecnt + 1, r, range + pcs[4], pcs[4]).setStyle("font-style:italic");
            
            if (!pcs[7].isEmpty()) {
                percentiles.put(pcs[0], pcs[7]);
            }
        }
        // Populate date headers
        Map<String, Listheader> headers = new HashMap<String, Listheader>();
        
        for (int c = 1; c <= datecnt; c++) {
            pcs = StrUtil.split(data.next(), StrUtil.U, 2);
            FMDate date = new FMDate(pcs[1]);
            Listheader hdr = (Listheader) hdrVitals.getChildren().get(c);
            hdr.setLabel(date.toString());
            hdr.setValue(date);
            hdr.setParent(hdrVitals);
            headers.put(pcs[0], hdr);
        }
        // Populate data cells
        for (int i = 0; i < datacnt; i++) {
            pcs = StrUtil.split(data.next(), StrUtil.U, 3);
            int col = headers.get(pcs[0]).getColumnIndex();
            int row = StrUtil.toInt(pcs[1]) - 1;
            setValue(col, row, StrUtil.strAppend(getValue(col, row), pcs[2], "; "), null);
            lstVitals.getItemAtIndex(row).setVisible(true);
        }
        
        lstVitals.invalidate();
        setSelectedRow(selectedItem);
    }
    
    private void initGrid(int colcount, int rowcount) {
        lstVitals.getItems().clear();
        hdrVitals.getChildren().clear();
        
        if (colcount > 2 && rowcount > 0) {
            tbarPaging.setVisible(true);
            sldVitals.setMaxpos(colcount - 2);
            sldVitals.setCurpos(maxCols);
            pgVitals.setActivePage(0);
            pgVitals.setTotalSize(colcount - 2);
            pgVitals.setPageSize(maxCols + 1);
            getCell(colcount - 1, rowcount - 1);
            
            for (int i = 1; i <= colcount; i++) {
                Listheader lh = new Listheader();
                lh.setVisible(i <= maxCols || i == colcount);
                lh.setAlign(i == 1 ? "right" : i == colcount ? "left" : "center");
                lh.setHflex("1");
                hdrVitals.appendChild(lh);
            }
        } else {
            tbarPaging.setVisible(false);
        }
    }
    
    private void showMessage(String message) {
        initGrid(0, 0);
        Listcell cell = getCell(0, 0);
        cell.setLabel(message);
        cell.getParent().setVisible(true);
    }
    
    private void setSelectedRow(String test) {
        if (StringUtils.isEmpty(test)) {
            setSelectedRow(-1);
        } else {
            for (int i = 0; i < lstVitals.getItemCount(); i++) {
                if (test.equals(getValue(0, i))) {
                    setSelectedRow(i);
                    break;
                }
            }
        }
    }
    
    private void setSelectedRow(int index) {
        if (index < 0) {
            selectedRow = -1;
            selectedItem = "";
            lstVitals.clearSelection();
            chkPercentiles.setVisible(false);
        } else {
            selectedRow = index;
            selectedItem = getValue(0, index);
            lstVitals.setSelectedIndex(index);
            chkPercentiles.setVisible(percentiles.containsKey(getObject(0, index)));
        }
        
        chartData();
    }
    
    private int getDefaultUnits() {
        return cboUnits.getSelectedIndex() - 1;
    }
    
    private List<String> doRPC(String rpcName, Date date1, Date date2, List<String> tests) {
        // TODO: need to use encounter location
        return broker.callRPCList(rpcName, null, patient.getDomainId(), date1, date2, 0, tests, 0, getDefaultUnits());
    }
    
    private void updatePaging() {
        int colcount = hdrVitals.getChildren().size() - 2;
        int maxcols = sldVitals.getCurpos() + 1;
        sldVitals.setTooltiptext(Integer.toString(maxcols));
        pgVitals.setActivePage(0);
        pgVitals.setPageSize(Math.min(maxcols, colcount));
        onPaging$pgVitals();
    }
    
    public void onScroll$sldVitals() {
        updatePaging();
    }
    
    public void onPaging$pgVitals() {
        int maxCols = sldVitals.getCurpos() + 1;
        int cols = hdrVitals.getChildren().size() - 1;
        int col1 = pgVitals.getActivePage() * maxCols;
        int col2 = col1 + maxCols;
        
        for (int col = 1; col < cols; col++) {
            ((Listheader) hdrVitals.getChildren().get(col)).setVisible(col > col1 && col <= col2);
        }
    }
    
    public void onSelect$lstVitals() {
        setSelectedRow(lstVitals.getSelectedIndex());
    }
    
    public void onSelectRange$datRange() {
        loadGrid();
    }
    
    public void onSelect$cboUnits() {
        loadGrid();
    }
    
    public void onCheck$chkGrid() {
        int w = chkGrid.isChecked() ? 1 : 0;
        chart.getXAxis().gridLineWidth = w;
        chart.getYAxis().gridLineWidth = w;
        chartData();
    }
    
    public void onCheck$chkAge() {
        chartData();
    }
    
    public void onCheck$chkPercentiles() {
        chartData();
    }
    
    public void onCheck$chkZoom() {
        chart.options.chart.zoomType = chkZoom.isChecked() ? ZoomType.xy : null;
        chartData();
    }
    
    /**
     * Called if the patient context change was canceled.
     */
    @Override
    public void canceled() {
    }
    
    /**
     * Called if the patient context was committed.
     */
    @Override
    public void committed() {
        patient = PatientContext.getCurrentPatient();
        chkAge.setVisible(patient != null && dateToAge(today) < 37);
        loadGrid();
    }
    
    /**
     * Called when a patient context change has been requested.
     * 
     * @param silent If true, user interaction is not permitted.
     */
    @Override
    public String pending(boolean silent) {
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
     * @param container Reference to the plugin's container.
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

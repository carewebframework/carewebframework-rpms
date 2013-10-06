/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.patientlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.patientlist.IPatientListFilterManager.FilterCapability;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.DateRange;

/**
 * Supports table-driven (BEH PATIENT LIST) patient lists.
 * 
 * @author dmartin
 */
public class BEHPatientList extends AbstractPatientList {
    
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(BEHPatientList.class);
    
    private final int listId;
    
    private final boolean dateRangeRequired;
    
    private final boolean useMixedCase;
    
    private final boolean noCaching;
    
    private final boolean sortList;
    
    private final Set<FilterCapability> filterCapabilities;
    
    private List<PatientListItem> patients;
    
    private BEHPatientList(String[] pcs) {
        super(pcs[1], pcs[3]);
        listId = Integer.parseInt(pcs[0]);
        String flags = pcs[2];
        dateRangeRequired = hasFlag(flags, 'D');
        useMixedCase = hasFlag(flags, 'M');
        noCaching = hasFlag(flags, 'N');
        sortList = hasFlag(flags, 'S');
        boolean canManage = hasFlag(flags, 'U');
        
        if (canManage) {
            filterCapabilities = Util.createImmutableSet(FilterCapability.ADD, FilterCapability.MOVE,
                FilterCapability.REMOVE, FilterCapability.RENAME);
        } else {
            filterCapabilities = null;
        }
    }
    
    public BEHPatientList(BrokerSession broker, int listId) {
        this(Util.split(broker.callRPC("BEHOPTPL LISTINFO1", listId), 5));
    }
    
    public BEHPatientList(BEHPatientList list) {
        super(list);
        this.listId = list.listId;
        this.dateRangeRequired = list.dateRangeRequired;
        this.useMixedCase = list.useMixedCase;
        this.noCaching = list.noCaching;
        this.sortList = list.sortList;
        this.filterCapabilities = list.filterCapabilities;
    }
    
    private boolean hasFlag(String flags, char flag) {
        return flags.indexOf(flag) >= 0;
    }
    
    public int getListId() {
        return listId;
    }
    
    /**
     * Returns the filter manager for this list, creating one if it doesn't already exist.
     */
    @Override
    public BEHPatientListFilterManager createFilterManager() {
        return new BEHPatientListFilterManager(this, filterCapabilities);
    }
    
    /**
     * Returns the patient list.
     * 
     * @return Patient list.
     */
    @Override
    public Collection<PatientListItem> getListItems() {
        if (!noCaching && patients != null) {
            return patients;
        }
        
        patients = new ArrayList<PatientListItem>();
        AbstractPatientListFilter filter = isFiltered() ? getActiveFilter() : null;
        BEHPatientListFilterEntity entity = filter == null ? null : (BEHPatientListFilterEntity) filter.getEntity();
        List<String> tempList = getBrokerSession().callRPCList("BEHOPTPL LISTPTS", null, listId,
            entity == null ? 0 : entity.getId(), formatDateRange());
        addPatients(patients, tempList, 0);
        return patients;
    }
    
    protected String formatDateRange() {
        DateRange range = dateRangeRequired ? getDateRange() : null;
        
        if (range != null) {
            String start = range.getStartDate() == null ? "" : new FMDate(range.getStartDate()).getFMDate();
            String end = range.getEndDate() == null ? "" : new FMDate(range.getEndDate()).getFMDate();
            return start + ";" + end;
        }
        
        return "";
    }
    
    /**
     * Indicate whether this list requires a date range.
     */
    @Override
    public boolean isDateRangeRequired() {
        return dateRangeRequired;
    }
    
    protected boolean getSortList() {
        return sortList;
    }
    
    protected boolean getUseMixedCase() {
        return useMixedCase;
    }
    
    protected Set<FilterCapability> getFilterCapabilities() {
        return filterCapabilities;
    }
    
    @Override
    public void setActiveFilter(AbstractPatientListFilter filter) {
        patients = null;
        super.setActiveFilter(filter);
    }
    
    @Override
    public void setDateRange(DateRange value) {
        patients = null;
        super.setDateRange(value);
    }
}

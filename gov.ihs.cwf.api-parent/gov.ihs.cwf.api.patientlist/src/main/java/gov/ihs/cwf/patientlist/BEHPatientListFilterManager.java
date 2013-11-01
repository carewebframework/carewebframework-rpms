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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.cal.api.patientlist.AbstractPatientListFilter;
import org.carewebframework.cal.api.patientlist.AbstractPatientListFilterManager;
import org.carewebframework.cal.api.patientlist.PatientListUtil;
import org.carewebframework.common.StrUtil;

/**
 * Filter manager for appointment-based lists.
 */
public class BEHPatientListFilterManager extends AbstractPatientListFilterManager {
    
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(BEHPatientListFilterManager.class);
    
    public BEHPatientListFilterManager(BEHPatientList patientList, Set<FilterCapability> capabilities) {
        super(patientList, capabilities);
    }
    
    @Override
    protected List<AbstractPatientListFilter> initFilters() {
        if (filters == null) {
            filters = new ArrayList<AbstractPatientListFilter>();
            
            BEHPatientList patientList = (BEHPatientList) getPatientList();
            int id = patientList.getListId();
            boolean sortList = patientList.getSortList();
            boolean useMixedCase = patientList.getUseMixedCase();
            String range = patientList.formatDateRange();
            List<String> tempList = RPMSUtil.getBrokerSession().callRPCList("BEHOPTPL LISTSEL", null, id, "", 1, 999, range);
            
            for (String item : tempList) {
                if (useMixedCase) {
                    String pcs[] = PatientListUtil.split(item, 2);
                    item = pcs[0] + StrUtil.U + PatientListUtil.formatName(pcs[1]);
                }
                BEHPatientListFilter filter = new BEHPatientListFilter(item);
                filters.add(filter);
            }
            
            if (sortList) {
                Collections.sort(filters);
            }
            
        }
        return filters;
    }
    
    @Override
    protected void refreshFilters() {
        filters = null;
        super.refreshFilters();
    }
    
    @Override
    protected AbstractPatientListFilter createFilter(Object entity) {
        return new BEHPatientListFilter((BEHPatientListFilterEntity) entity);
    }
    
    @Override
    protected AbstractPatientListFilter deserializeFilter(String serializedEntity) {
        return new BEHPatientListFilter(serializedEntity);
    }
}

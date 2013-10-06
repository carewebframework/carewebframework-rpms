/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientselection;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.carewebframework.ui.zk.PromptDialog;
import gov.ihs.cwf.patientlist.Util;
import gov.ihs.cwf.util.RPMSUtil;
import gov.ihs.cwf.mbroker.BrokerSession;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Person.Name;
import gov.ihs.cwf.factory.DomainObjectFactory;
import gov.ihs.cwf.mbroker.FMDate;

import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;

/**
 * Patient search services.
 */
public class PatientSearch {
    
    private static final Log log = LogFactory.getLog(PatientSearch.class);
    
    private static final String ERROR_PATIENT_NOT_FOUND = "patientsearch.error.patient.not.found";
    
    private static final String UNEXPECTED_ERROR = "patientsearch.error.unknown";
    
    private static final String WARN_TOO_MANY_MATCHES = "patientsearch.warn.too.many.matches";
    
    private static final Comparator<Patient> patientComparator = new Comparator<Patient>() {
        
        /**
         * Sort by patient full name, ignoring case.
         * 
         * @param patient1
         * @param patient2
         * @return Result of comparison.
         */
        @Override
        public int compare(Patient patient1, Patient patient2) {
            return patient1.getFullName().compareToIgnoreCase(patient2.getFullName());
        }
        
    };
    
    /**
     * Perform search, using the specified search text.
     * 
     * @param searchText Text to use in search.
     * @param maxMatches Maximum number of allowable matches. If this value is exceeded, the user
     *            will be given the opportunity to cancel the search. A value of zero suppresses
     *            this feature.
     * @return A list of patients matching the specified search criteria. The return value will be
     *         null if no search criteria are provided or the search exceeds the maximum allowable
     *         matches and the user chooses to cancel the search.
     */
    public static List<Patient> execute(String searchText, int maxMatches) {
        return execute(new PatientSearchCriteria(searchText), maxMatches);
    }
    
    /**
     * Perform search, using the specified criteria.
     * 
     * @param criteria The search criteria.
     * @param maxMatches Maximum number of allowable matches. If this value is exceeded, the user
     *            will be given the opportunity to cancel the search. A value of zero suppresses
     *            this feature.
     * @return A list of patients matching the specified search criteria. The return value will be
     *         null if no search criteria are provided or the search exceeds the maximum allowable
     *         matches and the user chooses to cancel the search.
     */
    public static List<Patient> execute(PatientSearchCriteria criteria, int maxMatches) {
        if (criteria == null || criteria.isEmpty())
            return null;
        
        criteria.validate();
        BrokerSession broker = RPMSUtil.getBrokerSession();
        
        try {
            Name name = criteria.getName();
            String lastName = name == null ? "" : name.getLastName();
            String firstName = name == null ? "" : name.getFirstName();
            String mrn = criteria.getMRN();
            String ssn = criteria.getSSN();
            String gender = criteria.getGender();
            String dob = criteria.getBirth() == null ? "" : new FMDate(criteria.getBirth()).getFMDate();
            String dfn = criteria.getId();
            List<String> hits = broker.callRPCList("BEHOPTPS SEARCH", null, 200, lastName, firstName, mrn, ssn, dfn, gender, dob);
            
            if (hits == null || hits.size() == 0)
                throw new PatientSearchException(ERROR_PATIENT_NOT_FOUND);
            
            if (maxMatches > 0 && hits.size() > maxMatches) {
                final String msg = MessageFormat.format(Labels.getLabel(WARN_TOO_MANY_MATCHES), hits.size());
                if ("Refine".equals(PromptDialog.show(msg, "Too Many Matches", "Refine|Continue", Messagebox.QUESTION)))
                    return null;
            }
            
            long[] ids = new long[hits.size()];
            int i = 0;
            
            for (String hit : hits) {
                String[] pcs = Util.split(hit, 2);
                int patientId = NumberUtils.toInt(pcs[0]);
                
                if (patientId <= 0)
                    throw new PatientSearchException(pcs[1]);
                
                ids[i++] = patientId;
                
            }
            
            List<Patient> matches = DomainObjectFactory.get(Patient.class, ids);
            
            if (log.isDebugEnabled())
                log.debug("Patient matches: " + matches.size());
            
            Collections.sort(matches, patientComparator);
            return matches;
        } catch (PatientSearchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during patient search.", e);
            throw new PatientSearchException(UNEXPECTED_ERROR, e);
        }
    }
}

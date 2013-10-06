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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.FrameworkUtil;
import gov.ihs.cwf.domain.Patient;

import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

/**
 * Controller for patient matches dialog.
 * 
 * @author dmartin
 */
public class PatientMatches extends Window {
	
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(PatientMatches.class);
	
	private DOBComparator dobComparatorAsc = new DOBComparator(true);
	
	private DOBComparator dobComparatorDsc = new DOBComparator(false);
	
	private Features features = Features.getInstance();
	
	/**
	 * Comparator for sorting by date of birth.
	 * 
	 * @author dmartin
	 */
	private class DOBComparator implements Comparator<Listitem> {
		
		private boolean ascending;
		
		DOBComparator(boolean ascending) {
			super();
			this.ascending = ascending;
		}
		
		@Override
		public int compare(Listitem o1, Listitem o2) {
			if (log.isDebugEnabled())
				log.debug("Listitem1: " + o1 + ", Listitem2: " + o2);
			Patient pat1 = (Patient) o1.getValue();
			Patient pat2 = (Patient) o2.getValue();
			Date dob1 = pat1.getBirthDate();
			Date dob2 = pat2.getBirthDate();
			int result = dob1 == null ? -1 : dob2 == null ? 1 : dob1.compareTo(dob2);
			return ascending ? result : -result;
		}
		
	}
	
	/**
	 * Returns the feature map for use by EL to determine if a given feature is enabled.
	 * 
	 * @return The feature map.
	 */
	public Map<String, Boolean> getFeatureEnabled() {
		return features.getFeatureMap();
	}
	
	/**
	 * Returns the date of birth ascending comparator.
	 * 
	 * @return Ascending DOB comparator.
	 */
	public DOBComparator getDOBComparatorAsc() {
		return dobComparatorAsc;
	}
	
	/**
	 * Returns the date of birth descending comparator.
	 * 
	 * @return Descending DOB comparator.
	 */
	public DOBComparator getDOBComparatorDsc() {
		return dobComparatorDsc;
	}
	
	/**
	 * Closes the dialog and returns the selected patient to the caller.
	 * 
	 * @param patient Patient selected by the user.
	 */
	public void selectPatient(Object patient) {
		log.trace("Start selectPatient()");
		FrameworkUtil.setAttribute(Constants.RESULT_ATTRIB, patient);
		detach();
	}
	
	/**
	 * Returns the patient list passed from the caller.
	 * 
	 * @return Patient list.
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getResults() {
		log.trace("Start getResults()");
		return (List<Patient>) FrameworkUtil.getAttribute(Constants.RESULT_ATTRIB);
	}
	
	/**
	 * Returns the size of the patient list.
	 * 
	 * @return Patient list size.
	 */
	public int getResultCount() {
		log.trace("Start getResultCount()");
		return getResults().size();
	}
}

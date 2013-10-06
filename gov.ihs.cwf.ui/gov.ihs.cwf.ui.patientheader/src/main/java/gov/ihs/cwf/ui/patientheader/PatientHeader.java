/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.patientheader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.ui.FrameworkController;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.ui.patientselection.PatientSelection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;

/**
 * Controller for patient header plugin.
 * 
 * @author dmartin
 */
public class PatientHeader extends FrameworkController implements PatientContext.IPatientContextEvent {
	
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(PatientHeader.class);
	
	private Label patientHeader;
	
	private String noSelectionMessage;
	
	private Component root;
	
	public void onClick$select() {
		PatientSelection.show();
	}
	
	public void onForceSelection() {
		PatientSelection.show();
	}
	
	/**
	 * Returns true if a patient selection should be forced when a null patient context exists.
	 * 
	 * @return
	 */
	private boolean forcePatientSelection() {
	
		try {
			return "Y".equals(PropertyUtil.getValue("CAREWEB.PATIENT.FORCE.SELECT", null));
		}
		catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		root = comp;
		noSelectionMessage = patientHeader.getValue();
		committed();
		
		// call the patient selection routine at login, if the user preference is set
		if (PatientContext.getCurrentPatient() == null && forcePatientSelection())
			Events.echoEvent("onForceSelection", comp, null);
	}
	
	@Override
	public void canceled() {
	}
	
	@Override
	public void committed() {
		Patient patient = PatientContext.getCurrentPatient();
		
		if (log.isDebugEnabled()) 
			log.debug("patient: " + patient);

		String text = "";
		
		if (patient == null) {
			text = noSelectionMessage;
		} else {
		    StringBuilder sb = new StringBuilder(patient.getFullName());
			String mrn = patient.getMedicalRecordNumber(); // May be null!
			sb.append("  #").append(mrn == null ? "Unknown" : mrn);
			sb.append("@").append(patient.getInstitution().getAbbreviation());
			sb.append("   (").append(patient.getGender()).append(")");
			sb.append("  Age: ").append(patient.getAgeForDisplay());
			
			if (patient.getDeathDate() != null)
				sb.append("  Died: ").append(DateUtil.formatDate(patient.getDeathDate()));
			
			text = sb.toString();
		}
		
		patientHeader.setValue(text);
		Clients.resize(root);
	}
	
	@Override
	public String pending(boolean silent) {
		return null;
	}
	
}

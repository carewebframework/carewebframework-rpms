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

import java.util.List;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.property.Property;

import org.zkoss.util.resource.Labels;

/**
 * This is the convenience class for accessing patient selectors.
 * 
 * @author dmartin
 */
public class PatientSelection {
    
    private PatientSelection() {
    }
    
    /**
     * Returns the current patient selector.  If one has not already been created, it is created from
     * the factory.
     * 
     * @return
     */
    private static IPatientSelector getSelector() {
        IPatientSelector selector = (IPatientSelector) FrameworkUtil.getAttribute(Constants.SELECTOR_ATTRIB);
        
        if (selector == null) {
            IPatientSelectorFactory factory = getFactory();
            selector = factory == null ? null : factory.create();
            FrameworkUtil.setAttribute(Constants.SELECTOR_ATTRIB, selector);
        }
        
        return selector;
    }
    
    /**
     * Returns the patient selector factory based on the PATIENT.SELECTION.SELECTOR property.  If this
     * property is not set, the first registered factory is returned.
     * 
     * @return
     */
    private static IPatientSelectorFactory getFactory() {
        String factoryBeanId;
        IPatientSelectorFactory factory;
        PatientSelectorRegistry registry = PatientSelectorRegistry.getInstance();
        
        try {
            factoryBeanId = new Property("PATIENT.SELECTION.SELECTOR").getValue();
        } catch (Exception e) {
            factoryBeanId = null;
        }
        
        factory = factoryBeanId == null ? null : registry.get(factoryBeanId);
        
        if (factory == null)
            if (registry.isEmpty())
                PromptDialog.showError("@patientselection.error.no.selectors");
            else
                factory = registry.iterator().next();
        
        return factory;
    }
    
    /**
     * Displays the new patient selection dialog.
     * 
     * @return The selected patient at the time the dialog was closed. This may be different from
     *         the patient in the shared context if <b>noContextChange</b> was true or the requested
     *         context change was rejected. It will be null if no patient was selected when the
     *         dialog was closed or if the selection was canceled by the user.
     */
    public static Patient show() {
        return show(false);
    }
    
    /**
     * Displays the new patient selection dialog.
     * 
     * @param noContextChange If true, no patient context change will be requested.
     * @return The selected patient at the time the dialog was closed. This may be different from
     *         the patient in the shared context if <b>noContextChange</b> was true or the requested
     *         context change was rejected. It will be null if no patient was selected when the
     *         dialog was closed or if the selection was canceled by the user.
     */
    public static Patient show(boolean noContextChange) {
        if (canSelect(true)) {
            IPatientSelector selector = getSelector();
            Patient patient = selector == null ? null : selector.select();
            return patient == null || noContextChange ? patient : changePatient(patient) ? patient : null;
        }
        return null;
    }
    
    /**
     * Invokes the patient match dialog, displaying the specified list of patients.
     * 
     * @param patientList List of patients from which to select.
     * @return The patient selected by the user or null if the operation was canceled.
     */
    public static Patient selectFromList(final List<Patient> patientList) {
        FrameworkUtil.setAttribute(Constants.RESULT_ATTRIB, patientList);
        PopupDialog.popup(Constants.RESOURCE_PREFIX + "patientMatches.zul");
        final Object result = FrameworkUtil.getAttribute(Constants.RESULT_ATTRIB);
        FrameworkUtil.setAttribute(Constants.RESULT_ATTRIB, null);
        return result instanceof Patient ? (Patient) result : null;
    }
    
    /**
     * Returns true if this user has patient selection privilege.
     * 
     * @param showMessage If true and the user does not have the required privilege, displays an
     *            error dialog.
     * @return
     */
    public static boolean canSelect(boolean showMessage) {
        boolean result = true; //SecurityUtil.isGranted("PRIV_PATIENT_SELECT");
        
        if (!result && showMessage) {
            PromptDialog.showError(Labels.getLabel(Constants.CANNOT_SELECT_MESSAGE),
                Labels.getLabel(Constants.CANNOT_SELECT_TITLE));
        }
        
        return result;
    }
    
    /**
     * Requests a context change for the specified patient and any accessible alternate
     * registrations. If the REGSELECT feature is enabled, the user is given the opportunity to
     * choose which registrations to select.
     * 
     * @param patient Patient to be selected into the context.
     * @return True if a context change was requested.
     */
    public static boolean changePatient(Patient patient) {
        PatientContext.changePatient(patient);
        return true;
    }
}

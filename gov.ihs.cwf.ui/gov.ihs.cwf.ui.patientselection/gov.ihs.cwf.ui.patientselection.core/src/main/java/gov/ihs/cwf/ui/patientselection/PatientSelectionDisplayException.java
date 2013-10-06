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

import org.carewebframework.shell.plugins.PluginExecutionException;

import org.zkoss.zk.ui.Execution;

/**
 * Exception class for patient selection related exceptions.
 * 
 * @author dmartin
 */
public class PatientSelectionDisplayException extends PluginExecutionException {
	
	private static final long serialVersionUID = 1L;
	
	public PatientSelectionDisplayException(Execution execution, String msg) {
		super(execution, msg);
	}
	
	public PatientSelectionDisplayException(Execution execution, String msg, Throwable t) {
		super(execution, msg, t);
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.common.bgo;

import java.util.List;

import gov.ihs.cwf.common.bgo.LookupParams.Table;
import gov.ihs.cwf.context.PatientContext;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ICDLookupController extends LookupController {
    
    private static final long serialVersionUID = 1L;
    
    private Radio radCode;
    
    private Radio radLexicon;
    
    public static String execute() {
        return execute((String) null);
    }
    
    public static String execute(String searchText) {
        return execute(searchText, false);
    }
    
    public static String execute(String searchText, boolean autoReturn) {
        return execute(searchText, autoReturn, null);
    }
    
    public static String execute(String searchText, boolean autoReturn, String screen) {
        return LookupController.execute(Table.rtICD, searchText, autoReturn, screen, new ICDLookupController());
    }
    
    public ICDLookupController() {
        mode = "ICD";
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }
    
    @Override
    protected List<String> executeRPC(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            searchText = "*";
        }
        
        String params = BgoUtil.concatParams(searchText, radLexicon.isChecked() ? "1" : "0", "", //m_sLookupDate Visit date
            PatientContext.getCurrentPatient().getGender(), "", //IIf(m_bEcodeMode, 2, IIf(m_bAllowEcode, 1, ""))
            "" // CInt(m_bDisplayShortText) ' VCodes
        );
        return broker.callRPCList(lookupParams.rpc, null, params);
    }
}

/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.ui.terminology.general.controller;

import java.util.List;

import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.Table;
import org.carewebframework.vista.api.util.VistAUtil;
import org.hspconsortium.cwf.api.patient.PatientContext;
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
        super("ICD");
    }

    @Override
    protected List<String> executeRPC(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            searchText = "*";
        }

        String params = VistAUtil.concatParams(searchText, radLexicon.isChecked() ? "1" : "0", "", //m_sLookupDate Visit date
            PatientContext.getActivePatient().getGender(), "", //IIf(m_bEcodeMode, 2, IIf(m_bAllowEcode, 1, ""))
            "" // CInt(m_bDisplayShortText) ' VCodes
        );
        return broker.callRPCList(lookupParams.getRpc(), null, params);
    }
}

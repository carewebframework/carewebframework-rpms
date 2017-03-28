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

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radio;

public class CPTLookupController extends LookupController {
    
    private static final long serialVersionUID = 1L;
    
    private Checkbox chkMedical;
    
    private Checkbox chkSurgical;
    
    private Checkbox chkHCPCS;
    
    private Checkbox chkEM;
    
    private Checkbox chkRadiology;
    
    private Checkbox chkLab;
    
    private Checkbox chkAnesthesia;
    
    private Checkbox chkHome;
    
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
        return LookupController.execute(Table.rtCPT, searchText, autoReturn, screen, new CPTLookupController());
    }
    
    public CPTLookupController() {
        super("mode");
    }
    
    @Override
    protected List<String> executeRPC(String searchText) {
        String params = VistAUtil.concatParams(lookupParams.getFileNum(), searchText, lookupParams.getFrom(),
            lookupParams.getDirection(), lookupParams.getMaxResults(), lookupParams.getXref(), screen,
            lookupParams.getAll(), lookupParams.getFields());
        return broker.callRPCList(lookupParams.getRpc(), null, params);
    }
    
}

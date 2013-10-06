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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.common.StrUtil;

public class LookupParams {
    
    public static enum Table {
        rtImmunization, rtSkinTest, rtLabTest, rtMeasurement, rtRadExam, rtExam, rtDrug, rtProcedure, rtClinic, rtDiscipline, rtHospLoc, rtProvider, rtICD, rtICDLex, rtLocation, rtCPT, rtCPTLex, rtCPTCategory, rtCPTModifier, rtTransCode, rtHealthFactor, rtVendor, rtEduTopic, rtICDProcedure, rtPatient
    }
    
    public static class ColumnControl {
        
        protected final String label;
        
        protected final boolean visible;
        
        protected final boolean capitalize;
        
        protected final int piece;
        
        protected final String width;
        
        private ColumnControl(String value) {
            String[] pcs = StrUtil.split(value, ":", 5);
            label = pcs[0];
            visible = !"1".equals(pcs[1]);
            piece = NumberUtils.toInt(pcs[2]);
            width = pcs[4].isEmpty() ? "60" : pcs[3];
            capitalize = "1".equals(pcs[4]);
        }
    }
    
    protected final Table table;
    
    protected String tableName;
    
    protected String fileNum;
    
    protected String rpc = "BGOUTL DICLKUP";
    
    protected String screen;
    
    protected String fields = ".01";
    
    protected String xref;
    
    protected String all;
    
    protected String from;
    
    protected int maxResults = 250;
    
    protected int sortCol = 2;
    
    protected String direction;
    
    protected boolean lookupNull = true;
    
    protected final List<ColumnControl> colControl = new ArrayList<ColumnControl>();
    
    public LookupParams(Table table) {
        this.table = table;
        String colControl = null;
        
        switch (table) {
            case rtImmunization:
                tableName = "Immunization";
                fileNum = "9999999.14";
                screen = ".07'=1";
                fields = ".01;1.14"; //".01;1.01;1.14";
                colControl = "ID:1:1^Imm #:1:0^Immunization:0:3^Brand Name:0:4^Long Name:1:5";
                break;
            
            case rtSkinTest:
                tableName = "Skin Test";
                fileNum = "9999999.28";
                screen = ".03'=1";
                break;
            
            case rtEduTopic:
                tableName = "Education Topic";
                fileNum = "9999999.09";
                screen = ".03'=1";
                break;
            
            case rtLabTest:
                tableName = "Lab Test";
                fileNum = "60";
                break;
            
            case rtMeasurement:
                tableName = "Measurement";
                fileNum = "9999999.07";
                fields = ".01;.02";
                colControl = "ID:1:1^Code:1:3^Measurement:0:4";
                break;
            
            case rtRadExam:
                tableName = "Radiology Exam";
                fileNum = "71";
                break;
            
            case rtExam:
                tableName = "Exam";
                fileNum = "9999999.15";
                screen = ".04'=1";
                break;
            
            case rtDrug:
                tableName = "Drug";
                fileNum = "50";
                break;
            
            case rtProcedure:
                tableName = "Diagnostic Procedure";
                fileNum = "9999999.68";
                break;
            
            case rtClinic:
                tableName = "Clinic";
                fileNum = "40.7";
                colControl = "ID:1:1^Code:1:4^Clinic:0:3::1";
                break;
            
            case rtDiscipline:
                tableName = "Discipline";
                fileNum = "7";
                break;
            
            case rtHospLoc:
                tableName = "Hospital Location";
                fileNum = "44";
                screen = "I $$ACTHLOC(Y)";
                colControl = "ID:1:1^Code:1:2^Location:0:2";
                break;
            
            case rtProvider:
                tableName = "Provider";
                fileNum = "200";
                screen = "I $$ACTPRV(Y)";
                colControl = "ID:1:1^Code:1:0^Provider:0:3";
                lookupNull = false;
                break;
            
            case rtICD:
                tableName = "Diagnosis";
                rpc = "BGOICDLK ICDLKUP";
                colControl = "ID:1:2^Code:1:4^Diagnosis:0:1::1";
                break;
            
            case rtICDLex:
                tableName = "Diagnosis";
                rpc = "BGOUTL ICDLEX";
                colControl = "ID:1:2^Code:1:4^Diagnosis:0:1::1";
                break;
            
            case rtLocation:
                tableName = "Location";
                fileNum = "4";
                colControl = "ID:1:1^Code:1:0^Location:0:3";
                break;
            
            case rtCPT:
                tableName = "CPT";
                rpc = "BGOVCPT CPTLKUP";
                break;
            
            case rtCPTLex:
                tableName = "CPT";
                rpc = "BGOUTL LEXLKUP";
                break;
            
            case rtCPTCategory:
                tableName = "CPT Category";
                rpc = "BGOCPTPR OTHCATS";
                break;
            
            case rtCPTModifier:
                tableName = "CPT Modifier";
                fileNum = "9999999.88"; // This will be redirected to 81.3 if CSV is active
                fields = ".01;.02";
                xref = "B~C";
                sortCol = 1;
                colControl = "ID:1:1^Code:0:2^CPT Modifier:0:4";
                break;
            
            case rtTransCode:
                tableName = "Transaction Code";
                rpc = "BGOVTC CMLKUP";
                fields = ".01;.07;.05;.06";
                colControl = "ID:1:1^Code:0:2^Transaction Description:0:3^CAN:0:4^Class:0:5";
                break;
            
            case rtHealthFactor:
                tableName = "Health Factor";
                fileNum = "9999999.64";
                fields = ".01";
                screen = ".13'=1";
                break;
            
            case rtVendor:
                tableName = "Vendor";
                fileNum = "9999999.11";
                break;
            
            case rtICDProcedure:
                tableName = "ICD Procedure";
                fileNum = "80.1";
                fields = ".01;10";
                screen = "100'=1";
                colControl = "ID:1:1^Code:0:2^Procedure:0:4";
                break;
            
            case rtPatient:
                tableName = "Patient";
                fileNum = "2";
                fields = ".01;.02;.03;.09";
                lookupNull = false;
                break;
        }
        
        parseColumnControl(colControl == null ? "ID:1:1^Code:1:4^" + tableName + ":0:3" : colControl);
    }
    
    private void parseColumnControl(String value) {
        String pcs[] = value.split("\\^");
        
        for (int i = 0; i < pcs.length; i++) {
            colControl.add(new ColumnControl(pcs[i]));
        }
    }
}

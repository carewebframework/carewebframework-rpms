/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.smart.immunization;

import java.util.List;
import java.util.Map;

import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.domain.Contraindication;
import gov.ihs.cwf.domain.Immunization;
import gov.ihs.cwf.domain.Refusal;

import org.carewebframework.smart.rdf.RDFDescription;
import org.carewebframework.smart.rdf.RDFDocument;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.smart.SmartRDFAPI;

public class SmartImmunizations extends SmartRDFAPI {
    
    public SmartImmunizations() {
        super("/records/{record_id}/immunizations", "Immunizations");
    }
    
    @Override
    public void handleAPI(RDFDocument doc, Map<String, String> params) {
        String record_id = params.get("record_id");
        String owner = doc.baseURL + "/records/" + record_id;
        List<String> data = VistAUtil.getBrokerSession().callRPCList("BGOVIMM GET", null, record_id);
        
        if (BgoUtil.errorCheck(data)) {
            return;
        }
        
        for (String value : data) {
            switch (value.charAt(0)) {
                case 'I':
                    handleImmunization(doc, new Immunization(value), owner);
                    break;
                case 'R':
                    handleRefusal(doc, new Refusal(value), owner);
                    break;
                case 'C':
                    handleContraindication(doc, new Contraindication(value), owner);
                    break;
            }
        }
    }
    
    private void handleImmunization(RDFDocument doc, Immunization imm, String owner) {
        /*
            <rdf:Description rdf:about="http://sandbox-api.smartplatforms.org/records/880378/immunizations/1f24e0c1-e101-4990-a7a4-9b90a373dd7c">
                <rdf:type rdf:resource="http://smartplatforms.org/terms#Immunization"/>
                <belongsTo rdf:resource="http://sandbox-api.smartplatforms.org/records/880378"/>
                <date xmlns="http://purl.org/dc/terms/">2009-03-01</date>
                <productName rdf:nodeID="node16rk1fgdvx351120"/>
                <productClass rdf:nodeID="node16rk1fgdvx351122"/>
                <administrationStatus rdf:nodeID="node16rk1fgdvx351121"/>
                <refusalReason rdf:nodeID="node16rk1fgdvx351119"/>
            </rdf:Description>
         */
        String about = owner + "/immunizations/" + imm.getDomainId();
        RDFDescription dx = doc.addDescription(about, "#Immunization");
        RDFDescription pn;
        
        if (imm.getCvx() != null) {
            pn = doc.addCodedValue("http://www2a.cdc.gov/nip/IIS/IISStandards/vaccines.asp?rpt=cvx#", imm.getCvx(), imm
                .getImmunization().getDisplaySimple(), "ImmunizationProduct");
        } else {
            pn = doc.addCodedValue("http://smartplatforms.org/terms/codes/Immunization#", ""
                    + imm.getImmunization().getDomainId(), imm.getImmunization().getDisplaySimple(), "ImmunizationProduct");
        }
        //RDFDescription pc = doc.addCodedValue();
        RDFDescription as = doc.addCodedValue("http://smartplatforms.org/terms/codes/ImmunizationAdministrationStatus#",
            "doseGiven", "Dose Given", "ImmunizationAdministrationStatus");
        
        dx.addResource("belongsTo", owner);
        dx.addChild("dc:date", imm.getEventDate());
        dx.addChild("productName", pn);
        //dx.addChild("productClass", pc);
        dx.addChild("administrationStatus", as);
    }
    
    private void handleRefusal(RDFDocument doc, Refusal ref, String owner) {
        String about = owner + "/immunizations/" + ref.getDomainId();
        RDFDescription dx = doc.addDescription(about, "#Immunization");
        RDFDescription pn = doc.addCodedValue("http://smartplatforms.org/terms/codes/Immunization#", ""
                + ref.getItem().getDomainId(), ref.getItem().getCodeSimple(), "ImmunizationProduct");
        RDFDescription as = doc.addCodedValue("http://smartplatforms.org/terms/codes/ImmunizationAdministrationStatus#",
            "notAdministered", "Not Administered", "ImmunizationAdministrationStatus");
        RDFDescription rr = doc.addCodedValue("http://smartplatforms.org/terms/codes/ImmunizationRefusalReason#",
            "patientRefused", ref.getReason(), "ImmunizationRefusalReason");
        
        dx.addResource("belongsTo", owner);
        dx.addChild("dc:date", ref.getDate());
        dx.addChild("productName", pn);
        dx.addChild("administrationStatus", as);
        dx.addChild("refusalReason", rr);
    }
    
    private void handleContraindication(RDFDocument doc, Contraindication contra, String owner) {
        String about = owner + "/immunizations/" + contra.getDomainId();
        RDFDescription dx = doc.addDescription(about, "#Immunization");
        RDFDescription pn = doc.addCodedValue("http://smartplatforms.org/terms/codes/Immunization", "unknown",
            contra.getImmunization(), "ImmunizationProduct");
        //RDFDescription pc = doc.addDescription(null, "#CodedValue");
        RDFDescription as = doc.addCodedValue("http://smartplatforms.org/terms/codes/ImmunizationAdministrationStatus#",
            "notAdministered", "Not Administered", "ImmunizationAdministrationStatus");
        RDFDescription rr = doc.addCodedValue("http://smartplatforms.org/terms/codes/ImmunizationRefusalReason#",
            "contraindication", contra.getReason(), "ImmunizationRefusalReason");
        
        dx.addResource("belongsTo", owner);
        dx.addChild("dc:date", contra.getDate());
        dx.addChild("productName", pn);
        dx.addChild("administrationStatus", as);
        dx.addChild("refusalReason", rr);
    }
}

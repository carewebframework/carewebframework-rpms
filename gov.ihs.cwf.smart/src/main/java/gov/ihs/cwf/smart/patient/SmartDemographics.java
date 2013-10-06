/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.smart.patient;

import java.util.Map;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Person.Name;
import gov.ihs.cwf.smart.RPMSAPIBase;

import org.carewebframework.smart.rdf.RDFDescription;
import org.carewebframework.smart.rdf.RDFDocument;

import org.w3c.dom.Element;

public class SmartDemographics extends RPMSAPIBase {
    
    private static final String VCARD = "http://www.w3.org/2006/vcard/ns#";
    
    private static final String FOAF = "http://xmlns.com/foaf/0.1/";
    
    private static final String TERMS = "http://purl.org/dc/terms/";
    
    public SmartDemographics() {
        super("/records/{record_id}/demographics", "Demographics");
    }
    
    @Override
    public void handleAPI(RDFDocument doc, Map<String, String> params) {
        doc.registerNamespace("foaf", FOAF);
        Patient patient = PatientContext.getCurrentPatient();
        String owner = "/records/" + patient.getDomainId();
        RDFDescription dem = doc.addDescription(owner + "/demographics", "#Demographics");
        RDFDescription mrn = doc.addDescription(null, "#Code");
        RDFDescription name = doc.addDescription(null, VCARD + "Name");
        RDFDescription addr = doc.addDescription(null, VCARD + "Home", VCARD + "Pref", VCARD + "Address");
        
        dem.addChild("belongsTo").setAttribute("rdf:resource", doc.baseURL + owner);
        addVCardEntry(dem, "bday", patient.getBirthDate());
        addVCardEntry(dem, "n", name);
        
        // Home Phone #
        if (true) { // Not available yet
            RDFDescription tel = doc.addDescription(null, VCARD + "Tel", VCARD + "Home", VCARD + "Pref");
            tel.addChild("rdf:value", "317-555-1212");
            addVCardEntry(dem, "tel", tel);
        }
        
        // Cell Phone #
        if (true) { // Not available yet
            RDFDescription tel = doc.addDescription(null, VCARD + "Tel", VCARD + "Cell");
            tel.addChild("rdf:value", "317-555-1213");
            addVCardEntry(dem, "tel", tel);
        }
        
        dem.addChild("medicalRecordNumber", mrn);
        dem.addChild("gender", patient.getGender(), FOAF);
        addVCardEntry(dem, "adr", addr);
        addVCardEntry(dem, "email", "test@gmail.com"); //temp
        
        // MRN
        mrn.addChild("title", "MRN #" + patient.getMedicalRecordNumber(), TERMS);
        mrn.addChild("system", patient.getInstitution().getName());
        mrn.addChild("identifier", patient.getMedicalRecordNumber(), TERMS);
        
        // Name
        Name nm = patient.getName();
        addVCardEntry(name, "family-name", nm.getLastName());
        addVCardEntry(name, "given-name", nm.getFirstName());
        addVCardEntry(name, "additional-name", nm.getMiddleName());
        
        // Address
        addVCardEntry(addr, "extended-address", "Apt 1");
        addVCardEntry(addr, "street-address", "48 Hill Rd");
        addVCardEntry(addr, "region", "OK");
        addVCardEntry(addr, "postal-code", "74066");
        addVCardEntry(addr, "country", "USA");
        addVCardEntry(addr, "locality", "Sapulpa");
    }
    
    private Element addVCardEntry(RDFDescription parent, String tag, Object value) {
        return parent.addChild(tag, value, VCARD);
    }
    
}

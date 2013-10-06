/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.smart.problem;

import java.util.List;
import java.util.Map;

import gov.ihs.cwf.common.bgo.BgoUtil;
import gov.ihs.cwf.domain.ICD9Concept;
import gov.ihs.cwf.domain.Problem;
import gov.ihs.cwf.smart.RPMSAPIBase;
import gov.ihs.cwf.util.RPMSUtil;

import org.carewebframework.smart.rdf.RDFDescription;
import org.carewebframework.smart.rdf.RDFDocument;

public class SmartProblems extends RPMSAPIBase {
    
    private static final String ICD = "http://purl.bioontology.org/ontology/ICD-9/";
    
    public SmartProblems() {
        super("/records/{record_id}/problems", "Problems");
    }
    
    @Override
    public void handleAPI(RDFDocument doc, Map<String, String> params) {
        String record_id = params.get("record_id");
        String owner = "/records/" + record_id;
        List<String> data = RPMSUtil.getBrokerSession().callRPCList("BGOPROB GET", null, record_id);
        
        if (data != null && !data.isEmpty()) {
            BgoUtil.errorCheck(data);
            
            for (String s : data) {
                addProblem(doc, new Problem(s), owner);
            }
        }
    }
    
    private void addProblem(RDFDocument doc, Problem problem, String owner) {
        ICD9Concept icd = problem.getIcd9Code();
        RDFDescription node = doc.addDescription(owner + "/problems/" + problem.getDomainId(), "#Problem");
        RDFDescription code = doc.addCodedValue(ICD, icd.getCode(), icd.getShortDescription(), "ICD9");
        node.addChild("startDate", problem.getOnsetDate());
        node.addResource("belongsTo", doc.baseURL + owner);
        node.addChild("problemName", code);
    }
}

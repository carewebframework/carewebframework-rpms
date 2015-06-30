/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.terminology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.StrUtil;

/**
 * Represents a single term match from a term search query.
 */
public class TermMatch {
    
    private static final String SUB_DELIMITER = "\034";
    
    private static final String SUBSUB_DELIMITER = "\035";
    
    public enum TermType {
        FSN, PREFERRED, SYNONYM, PROBLEM
    };
    
    public static class Term {
        
        public final String descriptionId;
        
        public final String termText;
        
        public final TermType termType;
        
        public Term(TermType termType, String termText, String descriptionId) {
            this.termType = termType;
            this.termText = termText;
            this.descriptionId = descriptionId;
        }
        
        public String getDescriptionId() {
            return descriptionId;
        }
        
        public String getTermText() {
            return termText;
        }
        
        public TermType getTermType() {
            return termType;
        }
        
        @Override
        public boolean equals(Object object) {
            return object instanceof Term && ((Term) object).descriptionId.equals(descriptionId);
        }
        
    }
    
    public static class ParentTerm {
        
        private final String dtsId;
        
        private final String conceptId;
        
        private final String termText;
        
        public ParentTerm(String data) {
            String[] pcs = StrUtil.split(data, SUBSUB_DELIMITER, 3);
            dtsId = pcs[0];
            conceptId = pcs[1];
            termText = pcs[2];
        }
        
        public String getDtsId() {
            return dtsId;
        }
        
        public String getConceptId() {
            return conceptId;
        }
        
        public String getTermText() {
            return termText;
        }
        
    }
    
    private final Map<TermType, List<Term>> termsByType = new HashMap<>();
    
    private final List<ParentTerm> parentTerms = new ArrayList<>();
    
    private final String dtsId;
    
    private final String code;
    
    private final String[] mappedICDs;
    
    private final String[] subsets;
    
    public TermMatch(String data) {
        String[] pcs = StrUtil.split(data, StrUtil.U, 14);
        addTerm(TermType.PROBLEM, pcs[1], pcs[0]);
        addTerm(TermType.PREFERRED, pcs[3], pcs[2]);
        code = pcs[4];
        dtsId = pcs[5];
        addTerm(TermType.FSN, pcs[7], pcs[6]);
        
        for (String parentTerm : pcs[8].split(SUB_DELIMITER)) {
            parentTerms.add(new ParentTerm(parentTerm));
        }
        
        mappedICDs = pcs[9].split(SUB_DELIMITER);
        subsets = pcs[10].split(SUB_DELIMITER);
        // pcs[11] ICD10 - ignore
        
        for (String synonym : pcs[12].split(SUB_DELIMITER)) {
            addTerm(synonym);
        }
        
        // pcs[13] isHdr - ignore
        // pcs[14] same as pcs[9]
    }
    
    public List<Term> getTerms(TermType termType) {
        List<Term> terms = termsByType.get(termType);
        
        if (terms == null) {
            termsByType.put(termType, terms = new ArrayList<Term>());
        }
        
        return terms;
    }
    
    public Term getTerm(TermType termType) {
        List<Term> terms = termsByType.get(termType);
        return terms != null && !terms.isEmpty() ? terms.iterator().next() : null;
    }
    
    private void addTerm(String data) {
        String pcs[] = StrUtil.split(data, SUBSUB_DELIMITER, 3);
        addTerm(TermType.valueOf(pcs[2].toUpperCase()), pcs[1], pcs[0]);
    }
    
    private void addTerm(TermType termType, String termText, String descriptionId) {
        Term term = new Term(termType, termText, descriptionId);
        List<Term> terms = getTerms(termType);
        
        if (!terms.contains(term)) {
            terms.add(term);
        }
    }
    
    public List<ParentTerm> getParentTerms() {
        return parentTerms;
    }
    
    public String getDtsId() {
        return dtsId;
    }
    
    public String getCode() {
        return code;
    }
    
    public String[] getMappedICDs() {
        return mappedICDs;
    }
    
    public String[] getSubsets() {
        return subsets;
    }
}

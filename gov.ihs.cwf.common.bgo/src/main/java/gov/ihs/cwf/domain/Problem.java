/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.domain;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.domain.ICD9Concept;
import org.carewebframework.vista.api.domain.Institution;
import org.carewebframework.vista.api.domain.Patient;
import org.carewebframework.vista.api.domain.Provider;
import org.carewebframework.vista.mbroker.FMDate;

public class Problem extends DomainObject {

    private static final long serialVersionUID = 1L;

    static {
        JSONUtil.registerAlias("Problem", Problem.class);
    }

    private String status;

    private String priority;

    private FMDate modifyDate;

    private FMDate entryDate;

    private FMDate onsetDate;

    private String numberCode;

    private Patient patient;

    private Provider provider;

    private Institution facility;

    private ICD9Concept icd9Code;

    private String problemClass;

    private String providerNarrative;

    private String notes;

    public Problem(Patient patient) {
        super();
        this.patient = patient;
        this.facility = patient.getInstitution();
    }

    /**
     * Temporary constructor to create a problem from serialized form (will move to json).
     *
     * @param value Number Code [1] ^ Patient IEN [2] ^ ICD Code [3] ^ Modify Date [4] ^ Class [5] ^
     *            Provider Narrative [6] ^ Date Entered [7] ^ Status [8] ^ Date Onset [9] ^ Problem
     *            IEN [10] ^ Notes [11] ^ ICD9 IEN [12] ^ ICD9 Short Name [13] ^ Provider [14] ^
     *            Facility IEN [15] ^ Priority [16] e.g., TC9^72101^472.0^05/23/2001^U^ATROPHIC
     *            RHINITIS^05/23/2001^A^^27771^^9102^CHRONIC RHINITIS^^3987^
     */
    public Problem(String value) {
        String pcs[] = StrUtil.split(value, StrUtil.U, 16);
        numberCode = pcs[0];
        patient = new Patient(pcs[1]);
        icd9Code = new ICD9Concept();
        icd9Code.setCode(pcs[2]);
        icd9Code.setDomainId(pcs[11]);
        icd9Code.setShortDescription(pcs[12]);
        modifyDate = parseDate(pcs[3]);
        problemClass = pcs[4];
        providerNarrative = pcs[5];
        entryDate = parseDate(pcs[6]);
        status = pcs[7];
        onsetDate = parseDate(pcs[8]);
        setDomainId(pcs[9]);
        notes = pcs[10];

        if (!pcs[13].isEmpty()) {
            provider = new Provider();
            provider.setFullName(pcs[13]);
        }

        if (!pcs[14].isEmpty()) {
            facility = new Institution(pcs[14]);
        }

        priority = pcs[15];
    }

    private FMDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        return new FMDate(DateUtil.parseDate(value));
    }

    public String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    public FMDate getModifyDate() {
        return modifyDate;
    }

    protected void setModifyDate(FMDate modifyDate) {
        this.modifyDate = modifyDate;
    }

    public FMDate getEntryDate() {
        return entryDate;
    }

    protected void setEntryDate(FMDate entryDate) {
        this.entryDate = entryDate;
    }

    public FMDate getOnsetDate() {
        return onsetDate;
    }

    protected void setOnsetDate(FMDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    public String getNumberCode() {
        return numberCode;
    }

    protected void setNumberCode(String numberCode) {
        this.numberCode = numberCode;
    }

    public Patient getPatient() {
        return patient;
    }

    protected void setPatient(Patient patient) {
        this.patient = patient;
    }

    public ICD9Concept getIcd9Code() {
        return icd9Code;
    }

    protected void setIcd9Code(ICD9Concept icd9Code) {
        this.icd9Code = icd9Code;
    }

    public String getProblemClass() {
        return problemClass;
    }

    protected void setProblemClass(String problemClass) {
        this.problemClass = problemClass;
    }

    public String getProviderNarrative() {
        return providerNarrative;
    }

    protected void setProviderNarrative(String providerNarrative) {
        this.providerNarrative = providerNarrative;
    }

    public String getPriority() {
        return priority;
    }

    protected void setPriority(String priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    protected void setNotes(String notes) {
        this.notes = notes;
    }

    public Institution getFacility() {
        return facility;
    }

    public void setFacility(Institution facility) {
        this.facility = facility;
    }

}

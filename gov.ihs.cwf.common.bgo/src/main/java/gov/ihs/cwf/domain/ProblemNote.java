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
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.domain.Institution;
import org.carewebframework.vista.mbroker.FMDate;

public class ProblemNote extends DomainObject {

    private static final long serialVersionUID = 1L;

    static {
        JSONUtil.registerAlias("ProblemNote", ProblemNote.class);
    }

    private IInstitution facility;

    private String number;

    private String narrative;

    private String status;

    private FMDate dateAdded;

    private String author;

    public ProblemNote() {

    }

    /**
     * Temporary constructor to create a problem note from serialized form (will move to json).
     *
     * @param value <code>
     * Location IEN [1] ^ Note IEN [2] ^ Note # [3] ^ Narrative [4] ^
     * Status [5] ^ Date Added [6] ^ Author Name [7]
     * e.g.,
     * 3987^1^1^STECWFD DEPENDENCY (LOW DOSE)^A^2960901^
     * </code>
     */
    public ProblemNote(String value) {
        String pcs[] = StrUtil.split(value, StrUtil.U, 7);
        this.facility = new Institution(pcs[0]);
        setDomainId(pcs[1]);
        this.number = pcs[2];
        this.narrative = pcs[3];
        this.status = pcs[4];
        this.dateAdded = new FMDate(pcs[5]);
        this.author = pcs[6];
    }

    public IInstitution getFacility() {
        return facility;
    }

    public void setFacility(IInstitution facility) {
        this.facility = facility;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FMDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(FMDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}

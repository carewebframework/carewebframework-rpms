/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.domain;

import ca.uhn.fhir.model.dstu.resource.Encounter;
import ca.uhn.fhir.model.dstu.resource.Location;
import ca.uhn.fhir.model.dstu.resource.Practitioner;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.domain.DomainFactoryRegistry;
import org.carewebframework.cal.api.encounter.EncounterRelated;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.FMDate;

public class Immunization extends EncounterRelated {
    
    static {
        JSONUtil.registerAlias("Immunization", Immunization.class);
    }
    
    private FMDate eventDate;
    
    private Practitioner provider;
    
    private Location location;
    
    private String lot;
    
    private String reaction;
    
    private String site;
    
    private String volume;
    
    private CodingProxy immunization;
    
    private String age;
    
    private String cvx;
    
    public Immunization() {
        super();
    }
    
    /**
     * Temporary constructor to create an immunization from serialized form (will move to json).
     *
     * @param value I ^ Imm Name [2] ^ Visit Date [3] ^ V File IEN [4] ^ Other Location [5] ^ Group
     *            [6] ^ Imm IEN [7] ^ Lot [8] ^ Reaction [9] ^ VIS Date [10] ^ Age [11] ^ Visit Date
     *            [12] ^ Provider IEN~Name [13] ^ Inj Site [14] ^ Volume [15] ^ Visit IEN [16] ^
     *            Visit Category [17] ^ Full Name [18] ^ Location IEN~Name [19] ^ Visit Locked [20]
     *            ^ Event Date/Time [21] ^ Dose Override [22] ^ VPED IEN [23] e.g.,
     *            I^Td-ADULT^09/25/1996^86751^^TD_B^102^^^^61 yrs^25-Sep-1996^^^^553825^A^TD
     *            (ADULT)^3987~EHR TEST SITE^1 I^HEP A^03/17/1995^250950^Home^HEPA^131^^^^59
     *            yrs^17-Mar-1995^^^^2020333^E^HEP A, NOS^4744~OTHER^1
     */
    public Immunization(String value) {
        String[] pcs = StrUtil.split(value, StrUtil.U, 23);
        setId(pcs[3]);
        immunization = new CodingProxy(pcs[6], "IMMUNIZATION", pcs[1], pcs[1]);
        
        String loc = pcs[4];
        
        if (loc.isEmpty()) {
            location = PCCUtil.parseLocation(pcs[18]);
        } else {
            location = new Location();
            location.setName(loc);
        }
        
        lot = pcs[7];
        reaction = pcs[8];
        site = pcs[13];
        volume = pcs[14];
        age = pcs[10];
        provider = PCCUtil.parsePractitioner(pcs[12]);
        long visitId = NumberUtils.toLong(pcs[15]);
        setEncounter(visitId > 0 ? DomainFactoryRegistry.fetchObject(Encounter.class, pcs[15]) : null);
        eventDate = PCCUtil.parseDate(pcs[20].isEmpty() ? pcs[2] : pcs[20]);
    }
    
    public FMDate getEventDate() {
        return eventDate;
    }
    
    protected void setEntryDate(FMDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public Practitioner getProvider() {
        return provider;
    }
    
    public void setProvider(Practitioner provider) {
        this.provider = provider;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public CodingProxy getImmunization() {
        return immunization;
    }
    
    public void setImmunization(CodingProxy immunization) {
        this.immunization = immunization;
    }
    
    public String getAge() {
        return age;
    }
    
    public void setAge(String age) {
        this.age = age;
    }
    
    public void setEventDate(FMDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getLot() {
        return lot;
    }
    
    public void setLot(String lot) {
        this.lot = lot;
    }
    
    public String getReaction() {
        return reaction;
    }
    
    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
    
    public String getSite() {
        return site;
    }
    
    public void setSite(String site) {
        this.site = site;
    }
    
    public String getVolume() {
        return volume;
    }
    
    public void setVolume(String volume) {
        this.volume = volume;
    }
    
    public String getCvx() {
        return cvx;
    }
    
    public void setCvx(String cvx) {
        this.cvx = cvx;
    }
    
}

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

import gov.ihs.cwf.common.bgo.PCC;
import gov.ihs.cwf.factory.DomainObjectFactory;
import gov.ihs.cwf.mbroker.FMDate;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class Immunization extends EncounterRelated {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Immunization", Immunization.class);
    }
    
    private FMDate eventDate;
    
    private User provider;
    
    private Location location;
    
    private String lot;
    
    private String reaction;
    
    private String site;
    
    private String volume;
    
    private Concept immunization;
    
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
        setDomainId(Long.parseLong(pcs[3]));
        immunization = new Concept("IMMUNIZATION");
        immunization.setCode(pcs[1]);
        immunization.setDomainId(NumberUtils.toLong(pcs[6]));
        immunization.setShortDescription(pcs[1]);
        
        String loc = pcs[4];
        
        if (loc.isEmpty()) {
            location = PCC.parseLocation(pcs[18]);
        } else {
            location = new Location(loc, null, null);
        }
        
        lot = pcs[7];
        reaction = pcs[8];
        site = pcs[13];
        volume = pcs[14];
        age = pcs[10];
        provider = PCC.parseUser(pcs[12]);
        long visitId = NumberUtils.toLong(pcs[15]);
        setEncounter(visitId > 0 ? DomainObjectFactory.get(Encounter.class, visitId) : null);
        eventDate = PCC.parseDate(pcs[20].isEmpty() ? pcs[2] : pcs[20]);
    }
    
    public FMDate getEventDate() {
        return eventDate;
    }
    
    protected void setEntryDate(FMDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public User getProvider() {
        return provider;
    }
    
    public void setProvider(User provider) {
        this.provider = provider;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Concept getImmunization() {
        return immunization;
    }
    
    public void setImmunization(Concept immunization) {
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

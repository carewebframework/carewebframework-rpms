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
import gov.ihs.cwf.mbroker.FMDate;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class SkinTest extends EncounterRelated {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("SkinTest", SkinTest.class);
    }
    
    private FMDate eventDate;
    
    private FMDate readDate;
    
    private User reader;
    
    private User provider;
    
    private Location location;
    
    private String result;
    
    private String reading;
    
    private Concept test;
    
    private String age;
    
    public SkinTest() {
        super();
    }
    
    /**
     * Temporary constructor to create a problem from serialized form (will move to json).
     * 
     * @param value S ^ Visit Date [2] ^ VFile IEN [3] ^ Other Location [4] ^ Result [5] ^ Reading
     *            [6] ^ Date Read [7] ^ Test Name [8] ^ Test IEN [9] ^ Age [10] ^ Provider IEN~Name
     *            [11] ^ Reader IEN~Name [12] ^ Visit IEN [13] ^ Service Category [14] ^ Location
     *            IEN~Name [15] ^ Visit Locked [16] ^ Event Date [ 17] e.g.,
     *            S^11/01/2004^7403^^^^^PPD^2^69 yrs^2779~USER,POWER^^2020429^A^3987~EHR TEST SITE^1
     *            S^10/31/2006^7400^Test Location^^^^PPD^2^71 yrs^^^2020427^E^4744~OTHER^1
     *            S^10/31/2006^7401^Test Location^Refused^^^COCCI^3^71 yrs^^^2020427^E^4744~OTHER^1
     *            S^11/01/2006^7402^^^^^PPD^2^71 yrs^2779~USER,POWER^^2020428^A^3987~EHR TEST
     */
    public SkinTest(String value) {
        String[] pcs = StrUtil.split(value, StrUtil.U, 17);
        setDomainId(Long.parseLong(pcs[2]));
        String loc = pcs[3];
        
        if (loc.isEmpty()) {
            location = PCC.parseLocation(pcs[14]);
        } else {
            location = new Location(loc, null, null);
        }
        
        result = pcs[4]; // SetProperCase?
        
        if (result.isEmpty()) {
            result = "Pending";
        } else {
            readDate = PCC.parseDate(pcs[6]);
        }
        
        test = new Concept("SKIN TEST");
        test.setCode(pcs[7]);
        test.setDomainId(NumberUtils.toLong(pcs[8]));
        test.setShortDescription(pcs[7]);
        age = pcs[9];
        provider = PCC.parseUser(pcs[10]);
        reader = PCC.parseUser(pcs[11]);
        long visitId = NumberUtils.toLong(pcs[12]);
        setEncounter(visitId > 0 ? DomainObjectFactory.get(Encounter.class, visitId) : null);
        eventDate = PCC.parseDate(pcs[16].isEmpty() ? pcs[1] : pcs[16]);
        
        if (getEncounter() != null && "E".equals(getEncounter().getServiceCategory()) && "0".equals(reading)) {
            reading = "";
        }
    }
    
    public FMDate getEventDate() {
        return eventDate;
    }
    
    protected void setEntryDate(FMDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public FMDate getReadDate() {
        return readDate;
    }
    
    public void setReadDate(FMDate readDate) {
        this.readDate = readDate;
    }
    
    public User getReader() {
        return reader;
    }
    
    public void setReader(User reader) {
        this.reader = reader;
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getReading() {
        return reading;
    }
    
    public void setReading(String reading) {
        this.reading = reading;
    }
    
    public Concept getTest() {
        return test;
    }
    
    public void setTest(Concept test) {
        this.test = test;
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
    
}

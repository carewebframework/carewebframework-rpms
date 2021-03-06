/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.api.domain;

import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.api.domain.DomainFactoryRegistry;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.encounter.EncounterUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.api.encounter.EncounterRelated;

public class SkinTest extends EncounterRelated {

    static {
        JSONUtil.registerAlias("SkinTest", SkinTest.class);
    }

    private FMDate eventDate;

    private FMDate readDate;

    private Practitioner reader;

    private Practitioner provider;

    private Location location;

    private String result;

    private String reading;

    private CodingProxy test;

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
        setId(pcs[2]);
        String loc = pcs[3];

        if (loc.isEmpty()) {
            location = PCCUtil.parseLocation(pcs[14]);
        } else {
            location = new Location();
            location.setName(loc);
        }

        result = pcs[4]; // SetProperCase?

        if (result.isEmpty()) {
            result = "Pending";
        } else {
            readDate = PCCUtil.parseDate(pcs[6]);
        }

        test = new CodingProxy(pcs[8], "SKIN TEST", pcs[7], pcs[7]);
        age = pcs[9];
        provider = PCCUtil.parsePractitioner(pcs[10]);
        reader = PCCUtil.parsePractitioner(pcs[11]);
        long visitId = NumberUtils.toLong(pcs[12]);
        setEncounter(visitId > 0 ? DomainFactoryRegistry.fetchObject(Encounter.class, pcs[12]) : null);
        eventDate = PCCUtil.parseDate(pcs[16].isEmpty() ? pcs[1] : pcs[16]);

        if (getEncounter() != null && "E".equals(EncounterUtil.getServiceCategory(getEncounter())) && "0".equals(reading)) {
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

    public Practitioner getReader() {
        return reader;
    }

    public void setReader(Practitioner reader) {
        this.reader = reader;
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

    public CodingProxy getTest() {
        return test;
    }

    public void setTest(CodingProxy test) {
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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import gov.ihs.cwf.domain.DomainObjectFactory;
import gov.ihs.cwf.domain.Person;
import gov.ihs.cwf.domain.User;
import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.property.PropertyDefinition;

import org.carewebframework.api.test.CommonTest;
import org.carewebframework.cal.api.domain.Name;
import org.carewebframework.common.JSONUtil;

import org.junit.Test;

public class SerializationTest extends CommonTest {
    
    private static final String PERSON_DATA = "{\"@class\": \"Person\"," + "\"domainId\": 1234,"
            + "\"birthDate\": \"2580727\"," + "\"name\":{" + "\"firstName\":\"Douglas\"," + "\"middleName\":\"Kent\","
            + "\"lastName\":\"Martin\"}" + "}";
    
    @Test
    public void testDeserializer() {
        new FMDate();
        JSONUtil.registerAlias("Person", Person.class);
        JSONUtil.registerAlias("Name", Name.class);
        Person person = (Person) JSONUtil.deserialize(PERSON_DATA);
        assertTrue(person.getDomainId() == 1234);
        assertTrue("Martin, Douglas Kent".equals(person.getFullName()));
        assertTrue("2580727".equals((new FMDate(person.getBirthDate())).getFMDate()));
    }
    
    @Test
    public void testFactory() throws Exception {
        User user = DomainObjectFactory.get(User.class, 1);
        assertTrue(user.getDomainId() == 1);
        List<User> users = DomainObjectFactory.get(User.class, new long[] { 1 });
        assertTrue(users.size() == 1);
        assertEquals(user, users.get(0));
    }
    
    @Test
    public void testProperty() throws Exception {
        PropertyDefinition def = PropertyDefinition.get("ORB OI ORDERED - INPT");
        assertTrue(def.getName().equals("ORB OI ORDERED - INPT"));
    }
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.context;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import gov.ihs.cwf.context.PatientContext.IPatientContextEvent;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Person.Name;
import gov.ihs.cwf.domain.User;

import org.carewebframework.api.context.ContextMarshaller;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.api.test.CommonTest;

import org.junit.Ignore;
import org.junit.Test;

public class PatientContextTest extends CommonTest {
    
    /**
     * Should accept context change on first survey and refuse on all subsequent surveys.
     * 
     * @author dmartin
     */
    private class ContextChangeSubscriber implements IPatientContextEvent {
        
        private String reason = "";
        
        @Override
        public void canceled() {
        }
        
        @Override
        public void committed() {
        }
        
        @Override
        public String pending(boolean silent) {
            String result = reason;
            reason = "refuse change";
            return result;
        }
        
    }
    
    DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy");
    
    @Test
    public void changePatientContext() throws Exception {
        changeUserContext();
        Patient patient1 = new Patient(456);
        patient1.setName(new Name("Smith, Joe"));
        patient1.setIdentifier(new EntityIdentifier("999-99-9999", "SSN"));
        patient1.setBirthDate(dateFormat.parse("7/27/1958"));
        Patient patient2 = new Patient(123);
        patient2.setName(new Name("Doe, Jane"));
        patient2.setIdentifier(new EntityIdentifier("123-45-6789", "SSN"));
        patient2.setBirthDate(dateFormat.parse("5/1/1963"));
        Object subscriber = new ContextChangeSubscriber(); // Create a patient context change subscriber
        appFramework.registerObject(subscriber); // Register it with the context manager
        PatientContext.changePatient(patient1); // Request a context change
        assertSame(patient1, PatientContext.getCurrentPatient()); // This time should succeed
        PatientContext.changePatient(patient2); // Reattempt the context change
        assertNotSame(patient2, PatientContext.getCurrentPatient()); // Subscriber should have refused the context change
        appFramework.unregisterObject(subscriber); // Unregister the subscriber
        PatientContext.changePatient(patient2); // Reattempt the context change
        assertSame(patient2, PatientContext.getCurrentPatient()); // This time should succeed
    }
    
    public void changeUserContext() throws Exception {
        User user = new User(999);
        UserContext.changeUser(user);
    }
    
    @Test
    @Ignore
    public void marshalling() throws Exception {
        changePatientContext();
        ContextMarshaller marshaller = contextManager.getContextMarshaller("keystore-test");
        String ctx = marshaller.marshal(contextManager.getMarshaledContext());
        String sig = marshaller.sign(ctx);
        PatientContext.changePatient(null);
        assertNull(PatientContext.getCurrentPatient());
        marshaller.unmarshal(ctx, sig);
        Patient patient = PatientContext.getCurrentPatient();
        assertTrue("Doe, Jane".equalsIgnoreCase(patient.getFullName()));
    }
    
}

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

import java.util.List;

import gov.ihs.cwf.factory.DomainObjectFactory;

import org.carewebframework.api.domain.IDomainFactory;
import org.carewebframework.cal.api.domain.IPatient;

/**
 * Service for managing patient domain objects.
 */
public class PatientFactory implements IDomainFactory<IPatient> {
    
    private static final PatientFactory instance = new PatientFactory();
    
    public static PatientFactory getInstance() {
        return instance;
    }
    
    private PatientFactory() {
    };
    
    @Override
    public Patient newObject() {
        return new Patient();
    }
    
    @Override
    public Patient fetchObject(long id) {
        return id > 0 ? DomainObjectFactory.get(Patient.class, id) : null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IPatient> fetchObjects(long[] ids) {
        return (List<IPatient>) (List<?>) DomainObjectFactory.get(Patient.class, ids);
    }
    
}

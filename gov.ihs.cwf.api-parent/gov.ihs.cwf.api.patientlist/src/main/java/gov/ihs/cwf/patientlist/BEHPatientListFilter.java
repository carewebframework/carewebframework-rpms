/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.patientlist;

import org.carewebframework.cal.api.patientlist.AbstractPatientListFilter;

public class BEHPatientListFilter extends AbstractPatientListFilter {
    
    public BEHPatientListFilter(BEHPatientListFilterEntity entity) {
        super(entity);
    }
    
    public BEHPatientListFilter(String value) {
        super(value);
    }
    
    /**
     * Return the serialized form of the associated entity.
     */
    @Override
    protected String serialize() {
        return getEntity().toString();
    }
    
    /**
     * Deserialize an entity from its serialized form.
     */
    @Override
    protected BEHPatientListFilterEntity deserialize(String value) {
        return new BEHPatientListFilterEntity(value);
    }
    
    /**
     * Returns the display name of the associated entity (the name of the service location).
     */
    @Override
    protected String initName() {
        return getEntity() == null ? "" : ((BEHPatientListFilterEntity) getEntity()).getName();
    }
}

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

/**
 * Abstract base class for encounter-associated domain objects.
 * 
 * 
 */
public abstract class EncounterRelated extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    private Encounter encounter;
    
    public EncounterRelated() {
        super();
    }
    
    public EncounterRelated(Encounter encounter) {
        super();
        this.encounter = encounter;
    }
    
    public Encounter getEncounter() {
        return encounter;
    }
    
    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }
    
}

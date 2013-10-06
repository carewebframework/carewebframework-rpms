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

/**
 * Filter associated with the personal patient list. The entity associated with this filter is
 * simply the name of the personal list.
 */
public class PersonalPatientListFilter extends AbstractPatientListFilter {
    
    /**
     * Create a filter using the specified entity value (the personal list name).
     * 
     * @param value The name of the personal list.
     */
    public PersonalPatientListFilter(Object value) {
        super(value);
    }
    
    /**
     * Returns the serialized form of the filter entity (which is simply the list name).
     */
    @Override
    protected String serialize() {
        return getEntity().toString();
    }
    
    /**
     * Returns the deserialized form of the filter entity (which is the same as its serialized
     * form).
     */
    @Override
    protected String deserialize(String value) {
        return value;
    }
    
    /**
     * Returns the initial default name for this filter.
     */
    @Override
    protected String initName() {
        return getEntity() == null ? "" : getEntity().toString();
    }
    
    /**
     * Sets the name for this filter, which also sets the associated entity.
     */
    @Override
    protected void setName(String name) {
        super.setName(name);
        setEntity(name);
    }
    
}

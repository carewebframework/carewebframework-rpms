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
 * Filter for the favorite patient list.
 */
public class FavoritePatientListFilter extends AbstractPatientListFilter {
    
    public FavoritePatientListFilter(Favorite entity) {
        super(entity);
    }
    
    public FavoritePatientListFilter(String value) {
        super(value);
    }
    
    @Override
    protected String serialize() {
        return getEntity().toString();
    }
    
    @Override
    protected Favorite deserialize(String value) {
        return new Favorite(value);
    }
    
    @Override
    protected String initName() {
        return ((Favorite) getEntity()).getName();
    }
    
    @Override
    protected void setName(String name) {
        super.setName(name);
        ((Favorite) getEntity()).setName(name);
    }
    
}

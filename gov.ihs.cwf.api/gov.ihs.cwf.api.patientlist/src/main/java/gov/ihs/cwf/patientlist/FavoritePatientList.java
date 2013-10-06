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

import java.util.Collection;

/**
 * Maintains a single list of favorite patient lists. The filters associated with this list are
 * essentially wrappers for other patient lists. Each filter is associated with an instance of
 * Favorite that wraps a patient list (and its active filter and date range if appropriate). The
 * list retrieval logic is essentially delegated to that of the wrapped list.
 * 
 * @author dmartin
 */
public class FavoritePatientList extends AbstractPatientList {
    
    private Collection<PatientListItem> patients;
    
    public FavoritePatientList() {
        super("Favorites", "Favorite");
    }
    
    /**
     * Copy constructor.
     * 
     * @param list Favorites list to copy.
     */
    public FavoritePatientList(FavoritePatientList list) {
        super(list);
    }
    
    /**
     * Creates the filter manager for this list.
     */
    @Override
    public FavoritePatientListFilterManager createFilterManager() {
        return new FavoritePatientListFilterManager(this);
    }
    
    /**
     * Adds a list to the favorites.
     * 
     * @param list
     */
    public void addFavorite(IPatientList list) {
        if (list != null) {
            ((FavoritePatientListFilterManager) getFilterManager()).addEntity(new Favorite(list));
        }
    }
    
    /* ==================== IPatientList ==================== */
    
    /**
     * Resets the patient list when the filter changes.
     */
    @Override
    public void setActiveFilter(AbstractPatientListFilter filter) {
        patients = null;
        super.setActiveFilter(filter);
    }
    
    /**
     * Forces this list to be the first.
     */
    @Override
    public int getSequence() {
        return Integer.MIN_VALUE;
    }
    
    /**
     * Returns the list of patients for the active filter. This simply calls the getListItems method
     * on the patient list associated with the currently selected filter.
     */
    @Override
    public Collection<PatientListItem> getListItems() {
        if (patients == null && getActiveFilter() != null) {
            AbstractPatientListFilter filter = getActiveFilter();
            Favorite favorite = (Favorite) filter.getEntity();
            patients = favorite.getPatientList().getListItems();
        }
        
        return patients;
    }
    
    /**
     * Resets the patient list upon refresh.
     */
    @Override
    public void refresh() {
        super.refresh();
        patients = null;
    }
    
}

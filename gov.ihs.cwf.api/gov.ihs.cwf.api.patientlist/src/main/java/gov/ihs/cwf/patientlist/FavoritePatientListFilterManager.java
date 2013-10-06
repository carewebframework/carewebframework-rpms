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

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter manager for maintaining favorite patient lists. The entity for a favorite patient list is
 * an instance of Favorite which essentially wraps another patient list (and its active filter and
 * date range, if appropriate).
 */
public class FavoritePatientListFilterManager extends AbstractPatientListFilterManager {
    
    private static final Log log = LogFactory.getLog(FavoritePatientListFilterManager.class);
    
    private static final String FAVORITES_PROPERTY = "CAREWEB.PATIENT.LIST.FAVORITES";
    
    private Property filterProperty;
    
    /**
     * Create the filter manager for the specified favorite patient list.
     * 
     * @param patientList
     */
    public FavoritePatientListFilterManager(FavoritePatientList patientList) {
        super(patientList, Util.createImmutableSet(FilterCapability.RENAME, FilterCapability.MOVE, FilterCapability.REMOVE));
    }
    
    /**
     * Returns the property used to store favorites. If the property has not yet been accessed, it
     * is instantiated and read.
     * 
     * @return
     */
    private Property getFilterProperty() {
        if (filterProperty == null) {
            filterProperty = new Property(FAVORITES_PROPERTY);
        }
        
        return filterProperty;
    }
    
    @Override
    protected List<AbstractPatientListFilter> initFilters() {
        if (filters == null) {
            filters = new ArrayList<AbstractPatientListFilter>();
            
            for (String value : getFilterProperty().getValues()) {
                try {
                    filters.add(createFilter(new Favorite(value)));
                } catch (Exception e) {
                    log.error("Error creating favorite list item.", e);
                }
            }
        }
        return filters;
    }
    
    /**
     * Saves the current favorites list to a property.
     */
    @Override
    protected void saveFilters() {
        ArrayList<String> values = new ArrayList<String>();
        
        for (AbstractPatientListFilter filter : initFilters()) {
            values.add(filter.getEntity().toString());
        }
        
        try {
            getFilterProperty().setValues(values);
            getFilterProperty().saveValues();
        } catch (Exception e) {
            log.error("Error saving favorites.", e);
        }
    }
    
    /**
     * Creates a filter for the specified entity.
     * 
     * @param entity The entity (an instance of Favorite).
     * @return A filter appropriate for this list type.
     */
    @Override
    public AbstractPatientListFilter createFilter(Object entity) {
        return new FavoritePatientListFilter((Favorite) entity);
    }
    
    /**
     * Creates a filter from its serialized form.
     * 
     * @param serializedEntity The serialized form of the filter.
     * @return A new filter.
     */
    @Override
    protected AbstractPatientListFilter deserializeFilter(String serializedEntity) {
        return createFilter(new Favorite(serializedEntity));
    }
    
    /**
     * Forces the filter property to be re-read upon filter refresh.
     */
    @Override
    public void refreshFilters() {
        filterProperty = null;
        super.refreshFilters();
    }
    
}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.ContextItems;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.context.IContextEvent;
import org.carewebframework.api.context.ISharedContext;
import org.carewebframework.api.context.ManagedContext;
import org.carewebframework.common.StrUtil;

import org.carewebframework.vista.api.domain.DomainObjectFactory;
import org.carewebframework.vista.api.domain.Location;
import org.carewebframework.vista.api.property.Property;

/**
 * Wrapper for shared user location context.
 * 
 * 
 */
public class LocationContext extends ManagedContext<Location> {
    
    private static final Log log = LogFactory.getLog(LocationContext.class);
    
    private static final String SUBJECT_NAME = "Location";
    
    private static final String PROPERTY_DEFAULT_LOCATION = "CAREWEB.LOCATION.DEFAULT";
    
    public interface ILocationContextEvent extends IContextEvent {};
    
    /**
     * Returns the managed location context.
     * 
     * @return Location context.
     */
    @SuppressWarnings("unchecked")
    static public ISharedContext<Location> getLocationContext() {
        return (ISharedContext<Location>) ContextManager.getInstance().getSharedContext(
            LocationContext.class.getName());
    }
    
    /**
     * Returns the current location from the shared context.
     * 
     * @return Current location.
     */
    public static Location getCurrentLocation() {
        return getLocationContext().getContextObject(false);
    }
    
    /**
     * Requests a context change to the specified location.
     * 
     * @param location
     */
    public static void changeLocation(Location location) {
        try {
            getLocationContext().requestContextChange(location);
            setDefaultLocation(location);
        } catch (Exception e) {
            log.error("Error during request context change.", e);
        }
    }
    
    /**
     * Returns the default location if any. This value is stored in the LOCATION.DEFAULT property.
     * 
     * @return Default location or null if no default is available.
     */
    public static Location getDefaultLocation() {
        try {
            Property propLocation = new Property(PROPERTY_DEFAULT_LOCATION);
            long id  = StrUtil.toLong(propLocation.getValue());
            return id == 0 ? null : DomainObjectFactory.get(Location.class, id);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Sets the default location to the specified value. This value is stored in the
     * LOCATION.DEFAULT property.
     * 
     * @param location
     */
    public static void setDefaultLocation(Location location) {
        try {
            Property propLocation = new Property(PROPERTY_DEFAULT_LOCATION);
            propLocation.setValue(Long.toString(location.getDomainId()));
            propLocation.saveValues("USR");
        } catch (Exception e) {}
    }
    
    /**
     * Creates the context wrapper and registers its context change callback interface.
     */
    public LocationContext() {
        this(getDefaultLocation());
    }
    
    /**
     * /** Creates the context wrapper and registers its context change callback interface.
     * 
     * @param location Initial value for this context.
     */
    public LocationContext(Location location) {
        super(SUBJECT_NAME, ILocationContextEvent.class, location);
    }
    
    /**
     * Commits or rejects the pending context change.
     * 
     * @param accept If true, the pending change is committed. If false, the pending change is
     *            canceled.
     */
    @Override
    public void commit(boolean accept) {
        super.commit(accept);
    }
    
    /**
     * Creates a CCOW context from the specified location object.
     */
    @Override
    protected ContextItems toCCOWContext(Location location) {
        //TODO: contextItems.setItem(...);
        return contextItems;
    }
    
    /**
     * Returns a list of patient objects based on the specified CCOW context.
     */
    @Override
    protected Location fromCCOWContext(ContextItems contextItems) {
        Location location = null;
        
        try {
            location = new Location();
            //TODO: populate location object from context items
            return location;
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }
    
    /**
     * Returns a priority value of 5.
     * 
     * @return Priority value for context manager.
     */
    @Override
    public int getPriority() {
        return 5;
    }
    
}

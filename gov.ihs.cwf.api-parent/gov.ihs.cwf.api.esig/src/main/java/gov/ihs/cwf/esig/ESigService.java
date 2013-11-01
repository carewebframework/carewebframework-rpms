/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.esig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;

/**
 * This is the esig service and type registry implementation.
 * 
 * @author dmartin
 */
public class ESigService implements IESigService, IESigTypeRegistry, IPatientContextEvent {
    
    public static IESigService getInstance() {
        return (IESigService) SpringUtil.getAppContext().getBean("eSigService");
    }
    
    public static IESigTypeRegistry getTypeRegistry() {
        return (IESigTypeRegistry) getInstance();
    }
    
    private final ESigList eSigList = new ESigList();
    
    private final Map<String, IESigType> typeRegistry = new HashMap<String, IESigType>();
    
    public ESigService() {
        super();
    }
    
    /**
     * Sets the event manager to be used by the service. Injected by the IOC container.
     * 
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        eSigList.setEventManager(eventManager);
    }
    
    /**
     * Returns the event manager used by the service. The event manager is used to notify
     * subscribers of changes to the signature items.
     * 
     * @return The event manager.
     */
    public EventManager getEventManager() {
        return eSigList.getEventManager();
    }
    
    /**
     * Clear the esig list and re-initialize all esig types.
     */
    private void init() {
        clear();
        
        for (IESigType eSigType : getTypes()) {
            init(eSigType);
        }
    }
    
    /**
     * Clear the entire esig list.
     */
    private void clear() {
        eSigList.clear();
    }
    
    /**
     * Initialize a new esig type.
     * 
     * @param eSigType
     */
    private void init(IESigType eSigType) {
        List<ESigItem> list = new ArrayList<ESigItem>();
        eSigType.loadESigItems(list);
        eSigList.addAll(list);
    }
    
    /**********************************************************************
     * IESigService Implementation
     **********************************************************************/
    
    /**
     * Add a new esig item to the list.
     * 
     * @param item The esig item to add.
     */
    @Override
    public void add(ESigItem item) {
        eSigList.add(item);
    }
    
    /**
     * Tests if the esig item of the specified type and unique id exists.
     * 
     * @param esigType The esig type.
     * @param id The unique id.
     * @return True if a matching item exists in the list.
     */
    @Override
    public boolean exist(IESigType esigType, String id) {
        return eSigList.indexOf(esigType, id) >= 0;
    }
    
    /**
     * Returns true if there is at least one signable item in the list.
     */
    @Override
    public boolean getCanSign() {
        return eSigList.canSign(true);
    }
    
    /**
     * Returns the number of items in the list.
     */
    @Override
    public int getCount() {
        return eSigList.getCount();
    }
    
    /**
     * Returns the number of items of the specified type.
     */
    @Override
    public int getCount(IESigType esigType) {
        return eSigList.getCount(esigType);
    }
    
    /**
     * Removes the item of the specified type and id from the list.
     * 
     * @param esigType The esig type.
     * @param id The unique id.
     */
    @Override
    public void remove(IESigType esigType, String id) {
        eSigList.remove(esigType, id);
    }
    
    /**
     * Removes the specified esig item from the list.
     * 
     * @param item The item to remove.
     */
    @Override
    public void remove(ESigItem item) {
        eSigList.remove(item);
    }
    
    /**
     * Returns true if any item in the list requires user review.
     */
    @Override
    public boolean requiresReview() {
        return eSigList.requiresReview();
    }
    
    /**
     * Returns esig items that match the specified filter.
     * 
     * @param filter The filter to be applied to the current list. If null, the entire list is
     *            returned.
     */
    @Override
    public Iterable<ESigItem> getItems(ESigFilter filter) {
        return eSigList.findItems(filter);
    }
    
    /**********************************************************************
     * IPatientContextEvent Implementation
     **********************************************************************/
    
    @Override
    public void canceled() {
    }
    
    /**
     * Reinitialize the item list when the patient context changes.
     */
    @Override
    public void committed() {
        init();
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    /**********************************************************************
     * IESigTypeRegistry Implementation
     **********************************************************************/
    
    /**
     * Returns the esig type corresponding to the specified id.
     * 
     * @param id The mnemonic id that uniquely identifies this esig type.
     */
    @Override
    public IESigType getType(String id) {
        return typeRegistry.get(id);
    }
    
    /**
     * Returns a list of all registered esig types.
     */
    @Override
    public Iterable<IESigType> getTypes() {
        return typeRegistry.values();
    }
    
    /**
     * Registers a new esig type. If a type with the same mnemonic id is already registered, an
     * exception is thrown.
     * 
     * @param eSigType The new type to register.
     */
    @Override
    public void register(IESigType eSigType) throws Exception {
        if (typeRegistry.get(eSigType.getESigTypeId()) != null) {
            throw new Exception("Duplicate esig type identifier: " + eSigType.getESigTypeId());
        }
        
        typeRegistry.put(eSigType.getESigTypeId(), eSigType);
        init(eSigType);
    }
}

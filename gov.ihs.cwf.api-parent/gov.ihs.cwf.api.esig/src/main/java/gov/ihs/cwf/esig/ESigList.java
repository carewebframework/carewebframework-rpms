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
import java.util.List;

import gov.ihs.cwf.esig.ESigItem.SignState;

import org.carewebframework.api.event.EventManager;

/**
 * Maintains a list of items queued for electronic signature and provides several methods for
 * manipulating items in the list.
 * 
 * 
 */
public class ESigList {
    
    private final List<ESigItem> items = new ArrayList<ESigItem>();
    
    private EventManager eventManager;
    
    /**
     * Returns true if items exist that the user may sign.
     * 
     * @param selectedOnly If true, examine only selected items.
     * @return True if items exist that the user may sign.
     */
    public boolean canSign(boolean selectedOnly) {
        for (ESigItem item : items) {
            if ((!selectedOnly || item.isSelected()) && (item.getSignState() != SignState.FORCED_NO)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the count of items.
     * 
     * @return Count of items.
     */
    public int getCount() {
        return items.size();
    }
    
    /**
     * Returns the count of items of the specified type.
     * 
     * @param eSigType
     * @return Count of items of specified type.
     */
    public int getCount(IESigType eSigType) {
        int result = 0;
        
        for (ESigItem item : items) {
            if (item.getESigType().equals(eSigType)) {
                result++;
            }
        }
        
        return result;
    }
    
    /**
     * Returns the index of a sig item identified by type and id.
     * 
     * @param eSigType
     * @param id
     * @return Index of item.
     */
    public int indexOf(IESigType eSigType, String id) {
        return items.indexOf(new ESigItem(eSigType, id));
    }
    
    /**
     * Returns a sig item identified by type and id, or null if not found.
     * 
     * @param eSigType
     * @param id
     * @return Sig item.
     */
    public ESigItem get(IESigType eSigType, String id) {
        int i = indexOf(eSigType, id);
        return i == -1 ? null : items.get(i);
    }
    
    /**
     * Returns a list of sig items that match the specified filter. If the filter is null, all sig
     * items are returned.
     * 
     * @param filter
     * @return List of matching sig items.
     */
    public List<ESigItem> findItems(ESigFilter filter) {
        List<ESigItem> list = new ArrayList<ESigItem>();
        
        if (filter == null) {
            list.addAll(items);
        } else {
            for (ESigItem item : items) {
                if (filter.matches(item)) {
                    list.add(item);
                }
            }
        }
        
        return list;
    }
    
    /**
     * Returns true if any sig items exist that require user review.
     * 
     * @return True if any sig items exist that require user review.
     */
    public boolean requiresReview() {
        for (ESigItem item : items) {
            if (item.getESigType().requiresReview()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds a list of sig items to the master list.
     * 
     * @param items
     */
    public void addAll(Iterable<ESigItem> items) {
        for (ESigItem item : items) {
            add(item);
        }
    }
    
    /**
     * Ads a new sig item to the list, given the required elements.
     * 
     * @param eSigType
     * @param id
     * @param text
     * @param subGroupName
     * @param signState
     * @param data
     * @param session
     * @return New sig item.
     */
    public ESigItem add(IESigType eSigType, String id, String text, String subGroupName, SignState signState, String data,
                        String session) {
        ESigItem item = new ESigItem(eSigType, id);
        item.setText(text);
        item.setSubGroupName(subGroupName);
        item.setSignState(signState);
        item.setData(data);
        item.setSession(session);
        return add(item);
    }
    
    /**
     * Adds a sig item to the list. Requests to add items already in the list are ignored. If an
     * item is successfully added, an ESIG.ADD event is fired.
     * 
     * @param item A sig item.
     * @return The item that was added.
     */
    public ESigItem add(ESigItem item) {
        if (!items.contains(item)) {
            items.add(item);
            fireEvent("ADD", item);
        }
        
        return item;
    }
    
    /**
     * Removes all items from the list. An ESIG.CLEAR event is fired.
     */
    public void clear() {
        items.clear();
        fireEvent("CLEAR", null);
    }
    
    /**
     * Notifies subscribers of changes to the list by firing an ESIG.[action] event.
     * 
     * @param action Name of the action. This becomes a subtype of the ESIG event.
     * @param item The item for which the action occurred.
     */
    private void fireEvent(String action, ESigItem item) {
        if (eventManager != null) {
            eventManager.fireLocalEvent("ESIG." + action, item);
        }
    }
    
    /**
     * Clears the selection state of all items.
     */
    public void clearSelections() {
        for (ESigItem item : items) {
            item.setSelected(false);
        }
    }
    
    /**
     * Removes a sig item from the list. Fires an ESIG.DELETE event if successful.
     * 
     * @param item Item to remove.
     * @return True if the operation was successful.
     */
    public boolean remove(ESigItem item) {
        boolean result = items.remove(item);
        
        if (result) {
            removed(item);
        }
        
        return result;
    }
    
    /**
     * Removes the sig item at the specified index. Fires an ESIG.DELETE event.
     * 
     * @param index
     */
    public void remove(int index) {
        remove(items.get(index));
    }
    
    /**
     * Fires an ESIG.DELETE event for the specified item.
     * 
     * @param item
     */
    private void removed(ESigItem item) {
        fireEvent("DELETE", item);
    }
    
    /**
     * Removes the sig item identified by its type and id. Fires an ESIG.DELETE event if successful.
     * 
     * @param eSigType Type of item.
     * @param id Id of item.
     * @return True if the operation was successful.
     */
    public boolean remove(IESigType eSigType, String id) {
        return remove(get(eSigType, id));
    }
    
    public void replaceSubGroup(String oldName, String newName) {
        for (ESigItem item : items) {
            if (item.getSubGroupName().equalsIgnoreCase(oldName)) {
                item.setSubGroupName(newName);
            }
        }
    }
    
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
}

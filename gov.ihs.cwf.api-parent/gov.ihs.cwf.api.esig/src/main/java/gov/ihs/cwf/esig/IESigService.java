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

/**
 * This is the interface declaration for the electronic signature service.
 * 
 * 
 */
public interface IESigService {
    
    void add(ESigItem item); // Adds the item to the list of items pending signature.
    
    boolean getCanSign(); // Returns true if the list contains any signable items.
    
    boolean exist(IESigType esigType, String id); // Returns true if the item identified by type and id exists.
    
    void remove(IESigType esigType, String id); // Removes the item identified by type and id.
    
    void remove(ESigItem item); // Removes the specified item.
    
    boolean requiresReview(); // Returns true if the list has any items the require review.
    
    int getCount(); // Returns the count of all items in the list.
    
    int getCount(IESigType esigType); // Returns the count of items of the specified type.
    
    Iterable<ESigItem> getItems(ESigFilter filter); // Returns an iterable of items matching the specified filter (or all items if filter is null).
}

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
 * This is the interface declaration for the esig type registry function.
 * 
 * 
 */
public interface IESigTypeRegistry {
    
    public IESigType getType(String id);
    
    public void register(IESigType esigType) throws Exception;
    
    public Iterable<IESigType> getTypes();
}

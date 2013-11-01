/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.factory;

import java.util.Collections;
import java.util.List;

import gov.ihs.cwf.util.RPMSUtil;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.JSONUtil;

/**
 * Factory for instantiated serialized domain objects from server.
 * 
 * @author dmartin
 */
public class DomainObjectFactory {
    
    /**
     * Requests a domain object from the server. The domain object, if found, is returned by the
     * server in serialized form using standard JSON format and is then deserialized and
     * instantiated. The class of the requested domain object must have an alias registered with the
     * JSON deserializer. This is typically done in a static initializer within the class itself.
     * 
     * @param clazz The class of the domain object to be returned.
     * @param id The internal record number of the requested domain object.
     * @return An instance of the requested domain object.
     */
    @SuppressWarnings("unchecked")
    public static <DomainClass extends DomainObject> DomainClass get(Class<DomainClass> clazz, long id) {
        String json = RPMSUtil.getBrokerSession().callRPC("CIAVMSER GETBYIEN", getAlias(clazz), id);
        return (DomainClass) JSONUtil.deserialize(json);
    }
    
    /**
     * Requests a list of domain objects of a common type from the server. The domain objects are
     * returned by the server in serialized form using standard JSON format and are then
     * deserialized and instantiated. The class of the requested domain object must have an alias
     * registered with the JSON deserializer. This is typically done in a static initializer within
     * the class itself.
     * 
     * @param clazz The class of the domain object to be returned.
     * @param ids An array of internal record numbers of the requested domain objects.
     * @return A list of instances of the requested domain objects.
     */
    @SuppressWarnings("unchecked")
    public static <DomainClass extends DomainObject> List<DomainClass> get(Class<DomainClass> clazz, long[] ids) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }
        
        String json = RPMSUtil.getBrokerSession().callRPC("CIAVMSER GETBYIEN", getAlias(clazz), ids);
        return (List<DomainClass>) JSONUtil.deserialize(json);
    }
    
    /**
     * Requests a domain object from the server. The domain object, if found, is returned by the
     * server in serialized form using standard JSON format and is then deserialized and
     * instantiated. The class of the requested domain object must have an alias registered with the
     * JSON deserializer. This is typically done in a static initializer within the class itself.
     * 
     * @param clazz The class of the domain object to be returned.
     * @param key A unique lookup identifier for the requested object.
     * @param table The table (number or name) in which to perform the lookup.
     * @return An instance of the requested domain object.
     */
    @SuppressWarnings("unchecked")
    public static <DomainClass extends DomainObject> DomainClass get(Class<DomainClass> clazz, String key, String table) {
        String json = RPMSUtil.getBrokerSession().callRPC("CIAVMSER GETBYKEY", getAlias(clazz), key, table);
        return (DomainClass) JSONUtil.deserialize(json);
    }
    
    /**
     * Returns the alias for the domain class. A domain class will typically register its alias in a
     * static initializer block. If the initial attempt to retrieve an alias fails, this method
     * forces the class loader to load the class to ensure that any static initializers are
     * executed, and then tries again.
     * 
     * @param clazz Domain class whose alias is sought.
     * @return The alias for the domain class.
     */
    private static <DomainClass extends DomainObject> String getAlias(Class<DomainClass> clazz) {
        // Locate the alias of the requested class.
        String alias = JSONUtil.getAlias(clazz);
        
        // If not found, ensure that the class has been loaded to allow for its
        // static initializers to execute, then try again.
        if (alias == null) {
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            alias = JSONUtil.getAlias(clazz);
        }
        
        // If still no alias found, raise an exception.
        if (alias == null) {
            throw new RuntimeException("Class is not registered for serialization.");
        }
        
        return alias;
    }
    
}

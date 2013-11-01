/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carewebframework.common.JSONUtil;

/**
 * Abstract base class for encounter-associated domain objects.
 * 
 * @author dmartin
 */
public class EncounterProvider extends EncounterRelated {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("EncounterProvider", EncounterProvider.class);
    }
    
    private Provider currentProvider;
    
    private Provider primaryProvider;
    
    private final List<Provider> providers = new ArrayList<Provider>();
    
    public EncounterProvider(Encounter encounter) {
        super(encounter);
    }
    
    public void assign(EncounterProvider encounterProvider) {
        if (this == encounterProvider) {
            throw new IllegalArgumentException("Cannot assign to self.");
        }
        
        clear();
        providers.addAll(encounterProvider.providers);
        currentProvider = encounterProvider.currentProvider;
        primaryProvider = encounterProvider.primaryProvider;
    }
    
    public Provider getCurrentProvider() {
        return currentProvider;
    }
    
    public void setCurrentProvider(Provider provider) {
        add(provider);
        this.currentProvider = provider;
    }
    
    public Provider getPrimaryProvider() {
        return primaryProvider;
    }
    
    public void setPrimaryProvider(Provider provider) {
        add(provider);
        this.primaryProvider = provider;
    }
    
    public int size() {
        return providers.size();
    }
    
    public void clear() {
        providers.clear();
        currentProvider = null;
        primaryProvider = null;
    }
    
    public boolean add(Provider provider) {
        boolean result = provider != null && !providers.contains(provider);
        
        if (result) {
            providers.add(provider);
        }
        
        return result;
    }
    
    public boolean remove(Provider provider) {
        boolean result = provider != null && providers.remove(provider);
        
        if (result) {
            if (provider.equals(primaryProvider)) {
                primaryProvider = null;
            }
            
            if (provider.equals(currentProvider)) {
                currentProvider = null;
            }
        }
        
        return result;
    }
    
    public List<Provider> providers() {
        return Collections.unmodifiableList(providers);
    }
}

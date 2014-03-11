/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.context.encounter;

import gov.ihs.cwf.context.EncounterUtil;
import gov.ihs.cwf.context.ProviderUtil;
import gov.ihs.cwf.domain.Encounter;
import gov.ihs.cwf.domain.EncounterProvider;
import gov.ihs.cwf.domain.Patient;
import gov.ihs.cwf.domain.Provider;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ProviderSelection extends Borderlayout implements IdSpace {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox edtProvider;
    
    private Listbox lstAllProviders;
    
    private Listbox lstEncounterProviders;
    
    private boolean modified = false;
    
    private EncounterProvider encounterProvider;
    
    private final ProviderRenderer providerRenderer = new ProviderRenderer();
    
    private final ListModelList<Provider> modelProviders = new ListModelList<Provider>();
    
    public void onCreate() {
        ZKUtil.wireController(this, this);
        lstAllProviders.setItemRenderer(providerRenderer);
        lstAllProviders.setModel(modelProviders);
        lstEncounterProviders.setItemRenderer(providerRenderer);
    }
    
    public EncounterProvider getEncounterProvider() {
        return encounterProvider;
    }
    
    public void updateCurrentProvider() {
        encounterProvider.setCurrentProvider(getSelectedProvider(lstEncounterProviders));
    }
    
    private Provider getSelectedProvider(Listbox lb) {
        Listitem item = lb.getSelectedItem();
        return item == null ? null : (Provider) item.getValue();
    }
    
    private void setPrimaryProvider(Provider provider) {
        encounterProvider.setPrimaryProvider(provider);
        refreshProviders();
        modified = true;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    public void loadProviders(Patient patient, Encounter encounter) {
        encounterProvider = encounter == null ? new EncounterProvider(null) : EncounterUtil.getEncounterProvider(patient,
            encounter);
        providerRenderer.setEncounterProvider(encounterProvider);
        modified = false;
        refreshProviders();
    }
    
    public void refreshProviders() {
        Provider provider = getSelectedProvider(lstEncounterProviders);
        ListModel<Provider> model = new ListModelList<Provider>(encounterProvider.providers());
        lstEncounterProviders.setModel((ListModel<?>) null);
        lstEncounterProviders.setModel(model);
        
        if (model.getSize() == 1) {
            lstEncounterProviders.setSelectedIndex(0);
        } else {
            selectFirstProvider(provider, UserContext.getActiveUser(), encounterProvider.getCurrentProvider(),
                encounterProvider.getPrimaryProvider());
        }
        
    }
    
    private void selectFirstProvider(Object... providers) {
        for (Object provider : providers) {
            if (findProvider(provider)) {
                break;
            }
        }
    }
    
    private boolean findProvider(Object provider) {
        if (provider != null) {
            int i = ListUtil.findListboxData(lstEncounterProviders, provider);
            
            if (i >= 0) {
                lstEncounterProviders.setSelectedIndex(i);
                return true;
            }
        }
        
        return false;
    }
    
    public void onClick$btnProvider() {
        ProviderUtil.search(edtProvider.getText(), 40, modelProviders);
    }
    
    public void onClick$btnPrimary() {
        Provider provider = getSelectedProvider(lstEncounterProviders);
        
        if (provider != null) {
            setPrimaryProvider(provider);
        }
    }
    
    public void onClick$btnProviderAdd() {
        Provider provider = getSelectedProvider(lstAllProviders);
        
        if (encounterProvider.add(provider)) {
            refreshProviders();
            modified = true;
        }
    }
    
    public void onClick$btnProviderRemove() {
        Provider provider = getSelectedProvider(lstEncounterProviders);
        
        if (encounterProvider.remove(provider)) {
            refreshProviders();
            modified = true;
        }
    }
    
}

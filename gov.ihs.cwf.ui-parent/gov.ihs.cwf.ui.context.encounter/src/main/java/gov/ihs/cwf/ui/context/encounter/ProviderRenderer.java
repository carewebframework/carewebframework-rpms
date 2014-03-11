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

import gov.ihs.cwf.context.ProviderUtil;
import gov.ihs.cwf.domain.EncounterProvider;
import gov.ihs.cwf.domain.Provider;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listitem;

public class ProviderRenderer extends AbstractListitemRenderer<Object, Object> {
    
    private EncounterProvider encounterProvider;
    
    @Override
    public void renderItem(Listitem item, Object data) {
        Provider provider = data instanceof Provider ? (Provider) data : ProviderUtil.fetchProvider(StrUtil.piece(
            (String) data, StrUtil.U));
        item.setValue(provider);
        createCell(item, provider.getFullName());
        Provider primaryProvider = encounterProvider.getPrimaryProvider();
        item.setSclass(primaryProvider == null || !provider.equals(primaryProvider) ? null : Constants.SCLASS_PRIMARY);
        item.addForward(Events.ON_DOUBLE_CLICK, item.getListbox(), null);
    }
    
    public void setEncounterProvider(EncounterProvider encounterProvider) {
        this.encounterProvider = encounterProvider;
    }
    
}

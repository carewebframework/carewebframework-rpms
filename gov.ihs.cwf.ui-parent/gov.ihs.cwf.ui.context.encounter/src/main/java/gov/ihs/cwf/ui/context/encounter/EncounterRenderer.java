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

import org.carewebframework.vista.api.domain.Encounter;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listitem;

public class EncounterRenderer extends AbstractListitemRenderer<Object, Object> {
    
    @Override
    public void renderItem(Listitem item, Object data) {
        Encounter encounter = data instanceof Encounter ? (Encounter) data : parse((String) data);
        item.setValue(encounter);
        item.addForward(Events.ON_DOUBLE_CLICK, item.getListbox(), null);
        item.setImage(encounter.isLocked() ? Constants.ICON_LOCKED : null);
        createCell(item, encounter.getLocation());
        createCell(item, encounter.getDateTime());
        createCell(item, encounter.getServiceCategory());
    }
    
    private Encounter parse(String value) {
        return Encounter.decode(StrUtil.piece(value, StrUtil.U));
    }
}

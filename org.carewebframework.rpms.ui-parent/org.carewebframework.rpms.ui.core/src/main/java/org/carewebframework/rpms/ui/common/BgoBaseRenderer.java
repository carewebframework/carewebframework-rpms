/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.common;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

/**
 * Abstract base class for renderers.
 * 
 * 
 * @param <T>
 */
public abstract class BgoBaseRenderer<T> implements org.zkoss.zul.ListitemRenderer<T> {
    
    @Override
    public final void render(Listitem item, T data, int index) throws Exception {
        item.setValue(data);
        renderItem(item, data);
    }
    
    protected abstract void renderItem(Listitem item, T data) throws Exception;
    
    /**
     * Add a cell to the list item.
     * 
     * @param item
     * @param object
     * @return
     */
    protected Listcell addCell(Listitem item, Object object) {
        Listcell cell = new Listcell(object == null ? "" : object.toString());
        cell.setParent(item);
        return cell;
    }
    
}

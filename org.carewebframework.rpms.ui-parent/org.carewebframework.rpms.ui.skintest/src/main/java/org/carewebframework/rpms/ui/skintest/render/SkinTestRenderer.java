/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.skintest.render;

import org.carewebframework.rpms.ui.skintest.model.TestItem;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zul.Listitem;

public class SkinTestRenderer extends AbstractListitemRenderer<TestItem, Object> {
    
    @Override
    protected void renderItem(Listitem item, TestItem data) {
        createCell(item, data.getDate());
        createCell(item, data.getTestName());
        createCell(item, data.getLocationName());
        createCell(item, data.getAge());
        createCell(item, data.getResult());
        createCell(item, data.getReading());
        createCell(item, data.getReadDate());
        createCell(item, data.getProviderName());
        createCell(item, data.getReader());
    }
    
}

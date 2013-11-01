/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.problemlist.render;

import gov.ihs.cwf.domain.Problem;
import gov.ihs.cwf.ui.problemlist.util.ProblemUtil;

import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zul.Listitem;

public class ProblemRenderer extends AbstractListitemRenderer<Problem, Object> {
    
    @Override
    public void renderItem(Listitem item, Problem problem) {
        createCell(item, problem.getNumberCode());
        createCell(item, problem.getProviderNarrative());
        createCell(item, ProblemUtil.getStatus(problem));
        createCell(item, problem.getModifyDate());
        createCell(item, problem.getPriority());
        createCell(item, problem.getNotes());
        createCell(item, problem.getOnsetDate());
    }
    
}

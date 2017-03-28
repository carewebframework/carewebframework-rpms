/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.plugin.problemlist.render;

import org.carewebframework.rpms.api.domain.Problem;
import org.carewebframework.rpms.plugin.problemlist.util.ProblemUtil;
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

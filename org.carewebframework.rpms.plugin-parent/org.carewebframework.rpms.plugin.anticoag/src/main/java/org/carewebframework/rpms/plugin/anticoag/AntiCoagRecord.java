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
package org.carewebframework.rpms.plugin.anticoag;

import static org.carewebframework.common.StrUtil.U;
import static org.carewebframework.common.StrUtil.split;

import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.BooleanUtils;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.DateUtil.TimeUnit;
import org.carewebframework.vista.api.util.VistAUtil;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hspconsortium.cwf.api.DomainObject;
import org.hspconsortium.cwf.fhir.common.FhirUtil;

/**
 * A single anticoagulation record.
 */
public class AntiCoagRecord extends DomainObject {
    
    private String goalRange;
    
    private Double goalMin;
    
    private Double goalMax;
    
    private String duration;
    
    private Date startDate;
    
    private Date endDate;
    
    private Date visitDate;
    
    private Date enteredDate;
    
    private String visitCategory;
    
    private String visitIEN;
    
    private Boolean visitLocked;
    
    private Practitioner provider;
    
    private String comment;
    
    private Boolean indicated;
    
    public AntiCoagRecord() {
        
    }
    
    /**
     * @param data <code>
     *    V IEN [1] ^ Indication [2] ^ Visit Date [3] ^ Goal [4] ^ Min [5] ^ Max [6] ^
     *    Duration [7] ^ Start Date [8] ^ Facility Name [9] ^ Practitioner IEN [10] ^ Location IEN [11] ^
     *    Entered Date [12]^ Visit IEN [13] ^ Visit Category [14] ^ Visit Locked [15] ^ Comment[16] ^
     *    Practitioner Name [17]
     * </code>
     */
    public AntiCoagRecord(String data) {
        String[] pcs = split(data, U, 17);
        setId(pcs[0]);
        setIndicated(BooleanUtils.toBoolean(pcs[1]));
        setVisitDate(VistAUtil.parseDate(pcs[2]));
        setGoalRange(pcs[3]);
        setGoalMin(parseDouble(pcs[4]));
        setGoalMax(parseDouble(pcs[5]));
        setDuration(pcs[6]);
        setStartDate(VistAUtil.parseDate(pcs[7]));
        setProvider(parseProvider(pcs[9], pcs[16]));
        setEnteredDate(VistAUtil.parseDate(pcs[11]));
        setVisitIEN(pcs[12]);
        setVisitCategory(pcs[13]);
        setVisitLocked("1".equals(pcs[14]));
        setComment(pcs[15]);
    }
    
    public AntiCoagRecord(AntiCoagRecord source) {
        try {
            BeanUtils.copyProperties(this, source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Double parseDouble(String value) {
        try {
            return value == null ? null : Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Practitioner parseProvider(String ien, String name) {
        if (ien.isEmpty()) {
            return null;
        }
        
        Practitioner provider = new Practitioner();
        provider.setId(ien);
        provider.addName(FhirUtil.parseName(name));
        return provider;
    }
    
    public String getGoalRange() {
        return goalRange;
    }
    
    public void setGoalRange(String goalRange) {
        this.goalRange = goalRange;
        
        if (goalRange != null && goalRange.contains("-")) {
            String[] pcs = goalRange.split("\\-");
            goalMin = Double.parseDouble(pcs[0].trim());
            goalMax = Double.parseDouble(pcs[1].trim());
        }
    }
    
    public Double getGoalMin() {
        return goalMin;
    }
    
    public void setGoalMin(Double goalMin) {
        this.goalMin = goalMin;
    }
    
    public Double getGoalMax() {
        return goalMax;
    }
    
    public void setGoalMax(Double goalMax) {
        this.goalMax = goalMax;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
        updateEndDate();
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        updateEndDate();
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void updateEndDate() {
        endDate = null;
        
        if (startDate != null && duration != null) {
            int elapsed = (int) DateUtil.parseElapsed(duration, TimeUnit.DAYS);
            endDate = elapsed == 0 ? null : DateUtil.addDays(startDate, elapsed, false);
        }
    }
    
    public Practitioner getProvider() {
        return provider;
    }
    
    public void setProvider(Practitioner provider) {
        this.provider = provider;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment == null ? null : comment.replace("^", " ");
    }
    
    public Boolean getIndicated() {
        return indicated;
    }
    
    public void setIndicated(Boolean indicated) {
        this.indicated = indicated;
    }
    
    public Date getVisitDate() {
        return visitDate;
    }
    
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }
    
    public Date getEnteredDate() {
        return enteredDate;
    }
    
    public void setEnteredDate(Date enteredDate) {
        this.enteredDate = enteredDate;
    }
    
    public String getVisitCategory() {
        return visitCategory;
    }
    
    public void setVisitCategory(String visitCategory) {
        this.visitCategory = visitCategory;
    }
    
    public String getVisitIEN() {
        return visitIEN;
    }
    
    public void setVisitIEN(String visitIEN) {
        this.visitIEN = visitIEN;
    }
    
    public Boolean getVisitLocked() {
        return visitLocked;
    }
    
    public void setVisitLocked(Boolean visitLocked) {
        this.visitLocked = visitLocked;
    }
    
}

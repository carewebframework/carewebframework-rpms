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

import gov.ihs.cwf.mbroker.FMDate;

import java.util.Date;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.common.JSONUtil;
import org.carewebframework.common.StrUtil;

public class Encounter extends DomainObject {
    
    private static final long serialVersionUID = 1L;
    
    static {
        JSONUtil.registerAlias("Encounter", Encounter.class);
    }
    
    private FMDate dateTime;
    
    private Location location;
    
    private boolean locked;
    
    private String bed;
    
    private String visitId;
    
    private ServiceCategory serviceCategory;
    
    private final EncounterProvider encounterProvider = new EncounterProvider(this);
    
    public static Encounter decode(String value) {
        String pcs[] = StrUtil.split(value, ";", 4);
        long id = StrUtil.toLong(pcs[3]);
        long locid = StrUtil.toLong(pcs[0]);
        Encounter encounter = null;
        
        if (id > 0) {
            try {
                encounter = DomainObjectFactory.get(Encounter.class, id);
            } catch (Exception e) {
                encounter = null;
            }
        }
        
        if (encounter == null) {
            encounter = new Encounter();
        }
        
        if (locid > 0 && (encounter.getLocation() == null || encounter.getLocation().getDomainId() != locid)) {
            try {
                encounter.setLocation(DomainObjectFactory.get(Location.class, locid));
            } catch (Exception e) {}
        }
        
        if (!pcs[1].isEmpty()) {
            encounter.setDateTime(new FMDate(pcs[1]));
        }
        
        encounter.setServiceCategoryStr(pcs[2]);
        
        return encounter;
    }
    
    public Encounter() {
        super();
    }
    
    public Encounter(long id) {
        super(id);
    }
    
    public Encounter(Date dateTime, Location location, String serviceCategory) {
        super();
        this.dateTime = dateTime instanceof FMDate ? (FMDate) dateTime : new FMDate(dateTime);
        this.location = location;
        setServiceCategoryStr(serviceCategory);
    }
    
    public FMDate getDateTime() {
        return dateTime;
    }
    
    protected void setDateTime(FMDate dateTime) {
        this.dateTime = dateTime;
    }
    
    public Location getLocation() {
        return location;
    }
    
    protected void setLocation(Location location) {
        this.location = location;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public boolean isPrepared() {
        return dateTime != null && serviceCategory != null && location != null
                && encounterProvider.getCurrentProvider() != null;
    }
    
    protected void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public String getBed() {
        return bed;
    }
    
    protected void setBed(String bed) {
        this.bed = bed;
    }
    
    public String getVisitId() {
        return visitId;
    }
    
    protected void setVisitId(String visitId) {
        this.visitId = visitId;
    }
    
    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }
    
    protected void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }
    
    private void setServiceCategoryStr(String serviceCategory) {
        if (serviceCategory == null || serviceCategory.isEmpty()) {
            this.serviceCategory = null;
        } else {
            this.serviceCategory = DomainObjectFactory.get(ServiceCategory.class, serviceCategory.charAt(0));
        }
    }
    
    public EncounterProvider getEncounterProvider() {
        return encounterProvider;
    }
    
    public void setEncounterProvider(EncounterProvider encounterProvider) {
        if (encounterProvider != this.encounterProvider) {
            this.encounterProvider.assign(encounterProvider);
        }
    }
    
    public String getEncoded() {
        return location.getDomainId() + ";" + dateTime.getFMDate() + ";" + serviceCategory
                + (getDomainId() > 0 ? ";" + getDomainId() : "");
    }
}

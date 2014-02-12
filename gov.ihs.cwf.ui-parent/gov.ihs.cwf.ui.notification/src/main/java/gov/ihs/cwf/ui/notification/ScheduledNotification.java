/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.notification;

import static org.carewebframework.common.StrUtil.U;

import java.util.Arrays;

import gov.ihs.cwf.mbroker.FMDate;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.common.StrUtil;

/**
 * A scheduled notification.
 */
public class ScheduledNotification extends AbstractNotification {
    
    private static final long serialVersionUID = 1L;
    
    private long ien;
    
    public ScheduledNotification() {
        
    }
    
    /**
     * Create a scheduled notification from raw data.
     * 
     * @param data Format is <code>
     *  1      2         3            4         5
     * IEN ^ Date ^ Patient Name ^ Subject ^ Extra Info
     * </code>
     */
    public ScheduledNotification(String data) {
        String[] pcs = StrUtil.split(data, U, 5);
        setIen(Long.parseLong(pcs[0]));
        setDeliveryDate(FMDate.fromString(pcs[1]));
        setPatientName(pcs[2]);
        setSubject(pcs[3]);
        setExtraInfo(Arrays.copyOfRange(pcs, 4, pcs.length));
    }
    
    /**
     * Returns the internal entry number of the notification.
     * 
     * @return The internal entry number. Will be 0 if this is a new notification.
     */
    public long getIen() {
        return ien;
    }
    
    /**
     * Sets the internal entry number of the notification.
     * 
     * @param ien The internal entry number. Will be 0 if this is a new notification.
     */
    protected void setIen(long ien) {
        this.ien = ien;
    }
    
    /**
     * Returns the priority of this notification.
     */
    @Override
    public Priority getPriority() {
        return Priority.fromString(getParam("PRI"));
    }
    
    /**
     * Sets the priority of this notification.
     */
    @Override
    protected void setPriority(Priority priority) {
        setParam("PRI", priority.ordinal() + 1);
    }
    
    /**
     * Returns the internal entry number of the associated patient, or null if no associated
     * patient.
     */
    @Override
    public Long getDfn() {
        String value = getParam("DFN");
        return value == null ? null : NumberUtils.toLong(value);
    }
    
    /**
     * Sets the internal entry number of the associated patient. Use null if no associated patient.
     */
    @Override
    protected void setDfn(Long dfn) {
        setParam("DFN", dfn);
    }
    
    /**
     * Scheduled notifications are not currently actionable.
     */
    @Override
    public boolean isActionable() {
        return false;
    }
    
    /**
     * All scheduled notifications may be deleted.
     */
    @Override
    public boolean canDelete() {
        return true;
    }
    
}

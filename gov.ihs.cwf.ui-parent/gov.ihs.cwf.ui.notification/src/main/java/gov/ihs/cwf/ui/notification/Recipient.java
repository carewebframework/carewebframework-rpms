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

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;

/**
 * Notification recipient.
 */
public class Recipient {
    
    private final String name;
    
    private final long ien;
    
    /**
     * Creates a recipient based on the specified user.
     * 
     * @param user A user.
     */
    protected Recipient(IUser user) {
        this.name = user.getFullName();
        this.ien = user.getDomainId();
    }
    
    /**
     * Creates a recipient based on raw data.
     * 
     * @param data Raw data.
     * @param isGroup If true, the raw data represents a group. If false, the type of recipient is
     *            to be inferred from the raw data.
     */
    protected Recipient(String data, boolean isGroup) {
        String[] pcs = StrUtil.split(data, StrUtil.U, 2);
        long val = NumberUtils.toLong(pcs[0]);
        this.ien = isGroup && val > 0 ? -val : val;
        this.name = pcs[1];
    }
    
    /**
     * Creates a recipient based on raw data.
     * 
     * @param data Raw data.
     */
    protected Recipient(String data) {
        this(data, false);
    }
    
    /**
     * Returns true if this recipient is a mail group.
     * 
     * @return True if a mail group.
     */
    public boolean isGroup() {
        return ien < 0;
    }
    
    /**
     * Returns the recipient name.
     * 
     * @return The recipient name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the recipient's internal entry number. Note that a negative value means that this is
     * a mail group, while a positive value means it is a user.
     * 
     * @return
     */
    public long getIen() {
        return ien;
    }
    
};

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.util;

import gov.ihs.cwf.domain.Institution;
import gov.ihs.cwf.domain.User;
import gov.ihs.cwf.mbroker.BrokerSession;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.spring.SpringUtil;

/**
 * Static utility class for the RPMS extensions.
 * 
 * @author dmartin
 */
public class RPMSUtil {
    
    public static BrokerSession getBrokerSession() {
        return SpringUtil.getBean("brokerSession", BrokerSession.class);
    }
    
    public static Institution getCurrentInstitution() {
        IUser user = UserContext.getActiveUser();
        return user == null ? null : ((User) user.getProxiedObject()).getInstitution();
    }
    
    /**
     * Enforces static class.
     */
    private RPMSUtil() {
    };
}

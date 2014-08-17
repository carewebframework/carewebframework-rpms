/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.common;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.util.VistAUtil;

public class BgoUtil {
    
    private static final String SECURITY_MESSAGE = "BGO.SECURITY.MESSAGE";
    
    /**
     * Returns message explaining write restrictions for user.
     * 
     * @param sDisableParam Restricts write access by user or by user class.
     * @param sFunctionKey Security key that restricts write access by specific function.
     * @return Reason for write access restriction, or null if none.
     */
    public static String initSecurity(String sDisableParam, String sFunctionKey) {
        String reason = (String) FrameworkUtil.getAttribute(SECURITY_MESSAGE);
        
        if (reason == null) {
            String s = "PROVIDER|BGOZ CAC|BGOZ VIEW ONLY|" + sFunctionKey + StrUtil.U + sDisableParam;
            s = VistAUtil.getBrokerSession().callRPC("BGOUTL CHKSEC", s);
            String[] sKeys = StrUtil.split(StrUtil.piece(s, StrUtil.U), "|", 4);
            String[] sParams = StrUtil.split(StrUtil.piece(s, StrUtil.U, 2), "~", 2);
            boolean isProvider = "1".equals(sKeys[0]);
            // boolean isCAC = "1".equals(sKeys[1]);
            boolean isViewOnly = "1".equals(sKeys[2]);
            boolean isEnabled = "1".equals(sKeys[3]) || sFunctionKey == null;
            boolean isUsrNoEdit = "1".equals(sParams[0]);
            String g_sClsNoEdit = sParams[1];
            
            if (isViewOnly) {
                reason = "You have the 'BGOZ VIEW ONLY' security key";
            } else if (!isProvider && !isEnabled) {
                if (sFunctionKey == null) {
                    reason = "You do not have the 'PROVIDER' key";
                } else {
                    reason = "You do not have either the 'PROVIDER' or the '" + sFunctionKey + "' security keys";
                }
            } else if (isUsrNoEdit) {
                reason = "You have been assigned the '" + sDisableParam + "' parameter";
            } else if (!g_sClsNoEdit.isEmpty()) {
                reason = "You are a member of the user class '" + g_sClsNoEdit + "' which has been assigned the '"
                        + sDisableParam + "' parameter";
            }
            
            FrameworkUtil.setAttribute(SECURITY_MESSAGE, reason == null ? "" : reason
                    + ", thus you cannot make modifications.");
        }
        
        return reason;
    }
    
    public static boolean checkSecurity(boolean silent) {
        String reason = (String) FrameworkUtil.getAttribute(SECURITY_MESSAGE);
        
        if (reason.isEmpty()) {
            return true;
        }
        
        if (!silent) {
            throw new RuntimeException(reason);
        }
        
        return false;
    }
    
    /**
     * If value represents an error code, throws a BgoException.
     * 
     * @param value Value to check.
     * @throws BgoException
     */
    public static void errorCheck(List<String> value) {
        if (value != null && !value.isEmpty()) {
            errorCheck(value.get(0));
        }
    }
    
    /**
     * If value represents an error code, throws a BgoException.
     * 
     * @param value Value to check.
     * @throws BgoException
     */
    public static void errorCheck(String value) {
        long errorCode;
        
        if ((errorCode = errorCode(value)) != 0) {
            EventUtil.status();
            throw new BgoException(errorCode, StrUtil.piece(value, StrUtil.U, 2));
        }
    }
    
    /**
     * Returns the error code, if any, represented in the specified value.
     * 
     * @param value Value containing a possible error code.
     * @return Positive integer if an error code was found, otherwise 0.
     */
    public static long errorCode(String value) {
        long i = value == null ? 0 : NumberUtils.toLong(StrUtil.piece(value, StrUtil.U));
        return i < 0 ? -i : 0;
    }
    
    public static Params packageParams(Object... params) {
        return new Params(params);
    }
    
    /**
     * Enforces static class.
     */
    private BgoUtil() {
    };
    
}
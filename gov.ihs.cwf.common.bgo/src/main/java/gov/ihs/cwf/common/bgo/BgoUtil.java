/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.common.bgo;

import java.util.Date;
import java.util.List;

import gov.ihs.cwf.mbroker.FMDate;
import gov.ihs.cwf.util.RPMSUtil;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.PromptDialog;

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
            s = RPMSUtil.getBrokerSession().callRPC("BGOUTL CHKSEC", s);
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
            PromptDialog.showError(reason, BgoConstants.TX_ERR_PERMISSIONS);
        }
        
        return true;
    }
    
    /**
     * If value represents an error code, displays the error and returns true. Otherwise, returns
     * false.
     * 
     * @param value Value to check.
     * @return True if the value represents an error code.
     */
    public static boolean errorCheck(List<String> value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        return errorCheck(value.get(0), null);
    }
    
    /**
     * If value represents an error code, displays the error and returns true. Otherwise, returns
     * false.
     * 
     * @param value Value to check.
     * @return True if the value represents an error code.
     */
    public static boolean errorCheck(String value) {
        return errorCheck(value, null);
    }
    
    /**
     * If value represents an error code, displays the error and returns true. Otherwise, returns
     * false.
     * 
     * @param value Value to check.
     * @param caption Optional caption to display for error message.
     * @return True if the value represents an error code.
     */
    public static boolean errorCheck(String value, String caption) {
        if (errorCode(value) != 0) {
            PromptDialog.showError(StrUtil.piece(value, StrUtil.U, 2), caption);
            EventUtil.status();
            return true;
        }
        
        return false;
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
    
    public static String normalizeDate(String value) {
        return normalizeDate(value, false);
    }
    
    public static String normalizeDate(String value, boolean includeTime) {
        Date date = parseDate(value);
        return date == null ? "" : DateUtil.formatDate(date, false, !includeTime);
    }
    
    public static Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        if (NumberUtils.isNumber(value)) {
            return new FMDate(value);
        }
        
        return DateUtil.parseDate(value);
    }
    
    public static String trimNarrative(String narrative) {
        return StrUtil.xlate(narrative == null ? "" : narrative, "^\n\r", "   ").trim();
    }
    
    public static String getSysParam(String param, String dflt, String instance) {
        String s = RPMSUtil.getBrokerSession().callRPC("CIAVMRPC GETPAR", param, "", instance == null ? "1" : instance);
        return s.isEmpty() ? dflt : s;
    }
    
    public static boolean setSysParam(String param, String value) {
        String s = StrUtil.piece(RPMSUtil.getBrokerSession().callRPC("CIAVMRPC SETPAR", param, value, "USR"), StrUtil.U, 2);
        
        if (!s.isEmpty()) {
            PromptDialog.showError(s, "Error Saving Parameter");
            return false;
        }
        
        return true;
    }
    
    /**
     * Converts a parameter list into a ^-delimited string
     * 
     * @param params
     * @return Concatenated list.
     */
    public static String concatParams(Object... params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Object param : params) {
            if (!first) {
                sb.append(StrUtil.U);
            } else {
                first = false;
            }
            
            if (param != null) {
                sb.append(param);
            }
        }
        
        return sb.toString();
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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.context;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.domain.DomainObjectFactory;
import org.carewebframework.vista.api.domain.Provider;
import org.carewebframework.vista.api.util.VistAUtil;

/**
 * Provider-related utilities.
 */
public class ProviderUtil {

    public static Provider fetchProvider(String value) {
        long id = Long.parseLong(StrUtil.piece(value, StrUtil.U));
        return DomainObjectFactory.get(Provider.class, id);
    }

    public static List<Provider> search(String text, int maxHits, List<Provider> hits) {
        if (hits == null) {
            hits = new ArrayList<Provider>();
        } else {
            hits.clear();
        }

        text = text == null ? null : text.trim().toUpperCase();

        if (text == null || text.isEmpty()) {
            return hits;
        }

        List<String> list = VistAUtil.getBrokerSession().callRPCList("BEHOUSCX NEWPERS", null, text, 1,
            "@BEHOENCX PROVIDER", maxHits);

        for (String prv : list) {
            String[] pcs = prv.split("\\^");

            if (pcs[1].toUpperCase().startsWith(text)) {
                Provider provider = new Provider(pcs[0]);
                provider.setFullName(pcs[1]);
                hits.add(provider);
            } else {
                break;
            }
        }

        return hits;
    }

    /**
     * Enforces static class.
     */
    private ProviderUtil() {
    };
}

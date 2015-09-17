package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.StaticRules;

import java.util.Map;

/**
 * Created by pim on 7/14/15.
 */
public class GenericAPIActions {

    public static int getLimit(Map<String, String[]> queryString) {
        /**
         * Object limits
         */

        int limit = StaticRules.DEFAULT_MAX_LIMIT;


        if (queryString.get("limit") != null) {
            String limitString =queryString.get("limit")[0];
            if (Safety.isInteger(limitString)) {
                int limitProposed = Integer.parseInt(limitString);
                if (limitProposed > StaticRules.DEFAULT_MAX_LIMIT) {
                    limit = StaticRules.DEFAULT_MAX_LIMIT;
                } else {
                    limit = limitProposed;
                }
            }
        }
        return limit;
    }

    public static int getOffset(Map<String, String[]> queryString) {

        int offset = StaticRules.DEFAULT_MIN_OFFSET;

        if (queryString.get("offset") != null) {
            String offsetString = queryString.get("offset")[0];
            if (Safety.isInteger(offsetString)) {
                offset = Integer.parseInt(offsetString);
            }
        }

        return offset;
    }

    public static int getCeil(Map<String, String[]> queryString) {

        int ceil = StaticRules.DEFAULT_MAX_CEIL;

        if (queryString.get("ceil") != null) {
            String ceilString = queryString.get("ceil")[0];
            if (Safety.isInteger(ceilString)) {
                ceil = Integer.parseInt(ceilString);
            }
        }
        return ceil;
    }
}

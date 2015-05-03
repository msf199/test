package whitespell.logic;

import whitespell.model.baseapi.Unit;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.logic
 */
public class UnitHandler {

    public static HashMap<String, Unit> Units = new HashMap<String, Unit>();

    /**
     * generateAdId generates an Unit ID, this is mainly used when the advertiser would like to run a new ad.
     */

    public static String generateAdId() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    /**
     * PutUnit puts a new Unit in the ads, along with it's model.
     * @param uuid              the unique identifier of the ad
     * @param ad                the model of the ad (for example the name, creation date)
     */

    public static void putUnit(String uuid, Unit ad) {
        Units.put(uuid, ad);
    }

    public static Unit getAd(String uuid) {
        return Units.get(uuid);
    }
}

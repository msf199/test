package whitespell.logic;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell.logic
 */
public class Safety {

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

}

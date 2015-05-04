package whitespell.logic;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * @author Pim de Witte(wwadewitte), Josh Lipson(mrgalkon), Whitespell LLC
 *         5/3/15
 *         whitespell.logic
 */
public class Safety {

    public static String getSalt() throws NoSuchAlgorithmException, NoSuchProviderException {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt.toString();
    }

    /**
     * Get a long as an integer and check that the integrity of the value isn't compromised.
     * @param l The long value to convert to an integer.
     * @return the value of the long as an integer.
     */
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    /**
     * Check whether or not a string is strictly numeric.
     * @param string The string to check.
     * @return <i>true</i> if the string is strictly numeric.
     */
    public static boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Check whether or not a user id is valid.
     * @param id The user id to check the validity of.
     * @return <i>true</i> if id is greater than or equal to 0 and less than or equal to Integer.MAX_VALUE.
     */
    public static boolean isValidUserId(int id) {
        return id >= 0 && id <= Integer.MAX_VALUE;
    }


}

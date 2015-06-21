package tests.tests;


/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/27/15
 *         whitespell.endpoints.tests
 */
public class CreateUserTest {

    /**
     * Requirements for Create user:
     *
     * @usernames
     * Should accept a username no shorter than StaticRules.MIN_USERNAME_LENGTH(1)
     * Should accept a username no longer than StaticRules.MAX_USERNAME_LENGTH(30) - smaller screens can not display these.
     *
     * @emails
     * Should accept an email no shorter than StaticRules.MIN_EMAIL_LENGTH(5) -- e.g. i@i.me
     * Should accept an email no longer than StaticRules.MAX_EMAIL_LENGTH(512)
     *
     * @passwords
     * Should accept a password no shorter than StaticRules.MIN_PASSWORD_LENGTH (5)
     * Should accept a password no longer than StaticRules.MAX_PASSWORD_LENGTH (512)
     * Passwords should be able to contain any character, including unicode characters.
     */


    /**
     * Tests:
     * == null value error caused
     * 1. Insert a call with no data at all, and assert equal on null value found
     * 2. Insert a call with just username, and assert equal on null value found
     * 3. Insert a call with just password, and assert equal on null value found
     * 4. Insert a call with just email, and assert equal on null value found
     * 5. Inesrt username+password and no email and assert on null value found
     * 6. Insert username+email and assertequal on no value found
     * 7. Insert password+email and assertequal on no value found
     * == unauthorized error (401)
     * 1. Insert a username that is length 4 and assetEqual on 401 status code and the error code of the username too short
     * 2. Insert a password
     */


}

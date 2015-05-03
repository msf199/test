package whitespell.sample.MyApplication.endpoints.users;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell.sample.MyApplication.endpoints.users
 */
public class CreateUserTest {

    // send a request with not enough info, expect 400 back:

    /**
     * {
     "username" : "pim",
     "password" : "test123"
     }
     */

    //test with different ASCII characters

    // test with unique username and unique email (should return OK)

    // test with unique username and taken email (should return 401)

    // test with unique email and taken username (should return 401)
}

package tests.tests;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/27/15
 *         whitespell.endpoints.tests
 */
public class BasicTest {

    /**
     * Basic tests:
     * Run server, execute test DDL on test database, build in security that the main dataase is never dropped.
     * Connect to API on port 80, create a ping endpoint that returns whether it is connected to MySQL, and the amount of threads and memory the application is consuming.
     * Insert a bunch of users with CreateUserTest
     * Return a bunch of users and assert equal on them
     * Check if memory is still stable and threads are being opened and closed properly
     * Authenticate as the users a bunch of times and ensure that the authentication works properly
     * Check if memory is still stable etc.
     * Insert and return content...
     */
}

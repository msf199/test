package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.sql.SQLException;
import java.sql.Timestamp;

import static main.com.whitespell.peak.logic.MandrillMailer.sendEmail;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/13/2015
 */
public class EmailSend {

    private static final int EXPIRES_IN_24_HOURS = 86400000;
    private static final String UPDATE_EMAIL_TOKEN = "UPDATE `user` SET `email_token` = ?, `email_expiration` = ? WHERE `username` = ? LIMIT 1";

    public static EmailTokenResponseObject updateDBandSendEmail(String username, String email){
        try {
            String emailToken = main.com.whitespell.peak.logic.SessionIdentifierGenerator.nextEmailId();

            /**
             * Update the user's email verification status in the database.
             */

            try {
                StatementExecutor executor = new StatementExecutor(UPDATE_EMAIL_TOKEN);
                Timestamp ts = new Timestamp(System.currentTimeMillis() + EXPIRES_IN_24_HOURS);
                final String finalUsername = username;
                final String finalEmailToken = emailToken;
                final Timestamp finalEmailExpiration = ts;
                executor.execute(ps -> {
                    ps.setString(1, finalEmailToken);
                    ps.setTimestamp(2, finalEmailExpiration);
                    ps.setString(3, finalUsername);

                    ps.executeUpdate();
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return null;
            }
            boolean sent =
                    sendEmail("noreply@peakapp.me", "Pim, CEO of Peak", "Welcome to Peak!",
                            "<html><body><h1>Congratulations " + username + "!" +
                                    "</h1>" +
                                    "<br>Here is your email token: " + emailToken + "<br>" +
                                    "It expires in 24 hours so click <a href=\\\"http://www.peakapp.me\\\">here</a> to get started!</body></html>", email);

            if(sent){
                EmailTokenResponseObject et = new EmailTokenResponseObject();
                et.setEmailToken(emailToken);
                return et;
            }
        }
        catch(Exception e){
            Logging.log("High", e);
            return null;
        }
        return null;
    }

    public static class EmailTokenResponseObject{

        String emailToken;

        public EmailTokenResponseObject(){
            this.emailToken = null;
        }

        public String getEmailToken() {
            return emailToken;
        }

        public void setEmailToken(String emailToken) {
            this.emailToken = emailToken;
        }
    }
}

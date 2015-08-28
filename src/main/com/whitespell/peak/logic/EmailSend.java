package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.sql.SQLException;
import java.sql.Timestamp;

import static main.com.whitespell.peak.logic.MandrillMailer.sendContentNotificationTemplatedMessage;
import static main.com.whitespell.peak.logic.MandrillMailer.sendTokenTemplatedMessage;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/13/2015
 */
public class EmailSend {

    private static final int EXPIRES_IN_24_HOURS = 86400000;

    private static final String UPDATE_EMAIL_TOKEN = "UPDATE `user` SET `email_token` = ?, `email_expiration` = ? WHERE `username` = ? LIMIT 1";

    private static final String UPDATE_RESET_TOKEN = "UPDATE `user` SET `reset_token` = ? WHERE `username` = ? LIMIT 1";

    public static tokenResponseObject updateDBandSendWelcomeEmail(String username, String email){
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
                    sendTokenTemplatedMessage("noreply@peakapp.me",
                            "Pim, CEO of Peak",
                            "Welcome to Peak!", "http://ws.kven.me",
                            username, emailToken,
                            "peak", "verify_email", email);

            if(sent){
                tokenResponseObject et = new tokenResponseObject();
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

    public static tokenResponseObject updateDBandSendResetEmail(String username, String email){
        try {
            String resetToken = main.com.whitespell.peak.logic.SessionIdentifierGenerator.nextResetId();

            /**
             * Update the user's Forgot Password reset token in the database.
             */
            boolean sent[] = {false};
            try {
                StatementExecutor executor = new StatementExecutor(UPDATE_RESET_TOKEN);
                final String finalUsername = username;
                final String finalResetToken = resetToken;
                executor.execute(ps -> {
                    ps.setString(1, finalResetToken);
                    ps.setString(2, finalUsername);

                    int rows = ps.executeUpdate();
                    if(rows>=0){
                        sent[0] =
                                sendTokenTemplatedMessage("noreply@peakapp.me",
                                        "Pim, CEO of Peak",
                                        "Password Reset Confirmation", "http://ws.kven.me",
                                        username, resetToken,
                                        "peak-1", "forgot_password", email);
                        System.out.println(resetToken);
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return null;
            }

            if(sent[0]){
                tokenResponseObject et = new tokenResponseObject();
                et.setEmailToken(resetToken);
                return et;
            }
        }
        catch(Exception e){
            Logging.log("High", e);
            return null;
        }
        return null;
    }

    public static boolean sendFollowerContentNotificationEmail(String username, String email, String publisherName, String contentName, String contentUrl){
            /**
             * Send a content upload notification email to the Follower
             */
            boolean sent[] = {false};

            sent[0] =
                    sendContentNotificationTemplatedMessage("noreply@peakapp.me",
                            "Pim, CEO of Peak",
                            publisherName + " uploaded a new video!", "http://ws.kven.me",
                            username,  contentName, contentUrl, "peak-2", "content_notification", email);
        return sent[0];
    }

    public static class tokenResponseObject {

        String emailToken;

        public tokenResponseObject(){
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

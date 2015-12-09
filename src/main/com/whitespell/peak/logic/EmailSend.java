package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.config.Config;
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

    private static final String UPDATE_EMAIL_TOKEN = "UPDATE `user` SET `email_token` = ?, `email_expiration` = ?, `email_verified` = ? WHERE `username` = ? LIMIT 1";

    private static final String UPDATE_RESET_TOKEN = "UPDATE `user` SET `reset_token` = ? WHERE `username` = ? LIMIT 1";

    public static tokenResponseObject updateDBandSendWelcomeEmail(String username, String email){
        try {
            String emailToken = RandomGenerator.nextEmailId();

            /**
             * Update the user's email verification status in the database.
             */

            try {
                StatementExecutor executor = new StatementExecutor(UPDATE_EMAIL_TOKEN);
                Timestamp ts = new Timestamp(Server.getMilliTime() + EXPIRES_IN_24_HOURS);
                final String finalUsername = username;
                final String finalEmailToken = emailToken;
                final Timestamp finalEmailExpiration = ts;
                executor.execute(ps -> {
                    ps.setString(1, finalEmailToken);
                    ps.setTimestamp(2, finalEmailExpiration);
                    ps.setInt(3, 0);
                    ps.setString(4, finalUsername);

                    ps.executeUpdate();
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return null;
            }
            boolean sent =
                    sendTokenTemplatedMessage(Config.PLATFORM_EMAIL_SEND_ADDRESS,
                            Config.PLATFORM_EMAIL_SEND_NAME,
                            "Welcome to "+ Config.PLATFORM_NAME+"!", Config.PLATFORM_HOME_PAGE_URL,
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
            String resetToken = RandomGenerator.nextResetId();

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
                                sendTokenTemplatedMessage(Config.PLATFORM_EMAIL_SEND_ADDRESS,
                                        Config.PLATFORM_EMAIL_SEND_NAME,
                                        "Password Reset Confirmation", Config.PLATFORM_HOME_PAGE_URL,
                                        username, resetToken,
                                        "peak-1", "forgot_password", email);
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

    public static boolean sendFollowerContentNotificationEmail(String userThumb, String email, String publisherName, String contentName, String contentUrl){
        /**
         * Send a content upload notification email to the Follower
         */

        boolean sent[] = {false};

        sent[0] =
                sendContentNotificationTemplatedMessage(Config.PLATFORM_EMAIL_SEND_ADDRESS,
                        Config.PLATFORM_EMAIL_SEND_NAME,
                        publisherName + " uploaded a new video!", Config.PLATFORM_HOME_PAGE_URL,
                        publisherName,  contentName, contentUrl, null,
                        "content-follower-notification", userThumb, email);

        return sent[0];
    }

    public static boolean sendSocialMediaLinkNotificationEmail(String contentThumb, String email, String publisherName, String contentName, String contentUrl, String contentUrl2){
        /**
         * Send a social media link notification to the publisher after their video processes
         */

        boolean sent[] = {false};

        sent[0] =
                sendContentNotificationTemplatedMessage(Config.PLATFORM_EMAIL_SEND_ADDRESS,
                        Config.PLATFORM_EMAIL_SEND_NAME,
                        "Share "+contentName+" on social media!", Config.PLATFORM_HOME_PAGE_URL,
                        publisherName,  contentName, contentUrl, contentUrl2,
                        "social-media-link-notification", contentThumb, email);

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

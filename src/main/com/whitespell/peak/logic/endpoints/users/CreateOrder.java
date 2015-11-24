package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.ContentHelper;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/8/15
 *         whitespell.logic.endpoints.users
 */

public class CreateOrder extends EndpointHandler {

    private static final String INSERT_ORDER_UPDATE = "INSERT INTO `order`(" +
            "`order_type`, `order_status`, `publisher_id`, `buyer_id`, `content_id`," +
            " `price`, `net_revenue`, `currency_id`, `publisher_share`, `peak_share`," +
            " `publisher_balance`, `peak_balance`, `receipt_html`, `email_sent`, `buyer_details`," +
            " `delivered`, `timestamp`) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String GET_ORDER_TYPE_NAME_QUERY = "SELECT `order_type_name` from `order_type` where `order_type_id` = ?";
    private static final String GET_ORDER_ORIGIN_NAME_QUERY = "SELECT `order_origin_name` from `order_origin` where `order_origin_id` = ?";

    //payload enums
    private static final String PAYLOAD_ORDER_TYPE_KEY = "orderType";
    private static final String PAYLOAD_ORDER_STATUS_KEY = "orderStatus";
    private static final String PAYLOAD_PUBLISHER_ID_KEY = "publisherId";
    private static final String PAYLOAD_BUYER_ID_KEY = "buyerId";
    private static final String PAYLOAD_CONTENT_ID_KEY = "contentId";
    private static final String PAYLOAD_CURRENCY_ID_KEY = "currencyId";
    private static final String PAYLOAD_BUYER_DETAILS_KEY = "buyerDetails";
    private static final String PAYLOAD_ORDER_ORIGIN_KEY = "orderOriginId";

    //db enums
    private static final String DB_ORDER_TYPE_NAME_KEY = "order_type_name";
    private static final String DB_ORDER_ORIGIN_NAME_KEY = "order_origin_name";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_ORDER_TYPE_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_ORDER_STATUS_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_PUBLISHER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_BUYER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CURRENCY_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_BUYER_DETAILS_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_ORDER_ORIGIN_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        //check order_origin, do calculation based on that.
        //google/apple take .3, web is price === net_rev
        //net_revenue = price - (price * .3)
        //handle price, net revenue, publisherShare, peakShare, publisherBalance = 0, receipt_html ... , email_sent -> method,
        //delivered = 0

        JsonObject j = context.getPayload().getAsJsonObject();

        //payload variables
        final int orderType = j.get(PAYLOAD_ORDER_TYPE_KEY).getAsInt();
        final int orderStatus = j.get(PAYLOAD_ORDER_STATUS_KEY).getAsInt();
        final int publisherId = j.get(PAYLOAD_PUBLISHER_ID_KEY).getAsInt();
        final int buyerId = j.get(PAYLOAD_BUYER_ID_KEY).getAsInt();
        final int contentId = j.get(PAYLOAD_CONTENT_ID_KEY).getAsInt();
        final int currencyId = j.get(PAYLOAD_CURRENCY_ID_KEY).getAsInt();
        final String buyerDetails = j.get(PAYLOAD_BUYER_DETAILS_KEY).getAsString();
        final int orderOriginId = j.get(PAYLOAD_ORDER_ORIGIN_KEY).getAsInt();
        final Timestamp now = new Timestamp(Server.getMilliTime());

        /**
         * Ensure the user is authenticated properly
         */
        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = buyerId == a.getUserId();

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Get the contentObject that is being ordered
         */
        ContentHelper h = new ContentHelper();

        ContentObject orderContent;
        try{
            orderContent = h.getContentById(context, contentId, a.getUserId());
        } catch(UnirestException e){
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }

        System.out.println("orderContent: " +orderContent);

        /**
         * Ensure content publisher matches payload
         */
        if(orderContent != null && orderContent.getPoster().getUserId() != publisherId){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_PAYLOAD);
            return;
        }

        /**
         * Get the price of this content to calculate revenue and shares
         */

        double price = orderContent.getContentPrice();

        //determined variables
        String[] orderTypeName = {""};
        String[] orderStatusName = {""};
        String[] orderOriginName = {""};
        final double netRevenue;
        final double peakShare;
        double peakBalance;
        final double publisherShare;
        double publisherBalance;
        String receiptHtml;

        //need to update
        int delivered;

        /**
         * Get orderTypeName
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_ORDER_TYPE_NAME_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, orderType);

                ResultSet results = ps.executeQuery();

                if (results.next()) {
                    orderTypeName[0] = results.getString(DB_ORDER_TYPE_NAME_KEY);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Get orderOriginName
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_ORDER_ORIGIN_NAME_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, orderOriginId);

                ResultSet results = ps.executeQuery();

                if (results.next()) {
                    orderOriginName[0] = results.getString(DB_ORDER_ORIGIN_NAME_KEY);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Calculate revenue and shares
         */

        /**
         * Google or apple takes 30%
         */
        if(orderOriginName[0].equalsIgnoreCase("google") || orderOriginName[0].equalsIgnoreCase("apple")){
            netRevenue = price - (price*(.3));
        }else{
            netRevenue = price;
        }

        /**
         * Publishers get 70%
         */

        publisherShare = netRevenue * (.7);

        /**
         * Peak gets the remainder
         */

        peakShare = netRevenue - publisherShare;

        /**
         * Insert the order in the database and return the receipt on success, fail with 500 if it fails
         */

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_ORDER_UPDATE);
            executor.execute(ps -> {
                ps.setInt(1, orderType);
                ps.setInt(2, orderStatus);
                ps.setInt(3, publisherId);
                ps.setInt(4, buyerId);
                ps.setInt(5, contentId);
                ps.setDouble(6, price);
                ps.setDouble(7, netRevenue);
                ps.setInt(8, currencyId);
                ps.setDouble(9, publisherShare);
                ps.setDouble(10, peakShare);
                //11 = pubBalance
                ps.setDouble(11, 0);
                //12 = peakBalance
                ps.setDouble(12, 0);
                //13 = receiptHtml
                ps.setString(13, "receipt");
                //14 = emailSent
                ps.setInt(14, 0);
                ps.setString(15, buyerDetails);
                //16 = delivered
                ps.setInt(16, 0);
                ps.setTimestamp(17, now);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("order successfully inserted for userId " + buyerId);

                    CreateOrderResponse or = new CreateOrderResponse();
                    or.setSuccess(true);
                    Gson g = new Gson();
                    String response = g.toJson(or);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }else{
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_SUBMIT_ORDER);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Send an email with the receipt_html
         */
        //updateDBandSendWelcomeEmail(username, email);

        /**
         * Send a push notification to the user regarding a successful purchase
         */
        //Server.NotificationService.offerNotification(new WelcomeNotification(user_id[0]));
    }

    public class CreateOrderResponse {

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        boolean success = false;
    }
}

package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.ContentHelper;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
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

    private static final String INSERT_ORDER_UPDATE = "INSERT INTO `order`(`order_uuid`," +
            " `order_type`, `order_origin`, `publisher_id`, `buyer_id`, `content_id`," +
            " `price`, `net_revenue`, `currency_id`, `publisher_share`, `peak_share`," +
            " `publisher_balance`, `peak_balance`, `receipt_html`, `email_sent`, `delivered`," +
            " `timestamp`) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_USER_SUBSCRIPTION_UPDATE = "INSERT INTO `user_subscriptions`(`user_id`," +
            " `subscription_start`, `subscription_end`, `subscription_renew_day`, " +
            "`subscription_price`, `subscription_type`, `subscription_token`) " +
            "VALUES (?,?,?,?,?,?,?)";

    private static final String INSERT_SUBSCRIBER_UPDATE = "UPDATE `user` SET `subscriber` = 1 WHERE `user_id` = ?";

    private static final String INSERT_ORDER_STATUS_UPDATE = "INSERT INTO `order_status`(`order_uuid`,`order_status_name`) " +
            "VALUES (?,?)";

    private static final String GET_ORDER_TYPE_NAME_QUERY = "SELECT `order_type_name` FROM `order_type` WHERE `order_type_id` = ?";
    private static final String GET_ORDER_ORIGIN_NAME_QUERY = "SELECT `order_origin_name` FROM `order_origin` WHERE `order_origin_id` = ?";

    //payload enums
    private static final String PAYLOAD_ORDER_UUID_KEY = "orderUUID";
    private static final String PAYLOAD_ORDER_TYPE_KEY = "orderType";
    private static final String PAYLOAD_PUBLISHER_ID_KEY = "publisherId";
    private static final String PAYLOAD_ORDER_PAYLOAD = "orderPayload";
    private static final String PAYLOAD_BUYER_ID_KEY = "buyerId";
    private static final String PAYLOAD_CONTENT_ID_KEY = "contentId";
    private static final String PAYLOAD_ORDER_ORIGIN_KEY = "orderOriginId";

    //db enums
    private static final String DB_ORDER_TYPE_NAME_KEY = "order_type_name";
    private static final String DB_ORDER_ORIGIN_NAME_KEY = "order_origin_name";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_ORDER_UUID_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_TYPE_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_PUBLISHER_ID_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_PAYLOAD, StaticRules.InputTypes.REG_STRING_OPTIONAL_UNLIMITED);
        payloadInput.put(PAYLOAD_BUYER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_ID_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_ORIGIN_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        //todo(cmcan)
        /**
         * If fail, make sure to update 'orderStatus' with order_status_name = FAIL and orderUUID, if success
         * update `orderStatus` with success and orderUUID
         */

        JsonObject j = context.getPayload().getAsJsonObject();

        //payload variables
        String orderUUID = j.get(PAYLOAD_ORDER_UUID_KEY).getAsString();
        if(orderUUID.length() <= 1) {
            orderUUID = "fail-"+System.currentTimeMillis();
        }
        final int orderType = j.get(PAYLOAD_ORDER_TYPE_KEY).getAsInt();
        final int buyerId = j.get(PAYLOAD_BUYER_ID_KEY).getAsInt();
        final String orderPayload = j.get(PAYLOAD_ORDER_PAYLOAD).getAsString();

        final int[] publisherId = {-1};
        if(j.get(PAYLOAD_PUBLISHER_ID_KEY) != null){
            publisherId[0] = j.get(PAYLOAD_PUBLISHER_ID_KEY).getAsInt();
        }
        final int[] contentId = {-1};
        if(j.get(PAYLOAD_CONTENT_ID_KEY) != null){
            contentId[0] = j.get(PAYLOAD_CONTENT_ID_KEY).getAsInt();

        }
        final int orderOriginId = j.get(PAYLOAD_ORDER_ORIGIN_KEY).getAsInt();
        final long currTime = Server.getMilliTime();
        final Timestamp now = new Timestamp(currTime);

        /**
         * If orderType bundle, ensure contentId and publisherId specified
         */
        if(orderType == Config.ORDER_TYPE_BUNDLE && (contentId[0] == -1 || publisherId[0] == -1)){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_PAYLOAD);
            return;
        }

        /**
         * If invalid orderType throw error
         */
        if(orderType != Config.ORDER_TYPE_BUNDLE && orderType != Config.ORDER_TYPE_SUBSCRIPTION){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_TYPE);
            return;
        }

        /**
         * If invalid orderOrigin throw error
         */
        if(orderOriginId != Config.ORDER_ORIGIN_GOOGLE && orderOriginId != Config.ORDER_ORIGIN_APPLE && orderOriginId != Config.ORDER_ORIGIN_WEB){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_ORIGIN);
            return;
        }

        /**
         * Ensure the user is authenticated properly
         */
        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = buyerId == a.getUserId();

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        //(todo) cmcan fix apple pay verification, could be malformed orderUUID
        /**
         * Use Google and Apple Requests to check that order information is valid and that order was submitted. Otherwise
         * reject order creation
         */

        /**
         * Apple endpoint call to verify receipt data
         */

        /**
         * (todo) Complete validation once iOS app is ready.
         */


        if(orderOriginId == Config.ORDER_ORIGIN_APPLE) {
            try {
                    HttpResponse<String> stringResponse = Unirest.post("https://sandbox.itunes.apple.com/verifyReceipt")
                            .header("accept", "application/json")
                            .body("{\n" +
                                    "\"receipt-data\":" + "\"" +orderPayload+ "\"" +
                                    "}")
                            .asString();


                if(stringResponse.getBody() != null && stringResponse.getBody().contains("\"status\":0")) {
                    JsonParser parser = new JsonParser();
                    JsonObject o = parser.parse(stringResponse.getBody()).getAsJsonObject();
                    JsonArray inApp = o.get("receipt").getAsJsonObject().get("in_app").getAsJsonArray();

                    for (int i = 0; i < inApp.size(); i++) {
                        if (i == inApp.size() - 1) {
                            orderUUID = inApp.get(i).getAsJsonObject().get("transaction_id").getAsString();
                            System.out.println(orderUUID);
                        }
                    }
                } else {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_PAYLOAD);
                   return;
                }


            } catch (Exception e) {
                //couldnt get UUID, store receipt locally?
            }
        } /*else if (orderOriginId == Config.ORDER_ORIGIN_GOOGLE){


        }

            try {

                String emailAddress = "123456789000-abc123def456@developer.gserviceaccount.com";
                JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(httpTransport)
                        .setJsonFactory(JSON_FACTORY)
                        .setServiceAccountId(emailAddress)
                        .setServiceAccountPrivateKeyFromP12File(new File("certificates/MyProject.p12"))
                        .setServiceAccountScopes(Collections.singleton(SQLAdminScopes.SQLSERVICE_ADMIN))
                        .build();

                SQLAdmin sqladmin =
                        new SQLAdmin.Builder(httpTransport, JSON_FACTORY, credential).build();

            } catch (Exception e) {

                *//**
                 * Update order status to FAIL
                 *//*


                try {
                    StatementExecutor executor2 = new StatementExecutor(INSERT_ORDER_STATUS_UPDATE);
                    executor2.execute(ps2 -> {
                        ps2.setString(1, orderUUID);
                        ps2.setString(2, "fail");

                        ps2.executeUpdate();
                    });
                } catch (SQLException s) {
                    Logging.log("High", s);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }

                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }

/*
        }


        /**
         * Get the contentObject that is being ordered
         */
        ContentHelper h = new ContentHelper();

        ContentObject orderContent = null;
        try{
            orderContent = h.getContentById(context, contentId[0], a.getUserId());
        } catch(UnirestException e){
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }

        /**
         * Only get content if order is a bundle
         */
        if(orderType == Config.ORDER_TYPE_BUNDLE) {
            try {
                orderContent = h.getContentById(context, contentId[0], a.getUserId());
            } catch (UnirestException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                return;
            }

            /**
             * Ensure content publisher matches payload
             */

            if (orderContent != null && orderContent.getPoster().getUserId() != publisherId[0]) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INCORRECT_ORDER_PAYLOAD);
                return;
            }
        }
        /**
         * Get the price of this content to calculate revenue and shares
         */


        double price;

        if(orderType == Config.ORDER_TYPE_BUNDLE && orderContent != null){
            price = orderContent.getContentPrice();
        }else{
            price = Config.ORDER_SUBSCRIPTION_PRICE;
        }


        //determined variables
        String[] orderTypeName = {""};
        String[] orderOriginName = {""};
        int orderCurrency = Config.ORDER_CURRENCY_USD;
        double netRevenue;
        double whitespellShare;
        double publisherShare;

        //will create a receipt with developerPayload received from transaction (android)
        //will create a receipt TBD (apple).
        String receiptHtml;

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
        if(orderOriginName[0].equals("google") || orderOriginName[0].equals("apple")){
            netRevenue = price - (price*(.3));
        }else{
            netRevenue = price;
        }

        /**
         * Publishers get 70%
         */

        publisherShare = netRevenue * (.7);

        /**
         * Whitespell gets the remainder
         */

        whitespellShare = netRevenue - publisherShare;

        /**
         * Insert the order in the database and return the receipt on success, fail with 500 if it fails
         */

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_ORDER_UPDATE);
            final String finalOrderUUID = orderUUID;
            final double finalPublisherShare = publisherShare;
            final double finalNetRevenue = netRevenue;
            final double finalWhitespellShare = whitespellShare;

            executor.execute(ps -> {
                ps.setString(1, finalOrderUUID);
                ps.setInt(2, orderType);
                ps.setInt(3, orderOriginId);
                ps.setInt(4, publisherId[0]);
                ps.setInt(5, buyerId);
                ps.setInt(6, contentId[0]);
                ps.setDouble(7, price);
                ps.setDouble(8, finalNetRevenue);
                ps.setInt(9, orderCurrency);
                ps.setDouble(10, finalPublisherShare);
                ps.setDouble(11, finalWhitespellShare);
                //13 = pubBalance. Will be updated when the order has been successfully processed
                ps.setDouble(12, finalPublisherShare);
                //14 = whitespellBalance. Will be updated when the order has been successfully processed
                ps.setDouble(13, finalWhitespellShare);
                //15 = receiptHtml. Will be updated based on orderOrigin.
                ps.setString(14, "receipt");
                //16 = emailSent. Will be updated in the emailSend method for orders.
                ps.setInt(15, 0);
                //18 = delivered. 0 initially, will be updated when user views content in app.
                ps.setInt(16, 0);
                ps.setTimestamp(17, now);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    /**
                     * Update order_status table with orderUUID and success (order has succeeded if it reached this point)
                     */
                    try {
                        StatementExecutor executor2 = new StatementExecutor(INSERT_ORDER_STATUS_UPDATE);

                        executor2.execute(ps2 -> {
                            ps2.setString(1, finalOrderUUID);
                            ps2.setString(2, "success");

                            ps2.executeUpdate();
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    System.out.println("order successfully inserted for userId " + buyerId);

                    /**
                     * If order is a subscription, add user to subscriber's table
                     */
                    if(orderType == Config.ORDER_TYPE_SUBSCRIPTION){
                        try {
                            StatementExecutor executor2 = new StatementExecutor(INSERT_USER_SUBSCRIPTION_UPDATE);
                            executor2.execute(ps2 -> {
                                ps2.setInt(1, buyerId);
                                ps2.setTimestamp(2, now);
                                ps2.setTimestamp(3, new Timestamp(currTime + (StaticRules.MS_ONE_DAY * StaticRules.DAYS_IN_A_MONTH)));
                                ps2.setTimestamp(4, new Timestamp(Server.getMilliDay() + (StaticRules.MS_ONE_DAY * StaticRules.DAYS_IN_A_MONTH)));
                                ps2.setDouble(5, Config.ORDER_SUBSCRIPTION_PRICE);
                                ps2.setInt(6, 1);
                                ps2.setString(7, "token");

                                ps2.executeUpdate();
                            });
                        } catch (SQLException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                            return;
                        }

                        /**
                         * Make the user a subscriber in the user table
                         */
                        try {
                            StatementExecutor executor2 = new StatementExecutor(INSERT_SUBSCRIBER_UPDATE);
                            executor2.execute(ps2 -> {
                                ps2.setInt(1, buyerId);

                                ps2.executeUpdate();
                            });
                        } catch (SQLException e) {
                            Logging.log("High", e);
                            //don't throw client side error

                        }

                        System.out.println("subscription successfully placed for userId " + buyerId);
                    }

                    CreateOrderResponse or = new CreateOrderResponse();
                    or.setSuccess(true);
                    or.setOrderType(orderType);
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

        public int getOrderType() {
            return orderType;
        }

        public void setOrderType(int orderType) {
            this.orderType = orderType;
        }

        int orderType = -1;
        boolean success = false;

    }
}

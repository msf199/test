package main.com.whitespell.peak.logic.endpoints.users;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

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

    private static final String ADD_CONTENT_ACCESS_UPDATE = "INSERT INTO `content_access`(`content_id`, `user_id`, `timestamp`) VALUES (?,?,?)";
    private static final String GET_CONTENT_ACCESS_QUERY = "SELECT `content_id` FROM `content_access` WHERE `user_id` = ?";

    private static final String GET_ORDER_TYPE_NAME_QUERY = "SELECT `order_type_name` FROM `order_type` WHERE `order_type_id` = ?";
    private static final String GET_ORDER_ORIGIN_NAME_QUERY = "SELECT `order_origin_name` FROM `order_origin` WHERE `order_origin_id` = ?";
    private static final String GET_ORDER_USERNAME = "SELECT `username` from `user` INNER JOIN `order` ON `order`.buyer_id = `user`.user_id WHERE `order`.order_UUID = ?";

    //payload enums
    private static final String PAYLOAD_ORDER_UUID_KEY = "orderUUID";
    private static final String PAYLOAD_ORDER_TYPE_KEY = "orderType";
    private static final String PAYLOAD_PUBLISHER_ID_KEY = "publisherId";
    private static final String PAYLOAD_ORDER_PAYLOAD = "orderPayload";
    private static final String PAYLOAD_PURCHASE_TOKEN = "purchaseToken";
    private static final String PAYLOAD_PRODUCT_ID = "productId";
    private static final String PAYLOAD_BUYER_ID_KEY = "buyerId";
    private static final String PAYLOAD_CONTENT_ID_KEY = "contentId";
    private static final String PAYLOAD_ORDER_ORIGIN_KEY = "orderOriginId";

    //db enums
    private static final String DB_ORDER_TYPE_NAME_KEY = "order_type_name";
    private static final String DB_USERNAME_KEY = "username";
    private static final String DB_ORDER_ORIGIN_NAME_KEY = "order_origin_name";

    /**
     * ContentIds the user already has access to
     */
    Set<Integer> accessibleContentIds = null;

    /**
     * ContentIds user will gain access to
     */
    Set<Integer> contentIdsToGrantAccessTo = null;

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_ORDER_UUID_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_TYPE_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_PUBLISHER_ID_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_PAYLOAD, StaticRules.InputTypes.REG_STRING_OPTIONAL_UNLIMITED);
        payloadInput.put(PAYLOAD_PURCHASE_TOKEN, StaticRules.InputTypes.REG_STRING_OPTIONAL_UNLIMITED);
        payloadInput.put(PAYLOAD_PRODUCT_ID, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_BUYER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_ID_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        payloadInput.put(PAYLOAD_ORDER_ORIGIN_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        /**
         * The receipt HTML of the email we are about to send out when the user places an order.
         */
        String receiptHtml = "TODO BEFORE RELEASE";

        /**
         * Used for checking access and granting access
         */
        accessibleContentIds = new HashSet<>();
        contentIdsToGrantAccessTo = new HashSet<>();

        /**
         * If fail, make sure to update 'orderStatus' with order_status_name = FAIL and orderUUID, if success
         * update `orderStatus` with success and orderUUID
         */

        JsonObject j = context.getPayload().getAsJsonObject();

        //payload variables
        String[] orderUUID = {"fail-"+System.currentTimeMillis()};
        if(j.get(PAYLOAD_ORDER_UUID_KEY) != null){
            orderUUID[0] = j.get(PAYLOAD_ORDER_UUID_KEY).getAsString();
        }


        final int orderType = j.get(PAYLOAD_ORDER_TYPE_KEY).getAsInt();
        final int buyerId = j.get(PAYLOAD_BUYER_ID_KEY).getAsInt();


         String orderPayload = null;

        if(j.get(PAYLOAD_ORDER_PAYLOAD) != null) {
            orderPayload = j.get(PAYLOAD_ORDER_PAYLOAD).getAsString();
        }

        String purchaseToken = null;

        if(j.get(PAYLOAD_PURCHASE_TOKEN) != null) {
            purchaseToken = j.get(PAYLOAD_PURCHASE_TOKEN).getAsString();
        }

        String productId = null;

        if(j.get(PAYLOAD_PRODUCT_ID) != null) {
            productId = j.get(PAYLOAD_PRODUCT_ID).getAsString();
        }

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
        /**
         * Also check that payload is accurate based on orderOrigin
         */
        System.out.println("contentId: " + contentId[0]);
        System.out.println("publisherId: " + publisherId[0]);
        System.out.println("orderType: " + orderType);

        if((orderType == Config.ORDER_TYPE_BUNDLE && (contentId[0] <= 0 || publisherId[0] <= 0))
                || (orderOriginId == Config.ORDER_ORIGIN_GOOGLE && (orderUUID == null || productId == null || purchaseToken == null))
                || (orderOriginId == Config.ORDER_ORIGIN_APPLE && orderPayload == null)
                || (orderOriginId == Config.ORDER_ORIGIN_WEB && purchaseToken == null)){
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


        /**
         * Get all the contentAccess details for this user, construct list of contentIds to prevent multiple grant access attempts
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_ACCESS_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, a.getUserId());

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    accessibleContentIds.add(results.getInt("content_id"));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        if(accessibleContentIds.contains(contentId[0])){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ALREADY_HAVE_ACCESS);
            return;
        }


        /**
         * Use Google and Apple Requests to check that order information is valid and that order was submitted. Otherwise
         * reject order creation
         */

        /**
         * Apple endpoint call to verify receipt data
         */

        if(orderOriginId == Config.ORDER_ORIGIN_APPLE) {
            try {
                    HttpResponse<String> stringResponse = Unirest.post("https://buy.itunes.apple.com/verifyReceipt")
                            .header("accept", "application/json")
                            .body("{\n" +
                                    "\"receipt-data\":" + "\"" +orderPayload+ "\"," +
                                    "\"password\":" + "\"4b2c76541cb641359bc5a981c1d36349\"" +
                                    "}")
                            .asString();

                if(stringResponse.getBody().contains("21007")) {
                    stringResponse = Unirest.post("https://sandbox.itunes.apple.com/verifyReceipt")
                            .header("accept", "application/json")
                            .body("{\n" +
                                    "\"receipt-data\":" + "\"" +orderPayload+ "\"," +
                                    "\"password\":" + "\"4b2c76541cb641359bc5a981c1d36349\"" +
                                    "}")
                            .asString();
                }


                if(stringResponse.getBody() != null && stringResponse.getBody().contains("\"status\":0")) {
                    Logging.log("HIGH", stringResponse.getBody());
                    JsonParser parser = new JsonParser();
                    JsonObject o = parser.parse(stringResponse.getBody()).getAsJsonObject();
                    JsonArray inApp = o.get("receipt").getAsJsonObject().get("in_app").getAsJsonArray();


                    long lastPurchaseTime = -1;
                    JsonObject latestPurchase = null;
                    for (int i = 0; i < inApp.size(); i++) {
                        if(inApp.get(i).getAsJsonObject().get("purchase_date_ms").getAsLong() > lastPurchaseTime) {
                            lastPurchaseTime = inApp.get(i).getAsJsonObject().get("purchase_date_ms").getAsLong();
                            latestPurchase = inApp.get(i).getAsJsonObject();
                        }
                    }
                    if(latestPurchase == null) {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.SUBSCRIPTION_FAILED);
                        return;
                    }
                    orderUUID[0] = latestPurchase.get("transaction_id").getAsString();
                } else {
                    Logging.log("High", "Error with payload: " + stringResponse.getBody() + " with payload" + orderPayload);

                    /**
                     *   Update order status to FAIL
                     */

                    try {
                        StatementExecutor executor2 = new StatementExecutor(INSERT_ORDER_STATUS_UPDATE);
                        executor2.execute(ps2 -> {
                            ps2.setString(1, orderUUID[0]);
                            ps2.setString(2, "fail");

                            ps2.executeUpdate();
                        });
                    } catch (SQLException s) {
                        Logging.log("High", s);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED);
                    return;
                }


            } catch (Exception e) {
                Logging.log("HIGH", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED);
                return;
            }
        } else if (orderOriginId == Config.ORDER_ORIGIN_WEB) {

            /**
             * Some parameters so that we can update the stripe order after it's processed
             */
            Map<String, String> initialMetadata = new HashMap<>();
            Map<String, Object> chargeParams = new HashMap<>();

            RequestOptions options = RequestOptions
                    .builder()
                    .setIdempotencyKey(RandomGenerator.nextSessionId())
                    .build();


            Stripe.apiKey = "sk_test_dXjn0tvA0REBaueScdQKxQaN";

            if(buyerId != a.getUserId()) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "You can only buy for yourself");
                return;
            }

            // Get the credit card details submitted by the form
            String token = purchaseToken;
            Charge charge = null;
            // Create the charge on Stripe's servers - this will charge the user's card
            try {
                // Use Stripe's library to make requests...


                ContentObject c = null;
                try {
                     c = new ContentHelper().getContentById(context, contentId[0], 134);
                } catch (Exception e) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Content lookup failed");
                    return;
                }

                if(c.getContentPrice() < 0.1) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Can't buy free bundles");
                    return;
                }

                chargeParams.put("amount", (int) c.getContentPrice() * 100); // amount in cents, again
                chargeParams.put("currency", "usd");
                chargeParams.put("source", token);
                chargeParams.put("description", c.getContentTitle());


                // sets the transaction meta data
                initialMetadata.put("buyer_id", Integer.toString(buyerId));
                initialMetadata.put("content_id", Integer.toString(c.getContentId()));
                initialMetadata.put("price", Double.toString(c.getContentPrice()));
                chargeParams.put("metadata", initialMetadata);




                charge = Charge.create(chargeParams, options);

                if(charge == null) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Charge is null");
                    return;
                }

               if(charge.getPaid()) {
                   //success
                   orderUUID[0] = charge.getId();
                   receiptHtml = charge.getInvoice();
               }

            } catch (CardException e) {
                // Since it's a decline, CardException will be caught
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Declined:" + e.getMessage());
                return;
            } catch (RateLimitException e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. Rate Limit" + e.getMessage());
                return;
                // Too many requests made to the API too quickly
            } catch (InvalidRequestException e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. InvRequest Failed:" + e.getMessage());
                return;
            } catch (AuthenticationException e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. Auth Failed:" + e.getMessage());
                return;
            } catch (APIConnectionException e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. Connection Failed:" + e.getMessage());
                return;
            } catch (StripeException e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. Stripe Exception Thrown:" + e.getMessage());
                MandrillMailer.sendDebugEmail(
                        "upfit@whitespell.com",
                        "Upfit",
                        "Stripe fail",
                        "Stripe fail",
                        "Fail:" + e.getMessage(),
                        "Fail:" + e.getMessage(),
                        "debug-email",
                        "pim@whitespell.com"
                );
                return;
            } catch (Exception e) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED, "Card not charged. Exception Thrown:" + e.getMessage());
                MandrillMailer.sendDebugEmail(
                        "upfit@whitespell.com",
                        "Upfit",
                        "Stripe excep fail",
                        "Stripe excep fail",
                        "Fail:" + e.getMessage(),
                        "Fail:" + e.getMessage(),
                        "debug-email",
                        "pim@whitespell.com"
                );
                return;
            }
        } else if (orderOriginId == Config.ORDER_ORIGIN_GOOGLE) {

            /**
             * Google purchases API to verify order
             */
            try {

                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = new JacksonFactory();

                List<String> scopes = new ArrayList<>();
                scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);

                Credential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
                        .setServiceAccountId(Config.GOOGLE_CLIENT_ID)
                        .setServiceAccountPrivateKeyFromP12File(new File(Config.GOOGLE_PRIVATE_KEY_PATH))
                        .setServiceAccountScopes(scopes).build();
                AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).build();
                AndroidPublisher.Purchases purchases = publisher.purchases();
                final AndroidPublisher.Purchases.Get request = purchases.get(Config.GOOGLE_PACKAGE_NAME, productId, purchaseToken);

                if(request != null && request.getToken() != null) {
                    System.out.println(orderUUID[0]);
                } else {
                    Logging.log("High", "Error with purchaseToken: " + purchaseToken + " with productId" + productId);

                    try {
                        StatementExecutor executor2 = new StatementExecutor(INSERT_ORDER_STATUS_UPDATE);
                        executor2.execute(ps2 -> {
                            ps2.setString(1, orderUUID[0]);
                            ps2.setString(2, "fail");

                            ps2.executeUpdate();
                        });
                    } catch (SQLException s) {
                        Logging.log("High", s);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED);
                    return;
                }


            } catch (Exception e) {

                /**
                 *   Update order status to FAIL
                 */

                try {
                    StatementExecutor executor2 = new StatementExecutor(INSERT_ORDER_STATUS_UPDATE);
                    executor2.execute(ps2 -> {
                        ps2.setString(1, orderUUID[0]);
                        ps2.setString(2, "fail");

                        ps2.executeUpdate();
                    });
                } catch (SQLException s) {
                    Logging.log("High", s);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }

                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDER_FAILED);
                return;
            }
        }


        /**
         * Get the contentObject that is being ordered
         */
        ContentHelper h = new ContentHelper();
        ContentObject orderContent = null;

        /**
         * Only get content if order is a bundle
         */
        if(orderType == Config.ORDER_TYPE_BUNDLE) {
            orderContent = h.getContentById(context, contentId[0], a.getUserId());

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

        //Currently orderCurrency is always USD
        int orderCurrency = 1;
        double netRevenue;
        double whitespellShare;
        double publisherShare;

        //will create a receipt with developerPayload received from transaction (android)
        //will create a receipt TBD (apple).


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
         * Subscription shares will be calculated at end of month, place 0 on creation
         */
        if(orderType == Config.ORDER_TYPE_SUBSCRIPTION){
            publisherShare = 0;
            whitespellShare = 0;
        }

        /**
         * Insert the order in the database and return the receipt on success, fail with 500 if it fails
         */
        final String finalOrderUUID = orderUUID[0];
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_ORDER_UPDATE);
            final double finalPublisherShare = publisherShare;
            final double finalNetRevenue = netRevenue;
            final double finalWhitespellShare = whitespellShare;


            final String finalReceiptHtml = receiptHtml;
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
                ps.setString(14, finalReceiptHtml);
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
                }else{
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_SUBMIT_ORDER);
                    return;
                }
            });
        } catch (SQLException e) {
            if(e.getMessage().contains("Duplicate entry")) {
                Logging.log("High", e);

                final String[] username = new String[1];


                /**
                 * Get orderTypeName
                 */
                try {
                    StatementExecutor executor = new StatementExecutor(GET_ORDER_USERNAME);
                    executor.execute(ps -> {
                        ps.setString(1, finalOrderUUID);

                        ResultSet results = ps.executeQuery();

                        if (results.next()) {
                            username[0] = results.getString(DB_USERNAME_KEY);
                        }
                    });
                } catch (SQLException e2) {
                    Logging.log("High", e2);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }


                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EXISTING_SUBSCRIPTION_ON_ACC, "You have an existing subscription on another account: "+username[0]+". Contact support to transfer it over.");
                return;
            }
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Grant content access to this content if bundle purchase
         */
        if(orderType == Config.ORDER_TYPE_BUNDLE){
            /**
             * Grant access to this content
             */

            if (orderContent == null) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                return;
            }

            /** If the type is a bundle, we need to grant all the children of the bundle access as well **/

            if (orderContent != null && orderContent.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                recursiveGrantChildrenAccess(orderContent);
            } else if (orderContent != null && accessibleContentIds != null && !accessibleContentIds.contains(orderContent.getContentId())) {
                contentIdsToGrantAccessTo.add(orderContent.getContentId());
            }

            /**
             * If no access can be granted, return
             */
            if (contentIdsToGrantAccessTo.size() == 0){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_GRANT_CONTENT_ACCESS);
                return;
            }

            /**
             * Attempt to grant access to the relevant contentIds
             */
            for (int to_insert_content_id: contentIdsToGrantAccessTo){

                try {
                    StatementExecutor executor = new StatementExecutor(ADD_CONTENT_ACCESS_UPDATE);

                    executor.execute(ps -> {
                        ps.setInt(1, to_insert_content_id);
                        ps.setInt(2, a.getUserId());
                        ps.setTimestamp(3, now);

                        int rows = ps.executeUpdate();

                        if (rows > 0) {
                            System.out.println("content_access successfully granted for contentId " + to_insert_content_id + " and userId " + a.getUserId());
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            }
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

        /**
         * Send an email with the receipt_html
         */
        //updateDBandSendWelcomeEmail(username, email);

        UserObject u = new UserHelper().getUserById(buyerId, false, false, false, false);

        if(u.getEmailVerified() == 1) {
            MandrillMailer.sendDebugEmail("upfit@whitespell.com", "Upfit", "Thank you for your business", "Upfit", "Dear " + u.getUserName() + "." +
                            " Thank you for placing your order. We have succesfully charged your card for an amount of " +Config.ORDER_CURRENCY_USD_SYMBOL + price + " " + Config.ORDER_CURRENCY_USD_NAME + ". If there are any issues, please reach out on upfit@whitespell.com, and mention your order ID: " + orderUUID[0] + ". You can find your receipt attached.", "Order-UUID: " + orderUUID[0] + "", "debug-email",
                    u.getEmail());
        }

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

    public void recursiveGrantChildrenAccess(ContentObject c) {
        if (accessibleContentIds != null && !accessibleContentIds.contains(c.getContentId())) {
            contentIdsToGrantAccessTo.add(c.getContentId());
        }
        if (c.getChildren() != null) {
            c.getChildren().forEach(this::recursiveGrantChildrenAccess);
        }
    }
}

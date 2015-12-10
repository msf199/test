package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.OrderObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/21/15
 *         whitespell.model
 */

public class GetOrders extends EndpointHandler {

    //DB Get Queries
    private static final String GET_ORDER_DETAILS_QUERY = "SELECT `order_id`, `order_uuid`, `order_type`," +
            " `order_status`, `order_origin`, `content_id`, `publisher_id`," +
            " `receipt_html`, `email_sent`," +
            " `delivered` FROM `order` WHERE `buyer_id` = ?";

    private static final String GET_ORDER_ORIGIN_NAME_QUERY = "SELECT `order_origin_name` FROM `order_origin` WHERE `order_origin_id` = ?";
    private static final String GET_ORDER_STATUS_NAME_QUERY = "SELECT `order_status_name` FROM `order_status` WHERE `order_UUID` = ?";
    private static final String GET_ORDER_TYPE_NAME_QUERY = "SELECT `order_type_name` FROM `order_type` WHERE `order_type_id` = ?";

    private static final String URL_USER_ID = "userId";

    //db enums
    private static final String DB_ORDER_ID_KEY = "order_id";
    private static final String DB_CONTENT_ID_KEY = "content_id";
    private static final String DB_ORDER_ORIGIN_KEY = "order_origin";
    private static final String DB_ORDER_TYPE_KEY = "order_type";
    private static final String DB_ORDER_UUID_KEY = "order_uuid";
    private static final String DB_PUBLISHER_ID_KEY = "publisher_id";
    private static final String DB_ORDER_EMAIL_SENT_KEY = "email_sent";
    private static final String DB_ORDER_DELIVERED_KEY = "delivered";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int contentId = -1;
        boolean[] contentOrder = {false};

        String[] orderTypeName = {""};
        String[] orderStatusName = {""};
        String[] orderOriginName = {""};

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (userId == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Get all the order details to allow the client side to verify if the current user has ordered the content
         * they are trying to view. If so, they will update 'delivered' in database using the updateOrder endpoint.
         */
        ArrayList<OrderObject> orders = new ArrayList<>();
        try {
            StatementExecutor executor = new StatementExecutor(GET_ORDER_DETAILS_QUERY);
            final int finalUserId = userId;
            final int finalContentId = contentId;
            executor.execute(ps-> {

                OrderObject order = null;

                ps.setInt(1, finalUserId);
                if(contentOrder[0]) {
                    ps.setInt(2, finalUserId);
                    ps.setInt(3, finalContentId);
                }

                final ResultSet results = ps.executeQuery();

                while (results.next()) {

                    /**
                     * Get order type, origin and order status from tables
                     */
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_ORDER_ORIGIN_NAME_QUERY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, results.getInt(DB_ORDER_ORIGIN_KEY));

                            ResultSet results2 = ps2.executeQuery();
                            if (results2.next()) {
                                orderOriginName[0] = results2.getString("order_origin_name");
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_ORDER_TYPE_NAME_QUERY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, results.getInt(DB_ORDER_TYPE_KEY));

                            ResultSet results2 = ps2.executeQuery();
                            if (results2.next()) {
                                orderTypeName[0] = results2.getString("order_type_name");
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_ORDER_STATUS_NAME_QUERY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, results.getInt(DB_ORDER_UUID_KEY));

                            ResultSet results2 = ps2.executeQuery();
                            if (results2.next()) {
                                orderStatusName[0] = results2.getString("order_status_name");
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                    /**
                     * Set all necessary details
                     */

                    order = new OrderObject();
                    order.setOrderStatus(orderStatusName[0]);
                    order.setOrderType(orderTypeName[0]);
                    order.setOrderOrigin(orderOriginName[0]);
                    order.setBuyerId(finalUserId);
                    order.setContentId(results.getInt(DB_CONTENT_ID_KEY));
                    order.setOrderId(results.getInt(DB_ORDER_ID_KEY));
                    order.setOrderUUID(results.getString(DB_ORDER_UUID_KEY));
                    order.setPublisherId(results.getInt(DB_PUBLISHER_ID_KEY));
                    order.setEmailSent(results.getInt(DB_ORDER_EMAIL_SENT_KEY));
                    order.setDelivered(results.getInt(DB_ORDER_DELIVERED_KEY));
                    orders.add(order);

                }

                if(orders.size() == 0) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ORDERS_NOT_FOUND);
                    return;
                }

                Gson g = new Gson();
                String response = g.toJson(orders);
                context.getResponse().setStatus(200);
                try {
                    context.getResponse().getWriter().write(response);
                } catch (Exception e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }
}

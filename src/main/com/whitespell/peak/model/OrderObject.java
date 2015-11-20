package main.com.whitespell.peak.model;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/21/15
 *         whitespell.model
 */

public class OrderObject {

    int orderId;
    String orderUUID;
    String orderType;
    String orderStatus;
    String orderOrigin;
    int publisherId;
    int buyerId;
    int contentId;
    String receiptHtml;
    int emailSent;
    String buyerDetails;
    int delivered;

    public OrderObject() {
        orderId = -1;
        orderUUID = null;
        orderType = null;
        orderStatus = null;
        orderOrigin = null;
        publisherId = -1;
        buyerId = -1;
        contentId = -1;
        receiptHtml = null;
        emailSent = -1;
        buyerDetails = null;
        delivered = -1;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getReceiptHtml() {
        return receiptHtml;
    }

    public void setReceiptHtml(String receiptHtml) {
        this.receiptHtml = receiptHtml;
    }

    public int getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(int emailSent) {
        this.emailSent = emailSent;
    }

    public String getBuyerDetails() {
        return buyerDetails;
    }

    public void setBuyerDetails(String buyerDetails) {
        this.buyerDetails = buyerDetails;
    }

    public String getOrderOrigin() {
        return orderOrigin;
    }

    public void setOrderOrigin(String orderOrigin) {
        this.orderOrigin = orderOrigin;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderUUID() {
        return orderUUID;
    }

    public void setOrderUUID(String orderUUID) {
        this.orderUUID = orderUUID;
    }
}

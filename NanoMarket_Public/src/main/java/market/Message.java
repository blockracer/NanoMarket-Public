package market;

import java.math.BigInteger;
import java.math.BigDecimal;

public class Message {
    	private String quantity;
    	private String id;
    	private String email;
	private String sellerAccount;
	private String paymentID;

	public Message(String paymentID) {
		this.paymentID = paymentID;
	}

	public Message(String id, String quantity, String email) {
        	this.quantity = quantity;
        	this.email = email;
        	this.id = id;
    	}

    	// Getters and setters for 'Message' fields
    	public String getId() {
        	return id;
    	}
	public String getPaymentID() {
        	return paymentID;
    	}

	public String getSellerAccount() {
        	return sellerAccount;
    	}
	public String getEmail() {
        	return email;
    	}

    	public String getQuantity() {
        	return quantity;
    	}
        public void setPaymentID(String paymentID) {
            this.paymentID = paymentID;
    	}
}


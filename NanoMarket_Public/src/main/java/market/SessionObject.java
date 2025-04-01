package market;

import org.eclipse.jetty.websocket.api.*;


public class SessionObject {
	private Session session;
	private String id;
	private String paymentAddress;
	private long timestamp;
	private boolean paid;
	private String paymentID;
    private String uniqueAccount;
    private String email;
    private long close;
        
    public SessionObject() {

    }

    public SessionObject(Session session, String paymentID, boolean paid) {
		this.session = session;
		this.paymentID = paymentID;
        this.paid = paid;
	}

	public SessionObject(Session session, String id, String paymentAddress, boolean paid, String paymentID) {
		this.session = session;
		this.id = id;
		this.paymentAddress = paymentAddress;
		this.paid = paid;
		this.paymentID = paymentID;
	}
	
	public SessionObject(Session session, String id, String paymentAddress, boolean paid) {
		this.session = session;
		this.id = id;
		this.paymentAddress = paymentAddress;
		this.paid = paid;


	}
    public SessionObject(Session session, String id, String paymentAddress, boolean paid, String paymentID, String uniqueAccount) {
		this.session = session;
        this.uniqueAccount = uniqueAccount;
		this.id = id;
		this.paymentAddress = paymentAddress;
		this.paid = paid;
		this.paymentID = paymentID;
	}
    public SessionObject(Session session, String id, String paymentAddress, boolean paid, String paymentID, String uniqueAccount, String email) {
		this.session = session;
        this.uniqueAccount = uniqueAccount;
		this.id = id;
		this.paymentAddress = paymentAddress;
		this.paid = paid;
		this.paymentID = paymentID;
		this.email = email;
	}


    //getters
    public String getEmail(){
		return email;
	}

	public String getPaymentID(){
		return paymentID;
	}
    public Session getSession(){
		return session;
	}
    public String getUniqueAccount(){
		return uniqueAccount;
	}

	public String getId(){
		return id;
	}
	public String getAddress(){
		return paymentAddress;
	}
	public long getTimestamp(){
		return timestamp;
	}
    public long getClose(){
		return close;
	}
	public boolean isPaid(){
		return paid;
	}
    //setters
    public void setSession(Session session){
		this.session = session;

	}
    //setters
    public void setPaid(boolean paid){
		this.paid = paid;

	}
    public void setClose(long close){
		this.close = close;

	}

    public void setPaymentAddress(String paymentAddress){
		this.paymentAddress = paymentAddress;

	}

    public void setPaymentID(String paymentID){

		this.paymentID =  paymentID;
	}
    public void setID(String id){

		this.id =  id;
	}


    

}


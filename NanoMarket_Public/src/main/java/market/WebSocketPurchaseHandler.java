package market;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.URISyntaxException;
import java.lang.InterruptedException;
import java.io.IOException;
import java.io.BufferedReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.nio.channels.ClosedChannelException;
import java.util.zip.ZipException;

@WebSocket
public class WebSocketPurchaseHandler {

    private String theMessage = "";
    private final Map<Session, String> sessionMessages = new ConcurrentHashMap<>();
    private final Map<Session, ScheduledExecutorService> keepAliveMethods = new ConcurrentHashMap<>();


    @OnWebSocketConnect
    public void connected(Session session) {

        /*
        ScheduledExecutorService scheduler;

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> sendKeepAliveMessage(session), 0, 5, TimeUnit.SECONDS);


        sessionMessages.put(session, ""); // Initialize with empty mesge
        keepAliveMethods.put(session, scheduler);
        */
        // Disable WebSocket compression by setting the compression threshold to -1
        WebSocketPolicy policy = session.getPolicy();
       // policy.setCompressionThreshold(-1);  // Disable compression
        policy.setMaxTextMessageSize(65536);
        
        // Set up the scheduler to send keep-alive messages every 5 seconds
              }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) throws FileNotFoundException {
        // Cleanup or any other actions when the WebSocket connection is closed
        //ScheduledExecutorService schedular = keepAliveMethods.get(session);
        //schedular.shutdown();
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
    sessionMessages.put(session, message); // Store the latest message per session
    theMessage = message;

    boolean found = false;
    String paymentID = message;
    System.out.println(message);
    String address = "";

    // Find the correct SessionObject where getId() matches the message
    SessionObject sessionObject = null;
    for (SessionObject item : Main.sessionObjects) {
        if(item.getPaymentID() != null) {
        if (item.getPaymentID().equals(paymentID)) {
           System.out.println("FOUND SessionObject for session");
            found = true;
            sessionObject = item;
            sessionObject.setSession(session);
            break;
        }
        }
    }
    //System.out.println("session OBJECT " + sessionObject);

    if (sessionObject != null) {
        // Check if the order is already marked as paid
        if (sessionObject.isPaid()) {
            String cancelID = "";
            System.out.println("PAID IS EQUAL TO TRUE IN MESSAGE METHOD");

            if (session.isOpen()) {
                try {
                    session.getRemote().sendString("PAID");
                    session.close();
                } catch (IOException e) {
                    System.out.println("cant send paid, channel closed: " + e.getMessage());
                    //e.printStackTrace();
                }

            }
            return;
        }

        // Update the session in the sessionObject
        //sessionObject.setSession(session);
        found = true;
    }

    // If not found, add a new SessionObject to the list
     /*
    if (!found) {
        SessionObject obj = new SessionObject(session, paymentID, false);
        Main.sessionObjects.add(obj);
    }
    */
    
}


     private void sendKeepAliveMessage(Session session) {
        String message = sessionMessages.getOrDefault(session, ""); // Get stored message
        if(!session.isOpen()) {
            ScheduledExecutorService scheduler = keepAliveMethods.get(session);
            scheduler.shutdown();  // Stop keep-alive messages
            return;
        }
        System.out.println("KEEP ALIVE");
        if (!message.isEmpty() && session != null && session.isOpen()) {
            String jsonFile = Main.itemsPath + message + ".json";

            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
                String orderId = message;

                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    // Check if paid
                    JsonElement paidElement = jsonObject.get("paid");
                    String paid = paidElement.getAsString();
                    if (paid.equals("true")) {
                        System.out.println("PAID IS EQUAL TO TRUE IN KEEPALIVE METHOD");
                        String cancelID = Tools.getCancelID(message);
                        if (session.isOpen()) {
                            session.getRemote().sendString(cancelID);
                            session.close();
                            return;
                        }
                    }
                }
            } catch (ZipException e) {
                System.out.println("ZipException during keep-alive: " + e.getMessage());
            } catch (ClosedChannelException e) {
                //System.out.println("ClosedChannelException during keep-alive: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException during keep-alive: " + e.getMessage());
            }

            // Send the keep-alive message if the session is still open
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString("keep-alive");
                }
            } catch (IOException e) {
                //System.out.println("IOException while sending keep-alive: " + e.getMessage());
            }
        }
    }
}


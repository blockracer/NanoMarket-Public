package market;


import com.google.gson.JsonObject;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.Message;
import jakarta.mail.internet.*;

public class HttpRequests {

    public static void send(String destination, String source, String amount) {
        // Create an instance of CloseableHttpClient
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // Define the URL
            String url = "https://foo.nanoriver.io/pay";

            // Create the HttpPost request
            HttpPost httpPost = new HttpPost(url);

            // Set request headers
            httpPost.setHeader("login", "");

            // Create the Gson object
            Gson gson = new Gson();

            // Create the JsonObject for the parameters
            JsonObject json = new JsonObject();
            json.addProperty("destination", destination);
            json.addProperty("source", source);
            json.addProperty("amount", amount);

            System.out.println("source: " + source);

            System.out.println("amount: " + amount);

            // Convert JsonObject to JSON string
            String jsonString = gson.toJson(json);

            // Set the request body
            StringEntity stringEntity = new StringEntity(jsonString);
            httpPost.setEntity(stringEntity);

            // Set the content type
            httpPost.setHeader("Content-Type", "application/json");

            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // Get the response entity
                HttpEntity entity = response.getEntity();

                // Print the response
                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity);
                    System.out.println("Response: " + responseBody);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  public static void sendOrderMail(JsonObject jsonObject) {
        // Sender's email ID and password
        final String username = "blockracer@nanoriver.io";
        final String password = "";

        // Recipient's email ID 
        String to = jsonObject.get("email").getAsString();
        String id = jsonObject.get("id").getAsString();
        String cancelID = jsonObject.get("cancelID").getAsString();

        // SMTP server configuration
        String host = "smtp.gmail.com";

        // Setup mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(username));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("You're ready to sell!");

            String htmlContent = String.format(
                "<html><body><p>Hello,</p>" +
                "<p>Create your listing here: <a href=\"https://market.nanoriver.io/listingform.html?id=%s&cancelid=%s\">Create listing</a></p>" +
                "</body></html>",
                id, cancelID
            );

                //"<p>Modify your listing here: <a href=\"http://192.168.1.17:9999/form.html?id=%s&cancelid=%s\">Modify Listing</a></p>" +

            message.setContent(htmlContent, "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


     public static void sendNewListing(JsonObject jsonObject) {
        // Sender's email ID and password
        final String username = "blockracer@nanoriver.io";
        final String password = "";

        // Recipient's email ID 
        String to = jsonObject.get("email").getAsString();
        String id = jsonObject.get("id").getAsString();
        String cancelID = jsonObject.get("cancelID").getAsString();
        String title = jsonObject.get("title").getAsString();

        // SMTP server configuration
        String host = "smtp.gmail.com";

        // Setup mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(username));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(title + " is listed!");

            String htmlContent = String.format(
                "<html><body><p>Hello,</p>" +
                "<p>Your item titled '<b>%s</b>' is up for sale!</p>" +
                "<p>View your listing here: <a href=\"https://market.nanoriver.io/listing/%s\">View Listing</a></p>" +
                "<p>Cancel anytime by clicking this link: <a href=\"http://market.nanoriver.io/cancel/%s/%s\">Cancel Item</a></p>" +
                "<p>Modify your listing here: <a href=\"https://market.nanoriver.io/modifyform.html?id=%s&cancelid=%s\">Modify Listing</a></p>" +
                "</body></html>",
                title, id, id, cancelID, id, cancelID
            );

                //"<p>Modify your listing here: <a href=\"http://192.168.1.17:9999/form.html?id=%s&cancelid=%s\">Modify Listing</a></p>" +

            message.setContent(htmlContent, "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public static void purchaseMail(Item item, Item.Buyer buyer, String title) {
        final String username = "blockracer@nanoriver.io";
        final String password = "";

        String buyerEmail = buyer.getEmail();
        String invoiceID = buyer.getInvoiceID();
        String id = item.getId();
        String sellerEmail = item.getEmail();
        String lockDate = buyer.getLockDate();
        String publicOrderID = buyer.getPublicOrderID();

        // SMTP server configuration
        String host = "smtp.gmail.com";

        // Setup mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(username));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(buyerEmail));

            // Set Subject: header field
            message.setSubject("Thank you for your order! item: " + title);

             // Set the actual message in HTML format
            String htmlContent = String.format(
                "<html><body><p>Thank you for your purchase! Item: %s</p>" +
                "<p>Public Order ID: <b>%s</b></p>" +
                "<p>Here is the email address of the seller: <b>%s</b></p>" +
                "<p>The item ID: <b>%s</b></p>" +
                "<p><a href=\"https://market.nanoriver.io/listing/%s\">Link to item</a></p>" +
                "<p>To approve the purchase, click here(This will unlock the collateral for both parties and the seller will receive the funds for the order): <a href=\"https://market.nanoriver.io/approve/%s/%s\">Approve Purchase</a></p>" +
                "<p>Funds will be permanently locked on %s if not approved </p>" +
                "</body></html>",
                title, publicOrderID, sellerEmail, id, id, id, invoiceID, lockDate
            );

            message.setContent(htmlContent, "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }


    }
  public static void soldMail(Item item, Item.Buyer buyer) {
        final String username = "blockracer@nanoriver.io";
        final String password = "";

        String buyerEmail = buyer.getEmail();
        String invoiceID = buyer.getInvoiceID();
        String id = item.getId();
        String sellerEmail = item.getEmail();
        String quantity = buyer.getQuantity();
        String title = item.getTitle();
        String lockDate = buyer.getLockDate();
        String publicOrderID = buyer.getPublicOrderID();

        // SMTP server configuration
        String host = "smtp.gmail.com";

        // Setup mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(username));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(sellerEmail));

            // Set Subject: header field
            message.setSubject("Your item sold! item: " + title);

             // Set the actual message in HTML format
            String htmlContent = String.format(
                "<html><body><p>Your item sold!!</p>" +
                "<p>Listing: <b>%s</b></p>" +
                "<p>Here is the email address of the buyer: <b>%s</b></p>" +
                "<p>Public Order ID: <b>%s</b></p>" +
                "<p>The item ID: <b>%s</b></p>" +
                "<p><a href=\"https://market.nanoriver.io/listing/%s\">Link to item</a></p>" +
                "<p>Item quantity: <b>%s</b></p>" +
                "<p>Funds will be permanently locked on %s if buyer doesn't approve.</p>" +
                "</body></html>",
                title, buyerEmail, publicOrderID, id, id, quantity, lockDate
            );

            message.setContent(htmlContent, "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }








 }




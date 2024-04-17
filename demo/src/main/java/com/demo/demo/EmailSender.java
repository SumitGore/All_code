package com.demo.demo;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class EmailSender {

    public static void main(String[] args) {

        // Sender's email address
        String from = "goresumit33@gmail.com";

        // Recipient's email address
//        String to = "goresumit41@gmail.com";
        String[] toAddresses = {"goresumit41@gmail.com", "goresumit3376@gmail.com"};


        // Sender's email password (for authentication)
        String password = "vovtyksmuccwhmbi";

        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");


        // Get the Session object
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Add recipients
            for (String to : toAddresses) {
            	// Create a default MimeMessage object
            	MimeMessage message = new MimeMessage(session);

                // Set the sender and recipient addresses
                message.setFrom(new InternetAddress(from));
             
            	System.out.println(to);
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
             // Set the email subject
                message.setSubject("Test Email with Attachment");

                // Create a MimeBodyPart object to represent the email body
                BodyPart messageBodyPart = new MimeBodyPart();

                // Set the text content of the email
                messageBodyPart.setText("This is a test email with an attachment.");

                // Create a MimeBodyPart object to represent the attachment
                MimeBodyPart attachmentPart = new MimeBodyPart();

                // Set the path to the file you want to attach
                String filePath = "D:\\CRF_Values1.xlsx";
                DataSource source = new FileDataSource(filePath);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName("CRF_Values1.xlsx");

                // Create a Multipart object to add the body and attachment parts
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);

                // Set the content of the message to the Multipart object
                message.setContent(multipart);

                // Send the message
                Transport.send(message);

                System.out.println("Email sent successfully to."+to);

            }

            
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

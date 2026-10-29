
package com.example.demo.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Base64;


@Service
public class GmailSendService {

    private static final String APPLICATION_NAME = "GMS";

    public void sendDigest(String account, String accessToken, String html) {

        try {
            
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            Gmail gmail = new Gmail.Builder(transport, GsonFactory.getDefaultInstance(),request -> request
                    .getHeaders().setAuthorization("Bearer " + accessToken))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Properties properties = new Properties();
            Session session = Session.getInstance(properties);
            MimeMessage email = new MimeMessage(session);
            
            email.setFrom(new InternetAddress(account));
            email.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(account));
            email.setSubject("Your Daily Gmail Digest", "UTF-8");
            email.setContent(html, "text/html; charset=UTF-8");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            email.writeTo(outputStream);

            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(outputStream.toByteArray());
            Message message = new Message();

            message.setRaw(new String(encodedEmail.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII));
            gmail.users().messages().send("me", message).execute();

        } catch (Exception e) {
            throw new RuntimeException("Failed to send Gmail digest: " + e.getMessage(), e);
        }
    }
}

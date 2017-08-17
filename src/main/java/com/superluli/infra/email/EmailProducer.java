package com.superluli.infra.email;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

//@Service
public class EmailProducer implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.email.from}")
    private String from;

    private AmazonSimpleEmailServiceClient client;

    
    @PostConstruct
    public void init() throws Exception {

        System.setProperty("aws.accessKeyId", accessKeyId);
        System.setProperty("aws.secretKey", secretKey);

        client = new AmazonSimpleEmailServiceClient();
        client.setRegion(Region.getRegion(Regions.fromName(region)));

        //   sendRPWelcomeEmailToUser("enlian1988@gmail.com");
    }

    private void sendMail(List<String> recipients, String subject, String body) {

        // Construct an object to contain the recipient address.otpEnabled
        Destination destination =
                new Destination().withToAddresses(recipients.toArray(new String[0]));

        // Create a message with the specified subject and body.
        Message message =
                new Message().withSubject(new Content().withData(subject)).withBody(
                        new Body().withHtml(new Content().withData(body)));

        // Assemble the email.
        SendEmailRequest request =
                new SendEmailRequest().withSource(from).withDestination(destination)
                        .withMessage(message);

        try {
            client.sendEmail(request);
        } catch (Exception e) {
            logger.error("Sending email failed", e);
        }
    }

    @Override
    public void sendEmail(String userEmail) {

        sendMail(Arrays.asList(userEmail), "INFRA-TEST", "XXX");
    }
}

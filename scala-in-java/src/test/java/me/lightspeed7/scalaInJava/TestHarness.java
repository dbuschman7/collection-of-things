package me.lightspeed7.scalaInJava;

import org.junit.Test;

public class TestHarness {

    boolean scala = true;

    @Test
    // @Ignore
    public void testSendBean() {

        MailMessage mailMessage = new MailMessage();
        mailMessage.setBody("test");
        mailMessage.setSubject("test email");
        mailMessage.setFromAddress("noreply@transzap.com");
        mailMessage.setToAddress("dbusch@transzap.com");

        try {
            // Scala Dispatch Library
            String baseUrl = "http://localhost:9000";

            new DispatchScala(baseUrl, "admin", "admin").send(mailMessage);

            // Java Async-Http-Client
            new JavaClient(baseUrl, "admin", "admin").sendAsyncHttpClient(mailMessage);

            // Scala Dispatch From Java
            new JavaClient(baseUrl, "admin", "admin").sendDispatchJava(mailMessage);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

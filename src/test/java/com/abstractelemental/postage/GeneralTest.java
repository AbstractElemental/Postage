package com.abstractelemental.postage;

import com.abstractelemental.postage.models.*;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

@Slf4j
public class GeneralTest {

    private static SMTPSettings SETTINGS;

    private static Boolean success;
    private static Boolean failure;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @BeforeClass
    public static void setup() {
        SETTINGS = new SMTPSettings();

        SETTINGS.setHost("localhost");
        SETTINGS.setPort(3025);
        SETTINGS.setUsername("test");
        SETTINGS.setPassword("potato");

        SETTINGS.setBounceEmailAddress("bounce@abstractelemental.com");
        SETTINGS.setExecutorThreadCount(5);
        SETTINGS.setRetryCount(5);
        SETTINGS.setRetryOnFailure(Boolean.TRUE);
        SETTINGS.setSslCheckServerIdentity(Boolean.FALSE);
        SETTINGS.setSslOnConnect(Boolean.FALSE);
        SETTINGS.setStartTLSRequired(Boolean.FALSE);

        SETTINGS.setClassForTemplateLoading(PostOffice.class);
    }

    @Test
    @SneakyThrows(InterruptedException.class)
    public void testEmail() {
        final Consumer<PostageReceipt> successCallback = pr -> {
            log.info("Email sent!");
            log.info(pr.getSuccess().toString());
            log.info(pr.getMessageId());

            success = Boolean.TRUE;
            failure = Boolean.FALSE;
        };

        final Consumer<PostageReceipt> failureCallback = pr -> {
            log.info("Something went wrong!");
            log.info(pr.getSuccess().toString());
            log.info(pr.getMessageId());

            success = Boolean.FALSE;
            failure = Boolean.TRUE;
        };

        try (PostOffice postOffice = new PostOffice(SETTINGS, successCallback, failureCallback)) {
            final Email email = Email.builder()
                    .subject("Hello World")
                    .plainBody("I wish I Wish I was a fish")
                    .freemarkerTemplateFilename("test_template.ftl")
                    .freemarkerView(new LazyEmailModel("Jim"))
                    .recipient(new Contact("jim@jimboson.com", "Jim Jimboson"))
                    .carbonCopy(new Contact("fred@fakerson.xyz"))
                    .blindCarbonCopy(new Contact("potato@salad.com"))
                    .metadata(new HashMap<>())
                    .metadatum("foo", "bar")
                    .from(new Contact("postage-test@abstractelemetal.com", "Abstract Elemental Open Source"))
                    .attachment(Attachment.fromPath("patrick.gif", "Fancy gif!", "src/test/resources/patrick.gif"))
                    .build();

            postOffice.send(email);

            Thread.sleep(1000L);

            assert success.equals(Boolean.TRUE);
            assert failure.equals(Boolean.FALSE);
        }
    }

    @Test
    public void modelTests() {
        Contact contactNoDisplay = new Contact("brandon@abstractelemental.com");
        Contact contactWithDisplay = new Contact("brandon@abstractelemental.com", "Brandon T. Kowalski");
        Contact thirdContact = new Contact("tim@abstractelemental.com", "Tim Parsons");

        assert contactNoDisplay.equals(contactWithDisplay);
        assert contactNoDisplay.compareTo(contactWithDisplay) == 0;
        assert !contactWithDisplay.equals(thirdContact);

    }

}

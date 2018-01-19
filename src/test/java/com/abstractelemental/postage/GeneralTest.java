package com.abstractelemental.postage;

import com.abstractelemental.postage.models.*;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;

import java.util.function.Consumer;

@Slf4j
public class GeneralTest {

	private static final SMTPSettings SETTINGS;

	static {
		SETTINGS = new SMTPSettings();

		SETTINGS.setHost("localhost");
		SETTINGS.setPort(3025);

		SETTINGS.setBounceEmailAddress("bounce@abstractelemental.com");
		SETTINGS.setExecutorThreadCount(5);
		SETTINGS.setRetryCount(5);
		SETTINGS.setRetryOnFailure(Boolean.TRUE);
		SETTINGS.setSslCheckServerIdentity(Boolean.FALSE);
		SETTINGS.setSslOnConnect(Boolean.FALSE);
		SETTINGS.setStartTLSRequired(Boolean.FALSE);

		SETTINGS.setClassForTemplateLoading(PostOffice.class);
	}

    private static Boolean success;
	private static Boolean failure;

	public static void main(final String[] args) {

		final Consumer<PostageReceipt> successCallback = pr -> {
			log.info("Email sent!");
			log.info(pr.getSuccess().toString());
			log.info(pr.getMessageId());
		};

		final Consumer<PostageReceipt> failureCallback = pr -> {
			log.info("Something went wrong!");
			log.info(pr.getSuccess().toString());
			log.info(pr.getMessageId());
		};

		try (PostOffice postOffice = new PostOffice(SETTINGS, successCallback, failureCallback)) {
			final Email email = Email.builder()
					.subject("Hello World")
					.plainBody("I wish I Wish I was a fish")
					.freemarkerTemplateFilename("test_template.ftl")
					.freemarkerView(new LazyEmailModel("Jim"))
					.recipient(new Contact("jim@jimboson.com", "Jim Jimboson"))
					.from(new Contact("postage-test@abstractelemetal.com", "Abstract Elemental Open Source"))
					.attachment(Attachment.builder().name("patrick.gif")
							.filesystemPath("src/test/resources/patrick.gif").build())
					.build();

			postOffice.send(email);
		} catch (Exception e) {
		    log.error("Something went wrong.", e);
        }
	}
}

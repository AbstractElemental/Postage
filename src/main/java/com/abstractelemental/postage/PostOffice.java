package com.abstractelemental.postage;

import com.abstractelemental.postage.models.Attachment;
import com.abstractelemental.postage.models.Contact;
import com.abstractelemental.postage.models.PostageReceipt;
import com.abstractelemental.postage.models.SMTPSettings;
import freemarker.template.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.mail.*;

import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * PostOffice is the central hub of Postage. It takes settings, success and failure callbacks and
 * ultimately sends your emails.
 */

@Slf4j
public class PostOffice implements AutoCloseable {

    private final ExecutorService executor;
    private final SMTPSettings settings;
    private final Configuration freemarkerConfiguration;

    private RetryPolicy retryPolicy;
    private Consumer<PostageReceipt> successCallback;
    private Consumer<PostageReceipt> failureCallback;

    /**
     * PostOffice requires SMTP Settings (obviously). You can optionally pass in two Consumer
     * functions that the Postmaster will call on success and failure respectively. If you need that
     * functionality use the other constructor!
     *
     * @param settings representing your email environment
     */
    public PostOffice(final SMTPSettings settings) {
        this.settings = settings;
        executor = Executors.newFixedThreadPool(settings.getExecutorThreadCount());

        if (settings.isRetryOnFailure()) {
            retryPolicy = new RetryPolicy()
                    .retryOn(EmailException.class)
                    .withDelay(5, TimeUnit.SECONDS)
                    .withMaxRetries(settings.getRetryCount());
        }

        final Version freemarkerVersion = Configuration.getVersion();
        freemarkerConfiguration = new Configuration(freemarkerVersion);
        freemarkerConfiguration.setObjectWrapper(new DefaultObjectWrapperBuilder(freemarkerVersion).build());
        freemarkerConfiguration.loadBuiltInEncodingMap();
        freemarkerConfiguration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        freemarkerConfiguration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        freemarkerConfiguration.setClassForTemplateLoading(
                settings.getClassForTemplateLoading() != null ? settings.getClassForTemplateLoading() : this.getClass(),
                "/");

    }

    /**
     * PostOffice requires SMTP Settings (obviously). You can optionally pass in two Consumer
     * functions that the Postmaster will call on success and failure respectively. If you do not
     * need one or the other pass null in the argument. If you need neither, use the other
     * constructor silly goose!
     *
     * @param settings        representing your email environment
     * @param successCallback function that consumes a PostageReceipt object
     * @param failureCallback function that consumes a PostageReceipt object
     */
    public PostOffice(final SMTPSettings settings, final Consumer<PostageReceipt> successCallback,
                      final Consumer<PostageReceipt> failureCallback) {
        this(settings);
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    /**
     * Send converts the Postage Email to a format that Apache Commons understands. From here the
     * can take multiple paths. If the SMTP settings have retry of failure enabled, this method will
     * attempt to send your email up to the number of times defined in the settings (retry on
     * failure is on by default with 5 retries).
     * <p>
     * If you set an success and / or failure callback when you constructed this Post Office, the
     * respective callback will be executed. Please note when retry on failure is enabled, the
     * failure callback will only be called when the Post Office has exhausted the number of retries
     * allowed.
     *
     * @param email to send
     */
    public void send(final com.abstractelemental.postage.models.Email email) {
        convertToCommons(email).ifPresent(commonsEmail -> executor.submit(() -> {
            log.debug("Sending email: " + email.toString());
            if (retryPolicy != null) {
                sendWithRetry(email, commonsEmail);
            } else {
                sendWithoutRetry(email, commonsEmail);
            }
        }));
    }

    private void sendWithRetry(final com.abstractelemental.postage.models.Email email, final Email commonsEmail) {
        Failsafe.with(retryPolicy).onSuccess(success -> {
            log.debug("Email sent: " + success);
            report(Boolean.TRUE, email, success.toString(), null);
        }).onFailure(e -> log.error("Unable to send email!.", e)).onRetry((c, f, ctx) -> log.warn(
                "Email sending attepmt #{} failed. Retrying...", ctx.getExecutions())).onRetriesExceeded(e -> {
            log.error("Max retries exceeded!");
            report(Boolean.FALSE, email, null, e);
        }).get(commonsEmail::sendMimeMessage);
    }

    private void sendWithoutRetry(final com.abstractelemental.postage.models.Email email, final Email commonsEmail) {
        try {
            final String res = commonsEmail.sendMimeMessage();
            log.debug("Email sent: " + res);
            report(Boolean.TRUE, email, res, null);
        } catch (final EmailException e) {
            log.error("Unable to send email!", e);
            report(Boolean.FALSE, email, null, e);
        }
    }

    private void report(final Boolean success, final com.abstractelemental.postage.models.Email email,
                        final String messageId, final Throwable throwable) {
        final PostageReceipt receipt = new PostageReceipt(success, email, messageId, throwable);

        if (success && successCallback != null) {
            successCallback.accept(receipt);
        } else if (!success && failureCallback != null) {
            failureCallback.accept(receipt);
        }
    }

    private Optional<Email> convertToCommons(final com.abstractelemental.postage.models.Email email) {
        try {
            final Email e;

            if (email.getFreemarkerTemplateFilename().isPresent() && email.getFreemarkerView().isPresent()) {
                e = new HtmlEmail();
            } else if (!email.getAttachments().isEmpty()) {
                e = new MultiPartEmail();
            } else {
                e = new SimpleEmail();
            }

            e.setHostName(settings.getHost());
            e.setSmtpPort(settings.getPort());
            e.setAuthenticator(new DefaultAuthenticator(settings.getUsername(), settings.getPassword()));
            e.setBounceAddress(settings.getBounceEmailAddress());
            e.setStartTLSRequired(settings.isStartTLSRequired());
            e.setSSLCheckServerIdentity(settings.isSslCheckServerIdentity());
            e.setSSLOnConnect(settings.isSslOnConnect());

            final Collection<InternetAddress> recipients = email.getRecipients().parallelStream().map(
                    Contact::toInternetAddress).collect(Collectors.toList());

            final Collection<InternetAddress> carbonCopies = email.getCarbonCopies().parallelStream().map(
                    Contact::toInternetAddress).collect(Collectors.toList());

            final Collection<InternetAddress> blindCarbonCopies = email.getBlindCarbonCopies().parallelStream().map(
                    Contact::toInternetAddress).collect(Collectors.toList());

            if (!recipients.isEmpty()) {
                e.setTo(recipients);
            }

            if (!carbonCopies.isEmpty()) {
                e.setCc(carbonCopies);
            }

            if (!blindCarbonCopies.isEmpty()) {
                e.setBcc(blindCarbonCopies);
            }

            e.setSubject(email.getSubject());
            e.setMsg(email.getPlainBody());

            if (e instanceof HtmlEmail) {
                final Template template = freemarkerConfiguration.getTemplate(
                        email.getFreemarkerTemplateFilename().get());

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final Writer out = new OutputStreamWriter(baos);
                template.process(email.getFreemarkerView().get(), out);
                final String html = baos.toString(StandardCharsets.UTF_8.name());
                ((HtmlEmail) e).setHtmlMsg(html);
            }

            if (e instanceof MultiPartEmail && !email.getAttachments().isEmpty()) {
                email.getAttachments().parallelStream().map(Attachment::toEmailAttachment).filter(Optional::isPresent).map(
                        Optional::get).forEach(a -> PostOffice.attach(a, e));
            }

            e.buildMimeMessage();

            return Optional.of(e);
        } catch (final TemplateException | IOException | EmailException e) {
            log.error("Unable to convert to Commons Email", e);
            return Optional.empty();
        }
    }

    @SneakyThrows(EmailException.class)
    private static void attach(final EmailAttachment a, final Email e) {
        ((MultiPartEmail) e).attach(a);
    }

    @Override
    public void close() {
        if (executor != null && !executor.isTerminated()) {
            executor.shutdown();
        }
    }

}

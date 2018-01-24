package com.abstractelemental.postage.models;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.mail.EmailAttachment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Attachment defines an attachment for an email. Name and one of the following
 * fields are required: filesystem path or url.
 */

@Getter
public class Attachment {

    private final String name;
    private final String description;
    private final String path;
    private final String url;
    private final byte[] bytes;

    /**
     * Build an attachement with something on the filesystem.
     */
    public static Attachment fromPath(final String name, final String description, String path) {
        return new Attachment(name, description, path, null, null);
    }

    /**
     * Build an attachment accessed via a URL.
     */
    public static Attachment fromURL(final String name, final String description, String url) {
        return new Attachment(name, description, null, url, null);
    }

    /**
     * Build an attachment with a byte array.
     */
    public static Attachment fromBytes(final String name, final String description, byte[] bytes) {
        return new Attachment(name, description, null, null, bytes);
    }

    private Attachment(final String name, final String description, final String path, final String url, final byte[] bytes) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.url = url;
        this.bytes = bytes;
    }

    /**
     * Performs the necessary steps to translate a Postage {@link Attachment}
     * into a Commons {@link EmailAttachment}
     */
    @SneakyThrows({MalformedURLException.class, IOException.class})
    public static Optional<EmailAttachment> toEmailAttachment(final Attachment a) {

        if (a.getName() == null || (a.getPath() == null && a.getUrl() == null)) {
            return Optional.empty();
        }

        final EmailAttachment attachment = new EmailAttachment();
        attachment.setName(a.getName());
        attachment.setDescription(a.getDescription());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);

        if (a.getPath() != null) {
            attachment.setPath(a.getPath());
        } else if (a.getUrl() != null) {
            attachment.setURL(new URL(a.getUrl()));
        } else {
            attachment.setPath(Files.createTempFile(a.getName(), "")
                    .toFile().getPath());
        }

        return Optional.of(attachment);
    }

}

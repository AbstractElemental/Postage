package com.abstractelemental.postage.models;

import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.mail.EmailAttachment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Attachment defines an attachment for an email. Name and one of the following
 * fields are required: filesystem path or url.
 */

@Getter
@Builder
@ToString
public class Attachment {

	private final String name;
	private final String description;

	private final String filesystemPath;
	private final String url;

	@SneakyThrows(MalformedURLException.class)
	public static Optional<EmailAttachment> toEmailAttachment(final Attachment a) {

		if (a.getName() == null || (a.getFilesystemPath() == null && a.getUrl() == null)) {
			return Optional.empty();
		}

		final EmailAttachment attachment = new EmailAttachment();
		attachment.setName(a.getName());
		attachment.setDescription(a.getDescription());
		attachment.setDisposition(EmailAttachment.ATTACHMENT);

		if (a.getFilesystemPath() != null) {
			attachment.setPath(a.getFilesystemPath());
		} else {
			attachment.setURL(new URL(a.getUrl()));
		}

		return Optional.of(attachment);
	}

}

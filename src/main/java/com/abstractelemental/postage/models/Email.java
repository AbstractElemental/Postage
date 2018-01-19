package com.abstractelemental.postage.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Email defines a unit that the Post Office can send. Both freemarkerTemplateFilename and
 * freemarkerView must be defined in order to send an HTML email. If either or both are null, only a
 * plain text email will be sent.
 */

@Builder
@Getter
@ToString
public class Email {

    @Singular
    private final Set<Contact> recipients;

    @Singular
    private final Set<Contact> carbonCopies;

    @Singular
    private final Set<Contact> blindCarbonCopies;

    @NotNull
    private final Contact from;

    private final String subject;

    private final String plainBody;

    private final String freemarkerTemplateFilename;

    private final Object freemarkerView;

    /**
     * This is not used what-so-ever in the process of sending an email.
     * This is simply a Key, Value store to use with the rest of your system when
     * handling Postage Receipts.
     */
    @Singular("metadatum")
    private final Map<String, Object> metadata;

    @Singular
    private final List<Attachment> attachments;

    public Optional<String> getFreemarkerTemplateFilename() {
        return Optional.ofNullable(freemarkerTemplateFilename);
    }

    public Optional<Object> getFreemarkerView() {
        return Optional.ofNullable(freemarkerView);
    }

}

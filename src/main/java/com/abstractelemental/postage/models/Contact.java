package com.abstractelemental.postage.models;

import lombok.*;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * Contact represents a recipient (to, cc, or bcc) or a sender.
 */

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"displayName"})
public class Contact implements Comparable<Contact> {

    private String email;
    private String displayName;


    /**
     * Contact with no display name
     *
     * @param email address of contact.
     */
    public Contact(final String email) {
        this(email, null);
    }

    /**
     * Contact with display name
     *
     * @param email       address of contact.
     * @param displayName of contact.
     */
    public Contact(final String email, final String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    @Override
    public int compareTo(final Contact o) {
        return email.compareToIgnoreCase(o.getEmail());
    }

    // Every JVM I've ever used has UTF-8 Encoding...
    @SneakyThrows(UnsupportedEncodingException.class)
    public static InternetAddress toInternetAddress(final Contact c) {
        return new InternetAddress(c.getEmail(), c.getDisplayName());
    }

}

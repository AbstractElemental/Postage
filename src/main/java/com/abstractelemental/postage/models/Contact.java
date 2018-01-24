package com.abstractelemental.postage.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

import static org.joor.Reflect.on;

/**
 * Contact represents a recipient (to, cc, or bcc) or a sender.
 */

@Getter
public class Contact implements Comparable<Contact> {

    private String email;
    private String displayName;

    public Contact(final String email) {
        this(email, null);
    }

    public Contact(final String email, final String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    public static InternetAddress toInternetAddress(final Contact c) {
        InternetAddress address = new InternetAddress();
        address.setAddress(c.getEmail());
        on(address).set("personal", c.getDisplayName());
        return address;
    }

    @Override
    public int compareTo(final Contact o) {
        return email.compareToIgnoreCase(o.getEmail());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Contact &&
                ((Contact) o).getEmail().equalsIgnoreCase(this.getEmail());
    }

    @Override
    public int hashCode() {
        return this.getEmail().hashCode();
    }

}

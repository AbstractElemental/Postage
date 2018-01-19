package com.abstractelemental.postage.models;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * SMTPSettings defines all of the parameters for the PostOffice Object. Every
 * field in this POJO is required (however there are several defaults in the
 * second half of the fields).
 */

@Getter
@Setter
public class SMTPSettings implements Serializable {

	private static final long serialVersionUID = 4806646436610575605L;

	@Valid
	@NotNull
	private String host;

	@Valid
	private int port;

	@Valid
	@NotNull
	private String username;

	@Valid
	@NotNull
	private String password;

	@Valid
	@NotNull
	private String bounceEmailAddress;

	@Valid
	@NotNull
	private Class<?> classForTemplateLoading;

	@Valid
	private boolean startTLSRequired = Boolean.FALSE;

	@Valid
	private boolean sslCheckServerIdentity = Boolean.FALSE;

	@Valid
	private boolean sslOnConnect = Boolean.FALSE;

	@Valid
	@Size(min = 1)
	private int executorThreadCount = 1;

	@Valid
	private boolean retryOnFailure = Boolean.TRUE;

	@Valid
	@Size(min = 1)
	private int retryCount = 5;

}

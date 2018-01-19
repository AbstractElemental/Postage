package com.abstractelemental.postage.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostageReceipt {

	private final Boolean success;
	private final Email email;

	private final String messageId;
	private final Throwable throwable;

}

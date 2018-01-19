package com.abstractelemental.postage;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class LazyEmailModel implements Serializable {

	private static final long serialVersionUID = -2218233289382469254L;

	private String name;

}

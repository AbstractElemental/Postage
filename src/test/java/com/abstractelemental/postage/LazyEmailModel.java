package com.abstractelemental.postage;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class LazyEmailModel implements Serializable {

    private static final long serialVersionUID = -2218233289382469254L;

    private String name;

}

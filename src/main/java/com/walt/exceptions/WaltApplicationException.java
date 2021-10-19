package com.walt.exceptions;

public class WaltApplicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public WaltApplicationException() {
		super();
	}

	public WaltApplicationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public WaltApplicationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public WaltApplicationException(String arg0) {
		super(arg0);
	}

	public WaltApplicationException(Throwable arg0) {
		super(arg0);
	}

}

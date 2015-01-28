package org.walkmod.exceptions;

@SuppressWarnings("serial")
public class InvalidConfigurationException extends Exception {

	public InvalidConfigurationException(Throwable cause) {
		super(cause);
		if (cause != null) {
			this.setStackTrace(cause.getStackTrace());
		}
	}
}

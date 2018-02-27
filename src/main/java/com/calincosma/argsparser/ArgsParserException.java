package com.calincosma.argsparser;

public class ArgsParserException extends RuntimeException {
	
	public ArgsParserException() {
		super();
	}
	
	public ArgsParserException(String message) {
		super(message);
	}
	
	public ArgsParserException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ArgsParserException(Throwable cause) {
		super(cause);
	}
	
}

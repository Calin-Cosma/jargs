/*
 * Copyright (c) 2018  Calin Cosma
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.calincosma.argsparser;

/**
 * ArgsParserException is thrown when the parsing of arguments fails for whatever reason.
 */
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

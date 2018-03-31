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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Arg is a field level annotation telling {@link Parser} that this field can be filled with values passed from the command line.
 * The value is the name of the switch. For example, if your command is <strong>java MyCopy -source /tmp/source.folder -dest /tmp/dest.folder</strong>, "-source" and "-dest"
 * would be the values.
 *
 * Mandatory fields must have required set to true.
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.FIELD)
public @interface Arg {
	
	String value();
	
	boolean required() default false;
}

package com.calincosma.argsparser;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.FIELD)
public @interface Arg {
	
	String value() default "";
	
	boolean required() default false;
	
	String delimiter() default ",";
	
	Class type() default String.class;
}

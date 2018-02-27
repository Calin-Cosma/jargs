package com.calincosma.argsparser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Parser {
	
	//	private static Parser instance = new Parser();
	
	public static Parser getInstance() {
		return new Parser();
	}
	
	private Map<String, Field> options = new HashMap<String, Field>();
	private ArrayList<Field> params = new ArrayList<Field>();
	private Set<Field> requiredFields = new HashSet<Field>();
	private Set<Field> treatedFields = new HashSet<Field>();
	private Field currentField = null;
	private Object currentValue = null;
	
	private Parser() {
	}
	
	
	public <T extends Object> T parse(String[] args, Class<T> clazz) throws ArgsParserException {
		try {
			T t = clazz.newInstance();
			
			for (Field field : clazz.getDeclaredFields()) {
				Arg annotation = field.getAnnotation(Arg.class);
				if (annotation != null) {
					if (annotation.value() != null && annotation.value().length() > 0) {
						options.put(annotation.value(), field);
					}
					
					if (annotation.required()) {
						requiredFields.add(field);
					}
					
					if (annotation.position() > 0) {
						params.add(annotation.position() - 1, field);
					}
				}
			}
			
			for (String arg : args) {
				Field newField = options.get(arg);
				
				if (newField != null) {
					/* it's a switch */
					newField.setAccessible(true);
					
					if (currentField != null && currentValue == null) {
						Class type = currentField.getType();
						if (Boolean.class == type || Boolean.TYPE == type) {
							setFieldValue(t, Boolean.TRUE, "Couldn't set value for " + arg);
						} else {
							throw new ArgsParserException("Missing value for " + arg);
						}
					}
					
					currentField = newField;
					currentValue = null;
				} else {
					/* it's a value/param */
					Class type = currentField.getType();
					
					if (String.class == type) {
						setFieldValue(t, arg, "Couldn't set value for " + arg);
					} else {
						Method method = null;
						try {
							method = type.getMethod("valueOf", String.class);
						} catch (Exception e) {
							// e.printStackTrace();
						}
						if (method != null && method.getReturnType() == type && Modifier.isStatic(method.getModifiers())) {
							setFieldValue(t, method.invoke(null, arg), "Argument for " + arg + " must be of type " + type.getName().replaceAll("java.lang.", ""));
						} else if (Integer.class == type || Integer.TYPE == type) {
							setFieldValue(t, Integer.valueOf(arg), "Argument for " + arg + " must be of type int");
						} else if (Long.class == type || Long.TYPE == type) {
							setFieldValue(t, Long.valueOf(arg), "Argument for " + arg + " must be of type long");
						} else if (Double.class == type || Double.TYPE == type) {
							setFieldValue(t, Double.valueOf(arg), "Argument for " + arg + " must be of type double");
						} else if (Float.class == type || Float.TYPE == type) {
							setFieldValue(t, Float.valueOf(arg), "Argument for " + arg + " must be of type float");
						} else if (Short.class == type || Short.TYPE == type) {
							setFieldValue(t, Short.valueOf(arg), "Argument for " + arg + " must be of type short");
						} else if (Boolean.class == type || Boolean.TYPE == type) {
							setFieldValue(t, Boolean.valueOf(arg), "Argument for " + arg + " must be of type " + "Integer");
						}
					}
				}
				
				//				if (arg.startsWith("--")) {
				//					optionName = arg.substring(2, arg.length());
				//				} else if (arg.startsWith("-")) {
				//					optionName = arg.substring(1, arg.length());
				//				}
				//
				//				if (optionName != null) {
				//					if (currentField != null) {
				//
				//					}
				//
				//					currentField = options.get(optionName);
				//					if (currentField == null) {
				//						throw new ArgsParserException("Option " + optionName + " is not defined.");
				//					}
				////					currentField.setAccessible(true);
				//				} else {
				//
				//				}
				
			}
			
			
			if (requiredFields.size() > 0) {
				String requiredFieldsNames = requiredFields.stream()
				                                           .map(field -> field.getAnnotation(Arg.class))
				                                           .filter(a -> a.value() != null)
				                                           .map(a -> a.value())
				                                           .collect(Collectors.joining(","));
				
				String requiredFieldsPositions = requiredFields.stream()
				                                               .map(field -> field.getAnnotation(Arg.class))
				                                               .filter(a -> a.position() > 0)
				                                               .map(a -> a.position())
				                                               .sorted()
				                                               .map(i -> String.valueOf(i))
				                                               .collect(Collectors.joining(","));
				
				String message = "Missing values for" +
						(requiredFieldsNames != null && requiredFieldsNames.length() > 0 ? (" required arguments " + requiredFieldsNames) : "") +
						(requiredFieldsPositions != null && requiredFieldsPositions.length() > 0 ? (requiredFieldsNames != null && requiredFieldsNames.length() > 0 ? " and" :
								"") + " for required positions: " + requiredFieldsPositions : "");
				
				throw new ArgsParserException(message);
			}
			
			return t;
		} catch (Exception e) {
			throw new ArgsParserException(e);
		}
	}
	
	
	private <Type, Value extends Object> void setFieldValue(Type t, Value v, String errorMessage) {
		try {
			currentField.set(t, v);
			requiredFields.remove(currentField);
			currentField = null;
		} catch (IllegalAccessException e) {
			throw new ArgsParserException(errorMessage);
		}
	}
}



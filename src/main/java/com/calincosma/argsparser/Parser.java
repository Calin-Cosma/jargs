package com.calincosma.argsparser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Parser {
	
	public static Parser getInstance() {
		return new Parser();
	}
	
	private Map<String, Field> options = new HashMap<String, Field>();
	private ArrayList<Field> params = new ArrayList<Field>();
	private Set<Field> requiredFields = new HashSet<Field>();
	private Set<Field> treatedFields = new HashSet<Field>();
	private Field currentField = null;
//	private Object currentValue = null;
	private int fieldIndex = 1;
//	private boolean currentValueSet = false;
	
	private Parser() {
	}
	
	
	public <T extends Object> T parse(String[] args, Class<T> clazz) throws ArgsParserException {
		try {
			T t = clazz.newInstance();
			
			/* go through all Args annotations, build helper collections */
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
					/* it's a switch,  */
					newField.setAccessible(true);
					
					/* if previous field's value wasn't set and it's boolean, set it to true (no param/value necessary). Otherwise throw an exception */
//					if (currentField != null && currentValue == null) {
					if (currentField != null && currentField.get(t) == null) {
						Class type = currentField.getType();
						if (Boolean.class == type || Boolean.TYPE == type) {
							setFieldValue(t, Boolean.TRUE, "Couldn't set value for " + arg);
						} else {
							throw new ArgsParserException("Missing value for " + arg);
						}
					}
					
					currentField = newField;
//					currentValue = null;
				} else {
					if (currentField == null) {
						/* treat positional params */
//						if (fieldIndex > )
					}
					
					/* it's a value/param */
					Class type = currentField.getType();
					
					if (String.class == type) {
						/* strings get set immediatelly, as they are the easiest param to set */
						setFieldValue(t, arg, "Couldn't set value for " + arg);
					} else if (type.isAssignableFrom(Collection.class)) {
						/* treating collections */
						if (currentField.get(t) == null) {
							/* the collection hasn't yet been instantiated. Attempting to do so here. */
							if (type.isInterface()) {
								/* if the type is an interface, use a common implementation */
								if (type.isAssignableFrom(List.class)) {
									setFieldValue(t, new ArrayList<>(), "Couldn't set value for " + arg);
//									currentValue = new ArrayList<>();
								} else if (type.isAssignableFrom(Set.class)) {
									setFieldValue(t, new HashSet<>(), "Couldn't set value for " + arg);
//									currentValue = new HashSet<>();
								} else if (type.isAssignableFrom(Queue.class)) {
									setFieldValue(t, new LinkedList<>(), "Couldn't set value for " + arg);
//									currentValue = new LinkedList<>();
								}
							} else {
								/* if the type is a class, instantiate it */
								setFieldValue(t, type.newInstance(), "Couldn't set value for " + arg);
//								currentValue = type.newInstance();
							}
						} else {
							/* collection has already been instantiated, just add value to collection */
//							((Collection<?>)currentValue).add()
						
						}
						
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
			treatedFields.add(currentField);
//			currentField = null;
		} catch (IllegalAccessException e) {
			throw new ArgsParserException(errorMessage);
		}
	}
}



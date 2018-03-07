package com.calincosma.argsparser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
	
	private Parser() {
	}
	
	
	public <ARGS extends Object> ARGS parse(String[] argsArray, Class<ARGS> clazz) throws ArgsParserException {
		try {
			ARGS args = clazz.newInstance();
			
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
			
			for (String arg : argsArray) {
				Field newField = options.get(arg);
				
				if (newField != null) {
					/* it's a switch,  */
					newField.setAccessible(true);
					
					/* if previous field's value wasn't set and it's boolean, set it to true (no param/value necessary). Otherwise throw an exception */
					if (currentField != null && currentField.get(args) == null) {
						Class type = currentField.getType();
						if (Boolean.class == type || Boolean.TYPE == type) {
							setFieldValue(args, currentField, Boolean.TRUE, "Couldn't set value for " + arg);
						} else {
							throw new ArgsParserException("Missing value for " + arg);
						}
					}
					
					currentField = newField;
				} else {
					/* it's a value */
					if (currentField == null) {
						/* treat positional params */
//						if (fieldIndex > )
					}
					
					/* it's a value/param */
					Class fieldType = currentField.getType();
					
//					if (String.class == fieldType) {
//						/* strings get set immediatelly, as they are the easiest param to set */
//						setFieldValue(args, currentField, arg, "Couldn't set value for " + arg);
//					} else
					
					if (fieldType.isAssignableFrom(Collection.class)) {
						/* treating collections */
						if (currentField.get(args) == null) {
							/* the collection hasn't yet been instantiated. Attempting to do so here. */
							if (fieldType.isInterface()) {
								/* if the type is an interface, use a common implementation */
								if (fieldType.isAssignableFrom(List.class)) {
									setFieldValue(args, currentField, new ArrayList<>(), "Couldn't set value for " + arg);
								} else if (fieldType.isAssignableFrom(Set.class)) {
									setFieldValue(args, currentField, new HashSet<>(), "Couldn't set value for " + arg);
								} else if (fieldType.isAssignableFrom(Queue.class)) {
									setFieldValue(args, currentField, new LinkedList<>(), "Couldn't set value for " + arg);
								}
							} else {
								/* if the type is a class, instantiate it */
								setFieldValue(args, currentField, fieldType.newInstance(), "Couldn't set value for " + arg);
							}
						} else {
							/* collection has already been instantiated, just add value to collection */
							//							((Collection<?>)currentValue).add()
							
						}
					} else if (fieldType.isArray()) {
						Class arrayType = fieldType.getComponentType();
						/* treating arrays */
						if (currentField.get(args) == null) {
							/* the array hasn't yet been instantiated. Attempting to do so here. */
							java.lang.reflect.Array.newInstance(arrayType, 1);
						} else {
							/* collection has already been instantiated, just add value to collection */
							//							((Collection<?>)currentValue).add()
							
						}
					} else {
						setFieldValue(args, currentField, getValue(args, arg, fieldType));
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
			
			return args;
		} catch (Exception e) {
			throw new ArgsParserException(e);
		}
	}
	
	
	
	private <ARGS extends Object, VALUE> VALUE getValue(ARGS args, String arg, Class<VALUE> fieldType) throws IllegalAccessException, InvocationTargetException {
		if (String.class == fieldType) {
						/* strings get set immediatelly, as they are the easiest param to set */
//			setFieldValue(args, currentField, arg, "Couldn't set value for " + arg);
			return (VALUE)arg;
		}
		
		Method method = null;
		try {
			method = fieldType.getMethod("valueOf", String.class);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		if (method != null && method.getReturnType() == fieldType && Modifier.isStatic(method.getModifiers())) {
//			setFieldValue(args, currentField, method.invoke(null, arg), "Argument for " + arg + " must be of type " + fieldType.getName().replaceAll("java.lang.", ""));
			return (VALUE)fieldType.cast(method.invoke(null, arg));
		} else if (Integer.class == fieldType || Integer.TYPE == fieldType) {
//			setFieldValue(args, currentField, Integer.valueOf(arg), "Argument for " + arg + " must be of type int");
			return (VALUE)Integer.valueOf(arg);
		} else if (Long.class == fieldType || Long.TYPE == fieldType) {
//			setFieldValue(args, currentField, Long.valueOf(arg), "Argument for " + arg + " must be of type long");
			return (VALUE)Long.valueOf(arg);
		} else if (Double.class == fieldType || Double.TYPE == fieldType) {
//			setFieldValue(args, currentField, Double.valueOf(arg), "Argument for " + arg + " must be of type double");
			return (VALUE)Double.valueOf(arg);
		} else if (Float.class == fieldType || Float.TYPE == fieldType) {
//			setFieldValue(args, currentField, Float.valueOf(arg), "Argument for " + arg + " must be of type float");
			return (VALUE)Float.valueOf(arg);
		} else if (Short.class == fieldType || Short.TYPE == fieldType) {
//			setFieldValue(args, currentField, Short.valueOf(arg), "Argument for " + arg + " must be of type short");
			return (VALUE)Short.valueOf(arg);
		} else if (Boolean.class == fieldType || Boolean.TYPE == fieldType) {
//			setFieldValue(args, currentField, Boolean.valueOf(arg), "Argument for " + arg + " must be of type " + "Integer");
			return (VALUE)Boolean.valueOf(arg);
		}
		return null;
	}
	
	
	private <ARGS, Value extends Object> void setFieldValue(ARGS args, Field field, Value value) {
		setFieldValue(args, field, value, "Couldn't set value of field: " + field.getName() + ", value: " + value.toString());
	}
	
	
	private <ARGS, Value extends Object> void setFieldValue(ARGS args, Field field, Value value, String errorMessage) {
		try {
			field.set(args, value);
			requiredFields.remove(field);
			treatedFields.add(field);
		} catch (IllegalAccessException e) {
			throw new ArgsParserException(errorMessage);
		}
	}
	
	
	private <ARGS, Value extends Object> void addFieldValueToCollection(ARGS args, Field field, Value value, String errorMessage) {
		try {
			((Collection)field.get(args)).add(getValue(args));

			Object newObject = clazz.newInstance();

			currentField.set(args, value);
			requiredFields.remove(currentField);
			treatedFields.add(currentField);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new ArgsParserException(errorMessage);
		}
	}
}



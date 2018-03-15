package com.calincosma.argsparser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	
	
	private Parser() {
	}
	
	
	public <ARGS> ARGS parse(String[] argsArray, Class<ARGS> clazz) throws ArgsParserException {
		try {
			ARGS args = clazz.newInstance();
			
			Map<String, Field> options = new HashMap<String, Field>();
			ArrayList<Field> params = new ArrayList<Field>();
			Set<Field> requiredFields = new HashSet<Field>();
			Set<Field> treatedFields = new HashSet<Field>();
			Field currentField = null;
			Map<Field, ArrayList> arrayValues = new HashMap<Field, ArrayList>();
			
			
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
			
			
			LinkedList<String> argsList = new LinkedList<>();
			Collections.addAll(argsList, argsArray);
			String currentArg = null;
			List<String> currentValues = new ArrayList<>();
			
			while (!argsList.isEmpty()) {
				if (options.get(argsList.getFirst()) != null) {
					currentField = options.get((argsList.pop()));
					currentField.setAccessible(true);
				}
				
				while (!argsList.isEmpty() && options.get(argsList.getFirst()) == null) {
					currentValues.add(argsList.pop());
				}
				
				setValues(args, currentField, currentValues);
				
				requiredFields.remove(currentField);
				treatedFields.add(currentField);
				
				currentField = null;
				currentValues.clear();
			}
			
//			for (String arg : argsArray) {
//				Field newField = options.get(arg);
//
//				if (newField != null) {
//					/* it's a switch,  */
//					newField.setAccessible(true);
//
//					/* if previous field's value wasn't set and it's boolean, set it to true (no param/value necessary). Otherwise throw an exception */
//					if (currentField != null && currentField.get(args) == null && arrayValues.get(currentField) == null) {
//						Class type = currentField.getType();
//						if (Boolean.class == type || Boolean.TYPE == type) {
//							setFieldValue(args, currentField, Boolean.TRUE, requiredFields, treatedFields);
//						} else {
//							throw new ArgsParserException("Missing value for " + arg);
//						}
//					}
//
//					currentField = newField;
//				} else {
//					/* it's a value */
//					if (currentField == null) {
//						/* treat positional params */
////						if (fieldIndex > )
//					}
//
//					/* it's a value/param */
//					Class fieldType = currentField.getType();
//
//					if (Collection.class.isAssignableFrom(fieldType)) {
//						/* treating collections */
//						if (currentField.get(args) == null) {
//							/* the collection hasn't yet been instantiated. Attempting to do so here. */
//							if (fieldType.isInterface()) {
//								/* if the type is an interface, use a common implementation */
//								if (List.class.isAssignableFrom(fieldType)) {
//									setFieldValue(args, currentField, new ArrayList<>(), requiredFields, treatedFields);
//								} else if (Set.class.isAssignableFrom(fieldType)) {
//									setFieldValue(args, currentField, new HashSet<>(), requiredFields, treatedFields);
//								} else if (Queue.class.isAssignableFrom(fieldType)) {
//									setFieldValue(args, currentField, new LinkedList<>(), requiredFields, treatedFields);
//								}
//							} else {
//								/* if the type is a class, instantiate it */
//								setFieldValue(args, currentField, fieldType.newInstance(), requiredFields, treatedFields);
//							}
//						}
//
//						/* collection has already been instantiated, just add value to collection */
//						ParameterizedType parameterizedType = (ParameterizedType)currentField.getGenericType();
//						Class<?> collectionType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
//						addFieldValueToCollection(args, currentField, arg, collectionType, currentField, requiredFields, treatedFields);
//
//					} else if (fieldType.isArray()) {
//						Class arrayType = fieldType.getComponentType();
//						/* treating arrays
//						* array values are kept in ArrayLists for convenience and only set as values on the fields at the end
//						*/
//						if (arrayValues.get(currentField) == null) {
//							/* the ArrayList hasn't yet been instantiated. Attempting to do so here. */
//							//setFieldValue(args, currentField, buildArrayList(arrayType), requiredFields, treatedFields);
//							arrayValues.put(currentField, buildArrayList(arrayType));
//							Arrays.copyOf(currentField.get(args), 2);
//						}
//						/* collection has already been instantiated, just add value to collection */
//						arrayValues.get(currentField).add(getValue(arg, arrayType));
//					} else {
//						setFieldValue(args, currentField, getValue(arg, fieldType), requiredFields, treatedFields);
//					}
//				}
//			}
			
			
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
			
			
			for (Field field : arrayValues.keySet()) {
				ArrayList arrayList = arrayValues.get(field);
				Class fieldType = field.getType();
				Class arrayType = fieldType.getComponentType();
				setValueAsArray(field, args, arrayList, arrayType);
			}
			
			
			return args;
		} catch (Exception e) {
			throw new ArgsParserException(e);
		}
	}
	
	
	
	private <ARGS, Value extends Object> void setFieldValue(ARGS args, Field field, Value value) {
		try {
			field.set(args, value);
		} catch (IllegalAccessException e) {
			throw new ArgsParserException("Couldn't set value of field: " + field.getName() + ", value: " + value.toString());
		}
	}
	
	
	
	private <ARGS, VALUE> void addFieldValueToCollection(ARGS args, Field field, String arg, Class<VALUE> clazz, Field currentField) {
		try {
			((Collection)field.get(args)).add(getValue(arg, clazz));
//			requiredFields.remove(currentField);
//			treatedFields.add(currentField);
		} catch (IllegalAccessException e) {
			throw new ArgsParserException("Couldn't add value to field of type collection: " + field.getName() + ", value: " + arg);
		}
	}
	
	
	
//	private <ARGS, VALUE> void addFieldValueToArray(ARGS args, Field field, String arg, Class<VALUE> clazz) {
//		try {
//			((Collection)field.get(args)).add(getValue(arg, clazz));
//			requiredFields.remove(currentField);
//			treatedFields.add(currentField);
//		} catch (IllegalAccessException | InvocationTargetException e) {
//			throw new ArgsParserException("Couldn't add value to field of type array: " + field.getName() + ", value: " + arg);
//		}
//	}
	
	
	
	private <ARGS extends Object, VALUE> VALUE getValue(String arg, Class<VALUE> fieldType) {
		try {
			if (String.class == fieldType) {
				/* strings get set immediatelly, as they are the easiest param to set */
				return (VALUE)arg;
			}
			
			Method method = null;
			try {
				method = fieldType.getMethod("valueOf", String.class);
			} catch (Exception e) {
			}
			if (method != null && method.getReturnType() == fieldType && Modifier.isStatic(method.getModifiers())) {
				return (VALUE)fieldType.cast(method.invoke(null, arg));
			} else if (Integer.class == fieldType || Integer.TYPE == fieldType) {
				return (VALUE)Integer.valueOf(arg);
			} else if (Long.class == fieldType || Long.TYPE == fieldType) {
				return (VALUE)Long.valueOf(arg);
			} else if (Double.class == fieldType || Double.TYPE == fieldType) {
				return (VALUE)Double.valueOf(arg);
			} else if (Float.class == fieldType || Float.TYPE == fieldType) {
				return (VALUE)Float.valueOf(arg);
			} else if (Short.class == fieldType || Short.TYPE == fieldType) {
				return (VALUE)Short.valueOf(arg);
			} else if (Boolean.class == fieldType || Boolean.TYPE == fieldType) {
				return (VALUE)Boolean.valueOf(arg);
			}
			return null;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ArgsParserException(e);
		}
	}
	
	
	public <T> ArrayList<T> buildArrayList(Class<T> clazz) {
		return new ArrayList<T>();
	}
	
	public <T, ARGS extends Object> void setValueAsArray(Field field, ARGS args, ArrayList arrayList, Class<T> arrayType) throws IllegalAccessException {
		field.set(args, arrayList.toArray((T[])Array.newInstance(arrayType, 0)));
	}
	
	
	
	public <ARGS> void setValues(ARGS args, Field field, List<String> values) throws IllegalAccessException, InstantiationException {
//		if (field == null) {
//						/* treat positional params */
//			//						if (fieldIndex > )
//		}
					
					/* it's a value/param */
		Class fieldType = field.getType();
		
		if (Collection.class.isAssignableFrom(fieldType)) {
			/* treating collections */
			
			/* the collection hasn't yet been instantiated. Attempting to do so here. */
			if (fieldType.isInterface()) {
				/* if the type is an interface, use a common implementation */
				if (List.class.isAssignableFrom(fieldType)) {
					setFieldValue(args, field, new ArrayList<>());
				} else if (Set.class.isAssignableFrom(fieldType)) {
					setFieldValue(args, field, new HashSet<>());
				} else if (Queue.class.isAssignableFrom(fieldType)) {
					setFieldValue(args, field, new LinkedList<>());
				}
			} else {
				/* if the type is a class, instantiate it */
				setFieldValue(args, field, fieldType.newInstance());
			}
			
						
			/* collection has already been instantiated, just add values to collection */
			ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
			Class<?> collectionType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
			
			
			for (String value : values) {
				((Collection)field.get(args)).add(getValue(value, collectionType));
			}
//			values.stream().map(v -> getValue(v, collectionType)).collect(Collectors.toList());
			
			
//			addFieldValueToCollection(args, field, arg, collectionType, currentField);
			
		} else if (fieldType.isArray()) {
			Class arrayType = fieldType.getComponentType();
			
			createArray(args, field, values, arrayType);
			
//			/* treating arrays
//			* array values are kept in ArrayLists for convenience and only set as values on the fields at the end
//			*/
//			if (arrayValues.get(field) == null) {
//				/* the ArrayList hasn't yet been instantiated. Attempting to do so here. */
//				//setFieldValue(args, currentField, buildArrayList(arrayType), requiredFields, treatedFields);
//				arrayValues.put(field, buildArrayList(arrayType));
//				Arrays.copyOf(field.get(args), 2);
//			}
//			/* collection has already been instantiated, just add value to collection */
//			arrayValues.get(field).add(getValue(arg, arrayType));
		} else if (values.size() == 1) {
			setFieldValue(args, field, getValue(values.get(0), fieldType));
		}
	}
	
	
	private <ARGS, ArrayType> void createArray(ARGS args, Field field, List<String> values, Class<ArrayType> arrayTypeClass) throws IllegalAccessException {
//		field.set(args, Array.newInstance(arrayTypeClass, values.size()));
//		int i = 0;
//		for (String value : values) {
//			((ArrayType[])field.get(args))[i++] = getValue(value, arrayTypeClass);
//		}
		
		
		Object array = Array.newInstance(arrayTypeClass, values.size());
		int i = 0;
		for (String value : values) {
			Array.set(array, i++, getValue(value, arrayTypeClass));
		}
		
		field.set(args, array);
	}
}



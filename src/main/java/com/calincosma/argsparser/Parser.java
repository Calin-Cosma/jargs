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
	
	
	
	/**
	 * Process the list of values and sets the value of the field as a collection, array, or single object depending on the field type.
	 *
	 * @param args
	 * @param field
	 * @param values
	 * @param <ARGS>
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <ARGS> void setValues(ARGS args, Field field, List<String> values) throws IllegalAccessException, InstantiationException {
		/* it's a value/param */
		Class fieldType = field.getType();
		
		if (Collection.class.isAssignableFrom(fieldType)) {
			/* treating collections - attempting to instantiate collection */
			if (fieldType.isInterface()) {
				/* if the type is an interface, use a common implementation */
				if (List.class.isAssignableFrom(fieldType)) {
					field.set(args, new ArrayList<>());
				} else if (Set.class.isAssignableFrom(fieldType)) {
					field.set(args, new HashSet<>());
				} else if (Queue.class.isAssignableFrom(fieldType)) {
					field.set(args, new LinkedList<>());
				}
			} else {
				/* if the type is a class, instantiate it */
				field.set(args, fieldType.newInstance());
			}
			
						
			/* collection has been instantiated, add values to collection */
			ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
			Class<?> collectionType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
			
			
			for (String value : values) {
				((Collection)field.get(args)).add(getValue(value, collectionType));
			}
			
		} else if (fieldType.isArray()) {
			/* arrays */
			Class arrayType = fieldType.getComponentType();
			createArray(args, field, values, arrayType);
		} else if (values.size() == 1) {
			/* single values, when the type is not a collection or array, should be single objects */
			field.set(args, getValue(values.get(0), fieldType));
		} else {
			// TODO throw exception because this case is not supported
		}
	}
	
	
	/**
	 * Take a string and return the value depending on the type. Almost all primitive types are supported, String,
	 * and all objects that have a static method named valueOf that takes a parameter of type String.
	 *
	 * This allows any custom classes to be easily adapted.
	 *
	 * @param arg
	 * @param fieldType
	 * @param <ARGS>
	 * @param <VALUE>
	 * @return
	 */
	private <ARGS extends Object, VALUE> VALUE getValue(String arg, Class<VALUE> fieldType) {
		try {
			if (String.class == fieldType) {
				/* strings get set immediately, as they are the easiest param to set */
				return (VALUE)arg;
			}
			
			Method method = null;
			try {
				method = fieldType.getMethod("valueOf", String.class);
			} catch (Exception e) {
			}
			if (method != null && method.getReturnType() == fieldType && Modifier.isStatic(method.getModifiers())) {
				return fieldType.cast(method.invoke(null, arg));
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
	
	
	/**
	 * Creates an array and fills in the values.
	 *
	 * @param args
	 * @param field
	 * @param values
	 * @param arrayTypeClass
	 * @param <ARGS>
	 * @param <ArrayType>
	 * @throws IllegalAccessException
	 */
	private <ARGS, ArrayType> void createArray(ARGS args, Field field, List<String> values, Class<ArrayType> arrayTypeClass) throws IllegalAccessException {
		Object array = Array.newInstance(arrayTypeClass, values.size());
		int i = 0;
		for (String value : values) {
			Array.set(array, i++, getValue(value, arrayTypeClass));
		}
		
		field.set(args, array);
	}
}



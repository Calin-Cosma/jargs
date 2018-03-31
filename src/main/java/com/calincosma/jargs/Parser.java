/*
 * Copyright (c) 2018  Calin Cosma
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.calincosma.jargs;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * The Parser class is responsible for processing command line arguments passed to a Java application and setting them as fields of a given POJO.
 * The fields must have been annotated with the {@link Arg} annotation.
 * The parser supports the following types:
 * <ul>
 *     <li>String</li>
 *     <li>Integer</li>
 *     <li>int</li>
 *     <li>Long</li>
 *     <li>long</li>
 *     <li>Double</li>
 *     <li>double</li>
 *     <li>Float</li>
 *     <li>float</li>
 *     <li>Short</li>
 *     <li>short</li>
 *     <li>Boolean</li>
 *     <li>boolean</li>
 *     <li>File</li>
 *     <li>Path</li>
 *     <li>collections (lists, sets, queues)</li>
 *     <li>maps</li>
 *     <li>arrays of all known types (not arrays of collections/maps/arrays)</li>
 *     <li>enums</li>
 *     <li>any type that has a <strong>static</strong> method called <strong>valueOf(String s)</strong> which returns that type; this includes custom, user defined types</li>
 * </ul>
 *
 * The POJO must have a no argument constructor.
 *
 * If for any reason the parsing of arguments fails, a {@link ArgsParserException} is thrown.
 *
 * Usage examples can be seen in the ParserTest class.
 *
 */
public class Parser {
	
	public static Parser getInstance() {
		return new Parser();
	}
	
	
	private Parser() {
	}
	
	
	/**
	 * Parse the arguments in the argsArray and set them as fields of a POJO of type ARGS.
	 * The POJO will be created and then the args set as fields.
	 *
	 * @param argsArray
	 * @param clazz
	 * @param <ARGS>
	 * @return new POJO of type ARGS, with the arguments as fields
	 * @throws ArgsParserException
	 */
	public <ARGS> ARGS parse(String[] argsArray, Class<ARGS> clazz) throws ArgsParserException {
		try {
			ARGS args = clazz.newInstance();
			
			Map<String, Field> options = new HashMap<String, Field>();
			Set<Field> requiredFields = new HashSet<Field>();
			Set<Field> treatedFields = new HashSet<Field>();
			Field currentField = null;
			
			
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
				throw new ArgsParserException("Missing values for required arguments " + requiredFieldsNames);
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
	private <ARGS> void setValues(ARGS args, Field field, List<String> values) throws IllegalAccessException, InstantiationException {
		/* it's a value/param */
		Class fieldType = field.getType();
		
		if (Collection.class.isAssignableFrom(fieldType)) {
			/* COLLECTIONS */
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
		} else if (Map.class.isAssignableFrom(fieldType)) {
			/* MAPS */
			if (fieldType.isInterface()) {
				/* if type is an interface, attempt to instantiate it */
				if (ConcurrentMap.class.isAssignableFrom(fieldType)) {
					field.set(args, new ConcurrentHashMap<>());
				} else if (NavigableMap.class.isAssignableFrom(fieldType) || SortedMap.class.isAssignableFrom(fieldType)) {
					field.set(args, new TreeMap<>());
				} else if (ConcurrentNavigableMap.class.isAssignableFrom(fieldType)) {
					field.set(args, new ConcurrentSkipListMap<>());
				} else {
					field.set(args, new HashMap<>());
				}
			} else {
				/* if the type is a class, instantiate it */
				field.set(args, fieldType.newInstance());
			}
			
			ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
			Class<?> keyType= (Class<?>)parameterizedType.getActualTypeArguments()[0];
			Class<?> valueType= (Class<?>)parameterizedType.getActualTypeArguments()[1];
			
			for (String value : values) {
				String mapKey = value.substring(0, value.indexOf("="));
				String mapValue = value.substring(value.indexOf("=") + 1);
				((Map)field.get(args)).put(getValue(mapKey, keyType), getValue(mapValue, valueType));
			}
		} else if (fieldType.isArray()) {
			/* ARRAYS */
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
			} else if (File.class == fieldType) {
				return (VALUE)new File(arg);
			} else if (Path.class == fieldType) {
				return (VALUE)Paths.get(arg);
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



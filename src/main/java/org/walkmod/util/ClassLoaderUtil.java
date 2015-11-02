/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ClassLoaderUtil {

	private static Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();

	private static Map<String, String> wrapperClasses = new HashMap<String, String>();

	private static Map<String, Integer> matrixTypePosition;

	private static boolean[][] compatibilityMatrix;

	private static Set<String> langPackageContent;

	static {
		primitiveClasses.put("boolean", boolean.class);
		primitiveClasses.put("int", int.class);
		primitiveClasses.put("long", long.class);
		primitiveClasses.put("double", double.class);
		primitiveClasses.put("char", char.class);
		primitiveClasses.put("float", float.class);
		primitiveClasses.put("short", short.class);
		primitiveClasses.put("byte", byte.class);
		matrixTypePosition = new HashMap<String, Integer>();
		matrixTypePosition.put("byte", 0);
		matrixTypePosition.put("java.lang.Byte", 0);
		matrixTypePosition.put("short", 1);
		matrixTypePosition.put("char", 2);
		matrixTypePosition.put("java.lang.Character", 2);
		matrixTypePosition.put("int", 3);
		matrixTypePosition.put("java.lang.Integer", 3);
		matrixTypePosition.put("long", 4);
		matrixTypePosition.put("java.lang.Long", 4);
		matrixTypePosition.put("float", 5);
		matrixTypePosition.put("java.lang.Float", 5);
		matrixTypePosition.put("double", 6);
		matrixTypePosition.put("java.lang.Double", 6);
		matrixTypePosition.put("boolean", 7);
		matrixTypePosition.put("java.lang.Boolean", 7);
		matrixTypePosition.put("String", 8);
		matrixTypePosition.put("java.lang.String", 8);
		matrixTypePosition.put("java.lang.Object", 9);
		wrapperClasses.put("java.lang.Byte", "byte");
		wrapperClasses.put("java.lang.Character", "char");
		wrapperClasses.put("java.lang.Integer", "int");
		wrapperClasses.put("java.lang.Long", "long");
		wrapperClasses.put("java.lang.Float", "float");
		wrapperClasses.put("java.lang.Double", "double");
		wrapperClasses.put("java.lang.Boolean", "boolean");
		compatibilityMatrix = new boolean[][] { { true, true, true, true, true, true, true, false, false, true },
				{ false, true, false, true, true, true, true, false, false, true },
				{ false, false, true, true, true, true, true, false, false, true },
				{ false, false, false, true, true, true, true, false, false, true },
				{ false, false, false, false, true, true, true, false, false, true },
				{ false, false, false, false, false, true, true, false, false, true },
				{ false, false, false, false, false, false, true, false, false, true },
				{ false, false, false, false, false, false, false, true, false, true },
				{ false, false, false, false, false, false, false, false, true, true },
				{ false, false, false, false, false, false, false, false, false, true } };
		langPackageContent = new HashSet<String>();
		langPackageContent.add("Appendable");
		langPackageContent.add("CharSequence");
		langPackageContent.add("Cloneable");
		langPackageContent.add("Comparable");
		langPackageContent.add("Iterable");
		langPackageContent.add("Readable");
		langPackageContent.add("Runnable");
		langPackageContent.add("Thread$UncaughtExceptionHandler");
		langPackageContent.add("Boolean");
		langPackageContent.add("Byte");
		langPackageContent.add("Character");
		langPackageContent.add("Character$Subset");
		langPackageContent.add("Character$UnicodeBlock");
		langPackageContent.add("Class");
		langPackageContent.add("ClassLoader");
		langPackageContent.add("Compiler");
		langPackageContent.add("Double");
		langPackageContent.add("Enum");
		langPackageContent.add("Float");
		langPackageContent.add("InheritableThreadLocal");
		langPackageContent.add("Integer");
		langPackageContent.add("Long");
		langPackageContent.add("Math");
		langPackageContent.add("Number");
		langPackageContent.add("Object");
		langPackageContent.add("Package");
		langPackageContent.add("Process");
		langPackageContent.add("ProcessBuilder");
		langPackageContent.add("Runtime");
		langPackageContent.add("RuntimePermission");
		langPackageContent.add("SecurityManager");
		langPackageContent.add("Short");
		langPackageContent.add("StackTraceElement");
		langPackageContent.add("StrictMath");
		langPackageContent.add("String");
		langPackageContent.add("StringBuffer");
		langPackageContent.add("StringBuilder");
		langPackageContent.add("System");
		langPackageContent.add("Thread");
		langPackageContent.add("ThreadGroup");
		langPackageContent.add("ThreadLocal");
		langPackageContent.add("Throwable");
		langPackageContent.add("Void");
		langPackageContent.add("Thread$State");
		langPackageContent.add("ArithmeticException");
		langPackageContent.add("ArrayIndexOutOfBoundsException");
		langPackageContent.add("ArrayStoreException");
		langPackageContent.add("ClassCastException");
		langPackageContent.add("ClassNotFoundException");
		langPackageContent.add("CloneNotSupportedException");
		langPackageContent.add("EnumConstantNotPresentException");
		langPackageContent.add("Exception");
		langPackageContent.add("IllegalAccessException");
		langPackageContent.add("IllegalArgumentException");
		langPackageContent.add("IllegalMonitorStateException");
		langPackageContent.add("IllegalStateException");
		langPackageContent.add("IllegalThreadStateException");
		langPackageContent.add("IndexOutOfBoundsException");
		langPackageContent.add("InstantiationException");
		langPackageContent.add("InterruptedException");
		langPackageContent.add("NegativeArraySizeException");
		langPackageContent.add("NoSuchFieldException");
		langPackageContent.add("NoSuchMethodException");
		langPackageContent.add("NullPointerException");
		langPackageContent.add("NumberFormatException");
		langPackageContent.add("RuntimeException");
		langPackageContent.add("SecurityException");
		langPackageContent.add("StringIndexOutOfBoundsException");
		langPackageContent.add("TypeNotPresentException");
		langPackageContent.add("UnsupportedOperationException");
		langPackageContent.add("AbstractMethodError");
		langPackageContent.add("AssertionError");
		langPackageContent.add("ClassCircularityError");
		langPackageContent.add("ClassFormatError");
		langPackageContent.add("Error");
		langPackageContent.add("ExceptionInInitializerError");
		langPackageContent.add("IllegalAccessError");
		langPackageContent.add("IncompatibleClassChangeError");
		langPackageContent.add("InstatiationError");
		langPackageContent.add("InternalError");
		langPackageContent.add("LinkageError");
		langPackageContent.add("NoClassDefFoundError");
		langPackageContent.add("NoSuchFieldError");
		langPackageContent.add("NoSuchMethodError");
		langPackageContent.add("OutOfMemoryError");
		langPackageContent.add("StackOverflowError");
		langPackageContent.add("ThreadDeath");
		langPackageContent.add("UnknownError");
		langPackageContent.add("UnsatistiedLinkError");
		langPackageContent.add("UnsupportedClassVersionError");
		langPackageContent.add("VerifyError");
		langPackageContent.add("VirtualMachineError");
		langPackageContent.add("Deprecated");
		langPackageContent.add("Override");
		langPackageContent.add("SuppressWarnings");
	}

	public static boolean belongsToLangPackage(String clazz) {
		return langPackageContent.contains(clazz);
	}

	public static boolean isCompatible(Class<?> fromClass, Class<?> toClass) {
		if (fromClass == null || toClass == null) {
			return true;
		}
		if (matrixTypePosition.containsKey(fromClass.getName()) && matrixTypePosition.containsKey(toClass.getName())) {
			return compatibilityMatrix[matrixTypePosition.get(fromClass.getName())][matrixTypePosition.get(toClass
					.getName())];
		} else {
			return toClass.isAssignableFrom(fromClass);
		}
	}

	public static boolean isPrimitiveWrapperClass(Class<?> clazz) {
		return wrapperClasses.containsKey(clazz.getName());
	}

	public static Class<?> getPrimitiveClass(Class<?> wrapperClazz) {
		if (isPrimitiveWrapperClass(wrapperClazz)) {
			return getPrimitiveClass(wrapperClasses.get(wrapperClazz.getName()));
		}
		return null;
	}

	public static Class<?> getPrimitiveClass(String name) {
		return primitiveClasses.get(name);
	}

	public static boolean isCompatible(Class<?>[] fromClasses, Class<?>[] toClasses) {
		if (fromClasses.length == toClasses.length) {
			boolean assignable = true;
			for (int i = 0; i < fromClasses.length && assignable; i++) {
				assignable = isCompatible(fromClasses[i], toClasses[i]);
			}
			return assignable;
		}
		return false;
	}

	/**
	 * This method returns a method(public, protected or private) which appears
	 * in the selected class or any of its parent (interface or superclass) or
	 * container classes (when the selected class is an inner class) which
	 * matches with the name and whose parameters are compatible with the
	 * selected typeArgs.
	 * 
	 * @param clazz
	 *            Class to scan
	 * @param methodName
	 *            Method name
	 * @param typeArgs
	 *            Method arguments
	 * @return Method instance
	 */
	public Method getMethod(Class<?> clazz, String methodName, Class<?>... typeArgs) {
		int numParams = typeArgs == null ? 0 : typeArgs.length;
		Method[] classMethods = clazz.getMethods();
		for (Method method : classMethods) {
			if (method.getName().equals(methodName)) {
				if (method.getParameterTypes().length == numParams) {
					boolean isCompatible = true;
					Class<?>[] methodParameterTypes = method.getParameterTypes();
					for (int i = 0; i < methodParameterTypes.length; i++) {
						isCompatible = isCompatible(typeArgs[i], methodParameterTypes[i]);
						if (!isCompatible)
							break;
					}
					if (isCompatible) {
						return method;
					}
				}
			}
		}
		classMethods = clazz.getDeclaredMethods();
		for (Method method : classMethods) {
			if (method.getName().equals(methodName)) {
				if (method.getParameterTypes().length == numParams) {
					boolean isCompatible = true;
					Class<?>[] methodParameterTypes = method.getParameterTypes();
					for (int i = 0; i < methodParameterTypes.length; i++) {
						isCompatible = isCompatible(typeArgs[i], methodParameterTypes[i]);
						if (!isCompatible)
							break;
					}
					if (isCompatible) {
						return method;
					}
				}
			}
		}
		Method result = null;
		if (clazz.isMemberClass()) {
			result = getMethod(clazz.getEnclosingClass(), methodName, typeArgs);
		}
		if (result == null && clazz.getSuperclass() != null) {
			return getMethod(clazz.getSuperclass(), methodName, typeArgs);
		}
		return result;
	}

	/**
	 * Load all resources with a given name, potentially aggregating all results
	 * from the searched classloaders. If no results are found, the resource
	 * name is prepended by '/' and tried again.
	 *
	 * This method will try to load the resources using the following methods
	 * (in order):
	 * <ul>
	 * <li>From Thread.currentThread().getContextClassLoader()
	 * <li>From ClassLoaderUtil.class.getClassLoader()
	 * <li>callingClass.getClassLoader()
	 * </ul>
	 *
	 * @param resourceName
	 *            The name of the resources to load
	 * @param callingClass
	 *            The Class object of the calling object
	 * @param aggregate
	 *            <code>true</code> to aggregate resources from all classloaders
	 * @return Iterator of matching resources
	 * @throws IOException
	 *             If I/O errors occur
	 */
	public static Iterator<URL> getResources(String resourceName, Class<?> callingClass, boolean aggregate)
			throws IOException {
		AggregateIterator<URL> iterator = new AggregateIterator<URL>();
		iterator.addEnumeration(Thread.currentThread().getContextClassLoader().getResources(resourceName));
		if (!iterator.hasNext() || aggregate) {
			iterator.addEnumeration(ClassLoaderUtil.class.getClassLoader().getResources(resourceName));
		}
		if (!iterator.hasNext() || aggregate) {
			ClassLoader cl = callingClass.getClassLoader();
			if (cl != null) {
				iterator.addEnumeration(cl.getResources(resourceName));
			}
		}
		if (!iterator.hasNext() && (resourceName != null)
				&& ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) {
			return getResources('/' + resourceName, callingClass, aggregate);
		}
		return iterator;
	}

	/**
	 * Load a given resource.
	 *
	 * This method will try to load the resource using the following methods (in
	 * order):
	 * <ul>
	 * <li>From Thread.currentThread().getContextClassLoader()
	 * <li>From ClassLoaderUtil.class.getClassLoader()
	 * <li>callingClass.getClassLoader()
	 * </ul>
	 *
	 * @param resourceName
	 *            The name IllegalStateException("Unable to call ")of the
	 *            resource to load
	 * @param callingClass
	 *            The Class object of the calling object
	 * @return Matching resouce or null if not found
	 */
	public static URL getResource(String resourceName, Class<?> callingClass) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if (url == null) {
			url = ClassLoaderUtil.class.getClassLoader().getResource(resourceName);
		}
		if (url == null) {
			ClassLoader cl = callingClass.getClassLoader();
			if (cl != null) {
				url = cl.getResource(resourceName);
			}
		}
		if ((url == null) && (resourceName != null)
				&& ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) {
			return getResource('/' + resourceName, callingClass);
		}
		return url;
	}

	/**
	 * This is a convenience method to load a resource as a stream.
	 *
	 * The algorithm used to find the resource is given in getResource()
	 *
	 * @param resourceName
	 *            The name of the resource to load
	 * @param callingClass
	 *            The Class object of the calling object
	 * @return Matching resouce or null if not found
	 */
	public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) {
		URL url = getResource(resourceName, callingClass);
		try {
			return (url != null) ? url.openStream() : null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Load a class with a given name.
	 *
	 * It will try to load the class in the following order:
	 * <ul>
	 * <li>From Thread.currentThread().getContextClassLoader()
	 * <li>Using the basic Class.forName()
	 * <li>From ClassLoaderUtil.class.getClassLoader()
	 * <li>From the callingClass.getClassLoader()
	 * </ul>
	 *
	 * @param className
	 *            The name of the class to load
	 * @param callingClass
	 *            The Class object of the calling object
	 * @return Class definition
	 * @throws ClassNotFoundException
	 *             If the class cannot be found anywhere.
	 */
	public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException ex) {
				try {
					return ClassLoaderUtil.class.getClassLoader().loadClass(className);
				} catch (ClassNotFoundException exc) {
					return callingClass.getClassLoader().loadClass(className);
				}
			}
		}
	}

	/**
	 * Aggregates Enumeration instances into one iterator and filters out
	 * duplicates. Always keeps one ahead of the enumerator to protect against
	 * returning duplicates.
	 */
	protected static class AggregateIterator<E> implements Iterator<E> {

		LinkedList<Enumeration<E>> enums = new LinkedList<Enumeration<E>>();

		Enumeration<E> cur = null;

		E next = null;

		Set<E> loaded = new HashSet<E>();

		public AggregateIterator<E> addEnumeration(Enumeration<E> e) {
			if (e.hasMoreElements()) {
				if (cur == null) {
					cur = e;
					next = e.nextElement();
					loaded.add(next);
				} else {
					enums.add(e);
				}
			}
			return this;
		}

		public boolean hasNext() {
			return (next != null);
		}

		public E next() {
			if (next != null) {
				E prev = next;
				next = loadNext();
				return prev;
			} else {
				throw new NoSuchElementException();
			}
		}

		private Enumeration<E> determineCurrentEnumeration() {
			if (cur != null && !cur.hasMoreElements()) {
				if (enums.size() > 0) {
					cur = enums.removeLast();
				} else {
					cur = null;
				}
			}
			return cur;
		}

		private E loadNext() {
			if (determineCurrentEnumeration() != null) {
				E tmp = cur.nextElement();
				while (loaded.contains(tmp)) {
					tmp = loadNext();
					if (tmp == null) {
						break;
					}
				}
				if (tmp != null) {
					loaded.add(tmp);
				}
				return tmp;
			}
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}

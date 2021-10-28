/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.jython;

import static gda.jython.GdaBuiltin.WildType.EXTENDS;
import static gda.jython.GdaBuiltin.WildType.SUPER;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyBuiltinFunction;
import org.python.core.PyClass;
import org.python.core.PyException;
import org.python.core.PyInstance;
import org.python.core.PyIterator;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyReflectedFunction;
import org.python.core.PySequenceList;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.core.PyXRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class to present multiple static methods as a PyObject that can be used
 * as a builtin method. Uses existing Jython classes to delegate calls to the correct
 * method and reflection to build useful docstrings for methods.
 * <p>
 * Builtins can be created individually (using a list of methods and the name the builtin should
 * be called) or by parsing a class for all static methods that have the {@link GdaJythonBuiltin}
 * annotation.
 */
public class GdaBuiltin extends PyBuiltinFunction {
	private static final Logger logger = LoggerFactory.getLogger(GdaBuiltin.class);
	/** Indent documentation of each function to distinguish it from the signature */
	private static final String INDENT = "\n    ";
	/** Python function to handle argument counts/types and pass call to correct function */
	private PyReflectedFunction function;
	/** The class that these methods are from */
	private Class<?> source;
	/** The original java methods that are being wrapped */
	private Collection<Method> methods;

	/** Create builtin from <name, List<Methods>> map entry. Mainly to be used in Map.foreach method */
	protected static GdaBuiltin from(Map.Entry<String, List<Method>> methodGroup) {
		logger.debug("Creating builtin function for {} from {} method(s)",
				methodGroup.getKey(),
				methodGroup.getValue().size());
		return new GdaBuiltin(methodGroup.getKey(), methodGroup.getValue());
	}

	/**
	 * Create a Python Callable for each static method in the given class
	 * and register them as builtins with the current PySystemState.
	 * @param clazz with static/annotated methods
	 */
	public static void registerBuiltinsFrom(Class<?> clazz) {
		logger.debug("Registering builtin functions from {}", clazz.getCanonicalName());
		builtinMethodsFrom(clazz)
				.forEach(m -> PySystemState.getDefaultBuiltins().__setitem__(m.getName(), m));
	}

	/**
	 * Create a builtin wrapper around all annotated static methods in the given class
	 * @param clazz with annotated static methods
	 * @return a collection of builtin functions
	 */
	public static Collection<GdaBuiltin> builtinMethodsFrom(Class<?> clazz) {
		return Stream.of(clazz.getMethods())
				.filter(m -> isStatic(m.getModifiers()))
				.filter(m -> m.isAnnotationPresent(GdaJythonBuiltin.class))
				.collect(groupingBy(Method::getName))
				.entrySet()
				.stream()
				.map(GdaBuiltin::from)
				.collect(toList());
	}

	/**
	 * Create builtin function for given methods. Calls are handled by Jython's
	 * PyReflectedFunction leaving this class to generate the documentation.
	 * @param name of the builtin
	 * @param methods used to handle calls to this builtin
	 */
	public GdaBuiltin(String name, List<Method> methods) {
		super(name, null); // null docs - we'll generate them if needed
		if (methods.isEmpty()) throw new IllegalArgumentException("At least one method is required");
		this.function = new PyReflectedFunction(methods.toArray(new Method[] {}));
		this.methods = methods;
		this.source = methods.get(0).getDeclaringClass();
	}

	public String getName() {
		return function.__name__;
	}

	@Override
	public String getDoc() {
		if (doc == null) buildDoc();
		return super.getDoc();
	}

	/** Generate and cache the doc string. Only called once to lazy load docs */
	private void buildDoc() {
		try {
			var docstring = methods.stream()
					.map(method -> method.getAnnotation(GdaJythonBuiltin.class))
					.map(GdaJythonBuiltin::docstring)
					.filter(docs -> !docs.isBlank())
					.collect(joining("\n"));
			var overloads = methods.stream()
					.map(this::docsFor)
					.sorted()
					.collect(joining("\n"))
				+ "\n\nfrom "
				+ source.getCanonicalName();
			doc = (docstring + "\n\n" + overloads).strip();
		} catch (Exception e) {
			logger.warn("Couldn't build docs for {}", getName(), e);
			doc = "from " + source.getCanonicalName();
		}
	}

	@Override
	public PyObject __call__(PyObject[] args, String[] keywords) {
		logger.debug("Calling {}.{} with {} args", source.getCanonicalName(), getName(), args.length);
		try {
			return function.__call__((PyObject)null, args, keywords);
		} catch (PyException pe) {
			if (keywords.length > 0) {
				throw new UsageException("Unexpected keywords passed to builtin: ",
						Stream.of(keywords).collect(joining(", ")));
			}
			if (pe.match(Py.TypeError) && !validArgs(args)) {
				String arglist = Stream.of(args)
						.map(GdaBuiltin::describe)
						.collect(joining(", "));
				throw new UsageException("Unexpected arguments to builtin method: ",
					 arglist);
			} else {
				throw pe;
			}
		}
	}

	/** Check if the supplied arguments are valid for any of the wrapped functions */
	private boolean validArgs(PyObject[] args) {
		for (var expected: function.argslist) {
			var types = expected.args;
			if (types.length < args.length && expected.isVarArgs) {
				args = boxVarargs(args, types.length);
			}
			if (types.length != args.length) {
				continue;
			}
			var finalArgs = args; // pointless redirect to make variable 'effectively final'
			var valid = IntStream.range(0, args.length)
					.allMatch(i -> finalArgs[i].__tojava__(expected.args[i]) != Py.NoConversion);
			if (valid) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If there are more arguments than expected, wrap all extra arguments into a single
	 * array to represent the varargs argument of a Java method. The types of the extra
	 * arguments are checked later so do not matter at this stage.
	 * <br>
	 * This is a modified version of the private function
	 * {@code ReflectedArgs#ensureBNoxedVarargs}.
	 *
	 * @param args All the arguments passed to the function
	 * @param length The number of arguments expected (including varargs array as 1)
	 * @return An array of objects matching the number expected - may be the original array
	 */
	private PyObject[] boxVarargs(PyObject[] args, int length) {
		if (args.length == 0) {
			// if length is > 1, this will still fail later but not our problem
			return new PyObject[]{new PyList()};
		}
		PyObject lastArg = args[args.length - 1];
		if (args.length == length &&
				lastArg instanceof PySequenceList ||
				lastArg instanceof PyArray ||
				lastArg instanceof PyXRange ||
				lastArg instanceof PyIterator) {
			return args; // will be boxed in an array once __tojava__ is called
		}
		int positionals = length - 1;
		if (args.length < positionals) {
			return args;
		}
		var boxedArgs = new PyObject[length];
		System.arraycopy(args, 0, boxedArgs, 0, positionals);
		int others = args.length - positionals;
		var varargs = new PyObject[others];
		System.arraycopy(args, positionals, varargs, 0, others);
		boxedArgs[positionals] = new PyList(varargs);
		return boxedArgs;
	}

	/**
	 * Generate docs string for method
	 * <p>
	 * Generated docs are of the form
	 * <pre>function_name(ArgType1, ArgType2, Etc) -> ReturnType
	 *     Doc string from annotation
	 *     indented and with line breaks preserved.</pre>
     * </pre>
	 * @param method to be documented
	 * @return method signature and doc string
	 */
	private String docsFor(Method method) {
		GdaJythonBuiltin builtin = method.getAnnotation(GdaJythonBuiltin.class);
		requireNonNull(builtin, "Method is not a builtin");

		boolean deprecated = method.isAnnotationPresent(Deprecated.class);

		String doc = Stream.of(builtin.overload().split("\n"))
				.collect(joining(INDENT));
		return getName()
				+ argList(method)
				+ returnType(method)
				+ (deprecated ? " DEPRECATED" : "")
				+ INDENT
				+ typeVariables(method)
				+ doc;
	}

	/** Get the parameter types for the given function as a  bracketed string */
	private static String argList(Method method) {
		Parameter[] parameters = method.getParameters();
		List<String> types = Stream.of(parameters)
				.map(Parameter::getParameterizedType)
				.map(GdaBuiltin::className)
				.collect(toList());

		if (method.isVarArgs()) { // display vararg as (eg) int... instead of int[]
			// Could probably also be done via reflection instead of messing with strings
			// but this seems to work
			int lastArg = method.getParameterCount() - 1;
			String type = types.get(lastArg);
			types.set(lastArg, type.substring(0, type.length()-2) + "...");
		}
		return types.stream().collect(joining(", ", "(", ")"));
	}

	/**
	 * Get the return type string for a method. Returns empty string for void methods.
	 */
	private static String returnType(Method method) {
		Type returnClass = method.getGenericReturnType();
		return Void.TYPE.equals(returnClass) ? "" : " -> " + className(returnClass);
	}

	/**
	 * List the bounds on any named type variables
	 * <p>
	 * If type arguments are not used in either arguments or return type, they are not
	 * listed.
	 * @param method possibly referencing type variables
	 * @return String listing bounds on type variables
	 */
	private static String typeVariables(Method method) {
		Type returnType = method.getGenericReturnType();
		Type[] args = method.getGenericParameterTypes();
		String bounds = Stream.concat(of(returnType), of(args))
				.filter(TypeVariable.class::isInstance)
				.map(TypeVariable.class::cast)
				.distinct()
				.sorted((t1, t2) -> t1.getName().compareTo(t2.getName()))
				.map(t -> {
					String boundString = EXTENDS.forBounds(t.getBounds());
					return t.getTypeName() + (boundString.isEmpty() ? " is any type" : boundString);
				})
				.collect(joining(INDENT + "       ")); // extra indent to line up with "where: "
		return bounds.isEmpty() ? "" : "where: " + bounds + INDENT;
	}

	/**
	 * Get the simple class name for the given type
	 * <p>
	 * Remove the package name and leave only the simple name. Runs recursively to
	 * ensure that generic types don't include FQCN in type parameters.
	 * @param type
	 * @return The simple classname including generic types
	 */
	private static String className(Type type) {
		if (type instanceof Class) { // eg String.class
			return ((Class<?>)type).getSimpleName();
		} else if (type instanceof ParameterizedType) { // eg List<String>
			ParameterizedType para = (ParameterizedType)type;
			return className(para.getRawType())
					+ Stream.of(para.getActualTypeArguments())
					.map(GdaBuiltin::className)
					.collect(joining(", ", "<", ">"));
		} else if (type instanceof WildcardType) { // eg ? super Findable
			WildcardType wild = (WildcardType) type;
			Type[] lower = wild.getLowerBounds();
			Type[] upper = wild.getUpperBounds();
			return "?" + SUPER.forBounds(lower) + EXTENDS.forBounds(upper); // should only ever be one of the two?
		} else if (type instanceof GenericArrayType) { // eg List<String>[]
			GenericArrayType array = (GenericArrayType) type;
			return className(array.getGenericComponentType()) + "[]";
		} else { // For anything else just return the name - generally type variables eg T
			return type.getTypeName();
		}
	}

	/**
	 * Get the class name for a PyObject. If the object is an instance of a Java class, return {@link #className},
	 * else extract the python class name. Used for reporting usage errors.
	 *
	 * @param obj the instance
	 * @return A string describing the class of the instance
	 */
	private static String describe(PyObject obj) {
		if (obj instanceof PyInstance) { // old style python class
			PyClass ins = ((PyInstance) obj).instclass;
			return ins.__str__().asString();
		} else if (obj instanceof PyObjectDerived) { // new style python class (extends object)
			PyType type = obj.getType();
			return type.getModule() + "." + type.getName();
		} else if (obj == Py.None) {
			return Py.None.toString();
		} else {
			return className(Py.tojava(obj, Object.class).getClass());
		}
	}

	public class UsageException extends RuntimeException {
		private String actualUsage;

		public UsageException(String message, String actual) {
			super(message);
			actualUsage = actual;
		}

		public String getError() {
			return actualUsage;
		}

		public String getUsage() {
			return getDoc();
		}

		@Override
		public String getMessage() {
			return super.getMessage() + "'" + getName() + "'\n"
					+ "Received: " + actualUsage + "\n"
					+ "Usage: " + getUsage();
		}
	}

	enum WildType {
		SUPER, EXTENDS;

		/**
		 * Get the bounds string for a wildcard type (eg super X or extends Y)
		 * @param bounds Array of bounding types (usually empty or single entry)
		 * @return The bounding string for this wildcard type and these bounds
		 */
		private String forBounds(Type[] bounds) {
			String bound = Stream.of(bounds)
					.filter(t -> !Object.class.equals(t)) // all wildcards extend at least Object
					.map(GdaBuiltin::className)
					.collect(joining(" & "));
			return bound.isEmpty() ? "" : " " + name().toLowerCase() + " " + bound;
		}
	}
}
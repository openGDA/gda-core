/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.annotation.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scanning.api.IServiceResolver;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The device manager parses annotations and allows methods to be
 * efficiently called during a scan to notify of progress. This replaces
 * the need to override an atScanStart() method as well as allowing resources
 * to be injected into the method.
 *
 * If attemps to parse all the reflection stuff up-front so that a call
 * to invoke(...) during the scan can be as efficiently despatched using
 * method.invoke(...) as possible.
 *
 * This class could be made into a general purpose annotation parsing
 * and method calling class once tested.
 *
 * NOTE: If you find yourself debugging this class to view despatched events,
 * consider adding a test to @see AnnotationManagerTest to reproduce the problem.
 * Trying to debug annotation parsing in a live scanning system is not desirable.
 *
 * @author Matthew Gerring
 *
 */
public class AnnotationManager {

	private static Logger logger = LoggerFactory.getLogger(AnnotationManager.class);

	private Map<Class<? extends Annotation>, Collection<MethodWrapper>> annotationMap;
	private Map<Class<?>, Collection<Class<?>>>                         cachedClasses;
	private Map<Class<?>, Object>                                       services;
	private Collection<Object>                                          extraContext;

	private Collection<Class<? extends Annotation>> annotations;
	private IServiceResolver serviceResolver;

	public AnnotationManager() {
		this((IServiceResolver)null, DeviceAnnotations.getAllAnnotations());
	}

	@SafeVarargs
	public AnnotationManager(Class<? extends Annotation>... a) {
		this(null, a);
	}

	public AnnotationManager(IServiceResolver resolver) {
		this(resolver, DeviceAnnotations.getAllAnnotations());
	}

	@SafeVarargs
	public AnnotationManager(IServiceResolver resolver, Class<? extends Annotation>... a) {
		this(resolver, Arrays.asList(a));
	}

	/**
	 * Set some implementations of types, for instance services.
	 * Used in addition to the OSGi services available.
	 * In test mode replaces OSGi services.
	 *
	 * @param services
	 */
	public AnnotationManager(IServiceResolver resolver, Map<Class<?>, Object> services) {
		this(resolver);
		this.services = services;
	}

	/**
	 *
	 * @param resolver - may be null
	 * @param a
	 */
	private AnnotationManager(IServiceResolver resolver, Collection<Class<? extends Annotation>> a) {
		this.serviceResolver = resolver;
		this.annotationMap = new ConcurrentHashMap<>();
		this.cachedClasses = new ConcurrentHashMap<>();
		this.annotations = a;
		this.services = Collections.emptyMap();

		if (resolver!=null) {
			try {
				Collection<IScanParticipant> others = resolver.getServices(IScanParticipant.class);
				if (others !=null) addDevices(others);
			} catch (Exception ne) {
				// We do not actually care if scanning could not get the IScanParticipants.
				logger.warn("Could not add implementors of "+IScanParticipant.class+" into annotated devices!", ne);
			}
		}
	}

	/**
	 * Add a group of devices. As the devices are added if they implement ILevel,
	 * they are sorted by level and added in that order. If another call to add
	 * devices is made the new collection of devices will be sorted by level and
	 * added to the end of the main list of devices.
	 *
	 * So for:
	 * <code>
	 * manager.add(devices1[])
	 * manager.add(devices2[])
	 * </code>
	 * The notification order will be all the devices1, by level then all the devices2
	 * by level. So the overall order is by add order followed by level. This allows
	 * for instance all devices of a given type to be notified by level before another
	 * group of objects of another type. If no distinction of type is required, simply
	 * add all devices in one go and they will be sorted by level.
	 *
	 * If a device does not implement ILevel its level is assumed to be ILevel.MAXIMUM
	 *
	 * @param ds
	 */
	public void addDevices(Object... ds) {
		if (ds == null)  throw new IllegalArgumentException("No devices specified!");
		if (ds.length<1) throw new IllegalArgumentException("No devices specified!");
		addDevices(Arrays.asList(ds));
	}

	/**
	 * Add a group of devices. As the devices are added if they implement ILevel,
	 * they are sorted by level and added in that order. If another call to add
	 * devices is made the new collection of devices will be sorted by level and
	 * added to the end of the main list of devices.
	 *
	 * So for:
	 * <code>
	 * manager.add(devices1[])
	 * manager.add(devices2[])
	 * </code>
	 * The notification order will be all the devices1, by level then all the devices2
	 * by level. So the overall order is by add order followed by level. This allows
	 * for instance all devices of a given type to be notified by level before another
	 * group of objects of another type. If no distinction of type is required, simply
	 * add all devices in one go and they will be sorted by level.
	 *
	 * If a device does not implement ILevel its level is assumed to be ILevel.MAXIMUM
	 *
	 * @param devices the devices to add
	 */
	public void addDevices(Collection<?> devices) {
		if (devices != null && !devices.isEmpty()) {
			// Make a copy of the list and sort it
			List<Object> sortedDevices = new ArrayList<>(devices);
			Collections.sort(sortedDevices, new LevelComparator<Object>());
			addOrderedDevices(sortedDevices);
		}
	}

	private void addOrderedDevices(Collection<?> devices) {
		for (Object object : devices) processAnnotations(object);
	}

	private void processAnnotations(Object device) {
		if (device==null) return;

		for (Method method : device.getClass().getMethods()) {
			for (Annotation annotation : method.getAnnotations()) {
				final Class<? extends Annotation> annotationType = annotation.annotationType();
				if (annotations.contains(annotationType)) {
					annotationMap.computeIfAbsent(annotationType, klass -> new ArrayList<>());
					final Collection<MethodWrapper> methodWrappers = annotationMap.get(annotationType);
					methodWrappers.add(new MethodWrapper(annotationType, device, method));
				}
			}
		}
	}

	/**
	 * Notify the methods with this annotation that it happened.
	 * Optionally provide some context which the system will try to insert into the
	 * argument list when it is called.
	 *
	 * @param annotation like &#64;ScanStart etc.
	 * @param context extra things like ScanInformation, IPosition etc.
	 * @throws ScanningException if an error occurred invoking the annotated methods
	 */
	public void invoke(Class<? extends Annotation> annotation, Object... context) throws ScanningException {
		try {
			logger.trace("Invoking methods annotated with {} with context: {}", annotation, context);
			final Collection<MethodWrapper> methodWrappers = annotationMap.get(annotation);
			if (methodWrappers != null) {
				for (MethodWrapper methodWrapper : methodWrappers) {
					logger.trace("Invoking on {} in invoke({},{})", methodWrapper.instance, annotation, context);
					methodWrapper.invoke(context);
					logger.trace("Invoked  on {} in invoke({},{})", methodWrapper.instance, annotation, context);
				}
			}
		} catch (InvocationTargetException e) {
			logger.error("Exception in invoke({},{}): ", annotation, context, e);
		    Throwable wrapped = e.getTargetException();
			if (wrapped instanceof ScanningException) {
				throw (ScanningException) wrapped;
			}
		    throw new ScanningException(e);
		} catch (IllegalAccessException | IllegalArgumentException e) {
		    throw new ScanningException(e);
		} finally {
			logger.trace("Completed invoke({}, {})", annotation, context);
		}
	}

	private class MethodWrapper {

		private final Object instance;
		private final Method method;
		private final List<Class<?>> argClasses;
		private final Object[] arguments; // Must be object[] for speed and is not variable

		MethodWrapper(final Class<? extends Annotation> aclass, Object instance, Method method) throws IllegalArgumentException {
			this.instance = instance;
			this.method = method;

			this.argClasses = Arrays.asList(method.getParameterTypes());
			 // We do not allow duplications in the classes list because a given service or
			 // information object should be required once. Type is used to determine argument
			 // position as well, therefore duplicates do not work with the current algorithm
			if (argClasses.stream().distinct().count() < argClasses.size())
		    	throw new IllegalArgumentException("Duplicated types are not allowed in injected methods!\n"
		    			+ "Your annotation of @"+aclass.getSimpleName()+" sits over a method '"+method.getName()+"' on class '"+instance.getClass().getSimpleName()+"' with duplicated types!\n"
		    			+ "More than one of any given type is not allowed. Have you seen '"+ScanInformation.class.getSimpleName()+"' class, which can be used to provide various metrics about the scan?");

		    this.arguments = this.argClasses.stream()
		    					.map(arg -> getService(arg)).toArray();
		}

		public void invoke(Object... objects) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (arguments.length == 0) {
				method.invoke(instance);
			} else {
				// Put the context into the args (if there are any)
				final List<Object> context = getContext(objects);
				for (int i = 0; i < context.size(); i++) {
					final Collection<Class<?>> classes = getCachedClasses(context.get(i));

					// Find the first class in classes which is in argClasses
					// NOTE this is why duplicates are not supported, type of argument used to map to injected class.
					final Optional<Class<?>> contained = classes.stream().filter(x -> argClasses.contains(x)).findFirst();
					if (contained.isPresent()) {
						final int index = argClasses.indexOf(contained.get());
						arguments[index] = context.get(i);
					}
				}
				boolean accessible = method.isAccessible();
				try {
					method.setAccessible(true);
					method.invoke(instance, arguments);
				} finally {
					method.setAccessible(accessible);
				}
			}
		}
	}

	/**
	 * TODO Cache for speed?
	 * @param object
	 * @return
	 */
	private Collection<Class<?>> getCachedClasses(Object object) {

		final Class<?> clazz = object.getClass();
		if (cachedClasses.containsKey(clazz)) return cachedClasses.get(clazz);

		final Collection<Class<?>> classes = new HashSet<>();
		Class<?> superClass = clazz;
		while (!superClass.equals(Object.class)) {
			classes.add(superClass);
			classes.addAll(Arrays.asList(superClass.getInterfaces()));
			superClass = superClass.getSuperclass();
		}

		cachedClasses.put(clazz, classes);

		return classes;
	}

	public List<Object> getContext(Object[] objects) {
		List<Object> context = new ArrayList<>();
		if (extraContext!=null) context.addAll(extraContext);
		if (objects!=null && objects.length>0) {
			for (Object object : objects) {
				if (object != null) {
					context.add(object);
				}
			}
		}
		return context;
	}

	/**
	 * @return true if item was added, false if there was a problem
	 * @param object
	 */
	public boolean addContext(Object object) {
		if (object==null) {
			logger.info("Null object context accidentally added to "+getClass().getSimpleName());
			return false;
		}
		if (extraContext == null) extraContext = new HashSet<>();
		return extraContext.add(object);
	}

	/**
	 *
	 * @param object
	 */
	public void removeContext(Object object) {
		if (extraContext == null) return;
		extraContext.remove(object);
	}

	private Object getService(Class<?> class1) {
		Object object=null;
		if (serviceResolver!=null) object = serviceResolver.getService(class1);
		if (object==null) object = services.get(class1);
		return object;
	}

	public void dispose() {
		annotationMap.clear();
		cachedClasses.clear();
		if (extraContext!=null) extraContext.clear();
	}
}

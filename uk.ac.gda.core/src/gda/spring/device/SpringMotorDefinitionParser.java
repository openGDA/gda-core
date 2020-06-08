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

package gda.spring.device;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gda.configuration.properties.LocalProperties;
import gda.device.scannable.ScannableMotor;
import gda.spring.namespaces.gda.GdaNamespaceHandler;

/**
 * Bean parser to convert a custom xml element into a ScannableMotor and mode specific motor
 * controller pair.
 * This is intended to remove the need to add three bean definitions in three places when adding
 * a motor to a GDA server configuration. Where previously a motor may have been created using
 *
 * <pre>
 * {@code
 * <!-- in common config -->
 * <bean id="stage_x" class="gda.device.scannable.ScannableMotor" >
 *     <property name="motor" ref="stage_x_motor" />
 * </bean>
 *
 * <!-- in live config -->
 * <bean id="stage_x_motor" class="gda.device.motor.EpicsMotor" >
 *     <property name="pvName" value="BL22I-MO-STABL-01:X" />
 * </bean>
 *
 * <!-- in dummy config -->
 * <bean id="stage_x_motor" class="gda.device.motor.DummyMotor" />
 * }</pre>
 *
 * It can now be replaced by a single declaration in common config (where gda and motor are the
 * configured namespace mappings. See {@link GdaNamespaceHandler}).
 *
 * <pre>{@code <gda:motor id="stage_x" live-pvName="BL22I-MO-STABL-01:X" />}</pre>
 *
 * Properties to be set on the constructed motors should be set using attributes. If an attribute name
 * is prefixed by the mode (and hyphen) it will only be used in that mode. Any attributes that are not
 * ScannableMotor properties will be set on the wrapped controller implementation.
 * <p>
 * The implementation for the controller can be given using top level attributes of the form
 * {@code modeClass} (note: not hyphenated to avoid the class being set as a property). The default
 * classes for different modes can be set using properties where the property used is
 * {@value #DEFAULT_MOTOR_CLASS_PROPERTY_BASE} with the mode appended. The most common modes (live and dummy)
 * have fallback values to reduce required configuration (see FALLBACK_MOTOR_CONTROLLERS).
 * <p>
 * A motor can copy whatever the default is for another mode by passing a value of {@code #mode} to
 * the class attribute, eg {@code liveClass="#dummy"} will create a dummy motor in live mode without
 * using whatever class is default for dummy motors.
 * <h2>Examples:</h2>
 * <dl>
 * <dt>A basic configuration suitable for most cases:</dt>
 * <dd><pre>{@code <gda:motor id="stage_x" live-pvName="BL22I-MO-STABL-01:X" />}</pre>
 * This will create a ScannableMotor that delegates to an EpicsMotor in live mode (using the given PV) and
 * a DummyMotor in dummy mode.
 * </dd>
 * <dt>A more complete example with mode specific values</dt>
 * <dd><pre>{@code
 * <gda:motor id="stage_x" live-pvName="BL22I-MO-STABL-01:X"
 *         scalingFactor="1.2"
 *         dummyClass="gda.device.motor.ThreadlessDummyMotor"
 *         dummy-speed="23.3"
 *         dummy-maxSpeed="32"
 *         live-missedTargetLevel="WARN"
 *         live-userOffset="1.23"/>
 * }</pre>
 * </dd>
 * <dt>A motor that is a dummy motor in both live and dummy mode</dt>
 * <dd><pre>{@code <gda:motor id="stage_x" liveClass="#dummy" />}</pre></dd>
 * </dl>
 */
public class SpringMotorDefinitionParser implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(SpringMotorDefinitionParser.class);
	/** Suffix given to the name of the mode specific implementation of motor controller */
	private static final String MODE_MOTOR_EXTENSION = "_motor";
	/** Base of property used to determine default motor controller implementation - mode is added */
	public static final String DEFAULT_MOTOR_CLASS_PROPERTY_BASE = "gda.spring.device.default.motor.";

	/** Attribute name for the name of created beans */
	private static final String ID_ATTRIBUTE = "id";

	// Required property names that need to be set on the created beans
	/** The name of the property to set the name of the motor */
	private static final String NAME_PROPERTY = "name";
	/** The name of the property to set the mode specific motor controller */
	private static final String MOTOR_PROPERTY = "motor";

	// Optional mode specific settings
	/** Optional properties that can be set on the scannable motor */
	private static final Collection<String> MOTOR_OPTIONS = new ArrayList<>();

	/** Properties that should be set on both scannable and controller */
	private static final Collection<String> DUPLICATED_OPTIONS = new ArrayList<>();

	/** For the common modes provide fallback classes for when properties aren't set */
	private static final Map<String, String> FALLBACK_MOTOR_CONTROLLERS = new HashMap<>();

	/** The Class to use for the top level scannable motor. This is not customisable */
	private static final Class<?> DEFAULT_MOTOR_CLASS = ScannableMotor.class;

	static {
		MOTOR_OPTIONS.add("userUnits");
		MOTOR_OPTIONS.add("configureAtStartup");
		MOTOR_OPTIONS.add("hardwareUnitString");
		MOTOR_OPTIONS.add("offset");
		MOTOR_OPTIONS.add("outputFormat");
		MOTOR_OPTIONS.add("scalingFactor");
		MOTOR_OPTIONS.add("upperGdaLimits");
		MOTOR_OPTIONS.add("lowerGdaLimits");
		MOTOR_OPTIONS.add("protectionLevel");

		DUPLICATED_OPTIONS.add("protectionLevel"); // needs to be in both lists for now - see DAQ-2750

		FALLBACK_MOTOR_CONTROLLERS.put("live", "gda.device.motor.EpicsMotor");
		FALLBACK_MOTOR_CONTROLLERS.put("dummy", "gda.device.motor.DummyMotor");
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Attributes attrs = new Attributes(element);

		String motorName = getMotorName(attrs);
		String modeMotorName = motorName + MODE_MOTOR_EXTENSION;

		logger.debug("Creating motor instances for {}", motorName);
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		Resource resource = parserContext.getReaderContext().getResource();

		BeanDefinition modeMotor = createController(modeMotorName, attrs, resource);
		BeanDefinition commonMotor = createScannable(motorName, attrs, resource);
		commonMotor.getPropertyValues()
				.add(MOTOR_PROPERTY, new RuntimeBeanReference(modeMotorName));
		registry.registerBeanDefinition(modeMotorName, modeMotor);
		registry.registerBeanDefinition(motorName, commonMotor);
		return null;
	}

	/** Check the element has specified an ID (and return it) */
	private String getMotorName(Attributes attributes) {
		return attributes.get(ID_ATTRIBUTE)
				.filter(id -> !id.isEmpty())
				.orElseThrow(() -> new IllegalStateException("No " + ID_ATTRIBUTE + " specified"));
	}

	/**
	 * Create the mode specific implementation of motor controller.
	 * <p>
	 * Create an epics motor connected to a given PV or a dummy motor if a pv is not specified.
	 * In live mode the {@value #PV_ATTRIBUTE} attribute is checked. In dummy mode, {@value #DUMMY_PV_ATTRIBUTE}
	 * is used instead. This allows dummy motors to be created in live mode and for dummy installations to
	 * use (simulated) epics motors if required.
	 * <p>
	 * The class used for each mode can be specified using the {@value #DUMMY_CLASS_ATTRIBUTE} and
	 * {@value #LIVE_CLASS_ATTRIBUTE} attributes but default to {@value #DEFAULT_DUMMY_CLASS} and
	 * {@value #DEFAULT_LIVE_CLASS} respectively.
	 * @param name of the motor controller to create
	 * @param attributes of element being parsed
	 * @param uri of the file being parsed - useful for debugging elsewhere
	 * @return A bean definition detailing the mode specific motor.
	 */
	private BeanDefinition createController(String name, Attributes attributes, Resource uri) {
		GenericBeanDefinition motor = new GenericBeanDefinition();
		motor.setBeanClassName(attributes.getClassName());
		MutablePropertyValues props = new MutablePropertyValues()
				.add(NAME_PROPERTY, name);
		for (Entry<String, String> entry : attributes.modeAttributes().entrySet()) {
			props.add(entry.getKey(), entry.getValue());
		}
		for (String option : DUPLICATED_OPTIONS) {
			setOptional(props, attributes, option);
		}
		motor.setPropertyValues(props);
		if (uri != null) motor.setResource(uri);
		return motor;
	}

	/**
	 * Create the top level scannable motor definition
	 * @param name for the motor
	 * @param attributes of element being parsed
	 * @param uri of the file being parsed - useful for debugging elsewhere
	 * @return Definition for scannable motor
	 */
	private GenericBeanDefinition createScannable(String name, Attributes attributes, Resource uri) {
		GenericBeanDefinition commonMotor = new GenericBeanDefinition();
		commonMotor.setBeanClass(DEFAULT_MOTOR_CLASS);
		MutablePropertyValues props = new MutablePropertyValues()
				.add(NAME_PROPERTY, name);

		for (String option : MOTOR_OPTIONS) {
			setOptional(props, attributes, option);
		}

		commonMotor.setPropertyValues(props);
		if (uri != null) commonMotor.setResource(uri);
		return commonMotor;
	}

	/**
	 * If the given attribute is present in the element, set it as a property.
	 * Does nothing if the attribute is not present.
	 * @param props Properties to add this attribute to
	 * @param element being parsed
	 * @param att The attribute to look for
	 */
	private void setOptional(MutablePropertyValues props, Attributes attributes, String att) {
		attributes.get(att).ifPresent(value -> props.add(att, value));
	}

	/**
	 * Wrapper around an NamedNodeMap of xml attributes to allow calling code to get attributes
	 * without handling the current mode.
	 */
	private class Attributes {

		/** The mode gda is currently using */
		private final String mode = LocalProperties.get("gda.mode");//, "duumy");
		/** The prefix used by mode specific keys */
		private final String prefix = mode + "-";
		/** The length of the {@link #prefix} */
		private final int prefixLength = prefix.length();
		/** Check if a string starts with "mode-" */
		private final Predicate<Entry<String, String>> hasPrefix = e -> e.getKey().startsWith(prefix);
		/** Removes "mode-" from the start of an entry's key */
		private final Function<Entry<String, String>, String> stripKey = e -> e.getKey().substring(prefixLength);

		/** The key-value pairs as taken from the xml element includes values for all modes */
		private final Map<String, String> rawAttributes;

		/** Wrap an xml element's attributes in a mode specific view */
		private Attributes(Element element) {
			NamedNodeMap attributes = element.getAttributes();
			rawAttributes = range(0, attributes.getLength())
					.mapToObj(attributes::item)
					.collect(toMap(Node::getLocalName, Node::getNodeValue));
		}

		/** Get the value of the given key in the current mode */
		private Optional<String> get(String key) {
			if (rawAttributes.containsKey(prefix + key)) {
				return Optional.of(rawAttributes.get(prefix + key));
			} else if (rawAttributes.containsKey(key)) {
				return Optional.of(rawAttributes.get(key));
			}
			return Optional.empty();
		}

		/**
		 * Get a map of key-value pairs for the current mode - excludes keys in
		 * {@link SpringMotorDefinitionParser#MOTOR_OPTIONS MOTOR_OPTIONS}
		 */
		private Map<String, String> modeAttributes() {
			return rawAttributes.entrySet().stream()
					.filter(hasPrefix)
					.filter(e -> !MOTOR_OPTIONS.contains(stripKey.apply(e)))
					.collect(toMap(stripKey, Entry::getValue));
		}

		private String defaultMotorControllerClass(String mode) {
			return LocalProperties.get(DEFAULT_MOTOR_CLASS_PROPERTY_BASE + mode,
					FALLBACK_MOTOR_CONTROLLERS.get(mode));
		}

		private String classNameFor(String mode, Set<String> visited) {
			String className = rawAttributes.getOrDefault(mode + "Class", defaultMotorControllerClass(mode));
			if (className == null || className.isEmpty()) {
				throw new IllegalArgumentException("No class specified for mode: " + mode);
			}
			if (className.startsWith("#")) {
				if (visited.add(mode)) {
					className = classNameFor(className.substring(1), visited);
				} else {
					throw new IllegalStateException("Circular reference in motor class: " +
						visited.stream().collect(joining(" -> ", "", " -> " + mode)));
				}
			}
			return className;
		}

		private String getClassName() {
			return classNameFor(mode, new LinkedHashSet<>());
		}
	}
}

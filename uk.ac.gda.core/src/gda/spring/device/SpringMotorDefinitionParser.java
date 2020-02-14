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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
 *     {@code
 *     <!-- in common config -->
 *     <bean id="stage_x" class="gda.device.scannable.ScannableMotor" >
 *         <property name="motor" ref="stage_x_motor" />
 *     </bean>
 *
 *     <!-- in live config -->
 *     <bean id="stage_x_motor" class="gda.device.motor.EpicsMotor" >
 *         <property name="pvName" value="BL22I-MO-STABL-01:X" />
 *     </bean>
 *
 *     <!-- in dummy config -->
 *     <bean id="stage_x_motor" class="gda.device.motor.DummyMotor" />
 * }</pre>
 *
 * It can now be replaced by a single declaration in common config (where gda and motor are the
 * configured namespace mappings. See {@link GdaNamespaceHandler}).
 *
 * <pre>
 *     {@code
 *     <gda:motor id="stage_x" pv="BL22I-MO-STABL-01:X" />
 * }</pre>
 *
 * By default when the server is started in dummy mode, a dummy motor controller is created.
 * In live mode, a live motor is created. The classes used can be customised using
 * dummy and live class attributes respectively.<br>
 * If a pv is not specified, a dummy motor will be created in both live and dummy mode.<br>
 * If a live motor is required when running in dummy mode, the dummy pv attribute
 * can be specified.
 * <p>
 * Further customisation of the motor and controller instances can be made using additional
 * attributes with the properties being set on the scannable or controller as appropriate.
 */
public class SpringMotorDefinitionParser implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(SpringMotorDefinitionParser.class);
	/** Suffix given to the name of the mode specific implementation of motor controller */
	private static final String MODE_MOTOR_EXTENSION = "_motor";

	/** A property to configure the default dummy motor class to use if none is specified */
	public static final String DEFAULT_DUMMY_MOTOR_PROPERTY = "gda.spring.device.defaults.motor.dummy";
	/** A property to configure the default live motor class to use if none is specified */
	public static final String DEFAULT_LIVE_MOTOR_PROPERTY = "gda.spring.device.defaults.motor.live";

	// Attributes that should be read from the parsed element
	/**
	 * The attribute holding the id of the motor to be created.
	 * This value is used as the motor name and the base for the controller name
	 */
	private static final String MOTOR_NAME_ATTRIBUTE = "id";
	/** Optional attribute for specifying the class of dummy motor to use */
	private static final String DUMMY_CLASS_ATTRIBUTE = "dummyImplementation";
	/** Optional attribute for specifying the class of live motor to use */
	private static final String LIVE_CLASS_ATTRIBUTE = "liveImplementation";
	/** Optional attribute for specifying the PV to use in live mode */
	private static final String PV_ATTRIBUTE = "pv";
	/** Optional attribute for specifying the PV to use in dummy mode */
	private static final String DUMMY_PV_ATTRIBUTE = "dummy-pv";

	// Required property names that need to be set on the created beans
	/** The name of the property to set the name of the motor */
	private static final String NAME_PROPERTY = "name";
	/** The name of the property to set the pv for motor controllers */
	private static final String PV_PROPERTY = "pvName"; // only needed for live classes
	/** The name of the property to set the mode specific motor controller */
	private static final String MOTOR_PROPERTY = "motor";

	// Optional mode specific settings
	/** Optional properties that can be set on the controller instance */
	private static final Collection<String> CONTROLLER_OPTIONS = new ArrayList<>();

	/** Optional properties that can be set on the scannable motor */
	private static final Collection<String> MOTOR_OPTIONS = new ArrayList<>();

	// Default values for things that need to be set
	/** The Class to use for the top level scannable motor. This is not customisable */
	private static final Class<?> DEFAULT_MOTOR_CLASS = ScannableMotor.class;
	/** The FQCN of the class to use for dummy motors when no class is specified */
	private static final String FALLBACK_DUMMY_CLASS = "gda.device.motor.DummyMotor";
	/** The FQCN of the class to use for live motors when no class is specified */
	private static final String FALLBACK_LIVE_CLASS = "gda.device.motor.EpicsMotor";

	/** The dummy motor implementation used if none is specified */
	private final String defaultDummyClass = LocalProperties.get(DEFAULT_DUMMY_MOTOR_PROPERTY, FALLBACK_DUMMY_CLASS);
	/** The live motor implementation used if none is specified */
	private final String defaultLiveClass = LocalProperties.get(DEFAULT_LIVE_MOTOR_PROPERTY, FALLBACK_LIVE_CLASS);

	/** True if this server is running in dummy mode */
	private final boolean dummy = LocalProperties.isDummyModeEnabled();

	static {
		MOTOR_OPTIONS.add("userUnits");
		MOTOR_OPTIONS.add("configureAtStartup");
		MOTOR_OPTIONS.add("hardwareUnitString");
		MOTOR_OPTIONS.add("offset");
		MOTOR_OPTIONS.add("outputFormat");
		MOTOR_OPTIONS.add("scalingFactor");
		MOTOR_OPTIONS.add("upperGdaLimits");
		MOTOR_OPTIONS.add("lowerGdaLimits");
		MOTOR_OPTIONS.add("protectionLevel"); // needs to be in both lists for now - see DAQ-2750

		CONTROLLER_OPTIONS.add("speed");
		CONTROLLER_OPTIONS.add("timeToVelocity");
		CONTROLLER_OPTIONS.add("protectionLevel");
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String motorName = getMotorName(element);
		String modeMotorName = motorName + MODE_MOTOR_EXTENSION;


		logger.debug("Creating motor instances for {}", motorName);
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		Resource resource = parserContext.getReaderContext().getResource();
		BeanDefinition modeMotor = createController(modeMotorName, element, resource);
		BeanDefinition commonMotor = createScannable(motorName, element, resource);
		commonMotor.getPropertyValues()
				.add(MOTOR_PROPERTY, new RuntimeBeanReference(modeMotorName));
		registry.registerBeanDefinition(modeMotorName, modeMotor);
		registry.registerBeanDefinition(motorName, commonMotor);
		return null;
	}

	/** Check the element has specified an ID (and return it) */
	private String getMotorName(Element bean) {
		return attribute(bean, MOTOR_NAME_ATTRIBUTE)
				.filter(id -> !id.isEmpty())
				.orElseThrow(() -> new IllegalStateException("No " + MOTOR_NAME_ATTRIBUTE + " specified"));
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
	 * @param element being parsed
	 * @param uri of the file being parsed - useful for debugging elsewhere
	 * @return A bean definition detailing the mode specific motor.
	 */
	private BeanDefinition createController(String name, Element element, Resource uri) {
		GenericBeanDefinition motor = new GenericBeanDefinition();
		Optional<String> pv = attribute(element, dummy ? DUMMY_PV_ATTRIBUTE : PV_ATTRIBUTE);
		MutablePropertyValues props = getProperties(element, name, CONTROLLER_OPTIONS);
		if (pv.isPresent()) {
			motor.setBeanClassName(attribute(element, LIVE_CLASS_ATTRIBUTE).orElse(defaultLiveClass));
			props.add(PV_PROPERTY, pv.get());
		} else {
			motor.setBeanClassName(attribute(element, DUMMY_CLASS_ATTRIBUTE).orElse(defaultDummyClass));
		}
		motor.setPropertyValues(props);
		if (uri != null) motor.setResource(uri);
		return motor;
	}

	/**
	 * Create the top level scannable motor definition
	 * @param name for the motor
	 * @param element being parsed
	 * @param uri of the file being parsed - useful for debugging elsewhere
	 * @return Definition for scannable motor
	 */
	private GenericBeanDefinition createScannable(String name, Element element, Resource uri) {
		GenericBeanDefinition commonMotor = new GenericBeanDefinition();
		commonMotor.setBeanClass(DEFAULT_MOTOR_CLASS);
		commonMotor.setPropertyValues(getProperties(element, name, MOTOR_OPTIONS));
		if (uri != null) commonMotor.setResource(uri);
		return commonMotor;
	}

	/** Get the given options as properties for the element being parsed */
	private MutablePropertyValues getProperties(Element element, String name, Collection<String> options) {
		MutablePropertyValues props = new MutablePropertyValues()
				.add(NAME_PROPERTY, name);
		for (String option : options) {
			setOptional(props, element, option);
		}
		return props;
	}

	/**
	 * Get an optional attribute from an element.<p>
	 * If the attribute is not present, return empty instead of an empty string or null.
	 * @param element being parsed
	 * @param name of the attribute to get
	 * @return optional attribute value
	 */
	private static Optional<String> attribute(Element element, String name) {
		// explicitly check for attribute to distinguish from empty string being passed in
		if (element.hasAttribute(name)) {
			return Optional.ofNullable(element.getAttribute(name));
		}
		return Optional.empty();
	}

	/**
	 * If the given attribute is present in the element, set it as a property.
	 * Does nothing if the attribute is not present.
	 * @param props Properties to add this attribute to
	 * @param element being parsed
	 * @param att The attribute to look for
	 */
	private void setOptional(MutablePropertyValues props, Element element, String att) {
		attribute(element, att).ifPresent(value -> props.add(att, value));
	}
}

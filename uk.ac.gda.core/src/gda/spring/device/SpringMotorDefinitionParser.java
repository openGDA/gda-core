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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
	private static final Collection<String> MOTOR_OPTIONS = Set.of(
			"userUnits",
			"configureAtStartup",
			"hardwareUnitString",
			"offset",
			"outputFormat",
			"scalingFactor",
			"upperGdaLimits",
			"lowerGdaLimits",
			"protectionLevel",
			"tolerances",
			"initialUserUnits");

	/** Properties that should be set on both scannable and controller */
	private static final Collection<String> DUPLICATED_OPTIONS = Set.of("protectionLevel");

	private static final Collection<String> NON_PROPERTIES = Set.of("class", "id");

	/** For the common modes provide fallback classes for when properties aren't set */
	private static final Map<String, String> FALLBACK_MOTOR_CONTROLLERS = Map.of(
			"live", "gda.device.motor.EpicsMotor",
			"dummy", "gda.device.motor.DummyMotor"
	);

	/** The Class to use for the top level scannable motor. This is not customisable */
	private static final Class<?> DEFAULT_MOTOR_CLASS = ScannableMotor.class;

	/** Class provider that uses the current mode to determine the class for a bean */
	private static final BeanClassProvider CLASS_PROVIDER = new BeanClassProvider(DEFAULT_MOTOR_CLASS_PROPERTY_BASE, FALLBACK_MOTOR_CONTROLLERS);

	/**
	 * Motors need to be configured before they can be used. If a motor is not being
	 * created in the main namespace (see {@link #nestedBeans}), the configure method is
	 * not called automatically so use Spring's init-method.
	 */
	private static final String INIT_METHOD = "configure";

	/** If true, return the bean definition instead of registering with the bean registry */
	private boolean nestedBeans;

	public SpringMotorDefinitionParser(boolean nest) {
		this.nestedBeans = nest;
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		var attrs = BeanAttributes.from(element);

		String motorName = getMotorName(attrs);
		String modeMotorName = motorName + MODE_MOTOR_EXTENSION;

		logger.debug("Creating motor instances for {}", motorName);
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		var resource = parserContext.getReaderContext().getResource();

		BeanDefinition modeMotor = createController(modeMotorName, attrs, resource);
		BeanDefinition commonMotor = createScannable(motorName, attrs, resource);


		if (nestedBeans) {
			modeMotor.setInitMethodName(INIT_METHOD);
			commonMotor.setInitMethodName(INIT_METHOD);
			commonMotor.getPropertyValues().add(MOTOR_PROPERTY, modeMotor);
			return commonMotor;
		} else {
			commonMotor.getPropertyValues()
					.add(MOTOR_PROPERTY, new RuntimeBeanReference(modeMotorName));
			registry.registerBeanDefinition(modeMotorName, modeMotor);
			registry.registerBeanDefinition(motorName, commonMotor);
			return null;
		}
	}

	/** Check the element has specified an ID (and return it) */
	private String getMotorName(BeanAttributes attributes) {
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
	private BeanDefinition createController(String name, BeanAttributes attributes, Resource uri) {
		var motor = new GenericBeanDefinition();
		motor.setBeanClassName(CLASS_PROVIDER.forCurrentMode(attributes));
		var props = new MutablePropertyValues()
				.add(NAME_PROPERTY, name);
		attributes.modeProperties()
				.filter(p -> !MOTOR_OPTIONS.contains(p.getKey()) // don't set the scannable properties
						|| DUPLICATED_OPTIONS.contains(p.getKey())) // unless they're also controller properties
				.filter(p -> !NON_PROPERTIES.contains(p.getKey())) // don't set id/class as property
				.forEach(p -> props.add(p.getKey(), p.getValue()));
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
	private GenericBeanDefinition createScannable(String name, BeanAttributes attributes, Resource uri) {
		var commonMotor = new GenericBeanDefinition();
		commonMotor.setBeanClass(DEFAULT_MOTOR_CLASS);
		var props = new MutablePropertyValues()
				.add(NAME_PROPERTY, name);
		attributes.modeProperties()
				.filter(p -> MOTOR_OPTIONS.contains(p.getKey()))
				.forEach(p -> props.add(p.getKey(), p.getValue()));

		commonMotor.setPropertyValues(props);
		if (uri != null) commonMotor.setResource(uri);
		return commonMotor;
	}
}

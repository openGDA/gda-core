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

import static gda.spring.device.SpringMotorDefinitionParser.DEFAULT_DUMMY_MOTOR_PROPERTY;
import static gda.spring.device.SpringMotorDefinitionParser.DEFAULT_LIVE_MOTOR_PROPERTY;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import gda.configuration.properties.LocalProperties;
import gda.device.scannable.ScannableMotor;
import uk.ac.diamond.daq.test.powermock.PowerMockBase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ParserContext.class, LocalProperties.class, XmlReaderContext.class})
public class SpringMotorDefinitionParserTest extends PowerMockBase {
	private static final String EPICS_MOTOR = "gda.device.motor.EpicsMotor";
	private static final String DUMMY_MOTOR = "gda.device.motor.DummyMotor";

	private BeanDefinitionParser parser;

	@Mock Element element;
	@Mock ParserContext context;
	@Mock BeanDefinitionRegistry registry;
	@Mock XmlReaderContext readerContext;

	@Captor private ArgumentCaptor<String> names;
	@Captor private ArgumentCaptor<BeanDefinition> beans;

	private Map<String, String> attrs = new HashMap<>();

	@Before
	public void setup() {
		when(context.getRegistry()).thenReturn(registry);
		when(context.getReaderContext()).thenReturn(readerContext);
	}

	@Test
	public void testParseReturnsNull() {
		initLive();
		set("id", "stage");
		set("pv", "pvValue");
		assertThat(parse(), is(nullValue()));
	}

	@Test(expected = IllegalStateException.class)
	public void elementNeedsIdAttribute() {
		initLive();
		parse();
	}

	@Test(expected = IllegalStateException.class)
	public void idAttributeMustNotBeEmpty() {
		initLive();
		set("id", "");
		parse();
		parser.parse(element, context);
	}

	@Test
	public void resourceShouldBeSetIfAvailable() {
		Resource testResource = new PathResource("/dev/null");
		when(readerContext.getResource()).thenReturn(testResource);
		initDummy();
		set("id", "stage");
		parse();
		verifyRegisteredBean(0, matchesController("stage_motor", DUMMY_MOTOR)
				.withResource(testResource));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor")
				.withResource(testResource));
	}

	@Test // the happy live path
	public void normalLiveUsageWorksAsExpected() {
		initLive();
		set("id", "stage");
		set("pv", "pvValue");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", EPICS_MOTOR)
				.withPv("pvValue"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test // the happy dummy path
	public void normalDummyUsageWorksAsExpected() {
		initDummy();
		set("id", "stage");
		set("pv", "pvValue");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", DUMMY_MOTOR));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void dummyMotorInLiveMode() {
		initLive();
		set("id", "stage");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", DUMMY_MOTOR));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void liveMotorInDummyMode() {
		initDummy();
		set("id", "stage");
		set("pv", "pvValue");
		set("dummy-pv", "dummyPV");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", EPICS_MOTOR)
				.withPv("dummyPV"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void fullyCustomisedDummy() {
		initDummy();
		set("id", "stage");
		set("pv", "pvValue");
		set("userUnits", "um");
		set("configureAtStartup", "false");
		set("hardwareUnitString", "m");
		set("offset", "12.4");
		set("outputFormat", "%.3f");
		set("scalingFactor", "3.2");
		set("upperGdaLimits", "17.4");
		set("lowerGdaLimits", "-3.21");
		set("speed", "42");
		set("timeToVelocity", "1.2");
		set("protectionLevel", "3");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", DUMMY_MOTOR)
				.withSpeed("42")
				.withProtection("3")
				.withTimeToVelocity("1.2"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor")
				.withUserUnits("um")
				.withConfigureAtStartup("false")
				.withHardwareUnitString("m")
				.withOffset("12.4")
				.withOutputFormat("%.3f")
				.withScalingFactor("3.2")
				.withUpperGdaLimits("17.4")
				.withLowerGdaLimits("-3.21")
				.withProtection("3"));
	}

	@Test
	public void fullyCustomisedLive() {
		initLive();
		set("id", "stage");
		set("pv", "pvValue");
		set("userUnits", "um");
		set("configureAtStartup", "false");
		set("hardwareUnitString", "m");
		set("offset", "12.4");
		set("outputFormat", "%.3f");
		set("scalingFactor", "3.2");
		set("upperGdaLimits", "17.4");
		set("lowerGdaLimits", "-3.21");
		set("speed", "42");
		set("timeToVelocity", "1.2");
		set("protectionLevel", "3");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", EPICS_MOTOR)
				.withPv("pvValue")
				.withSpeed("42")
				.withProtection("3")
				.withTimeToVelocity("1.2"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor")
				.withUserUnits("um")
				.withConfigureAtStartup("false")
				.withHardwareUnitString("m")
				.withOffset("12.4")
				.withOutputFormat("%.3f")
				.withScalingFactor("3.2")
				.withUpperGdaLimits("17.4")
				.withLowerGdaLimits("-3.21")
				.withProtection("3"));
	}

	@Test
	public void customDummyClass() {
		initDummy();
		set("id", "stage");
		set("pv", "pvValue");
		String customClass = "made.up.motor.class";
		set("dummyImplementation", customClass);
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", customClass));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void customDummyClassWithDummyPv() {
		initDummy();
		set("id", "stage");
		set("pv", "pvValue");
		set("dummy-pv", "dummyPV");
		String customClass = "made.up.motor.class";
		set("dummyImplementation", customClass);
		parse();

		// Shoudn't use custom dummy class even in dummy mode if pv is specified
		verifyRegisteredBean(0, matchesController("stage_motor", EPICS_MOTOR)
				.withPv("dummyPV"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void customLiveClass() {
		initLive();
		set("id", "stage");
		set("pv", "pvValue");
		String customClass = "made.up.motor.class";
		set("liveImplementation", customClass);
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", customClass)
				.withPv("pvValue"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void customLiveClassWithoutPv() {
		initLive();
		set("id", "stage");
		String customClass = "made.up.motor.class";
		set("liveImplementation", customClass);
		parse();

		// Without a PV it should still be a dummy motor
		verifyRegisteredBean(0, matchesController("stage_motor", DUMMY_MOTOR));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void customDummyClassProperty() {
		String customClass = "made.up.default.motor";
		init(true, null, customClass);
		set("id", "stage");
		set("pv", "pvValue");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", customClass));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	@Test
	public void customLiveClassProperty() {
		String customClass = "made.up.default.motor";
		init(false, customClass, null);
		set("id", "stage");
		set("pv", "pvValue");
		parse();

		verifyRegisteredBean(0, matchesController("stage_motor", customClass)
				.withPv("pvValue"));
		verifyRegisteredBean(1, matchesMotor("stage", "stage_motor"));
	}

	private void set(String key, String value) {
		attrs.put(key, value);
	}

	private BeanDefinition parse() {
		setupElement(attrs);
		BeanDefinition bean = parser.parse(element, context);
		verify(registry, times(2)).registerBeanDefinition(names.capture(), beans.capture());
		return bean;
	}

	private void verifyRegisteredBean(int index, BeanMatcher matches) {
		String actualName = names.getAllValues().get(index);
		BeanDefinition def = beans.getAllValues().get(index);
		assertThat(actualName, is(matches.name));
		assertThat(def, matches);
	}

	private void setupElement(Map<String, String> attributes) {
		when(element.hasAttribute(anyString()))
				.thenAnswer(inv -> attributes.containsKey(inv.getArgumentAt(0, String.class)));
		when(element.getAttribute(anyString()))
				.thenAnswer(inv -> attributes.getOrDefault(inv.getArgumentAt(0, String.class), ""));
	}

	private void initDummy() {
		init(true, null, null);
	}

	private void initLive() {
		init(false, null, null);
	}

	private void init(boolean dummyMode, String live, String dummy) {
		PowerMockito.mockStatic(LocalProperties.class, RETURNS_SMART_NULLS);
		when(LocalProperties.isDummyModeEnabled()).thenReturn(dummyMode);
		when(LocalProperties.get(eq(DEFAULT_DUMMY_MOTOR_PROPERTY), anyString())).then(answerProperty(dummy));
		when(LocalProperties.get(eq(DEFAULT_LIVE_MOTOR_PROPERTY), anyString())).then(answerProperty(live));
		when(LocalProperties.get(anyString())).thenCallRealMethod();
		parser = new SpringMotorDefinitionParser();
	}

	/** Mock the LocalProperties lookup, if the value is null use the given fallback, else use our value */
	private Answer<String> answerProperty(String value) {
		return invocation -> ofNullable(value).orElse(invocation.getArgumentAt(1, String.class));
	}

	private MotorMatcher matchesMotor(String name, String motorName) {
		return new MotorMatcher(name, motorName);
	}

	private ControllerMatcher matchesController(String name, String fqcn) {
		return new ControllerMatcher(name, fqcn);
	}

	/** Bean matching base class - checks name and class are correct. Lets subclasses check properties */
	private abstract class BeanMatcher extends BaseMatcher<BeanDefinition> {
		private final String fqcn;
		private final String name;
		private String resourceDescription;
		private String expected;
		private String actual;

		private MutablePropertyValues props;
		public BeanMatcher(String name, String fqcn) {
			this.name = name;
			this.fqcn = fqcn;
		}
		@Override
		public boolean matches(Object argument) {
			if (!(argument instanceof BeanDefinition)) {
				return false;
			}
			BeanDefinition def = (BeanDefinition)argument;
			props = def.getPropertyValues();
			return matchBean(def) && matchProps();
		}

		protected boolean matchBean(BeanDefinition def) {
			if (!fqcn.equals(def.getBeanClassName())) {
				expected = "class to be " + fqcn;
				actual = "class was " + def.getBeanClassName();
				return false;
			} else {
				String resDesc = def.getResourceDescription();
				if ((resourceDescription == null && resDesc != null) ||
						(resourceDescription != null && !resourceDescription.equals(resDesc))) {
					expected = "resource to be " + resourceDescription;
					actual = "resource was " + resDesc;
					return false;
				}
			}
			return check("name", name);
		}
		protected boolean check(String field, Object expected) {
			Object actual = props.get(field);
			if (match(expected, actual)) {
				return true;
			} else {
				this.expected = "property '" + field + "' to be " + expected;
				this.actual = field + " was " + actual;
				return false;
			}
		}
		@Override
		public void describeMismatch(Object item, Description description) {
			description.appendText(actual);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(expected);
		}
		protected BeanMatcher withResource(Resource res) {
			resourceDescription = requireNonNull(res, "Resource must be non null if specified. Leave unspecified for null")
					.getDescription();
			return this;
		}
		protected abstract boolean matchProps();
	}

	private class ControllerMatcher extends BeanMatcher {
		private String pv;
		private String protectionLevel;
		private String speed;
		private String timeToVelocity;

		public ControllerMatcher(String name, String fqcn) {
			super(name, fqcn);
		}

		@Override
		protected boolean matchProps() {
			return check("pvName", pv)
					&& check("protectionLevel", protectionLevel)
					&& check("speed", speed)
					&& check("timeToVelocity", timeToVelocity);
		}

		private BeanMatcher withTimeToVelocity(String ttv) {
			this.timeToVelocity = ttv;
			return this;
		}

		private ControllerMatcher withPv(String pv) {
			this.pv = pv;
			return this;
		}

		private ControllerMatcher withProtection(String protection) {
			this.protectionLevel = protection;
			return this;
		}

		private ControllerMatcher withSpeed(String speed) {
			this.speed = speed;
			return this;
		}
	}

	private class MotorMatcher extends BeanMatcher {
		private String userUnits;
		private String configureAtStartup;
		private String hardwareUnitString;
		private String offset;
		private String outputFormat;
		private String scalingFactor;
		private String upperGdaLimits;
		private String lowerGdaLimits;
		private String controllerName;
		private String protectionLevel;

		public MotorMatcher(String name, String controllerName) {
			super(name, ScannableMotor.class.getCanonicalName());
			this.controllerName = controllerName;
		}

		@Override
		protected boolean matchProps() {
			return  check("motor", new RuntimeBeanReference(controllerName))
					&& check("userUnits", userUnits)
					&& check("configureAtStartup", configureAtStartup)
					&& check("hardwareUnitString", hardwareUnitString)
					&& check("offset", offset)
					&& check("outputFormat", outputFormat)
					&& check("scalingFactor", scalingFactor)
					&& check("upperGdaLimits", upperGdaLimits)
					&& check("lowerGdaLimits", lowerGdaLimits)
					&& check("protectionLevel", protectionLevel);
		}

		private MotorMatcher withUserUnits(String userUnits) {
			this.userUnits = userUnits;
			return this;
		}

		private MotorMatcher withConfigureAtStartup(String configureAtStartup) {
			this.configureAtStartup = configureAtStartup;
			return this;
		}

		private MotorMatcher withHardwareUnitString(String hardwareUnitString) {
			this.hardwareUnitString = hardwareUnitString;
			return this;
		}

		private MotorMatcher withOffset(String offset) {
			this.offset = offset;
			return this;
		}

		private MotorMatcher withOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

		private MotorMatcher withScalingFactor(String scalingFactor) {
			this.scalingFactor = scalingFactor;
			return this;
		}

		private MotorMatcher withUpperGdaLimits(String upperGdaLimits) {
			this.upperGdaLimits = upperGdaLimits;
			return this;
		}

		private MotorMatcher withLowerGdaLimits(String lowerGdaLimits) {
			this.lowerGdaLimits = lowerGdaLimits;
			return this;
		}

		private BeanMatcher withProtection(String level) {
			protectionLevel = level;
			return this;
		}
	}

	public boolean match(Object left, Object right) {
		return (left == null && right == null) || (left != null && left.equals(right));
	}
}

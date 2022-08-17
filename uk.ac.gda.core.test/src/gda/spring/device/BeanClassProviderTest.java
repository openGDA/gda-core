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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import gda.configuration.properties.LocalProperties;

public class BeanClassProviderTest {
	private static final String GDA_MODE = "gda.mode";

	private static final String CLASS_PROPERTY_PREFIX = "gda.spring.device.test.class.";

	private static final String MODE_X = "mode_x";
	private static final String MODE_Y = "mode_y";
	private static final String MODE_Z = "mode_z";

	// These are almost certainly not set in tests but just in case...
	private static final String INITIAL_X_CLASS = LocalProperties.get(CLASS_PROPERTY_PREFIX + MODE_X, null);
	private static final String INITIAL_Y_CLASS = LocalProperties.get(CLASS_PROPERTY_PREFIX + MODE_Y, null);
	private static final String INITIAL_Z_CLASS = LocalProperties.get(CLASS_PROPERTY_PREFIX + MODE_Z, null);

	private Map<String, String> fallbacks = new HashMap<>();
	private Map<String, String> attributes = new HashMap<>();

	@AfterEach
	public void resetMode() {
		resetMode(MODE_X, INITIAL_X_CLASS);
		resetMode(MODE_Y, INITIAL_Y_CLASS);
		resetMode(MODE_Z, INITIAL_Z_CLASS);
	}

	private static void setClassAsProperty(String mode, String className) {
		LocalProperties.set(CLASS_PROPERTY_PREFIX + mode, className);
	}

	private static void resetMode(String mode, String value) {
		if (value != null) {
			LocalProperties.set(CLASS_PROPERTY_PREFIX + mode, value);
		} else {
			LocalProperties.clearProperty(CLASS_PROPERTY_PREFIX + mode);
		}
	}

	private static String redirectTo(String mode) {
		return "#" + mode;
	}

	private String classForMode(String mode) {
		BeanClassProvider bcp = new BeanClassProvider(CLASS_PROPERTY_PREFIX, fallbacks);
		return bcp.forMode(mode, new BeanAttributes(attributes));
	}

	@Test
	public void classDefinedInProperties() {
		setClassAsProperty(MODE_X, "foo");

		assertThat(classForMode(MODE_X), is("foo"));
	}

	@Test
	public void propertyRedirectsToFallback() {
		setClassAsProperty(MODE_X, redirectTo(MODE_Y));
		fallbacks.put(MODE_Y, "fallback_class");

		assertThat(classForMode(MODE_Y), is("fallback_class"));
	}

	@Test
	public void propertyRedirectsToProperty() {
		setClassAsProperty(MODE_X, redirectTo(MODE_Y));
		setClassAsProperty(MODE_Y, "property_class");

		assertThat(classForMode(MODE_X), is("property_class"));
	}

	@Test
	public void fallbackRedirectsToProperty() {
		fallbacks.put(MODE_X, redirectTo(MODE_Y));
		setClassAsProperty(MODE_Y, "property_class");

		assertThat(classForMode(MODE_X), is("property_class"));
	}

	@Test
	public void multipleRedirects() {
		setClassAsProperty(MODE_X, redirectTo(MODE_Y));
		setClassAsProperty(MODE_Y, redirectTo(MODE_Z));
		setClassAsProperty(MODE_Z, "property_class");

		assertThat(classForMode(MODE_X), is("property_class"));
	}

	@Test
	public void circularReferemceThrows() throws Exception {
		setClassAsProperty(MODE_X, redirectTo(MODE_Y));
		setClassAsProperty(MODE_Y, redirectTo(MODE_Z));
		setClassAsProperty(MODE_Z, redirectTo(MODE_X));

		assertThrows(IllegalStateException.class, () -> classForMode(MODE_X));
	}

	@Test
	public void emptyClassNameIsIgnored() throws Exception {
		setClassAsProperty(MODE_X, "");
		fallbacks.put(MODE_X, "fallback_class");

		assertThat(classForMode(MODE_X), is("fallback_class"));
	}

	@Test
	public void attributesOverrideFallbacks() throws Exception {
		attributes.put(classKey(MODE_X), "att_class");
		fallbacks.put(MODE_X, "fallbacks_class");

		assertThat(classForMode(MODE_X), is("att_class"));
	}

	@Test
	public void propertiesOverrideFallbacks() throws Exception {
		fallbacks.put(MODE_X, "fallbacks_class");
		setClassAsProperty(MODE_X, "property_class");

		assertThat(classForMode(MODE_X), is("property_class"));
	}

	@Test
	public void attributesOverrideProperties() throws Exception {
		attributes.put(classKey(MODE_X), "att_class");
		setClassAsProperty(MODE_X, "fallbacks_class");

		assertThat(classForMode(MODE_X), is("att_class"));
	}

	@Test
	public void fallbackToModeFromProperty() throws Exception {
		String initMode = null;
		try {
			initMode = LocalProperties.get(GDA_MODE);
			LocalProperties.set(GDA_MODE, MODE_X);

			attributes.put(classKey(MODE_X), "mode_x_class");
			attributes.put(classKey(MODE_Y), "mode_y_class");
			attributes.put(classKey(MODE_Z), "mode_z_class");

			BeanClassProvider bcp = new BeanClassProvider(CLASS_PROPERTY_PREFIX, fallbacks);
			String className = bcp.forMode(MODE_X, new BeanAttributes(attributes));
			assertThat(className, is("mode_x_class"));
		} finally {
			if (initMode != null) {
				LocalProperties.set(GDA_MODE, initMode);
			} else {
				LocalProperties.clearProperty(GDA_MODE);
			}
		}
	}

	private static String classKey(String mode) {
		return mode + "-class";
	}

	@Test
	public void unknownModeFails() {
		assertThrows(IllegalArgumentException.class, () -> classForMode("unknown"));
	}
}

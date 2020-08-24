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

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;

public class BeanAttributesTest {
	private static final String MODE = "gda.mode";
	private static final String INITIAL_MODE = LocalProperties.get(MODE);

	private Map<String, String> raw = new HashMap<>();

	@After
	public void resetMode() {
		LocalProperties.set(MODE, INITIAL_MODE);
	}

	private void setMode(String mode) {
		LocalProperties.set(MODE, mode);
	}

	@Test
	public void basicKeyValue() {
		raw.put("foo", "bar");
		BeanAttributes att = new BeanAttributes(raw);
		assertThat(att.get("foo"), is(Optional.of("bar")));
	}

	@Test
	public void modeSpecificValuesAreUsed() throws Exception {
		raw.put("foo", "bar");
		raw.put("mode_x-foo", "not-bar");
		setMode("mode_x");
		BeanAttributes att = new BeanAttributes(raw);
		assertThat(att.get("foo"), is(Optional.of("not-bar")));
	}

	@Test
	public void missingModeUsesDefault() throws Exception {
		raw.put("foo", "bar");
		raw.put("mode_x-foo", "not-bar");
		setMode("mode_y");
		BeanAttributes att = new BeanAttributes(raw);
		assertThat(att.get("foo"), is(Optional.of("bar")));
	}

	@Test
	public void manyProperties() {
		raw.put("foo1", "bar1");
		raw.put("foo2", "bar2");
		raw.put("mode_x-foo1", "bar3");
		raw.put("mode_x-foo2", "bar4");
		raw.put("mode_y-foo1", "bar5");
		raw.put("mode_y-foo2", "bar6");
		BeanAttributes att = new BeanAttributes(raw);
		assertThat(att.get("foo1"), is(Optional.of("bar1")));
	}

	@Test
	public void missingPropertyReturnsEmpty() throws Exception {
		assertThat(new BeanAttributes(raw).get("foo"), is(empty()));
	}

	@Test
	public void streamReturnsExpectedProperties() throws Exception {
		raw.put("foo", "bar");
		raw.put("one", "1");
		raw.put("mode_x-one", "ONE");
		raw.put("mode_x-two", "two");
		raw.put("mode_y-three", "three");
		setMode("mode_x");
		BeanAttributes att = new BeanAttributes(raw);
		Map<String, String> atts = att.modeProperties()
				.collect(toMap(Entry::getKey, Entry::getValue));
		assertThat(atts.keySet(), containsInAnyOrder("foo", "one", "two"));
		assertThat(atts.get("one"), is("ONE"));
	}
}

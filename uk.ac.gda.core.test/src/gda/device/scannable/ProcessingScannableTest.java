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

package gda.device.scannable;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProcessingScannableTest {
	private ProcessingScannable pr;
	@BeforeEach
	public void setup() {
		pr = new ProcessingScannable("test");
	}

	@Test
	public void startsEmpty() {
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), is(empty()));
	}

	@Test
	public void setIndividualConfiguration() {
		pr.setProcessingRequest("foo", asList("bar"));

		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "bar")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setMultipleConfiguration() {
		pr.setProcessingRequest("foo", asList("one"));
		pr.setProcessingRequest("bar", asList("two"));

		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), containsInAnyOrder(entry("foo", "one"), entry("bar", "two")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setFullConfiguration() {
		Map<String, Collection<Object>> requestIn = new HashMap<>();
		requestIn.put("foo", asList("one"));
		requestIn.put("bar", asList("two"));
		pr.setProcessingRequest(requestIn);

		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), containsInAnyOrder(entry("foo", "one"), entry("bar", "two")));
	}

	@Test
	public void extendExistingRequest() {
		pr.addProcessingRequest("foo", "one");
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "one")));

		pr.addProcessingRequest("foo", "two");
		// Previous request shouldn't be updated
		assertThat(request.get("foo"), is(not(contains("two"))));

		// get new request
		request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "one", "two")));
	}

	@Test
	public void setSingleConfiguration() {
		pr.setProcessingRequest("foo", "one");
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "one")));

		pr.setProcessingRequest("foo", "two");
		// Previous request shouldn't be updated
		assertThat(request.get("foo"), is(not(contains("two"))));

		// get new request
		request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "two")));

	}

	@Test
	public void clearSingleKey() {
		Map<String, Collection<Object>> requestIn = new HashMap<>();
		requestIn.put("foo", asList("one"));
		requestIn.put("bar", asList("two"));
		pr.setProcessingRequest(requestIn);

		pr.clearProcessing("foo");
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("bar", "two")));
	}

	@Test
	public void clearProcessing() {
		Map<String, Collection<Object>> requestIn = new HashMap<>();
		requestIn.put("foo", asList("one"));
		requestIn.put("bar", asList("two"));
		pr.setProcessingRequest(requestIn);

		pr.clearProcessing();
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), is(empty()));
	}

	@Test
	public void listsAreCopies() throws Exception {
		List<Object> configs = new ArrayList<>();
		configs.add("one");
		pr.setProcessingRequest("foo", configs);
		configs.clear();

		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.entrySet(), contains(entry("foo", "one")));

	}


	private Matcher<Entry<String, Collection<Object>>> entry(String key, Object... values) {
		return new TypeSafeMatcher<Entry<String, Collection<Object>>>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("<" + key + stream(values).map(Object::toString).collect(joining(", ", "=[", "]")) + ">");
			}

			@Override
			protected boolean matchesSafely(Entry<String, Collection<Object>> item) {
				return item.getKey().equals(key) && contains(values).matches(item.getValue());
			}

		};
	}
}

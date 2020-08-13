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
import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.python.core.Py;

import gda.device.ProcessingRequestProvider;

/** A scannable that provides a configurable processing request. Takes no other actions in scan */
public class ProcessingScannable extends AbstractScanHook implements ProcessingRequestProvider {

	public ProcessingScannable(String name) {
		super(name);
	}

	/** Internal processing request - should not be exposed to callers directly */
	private final Map<String, Collection<Object>> request = new HashMap<>();

	@Override
	public Map<String, Collection<Object>> getProcessingRequest() {
		return request.entrySet().stream()
				.collect(toMap(Entry::getKey, e -> unmodifiableCollection(e.getValue())));
	}

	/** Set complete processing request removing any previous configuration */
	public void setProcessingRequest(Map<String, Collection<Object>> newRequest) {
		request.clear();
		// deep copy request map but not much we can do about mutable objects in the collections
		request.putAll(newRequest.entrySet().stream()
				.collect(toMap(Entry::getKey, e -> new ArrayList<>(e.getValue()))));
	}

	/** Set the configuration for a processing key removing any previous configuration <em>for that key</em> */
	public void setProcessingRequest(String key, Collection<Object> config) {
		request.put(key, new ArrayList<>(config));
	}

	/**
	 * Set the configuration for a processing key (to a list containing a single value)
	 * removing any previous configuration <em>for that key</em>
	 */
	public void setProcessingRequest(String key, Object config) {
		setProcessingRequest(key, asList(config));
	}

	/** Add configuration to a previous process, creating one if it doesn't exist */
	public void addProcessingRequest(String key, Object value) {
		request.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
	}

	/** Remove all configuration */
	public void clearProcessing() {
		request.clear();
	}

	/** Remove all configuration for a processing key */
	public void clearProcessing(String key) {
		request.remove(key);
	}

	/** Python method to get processing configuration - returns unmodifiable copy */
	public Collection<Object> __getitem__(String process) {
		return unmodifiableCollection(ofNullable(request.get(process))
				.orElseThrow(() -> Py.KeyError(process)));
	}

	/**
	 * Python convenience method for setting processing configurations
	 * <pre>
	 * >>> pr['foo'] = ['hello', 'world'] # sets the config for the 'foo' entry
	 * </pre>
	 */
	public void __setitem__(String process, Iterable<Object> config) {
		setProcessingRequest(process, stream(config.spliterator(), false).collect(toList()));
	}

	// Strings in python are also collections so prevent unexpected behaviour of setting a string
	// and getting a list of characters
	/**
	 * Python convenience method for setting processing configuration to a single string
	 * <pre>
	 * >>> pr['foo'] = 'helloWorld' # sets the config for the 'foo' entry
	 * >>> pr['foo']
	 * ['helloWorld']
	 * >>>
	 * </pre>
	 */
	public void __setitem__(String process, String config) {
		setProcessingRequest(process, asList(config));
	}

	/** Python method to remove a processing entry */
	public void __delitem__(String process) {
		clearProcessing(process);
	}
}

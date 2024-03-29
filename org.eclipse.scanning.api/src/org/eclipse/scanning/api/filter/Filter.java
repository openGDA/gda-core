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
package org.eclipse.scanning.api.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.AbstractNameable;

/**
 * A springable class which encapsulates information for a given feature.
 *
 * <h3> Filters </h3>
 * Filters work as follows:<p>
 *
 * <pre>
 * L = list of things to be filtered
 * e = excluded function
 * i = included function
 * l = filtered list.
 * <p>
 *   l = L-e(L)+i(L)
 * </pre>
 *
 * This means that all combinations are possible.
 * An exclude of .* may be applied if only particular devices
 * are required in the filter and everything done with includes.
 * A particular set of devices may be removed but if one is
 * required, it may be added back in with the final include.
 *
 * <h3>  Example Spring </h3>
 *   <pre>
	{@literal <bean id="filter" class="org.eclipse.scanning.api.filter.Filter" init-method="register">}
	{@literal	<property name="name" value="org.eclipse.scanning.scannableFilter"/>}
	{@literal	<property name="excludes">}
	{@literal		<list>}
	{@literal			<value>monitor.*</value>}
	{@literal			<value>a</value>}
	{@literal			<value>b</value>}
	{@literal			<value>c</value>}
	{@literal			<value>beam.*</value>}
	{@literal			<value>neXusScannable.*</value>}
	{@literal			<value>monitor1</value>}
	{@literal		</list>}
	{@literal	</property>}
	{@literal	<property name="includes">}
	{@literal		<list>}
	{@literal			<value>monitor.*</value>}
	{@literal			<value>beamcurrent</value>}
	{@literal			<value>neXusScannable2</value>}
	{@literal			<value>neXusScannable</value> <!--  Should not match anything -->}
	{@literal		</list>}
	{@literal	</property>}
    {@literal    <property name="bundle"   value="org.eclipse.scanning.example" /> <!-- Delete for real spring? -->}
	{@literal</bean>}
	  </pre>
 *
 *
 * @author Matthew Gerring
 *
 */
public class Filter extends AbstractNameable implements IFilter<String> {

	private List<String> excludes;
	private List<String> includes;

	public void register() {
		IFilterService.DEFAULT.register(this);
	}

	@Override
	public List<String> getExcludes() {
		return excludes;
	}
	@Override
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}
	@Override
	public List<String> getIncludes() {
		return includes;
	}
	@Override
	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	@Override
	public List<String> filter(final Collection<String> items) {
		final List<String> ret = new ArrayList<>(items);

		// Process excludes
		final List<String> excludePatterns = getExcludes();
		if (excludePatterns != null) {
			final Collection<String> excluded = match(excludePatterns, items);
			ret.removeAll(excluded);
		}

		// Process includes
		final List<String> includePatterns = getIncludes();
		if (includePatterns != null) {
			final Collection<String> included = match(includePatterns, items);
			final Collection<String> done = new HashSet<>();

			for (final String item : included) {
				if (done.contains(item)) continue;
				int ecount = (int)ret.stream().filter(t->t.equals(item)).count();
				int icount = (int)included.stream().filter(t->t.equals(item)).count();
				ret.addAll(Arrays.stream(new String[icount-ecount]).map(nothing->item).collect(Collectors.toList()));

				done.add(item);
			}
		}
		return ret;
	}

	/**
	 * Gets the items in items which match one of regex in the same
	 * order they were provided
	 * @param regexes
	 * @param items
	 * @return
	 */
	private Collection<String> match(List<String> regexes, Collection<String> items) {

		// TODO Could solve with lambda but it was not looking as concise at this
		// simple loop.
		Collection<String> ret = new ArrayList<>();
		ITEMS: for (String item : items) {
			for (String regex : regexes) {
				if (item.matches(regex)) {
					ret.add(item);
					continue ITEMS;
				}
			}
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
		result = prime * result + ((includes == null) ? 0 : includes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Filter other = (Filter) obj;
		if (excludes == null) {
			if (other.excludes != null)
				return false;
		} else if (!excludes.equals(other.excludes))
			return false;
		if (includes == null) {
			if (other.includes != null)
				return false;
		} else if (!includes.equals(other.includes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Filter [excludes=" + excludes + ", includes=" + includes + "]";
	}
}

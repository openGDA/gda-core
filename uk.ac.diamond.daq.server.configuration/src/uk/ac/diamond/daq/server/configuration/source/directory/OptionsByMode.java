/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.configuration.source.directory;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;

import uk.ac.diamond.daq.server.configuration.source.directory.ConfigFile.Options;

public class OptionsByMode {
	private List<String> common = new ArrayList<>();
	private Map<String, List<String>> modes = new HashMap<>();
	public OptionsByMode() {}
	@JsonCreator
	public OptionsByMode(String single) {
		this.common = new Options(single).options;
	}
	@JsonCreator
	public OptionsByMode(List<Object> options) {
		this.common = new Options(options).options;
	}

	@SuppressWarnings("unused")
	public void setCommon(Options common) {
		this.common = common.options;
	}

	@SuppressWarnings("unused")
	public void setMode(Map<String, Options> mode) {
		this.modes = mode.entrySet().stream().collect(toMap(Entry::getKey, e -> e.getValue().options));
	}
	public Stream<String> forMode(String mode) {
		return Stream.of(modes.getOrDefault(mode, List.of()), common).flatMap(List::stream);
	}
	public OptionsByMode merge(OptionsByMode other) {
		var opts = new OptionsByMode();
		opts.common = Stream.of(this.common, other.common).flatMap(List::stream).toList();
		opts.modes = Stream.of(this.modes, other.modes)
				.map(Map::entrySet)
				.flatMap(Set::stream)
				.collect(toMap(
						Entry::getKey,
						Entry::getValue,
						(l, r) -> Stream.of(l, r).flatMap(List::stream).toList()));
		return opts;
	}
}
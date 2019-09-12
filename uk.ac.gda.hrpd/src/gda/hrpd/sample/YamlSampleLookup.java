/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.hrpd.sample;

import static gda.hrpd.sample.SampleDefinitionException.missing;
import static gda.hrpd.sample.SampleDefinitionException.parseError;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.yaml.snakeyaml.Yaml;

import gda.hrpd.sample.api.SampleMetadata;
import gda.hrpd.sample.api.ScanDescription;
import gda.hrpd.sample.scans.MacScan;
import gda.hrpd.sample.scans.PsdScan;

public class YamlSampleLookup extends BaseSampleLookup {
	private String filename;

	private static final String MAC = "mac";
	private static final String PSD = "psd";

	public YamlSampleLookup(String filename) throws IOException {
		this.filename = filename;
		Yaml yaml = new Yaml();
		try (InputStream in = Files.newInputStream(Paths.get(filename))) {
			Map<String, Object> data = yaml.load(in);
			samples = data.entrySet().stream().map(this::getSample).collect(toList());
		}
	}

	@Override
	public String toString() {
		return "YamlSampleData[filename="+filename+"]";
	}

	private SampleMetadata getSample(Entry<String, Object> data) {
		return new SampleMetadataBean(new YamlSampleMetadata(data));
	}

	private class YamlSampleMetadata implements SampleMetadataBean.Info {

		private String name;
		private Map<String, Object> sample;

		@SuppressWarnings("unchecked") // cast to map
		public YamlSampleMetadata(Entry<String, Object> data) {
			name = data.getKey();
			sample = checked(() -> (Map<String, Object>) data.getValue(), name);
			if (sample == null) {
				throw missing(name, name);
			}
		}

		private <T> T checked(Supplier<T> source, String field) {
			try {
				return source.get();
			} catch (Exception e) {
				throw parseError(name, field, e);
			}
		}

		@Override
		public int getCarouselPosition() {
			return checked(() -> (int)sample.get("position"), "position");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getComment() {
			return sample.getOrDefault("comment", "").toString();
		}

		@Override
		public String getTitle() {
			return sample.getOrDefault("title", "").toString();
		}

		@Override
		public String getDirectory() {
			return sample.getOrDefault("directory", "").toString();
		}

		@Override
		public ScanDescription getScan() {
			ScanDescription scan;
			if (sample.containsKey(PSD)) {
				double[] delta = getDelta(PSD);
				if (delta.length == 1) {
					scan = new PsdScan(getCollectionTime(PSD), delta[0], getSpin(PSD), getSpos(PSD), new HashMap<>(), getExtras(PSD));
				} else {
					scan = new PsdScan(getCollectionTime(PSD), delta[0], delta[1], delta[2], getSpin(PSD), getSpos(PSD), new HashMap<>(), getExtras(PSD));
				}
			} else if (sample.containsKey(MAC)) {
				scan = new MacScan(getCollectionTime(MAC), getRebinningSizes(MAC), getSpin(MAC), getSpos(MAC), new HashMap<>(), getExtras(MAC));
			} else {
				throw missing(name, "psd or mac");
			}
			return scan;
		}

		private double[] getDelta(String scan) {
			double[] delta = checked(() -> {
				Object rawDelta = scanData(scan).get("delta");
				if (rawDelta instanceof Number) {
					return new double[] {((Number)rawDelta).doubleValue()};
				} else if ( rawDelta instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Number> sizes = (List<Number>)rawDelta;
					return sizes.stream().mapToDouble(Number::doubleValue).toArray();
				} else if (rawDelta == null) {
					return new double[] {};
				}
				throw parseError(name, "delta", "Unexpected value type - found " + rawDelta.getClass());
			}, "delta");
			if (delta.length != 1 && delta.length != 3) {
				throw parseError(name, "delta", "value must be either \"posn\" or \"start, stop, step\")");
			}
			return delta;
		}

		public double getCollectionTime(String scan) {
			return checked(((Number)scanData(scan).get("collectionTime"))::doubleValue, "collectionTime");
		}

		public double getSpos(String scan) {
			return checked(() -> ((Number)scanData(scan).getOrDefault("spos", 0)).doubleValue(), "spos");
		}

		public boolean getSpin(String scan) {
			return checked(() -> (boolean)scanData(scan).getOrDefault("spin", false), "spin");
		}

		public double[] getRebinningSizes(String scan) {
			return checked(() -> {
				@SuppressWarnings("unchecked")
				List<Double> sizes = (List<Double>)scanData(scan).getOrDefault("rebinning", Arrays.asList());
				return sizes.stream().mapToDouble(d -> d).toArray();
			}, "rebinning");
		}

		@Override
		public String getVisit() {
			return sample.getOrDefault("visit", getDefaultVisit()).toString();
		}

		public Collection<String> getExtras(String scan) {
			return checked(() -> {
					String value = (String) scanData(scan).getOrDefault("extras", "");
					return value.isEmpty() ? emptyList() : asList(value.split("\\s*,\\s*"));
				}, "extras");
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> scanData(String scan) {
			return (Map<String, Object>)sample.get(scan);
		}
	}

}

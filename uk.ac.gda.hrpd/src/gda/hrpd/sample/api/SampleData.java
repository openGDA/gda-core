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

package gda.hrpd.sample.api;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import gda.hrpd.sample.ExcelSampleLookup;
import gda.hrpd.sample.YamlSampleLookup;
import gda.hrpd.sample.YamlSampleWriter;

public final class SampleData {
	private SampleData() {}

	public static SampleLookup loadExcel(String filename) throws IOException {
		return new ExcelSampleLookup(filename);
	}
	public static SampleLookup loadYaml(String filename) throws IOException {
		return new YamlSampleLookup(filename);
	}
	public static SampleLookup load(String filename) throws IOException {
		if (isExcel(filename)) {
			return loadExcel(filename);
		} else if (isYaml(filename)) {
			return loadYaml(filename);
		} else {
			throw new IllegalArgumentException("Could not determine file type");
		}
	}

	public static SampleWriter getWriter(String filepath) throws IOException {
		if (isExcel(filepath)) {
			throw new UnsupportedOperationException("No Excel writer is available yet");
		} else if (isYaml(filepath)) {
			return new YamlSampleWriter(filepath);
		} else {
			throw new IllegalArgumentException("Unrecognised file type: " + filepath);
		}
	}

	public static void writeYaml(SampleLookup samples, String filename) throws IOException {
		try (YamlSampleWriter writer = new YamlSampleWriter(filename)) {
			writer.write(samples);
		}
	}

	public static void writeExcel(SampleLookup samples, String filename) {
		throw new UnsupportedOperationException("No Excel writer is available yet");
	}

	public static void write(SampleLookup samples, String filename) throws IOException {
		try (SampleWriter writer = getWriter(filename)) {
			writer.write(samples);
		}
	}

	private static boolean isYaml(String filename) {
		requireNonNull(filename, "Filename must not be null");
		return filename.endsWith("yaml");
	}

	private static boolean isExcel(String filename) {
		requireNonNull(filename, "Filename must not be null");
		return filename.endsWith(".xls") || filename.endsWith(".xlsx");
	}
}

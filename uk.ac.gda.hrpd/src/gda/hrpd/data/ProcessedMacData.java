/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.hrpd.data;

import static java.lang.Math.sqrt;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessedMacData {
	private static final int EXPECTED_ROWS = 150_000;
	private static final Logger logger = LoggerFactory.getLogger(ProcessedMacData.class);
	private static final String MONITOR_AVERAGE_KEY = "MonitorAverageCount";
	private static final String SCAN_TIME_KEY = "ScanTime";
	private static final Pattern WS = Pattern.compile("\\s+");
	private static final Pattern EQ = Pattern.compile("=");

	private String filename;
	private Map<String, String> headers;
	private double[] tth;
	private double[] counts;
	private double[] error;

	public ProcessedMacData(String file) throws IOException {
		filename = file;
		load();
	}

	public static Optional<ProcessedMacData> readFile(String filename) {
		try {
			return Optional.of(new ProcessedMacData(filename));
		} catch (IOException e) {
			logger.error("Failed to read raw mac data from {}", filename, e);
			return Optional.empty();
		}
	}

	private ProcessedMacData(String filename, Map<String, String> headers, double[] tth, double[] counts, double[] errors) {
		this.filename = filename;
		this.headers = headers;
		this.tth = tth;
		this.counts = counts;
		this.error = errors;
	}

	private void load() throws IOException {
		try (var in = Files.newInputStream(Paths.get(filename));
				var isr = new InputStreamReader(in);
				var br = new BufferedReader(isr);) {
			readHeaders(br);
			readFields(br);
			readData(br);
		}
	}

	private void readData(BufferedReader br) {
		tth = new double[EXPECTED_ROWS];
		counts = new double[EXPECTED_ROWS];
		error = new double[EXPECTED_ROWS];
		for (var i = 0; i < EXPECTED_ROWS; i++) {
			String line;
			try {
				line = br.readLine();
			} catch (IOException e) {
				throw new IllegalStateException("Could not read line " + i + " from " + filename);
			}
			var fields = WS.split(line);
			try {
				tth[i] = Double.parseDouble(fields[0].strip());
				counts[i] = Double.parseDouble(fields[1].strip());
				error[i] = Double.parseDouble(fields[2].strip());
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("Invalid value on line (ignoring headers) " + i, nfe);
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				throw new IllegalStateException("Not enough fields on line (ignoring headers) " + i, aioobe);
			}
		}
	}

	private void readFields(BufferedReader br) throws IOException {
		String fieldLine = br.readLine();
		var fields = WS.split(fieldLine);
		if (!"tth".equals(fields[0].strip()) || !"counts".equals(fields[1].strip()) || !"error".equals(fields[2].strip())) {
			throw new IllegalStateException("Unknown field headings '" + fieldLine + "'. Expected 'tth counts error'");
		}
	}

	private void readHeaders(BufferedReader br) {
		headers = br.lines()
				.takeWhile(s -> !s.equals("&END"))
				.filter(s -> !s.startsWith("&"))
				.map(s -> EQ.split(s, 2))
				.collect(toMap(s -> s[0],
						s -> s.length > 1 ? s[1] : "",
						(a, b) -> a,
						LinkedHashMap::new));
	}

	public ProcessedMacData add(String file) throws IOException {
		var other = new ProcessedMacData(file);
		return add(other);
	}

	public ProcessedMacData add(ProcessedMacData other) {
		Map<String, String> newHeaders = new LinkedHashMap<>(headers);
		var thisMonitorAverage = Double.parseDouble(headers.getOrDefault(MONITOR_AVERAGE_KEY, "0"));
		var otherMonitorAverage = Double.parseDouble(other.headers.getOrDefault(MONITOR_AVERAGE_KEY, "0"));
		var thisScanTime = Double.parseDouble(headers.getOrDefault(SCAN_TIME_KEY, "0"));
		var otherScanTime = Double.parseDouble(other.headers.getOrDefault(SCAN_TIME_KEY, "0"));
		double totalScanTime = thisScanTime + otherScanTime;
		double weightedMonitorAverage = (thisMonitorAverage*thisScanTime + otherMonitorAverage*otherScanTime)/totalScanTime;
		newHeaders.put(MONITOR_AVERAGE_KEY, String.valueOf(weightedMonitorAverage));
		newHeaders.put(SCAN_TIME_KEY, String.valueOf(totalScanTime));

		var newCounts = new double[EXPECTED_ROWS];
		var newError = new double[EXPECTED_ROWS];
		var otherCounts = other.counts;
		var otherError = other.error;
		for (var i = 0; i < EXPECTED_ROWS; i++) {
			newCounts[i] = counts[i] + otherCounts[i];
			newError[i] = sqrt(error[i]*error[i] + otherError[i] * otherError[i]);
		}
		return new ProcessedMacData(null, newHeaders, tth, newCounts, newError);
	}

	public void write(String file) throws IOException {
		try (var wr = new Formatter(new File(file))) {
			wr.format("&DLS%n");
			for (var entry: headers.entrySet()) {
				wr.format("%s=%s%n", entry.getKey(), entry.getValue());
			}
			wr.format("&END%n");
			wr.format("tth\tcounts\terror%n");
			for (var i = 0; i < EXPECTED_ROWS; i++) {
				wr.format("%.3f\t%.3f\t%.3f%n", tth[i], counts[i], error[i]);
			}
		}
	}

	public Optional<IOException> tryWrite(String filename) {
		try {
			write(filename);
			return Optional.empty();
		} catch (IOException e) {
			return Optional.of(e);
		}
	}
}

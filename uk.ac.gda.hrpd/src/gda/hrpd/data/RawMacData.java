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

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Container for raw MAC data read from a file */
public class RawMacData {
	private static final Logger logger = LoggerFactory.getLogger(RawMacData.class);
	private static final String MONITOR_AVERAGE_KEY = "MonitorAverageCount";
	private static final String SCAN_TIME_KEY = "ScanTime";
	private static final Pattern WS = Pattern.compile("\\s+");
	private static final Pattern EQ = Pattern.compile("=");

	private String filename;
	private String[] fields;
	private Map<String, String> headers;
	private List<Double> positions;
	private List<int[]> data;

	public RawMacData(String file) throws IOException {
		filename = file;
		load();
	}

	public static Optional<RawMacData> readFile(String filename) {
		try {
			return Optional.of(new RawMacData(filename));
		} catch (IOException e) {
			logger.error("Failed to read raw mac data from {}", filename, e);
			return Optional.empty();
		}
	}

	private RawMacData(String filename, String[] fields, Map<String, String> headers, List<Double> positions, List<int[]> data) {
		this.filename = filename;
		this.fields = fields;
		this.headers = headers;
		this.positions = positions;
		this.data = data;
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
		positions = new ArrayList<>(60000);
		data = new ArrayList<>(60000);
		br.lines()
				.map(s -> WS.split(s))
				.forEach(s -> {
						positions.add(Double.valueOf(s[0]));
						data.add(Arrays.stream(s).skip(1).mapToInt(Integer::valueOf).toArray());
				});
	}

	private void readFields(BufferedReader br) throws IOException {
		String fieldLine = br.readLine();
		fields = WS.split(fieldLine);
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

	public RawMacData add(String file) throws IOException {
		var other = new RawMacData(file);
		return add(other);
	}

	public RawMacData add(RawMacData other) {
		Map<String, String> newHeaders = new LinkedHashMap<>(headers);
		var thisMonitorAverage = Double.parseDouble(headers.getOrDefault(MONITOR_AVERAGE_KEY, "0"));
		var otherMonitorAverage = Double.parseDouble(other.headers.getOrDefault(MONITOR_AVERAGE_KEY, "0"));
		var thisScanTime = Double.parseDouble(headers.getOrDefault(SCAN_TIME_KEY, "0"));
		var otherScanTime = Double.parseDouble(other.headers.getOrDefault(SCAN_TIME_KEY, "0"));
		double totalScanTime = thisScanTime + otherScanTime;
		double weightedMonitorAverage = (thisMonitorAverage*thisScanTime + otherMonitorAverage*otherScanTime)/totalScanTime;
		newHeaders.put(MONITOR_AVERAGE_KEY, String.valueOf(weightedMonitorAverage));
		newHeaders.put(SCAN_TIME_KEY, String.valueOf(totalScanTime));
		List<int[]> newData = new ArrayList<>(positions.size());
		for (int i = 0; i < positions.size(); i++) {
			int[] left = data.get(i);
			int[] right = other.data.get(i);
			int[] sum = new int[left.length];
			for (int j = 0; j < sum.length; j++) {
				sum[j] = left[j] + right[j];
			}
			newData.add(sum);
		}
		return new RawMacData(null, fields, newHeaders, other.positions, newData);
	}

	public void write(String file) throws IOException {
		try (var wr = new FileWriter(new File(file))) {
			wr.write("&DLS\n");
			for (var entry: headers.entrySet()) {
				wr.write(entry.getKey());
				wr.write('=');
				wr.write(entry.getValue());
				wr.write('\n');
			}
			wr.write("&END\n");
			wr.write(String.join("\t", fields));
			wr.write('\n');
			for (var i = 0; i<data.size(); i++) {
				wr.write(String.format("%.9f", positions.get(i)));
				for (var v: data.get(i)) {
					wr.write('\t');
					wr.write(Integer.toString(v));
				}
				wr.write('\n');
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

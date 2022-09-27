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

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import gda.hrpd.sample.api.SampleMetadata;
import gda.hrpd.sample.api.SampleWriter;

public class YamlSampleWriter implements SampleWriter {
	private static final Logger logger = LoggerFactory.getLogger(YamlSampleWriter.class);
	private final String filepath;
	private final Yaml yaml;
	private final FileWriter outputFile;
	private boolean closed;

	public YamlSampleWriter(String filepath) throws IOException {
		this.filepath = filepath;
		outputFile = new FileWriter(filepath);
		Representer representer = new Representer();
		representer.addClassTag(Map.class,Tag.MAP);
		representer.addClassTag(List.class,Tag.SEQ);
		representer.addClassTag(SampleMetadata.class, Tag.MAP);
		representer.setDefaultFlowStyle(FlowStyle.BLOCK);
		yaml = new Yaml(representer);
	}

	@Override
	public void write(SampleMetadata sample) {
		yaml.dump(Arrays.asList(getMapForm(sample)), outputFile);
	}

	@Override
	public void write(Iterable<SampleMetadata> samples) {
		Collection<Map<String, Object>> data = stream(samples.spliterator(), false)
				.map(this::getMapForm)
				.collect(toList());
		yaml.dump(data, outputFile);
	}

	private Map<String, Object> getMapForm(SampleMetadata sample) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", sample.getName());
		map.put("position", sample.getCarouselPosition());
		map.put("comment", sample.getComment());
		map.put("title", sample.getTitle());
		map.put("directory", sample.getDirectory());
		map.put("visit", sample.getVisit());
		map.put("scan", sample.getScan().asMap());
		sample.getResults().ifPresent(results -> {
			Map<String, Object> resMap = new LinkedHashMap<>();
			if (results.isSuccess()) {
				resMap.put("scanNumber", results.getScanNumber());
				resMap.put("scanFile", results.getScanFile());
				resMap.put("files", results.getAllFiles());
				resMap.put("startTime", results.getStartTime().toString());
				resMap.put("endTime", results.getEndTime().toString());
				resMap.put("context", results.getContextData());
				resMap.put("user", results.getUser());
				resMap.put("collectionTime", sample.getScan().getCollectionTime());
			} else {
				resMap.put("error", results.getError().toString());
			}
			map.put("results", resMap);
		});
		return map;
	}

	@Override
	public String toString() {
		return "YamlSampleWriter[" + filepath + (closed ? ", closed" : "") + "]";
	}

	@Override
	public void close() {
		try {
			outputFile.close();
		} catch (IOException e) {
			// Not much we can do here
			logger.error("Failed to close outputFile: {}", filepath, e);
		} finally {
			closed = true;
		}
	}
}

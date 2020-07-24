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

package org.eclipse.scanning.api.event.scan;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for a {@link ScanRequest}
 * <p>
 * For descriptions of the parameters, see {@link ScanRequest}
 */
public class ScanRequestBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ScanRequestBuilder.class);

	public static final String DETECTORS = "detectors";
	public static final String MONITOR_NAMES_PER_POINT = "monitorNamesPerPoint";
	public static final String MONITOR_NAMES_PER_SCAN = "monitorNamesPerScan";
	public static final String SAMPLE_DATA = "sampleData";
	public static final String SCAN_METADATA = "scanMetadata";
	public static final String FILE_PATH = "filePath";
	public static final String TEMPLATE_FILE_PATHS = "templateFilePaths";
	public static final String START_POSITION = "startPosition";
	public static final String END_POSITION = "endPosition";
	public static final String BEFORE_SCRIPT = "beforeScript";
	public static final String AFTER_SCRIPT = "afterScript";
	public static final String ALWAYS_RUN_AFTER_SCRIPT = "alwaysRunAfterScript";
	public static final String IGNORE_PREPROCESS = "ignorePreprocess";
	public static final String PROCESSING_REQUEST = "processingRequest";

	private IScanPointGeneratorModel model;

	private Map<String, IDetectorModel> detectors = new HashMap<>();

	private Collection<String> monitorNamesPerPoint = Collections.emptyList();
	private Collection<String> monitorNamesPerScan = Collections.emptyList();

	private SampleData sampleData = null;

	private List<ScanMetadata> scanMetadata = Collections.emptyList();

	private String filePath = null;

	private Set<String> templateFilePaths = Collections.emptySet();

	private IPosition startPosition = null;
	private IPosition endPosition = null;

	private ScriptRequest beforeScript = null;
	private ScriptRequest afterScript = null;

	private boolean alwaysRunAfterScript = false;

	private boolean ignorePreprocess = false;

	private ProcessingRequest processingRequest = null;

	// Construct setting just the model
	public ScanRequestBuilder(IScanPointGeneratorModel model) {
		this.model = model;
	}

	// Construct setting model and arbitrary other values
	public ScanRequestBuilder(IScanPointGeneratorModel model, Map<String, Object> values) {
		this.model = model;
		values.entrySet().stream().forEach(entry -> setValue(entry.getKey(), entry.getValue()));
	}

	@SuppressWarnings("unchecked")
	private void setValue(String field, Object value) {
		try {
			if (field.equals(DETECTORS)) {
				withDetectors((Map<String, IDetectorModel>) value);
			} else if (field.equals(MONITOR_NAMES_PER_POINT)) {
				withMonitorNamesPerPoint((Collection<String>) value);
			} else if (field.equals(MONITOR_NAMES_PER_SCAN)) {
				withMonitorNamesPerScan((Collection<String>) value);
			} else if (field.equals(SAMPLE_DATA)) {
				withSampleData((SampleData) value);
			} else if (field.equals(SCAN_METADATA)) {
				withScanMetadata((List<ScanMetadata>) value);
			} else if (field.equals(FILE_PATH)) {
				withFilePath((String) value);
			} else if (field.equals(TEMPLATE_FILE_PATHS)) {
				withTemplateFilePaths((Set<String>) value);
			} else if (field.equals(START_POSITION)) {
				withStartPosition((IPosition) value);
			} else if (field.equals(END_POSITION)) {
				withEndPosition((IPosition) value);
			} else if (field.equals(BEFORE_SCRIPT)) {
				withBeforeScript((ScriptRequest) value);
			} else if (field.equals(AFTER_SCRIPT)) {
				withAfterScript((ScriptRequest) value);
			} else if (field.equals(ALWAYS_RUN_AFTER_SCRIPT)) {
				alwaysRunAfterScript((boolean) value);
			} else if (field.equals(IGNORE_PREPROCESS)) {
				ignorePreprocess((boolean) value);
			} else if (field.equals(PROCESSING_REQUEST)) {
				withProcessingRequest((ProcessingRequest) value);
			}
		} catch (ClassCastException e) {
			logger.error("'{}' is not a valid value for {}", value, field);
			throw e;
		}
	}

	public ScanRequestBuilder withDetectors(Map<String, IDetectorModel> detectors) {
		this.detectors = detectors;
		return this;
	}

	public ScanRequestBuilder withMonitorNamesPerPoint(Collection<String> monitorNamesPerPoint) {
		this.monitorNamesPerPoint = monitorNamesPerPoint;
		return this;
	}

	public ScanRequestBuilder withMonitorNamesPerScan(Collection<String> monitorNamesPerScan) {
		this.monitorNamesPerScan = monitorNamesPerScan;
		return this;
	}

	public ScanRequestBuilder withSampleData(SampleData sampleData) {
		this.sampleData = sampleData;
		return this;
	}

	public ScanRequestBuilder withScanMetadata(List<ScanMetadata> scanMetadata) {
		this.scanMetadata = scanMetadata;
		return this;
	}

	public ScanRequestBuilder withFilePath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public ScanRequestBuilder withTemplateFilePaths(Set<String> templateFilePaths) {
		this.templateFilePaths = templateFilePaths;
		return this;
	}

	public ScanRequestBuilder withStartPosition(IPosition startPosition) {
		this.startPosition = startPosition;
		return this;
	}

	public ScanRequestBuilder withEndPosition(IPosition endPosition) {
		this.endPosition = endPosition;
		return this;
	}

	public ScanRequestBuilder withBeforeScript(ScriptRequest beforeScript) {
		this.beforeScript = beforeScript;
		return this;
	}

	public ScanRequestBuilder withAfterScript(ScriptRequest afterScript) {
		this.afterScript = afterScript;
		return this;
	}

	public ScanRequestBuilder alwaysRunAfterScript(boolean value) {
		this.alwaysRunAfterScript = value;
		return this;
	}

	public ScanRequestBuilder ignorePreprocess(boolean value) {
		this.ignorePreprocess = value;
		return this;
	}

	public ScanRequestBuilder withProcessingRequest(ProcessingRequest processingRequest) {
		this.processingRequest = processingRequest;
		return this;
	}

	public ScanRequest build() {
		final ScanRequest request = new ScanRequest();
		request.setCompoundModel(new CompoundModel(model));
		request.setDetectors(detectors);
		request.setMonitorNamesPerPoint(monitorNamesPerPoint);
		request.setMonitorNamesPerScan(monitorNamesPerScan);
		request.setSampleData(sampleData);
		request.setScanMetadata(scanMetadata);
		request.setFilePath(filePath);
		request.setTemplateFilePaths(templateFilePaths);
		request.setStartPosition(startPosition);
		request.setEnd(endPosition);
		request.setBeforeScript(beforeScript);
		request.setAfterScript(afterScript);
		request.setAlwaysRunAfterScript(alwaysRunAfterScript);
		request.setIgnorePreprocess(ignorePreprocess);
		request.setProcessingRequest(processingRequest);
		return request;
	}
}

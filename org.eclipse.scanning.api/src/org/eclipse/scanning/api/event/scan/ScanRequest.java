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
package org.eclipse.scanning.api.event.scan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to encapsulate minimal information required to run a scan.
 *
 * For instance the JSON string of this class could be used on B23
 *
 * The class automatically assigns a unique id for the run.
 *
 * @author Matthew Gerring
 * @param <T> must be type of region that the regions correspond to. For instance IROI for any region type or IRectangularROI is all known to be rectangular.
 *
 */
public class ScanRequest<T> implements Serializable {
	private static Logger logger = LoggerFactory.getLogger(ScanRequest.class);

	private static final long serialVersionUID = 456095444930240261L;

	/**
	 * The models for generating the points for a scan
	 * The models must be in the same nested order that the
	 * compound scan will be generated as.
	 *
	 * e.g. a StepModel
	 */
	private CompoundModel<T> compoundModel;

	/**
	 * The names of the detectors to use in the scan
	 */
	private Map<String, Object> detectors = new HashMap<>(); // not emptyMap as we have a put method

	/**
	 * The names of monitors in the scan
	 */
	private Collection<String> monitorNamesPerPoint = Collections.emptyList();
	private Collection<String> monitorNamesPerScan = Collections.emptyList();

	/**
	 * The sample data which the user entered (if any) which determines
	 */
	private SampleData sampleData;

	/**
	 * Scan metadata that is not produced by a particular device, e.g.
	 * scan command, chemical formula etc., grouped by type.
	 */
	private List<ScanMetadata> scanMetadata; // TODO use EnumMap instead of list?

	/**
	 * Part or all of the file path to be used for this scan.
	 */
	private String filePath;

	/**
	 * The file paths to nexus template files. If the file path is relative it will be resolved
	 * relative to {@link IFilePathService#getPersistenceDir()}
	 */
	private Set<String> templateFilePaths;

	/**
	 * The start position or null if there is no start position to move to.
	 */
	private IPosition startPosition;

	/**
	 * The script run before the data collection but after the start position has been set.
	 */
	private ScriptRequest     beforeScript;

	/**
	 * The end position or null if there is no start position to move to.
	 */
	private IPosition endPosition;


	/**
	 * The script run after the data collection but before the end position has been set.
	 */
	private ScriptRequest afterScript;

	/**
	 * If <code>true</code>, the script defined in {@link #afterScript} will always be run, even if there is an error in
	 * the scan
	 */
	private boolean alwaysRunAfterScript;

	/**
	 * Set to ignore processing of this request if the request has been
	 * prepared for a specific server. For instance in the case where the client
	 * has build a legal scan request for a given beamline, it will not want this
	 * request preprocessed.
	 *
	 * Default is false.
	 */
	private boolean ignorePreprocess;

	private ProcessingRequest processingRequest;

	public ScanRequest() {

	}

	public ScanRequest(IScanPathModel m, String filePath, List<String> monitorNamesPerPoint, List<String> monitorNamesPerScan, ProcessingRequest processing) {
		super();
		this.compoundModel = new CompoundModel<T>(m);
		this.monitorNamesPerPoint = monitorNamesPerPoint;
		this.monitorNamesPerScan = monitorNamesPerScan;
		this.filePath = filePath;
		this.processingRequest = processing;
	}

	public ProcessingRequest getProcessingRequest() {
		return processingRequest;
	}

	public void setProcessingRequest(ProcessingRequest processingRequest) {
		this.processingRequest = processingRequest;
	}

	public ScanRequest(IScanPathModel m, T region, String filePath, List<String> monitorNamesPerPoint, List<String> monitorNamesPerScan) {
		this(m, filePath, monitorNamesPerPoint, monitorNamesPerScan, null);
		compoundModel.setRegions(Arrays.asList(new ScanRegion<T>(region, m.getScannableNames())));
	}

	public SampleData getSampleData() {
		return sampleData;
	}

	public void setSampleData(SampleData sampleData) {
		this.sampleData = sampleData;
	}

	public Collection<String> getMonitorNamesPerPoint() {
		return monitorNamesPerPoint;
	}

	public void setMonitorNamesPerPoint(Collection<String> monitorNames) {
		logger.trace("setMonitorNamesPerPoint({}) was {} ({})", monitorNames, this.monitorNamesPerPoint, this);
		this.monitorNamesPerPoint = monitorNames;
	}

	public Collection<String> getMonitorNamesPerScan() {
		return monitorNamesPerScan;
	}

	public void setMonitorNamesPerScan(Collection<String> monitorNames) {
		logger.trace("setMonitorNamesPerScan({}) was {} ({})", monitorNames, this.monitorNamesPerScan, this);
		this.monitorNamesPerScan = monitorNames;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Set<String> getTemplateFilePaths() {
		return templateFilePaths;
	}

	public void setTemplateFilePaths(Set<String> templateFilePaths) {
		this.templateFilePaths = templateFilePaths;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((afterScript == null) ? 0 : afterScript.hashCode());
		result = prime * result + (alwaysRunAfterScript ? 1231 : 1237);
		result = prime * result + ((beforeScript == null) ? 0 : beforeScript.hashCode());
		result = prime * result + ((compoundModel == null) ? 0 : compoundModel.hashCode());
		result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
		result = prime * result + ((endPosition == null) ? 0 : endPosition.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + (ignorePreprocess ? 1231 : 1237);
		result = prime * result + ((monitorNamesPerPoint == null) ? 0 : monitorNamesPerPoint.hashCode());
		result = prime * result + ((monitorNamesPerScan == null) ? 0 : monitorNamesPerScan.hashCode());
		result = prime * result + ((sampleData == null) ? 0 : sampleData.hashCode());
		result = prime * result + ((scanMetadata == null) ? 0 : scanMetadata.hashCode());
		result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanRequest<?> other = (ScanRequest<?>) obj;
		if (afterScript == null) {
			if (other.afterScript != null)
				return false;
		} else if (!afterScript.equals(other.afterScript))
			return false;
		if (alwaysRunAfterScript != other.alwaysRunAfterScript)
			return false;
		if (beforeScript == null) {
			if (other.beforeScript != null)
				return false;
		} else if (!beforeScript.equals(other.beforeScript))
			return false;
		if (compoundModel == null) {
			if (other.compoundModel != null)
				return false;
		} else if (!compoundModel.equals(other.compoundModel))
			return false;
		if (detectors == null) {
			if (other.detectors != null)
				return false;
		} else if (!detectors.equals(other.detectors))
			return false;
		if (endPosition == null) {
			if (other.endPosition != null)
				return false;
		} else if (!endPosition.equals(other.endPosition))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (ignorePreprocess != other.ignorePreprocess)
			return false;
		if (monitorNamesPerPoint == null) {
			if (other.monitorNamesPerPoint != null)
				return false;
		} else if (!monitorNamesPerPoint.equals(other.monitorNamesPerPoint))
			return false;
		if (monitorNamesPerScan == null) {
			if (other.monitorNamesPerScan != null)
				return false;
		} else if (!monitorNamesPerScan.equals(other.monitorNamesPerScan))
			return false;
		if (sampleData == null) {
			if (other.sampleData != null)
				return false;
		} else if (!sampleData.equals(other.sampleData))
			return false;
		if (scanMetadata == null) {
			if (other.scanMetadata != null)
				return false;
		} else if (!scanMetadata.equals(other.scanMetadata))
			return false;
		if (startPosition == null) {
			if (other.startPosition != null)
				return false;
		} else if (!startPosition.equals(other.startPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScanRequest [compoundModel=" + compoundModel + ", detectors=" + detectors + ", monitorNamesPerPoint="
				+ monitorNamesPerPoint + ", monitorNamesPerScan=" + monitorNamesPerScan + ", sampleData=" + sampleData
				+ ", scanMetadata=" + scanMetadata + ", filePath=" + filePath + ", startPosition=" + startPosition
				+ ", beforeScript=" + beforeScript + ", endPosition=" + endPosition + ", afterScript=" + afterScript
				+ ", alwaysRunAfterScript=" + alwaysRunAfterScript + ", ignorePreprocess=" + ignorePreprocess + "]";
	}

	public Map<String, Object> getDetectors() {
		if (detectors == null) return Collections.emptyMap();
		return detectors;
	}

	public void setDetectors(Map<String, Object> detectors) {
		this.detectors = detectors;
	}

	public void putDetector(String name, Object dmodel) {
		if (detectors==null) detectors = new HashMap<>(3);
		detectors.put(name, dmodel);
	}

	public IPosition getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(IPosition startPosition) {
		this.startPosition = startPosition;
	}

	public IPosition getEndPosition() {
		return endPosition;
	}

	public void setEnd(IPosition endPosition) {
		this.endPosition = endPosition;
	}

	public boolean isIgnorePreprocess() {
		return ignorePreprocess;
	}

	public void setIgnorePreprocess(boolean ignorePreprocess) {
		this.ignorePreprocess = ignorePreprocess;
	}

	public ScriptRequest getBeforeScript() {
		return beforeScript;
	}

	public void setBeforeScript(ScriptRequest before) {
		this.beforeScript = before;
	}

	public ScriptRequest getAfterScript() {
		return afterScript;
	}

	public void setAfterScript(ScriptRequest afterScript) {
		this.afterScript = afterScript;
	}

	public List<ScanMetadata> getScanMetadata() {
		return scanMetadata;
	}

	public void setScanMetadata(List<ScanMetadata> scanMetadata) {
		this.scanMetadata = scanMetadata;
	}

	public void addScanMetadata(ScanMetadata scanMetadata) {
		if (this.scanMetadata == null) {
			this.scanMetadata = new ArrayList<>();
		}
		this.scanMetadata.add(scanMetadata);
	}

	public CompoundModel<T> getCompoundModel() {
		return compoundModel;
	}

	public void setCompoundModel(CompoundModel<T> model) {
		this.compoundModel = model;
	}

	public boolean isAlwaysRunAfterScript() {
		return alwaysRunAfterScript;
	}

	public void setAlwaysRunAfterScript(boolean alwaysRunAfterScript) {
		this.alwaysRunAfterScript = alwaysRunAfterScript;
	}

}

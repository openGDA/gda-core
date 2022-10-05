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

package org.eclipse.scanning.sequencer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.scan.NexusScanConstants;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.sequencer.nexus.NexusScanFileManager;
import org.eclipse.scanning.sequencer.nexus.SolsticeConstants;
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
	public static final String PATHANDREGION = "pathAndRegion";
	public static final String PATHREGIONANDMUTATORS = "pathRegionAndMutators";
	public static final String COMPOUNDMODEL = "compoundModel";
	public static final String SCANPATHMODEL = "scanPathModel";
	public static final String REGION = "region";
	public static final String MUTATORS = "mutators";
	public static final String ORIGINAL_SCAN_NAME = "solstice_scan";
	public static final String SCAN_REQUEST = "scan_request";

	private CompoundModel model;

	private Map<String, IDetectorModel> detectors = new HashMap<>();

	private Collection<String> monitorNamesPerPoint = Collections.emptyList();
	private Collection<String> monitorNamesPerScan = Collections.emptyList();

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

	public ScanRequestBuilder() {
	}

	// Construct setting just the model
	public ScanRequestBuilder(IScanPointGeneratorModel model) {
		this.model = new CompoundModel(model);
	}

	// Construct setting model and arbitrary other values
	public ScanRequestBuilder(IScanPointGeneratorModel model, Map<String, Object> values) {
		this(model);
		values.entrySet().stream().forEach(entry -> setValue(entry.getKey(), entry.getValue()));
	}

	@SuppressWarnings("unchecked")
	private void setValue(String field, Object value) {
		try {
			switch(field) {
			case DETECTORS:
				withDetectors((Map<String, IDetectorModel>) value);
				break;
			case MONITOR_NAMES_PER_POINT:
				withMonitorNamesPerPoint((Collection<String>) value);
				break;
			case MONITOR_NAMES_PER_SCAN:
				withMonitorNamesPerScan((Collection<String>) value);
				break;
			case SCAN_METADATA:
				withScanMetadata((List<ScanMetadata>) value);
				break;
			case FILE_PATH:
				withFilePath((String) value);
				break;
			case TEMPLATE_FILE_PATHS:
				withTemplateFilePaths((Set<String>) value);
				break;
			case START_POSITION:
				withStartPosition((IPosition) value);
				break;
			case END_POSITION:
				withEndPosition((IPosition) value);
				break;
			case BEFORE_SCRIPT:
				withBeforeScript((ScriptRequest) value);
				break;
			case AFTER_SCRIPT:
				withAfterScript((ScriptRequest) value);
				break;
			case ALWAYS_RUN_AFTER_SCRIPT:
				alwaysRunAfterScript((boolean) value);
				break;
			case IGNORE_PREPROCESS:
				ignorePreprocess((boolean) value);
				break;
			case PROCESSING_REQUEST:
				withProcessingRequest((ProcessingRequest) value);
				break;
			case PATHANDREGION:
				Map<String, Object> prMap = (Map<String, Object>) value;
				withPathAndRegion((IScanPointGeneratorModel) prMap.get(SCANPATHMODEL), (IROI) prMap.get(REGION));
				break;
			case PATHREGIONANDMUTATORS:
				Map<String, Object> prmMap = (Map<String, Object>) value;
				withPathRegionAndMutators((IScanPointGeneratorModel) prmMap.get(SCANPATHMODEL), (IROI) prmMap.get(REGION), (List<IMutator>) prmMap.get(MUTATORS));
				break;
			case COMPOUNDMODEL:
				withCompoundModel((CompoundModel) value);
				break;
			default:
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

	public ScanRequestBuilder withPathAndRegion(IScanPointGeneratorModel scanPathModel, IROI region) {
		this.model = new CompoundModel(scanPathModel, region);
		return this;
	}

	public ScanRequestBuilder withPathRegionAndMutators(IScanPointGeneratorModel scanPathModel, IROI region, List<IMutator> mutators) {
		this.model = new CompoundModel(scanPathModel, region);
		this.model.setMutators(mutators);
		return this;
	}

	public ScanRequestBuilder withCompoundModel(CompoundModel model) {
		this.model = model;
		return this;
	}

	public ScanRequestBuilder withParameters(Map<String, Object> values) {
		values.entrySet().stream().forEach(entry -> setValue(entry.getKey(), entry.getValue()));
		return this;
	}

	public ScanRequest build() {
		final ScanRequest request = new ScanRequest();
		request.setCompoundModel(model);
		request.setDetectors(detectors);
		request.setMonitorNamesPerPoint(monitorNamesPerPoint);
		request.setMonitorNamesPerScan(monitorNamesPerScan);
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

	/**
	 * Retrieves a ScanRequest from a previously saved Nexus file if it contains one. The file must
	 * contain a NxEntry with the default name or that specified by NexusScanFileManager.getEntryName().
	 *
	 * @param nxFilename	The filename of the Nexus file
	 * @return				An Optional of the Scan Request, empty if the filename is null or blank
	 * @throws Exception	If any of the Nexus reading operations fail
	 */
	public static Optional<ScanRequest> buildFromNexusFile(final String nxFilename) throws Exception {
		if (nxFilename != null && !nxFilename.isBlank()) {
			final IMarshallerService marshaller = ServiceHolder.getMarshallerService();
			final INexusFileFactory nxFileFactory = ServiceHolder.getNexusFileFactory();

			try (NexusFile nxFile = nxFileFactory.newNexusFile(nxFilename)) {
				nxFile.openToRead();

				Optional<StringJoiner> pathJoiner = getJoiner(nxFile);  // includes currently set entry name

				if (pathJoiner.isPresent()) {
					// solstice_scan has been renamed to 'diamond_scan' so this will be the case in older files
					if (!nxFile.isPathValid(pathJoiner.get().add(NexusScanConstants.GROUP_NAME_DIAMOND_SCAN).toString())) {

						pathJoiner = getJoiner(nxFile);						// sorry, no reset() so have to re-get it
						pathJoiner.get().add(ORIGINAL_SCAN_NAME);
					}
					pathJoiner.get().add(SCAN_REQUEST);

					DataNode json = nxFile.getData(pathJoiner.get().toString());
					ScanRequest request = marshaller.unmarshal(json.toString(), ScanRequest.class);
					return Optional.of(request);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Retrieves an Optional of a String Joiner preloaded with the NXEntry name if the file contains a NXEntry
	 * whose name matches either the default name or that specified by NexusScanFileManager.getEntryName().
	 * Otherwise an empty Optional is returned and a warning is logged
	 *
	 * @param nxFile	The file to be checked for the NXEntry
	 * @return			Optional of the start of the path including the NXEntry name on successful match, or empty
	 */
	private static Optional<StringJoiner> getJoiner(NexusFile nxFile) {
		StringJoiner joiner = new StringJoiner("/","/","");

		// As we are only concerned with 'new' scanning files, we first try the appropriate constant
		String nxEntryName = NexusScanFileManager.getEntryName();
		if (nxFile.isPathValid(joiner.add(nxEntryName).toString())) {
			return Optional.of(joiner);
		} else {
			joiner = new StringJoiner("/","/","");														// sorry, no reset()
			if (nxFile.isPathValid(joiner.add(SolsticeConstants.DEFAULT_ENTRY_NAME).toString())) {
				return Optional.of(joiner);
			} else {
				logger.warn(
					"NXEntry name is neither the default, nor that set in the properties files ({}), cannot load Scan Definition", nxEntryName);
			}
		}
		return Optional.empty();
	}
}

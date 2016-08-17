/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

public class ScanRequestConverter {

	private static final Logger logger = LoggerFactory.getLogger(ScanRequestConverter.class);

	private MappingStageInfo mappingStageInfo;

	public void setMappingStageInfo(MappingStageInfo mappingStageInfo) {
		this.mappingStageInfo = mappingStageInfo;
	}

	/**
	 * Convert an IMappingExperimentBean to a ScanBean.
	 * <p>
	 * This will include setting the mapping scan axes with the names from the mapping axis manager.
	 * <p>
	 * This method is made <code>public</code> to allow testing.
	 *
	 * @param mappingExperimentBean
	 *            the IMappingExperimentBean to be converted
	 * @return the ScanBean
	 */
	public ScanBean convertToScanBean(IMappingExperimentBean mappingExperimentBean) {

		ScanBean scanBean = new ScanBean();
		String sampleName = mappingExperimentBean.getSampleMetadata().getSampleName();
		if (sampleName == null || sampleName.length() == 0) {
			sampleName = "unknown sample";
		}
		String pathName = mappingExperimentBean.getScanDefinition().getMappingScanRegion().getScanPath().getName();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		ScanRequest<IROI> req = new ScanRequest<IROI>();
		scanBean.setScanRequest(req);

		IMappingScanRegion scanRegion = mappingExperimentBean.getScanDefinition().getMappingScanRegion();

		if (mappingStageInfo != null) {
			IScanPathModel scanPath = scanRegion.getScanPath();
			if (scanPath instanceof AbstractBoundingBoxModel) {
				AbstractBoundingBoxModel boxModel = (AbstractBoundingBoxModel) scanPath;
				boxModel.setFastAxisName(mappingStageInfo.getActiveFastScanAxis());
				boxModel.setSlowAxisName(mappingStageInfo.getActiveSlowScanAxis());
			}
		} else {
			logger.warn("No mapping axis manager is set - the scan request will use default axis names!");
		}

		CompoundModel cmodel = new CompoundModel(scanRegion.getScanPath(), scanRegion.getRegion().toROI());
		// FIXME Outer scannables are not supported in the new compound model way yet!
//		for (IScanPathModelWrapper scanPathModelWrapper : eBean.getMappingExperimentBean().getScanDefinition().getOuterScannables()) {
//			if (scanPathModelWrapper.isIncludeInScan()) {
//				cmodel.addData(scanPathModelWrapper.getModel(), Arrays.asList(roi));
//			}
//		}

		req.setCompoundModel(cmodel);

		for (IDetectorModelWrapper detectorWrapper : mappingExperimentBean.getDetectorParameters()) {
			if (detectorWrapper.isIncludeInScan()) {
				req.putDetector(detectorWrapper.getName(), detectorWrapper.getModel());
			}
		}

		if (mappingExperimentBean.getScriptFiles() != null) {
			IScriptFiles scriptFiles = mappingExperimentBean.getScriptFiles();
			req.setBefore(getScriptRequest(scriptFiles.getBeforeScanScript()));
			req.setAfter(getScriptRequest(scriptFiles.getAfterScanScript()));
		}

		return scanBean;
	}

	private ScriptRequest getScriptRequest(String scriptFile) {
		if (scriptFile == null || scriptFile.isEmpty()) {
			return null;
		}

		final ScriptRequest scriptRequest = new ScriptRequest();
		scriptRequest.setLanguage(ScriptLanguage.SPEC_PASTICHE);
		scriptRequest.setFile(scriptFile);
		return scriptRequest;
	}

}

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

package org.opengda.detector.electronanalyser.nxdetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.SliceND;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSequenceHelper;
import org.opengda.detector.electronanalyser.event.SequenceFileChangeEvent;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionDatasetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

/*
 * A wrapper detector for VGScienta Electron Analyser, which takes a sequence file defining a list of
 * regions as input and collects analyser data: image, spectrum, and external IO data for each enabled
 * region. This uses a collection strategy to setup, collect, and save this region data immediately.
 *
 * @author Oli Wenman
 *
 */
@ServiceInterface(IEW4000.class)
public class EW4000 extends AbstractWriteRegionsImmediatelyNXDetector implements IEW4000{

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private static final long serialVersionUID = -222459754772057676L;
	private static final String REGION_LIST = "region_list";
	private static final String INVALID_REGION_LIST = "invalid_region_list";

	private String sequenceFileName;
	private transient EW4000CollectionStrategy collectionStrategy;
	private transient Scannable topup;

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin nxCollectionStrategyPlugin) {
		if (!(nxCollectionStrategyPlugin instanceof EW4000CollectionStrategy)) {
			throw new IllegalArgumentException("Invalid collection strategy used. Only " + EW4000CollectionStrategy.class + " is compatible.");
		}
		super.setCollectionStrategy(nxCollectionStrategyPlugin);
		collectionStrategy = (EW4000CollectionStrategy) nxCollectionStrategyPlugin;
	}

	@Override
	public void setSequenceFile(String sequenceFilename) throws DeviceException, FileNotFoundException {
		if (sequenceFilename == null) {
			throw new IllegalArgumentException("sequenceFilename cannot be null");
		}
		if (!Paths.get(sequenceFilename).isAbsolute()) {
			sequenceFilename = InterfaceProvider.getPathConstructor().createFromProperty("gda.ses.electronanalyser.seq.dir")
				+ File.separator
				+ sequenceFilename;
		}
		if (! new File(sequenceFilename).isFile()) {
			throw new FileNotFoundException("Sequence file \"" + sequenceFilename + "\" doesn't exist!");
		}

		SESSequence sequence = null;
		try {
			if (SESSequenceHelper.isFileXMLFormat(sequenceFilename)) {
				sequence = SESSequenceHelper.convertSequenceFileFromXMLToJSON(sequenceFilename);
			} else {
				sequence = SESSequenceHelper.loadSequence(sequenceFilename);
			}
			if (sequence == null) throw new DeviceException("Sequence from \"" + sequenceFilename + "\" is null");
		} catch (Exception e) {
			throw new DeviceException("Unable to load sequence from file \"{}\"", e);
		}
		collectionStrategy.setSequence(sequence);
		this.sequenceFileName = sequenceFilename;
	}

	@Override
	public void prepareForCollection() throws DeviceException{
		//ToDo - change to spring bean decoratee?
		try {
			final NDPluginBase pluginBase = collectionStrategy.getAnalyser().getNdArray().getPluginBase();
			final ADBase adBase = collectionStrategy.getAnalyser().getAdBase();
			if (!pluginBase.isCallbackEnabled()) {
				pluginBase.setNDArrayPort(adBase.getPortName_RBV());
				pluginBase.enableCallbacks();
				pluginBase.setBlockingCallbacks(1);
			}
			final NDStats ndStats = collectionStrategy.getAnalyser().getNdStats();
			final NDPluginBase pluginBase2 = ndStats.getPluginBase();
			if (!pluginBase2.isCallbackEnabled()) {
				pluginBase2.setNDArrayPort(adBase.getPortName_RBV());
				pluginBase2.enableCallbacks();
				pluginBase2.setBlockingCallbacks(1);
				ndStats.setComputeStatistics((short) 1);
				ndStats.setComputeCentroid((short) 1);
			}
		} catch (Exception e) {
			logger.error("Failed to initialise ADArray and ADStats Plugins", e);
		}
		super.prepareForCollection();
	}

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(final NXdetector detector, final NexusScanInfo info) {
		detector.setAttribute(null, NXdetector.NX_LOCAL_NAME, getName());
		detector.setAttribute(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE, "detector");
		final int[] scanDimensions = info.getOverallShape();
		final int numberOfRegions = collectionStrategy.getEnabledRegionNames().size();

		AnalyserRegionDatasetUtil.createOneDimensionalStructure(REGION_LIST, detector, new int[] {numberOfRegions}, String.class);
		getDataStorage().setupMultiDimensionalData(getName(), INVALID_REGION_LIST, scanDimensions, detector, new int[] {numberOfRegions}, String.class);

		String psuMode = "unknown";
		try {
			psuMode = collectionStrategy.getAnalyser().getPsuMode();
		} catch (Exception e) {
			logger.error("Unable to get {} mode to write to file",VGScientaAnalyser.PSU_MODE, e);
		}
		detector.setField(VGScientaAnalyser.PSU_MODE, psuMode);
		return new NexusObjectWrapper<>(getName(), detector);
	}

	@Override
	protected void setupAdditionalDataAxisFields(final NexusObjectWrapper<?> nexusWrapper, final int scanRank) {
		//Set up axes as [scannables, ..., region_list]
		final int regionAxisIndex = scanRank;
		nexusWrapper.setPrimaryDataFieldName(INVALID_REGION_LIST);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(REGION_LIST, INVALID_REGION_LIST, regionAxisIndex, regionAxisIndex);
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		logger.info("Updating clients to sequence file: {}", getSequenceFile());
		collectionStrategy.updateScriptController(new SequenceFileChangeEvent(getSequenceFile()));
	}

	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();
		if (topup == null) {
			logger.warn("topup is null");
		} else {
			//Block and wait for top-up injection to finish + topup.tolerance time.
			topup.atPointStart();
		}
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		//Not used as collection strategy writes own data when it needs to.
		//This class also writes own data before collectData of collectionStrategy.
	}

	@Override
	public String getSequenceFile() {
		return sequenceFileName;
	}

	@Override
	public void beforeCollectData() throws DeviceException{
		try {
			//Validate regions to skip over any during data collection. Get invalid region names to save after.
			final List<String> invalidRegionNames = collectionStrategy.validateRegions();
			getDataStorage().overridePosition(getName(), REGION_LIST, collectionStrategy.getEnabledRegionNames());
			getDataStorage().writeNewPosition(getName(), INVALID_REGION_LIST, invalidRegionNames);
		} catch (DatasetException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public boolean isBusy() {
		return collectionStrategy.isBusy();
	}

	@Override
	public String getDescription() throws DeviceException {
		return "VGH Scienta Electron Analyser EW4000";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "EW4000";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Electron Analyser";
	}

	@Override
	public SESRegion getCurrentRegion() {
		return collectionStrategy.getCurrentRegion();
	}

	public Scannable getTopup() {
		return topup;
	}

	public void setTopup(Scannable topup) {
		this.topup = topup;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return collectionStrategy.getAcquireTime();
	}
}
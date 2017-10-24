/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to delegate calls to a detector. Used for overriding method of an existing detector Turns a detector that does
 * not support PositionCallableProvider into one that does Functions add data to the NexusData provide helper functions
 * to get the image data from the detector for derived classes Passes data to processor. Once 1 thread per processor.
 */
public class NexusDetectorProcessor implements NexusDetector, PositionCallableProvider<GDANexusDetectorData>, HardwareTriggerableDetector {
	private static final Logger logger = LoggerFactory.getLogger(NexusDetectorProcessor.class);
	boolean mergeWithDetectorData=true;
	NexusDetector detector;
	NexusTreeProviderProcessor processor;

	private boolean readoutDone;

	boolean enableProcessing=true;

	public boolean isEnableProcessing() {
		return enableProcessing;
	}

	public void setEnableProcessing(boolean enableProcessing) {
		this.enableProcessing = enableProcessing;
	}

	public NexusDetectorProcessor() {
		super();
	}

	public NexusDetector getDetector() {
		return detector;
	}

	public void setDetector(NexusDetector detector) {
		this.detector = detector;
		// getExtraNames afresh
		clearProcessorCache();
	}

	public boolean isMergeWithDetectorData() {
		return mergeWithDetectorData;
	}

	public void setMergeWithDetectorData(boolean mergeWithDetectorData) {
		this.mergeWithDetectorData = mergeWithDetectorData;
	}

	public NexusTreeProviderProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(NexusTreeProviderProcessor processor) {
		this.processor = processor;
		clearProcessorCache();
	}

	/**
	 * Method to allow the correct setting of extraNames following enabling of processors
	 * Maybe the object should listen to the sub processors
	 */
	public void rescanProcessors(){
		clearProcessorCache();
	}
	protected void clearProcessorCache() {
		outputFormatCache = null;
		extraNamesCache = null;
	}

	String wrapperName = "NexusDetectorWrapper_Unnamed";
	private String[] outputFormatCache;
	private String[] extraNamesCache;

	@Override
	public void setName(String name) {
		// do not pass on this is allow the wrapper to be named
		// detector.setName(name);
		this.wrapperName = name;
	}

	@Override
	public void reconfigure() throws FactoryException {
		detector.reconfigure();
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Could not get position", e);
		}
	}

	@Override
	public String getName() {
		return wrapperName; // detector.getName();
	}

	@Override
	public void addIObserver(IObserver observer) {
		detector.addIObserver(observer);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		detector.setAttribute(attributeName, value);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		detector.deleteIObserver(observer);
	}

	@Override
	public String toString() {
		return "NexusDetectorWrapper [detector=" + detector + ", wrapperName=" + wrapperName + "]";
	}

	@Override
	public void deleteIObservers() {
		detector.deleteIObservers();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return detector.getAttribute(attributeName);
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		detector.moveTo(position);
	}

	@Override
	public void collectData() throws DeviceException {
		detector.collectData();
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		detector.asynchronousMoveTo(position);
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		detector.setCollectionTime(time);
	}

	@Override
	public void close() throws DeviceException {
		detector.close();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		detector.setProtectionLevel(newLevel);
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return detector.getCollectionTime();
	}

	@Override
	public int getStatus() throws DeviceException {
		return detector.getStatus();
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return detector.checkPositionValid(position);
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return detector.getProtectionLevel();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Could not get position for readout", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		detector.stop();
	}



	@Override
	public boolean isBusy() throws DeviceException {
		return detector.isBusy();
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return detector.getDataDimensions();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		detector.waitWhileBusy();
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		detector.prepareForCollection();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return detector.isAt(positionToTest);
	}

	@Override
	public void endCollection() throws DeviceException {
		detector.endCollection();
	}

	@Override
	public void setLevel(int level) {
		detector.setLevel(level);
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return detector.createsOwnFiles();
	}

	@Override
	public int getLevel() {
		return detector.getLevel();
	}

	@Override
	public String[] getInputNames() {
		return detector.getInputNames();
	}

	@Override
	public void setInputNames(String[] names) {
		detector.setInputNames(names);
	}

	@Override
	public String getDescription() throws DeviceException {
		return detector.getDescription();
	}

	@Override
	public String[] getExtraNames() {
		if (extraNamesCache == null) {
			String[] extraNames = mergeWithDetectorData ? detector.getExtraNames() : new String[]{};
			Collection<String> processorExtraNames = (processor != null || isEnableProcessing()) ? processor.getExtraNames() : null;
			if (processorExtraNames != null && !processorExtraNames.isEmpty()) {
				Vector<String> totalList = new Vector<String>();
				List<String> asList = Arrays.asList(extraNames);
				totalList.addAll(asList);
				totalList.addAll(processorExtraNames);
				extraNamesCache = totalList.toArray(new String[] {});
			} else {
				extraNamesCache = extraNames;
			}
		}
		return extraNamesCache;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detector.getDetectorID();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detector.getDetectorType();
	}

	@Override
	public void setExtraNames(String[] names) {
		detector.setExtraNames(names);
	}

	@Override
	public void setOutputFormat(String[] names) {
		detector.setOutputFormat(names);
	}

	@Override
	public String[] getOutputFormat() {
		if (outputFormatCache == null) {
			String[] outputFormat = mergeWithDetectorData ? detector.getOutputFormat(): new String[]{};
			Collection<String> processorOutputFormat = (processor != null || isEnableProcessing()) ? processor.getOutputFormat():null;
			if (processorOutputFormat != null && !processorOutputFormat.isEmpty()) {
				Vector<String> totalList = new Vector<String>();
				List<String> asList = Arrays.asList(outputFormat);
				totalList.addAll(asList);
				totalList.addAll(processorOutputFormat);
				outputFormatCache = totalList.toArray(new String[] {});
			} else {
				outputFormatCache = outputFormat;
			}

		}
		return outputFormatCache;

	}

	@Override
	public void atStart() throws DeviceException {
		// deprecated
	}

	@Override
	public void atEnd() throws DeviceException {
		// deprecated
	}

	@Override
	public void atScanStart() throws DeviceException {
		clearProcessorCache(); //ensure extraNames/outputFormats match enabled processors
		detector.atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		detector.atScanEnd();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		detector.atScanLineStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		detector.atScanLineEnd();
	}

	@Override
	public void atPointStart() throws DeviceException {
		positionCallableCache = null;
		detector.atPointStart();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		detector.atPointEnd();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		detector.atLevelMoveStart();
	}

	@Override
	public void atLevelStart() throws DeviceException {
		detector.atLevelStart();
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		detector.atLevelEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		detector.atCommandFailure();
	}

	@Override
	public String toFormattedString() {
		String res = detector.toFormattedString();
		return res.replaceFirst(detector.getName(), getName());
	}

	/*
	 * To allow multiple calls to getPositionCallable for the same point hold onto the result of the first call. Clear
	 * at atPointStart
	 */
	Callable<GDANexusDetectorData> positionCallableCache = null;

	@Override
	@SuppressWarnings("unchecked")
	public Callable<GDANexusDetectorData> getPositionCallable() throws DeviceException {
		if (readoutDone) {
			logger.error("getPositionCallable already called for " + getName());
		}
		/*
		 * create a callable that when called will process the data from the detector.
		 */
		if (positionCallableCache != null) {
			return positionCallableCache;
		}

		positionCallableCache = new Callable<GDANexusDetectorData>() {
			Callable<GDANexusDetectorData> callable;
			boolean merge= NexusDetectorProcessor.this.mergeWithDetectorData;
			{
				callable = null;
				if (detector instanceof PositionCallableProvider) {
					//TODO check type first
					callable = ((PositionCallableProvider<GDANexusDetectorData>) detector).getPositionCallable();
				} else {
					callable = new Callable<GDANexusDetectorData>() {
						GDANexusDetectorData pos;
						{
							//TODO check type first
							pos = (GDANexusDetectorData)detector.readout();
						}

						@Override
						public GDANexusDetectorData call() throws Exception {
							return pos;
						}
					};
				}
			}
			/*
			 * The single instance of the Callable may be referenced many times. The result of the call method is
			 * therefore stored on the first call and returned on subsequent calls. Synchronisation is used to avoid
			 * race conditions
			 */
			GDANexusDetectorData result = null;

			private synchronized GDANexusDetectorData getResult() throws Exception {
				if (result == null) {
					GDANexusDetectorData detectorNexusTreeProvider = callable.call();
					// do something with nexusTreeProvider before returning
					if( processor == null || !isEnableProcessing())
						result = detectorNexusTreeProvider;
					else{
						result = processor.process(detectorNexusTreeProvider);
						if( merge){
							result = detectorNexusTreeProvider.mergeIn( result);
						}
						if( result == null){
							result = new NXDetectorData();
						}

					}
				}
				return result;
			}


			@Override
			public GDANexusDetectorData call() throws Exception {

				return getResult();
			}
		};
		return positionCallableCache;
	}

	HardwareTriggerableDetector getHardDet(){
		if( detector instanceof HardwareTriggerableDetector)
			return (HardwareTriggerableDetector)detector;
		throw new UnsupportedOperationException("Detector '" + detector.getName() + "' is not a HardwareTriggerableDetector ");
	}

	@Override
	public void setHardwareTriggering(boolean b) throws DeviceException {
		getHardDet().setHardwareTriggering(b);
	}

	@Override
	public boolean isHardwareTriggering() {
		return getHardDet().isHardwareTriggering();
	}

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return getHardDet().getHardwareTriggerProvider();
	}

	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		getHardDet().setNumberImagesToCollect(numberImagesToCollect);
	}

	@Override
	public boolean integratesBetweenPoints() {
		return getHardDet().integratesBetweenPoints();
	}




}

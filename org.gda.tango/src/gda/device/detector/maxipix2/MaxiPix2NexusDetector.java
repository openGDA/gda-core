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

package gda.device.detector.maxipix2;

import fr.esrf.Tango.DevFailed;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.TangoUtils;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.lima.LimaBin;
import gda.device.lima.LimaCCD;
import gda.device.lima.LimaCCD.AcqMode;
import gda.device.lima.LimaFlip;
import gda.device.lima.LimaROIInt;
import gda.device.maxipix2.MaxiPix2;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;

import java.util.Vector;
import java.util.concurrent.Callable;

import org.nexusformat.NexusFile;

/*
 * MaxiPix2 detector that works in the mode: 1 readout/getPositionCallable per acquisition - multiple frames. Used to
 * taking multiple exposures with feedback to user after each acquisition. This is the default mode.
 * det.setNumberOfFrames(10) pos det 0.1 This will take 1 acquisition of 10 frames with timing of 0.1s. Timing done by
 * the detector. det.setNumberOfFrames(10) scan x - - - det 0.1 This will results in 1 acquisition per value of x. Each
 * acquisition takes 10 frames each 0.1 s apart. There will be 1 scandatapoint per value of x. Timing between each x
 * value is determined by GDA server. det.setNumberOfFrames(10) repscan 20 det 0.1 This will results in 20 acquisitions
 * each consisting of 10 frames each 0.1 s apart. There will be 20 scandatapoints. Timing between each point is
 * determined by GDA server.
 */
public class MaxiPix2NexusDetector extends DetectorBase implements NexusDetector, PositionCallableProvider<NexusTreeProvider>, HardwareTriggerableDetector {

	MaxiPix2MultiFrameDetector maxiPix2MultiFrameDetector;

	// number of frames for current acqusition. Used to check data returned from detector
	private int numberOfFramesCurrentAcq;

	// static Nexus data
	private double[] timesCurrentAcq;
	private INexusTree nexusMetaDataForLima;

	// flag used to control when to add static data to nexus tree
	boolean getPositionCalledForCurrentAcq = false;

	public MaxiPix2MultiFrameDetector getMaxiPix2MultiFrameDetector() {
		return maxiPix2MultiFrameDetector;
	}

	public void setMaxiPix2MultiFrameDetector(MaxiPix2MultiFrameDetector maxiPix2ContinuosScanDetector) {
		this.maxiPix2MultiFrameDetector = maxiPix2ContinuosScanDetector;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		setLocal(true);
		setExtraNames(new String[] { "lastImageNumber"});
		setOutputFormat(new String[]{"%d"});
		this.setInputNames(new String[0]);
		if (getMaxiPix2MultiFrameDetector() == null)
			throw new IllegalStateException("maxiPix2ContinuosScanDetector is not set");
	}

	@Override
	public void collectData() throws DeviceException {
		maxiPix2MultiFrameDetector.collectData();
		getPositionCalledForCurrentAcq = false;

	}

	public void setNumberOfFrames(int numberOfFrames) {
		maxiPix2MultiFrameDetector.setNumberOfFrames(numberOfFrames);
	}

	public int getNumberOfFrames() {
		return maxiPix2MultiFrameDetector.getNumberOfFrames();
	}

	INexusTree makeNexusTreeNode(String label, double data) {
		NexusGroupData groupData = new NexusGroupData(new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] { data });
		return new NexusTreeNode(label, NexusExtractor.SDSClassName, null, groupData);
	}
	INexusTree makeNexusTreeNode(String label, String data) {
		return new NexusTreeNode(label, NexusExtractor.SDSClassName, null, new NexusGroupData(data));
	}
	INexusTree makeNexusTreeNode(String label, Boolean data) {
		return new NexusTreeNode(label, NexusExtractor.SDSClassName, null, new NexusGroupData(data));
	}

	INexusTree getNexusMetaDataForLima(LimaCCD limaCCD, MaxiPix2 maxiPix2) throws DeviceException {
		try {
			NexusTreeNode top = new NexusTreeNode("top", NexusExtractor.NXDetectorClassName, null);
			top.addChildNode(makeNexusTreeNode("exposureTime", limaCCD.getAcqExpoTime()));
			top.addChildNode(makeNexusTreeNode("fillMode", maxiPix2.getFillMode().toString()));
			top.addChildNode(makeNexusTreeNode("energyThreshold", maxiPix2.getEnergyThreshold()));
			top.addChildNode(makeNexusTreeNode("threshold", maxiPix2.getThreshold()));
			AcqMode acqMode = limaCCD.getAcqMode();
			top.addChildNode(makeNexusTreeNode("acqMode", acqMode.toString()));
			if( acqMode.equals(LimaCCD.AcqMode.ACCUMULATION)){
				top.addChildNode(makeNexusTreeNode("accDeadTime", limaCCD.getAccDeadTime()));
				top.addChildNode(makeNexusTreeNode("accExpoTime", limaCCD.getAccExpoTime()));
				top.addChildNode(makeNexusTreeNode("accLiveTime", limaCCD.getAccLiveTime()));
				top.addChildNode(makeNexusTreeNode("accMaxExpoTime", limaCCD.getAccMaxExpoTime()));
				top.addChildNode(makeNexusTreeNode("accNbFrames", limaCCD.getAccNbFrames()));
				top.addChildNode(makeNexusTreeNode("accTimeMode", limaCCD.getAccTimeMode().toString()));
			}
			top.addChildNode(makeNexusTreeNode("latencyTime", limaCCD.getLatencyTime()));
			top.addChildNode(makeNexusTreeNode("acqNbFrames", limaCCD.getAcqNbFrames()));
			top.addChildNode(makeNexusTreeNode("imageType", limaCCD.getImageType().toString()));
			LimaBin imageBin = limaCCD.getImageBin();
			top.addChildNode(makeNexusTreeNode("imageBinX", imageBin.getBinX()));
			top.addChildNode(makeNexusTreeNode("imageBinY", imageBin.getBinY()));
			LimaFlip imageFlip = limaCCD.getImageFlip();
			top.addChildNode(makeNexusTreeNode("imageFlipX",imageFlip.getFlipX()));
			top.addChildNode(makeNexusTreeNode("imageFlipY",imageFlip.getFlipY()));

			LimaROIInt imageROIInt = limaCCD.getImageROIInt();
			top.addChildNode(makeNexusTreeNode("imageROIBeginX",imageROIInt.getBeginX()));
			top.addChildNode(makeNexusTreeNode("imageROIBeginY",imageROIInt.getBeginY()));
			top.addChildNode(makeNexusTreeNode("imageROIEndX",imageROIInt.getEndX()));
			top.addChildNode(makeNexusTreeNode("imageROIEndY",imageROIInt.getEndY()));
			
			return top;
		} catch (DevFailed e) {
			throw new DeviceException("Error getting metadata for limaCCD", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		nexusMetaDataForLima = getNexusMetaDataForLima(maxiPix2MultiFrameDetector.getLimaCCD(), maxiPix2MultiFrameDetector.getMaxiPix2());
		maxiPix2MultiFrameDetector.atScanStart();
		double collectionTimeCurrentAcq = getCollectionTime();
		numberOfFramesCurrentAcq = getNumberOfFrames();
		timesCurrentAcq = new double[numberOfFramesCurrentAcq];
		for (int i = 0; i < timesCurrentAcq.length; i++) {
			timesCurrentAcq[i] = i * collectionTimeCurrentAcq;
		}
		
	}

	@Override
	public int getStatus() throws DeviceException {
		return maxiPix2MultiFrameDetector.getStatus();
	}

	public boolean isFastMode() {
		return maxiPix2MultiFrameDetector.isFastMode();
	}

	public void setFastMode(boolean fastMode) {
		maxiPix2MultiFrameDetector.setFastMode(fastMode);
	}	
	
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Error in readout", e);
		}
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		FilePathNumber[] filePathNumberArray=null;
		Callable<FilePathNumber> filePathNumberCallable=null;
		if( !isHardwareTriggering()){
			filePathNumberArray = maxiPix2MultiFrameDetector.getFilePathNumberArray();
			if (filePathNumberArray.length != numberOfFramesCurrentAcq)
				throw new DeviceException("filePathNumberArray.length != numberOfFramesCurrentAcq");
		}else{
			filePathNumberCallable = maxiPix2MultiFrameDetector.getFilePathNumberCallable();
		}
		NexusTreeProviderCallable nexusTreeProviderCallable = new NexusTreeProviderCallable(getName(),
				nexusMetaDataForLima, filePathNumberArray, filePathNumberCallable, timesCurrentAcq, !getPositionCalledForCurrentAcq);
		getPositionCalledForCurrentAcq = true;
		return nexusTreeProviderCallable;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return maxiPix2MultiFrameDetector.getCollectionTime();
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		maxiPix2MultiFrameDetector.setCollectionTime(collectionTime);
	}

	@Override
	public String getDescription() throws DeviceException {
		return this.getClass().getName();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return this.getClass().getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return this.getClass().getName();
	}

	class NexusTreeProviderCallable implements Callable<NexusTreeProvider> {

		final private String name;
		final private INexusTree nexusMetaDataForLima1;
		private FilePathNumber[] filePathNumbers;
		private final double[] timesCurrentAcq2;
		private final boolean firstCall;
		private Callable<FilePathNumber> filePathNumberCallable;

		public NexusTreeProviderCallable(String name, INexusTree nexusMetaDataForLima,
				FilePathNumber[] filePathNumbers, Callable<FilePathNumber> filePathNumberCallable, double[] timesCurrentAcq, boolean firstCall) {
			this.name = name;
			this.nexusMetaDataForLima1 = nexusMetaDataForLima;
			this.filePathNumbers = filePathNumbers;
			this.filePathNumberCallable = filePathNumberCallable;
			timesCurrentAcq2 = timesCurrentAcq;
			this.firstCall = firstCall;
		}

		@Override
		public NexusTreeProvider call() throws Exception {

			//use NXDetectorDataWithFilepathForSrs so that filename is printed in terminal
			NXDetectorData data = new NXDetectorData(MaxiPix2NexusDetector.this);
			int lastImageNumber = -1;
			Vector<String> filenames = new Vector<String>();
			if( filePathNumberCallable != null){
				filePathNumbers = new FilePathNumber[]{filePathNumberCallable.call()};
			}
			for (FilePathNumber fPathNumber : filePathNumbers) {
				filenames.add(fPathNumber.path);
				lastImageNumber = fPathNumber.imageNumber;
			}

			NexusTreeNode fileNameNode = data.addFileNames(name, "image_data", filenames.toArray(new String[] {}), true, true);

			if (firstCall) {
				fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData(1)));
				fileNameNode.addChildNode(new NexusTreeNode("axes", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData("time")));

				data.addAxis(name, "time", new int[] { timesCurrentAcq2.length }, NexusFile.NX_FLOAT64,
						timesCurrentAcq2, 1, 1, "s", false);

				{
					INexusTree detTree = data.getDetTree(name);
					for (INexusTree item : nexusMetaDataForLima1) {
						detTree.addChildNode(item);
					}
				}
			}

			// must match the list of input and extra names
			data.setDoubleVals(new Double[] { new Double(lastImageNumber) });

			//The following lines go together
/*			data.addFileName(name, filenames.firstElement());
			data.setFilepathOutputFieldIndex(data.getDoubleVals().length);
*/
			return data;
		}

	}

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return maxiPix2MultiFrameDetector.getHardwareTriggerProvider();
	}

	@Override
	public boolean integratesBetweenPoints() {
		return maxiPix2MultiFrameDetector.integratesBetweenPoints();
	}

	@Override
	public void setHardwareTriggering(boolean b) throws DeviceException {
		maxiPix2MultiFrameDetector.setHardwareTriggering(b);
	}

	@Override
	public boolean isHardwareTriggering() {
		return maxiPix2MultiFrameDetector.isHardwareTriggering();
	}

	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		maxiPix2MultiFrameDetector.setNumberImagesToCollect(numberImagesToCollect);
	}

	@Override
	public void stop() throws DeviceException {
		maxiPix2MultiFrameDetector.stop();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		maxiPix2MultiFrameDetector.atScanEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		maxiPix2MultiFrameDetector.atCommandFailure();
	}
	
	
}

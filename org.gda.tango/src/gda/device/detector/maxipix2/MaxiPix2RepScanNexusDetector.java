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
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.RepScanScannable;
import gda.device.lima.LimaCCD;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;

import java.util.concurrent.Callable;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * MaxiPix detector that works in mode: 1. readout/getPositionCallable per frame of an acquisition - with timing done by
 * detector. Used to taking multiple exposures with feedback to user after each frame. This is configured by repscan
 * repscan 10 det 0.1 This will results in 1 acquisition consisting of 10 frames each 0.1 s apart. There will be 10
 * scandatapoints. The timing of the exposures is determined by the detector.
 */
public class MaxiPix2RepScanNexusDetector extends DetectorBase implements PositionCallableProvider<NexusTreeProvider>, NexusDetector,
		RepScanScannable {
	private static final Logger logger = LoggerFactory.getLogger(MaxiPix2RepScanNexusDetector.class);

	private MaxiPix2MultiFrameDetector maxiPix2MultiFrameDetector;

	// flag to determine when to add static data to NexusTree
	private boolean getPositionCalledForCurrentAcq = false;

	// static NexusTree data
	private INexusTree nexusMetaDataForLima;
	private double[] timesCurrentAcq;

	// counters used to control when to start an acqusition
	private int numRepFramesRemaining = 0;
	private int numRepScanFrames = 0;

	private boolean numRepScansIsPerScanLine=true;

	public boolean isNumRepScansIsPerScanLine() {
		return numRepScansIsPerScanLine;
	}

	/*
	 * To allow this detector to be used to monitor a set of triggers that represents a multidimensional scan
	 * rather than a single scan line set numRepScansIsPerScanLine to false. Default is true 
	 * If true call atRepScanStart with the total number of images to be made in the whole scan
	 */
	public void setNumRepScansIsPerScanLine(boolean numRepScansIsPerScanLine) {
		this.numRepScansIsPerScanLine = numRepScansIsPerScanLine;
	}

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
		setExtraNames(new String[] { "imageNumber" });
		this.setInputNames(new String[0]);
		if (getMaxiPix2MultiFrameDetector() == null)
			throw new IllegalStateException("maxiPix2ContinuosScanDetector is not set");
	}

	INexusTree makeNexusTreeNode(String label, double data) {
		NexusGroupData groupData = new NexusGroupData(new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] { data });
		return new NexusTreeNode(label, NexusExtractor.SDSClassName, null, groupData);
	}

	INexusTree getNexusMetaDataForLima(LimaCCD limaCCD) throws DeviceException {
		try {
			NexusTreeNode top = new NexusTreeNode("top", NexusExtractor.NXDetectorClassName, null);
			top.addChildNode(makeNexusTreeNode("exposureTime", limaCCD.getAcqExpoTime()));
			return top;
		} catch (DevFailed e) {
			throw new DeviceException("Error getting metadata for limaCCD", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public void collectData() throws DeviceException {
		if (numRepScanFrames == 0)
			throw new DeviceException("numRepScanFrames = 0");
		if (numRepFramesRemaining == numRepScanFrames) {
			maxiPix2MultiFrameDetector.collectData();
			nexusMetaDataForLima = getNexusMetaDataForLima(maxiPix2MultiFrameDetector.getLimaCCD());
			double collectionTimeCurrentAcq = getCollectionTime();
			timesCurrentAcq = new double[numRepScanFrames];
			for (int i = 0; i < timesCurrentAcq.length; i++) {
				timesCurrentAcq[i] = i * collectionTimeCurrentAcq;
			}
			getPositionCalledForCurrentAcq = false;

		}
		numRepFramesRemaining--;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		throw new UnsupportedOperationException(getName()
				+ " make sure you have enabled the MultithreadedScanDataPointPipeline. Only use with repscan");
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public int getStatus() throws DeviceException {
		return numRepFramesRemaining == 0 ? maxiPix2MultiFrameDetector.getStatus() : IDLE;
	}

	@Override
	public void atRepScanStart(int numberOfFrames) throws DeviceException {
		maxiPix2MultiFrameDetector.setNumberOfFrames(numberOfFrames);
		numRepScanFrames = numRepFramesRemaining = numberOfFrames;
	}

	public void setNumberOfFrames(int numberOfFrames) throws DeviceException {
		atRepScanStart(numberOfFrames);
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		super.atScanLineEnd();
		if( numRepScansIsPerScanLine)
			numRepFramesRemaining = numRepScanFrames;
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		numRepScanFrames = 0;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		logger.info("getPositionCallable numRepFramesRemaining:" + numRepFramesRemaining);
		return new NexusTreeProviderCallable(getName(), nexusMetaDataForLima,
				maxiPix2MultiFrameDetector.getFilePathNumberCallable(), timesCurrentAcq,
				!getPositionCalledForCurrentAcq);
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

		private final Callable<FilePathNumber> positionCallable;
		final private String name;
		final private INexusTree nexusMetaDataForLima1;
		private final double[] timesCurrentAcq2;
		private final boolean firstCall;

		public NexusTreeProviderCallable(String name, INexusTree nexusMetaDataForLima,
				Callable<FilePathNumber> callable, double[] timesCurrentAcq, boolean firstCall) {
			this.name = name;
			this.nexusMetaDataForLima1 = nexusMetaDataForLima;
			this.positionCallable = callable;
			timesCurrentAcq2 = timesCurrentAcq;
			this.firstCall = firstCall;
		}

		@Override
		public NexusTreeProvider call() throws Exception {
			FilePathNumber result = positionCallable.call();

			NXDetectorData data = new NXDetectorData(MaxiPix2RepScanNexusDetector.this);
			NexusTreeNode fileNameNode = data.addFileNames(name, "image_data", new String[] { result.path }, true, true);

			if (firstCall) {
				fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData(1)));
				fileNameNode.addChildNode(new NexusTreeNode("axes", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData("time")));

				//in a repscan an additional outer variable is scanned which is the time.
				//so we create the time axis to represent this variable with dim=0
				data.addAxis(name, "time", new int[] { timesCurrentAcq2.length }, NexusFile.NX_FLOAT64,
						timesCurrentAcq2, 0, 1, "s", false);

				{
					INexusTree detTree = data.getDetTree(name);
					for (INexusTree item : nexusMetaDataForLima1) {
						detTree.addChildNode(item);
					}
				}
			}

			data.setDoubleVals(new Double[] { new Double(result.imageNumber) });

			return data;
		}

	}
}

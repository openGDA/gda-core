package org.opengda.detector.electronanalyser.nxdata;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Electron analyser data appender to add point-dependent spectrum, image,
 * external IO and excitation energy data to the NXDetectorData to be written to
 * scan nexus data file at end of each scan data point collection.
 * 
 * @author fy65
 * 
 */
public class NXDetectorDataAnalyserRegionAppender implements
		NXDetectorDataAppender {
	// region name
	private List<INexusTree> regionDataList;
	private Logger logger = LoggerFactory.getLogger(NXDetectorDataAnalyserRegionAppender.class);
	private boolean firstInScan;
	private List<Double> totalIntensity=new ArrayList<Double>();

	public NXDetectorDataAnalyserRegionAppender(List<INexusTree> regionDataList, boolean firstInScan,List<Double> totalIntensity) {
		this.regionDataList = regionDataList;
		this.firstInScan = firstInScan;
		this.totalIntensity = totalIntensity;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName)
			throws DeviceException {
		INexusTree detTree = data.getDetTree(detectorName);
		synchronized (regionDataList) {
			if (firstInScan) {
				for (INexusTree regiontree : regionDataList) {
					NexusTreeNode regionNode=new NexusTreeNode(regiontree.getName(), regiontree.getNxClass(), null);
					detTree.addChildNode(regionNode);
				}
			}
			for (int i = 0; i < regionDataList.size(); i++) {
				INexusTree regiontree=regionDataList.get(i);
				NexusTreeNode regionNode = (NexusTreeNode) detTree.getChildNode(regiontree.getName(),regiontree.getNxClass());
				for (INexusTree regionchild : regiontree) {
					logger.debug("add {} to region node {} in detector tree.",regionchild.getName(), regiontree.getName());
					data.addData(regionNode, regionchild.getName(),regionchild.getData().dimensions, regionchild.getData().type,regionchild.getData().getBuffer(), null, null);
				}
				String regionName = regiontree.getName();
				Double regionIntensity = totalIntensity.get(i);
				data.setPlottableValue(regionName, regionIntensity);
			}
			
//			data.setDoubleVals(totalIntensity.toArray(new Double[] {}));
		}
	}

	// private void appendAnalyserSettings() {
	// // TODO Auto-generated method stub
	//
	// }

	// private void appendRegionExcitationEnergyData(INexusTree
	// regionTree,NXDetectorData data, String detectorName) throws
	// DeviceException {
	// int[] dims=new int[] {1};
	// double s;
	// try {
	// s = getAnalyser().getExcitationEnergy();
	// } catch (Exception e) {
	// throw new
	// DeviceException("Failed to get excitation energy from "+detectorName, e);
	// }
	// data.addData(regionTree, "excitation_energy", dims, NexusFile.NX_FLOAT64,
	// s, "eV", 1);
	// }
	//
	// private void appendRegionExternalIOData(INexusTree
	// regionTree,NXDetectorData data, String detectorName) throws
	// DeviceException {
	// int size = 0;
	// try {
	// if (getAnalyser().getAcquisitionMode().equalsIgnoreCase("Fixed")) {
	// size =1;
	// } else {
	// size = getAnalyser().getEnergyAxis().length;
	// }
	// } catch (Exception e) {
	// throw new
	// DeviceException("Failed to get the size of external IO data from "+detectorName,
	// e);
	// }
	// int[] dims=new int[] {size};
	// if (dims.length == 0) {
	// logger.warn("Dimensions of external IO data from " + detectorName +
	// " are zero length");
	// return;
	// }
	// double[] s;
	// try {
	// s = getAnalyser().getExternalIOData(dims[0]);
	// } catch (Exception e) {
	// throw new
	// DeviceException("Failed to get external IO data from "+detectorName, e);
	// }
	// data.addData(regionTree, "external_io_data", dims, NexusFile.NX_FLOAT64,
	// s, "count", 1);
	//
	// }
	//
	// private void appendRegionSpectrumData(INexusTree
	// regionTree,NXDetectorData data, String detectorName) throws
	// DeviceException {
	// int size;
	// try {
	// size = getAnalyser().getEnergyAxis().length;
	// } catch (Exception e) {
	// throw new DeviceException("Failed to get energy axis from "+detectorName,
	// e);
	// }
	// int[] dims=new int[] {size};
	// if (dims.length == 0) {
	// logger.warn("Dimensions of spectrum from " + detectorName +
	// " are zero length");
	// return;
	// }
	//
	// double[] s;
	// try {
	// s = getAnalyser().getSpectrum(dims[0]);
	// } catch (Exception e) {
	// throw new DeviceException("Failed to get spectrum from "+detectorName,
	// e);
	// }
	// data.addData(regionTree, "spectrum_data", dims, NexusFile.NX_FLOAT64, s,
	// "count", 1);
	//
	// }
	//
	// private void appendRegionImageData(INexusTree regionTree, NXDetectorData
	// data, String detectorName) throws DeviceException {
	// NDArray ndArray=getAnalyser().getNdArray();
	// int[] dims;
	// try {
	// dims = determineDataDimensions(ndArray);
	// } catch (Exception e) {
	// throw new
	// DeviceException("Failed to get NDArray dimension form "+detectorName,e);
	// }
	// if (dims.length == 0) {
	// logger.warn("Dimensions of NDArray data from " + detectorName +
	// " are zero length");
	// return;
	// }
	// int expectedNumPixels = dims[0];
	// for (int i = 1; i < dims.length; i++) {
	// expectedNumPixels = expectedNumPixels * dims[i];
	// }
	// float[] dataVals;
	// try {
	// dataVals = ndArray.getFloatArrayData(expectedNumPixels);
	// } catch (Exception e) {
	// throw new DeviceException("Failed to get NDArray data from "
	// +detectorName,e);
	// }
	// data.addData(regionTree, "image_data", dims, NexusFile.NX_INT32,
	// dataVals, "count", 1);
	// }

}

package gda.device.detector.xspress.xspress2data;

import gda.device.DeviceException;
import gda.device.detector.xspress.Xspress2System;
import gda.device.detector.xspress.XspressDetector;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;

/**
 * Holds the current settings for the Xspress2 detector. This object is shared
 * by the Xspress2System class which operates the hardware and
 * {@link Xspress2SystemData} which converts the raw output from the hardware
 * into usable chunks for Nexus file.
 * 
 * @author rjw82
 * 
 */
public class Xspress2CurrentSettings {

	int mcaGrades;
	private int fullMCABits = 12;
	private XspressParameters xspressParameters;
	private Double deadtimeEnergy; // in keV NOT eV!
	private boolean addDTScalerValuesToAscii = false;
	private String[] defaultOutputFormat = new String[] { "%5.4g" };

	public Xspress2CurrentSettings() {
	}

	public ArrayList<String> getChannelLabels() {
		ArrayList<String> channelLabels = new ArrayList<String>();
		if (getParameters().isOnlyShowFF())
			channelLabels.add("FF");
		else
			getChannelLabels(channelLabels, true);

		if (isAddDTScalerValuesToAscii()) {
			for (DetectorElement detector : getParameters().getDetectorList()) {
				if (!detector.isExcluded()) {
					channelLabels.add(detector.getName() + "_allEvents");
					channelLabels.add(detector.getName() + "_numResets");
					channelLabels.add(detector.getName() + "_inWinEvents");
					channelLabels.add(detector.getName() + "_tfgClock");
				}
			}
		}
		return channelLabels;
	}

	/*
	 * @param channelLabels
	 * 
	 * @param filteroutExcludedChannels - true if you only want included
	 * detectors (elements)
	 */
	private void getChannelLabels(ArrayList<String> channelLabels, boolean filteroutExcludedChannels) {
		if (getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			// loop through all elements and find all the virtual scalers
			if (getMcaGrades() != Xspress2System.ALL_RES) {
				for (DetectorElement detector : getParameters().getDetectorList()) {
					if (!detector.isExcluded() || !filteroutExcludedChannels) {
						String channelName = detector.getName() + "_";
						for (XspressROI roi : detector.getRegionList())
							channelLabels.add(channelName + roi.getRoiName());
					}
				}
			}
			// when all 16 grades separate, then a 'full' list of channels means
			// 16 resGrade bins: 15, 15+14,
			// 15+14+13....
			else {
				for (int i = 0; i < 16; i++)
					channelLabels.add("res_bin_" + i + "_norm");
				for (DetectorElement element : getParameters().getDetectorList())
					if (!element.isExcluded() || !filteroutExcludedChannels)
						channelLabels.add(element.getName() + "_best8");
			}
			channelLabels.add("FF");

			if (getMcaGrades() == Xspress2System.RES_THRES)
				channelLabels.add("FF_bad");

		} else {
			for (DetectorElement detector : getParameters().getDetectorList())
				if (!detector.isExcluded() || !filteroutExcludedChannels)
					channelLabels.add(detector.getName());
			channelLabels.add("FF");
		}
	}

	public String[] getExtraNames() {
		ArrayList<String> labels = getChannelLabels();
		return labels.toArray(new String[0]);
	}

	public String[] getOutputFormat() {
		if (getParameters().isOnlyShowFF()
				|| !(getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS) && getMcaGrades() == Xspress2System.ALL_RES))
			return defaultOutputFormat();
		return getAllResGradesInAsciiOutputFormat();
	}

	private String[] defaultOutputFormat() {
		return defaultOutputFormat;
	}

	public void setDefaultOutputFormat(String[] outputFormat) {
		defaultOutputFormat = outputFormat;
	}

	/*
	 * for only when all res grades being displayed separately in ascii output
	 */
	private String[] getAllResGradesInAsciiOutputFormat() {
		ArrayList<String> format = new ArrayList<String>();
		for (int i = 0; i < 16; i++)
			format.add("%.4g");
		// for best8 grades for each element
		for (int i = 0; i < getParameters().getDetectorList().size(); i++)
			if (!isDetectorElementExcluded(i))
				format.add(defaultOutputFormat()[0]);
		// for FF
		format.add(defaultOutputFormat()[0]);

		if (isAddDTScalerValuesToAscii())
			for (int i = 0; i < getParameters().getDetectorList().size(); i++)
				if (!isDetectorElementExcluded(i))
					for (int j = 0; j < 4; j++)
						format.add(defaultOutputFormat()[0]);
		return format.toArray(new String[0]);
	}

	private boolean isDetectorElementExcluded(int i) {
		return getParameters().getDetectorList().get(i).isExcluded();
	}

	public List<DetectorElement> getDetectorElements() {
		return xspressParameters.getDetectorList();
	}

	public void setDetectorElements(List<DetectorElement> detectorElements) {
		xspressParameters.setDetectorList(detectorElements);
	}

	public int getMcaGrades() {
		return mcaGrades;
	}

	public void setMcaGrades(int mcaGrades) {
		this.mcaGrades = mcaGrades;
	}

	public int getMcaSize() {
		return getFullMCASize();
	}

	public XspressParameters getParameters() {
		return xspressParameters;
	}

	public void setXspressParameters(XspressParameters xspressParameters) {
		this.xspressParameters = xspressParameters;
	}

	public int getNumberOfDetectors() {
		return getDetectorElements().size();
	}

	public boolean isDetectorExcluded(int detectorElementNumber) {
		return getDetectorElements().get(detectorElementNumber).isExcluded();
	}

	public Double getDeadtimeEnergy() {
		return deadtimeEnergy;
	}

	public void setDeadtimeEnergy(Double deadtimeEnergy) {
		this.deadtimeEnergy = deadtimeEnergy;
	}

	public int getNumberOfIncludedDetectors() {
		int numFilteredDetectors = 0;
		for (int element = 0; element < getDetectorElements().size(); element++)
			if (!getDetectorElements().get(element).isExcluded())
				numFilteredDetectors++;
		return numFilteredDetectors;
	}

	public boolean isAddDTScalerValuesToAscii() {
		return addDTScalerValuesToAscii;
	}

	public void setAddDTScalerValuesToAscii(boolean addDTScalerValuesToAscii) {
		this.addDTScalerValuesToAscii = addDTScalerValuesToAscii;
	}

	/*
	 * @return Returns the bit size of a full mca (12 = 4096)
	 */
	public int getFullMCABits() {
		return fullMCABits;
	}

	public int getFullMCASize() {
		return 1 << fullMCABits;
	}

	public void setFullMCABits(int fullMCABits) throws DeviceException {
		this.fullMCABits = fullMCABits;
	}
}

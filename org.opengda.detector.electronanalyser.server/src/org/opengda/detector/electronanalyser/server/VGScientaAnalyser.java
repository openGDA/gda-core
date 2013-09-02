/**
 * Copyright © 2012 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.server;

import gda.data.nexus.NeXusUtils;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.addetector.ArrayData;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class VGScientaAnalyser extends ADDetector implements IVGScientaAnalyser {

	private static final long serialVersionUID = -2907729482321978030L;

	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyser.class);

	private VGScientaController controller;
	private AnalyserCapabilities capabilites;
	private int[] fixedModeRegion;
	private int[] sweptModeRegion;

	private NDProcess ndProc;

	private NeXusFileInterface nexusFile;

	private String regionName;

	private String cachedEnergyMode;


	@Override
	public AnalyserCapabilities getCapabilities() {
		return capabilites;
	}

	@Override
	public void setCapabilities(AnalyserCapabilities ac) {
		this.capabilites = ac;
	}

	@Override
	public VGScientaController getController() {
		return controller;
	}

	@Override
	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	@Override
	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getTotalDataPoints();
	}

	@Override
	public double[] getEnergyAxis() throws Exception {
		double start, step;
		int length, startChannel = 0;
		if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
//			int pass = controller.getPassEnergy().intValue();
//			start = controller.getCentreEnergy() - (getCapabilities().getEnergyWidthForPass(pass) / 2);
//			step = getCapabilities().getEnergyStepForPass(pass);
			start = controller.getStartEnergy();
			step = controller.getEnergyStep();
			length = getAdBase().getSizeX();
			startChannel = getAdBase().getMinX();
		} else {
			start = controller.getStartEnergy();
			step = controller.getEnergyStep();
			length = getNumberOfSweeptSteps();
		}

		double[] axis = new double[length];
		for (int j = 0; j < length; j++) {
			axis[j] = start + (j + startChannel) * step;
		}
		return axis;
	}

	@Override
	public double[] getAngleAxis() throws Exception {
		return getCapabilities().getAngleAxis(controller.getLensMode(),
				getAdBase().getMinY_RBV(), controller.getSlice(), getAdBase().getArraySizeY_RBV());
	}

	@Override
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		short state = getAdBase().getDetectorState_RBV();
		switch (state) {
			case 6:
				throw new DeviceException("analyser in error state during readout");
			case 1:
				// The IOC can report acquiring for quite a while after being stopped
				logger.debug("analyser status is acquiring during readout although we think it has stopped");
				break;
			case 10:
				logger.warn("analyser in aborted state during readout");
				break;
			default:
				break;
		}
		if (firstReadoutInScan) {
			int i = 1;
			String aname = "energies";
			String aunit = "eV";
			double[] axis = getEnergyAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = "pixel";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);

			data.addData(getName(), "lens_mode", new NexusGroupData(getLensMode()), null, null);
			
			data.addData(getName(), "pass_energy", new int[] {1}, NexusFile.NX_INT32, new int[] { getPassEnergy() }, null, null);

			data.addData(getName(), "acquisition_mode", new NexusGroupData(getAcquisitionMode()), null, null);
			
			data.addData(getName(), "energy_mode", new NexusGroupData( getEnergyMode() ), null, null);

			data.addData(getName(), "detector_mode", new NexusGroupData( getDetectorMode() ), null, null);

			data.addData(getName(), "sensor_size", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV() }, null, null);

			data.addData(getName(), "region_origin", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV() }, null, null);

			data.addData(getName(), "region_size", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV() }, null, null);

			data.addData(getName(), "number_of_iterations", new int[] {1}, NexusFile.NX_INT32, new int[] { getNumberIterations() }, null, null);
		}
	}
	
	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {
		super.appendNXDetectorDataFromCollectionStrategy(data);
		// add additional data (image/array data are already added by the framework createNXDetectorData() by default)
		double[] spectrum=null;
		spectrum = getSpectrum();
		if (spectrum!=null) {
			data.addData(getName(), "spectrum", new int[] {spectrum.length}, NexusFile.NX_FLOAT64, spectrum, "counts", null);
		}
		double[] externalIO=null;
		externalIO = getExternalIOData();
		if (externalIO!=null) {
			data.addData(getName(), "externalIO", new int[] {externalIO.length}, NexusFile.NX_FLOAT64, externalIO, null, null);
		}
	
	}
	@Override
	public void collectData() throws DeviceException {
		//TODO test this
		
//		try {
//			getAdBase().startAcquiringSynchronously();
//		} catch (Exception e) {
//			throw new DeviceException(e);
//		}
		super.collectData();
	}

	public void writeOut(int scanDataPoint)  {
		try {
	//		InterfaceProvider.getTerminalPrinter().print("Writing region " + getRegionName() + " data to file : "+ datafilepath+". Please wait ......" );
			nexusFile.opengroup("entry1","NXentry");
			nexusFile.opengroup("instrument", "NXinstrument");
			if (nexusFile.groupdir().get("detector") == null) {
				nexusFile.makegroup("detector","NXdetector");
			}
			nexusFile.opengroup("detector", "NXdetector");
			if (scanDataPoint == 0) {
				try {
					String lensMode= getLensMode();
					//write analyser parameters here
					NeXusUtils.writeNexusString(nexusFile, "reagion_name", getRegionName());
					NeXusUtils.writeNexusString(nexusFile, "lens_mode", lensMode);
					NeXusUtils.writeNexusString(nexusFile, "acquisition_mode", getAcquisitionMode());
					NeXusUtils.writeNexusString(nexusFile, "energy_mode", getEnergyMode());
					NeXusUtils.writeNexusString(nexusFile, "detector_mode", getDetectorMode());
					NeXusUtils.writeNexusInteger(nexusFile, "pass_energy", getPassEnergy());
					double excitationEnergy = getExcitationEnergy();
					if (getCachedEnergyMode().equalsIgnoreCase("Binding")) {
						NeXusUtils.writeNexusDouble(nexusFile, "low_energy", excitationEnergy-getEndEnergy(), "eV");
						NeXusUtils.writeNexusDouble(nexusFile, "high_energy", excitationEnergy-getStartEnergy(), "eV");
						NeXusUtils.writeNexusDouble(nexusFile, "fixed_energy", excitationEnergy-getCentreEnergy(), "eV");
						
					} else {
						NeXusUtils.writeNexusDouble(nexusFile, "low_energy", getStartEnergy(), "eV");
						NeXusUtils.writeNexusDouble(nexusFile, "high_energy", getEndEnergy(), "eV");
						NeXusUtils.writeNexusDouble(nexusFile, "fixed_energy", getCentreEnergy(), "eV");
					}
					NeXusUtils.writeNexusDouble(nexusFile, "energy_step", getEnergyStep(), "eV");
					double stepTime = getStepTime();
					NeXusUtils.writeNexusDouble(nexusFile, "step_time", stepTime, "s");
					NeXusUtils.writeNexusInteger(nexusFile, "number_of_slices", getSlices());
					NeXusUtils.writeNexusInteger(nexusFile, "number_of_iterations", getNumberIterations());
					int totalSteps = getTotalSteps().intValue();
					NeXusUtils.writeNexusInteger(nexusFile, "total_steps", totalSteps);
					NeXusUtils.writeNexusDouble(nexusFile, "total_time", totalSteps*stepTime, "s");
					int cameraMinX = getCameraMinX();
					NeXusUtils.writeNexusInteger(nexusFile, "detector_x_from", cameraMinX);
					int cameraMinY = getCameraMinY();
					NeXusUtils.writeNexusInteger(nexusFile, "detector_y_from", cameraMinY);
					NeXusUtils.writeNexusInteger(nexusFile, "detector_x_to", getCameraSizeX()-cameraMinX);
					NeXusUtils.writeNexusInteger(nexusFile, "detector_y_to", getCameraSizeY()-cameraMinY);
					// write axis
					int i = 1;
					String aname = "energies";
					String aunit = "eV";
					double[] axis;
					try {
						axis = getEnergyAxis();
						//convert EPICS Kinetic energy to GDA Binding energies
						if (getCachedEnergyMode().equalsIgnoreCase("Binding")) {
							for (int j=0; j<axis.length; j++) {
								axis[j]=excitationEnergy-axis[j];
							}
						}

						NeXusUtils.writeNexusDoubleArray(nexusFile, aname, axis);
						nexusFile.opendata(aname);
						nexusFile.putattr("axis", new int[] {i+1}, NexusFile.NX_INT32);
						nexusFile.putattr("primary", new int[] {1}, NexusFile.NX_INT32);
						nexusFile.putattr("unit", aunit.getBytes(), NexusFile.NX_CHAR);
						nexusFile.closedata();
					} catch (Exception e) {
						logger.error("failed to get energy axis data from analyer.", e);
					}
					
					i = 0;
					if ("Transmission".equals(lensMode)) {
						aname = "location";
						aunit = "pixel";
					} else {
						aname = "angles";
						aunit = "degree";
					}
					try {
						axis = getAngleAxis();
						NeXusUtils.writeNexusDoubleArray(nexusFile, aname, axis);
						nexusFile.opendata(aname);
						nexusFile.putattr("axis", new int[] {i+1}, NexusFile.NX_INT32);
						nexusFile.putattr("primary", new int[] {1}, NexusFile.NX_INT32);
						nexusFile.putattr("unit", aunit.getBytes(), NexusFile.NX_CHAR);
						nexusFile.closedata();
					} catch (Exception e) {
						logger.error("failed to get angle or location axis data from analyer.", e);
					
					}
				} catch (Exception e) {
					logger.error("failed to get analyser parameters on write data out",e);
				}
			}
			// write data that changes with scan data point here
			writeImageData(scanDataPoint);
			writeSpectrumData(scanDataPoint);
			writeExternalIOData(scanDataPoint);
			writeExciationEnergy(scanDataPoint);
			//close detector
			nexusFile.closegroup();
			//close instrument
			nexusFile.closegroup();
			//close entry1
			nexusFile.closegroup();
			//TODO Test this
			nexusFile.flush();
		} catch (NexusException e) {
			logger.error("NexusException on write data out",e);
		} 
	}

	private void writeImageData(int scanDataPoint) {
		try {
			NDArray ndArray=getNdArray();
			int[] dims=ArrayData.determineDataDimensions(ndArray);
			if (dims.length == 0) {
				logger.warn("Dimensions of NDArray data from " + getName() + " are zero length");
				return;
			}
			int[] datadims = new int[] {NexusFile.NX_UNLIMITED , dims[0], dims[1] };
			int rank = datadims.length;
			if (scanDataPoint == 1) {
				nexusFile.makedata("image_data", NexusFile.NX_INT32, rank, datadims);
			}
			nexusFile.opendata("image_data");
			int[] startPos = new int[rank];
			int[] slabdatadims = new int[] { 1, dims[0], dims[1] };
	
			int expectedNumPixels = dims[0];
			for (int i = 1; i < dims.length; i++) {
				expectedNumPixels = expectedNumPixels * dims[i];
			}
			float[] s = ndArray.getFloatArrayData(expectedNumPixels);
			startPos[0] = scanDataPoint;
			nexusFile.putslab(s, startPos, slabdatadims);
			nexusFile.closedata();
		} catch (NexusException e) {
			logger.error("Error writing image data to nexus file. ", e);
		} catch (Exception e) {
			logger.error("Failed to get NDArray data from EPICS plugin. ",e);
		}
	}
	private void writeSpectrumData(int scanDataPoint) {
		try {
			int size = getEnergyAxis().length;
			int[] dims=new int[] {size};
			if (dims.length == 0) {
				logger.warn("Dimensions of spectrum from " + getName() + " are zero length");
				return;
			}
			int[] datadims = new int[] {NexusFile.NX_UNLIMITED , dims[0] };
			int rank = datadims.length;
			if (scanDataPoint == 1) {
				nexusFile.makedata("spectrum_data", NexusFile.NX_FLOAT64, rank, datadims);
			}
			nexusFile.opendata("spectrum_data");
			int[] startPos = new int[rank];
			int[] slabdatadims = new int[] { 1, dims[0] };
	
			double[] s = getSpectrum(dims[0]);
			startPos[0] = scanDataPoint;
			nexusFile.putslab(s, startPos, slabdatadims);
			nexusFile.closedata();
		} catch (NexusException e) {
			logger.error("Error writing spectrum data to nexus file. ", e);
		} catch (Exception e) {
			logger.error("Failed to get spectrum data from EPICS analyser. ",e);
		}
	}

	private void writeExternalIOData(int scanDataPoint) {
		try {
			int size;
			if (getAcquisitionMode().equalsIgnoreCase("Fixed")) {
				size =1;
			} else {
				size = getEnergyAxis().length;
			}
			int[] dims=new int[] {size};
			if (dims.length == 0) {
				logger.warn("Dimensions of external IO data from " + getName() + " are zero length");
				return;
			}
			int[] datadims = new int[] {NexusFile.NX_UNLIMITED , dims[0] };
			int rank = datadims.length;
			if (scanDataPoint == 1) {
				nexusFile.makedata("external_io_data", NexusFile.NX_FLOAT64, rank, datadims);
			}
			nexusFile.opendata("external_io_data");
			int[] startPos = new int[rank];
			int[] slabdatadims = new int[] { 1, dims[0] };
	
			double[] s = getExternalIOData(dims[0]);
			startPos[0] = scanDataPoint;
			nexusFile.putslab(s, startPos, slabdatadims);
			nexusFile.closedata();
		} catch (NexusException e) {
			logger.error("Error writing external IO data to nexus file. ", e);
		} catch (Exception e) {
			logger.error("Failed to get external IO data from EPICS analyser. ",e);
		}
	}
	private void writeExciationEnergy(int scanDataPoint) {
		try {
			int[] dims=new int[] {1};
			int[] datadims = new int[] {NexusFile.NX_UNLIMITED , dims[0] };
			int rank = datadims.length;
			if (scanDataPoint == 1) {
				nexusFile.makedata("excitation_energy", NexusFile.NX_FLOAT64, rank, datadims);
			}
			nexusFile.opendata("excitation_energy");
			int[] startPos = new int[rank];
			int[] slabdatadims = new int[] { 1, dims[0] };
	
			double s = getExcitationEnergy();
			double[] ee= new double[]{s};
			startPos[0] = scanDataPoint;
			nexusFile.putslab(ee, startPos, slabdatadims);
			nexusFile.closedata();
		} catch (NexusException e) {
			logger.error("Error writing excitation energy to nexus file. ", e);
		} catch (Exception e) {
			logger.error("Failed to get excitation energy from EPICS analyser. ",e);
		}
	}


	public double[] getExternalIOData(int i) throws TimeoutException, CAException, InterruptedException, Exception {
		return controller.getExtIO(i);
	}

	public double[] getSpectrum(int i) throws TimeoutException, CAException, InterruptedException, Exception {
		return controller.getSpectrum(i);
	}

	@Override
	public void setFixedMode(boolean fixed) throws Exception {
		int[] region = fixedModeRegion;
		if (fixed) {
			controller.setAcquisitionMode("Fixed");
		} else {
			controller.setAcquisitionMode("Swept");
			if (getSweptModeRegion() != null) {
				region = getSweptModeRegion();
			}
		}
		getAdBase().setMinX(region[0]);
		getAdBase().setMinY(region[1]);
		getAdBase().setSizeX(region[2]);
		getAdBase().setSizeY(region[3]);
		controller.setSlice(region[3]);
		getAdBase().setImageMode(0);
		getAdBase().setTriggerMode(0);
	}
	
	
	public int[] getSweptModeRegion() {
		return sweptModeRegion;
	}

	public void setSweptModeRegion(int[] sweptModeRegion) {
		this.sweptModeRegion = sweptModeRegion;
	}

	@Override
	public int[] getFixedModeRegion() {
		return fixedModeRegion;
	}

	@Override
	public void setFixedModeRegion(int[] fixedModeRegion) {
		this.fixedModeRegion = fixedModeRegion;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return getAdBase().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("error getting collection time", e);
		}
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			getAdBase().setAcquireTime(collectionTime);
		} catch (Exception e) {
			throw new DeviceException("error setting collection time", e);
		}
	}
	
	@Override
	public void setNumberInterations(int value) throws Exception {
		getAdBase().setNumExposures(value);
	}
	@Override
	public void setNumberInterations(int value, double timeout) throws Exception {
		getAdBase().setNumExposures(value, timeout);
	}
	
	@Override
	public Integer getNumberIterations() throws Exception {
		return getAdBase().getNumExposures_RBV();
	}
	@Override
	public void setCameraMinX(int value) throws Exception {
		getAdBase().setMinX(value);
	}

	@Override
	public void setCameraMinX(int value, double timeout) throws Exception {
		getAdBase().setMinXWait(value, timeout);
	}

	@Override
	public int getCameraMinX() throws Exception {
		return getAdBase().getMinX_RBV();
	}
	@Override
	public void setCameraMinY(int value) throws Exception {
		getAdBase().setMinY(value);
	}
	@Override
	public void setCameraMinY(int value, double timeout) throws Exception {
		getAdBase().setMinYWait(value, timeout);
	}
	
	@Override
	public int getCameraMinY() throws Exception {
		return getAdBase().getMinY_RBV();
	}
	@Override
	public void setCameraSizeX(int value) throws Exception {
		getAdBase().setSizeX(value);
	}
	@Override
	public void setCameraSizeX(int value, double timeout) throws Exception {
		getAdBase().setSizeXWait(value, timeout);
	}
	@Override
	public int getCameraSizeX() throws Exception {
		return getAdBase().getSizeX_RBV();
	}
	@Override
	public void setCameraSizeY(int value) throws Exception {
		getAdBase().setSizeY(value);
	}
	@Override
	public void setCameraSizeY(int value, double timeout) throws Exception {
		getAdBase().setSizeYWait(value, timeout);
	}
	@Override
	public void setImageMode(ImageMode imagemode) throws Exception {
		getAdBase().setImageMode(imagemode);
	}
	@Override
	public void setImageMode(ImageMode imagemode, double timeout) throws Exception {
		getAdBase().setImageModeWait(imagemode, timeout);
	}
	@Override
	public int getCameraSizeY() throws Exception {
		return getAdBase().getSizeY_RBV();
	}

	@Override
	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}
	@Override
	public void setLensMode(String value, double timeout) throws Exception {
		controller.setLensMode(value, timeout);
	}
	@Override
	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}
	@Override
	public void setAcquisitionMode(String value) throws Exception {
		controller.setAcquisitionMode(value);
	}
	@Override
	public void setAcquisitionMode(String value, double timeout) throws Exception {
		controller.setAcquisitionMode(value, timeout);
	}

	@Override
	public String getAcquisitionMode() throws Exception {
		return controller.getAcquisitionMode();
	}
	@Override
	public void setEnergyMode(String value) throws Exception {
		controller.setEnergyMode(value);
	}
	@Override
	public void setEnergyMode(String value, double timeout) throws Exception {
		controller.setEnergyMode(value, timeout);
	}

	@Override
	public String getEnergyMode() throws Exception {
		return controller.getEnergyMode();
	}

	@Override
	public void setDetectorMode(String value) throws Exception {
		controller.setDetectorMode(value);
	}
	@Override
	public void setDetectorMode(String value, double timeout) throws Exception {
		controller.setDetectorMode(value,timeout);
	}
	@Override
	public String getDetectorMode() throws Exception {
		return controller.getDetectorMode();
	}
	@Override
	public void setElement(String value) throws Exception {
		controller.setElement(value);
	}

	@Override
	public String getElement() throws Exception {
		return controller.getElement();
	}

	@Override
	public void setPassEnergy(Integer value) throws Exception {
		controller.setPassEnergy(value);
	}

	@Override
	public void setPassEnergy(Integer value, double timeout) throws Exception {
		controller.setPassEnergy(value, timeout);
	}
	@Override
	public Integer getPassEnergy() throws Exception {
		return controller.getPassEnergy();
	}

	@Override
	public void setStartEnergy(Double value) throws Exception {
		controller.setStartEnergy(value);
	}

	@Override
	public void setStartEnergy(Double value, double timeout) throws Exception {
		controller.setStartEnergy(value, timeout);
	}
	@Override
	public Double getStartEnergy() throws Exception {
		return controller.getStartEnergy();
	}

	@Override
	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}

	@Override
	public void setCentreEnergy(Double value, double timeout) throws Exception {
		controller.setCentreEnergy(value, timeout);
	}
	@Override
	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	@Override
	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}

	@Override
	public void setEndEnergy(Double value, double timeout) throws Exception {
		controller.setEndEnergy(value, timeout);
	}
	@Override
	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	@Override
	public void setEnergyStep(Double value) throws Exception {
		controller.setEnergyStep(value);
	}
	@Override
	public void setEnergyStep(Double value, double timeout) throws Exception {
		controller.setEnergyStep(value, timeout);
	}
	@Override
	public Double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}

	@Override
	public void setFrames(Integer value) throws Exception {
		controller.setFrames(value);
	}
	@Override
	public Integer getFrames() throws Exception {
		return controller.getFrames();
	}

	@Override
	public void setStepTime(double value) throws Exception {
		controller.setStepTime(value);
	}
	@Override
	public void setStepTime(double value, double timeout) throws Exception {
		controller.setStepTime(value, timeout);
	}
	@Override
	public double getStepTime() throws Exception {
		return controller.getStepTime();
	}
	@Override
	public void setSlices(int value) throws Exception {
		controller.setSlice(value);
	}
	@Override
	public void setSlices(int value, double timeout) throws Exception {
		controller.setSlice(value, timeout);
	}
	@Override
	public int getSlices() throws Exception {
		return controller.getSlice();
	}

	@Override
	public Integer getTotalSteps() throws Exception {
		return controller.getTotalSteps();
	}

	@Override
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}

	@Override
	public int getNdarrayXsize() throws Exception {
		return getNdArray().getPluginBase().getArraySize0_RBV();
	}

	@Override
	public int getNdarrayYsize() throws Exception {
		return getNdArray().getPluginBase().getArraySize1_RBV();
	}


	public double[] getExternalIOData() throws Exception {
		return controller.getExtIO();
	}

	public double[] getSpectrum() throws Exception {
		return controller.getSpectrum();
	}

	public NDProcess getNdProc() {
		return ndProc;
	}

	public void setNdProc(NDProcess ndProc) {
		this.ndProc = ndProc;
	}

	@Override
	public void start() throws Exception {
		getCollectionStrategy().collectData();
	}

	@Override
	public String[] getPassENergies() throws DeviceException {
		return controller.getPassEnergies();
	}

	@Override
	public String[] getLensModes() throws DeviceException {
		return controller.getLensModes();
	}

	@Override
	public double getExcitationEnergy() throws Exception {
		return controller.getExcitationEnergy();
	}
	
	@Override
	public void setExcitationEnergy(double energy) throws Exception {
		controller.setExcitationEnergy(energy);
	}

	public NeXusFileInterface getNexusFile() {
		return nexusFile;
	}

	public void setNexusFile(NeXusFileInterface nexusFile) {
		this.nexusFile = nexusFile;
	}


	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionname) {
		this.regionName = regionname;
	}

	public String getCachedEnergyMode() {
		return cachedEnergyMode;
	}

	public void setCachedEnergyMode(String energyMode) {
		this.cachedEnergyMode = energyMode;
	}


}
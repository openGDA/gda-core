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

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.vgscienta.VGScientaController;

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

	private NexusFile nexusFile;

	private String regionName;

	private String cachedEnergyMode;

	private Double totalIntensity=new Double(0.0);


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

			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = "pixel";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			data.addData(getName(), "lens_mode", new NexusGroupData(getLensMode()));

			data.addData(getName(), "pass_energy", new NexusGroupData(getPassEnergy()));

			data.addData(getName(), "acquisition_mode", new NexusGroupData(getAcquisitionMode()));

			data.addData(getName(), "energy_mode", new NexusGroupData(getEnergyMode()));

			data.addData(getName(), "detector_mode", new NexusGroupData(getDetectorMode()));

			data.addData(getName(), "sensor_size", new NexusGroupData(getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV()));

			data.addData(getName(), "region_origin", new NexusGroupData(getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV()));

			data.addData(getName(), "region_size", new NexusGroupData(getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV()));

			data.addData(getName(), "number_of_iterations", new NexusGroupData(getNumberIterations()));
		}
	}

	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {
		super.appendNXDetectorDataFromCollectionStrategy(data);
		// add additional data (image/array data are already added by the framework createNXDetectorData() by default)
		double[] spectrum=null;
		spectrum = getSpectrum();
		if (spectrum!=null) {
			data.addData(getName(), "spectrum", new NexusGroupData(spectrum), "counts");
		}
		double[] externalIO=null;
		externalIO = getExternalIOData();
		if (externalIO!=null) {
			data.addData(getName(), "externalIO", new NexusGroupData(externalIO));
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

	public INexusTree createRegionNodeWithFirstData(String name) throws Exception {
		INexusTree regionNode=new NexusTreeNode(name, NexusExtractor.NXDetectorClassName, null);

		String lensMode= getLensMode();
		NexusGroupData lens_mode=new NexusGroupData(lensMode);
		INexusTree lens_mode_ode=new NexusTreeNode("lens_mode", NexusExtractor.SDSClassName, null,lens_mode);
		regionNode.addChildNode(lens_mode_ode);

		NexusGroupData acquisition_mode=new NexusGroupData(getAcquisitionMode());
		INexusTree acquisition_mode_node=new NexusTreeNode("acquisition_mode", NexusExtractor.SDSClassName, null,acquisition_mode);
		regionNode.addChildNode(acquisition_mode_node);

		if (!getCachedEnergyMode().equalsIgnoreCase("Binding")) {
			NexusGroupData energy_mode=new NexusGroupData(getEnergyMode());
			INexusTree energy_mode_node=new NexusTreeNode("energy_mode", NexusExtractor.SDSClassName, null,energy_mode);
			regionNode.addChildNode(energy_mode_node);
		} else {
			NexusGroupData energy_mode=new NexusGroupData("Binding");
			INexusTree energy_mode_node=new NexusTreeNode("energy_mode", NexusExtractor.SDSClassName, null,energy_mode);
			regionNode.addChildNode(energy_mode_node);
		}

		NexusGroupData detector_mode=new NexusGroupData(getDetectorMode());
		INexusTree detector_mode_node=new NexusTreeNode("detector_mode", NexusExtractor.SDSClassName, null,detector_mode);
		regionNode.addChildNode(detector_mode_node);

		NexusGroupData pass_energy=new NexusGroupData(getPassEnergy());
		INexusTree pass_energy_node=new NexusTreeNode("pass_energy", NexusExtractor.SDSClassName, null,pass_energy);
		regionNode.addChildNode(pass_energy_node);
		pass_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, pass_energy_node, new NexusGroupData("eV")));

		double excitationEnergy = getExcitationEnergy();
		if (!getCachedEnergyMode().equalsIgnoreCase("Binding")) {
			NexusGroupData low_energy=new NexusGroupData(getStartEnergy());
			INexusTree low_energy_node=new NexusTreeNode("low_energy", NexusExtractor.SDSClassName, null,low_energy);
			regionNode.addChildNode(low_energy_node);
			low_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, low_energy_node, new NexusGroupData("eV")));

			NexusGroupData high_energy=new NexusGroupData(getEndEnergy());
			INexusTree high_energy_node=new NexusTreeNode("high_energy", NexusExtractor.SDSClassName, null,high_energy);
			regionNode.addChildNode(high_energy_node);
			high_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, high_energy_node, new NexusGroupData("eV")));

			NexusGroupData fixed_energy=new NexusGroupData(getCentreEnergy());
			INexusTree fixed_energy_node=new NexusTreeNode("fixed_energy", NexusExtractor.SDSClassName, null,fixed_energy);
			regionNode.addChildNode(fixed_energy_node);
			fixed_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, high_energy_node, new NexusGroupData("eV")));
		} else {
			//#TODO hack to fix EPICS cannot set binding mode issue.
			NexusGroupData low_energy=new NexusGroupData(excitationEnergy-getEndEnergy());
			INexusTree low_energy_node=new NexusTreeNode("low_energy", NexusExtractor.SDSClassName, null,low_energy);
			regionNode.addChildNode(low_energy_node);
			low_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, low_energy_node, new NexusGroupData("eV")));

			NexusGroupData high_energy=new NexusGroupData(excitationEnergy-getStartEnergy());
			INexusTree high_energy_node=new NexusTreeNode("high_energy", NexusExtractor.SDSClassName, null,high_energy);
			regionNode.addChildNode(high_energy_node);
			high_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, high_energy_node, new NexusGroupData("eV")));

			NexusGroupData fixed_energy=new NexusGroupData(excitationEnergy-getCentreEnergy());
			INexusTree fixed_energy_node=new NexusTreeNode("fixed_energy", NexusExtractor.SDSClassName, null,fixed_energy);
			regionNode.addChildNode(fixed_energy_node);
			fixed_energy_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, high_energy_node, new NexusGroupData("eV")));

		}

		NexusGroupData energy_step=new NexusGroupData(getEnergyStep());
		INexusTree energy_step_node=new NexusTreeNode("energy_step", NexusExtractor.SDSClassName, null,energy_step);
		regionNode.addChildNode(energy_step_node);
		energy_step_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, energy_step_node, new NexusGroupData("eV")));

		double stepTime = getStepTime();
		NexusGroupData step_time=new NexusGroupData(stepTime);
		INexusTree step_time_node=new NexusTreeNode("step_time", NexusExtractor.SDSClassName, null,step_time);
		regionNode.addChildNode(step_time_node);
		step_time_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, energy_step_node, new NexusGroupData("s")));

		NexusGroupData number_of_slices=new NexusGroupData(getSlices());
		INexusTree number_of_slices_node=new NexusTreeNode("number_of_slices", NexusExtractor.SDSClassName, null,number_of_slices);
		regionNode.addChildNode(number_of_slices_node);

		NexusGroupData number_of_iterations=new NexusGroupData(getNumberIterations());
		INexusTree number_of_iterations_node=new NexusTreeNode("number_of_iterations", NexusExtractor.SDSClassName, null,number_of_iterations);
		regionNode.addChildNode(number_of_iterations_node);

		int totalSteps = getTotalSteps().intValue();
		NexusGroupData total_steps=new NexusGroupData(totalSteps);
		INexusTree total_steps_node=new NexusTreeNode("total_steps", NexusExtractor.SDSClassName, null,total_steps);
		regionNode.addChildNode(total_steps_node);

		NexusGroupData total_time=new NexusGroupData(totalSteps*stepTime);
		INexusTree total_time_node=new NexusTreeNode("total_time", NexusExtractor.SDSClassName, null,total_time);
		regionNode.addChildNode(total_time_node);
		total_time_node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, energy_step_node, new NexusGroupData("s")));

		int cameraMinX = getCameraMinX();
		NexusGroupData detector_x_from=new NexusGroupData(cameraMinX);
		INexusTree detector_x_from_node=new NexusTreeNode("detector_x_from", NexusExtractor.SDSClassName, null,detector_x_from);
		regionNode.addChildNode(detector_x_from_node);

		NexusGroupData detector_x_to=new NexusGroupData(getCameraSizeX()-cameraMinX);
		INexusTree detector_x_to_node=new NexusTreeNode("detector_x_to", NexusExtractor.SDSClassName, null,detector_x_to);
		regionNode.addChildNode(detector_x_to_node);

		int cameraMinY = getCameraMinY();
		NexusGroupData detector_y_from=new NexusGroupData(cameraMinY);
		INexusTree detector_y_from_node=new NexusTreeNode("detector_y_from", NexusExtractor.SDSClassName, null,detector_y_from);
		regionNode.addChildNode(detector_y_from_node);

		NexusGroupData detector_y_to=new NexusGroupData(getCameraSizeY()-cameraMinY);
		INexusTree detector_y_to_node=new NexusTreeNode("detector_y_to", NexusExtractor.SDSClassName, null,detector_y_to);
		regionNode.addChildNode(detector_y_to_node);

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
			NexusGroupData energies=new NexusGroupData(axis);
			energies.isDetectorEntryData = true;
			INexusTree energies_node=new NexusTreeNode(aname, NexusExtractor.SDSClassName, null,energies);
			regionNode.addChildNode(energies_node);
			energies_node.addChildNode(new NexusTreeNode("axis",NexusExtractor.AttrClassName, energies_node, new NexusGroupData(i+1)));
			energies_node.addChildNode(new NexusTreeNode("primary",NexusExtractor.AttrClassName, energies_node, new NexusGroupData(1)));
			energies_node.addChildNode(new NexusTreeNode("unit",NexusExtractor.AttrClassName, energies_node, new NexusGroupData(aunit)));
		} catch (Exception e) {
			logger.error("failed to get energy axis data from analyser.", e);
			throw e;
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
			NexusGroupData vertiaclaxis=new NexusGroupData(axis);
			vertiaclaxis.isDetectorEntryData = true;
			INexusTree verticalaxis_node=new NexusTreeNode(aname, NexusExtractor.SDSClassName, null,vertiaclaxis);
			regionNode.addChildNode(verticalaxis_node);
			verticalaxis_node.addChildNode(new NexusTreeNode("axis",NexusExtractor.AttrClassName, verticalaxis_node, new NexusGroupData(i+1)));
			verticalaxis_node.addChildNode(new NexusTreeNode("primary",NexusExtractor.AttrClassName, verticalaxis_node, new NexusGroupData(1)));
			verticalaxis_node.addChildNode(new NexusTreeNode("unit",NexusExtractor.AttrClassName, verticalaxis_node, new NexusGroupData(aunit)));
		} catch (Exception e) {
			logger.error("failed to get angle or location axis data from analyser.", e);
			throw e;
		}
		createImageData(regionNode);
		createSpectrumData(regionNode);
		createExternalIOData(regionNode);
		createExciationEnergy(regionNode);

		return regionNode;
	}
	private void createImageData(INexusTree regionNode) {
		try {
			int[] dims=new int[2];
			dims[0] = getAngleAxis().length;
			dims[1] = getEnergyAxis().length;

			if (dims.length == 0) {
				logger.warn("Dimensions of image data from " + getName() + " are zero length");
				return;
			}

			NexusGroupData image_data = new NexusGroupData(dims, getImage(dims[0] * dims[1]));
			image_data.isDetectorEntryData=true;
			NexusTreeNode image_data_node=new NexusTreeNode("image_data", NexusExtractor.SDSClassName, null,image_data);
			image_data_node.setIsPointDependent(true);
			regionNode.addChildNode(image_data_node);
		} catch (Exception e) {
			logger.error("Failed to get NDArray data from EPICS plugin. ",e);
		}
	}
	@Override
	public void stop() throws DeviceException {
		try {
			getAdBase().stopAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Failed to stop acquiring", e);
		}
		super.stop();
	}
	private void createSpectrumData(INexusTree regionNode) {
		try {
			int size = getEnergyAxis().length;

			double[] s = getSpectrum(size);
			NexusGroupData spectrum_data=new NexusGroupData(s);
			spectrum_data.isDetectorEntryData=true;
			NexusTreeNode spectrum_data_node=new NexusTreeNode("spectrum_data", NexusExtractor.SDSClassName, null,spectrum_data);
			spectrum_data_node.setIsPointDependent(true);
			regionNode.addChildNode(spectrum_data_node);
			this.totalIntensity = (Double) DatasetFactory.createFromObject(s).sum();
		} catch (Exception e) {
			logger.error("Failed to get spectrum data from EPICS analyser. ",e);
		}
	}

	private void createExternalIOData(INexusTree regionNode) {
		try {
			int size;
			if (getAcquisitionMode().equalsIgnoreCase("Fixed")) {
				size =1;
			} else {
				size = getEnergyAxis().length;
			}
			NexusGroupData external_io_data=new NexusGroupData(getExternalIOData(size));
			external_io_data.isDetectorEntryData=true;
			NexusTreeNode external_io_data_node=new NexusTreeNode("external_io_data", NexusExtractor.SDSClassName, null,external_io_data);
			external_io_data_node.setIsPointDependent(true);
			regionNode.addChildNode(external_io_data_node);
		} catch (Exception e) {
			logger.error("Failed to get external IO data from EPICS analyser. ",e);
		}
	}
	private void createExciationEnergy(INexusTree regionNode) {
		try {
			NexusGroupData excitation_energy=new NexusGroupData(getExcitationEnergy());
			excitation_energy.isDetectorEntryData=true;
			NexusTreeNode excitation_energy_node=new NexusTreeNode("excitation_energy", NexusExtractor.SDSClassName, null,excitation_energy);
			excitation_energy_node.setIsPointDependent(true);
			regionNode.addChildNode(excitation_energy_node);
		} catch (Exception e) {
			logger.error("Failed to get excitation energy from EPICS analyser. ",e);
		}
	}

	public INexusTree createRegionNodeWithNewData(String name) {
		INexusTree regionNode=new NexusTreeNode(name, NexusExtractor.NXDetectorClassName, null);

		createImageData(regionNode);
		createSpectrumData(regionNode);
		createExternalIOData(regionNode);
		createExciationEnergy(regionNode);
		return regionNode;
	}

	public void writeOut(int scanDataPoint)  {
		try {
	//		InterfaceProvider.getTerminalPrinter().print("Writing region " + getRegionName() + " data to file : "+ datafilepath+". Please wait ......" );
			StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), "entry1", NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
			NexusUtils.addToAugmentPath(path, "detector", NexusExtractor.NXDetectorClassName);
			GroupNode g = nexusFile.getGroup(path.toString(), true);
			if (scanDataPoint == 1) {
				try {
					String lensMode= getLensMode();
					//write analyser parameters here
					NexusUtils.writeString(nexusFile, g, "reagion_name", getRegionName());
					NexusUtils.writeString(nexusFile, g, "lens_mode", lensMode);
					NexusUtils.writeString(nexusFile, g, "acquisition_mode", getAcquisitionMode());
					NexusUtils.writeString(nexusFile, g, "detector_mode", getDetectorMode());
					NexusUtils.writeInteger(nexusFile, g, "pass_energy", getPassEnergy());
					double excitationEnergy = getExcitationEnergy();
					if (getCachedEnergyMode().equalsIgnoreCase("Binding")) {
						NexusUtils.writeString(nexusFile, g, "energy_mode", "Binding");
						NexusUtils.writeDouble(nexusFile, g, "low_energy", excitationEnergy-getEndEnergy(), "eV");
						NexusUtils.writeDouble(nexusFile, g, "high_energy", excitationEnergy-getStartEnergy(), "eV");
						NexusUtils.writeDouble(nexusFile, g, "fixed_energy", excitationEnergy-getCentreEnergy(), "eV");

					} else {
						NexusUtils.writeString(nexusFile, g, "energy_mode", "Kinetic");
						NexusUtils.writeDouble(nexusFile, g, "low_energy", getStartEnergy(), "eV");
						NexusUtils.writeDouble(nexusFile, g, "high_energy", getEndEnergy(), "eV");
						NexusUtils.writeDouble(nexusFile, g, "fixed_energy", getCentreEnergy(), "eV");
					}
					NexusUtils.writeDouble(nexusFile, g, "energy_step", getEnergyStep(), "eV");
					double stepTime = getStepTime();
					NexusUtils.writeDouble(nexusFile, g, "step_time", stepTime, "s");
					NexusUtils.writeInteger(nexusFile, g, "number_of_slices", getSlices());
					NexusUtils.writeInteger(nexusFile, g, "number_of_iterations", getNumberIterations());
					int totalSteps = getTotalSteps().intValue();
					NexusUtils.writeInteger(nexusFile, g, "total_steps", totalSteps);
					NexusUtils.writeDouble(nexusFile, g, "total_time", totalSteps*stepTime, "s");
					int cameraMinX = getCameraMinX();
					NexusUtils.writeInteger(nexusFile, g, "detector_x_from", cameraMinX);
					int cameraMinY = getCameraMinY();
					NexusUtils.writeInteger(nexusFile, g, "detector_y_from", cameraMinY);
					NexusUtils.writeInteger(nexusFile, g, "detector_x_to", getCameraSizeX()-cameraMinX);
					NexusUtils.writeInteger(nexusFile, g, "detector_y_to", getCameraSizeY()-cameraMinY);
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

						DataNode d = NexusUtils.writeDoubleArray(nexusFile, g, aname, axis);
						NexusUtils.writeIntegerAttribute(nexusFile, d, "axis", i+1);
						NexusUtils.writeIntegerAttribute(nexusFile, d, "primary", 1);
						NexusUtils.writeStringAttribute(nexusFile, d, "unit", aunit);
					} catch (Exception e) {
						logger.error("failed to get energy axis data from analyser.", e);
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
						DataNode d = NexusUtils.writeDoubleArray(nexusFile, g, aname, axis);
						NexusUtils.writeIntegerAttribute(nexusFile, d, "axis", i+1);
						NexusUtils.writeIntegerAttribute(nexusFile, d, "primary", 1);
						NexusUtils.writeStringAttribute(nexusFile, d, "unit", aunit);
					} catch (Exception e) {
						logger.error("failed to get angle or location axis data from analyser.", e);

					}
				} catch (Exception e) {
					logger.error("failed to get analyser parameters on write data out",e);
				}
			}
			// write data that changes with scan data point here
			writeImageData(g, scanDataPoint);
			writeSpectrumData(g, scanDataPoint);
			writeExternalIOData(g, scanDataPoint);
			writeExcitationEnergy(g, scanDataPoint);
			//TODO Test this
			nexusFile.flush();
		} catch (Exception e) {
			logger.error("NexusException on write data out",e);
		}
	}

	private void writeImageData(GroupNode g, int scanDataPoint) {
		try {
			int[] dims=new int[] {1, getEnergyAxis().length, getAngleAxis().length};
			int[] datadims = new int[] {ILazyWriteableDataset.UNLIMITED , dims[1], dims[2] };
			DataNode d = nexusFile.getData(g, "image_data");
			ILazyWriteableDataset lazy;
			if (d == null || !Arrays.equals(d.getDataset().getShape(), dims)) {
				lazy = NexusUtils.createLazyWriteableDataset("image_data", Dataset.INT32, dims, datadims, dims);
				d = nexusFile.createData(g, lazy);
			} else {
				lazy = d.getWriteableDataset();
			}
			Dataset image = DatasetFactory.createFromObject(getImage(dims[1] * dims[2]));
			image.setShape(dims);

			lazy.setSlice(null, image, SliceND.createSlice(lazy, new int[] {scanDataPoint}, new int[] {scanDataPoint+1}));
		} catch (Exception e) {
			logger.error("Failed to get NDArray data from EPICS plugin. ",e);
		}
	}

	private void writeSpectrumData(GroupNode g, int scanDataPoint) {
		try {
			int size = getEnergyAxis().length;
			int[] dims=new int[] {1, size};
			int[] datadims = new int[] {ILazyWriteableDataset.UNLIMITED , dims[1] };

			DataNode d = nexusFile.getData(g, "spectrum_data");
			ILazyWriteableDataset lazy;
			if (d == null || !Arrays.equals(d.getDataset().getShape(), dims)) {
				lazy = NexusUtils.createLazyWriteableDataset("spectrum_data", Dataset.FLOAT64, dims, datadims, dims);
				d = nexusFile.createData(g, lazy);
			} else {
				lazy = d.getWriteableDataset();
			}

			Dataset spectrum = DatasetFactory.createFromObject(getSpectrum(size));
			spectrum.setShape(dims);

			lazy.setSlice(null, spectrum, SliceND.createSlice(lazy, new int[] {scanDataPoint}, new int[] {scanDataPoint+1}));

			this.totalIntensity=(Double) spectrum.sum();
		} catch (Exception e) {
			logger.error("Failed to get spectrum data from EPICS analyser. ",e);
		}
	}

	private void writeExternalIOData(GroupNode g, int scanDataPoint) {
		try {
			int size;
			if (getAcquisitionMode().equalsIgnoreCase("Fixed")) {
				size =1;
			} else {
				size = getEnergyAxis().length;
			}
			int[] dims=new int[] {1, size};
			int[] datadims = new int[] {ILazyWriteableDataset.UNLIMITED , dims[1] };

			DataNode d = nexusFile.getData(g, "external_io_data");
			ILazyWriteableDataset lazy;
			if (d == null || !Arrays.equals(d.getDataset().getShape(), dims)) {
				lazy = NexusUtils.createLazyWriteableDataset("external_io_data", Dataset.FLOAT64, dims, datadims, dims);
				d = nexusFile.createData(g, lazy);
			} else {
				lazy = d.getWriteableDataset();
			}

			Dataset io = DatasetFactory.createFromObject(getExternalIOData(size));
			io.setShape(dims);

			lazy.setSlice(null, io, SliceND.createSlice(lazy, new int[] {scanDataPoint}, new int[] {scanDataPoint+1}));
		} catch (Exception e) {
			logger.error("Failed to get external IO data from EPICS analyser. ",e);
		}
	}
	private void writeExcitationEnergy(GroupNode g, int scanDataPoint) {
		try {
			int size = 1;
			int[] dims=new int[] {1, size};
			int[] datadims = new int[] {ILazyWriteableDataset.UNLIMITED , size };

			DataNode d = nexusFile.getData(g, "excitation_energy");
			ILazyWriteableDataset lazy;
			if (d == null || !Arrays.equals(d.getDataset().getShape(), dims)) {
				lazy = NexusUtils.createLazyWriteableDataset("excitation_energy", Dataset.FLOAT64, dims, datadims, dims);
				d = nexusFile.createData(g, lazy);
			} else {
				lazy = d.getWriteableDataset();
			}

			Dataset exEnergy = DatasetFactory.createFromObject(getExcitationEnergy());
			exEnergy.setShape(dims);

			lazy.setSlice(null, exEnergy, SliceND.createSlice(lazy, new int[] {scanDataPoint}, new int[] {scanDataPoint+1}));
		} catch (Exception e) {
			logger.error("Failed to get excitation energy from EPICS analyser. ",e);
		}
	}


	public double[] getExternalIOData(int i) throws TimeoutException, CAException, InterruptedException, Exception {
		return controller.getExtIO(i);
	}

	@Override
	public double[] getSpectrum(int i) throws Exception {
		return controller.getSpectrum(i);
	}

	@Override
	public double[] getImage(int i) throws Exception {
		return controller.getImage(i);
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
		controller.setSlices(region[3]);
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
		getAdBase().setNumExposures(value);
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
		controller.setLensMode(value);
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
		controller.setAcquisitionMode(value);
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
		controller.setEnergyMode(value);
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
		controller.setDetectorMode(value);
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
		controller.setPassEnergy(value);
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
		controller.setStartEnergy(value);
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
		controller.setCentreEnergy(value);
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
		controller.setEndEnergy(value);
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
		controller.setEnergyStep(value);
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
		controller.setExposureTime(value);
	}
	@Override
	public void setStepTime(double value, double timeout) throws Exception {
		controller.setExposureTime(value);
	}
	@Override
	public double getStepTime() throws Exception {
		return controller.getExposureTime();
	}
	@Override
	public void setSlices(int value) throws Exception {
		controller.setSlices(value);
	}
	@Override
	public void setSlices(int value, double timeout) throws Exception {
		controller.setSlices(value);
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

	public double[] getImage() throws Exception {
		return controller.getImage();
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
		return controller.getPassEnergies().toArray(new String[0]);
	}

	@Override
	public String[] getLensModes() throws DeviceException {
		return controller.getLensModes().toArray(new String[0]);
	}
	@Override
	public String[] getElementSet() throws DeviceException {
		return controller.getElementset();
	}
	@Override
	public double getExcitationEnergy() throws Exception {
		return controller.getExcitationEnergy();
	}

	@Override
	public void setExcitationEnergy(double energy) throws Exception {
		controller.setExcitationEnergy(energy);
	}

	public NexusFile getNexusFile() {
		return nexusFile;
	}

	public void setNexusFile(NexusFile nexusFile) {
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

	public Double getTotalIntensity() {
		return totalIntensity;
	}

	@Override
	public double[] getExtIO(int length) throws Exception {
		return controller.getExtIO(length);
	}
}

package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;

public class Xspress3Detector extends DetectorBase implements NexusDetector {

	
	private final Xspress3Controller controller;

	public Xspress3Detector(Xspress3Controller controller){
		this.controller = controller;
	}
	
	@Override
	public void configure() throws FactoryException {
		controller.configure();
		super.configure();
	}
	
	
	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatus() throws DeviceException {
		return controller.getStatus();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;  // false as this will return data for the GDA to write itself.
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}


}

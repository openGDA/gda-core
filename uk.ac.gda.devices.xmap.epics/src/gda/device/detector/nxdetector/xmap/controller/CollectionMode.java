package gda.device.detector.nxdetector.xmap.controller;

import gda.device.detector.nxdetector.xmap.PVBase;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer.CollectionModeEnum;


public class CollectionMode{

	private CollectionModeEnum collectMode;
	
	public CollectionMode() {
		this.collectMode = CollectionModeEnum.MCA_SPECTRA;
	}

	public CollectionModeEnum getCollectMode(){
		return collectMode;
	}
	
}

package gda.device.detector.nxdetector.xmap.Controller;

import gda.device.detector.nxdetector.xmap.PVBase;
import gda.device.detector.nxdetector.xmap.Controller.XmapAcquisitionBaseEpicsLayer.CollectionModeEnum;


public class CollectionMode{

	private CollectionModeEnum collectMode;
	
	public CollectionMode() {
		this.collectMode = CollectionModeEnum.MCA_SPECTRA;
	}

	public CollectionModeEnum getCollectMode(){
		return collectMode;
	}
	
}

package gda.device.detector.nxdetector.xmap.xbufferPlugin;

import java.io.IOException;

import gda.device.detector.nxdetector.xmap.PVBase;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;

/*
 * This class is a communication layer between EPICs and GDA for the Xbuf intermediate plugin used in Xmap detector IOC.
 * Xbuf EPICs plugin only contains 2 PVs: EnableCallbacks and EnableCallbacks_RBV, when EnableCallBacks is set 
 * to Enable then the MCA spectra are "correctly" formatted in HDF5 file.
*/

public class XBufferPVs {		
	
	public enum EnableCallbacksEnum{
		EnableCallbacks, EnableCallbacks_RBV
	}
	
	private PV<Boolean> EnableCallbacksPV;
	private String basePVName;
	
	
	public XBufferPVs(String basePVname){
		this.basePVName = basePVname;
		if (basePVname == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createLazyPVs();
	}
	
	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;
		
	}
	
	public void createLazyPVs(){
		EnableCallbacksPV = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(EnableCallbacksEnum.EnableCallbacks.name())),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullPVname(EnableCallbacksEnum.EnableCallbacks.name())));
	}
	
	public void setEnableCallbacks(boolean enable) throws IOException{
		EnableCallbacksPV.putWait(enable);
	}
	
	public boolean getEnableCallbacks() throws IOException{
		return EnableCallbacksPV.get();
	}
}

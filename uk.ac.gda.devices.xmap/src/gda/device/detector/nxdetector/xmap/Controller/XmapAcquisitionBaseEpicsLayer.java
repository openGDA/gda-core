/**
 * 
 */
package gda.device.detector.nxdetector.xmap.Controller;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;

/**
 * @author dfq16044
 * @param <T>
 *
 */
public class XmapAcquisitionBaseEpicsLayer {
	
	public enum CollectionModeEnum{
		/* MCA spectra used in step scan: acquire only one spectrum */
		MCA_SPECTRA,
		/* MCA mapping used in raster/continuous scan: acquire multiple spectra */
		MCA_MAPPING,
		SCA_MAPPING,
		LIST_MAPPING
	}
	
	// This class defines the acquisition mode: step scan uses MCA SPECTRA while raster scan uses MCA_MAPPING
	private CollectionMode collectionMode;
	
	public enum PresetMode{
		/* Option used for hardware trigger*/
		NO_PRESET,
		REAL_TIME,
		LIVE_TIME,
		EVENTS,
		TRIGGERS
	}
	
	 // PV names corresponding to the EPICs Acquisition Control panel
	 
	private enum AcqControlPVname{
		StartAll, EraseStart, EraseAll, StopAll, Acquiring;
	}
	
	 // PV names corresponding to the EPICs Acquisition Configuration panel
	 
	private enum AcqConfigPVname {
		CollectMode("CollectMode"), CollectMode_RBV("CollectMode_RBV"), NBINS(
				"MCA1.NUSE"), NBINS_RBV("MCA1:NBINS"), PresetMode("PresetMode"), PresetReal(
				"PresetReal"), PresetLive("PresetLive"), PresetValue(
				"PresetValue"), PresetEvents("PresetEvents"), PresetTriggers(
				"PresetTriggers");

		private final String PVName;

		AcqConfigPVname(String pvname) {
			this.PVName = pvname;
		}

		public String toString() {
			return PVName;
		}
	}

	//Map <AcqControlPVname,PV<Boolean>> acqControlPV = new EnumMap<AcqControlPVname,PV<Boolean>>(AcqControlPVname.class); 

	private PV<Boolean> startAllPV;
	private PV<Boolean> eraseStartPV;
	private PV<Boolean> eraseAllPV;
	private PV<Boolean> stopAllPV;
	private ReadOnlyPV<Boolean> acquiringPV;
	private String basePVName;
	private PV<CollectionModeEnum> collectModePVPair;
	private PV<Integer> nbinsPVPair;
	private PV<PresetMode> presetModePV;
	private PV<Double> presetRealPV;
	private PV<Double> presetLivePV;
	private PV<Integer> presetEventsPV;
	private PV<Integer> presetTriggersPV;
	private PV<Double> presetValuePV;
		
	
	public XmapAcquisitionBaseEpicsLayer(String basePVname,CollectionMode collectMode) throws IOException{
		this.basePVName= basePVname;
		this.collectionMode = collectMode;
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createAcquisitionControlLazyPVs();
		createAcquisitionConfigurationLazyPVs();
	}

	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;
		
	}
	
	private void createAcquisitionControlLazyPVs() throws IOException{		
		/* Create PVs corresponding to the EPICs Acquisition Control panel, all buttons in the panel do not have any returned value
		(StartAll, StopAll, EraseAll, Erase), only the Acquiring field will return the status of the acquisition.
		*/	
		/*for (AcqControlPVname pvname: AcqControlPVname.values()){
			acqControlPV.put(pvname, LazyPVFactory.newBooleanFromEnumPV(fullPVname(pvname.name())));
		}*/
		startAllPV =  LazyPVFactory.newBooleanFromEnumPV(fullPVname(AcqControlPVname.StartAll.name()));
		eraseStartPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(AcqControlPVname.EraseStart.name()));
		eraseAllPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(AcqControlPVname.EraseAll.name()));
		stopAllPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(AcqControlPVname.StopAll.name()));
		acquiringPV = LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullPVname(AcqControlPVname.Acquiring.name()));
		
	}
	
	private void createAcquisitionConfigurationLazyPVs() throws IOException{			
		collectModePVPair = new PVWithSeparateReadback<CollectionModeEnum>(
				LazyPVFactory.newEnumPV(fullPVname(AcqConfigPVname.CollectMode.name()), CollectionModeEnum.class),
				LazyPVFactory.newReadOnlyEnumPV(fullPVname(AcqConfigPVname.CollectMode.name()), CollectionModeEnum.class));
		nbinsPVPair = new PVWithSeparateReadback<Integer>(
				LazyPVFactory.newIntegerPV(fullPVname(AcqConfigPVname.NBINS.toString())),
				LazyPVFactory.newReadOnlyIntegerPV(fullPVname(AcqConfigPVname.NBINS_RBV.toString())));
		presetModePV = LazyPVFactory.newEnumPV(fullPVname(AcqConfigPVname.PresetMode.name()), PresetMode.class);
		presetRealPV = LazyPVFactory.newDoublePV(fullPVname(AcqConfigPVname.PresetReal.name()));
		presetLivePV = LazyPVFactory.newDoublePV(fullPVname(AcqConfigPVname.PresetLive.name()));
		presetEventsPV= LazyPVFactory.newIntegerPV(fullPVname(AcqConfigPVname.PresetEvents.name()));
		presetTriggersPV= LazyPVFactory.newIntegerPV(fullPVname(AcqConfigPVname.PresetTriggers.name()));		
		presetValuePV= LazyPVFactory.newDoublePV(fullPVname(AcqConfigPVname.PresetValue.name()));	
		
		
	}

	
	public void setStart() throws Exception {
		startAllPV.putNoWait(true);
	}
	
	
	public void setStop() throws Exception {
		stopAllPV.putNoWait(true);
	}
	
	public void setErase() throws Exception {
		eraseAllPV.putNoWait(true);
	}
	
	public void setEraseStart() throws Exception {
		eraseStartPV.putNoWait(true);
	}
	
	
	public boolean getAcquiring() throws Exception {
		return acquiringPV.get();
	}
	// Add this method as it is called in CollectionStrategyPlugin
	public int getStatus() throws Exception{
		if (getAcquiring()) return 1;
		else return 0;
	}
	
	public void setCollectMode(CollectionModeEnum collectMode) throws Exception {
		if (collectMode.equals(CollectionModeEnum.MCA_MAPPING))
			isXmapMappingModeInstance("MCA_MAPPING mode");
		collectModePVPair.putWait(collectMode);
	}
	
	public CollectionModeEnum getCollectMode() throws Exception {
		return collectModePVPair.get();
	}
	
	public void setNbins(int nbins) throws Exception {
		nbinsPVPair.putWait(nbins);
	}
	
	public int getNbins() throws Exception {
		return nbinsPVPair.get();
	}
	
	public void setPresetMode(PresetMode presetMode) throws Exception {
		if (presetMode.equals(PresetMode.NO_PRESET))
				isXmapMappingModeInstance("NO_PRESET type");
		presetModePV.putWait(presetMode);
	}
	
	public PresetMode getPresetMode() throws Exception {
		return presetModePV.get();
	}
	
	public void setPresetRealTime(double realTime) throws Exception {
		presetRealPV.putWait(realTime);
	}
	
	public double getPresetRealTime() throws Exception {
		return presetRealPV.get();
	}
	
	public void setPresetLiveTime(double liveTime) throws Exception {
		presetLivePV.putWait(liveTime);
	}
	
	public double getPresetLiveTime() throws Exception {
		return presetLivePV.get();
	}
	
	public void setPresetEvents(int event) throws Exception {
		presetEventsPV.putWait(event);
	}
	
	public int getPresetEvents() throws Exception {
		return presetEventsPV.get();
	}
	
	public void setPresetTriggers(int triggers) throws Exception {
		presetTriggersPV.putWait(triggers);
	}
	
	public int getPresetTriggers() throws Exception {
		return presetTriggersPV.get();
	}
	
	public void setAquisitionTime(double presetValue) throws Exception {
		presetValuePV.putWait(presetValue);
	}
	
	public double getAquisitionTime() throws Exception {
		return presetValuePV.get();
	}
	
	public CollectionMode getCollectionMode(){
		return collectionMode;
	}
	
	public boolean isXmapMappingModeInstance(String message){
		if (!(collectionMode instanceof XmapMappingModeEpicsLayer)) 
			throw new ClassCastException("For "+ message + " CollectionMode object should be of type "
					+ "XmapMappingModeEpicsLayer.");
		else return true;
	}	
}

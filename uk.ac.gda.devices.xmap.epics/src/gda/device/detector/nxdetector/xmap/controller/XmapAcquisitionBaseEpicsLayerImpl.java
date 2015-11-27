package gda.device.detector.nxdetector.xmap.controller;

import gda.device.detector.nxdetector.xmap.controller.XmapModes.CollectionModeEnum;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PresetMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;

/**
 * Communication layer between
 *
 */
public class XmapAcquisitionBaseEpicsLayerImpl implements XmapAcquisitionBaseEpicsLayer {

	// This class defines the acquisition mode: step scan uses MCA SPECTRA while raster scan uses MCA_MAPPING
	private CollectionMode collectionMode;

	 // PV names corresponding to the EPICs Acquisition Control panel

	private enum AcqControlPVname{
		StartAll, EraseStart, EraseAll, StopAll, Acquiring;
	}

	 // PV names corresponding to the EPICs Acquisition Configuration panel

	private enum AcqConfigPVname {
		CollectMode("CollectMode"), CollectMode_RBV("CollectMode_RBV"), NBINS(
				"MCA1.NUSE"), NBINS_RBV("MCA1:NBINS"), PresetMode("PresetMode"), PresetReal(
				"PresetReal"), PresetLive("PresetLive"), PresetValue("PresetValue"), PresetEvents("PresetEvents"), PresetTriggers("PresetTriggers"), MCAVal(
				"MCA1.VAL");

		private final String PVName;

		AcqConfigPVname(String pvname) {
			this.PVName = pvname;
		}

		@Override
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
	private ReadOnlyPV<Integer[]> latestMCA; // [channel]

	public XmapAcquisitionBaseEpicsLayerImpl(String basePVname,CollectionMode collectMode) throws IOException{
		this.basePVName= basePVname;
		this.collectionMode = collectMode;
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		if (collectMode == null) {
			throw new NullPointerException("Collection Mode needs to be declared");
		}
		createAcquisitionControlLazyPVs();
		createAcquisitionConfigurationLazyPVs();
	}

	@Override
	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;

	}

	@Override
	public String getBasePVName(){
		return basePVName;
	}


	private void createAcquisitionControlLazyPVs() throws IOException {
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
		latestMCA = LazyPVFactory.newReadOnlyIntegerArrayPV(fullPVname(AcqConfigPVname.MCAVal.toString()));

	}

	private void createAcquisitionConfigurationLazyPVs() throws IOException {
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


	@Override
	public void setStart() throws Exception {
		startAllPV.putNoWait(true);
	}


	@Override
	public void setStop() throws Exception {
		stopAllPV.putNoWait(true);
	}

	@Override
	public void setErase() throws Exception {
		eraseAllPV.putNoWait(true);
	}

	@Override
	public void setEraseStart() throws Exception {
		eraseStartPV.putNoWait(true);
	}

	@Override
	public boolean getAcquiring() throws Exception {
		return acquiringPV.get();
	}
	// Add this method as it is called in CollectionStrategyPlugin
	@Override
	public int getStatus() throws Exception{
		if (getAcquiring()) return 1;
		else return 0;
	}

	@Override
	public void setCollectMode(CollectionModeEnum collectMode) throws Exception {
		if (collectMode.equals(CollectionModeEnum.MCA_MAPPING))
			isXmapMappingModeInstance("MCA_MAPPING mode");
		collectModePVPair.putWait(collectMode);
	}

	@Override
	public CollectionModeEnum getCollectMode() throws Exception {
		return collectModePVPair.get();
	}

	@Override
	public void setNbins(int nbins) throws Exception {
		nbinsPVPair.putWait(nbins);
	}

	@Override
	public int getNbins() throws Exception {
		return nbinsPVPair.get();
	}

	@Override
	public void setPresetMode(PresetMode presetMode) throws Exception {
		if (presetMode.equals(PresetMode.NO_PRESET))
				isXmapMappingModeInstance("NO_PRESET type");
		presetModePV.putWait(presetMode);
	}

	@Override
	public PresetMode getPresetMode() throws Exception {
		return presetModePV.get();
	}

	@Override
	public void setPresetRealTime(double realTime) throws Exception {
		presetRealPV.putWait(realTime);
	}

	@Override
	public double getPresetRealTime() throws Exception {
		return presetRealPV.get();
	}

	@Override
	public void setPresetLiveTime(double liveTime) throws Exception {
		presetLivePV.putWait(liveTime);
	}

	@Override
	public double getPresetLiveTime() throws Exception {
		return presetLivePV.get();
	}

	@Override
	public void setPresetEvents(int event) throws Exception {
		presetEventsPV.putWait(event);
	}

	@Override
	public int getPresetEvents() throws Exception {
		return presetEventsPV.get();
	}

	@Override
	public void setPresetTriggers(int triggers) throws Exception {
		presetTriggersPV.putWait(triggers);
	}

	@Override
	public int getPresetTriggers() throws Exception {
		return presetTriggersPV.get();
	}

	@Override
	public void setAquisitionTime(double presetValue) throws Exception {
		presetValuePV.putWait(presetValue);
	}

	@Override
	public double getAquisitionTime() throws Exception {
		return presetValuePV.get();
	}

	@Override
	public CollectionMode getCollectionMode(){
		return collectionMode;
	}

	@Override
	public boolean isXmapMappingModeInstance(String message){
		if (!(collectionMode instanceof XmapMappingModeEpicsLayer))
			throw new ClassCastException("For "+ message + " CollectionMode object should be of type "
					+ "XmapMappingModeEpicsLayer.");
		else return true;
	}

	@Override
	public double[] getDataPerElement(int i) throws IOException {
		double[] MCAspectrum = new double[latestMCA.get().length];
		for (i = 0; i < latestMCA.get().length; i++) {
			MCAspectrum[i] = (double) (ArrayUtils.toPrimitive(latestMCA.get())[i]);
		}
		return MCAspectrum;
	}
}

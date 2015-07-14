/**
 *
 */
package gda.device.detector.nxdetector.xmap.controller;

import java.io.IOException;

import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer.CollectionModeEnum;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;

/**
 * @author dfq16044
 *
 */
public class XmapMappingModeEpicsLayer extends CollectionMode{

	public enum ListMode{
		EAndGate, EAndSync, EAndClock
	}

	public enum PixelAdvanceMode{
		Gate, Sync
	}

	/* Here the CollectMode will be not used as the PV is already created in the XmapAcquisitionBase class*/

	private enum MappingSettingsPVname{
		ListMode_RBV, PixelAdvanceMode, PixelAdvanceMode_RBV, SyncCount, SyncCount_RBV,
		IgnoreGate, IgnoreGate_RBV, InputLogicPolarity, PixelsPerRun, PixelsPerRun_RBV
	}

	private enum PixelsPerBufferPVname{
		AutoPixelsPerBuffer, AutoPixelsPerBuffer_RBV, PixelsPerBuffer, PixelsPerBuffer_RBV,
		BufferSize
	}

	private enum RunTimePV{
		NextPixel("NextPixel"), CurrentPixel("DXP1:CurrentPixel");
		private final String PVName;

		 RunTimePV(String pvname){
			 this.PVName = pvname;
		 }
		 @Override
		public String toString(){return PVName;}
	}

	private ReadOnlyPV<ListMode> ListMode_RBVPV;
	private PV<PixelAdvanceMode> PixelAdvanceModePV;
	private PV<Integer> SyncCountPV;
	private PV<Boolean> IgnoreGatePV;
	private PV<Boolean> InputLogicPolarityPV;
	private PV<Integer> PixelsperRunPV;
	private PV<Boolean> AutoPixelsPerBufferPV;
	private PV<Integer> PixelsPerBufferPV;
	private PV<Boolean> NextPixelPV;
	private ReadOnlyPV<Integer> CurrentPixelPV;

	private String basePVName;
	CollectionModeEnum collectMode;

	public XmapMappingModeEpicsLayer(String basePVname) {
		this.collectMode = CollectionModeEnum.MCA_MAPPING;
		this.basePVName = basePVname;
		if (basePVname == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createMappingSettingsLazyPVs();
		createPixelsPerBufferLazyPVs();
		createRuntimeLazyPVs();
	}

	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;
	}

	private void createMappingSettingsLazyPVs(){
		ListMode_RBVPV = LazyPVFactory.newReadOnlyEnumPV(fullPVname(MappingSettingsPVname.ListMode_RBV.name()), ListMode.class);
		PixelAdvanceModePV = new PVWithSeparateReadback<PixelAdvanceMode>(
				LazyPVFactory.newEnumPV(fullPVname(MappingSettingsPVname.PixelAdvanceMode.name()), PixelAdvanceMode.class),
				LazyPVFactory.newReadOnlyEnumPV(fullPVname(MappingSettingsPVname.PixelAdvanceMode_RBV.name()), PixelAdvanceMode.class));
		SyncCountPV = new PVWithSeparateReadback<Integer>(
					LazyPVFactory.newIntegerPV(fullPVname(MappingSettingsPVname.SyncCount.name())),
					LazyPVFactory.newReadOnlyIntegerPV(fullPVname(MappingSettingsPVname.SyncCount_RBV.name())));
		IgnoreGatePV = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(MappingSettingsPVname.IgnoreGate.name())),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullPVname(MappingSettingsPVname.IgnoreGate_RBV.name())));
		InputLogicPolarityPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(MappingSettingsPVname.InputLogicPolarity.name()));
		PixelsperRunPV = new PVWithSeparateReadback<Integer>(
				LazyPVFactory.newIntegerPV(fullPVname(MappingSettingsPVname.PixelsPerRun.name())),
				LazyPVFactory.newReadOnlyIntegerPV(fullPVname(MappingSettingsPVname.PixelsPerRun_RBV.name())));
	}


	private void createPixelsPerBufferLazyPVs(){
		AutoPixelsPerBufferPV = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(PixelsPerBufferPVname.AutoPixelsPerBuffer.name())),
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(PixelsPerBufferPVname.AutoPixelsPerBuffer_RBV.name())));
		PixelsPerBufferPV = new PVWithSeparateReadback<Integer>(
				LazyPVFactory.newIntegerPV(fullPVname(PixelsPerBufferPVname.PixelsPerBuffer.name())),
				LazyPVFactory.newReadOnlyIntegerPV(fullPVname(PixelsPerBufferPVname.PixelsPerBuffer_RBV.name())));
	}

	private void createRuntimeLazyPVs(){
		NextPixelPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(RunTimePV.NextPixel.name()));
		CurrentPixelPV = LazyPVFactory.newReadOnlyIntegerPV(fullPVname(RunTimePV.CurrentPixel.toString()));
	}

	public ListMode getListMode_RBV() throws IOException{
		return ListMode_RBVPV.get();
	}

	public void setPixelAdvanceMode(PixelAdvanceMode pixelAdvanceMode) throws IOException{
		PixelAdvanceModePV.putWait(pixelAdvanceMode);
	}

	public PixelAdvanceMode getPixelAdvanceMode() throws IOException{
		return PixelAdvanceModePV.get();
	}



	public void setSyncCount(int syncCount) throws IOException{
		SyncCountPV.putWait(syncCount);
	}

	public int getSyncCount() throws IOException{
		return SyncCountPV.get();
	}

	public void setIgnoreGate(boolean ignoreGate) throws IOException{
		IgnoreGatePV.putWait(ignoreGate);
	}

	public boolean getIgnoreGate() throws IOException{
		return IgnoreGatePV.get();
	}

	public void setInputLogicPolarity(boolean inputLogicParity) throws IOException{
		InputLogicPolarityPV.putWait(inputLogicParity);
	}

	public boolean getInputLogicPolarity() throws IOException{
		return InputLogicPolarityPV.get();
	}

	public void setPixelsPerRun(int pixelsPerRun) throws IOException{
		PixelsperRunPV.putWait(pixelsPerRun);
	}

	public int getPixelsPerRun() throws IOException{
		return PixelsperRunPV.get();
	}

	public void setAutoPixelsPerBuffer(boolean autoPixelsPerBuffer) throws IOException{
		AutoPixelsPerBufferPV.putWait(autoPixelsPerBuffer);
	}

	public boolean getAutoPixelsPerBuffer() throws IOException{
		return AutoPixelsPerBufferPV.get();
	}

	public void setPixelsPerBuffer(int pixelsPerBuffer) throws IOException{
		PixelsPerBufferPV.putWait(pixelsPerBuffer);
	}

	public int getPixelsPerBuffer() throws IOException{
		return PixelsPerBufferPV.get();
	}

	public void setNextPixel() throws IOException{
		NextPixelPV.putWait(true);
	}

	public int getCurrentPixel() throws IOException{
		return CurrentPixelPV.get();
	}
}

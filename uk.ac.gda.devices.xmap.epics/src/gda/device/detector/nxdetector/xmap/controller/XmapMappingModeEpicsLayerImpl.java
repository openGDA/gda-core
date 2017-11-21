/**
 *
 */
package gda.device.detector.nxdetector.xmap.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nxdetector.xmap.controller.XmapModes.ListMode;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PixelAdvanceMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;

/**
 * @author dfq16044
 *
 */
public class XmapMappingModeEpicsLayerImpl extends CollectionMode implements XmapMappingModeEpicsLayer, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(XmapMappingModeEpicsLayerImpl.class);

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
		public String toString() {
			return PVName;
		}
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

	/**
	 * Initial setting of pixels per buffer<br>
	 * A value less than 1 means no initial setting
	 */
	private int initialPixelsPerBuffer;

	public XmapMappingModeEpicsLayerImpl(String basePVname, int initialPixelsPerBuffer) {
		this.basePVName = basePVname;
		this.initialPixelsPerBuffer = initialPixelsPerBuffer;
	}

	@Override
	public void configure() throws FactoryException {
		logger.debug("Configuring XMAP mapping");
		if (basePVName == null) {
			throw new FactoryException("'basePVName' needs to be declared");
		}
		try {
			createMappingSettingsLazyPVs();
			createPixelsPerBufferLazyPVs();
			if (initialPixelsPerBuffer > 0) {
				setPixelsPerBuffer(initialPixelsPerBuffer);
			}
			createRuntimeLazyPVs();
		} catch (Exception e) {
			final String message = "Exception configuring XMAP mapping";
			logger.error(message, e);
			throw new FactoryException(message, e);
		}
	}

	@Override
	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;
	}

	private void createMappingSettingsLazyPVs() {
		ListMode_RBVPV = LazyPVFactory.newReadOnlyEnumPV(fullPVname(MappingSettingsPVname.ListMode_RBV.name()), ListMode.class);
		PixelAdvanceModePV = new PVWithSeparateReadback<>(
				LazyPVFactory.newEnumPV(fullPVname(MappingSettingsPVname.PixelAdvanceMode.name()), PixelAdvanceMode.class),
				LazyPVFactory.newReadOnlyEnumPV(fullPVname(MappingSettingsPVname.PixelAdvanceMode_RBV.name()), PixelAdvanceMode.class));
		SyncCountPV = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(fullPVname(MappingSettingsPVname.SyncCount.name())),
					LazyPVFactory.newReadOnlyIntegerPV(fullPVname(MappingSettingsPVname.SyncCount_RBV.name())));
		IgnoreGatePV = new PVWithSeparateReadback<>(
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(MappingSettingsPVname.IgnoreGate.name())),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullPVname(MappingSettingsPVname.IgnoreGate_RBV.name())));
		InputLogicPolarityPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(MappingSettingsPVname.InputLogicPolarity.name()));
		PixelsperRunPV = new PVWithSeparateReadback<>(
				LazyPVFactory.newIntegerPV(fullPVname(MappingSettingsPVname.PixelsPerRun.name())),
				LazyPVFactory.newReadOnlyIntegerPV(fullPVname(MappingSettingsPVname.PixelsPerRun_RBV.name())));
	}


	private void createPixelsPerBufferLazyPVs() {
		AutoPixelsPerBufferPV = new PVWithSeparateReadback<>(
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(PixelsPerBufferPVname.AutoPixelsPerBuffer.name())),
				LazyPVFactory.newBooleanFromEnumPV(fullPVname(PixelsPerBufferPVname.AutoPixelsPerBuffer_RBV.name())));
		PixelsPerBufferPV = new PVWithSeparateReadback<>(
				LazyPVFactory.newIntegerPV(fullPVname(PixelsPerBufferPVname.PixelsPerBuffer.name())),
				LazyPVFactory.newReadOnlyIntegerPV(fullPVname(PixelsPerBufferPVname.PixelsPerBuffer_RBV.name())));
	}

	private void createRuntimeLazyPVs() {
		NextPixelPV = LazyPVFactory.newBooleanFromEnumPV(fullPVname(RunTimePV.NextPixel.name()));
		CurrentPixelPV = LazyPVFactory.newReadOnlyIntegerPV(fullPVname(RunTimePV.CurrentPixel.toString()));
	}

	@Override
	public ListMode getListMode_RBV() throws IOException{
		return ListMode_RBVPV.get();
	}

	@Override
	public void setPixelAdvanceMode(PixelAdvanceMode pixelAdvanceMode) throws IOException{
		PixelAdvanceModePV.putWait(pixelAdvanceMode);
	}

	@Override
	public PixelAdvanceMode getPixelAdvanceMode() throws IOException{
		return PixelAdvanceModePV.get();
	}

	@Override
	public void setSyncCount(int syncCount) throws IOException{
		SyncCountPV.putWait(syncCount);
	}

	@Override
	public int getSyncCount() throws IOException{
		return SyncCountPV.get();
	}

	@Override
	public void setIgnoreGate(boolean ignoreGate) throws IOException{
		IgnoreGatePV.putWait(ignoreGate);
	}

	@Override
	public boolean getIgnoreGate() throws IOException{
		return IgnoreGatePV.get();
	}

	@Override
	public void setInputLogicPolarity(boolean inputLogicParity) throws IOException{
		InputLogicPolarityPV.putWait(inputLogicParity);
	}

	@Override
	public boolean getInputLogicPolarity() throws IOException{
		return InputLogicPolarityPV.get();
	}

	@Override
	public void setPixelsPerRun(int pixelsPerRun) throws IOException{
		PixelsperRunPV.putWait(pixelsPerRun);
	}

	@Override
	public int getPixelsPerRun() throws IOException{
		return PixelsperRunPV.get();
	}

	@Override
	public void setAutoPixelsPerBuffer(boolean autoPixelsPerBuffer) throws IOException{
		AutoPixelsPerBufferPV.putWait(autoPixelsPerBuffer);
	}

	@Override
	public boolean getAutoPixelsPerBuffer() throws IOException{
		return AutoPixelsPerBufferPV.get();
	}

	@Override
	public void setPixelsPerBuffer(int pixelsPerBuffer) throws IOException{
		PixelsPerBufferPV.putWait(pixelsPerBuffer);
	}

	@Override
	public int getPixelsPerBuffer() throws IOException{
		return PixelsPerBufferPV.get();
	}

	@Override
	public void setNextPixel() throws IOException{
		NextPixelPV.putWait(true);
	}

	@Override
	public int getCurrentPixel() throws IOException{
		return CurrentPixelPV.get();
	}
}

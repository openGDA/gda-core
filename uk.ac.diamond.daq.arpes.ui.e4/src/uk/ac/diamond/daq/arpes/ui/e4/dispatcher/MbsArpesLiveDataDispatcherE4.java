package uk.ac.diamond.daq.arpes.ui.e4.dispatcher;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

public class MbsArpesLiveDataDispatcherE4 extends AbstractBaseArpesLiveDataDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(MbsArpesLiveDataDispatcherE4.class);

	@Override
	protected void emitNewData(IDataset data) throws TimeoutException, CAException, InterruptedException {
			// This bit is for Slicing view or any view that wants to get progress on
			// iterations
			if (isMonitorIterationProgress()) {
				int totalScans = epicsController.cagetInt(getChannel(numScansPV));
				int progressScans = epicsController.cagetInt(getChannel(progressCounterPV));
				switch (acquisitionMode) {
					case AcquisitionMode.SWEPT -> {
						int totalSteps = epicsController.cagetInt(getChannel(numStepsSweptPV));
						int currentStep = epicsController.cagetInt(getChannel(currentStepSweptPV));
						dataUpdate.setUpdateSameFrame((progressScans!=0) || (currentStep!=1));
						logger.debug("SWEPT mode");
						logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
						logger.debug("UPDATING SAME FRAME? {}", (progressScans!=0) || (currentStep!=1));
						logger.debug("CurrentStep: {}, TotalSteps: {}",currentStep,totalSteps);
					}
					case AcquisitionMode.FIXED -> {
						dataUpdate.setUpdateSameFrame(progressScans != 1); // this is a bug fix that sometimes instead of
																			// max counter ioc returns 0.
						logger.debug("FIXED mode");
						logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
						logger.debug("UPDATING SAME FRAME? {}", (progressScans!=1));
					}
					case AcquisitionMode.DITHER -> {
						dataUpdate.setUpdateSameFrame(progressScans != 1);
						logger.debug("DITHER mode");
						logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
						logger.debug("UPDATING SAME FRAME? {}", (progressScans!=1));
					}
				}
			}
			dataUpdate.setData(data);// rely on PV that updates with accumulated data with iterations
			notifyListeners(dataUpdate);
	}

	private void notifyListeners(LiveDataPlotUpdate evt) {
		logger.debug("Notifying listeners on new data");
		observableComponent.notifyIObservers(this, evt);
	}

	@Override
	protected void acquireStatusChanged(final MonitorEvent event) {
		logger.debug("Received change of acquire state: {}", event);
	}
}
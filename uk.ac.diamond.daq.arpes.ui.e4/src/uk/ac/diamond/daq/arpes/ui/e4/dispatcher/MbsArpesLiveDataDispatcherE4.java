package uk.ac.diamond.daq.arpes.ui.e4.dispatcher;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;

public class MbsArpesLiveDataDispatcherE4 extends AbstractBaseArpesLiveDataDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(MbsArpesLiveDataDispatcherE4.class);
	private static final short NEW_IMAGE_INDICATOR = 1;
	private boolean updateSameFrame;
	private int number;

	@Override
	protected void emitNewData(IDataset data) throws TimeoutException, CAException, InterruptedException {
			dataUpdate.setData(data);// rely on PV that updates with accumulated data with iterations
			dataUpdate.setUpdateSameFrame(updateSameFrame);
			notifyListeners(dataUpdate);
	}

	@Override
	protected void monitorNumExposures(final MonitorEvent event) {
		logger.trace("Received change of acquire state: {}", event);
		try {
			number = epicsController.cagetInt(getChannel(numExposuresPV));
		} catch (Exception e) {
			logger.error("Error getting number exposures", e);
		}

		updateSameFrame  = (number != NEW_IMAGE_INDICATOR);
	}
}
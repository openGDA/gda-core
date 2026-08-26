package uk.ac.diamond.daq.arpes.ui.e4.dispatcher;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class MbsArpesLiveDataDispatcherE4 extends AbstractBaseArpesLiveDataDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(MbsArpesLiveDataDispatcherE4.class);
	private static final short NEW_IMAGE_INDICATOR = 1;
	private volatile boolean updateSameFrame;
	private int number;

	protected void emitNewData(IDataset data) throws TimeoutException, CAException, InterruptedException {
			dataUpdate.setData(data);// rely on PV that updates with accumulated data with iterations
			dataUpdate.setUpdateSameFrame(updateSameFrame);
			notifyListeners(dataUpdate);
	}

	protected void updateNumExposure() {
		try {
			number = epicsController.cagetInt(getChannel(numExposuresPV));
		} catch (Exception e) {
			logger.error("Error getting number exposures", e);
		}
		logger.debug("Frame number monitor is {}", number);
		updateSameFrame  = (number != NEW_IMAGE_INDICATOR);
	}
}
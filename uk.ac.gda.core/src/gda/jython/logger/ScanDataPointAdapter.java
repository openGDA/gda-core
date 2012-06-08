package gda.jython.logger;

import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.JythonServerFacade;
import gda.scan.ScanDataPoint;

/**
 * A ScanDataPointLineLoggerAdapter listens for ScanDataPoints and logs them to the specified logger.
 */
public class ScanDataPointAdapter implements IScanDataPointObserver {

	private final LineLogger logger;

	/**
	 * 
	 * @param logger
	 * @param scanDataPointProvider The GDA's {@link JythonServerFacade} singleton is often a good choice.
	 */
	ScanDataPointAdapter(LineLogger logger, IScanDataPointProvider scanDataPointProvider) {
		this.logger = logger;
		scanDataPointProvider.addIScanDataPointObserver(this);
	}

	@Override
	public void update(Object source, Object event) {

		if (event instanceof ScanDataPoint) {
			// JSF sends updates to all IObservers no matter which Provider interface they registered with
			ScanDataPoint sdp = (ScanDataPoint) event;
			if (sdp.getCurrentPointNumber()==0) {
				logger.log(sdp.getHeaderString() + "\n");
			}
			logger.log(((ScanDataPoint) event).toFormattedString() + "\n");
		}
	}
}
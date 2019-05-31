package uk.ac.diamond.daq.experiment.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Software-triggered {@link IExperimentDriver} which controls a {@link IScannableMotor}.
 */
@ServiceInterface(IExperimentDriver.class)
public class GdaScannableDriver extends ExperimentDriverBase<SingleAxisLinearSeries> {

	private static final Logger logger = LoggerFactory.getLogger(GdaScannableDriver.class);

	private final IScannableMotor scannableMotor;
	private double tolerance = 0.005;
	private final String quantityName;
	private final String quantityUnits;
	
	public GdaScannableDriver(IScannableMotor scannableMotor, String quantityName, String quantityUnits) {
		this.scannableMotor = scannableMotor;
		double demandPositionTolerance = scannableMotor.getDemandPositionTolerance();
		if (!Double.isNaN(demandPositionTolerance)) {
			this.tolerance = demandPositionTolerance;
		}
		this.quantityName = quantityName;
		this.quantityUnits = quantityUnits;
	}

	@Override
	protected void doZero() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doStart() {
		try {
			double originalSpeed = scannableMotor.getSpeed();

			for (DriverProfileSection section : getModel().getProfile()) {
				if (getState() == DriverState.RUNNING) {
					logger.info("Running {}", section);
	
					if (!isAtStartPosition(section.getStart())) {
						scannableMotor.setSpeed(originalSpeed);
						scannableMotor.moveTo(section.getStart());
					}
	
					if (section.getStart()==section.getStop()) {
						hold(section.getDuration());
					} else {
						ramp(section);
					}
				}
			}
			
			if (getState() == DriverState.RUNNING) {
				logger.info("Driver profile complete");
			}

			scannableMotor.setSpeed(originalSpeed);
		} catch (DeviceException deviceException) {
			logger.error("Error running driver profile", deviceException);
		} catch (InterruptedException interruptedException) {
			logger.error("Execution interrupted!", interruptedException);
			Thread.currentThread().interrupt();
		}
	}

	private void hold(double duration) throws InterruptedException {
		Thread.sleep((long) (duration * 60 * 1000.0));
	}

	private void ramp(DriverProfileSection section) throws DeviceException {
		double speed = Math.abs(section.getStop() - section.getStart()) / (section.getDuration() * 60);
		scannableMotor.setSpeed(speed);
		scannableMotor.moveTo(section.getStop());
	}

	private boolean isAtStartPosition(double startPosition) throws DeviceException {
		double currentPosition = (double) scannableMotor.getPosition();
		return Math.abs(currentPosition-startPosition) <= tolerance;
	}

	@Override
	protected void doPause() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doResume() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doAbort() {
		try {
			scannableMotor.stop();
		} catch (DeviceException e) {
			logger.error("Error aborting driver", e);
		}
	}

	@Override
	public String getQuantityName() {
		return quantityName != null ? quantityName : super.getQuantityName();
	}
	
	@Override
	public String getQuantityUnits() {
		return quantityUnits != null ? quantityUnits : super.getQuantityUnits();
	}
	
	@Override
	public String toString() {
		return "GdaScannableDriver [scannableMotor=" + scannableMotor + "]";
	}

}
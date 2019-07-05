package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FindableBase;

public class ScannableSignalSource extends FindableBase implements DoubleSupplier {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableSignalSource.class);
	private Scannable scannable;

	@Override
	public double getAsDouble() {
		try {
			return (double) scannable.getPosition();
		} catch (DeviceException e) {
			logger.error("Could not read position from scannable", e);
			return 0;
		}
	}
	
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

}

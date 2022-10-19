package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;

/**
 * {@link Scannable} wrapping a {@link DoubleSupplier}
 */
public class ExternalSourceWrapper extends ScannableBase {
	
	private DoubleSupplier source;

	public ExternalSourceWrapper(DoubleSupplier source) {
		this(source, source.toString());
	}
	
	public ExternalSourceWrapper(DoubleSupplier source, String name) {
		this.source = source;
		setName(name);
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		return source.getAsDouble();
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}

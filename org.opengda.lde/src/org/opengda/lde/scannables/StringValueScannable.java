package org.opengda.lde.scannables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class StringValueScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(StringValueScannable.class);

	private String value="Undefined";
	private String name;

	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	public StringValueScannable() {
	}

	@Override
	public void configure() throws gda.factory.FactoryException {
		if (!isConfigured()) {
			setName(name);
			setInputNames(new String[] { name });
			setOutputFormat(new String[] { "%s" });
			setConfigured(true);
		}
	}
	@Override
	public Object getPosition() throws DeviceException {
		return value;
	}
	@Override
	public void asynchronousMoveTo(Object externalPosition)
			throws DeviceException {
		this.value=externalPosition.toString();
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
	@Override
	public String toFormattedString() {
		try {
			return getName() + " : " + getPosition();
		} catch (Exception e) {
			logger.warn("Exception formatting position for {}", getName(), e);
			return valueUnavailableString();
		}
	}
}

package org.opengda.lde.scannables;

import gda.device.DeviceException;

import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class FileReferenceScannable extends ScannableBase {

	private String calibrationFilename="Undefined";
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public FileReferenceScannable() {
	}
	
	public void configure() throws gda.factory.FactoryException {
		if (!configured) {
			setName(name);
			setInputNames(new String[] { name });
			setOutputFormat(new String[] { "%s" });
			configured=true;
		}
	};
	@Override
	public Object getPosition() throws DeviceException {
		return calibrationFilename;
	}
	@Override
	public void asynchronousMoveTo(Object externalPosition)
			throws DeviceException {
		this.calibrationFilename=externalPosition.toString();
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
	@Override
	public String toFormattedString() {
		try {
			return getName() +": "+getPosition();
		} catch (Exception e) {
			throw new RuntimeException("Exception in " + getName() + ".toFormattedString()", e);
		}
	}
}

/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.scan.nexus.device;

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_DECIMALS;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.PROPERTY_VALUE_WRITE_DECIMALS;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.IGDAScannableNexusDevice;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Abstract superclass of classes that adapt {@link Scannable}s (including {@link Detector}s)
 * to {@link INexusDevice}, so that they can be used by the {@link NexusScanFileService}.
 *
 * @param <N>
 */
public abstract class AbstractNexusDeviceAdapter<N extends NXobject> implements IWritableNexusDevice<N>, IGDAScannableNexusDevice<N> {

	private static final String PROPERTY_NAME_FLOAT_FILL_VALUE = "gda.nexus.floatfillvalue";

	protected static final int DOUBLE_DATA_BYTE_SIZE = 8; // the number of bytes in a double

	private static final List<String> SPECIAL_ATTRIBUTES =
			Collections.unmodifiableList(Arrays.asList(Scannable.ATTR_NX_CLASS, Scannable.ATTR_NEXUS_CATEGORY));

	private static Logger logger = LoggerFactory.getLogger(AbstractNexusDeviceAdapter.class);

	private final Scannable device;

	protected AbstractNexusDeviceAdapter(final Scannable device) {
		this.device = device;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	protected Scannable getDevice() {
		return device;
	}

	/**
	 * Add the attributes for the given attribute container into the given nexus object.
	 * TODO: consider removing this and providing an easier mechanism. see DAQ-3186.
	 *
	 * @throws NexusException if the attributes could not be added for any reason
	 */
	protected void registerAttributes(NXobject nexusObject) throws NexusException {
		// We create the attributes, if any
		try {
			final Set<String> attributeNames = getDevice().getScanMetadataAttributeNames();
			for (String attrName : attributeNames) {
				addAttribute(nexusObject, getDevice(), attrName);
			}
		} catch (DeviceException e) {
			throw new NexusException("Could not get attributes of device: " + getName());
		}
	}

	private void addAttribute(NXobject nexusObject, final Scannable scannable, String attrName) throws NexusException {
		if (!SPECIAL_ATTRIBUTES.contains(attrName)) {
			try {
				nexusObject.setField(attrName, scannable.getScanMetadataAttribute(attrName));
			} catch (Exception e) {
				throw new NexusException(
						MessageFormat.format("An exception occurred attempting to get the value of the attribute ''{0}'' for the device ''{1}''",
								scannable.getName(), attrName));
			}
		}
	}

	protected void addAttributesToDataNode(String inputFieldName, int numDecimals, String unitsStr,
			final DataNode dataNode) {
		// set 'local_name' attribute to the scannable + input field name
		dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_LOCAL_NAME, getName() + "." + inputFieldName));
		// set field name attribute so we can recreate the scannable position from the nexus file (is this needed if its the same as above)?
		dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME, inputFieldName));

		// set units attribute
		if (unitsStr != null) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_UNITS, unitsStr));
		}
		// set 'decimals' attribute if required
		if (numDecimals != -1) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_DECIMALS, numDecimals));
		}
	}

	protected int[] getNumDecimalsArray(final Scannable scannable) {
		if (!LocalProperties.check(PROPERTY_VALUE_WRITE_DECIMALS, false)) return null;
		if (scannable.getOutputFormat() == null) return null;

		// note, scannable outputFormat must be set to an array of the same length as the scannable position
		return Arrays.stream(scannable.getOutputFormat()).mapToInt(this::getNumDecimals).toArray();
	}

	// copied from java.util.Formatter
	private static final Pattern FORMAT_PATTERN = Pattern.compile(
			"%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"); // NOSONAR - pattern is correct
	private static final String FLOAT_CONVERSIONS = "fgeFGE"; // conversion characters for floating-point values for java.util.Formatter

	private int getNumDecimals(String outputFormat) {
		// the output format is a format string for the String.format() method, e.g. "%5.3g", where the
		// (optional) first digit is the number of digits required before the decimal point in the output string,
		// the second digit is the number of digits after. The final letter will be either e, f, or g
		// for floating point numbers - the only type we are concerned with
		final Matcher matcher = FORMAT_PATTERN.matcher(outputFormat);

		if (matcher.find(0)) {
			final char conversion = outputFormat.charAt(matcher.start(6));
			if (FLOAT_CONVERSIONS.indexOf(conversion) == -1) {
				return -1; // not a float
			}
			final int precisionStart = matcher.start(4); // group 4 is the precision group (\\.\\d+)?
			if (precisionStart >= 0) {
				// parse the precision as an int (start + 1 to skip the leading '.')
				final int precisionEnd = matcher.end(4);
				final int precision = Integer.parseInt(outputFormat, precisionStart + 1, precisionEnd, 10);
				if (precision > 0) {
					return precision;
				} else {
					logger.warn("Invalid precision in output format ''{}'' for scannable ''{}''", outputFormat, getName());
				}
			}
		} else {
			logger.warn("Invalid output format ''{}'' for scannable ''{}''", outputFormat, getName());
		}

		return -1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.debug("Creating nexus object for device {}", getName());
		if (getDevice() instanceof INexusDevice<?>) {
			return ((INexusDevice<N>) getDevice()).getNexusProvider(info);
		}

		final N nexusObject = createNexusObject(info);
		return createNexusProvider(nexusObject, info);
	}

	protected abstract N createNexusObject(NexusScanInfo info) throws NexusException;

	protected NexusObjectProvider<N> createNexusProvider(N nexusObject, NexusScanInfo info) throws NexusException {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(getName(), nexusObject);
		configureNexusWrapper(nexusWrapper, info);
		return nexusWrapper;
	}

	/**
	 * Configure the nexus wrapper, an object that contains the nexus object and contains additional information
	 * describing how the nexus object should be added to the nexus tree - in particular to {@link NXdata} groups.
	 *
	 * @param nexusWrapper
	 * @param info
	 * @throws NexusException
	 */
	protected void configureNexusWrapper(NexusObjectWrapper<N> nexusWrapper, @SuppressWarnings("unused") NexusScanInfo info) throws NexusException {
		nexusWrapper.setPrimaryDataFieldName(getPrimaryDataFieldName());
	}

	protected abstract String getPrimaryDataFieldName();

	protected static Object getFillValue(Class<?> clazz) {
		if (clazz.equals(Double.class)) {
			String floatFill = LocalProperties.get(PROPERTY_NAME_FLOAT_FILL_VALUE, "nan");
			return floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill);
		} else if (clazz.equals(Float.class)) {
			String floatFill = LocalProperties.get(PROPERTY_NAME_FLOAT_FILL_VALUE, "nan");
			return floatFill.equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(floatFill);
		} else if (clazz.equals(Byte.class)) {
			return (byte) 0;
		} else if (clazz.equals(Short.class)) {
			return (short) 0;
		} else if (clazz.equals(Integer.class)) {
			return 0;
		} else if (clazz.equals(Long.class)) {
			return (long) 0;
		}
		return null;
	}

	@Override
	@ScanEnd
	public void scanEnd() throws NexusException {
		// do nothing, subclasses may override
		// (note, keeping this method is useful as subclasses may call super.scanEnd() which will
		// fail to compile if this method is removed. If that call is removed, it would be very easy to
		// forget to add it again if necessary
	}

}

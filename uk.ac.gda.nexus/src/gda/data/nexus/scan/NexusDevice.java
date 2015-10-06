package gda.data.nexus.scan;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;

/**
 * This interface defines a method that returns a new instance
 * of the appropriate NeXus base class for this device.
 *
 * @param <D> the NeXus bass class for the device
 */
public interface NexusDevice<D extends NXobject> {

	/**
	 * Enumeration of device types.
	 *
	 */
	public enum DeviceType {
		INSTRUMENT, SAMPLE
	}

	/**
	 * The {@link Class} of the NeXus object returned by
	 * {@link #createBaseClassInstance(NXobjectFactory)}, a
	 * subclass of {@link NXobject}.
	 *
	 * @return NeXus base class
	 */
	public Class<D> getNexusBaseClass();

	/**
	 * Returns a new instance of the appropriate NeXus base class for this device.
	 *
	 * @param nxObjectFactory
	 *            object factory to use to create instances of NeXus base classes
	 * @return new instance of NeXus base class
	 */
	public D createBaseClassInstance(NXobjectFactory nxObjectFactory);


	/**
	 * Returns the device type. The {@link NexusFileBuilder} uses this to determine which element to add the NeXus base class instance for the device to. TODO:
	 * is an enum the best way to do this - we could use a path instead?
	 *
	 * @return device type
	 */
	public DeviceType getDeviceType();

	/**
	 * Return the name of the device, cannot be <code>null</code> or empty.
	 *
	 * TODO: maybe if this is null or empty we use a default name based on the class,
	 * e.g. for an NXdetector we use 'detector'
	 * @return device name
	 */
	public String getName();

	/**
	 * Returns the default dataset for the device.
	 *
	 * @return dataset
	 */
	public ILazyWriteableDataset getDefaultWriteableDataset();

	/**
	 * Returns the dataset with the given name or path
	 * 
	 * @param path
	 *            name or path
	 * @return dataset
	 */
	public ILazyWriteableDataset getDataset(String path);

}

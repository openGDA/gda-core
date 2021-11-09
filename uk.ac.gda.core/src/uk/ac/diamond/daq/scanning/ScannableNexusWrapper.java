package uk.ac.diamond.daq.scanning;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.PositionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.nexus.device.AbstractScannableNexusDevice;
import gda.data.scan.nexus.device.DefaultScannableNexusDevice;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.IScannableMotor;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.scannablegroup.ScannableGroup;

/**
 * An instance of this class wraps a {@link Scannable} and adapts to both {@link IScannable} and {@link INexusDevice}.
 * This allows a scannable to be used in a new scanning (a.k.a. solstice scanning) scan and write to the nexus file.
 * The nexus writing delegated to an instanceof {@link AbstractScannableNexusDevice}.
 *
 * Note that an instance of this class is used only in new scanning.
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 */
public class ScannableNexusWrapper<N extends NXobject> extends AbstractScannable<Object> implements INexusDevice<N>, IMultipleNexusDevice {

	private static final Logger logger = LoggerFactory.getLogger(ScannableNexusWrapper.class);

	/**
	 * The GDA8 scannable being wrapped.
	 * <BR><BR>NOTE: <b>always use {@link #getScannable()}</b> rather than
	 * accessing this field directly as subclasses override it.
	 */
	private Scannable scannable_only_use_via_getScannable;

	/**
	 * Encapsulates how to creating nexus for this scannable.
	 */
	private AbstractScannableNexusDevice<N> scannableNexusDevice;

	private PositionDelegate positionDelegate;

	private Object previousPosition = null;

	/**
	 * Used from spring
	 */
	public ScannableNexusWrapper() {
		super(ScannableDeviceConnectorService.getInstance());
		this.positionDelegate = new PositionDelegate(this);
	}

	ScannableNexusWrapper(Scannable scannable) {
		setScannable(scannable);
		this.positionDelegate = new PositionDelegate(this);
	}

	/**
	 * Used from spring to connect the wrapper to a particular GDA8 scannable.
	 * @param scannable the GDA8 scannable to wrap
	 * @throws IllegalStateException if the scannable is already set
	 */
	public void setScannable(Scannable scannable) {
		if (this.scannable_only_use_via_getScannable != null) {
			throw new IllegalStateException("The wrapped scannable has already been set");
		}

		this.scannable_only_use_via_getScannable = scannable;
		if (canReadPosition()) {
			try {
				this.previousPosition = scannable.getPosition();
			} catch (DeviceException e) {
				logger.error("Could not get position of scannable ''{}''", scannable.getName(), e);
			}
			this.scannable_only_use_via_getScannable.addIObserver(this::update);
		}
	}

	public Scannable getScannable() {
		return scannable_only_use_via_getScannable;
	}

	public boolean canReadPosition() {
		// don't read the positions of scannable groups or detectors
		return !(getScannable() instanceof ScannableGroup || getScannable() instanceof Detector);
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		throw new UnsupportedOperationException("getNexusProviders() should be called instead of this method");
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		if (getScannable().getInputNames().length > 1 && info.getScanRole(getName()) == ScanRole.SCANNABLE) {
			throw new UnsupportedOperationException("New scanning does not support scanning over scannables with multiple input fields");
		}

		return getScannableNexusDevice(true).getNexusProviders(info);
	}

	private AbstractScannableNexusDevice<N> getScannableNexusDevice(boolean create) {
		if (scannableNexusDevice == null && create) {
			scannableNexusDevice = new DefaultScannableNexusDevice<>(getScannable(), true);
		}
		return scannableNexusDevice;
	}

	@Override
	public CustomNexusEntryModification getCustomNexusModification() {
		// called after getNexusProviders, so scannableNexusDevice should never be null
		return getScannableNexusDevice(false).getCustomNexusModification();
	}

	@Override
	public void setLevel(int level) {
		getScannable().setLevel(level);
	}

	@Override
	public int getLevel() {
		return getScannable().getLevel();
	}

	@Override
	public String getName() {
		return getScannable().getName();
	}

	@Override
	public void setName(String name) {
		getScannable().setName(name);
	}

	@Override
	public Object getPosition() throws ScanningException {
		if (canReadPosition()) {
			try {
				return getScannable().getPosition();
			} catch (DeviceException e) {
				throw new ScanningException("Could not get position of scannable: " + getName(), e);
			}
		}
		return null;
	}

	@Override
	public Object setPosition(Object value) throws ScanningException {
		return setPosition(value, null);
	}

	@Override
	public Object setPosition(Object value, IPosition scanPosition) throws ScanningException {
		try {
			final Scannable scannable = getScannable();
			if (value != null) {
				final int index = (scanPosition == null ? -1 : scanPosition.getIndex(getName()));
				final IPosition position = new Scalar<Object>(getName(), index, value);
				positionDelegate.firePositionWillPerform(position);
				logger.debug("Moving scannable {} to position {} at {}", scannable.getName(), value, scanPosition);
				scannable.moveTo(value);
				positionDelegate.firePositionPerformed(getLevel(), position);
			} else {
				logger.debug("Ignoring request to move scannable {} to position {} at {}", scannable.getName(), value, scanPosition);
			} // setPosition is called with a value==null if it is a monitor in a scan and doesn't need to be moved.

			if (scanPosition != null && getScannableNexusDevice(false) != null) {
				return getScannableNexusDevice(false).writePosition(value, scanPosition);  // It stops it being read again.
			}
		} catch (Exception e) {
			throw new ScanningException("Could not set position of scannable " + getName(), e);
		}

		// We didn't read real position again when setting the value so we cannot provide the
		// new position.
		return null;
	}

	@Override
	public String getUnit() {
		final Scannable scannable = getScannable();
		if (scannable instanceof ScannableMotionUnits) {
			return ((ScannableMotionUnits) scannable).getUserUnits();
		}
		if (scannable instanceof Monitor) {
			try {
				return ((Monitor) scannable).getUnit();
			} catch (DeviceException e) {
				logger.error("{}: Could not get units for scannable", scannable.getName(), e);
			}
		}

		return null;
	}

	@Override
	public Object getMaximum() {
		final Scannable scannable = getScannable();
		if (scannable instanceof ScannableMotion) {
			// return upper limit for first input name
			final Double[] upperLimits = ((ScannableMotion) scannable).getUpperGdaLimits();
			if (upperLimits != null) {
				return upperLimits[0];
			} else {
				if (scannable instanceof IScannableMotor) {
					try {
						return ((IScannableMotor) scannable).getUpperMotorLimit();
					} catch (DeviceException e) {
						logger.error("Could not read upper motor limit for {}", scannable.getName(), e);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Object getMinimum() {
		final Scannable scannable = getScannable();
		if (scannable instanceof ScannableMotion) {
			// return lower limit for first input name
			final Double[] lowerLimits = ((ScannableMotion) scannable).getLowerGdaLimits();
			if (lowerLimits != null) {
				return lowerLimits[0];
			} else {
				if (scannable instanceof IScannableMotor) {
					try {
						return ((IScannableMotor) scannable).getLowerMotorLimit();
					} catch (DeviceException e) {
						logger.error("Problem reading lower motor limit for {}", scannable.getName(), e);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String[] getPermittedValues() throws Exception {
		final Scannable scannable = getScannable();
		if (scannable instanceof EnumPositioner) {
			return ((EnumPositioner) scannable).getPositions();
		}
		return null;
	}

	@ScanFinally
	public void scanFinally() {
		// clear the nexus device for the next scan, allowing it and its datasets to be garbage collected
		scannableNexusDevice = null;
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		positionDelegate.addPositionListener(listener);
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		positionDelegate.removePositionListener(listener);
	}

	public void update(@SuppressWarnings("unused") Object source, Object arg) {
		if (arg instanceof ScannableStatus && arg != ScannableStatus.IDLE) {
			// wait until the scannable is IDLE
			return;
		}

		if (canReadPosition()) {
			Object newPosition = null;
			if (arg instanceof ScannablePositionChangeEvent) {
				newPosition = ((ScannablePositionChangeEvent) arg).newPosition;
			} else if (isValueType(arg.getClass())) {
				newPosition = arg;
			} else {
				try { // just get the new position from the scannable
					newPosition = getScannable().getPosition();
				} catch (Exception e) {
					logger.error("Could not get current position of scannable {}", getName());
				}
			}

			firePositionChanged(newPosition);
		}
	}

	private void firePositionChanged(Object newPosition) {
		if (newPosition != null && !Objects.equals(newPosition, previousPosition)) {
			final IPosition position = new Scalar<Object>(getName(), -1, newPosition);
			try {
				positionDelegate.firePositionChanged(getLevel(), position);
			} catch (Exception e) {
				logger.error("An error occurred while notifying position listeners", e);
			} finally {
				previousPosition = newPosition;
			}
		}
	}

	private static final Set<Class<?>> VALUE_TYPES = new HashSet<>(Arrays.asList(
			Double.class, Float.class,
			Long.class, Integer.class, Short.class, Byte.class,
			String.class, Character.class,
			Boolean.class));

	private static boolean isValueType(Class<?> klass) {
		if (klass.isArray()) {
			klass = klass.getComponentType();
		}
		return VALUE_TYPES.contains(klass) || (klass.isEnum() && !klass.equals(ScannableStatus.class)) ;
	}

	@Override
	public String toString() {
		return "ScannableNexusWrapper [scannable=" + getScannable() + "]";
	}

	@Override
	public void abort() throws ScanningException {
		try {
			getScannable().stop();
		} catch (DeviceException e) {
			throw new ScanningException("Device exception while stopping scannable " + getName(), e);
		}
	}

}

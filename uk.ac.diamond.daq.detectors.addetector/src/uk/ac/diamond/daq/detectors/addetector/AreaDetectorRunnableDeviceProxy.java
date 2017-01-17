/**
 *
 */
package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.device.detector.addetector.ADDetector;
import gda.factory.Finder;
import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * This is an implementation of a new style GDA detector which delegates much of its specific
 * functionality to an AbstractAreaDetectorRunnableDeviceDelegate. This allows the creation
 * of runnable devices defined by Jython classes which don't exist at spring configuration time.
 */
public class AreaDetectorRunnableDeviceProxy extends AbstractAreaDetectorRunnableDevice {

	private AbstractAreaDetectorRunnableDeviceDelegate delegate;
	private ADDetector detector;

	public AreaDetectorRunnableDeviceProxy() {
		super(ServiceHolder.getRunnableDeviceService());
	}

	// AbstractRunnableDevice<AreaDetectorRunnableDeviceModel>

	@Override
	public void configure(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Get the detector by name defined in the model
		detector = Finder.getInstance().find(model.getName());
		if (detector == null) throw new ScanningException("Could not find detector for " + model.getName());
		if (delegate == null) throw new ScanningException("No delegate defined for " + model.getName());

		try {
			delegate.configure(model);
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Failed configuring detector " + model.getName(), e);
		}
		setDeviceState(DeviceState.READY);
	}

	/**
	 * Allow delegates to set the device state.
	 *
	 * @param nstate New State
	 */
	@Override
	public void setDeviceState(DeviceState nstate) throws ScanningException {
		super.setDeviceState(nstate);
	}

	// Delegated interface IRunnableDevice<AreaDetectorRunnableDeviceModel> methods

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		delegate.run(position);
	}

	// Delegated interface IWritableDetector<AreaDetectorRunnableDeviceModel> methods

	@Override
	public boolean write(IPosition position) throws ScanningException {
		return delegate.write(position);
	}

	// Delegated interface INexusDevice<NXdetector> methods

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		return delegate.getNexusProvider(info);
	}

	// Delegated annotated methods

	@Override
	@PreConfigure
	public void preConfigure(Object model) throws ScanningException {
		delegate.preConfigure(model);
	}

	@Override
	@PostConfigure
	public void postConfigure(Object model) throws ScanningException {
		delegate.postConfigure(model);
	}

	@Override
	@LevelStart
	public void levelStart(LevelInformation info) throws ScanningException {
		delegate.levelStart(info);
	}

	@Override
	@LevelEnd
	public void levelEnd(LevelInformation info) throws ScanningException {
		delegate.levelEnd(info);
	}

	@Override
	@PointStart
	public void pointStart(IPosition point) throws ScanningException {
		delegate.pointStart(point);
	}

	@Override
	@PointEnd
	public void pointEnd(IPosition point) throws ScanningException {
		delegate.pointEnd(point);
	}

	@Override
	@ScanStart
	public void scanStart(ScanInformation info) throws ScanningException {
		delegate.scanStart(info);
	}

	@Override
	@ScanEnd
	public void scanEnd(ScanInformation info) throws ScanningException {
		delegate.scanEnd(info);
	}

	@Override
	@ScanAbort
	public void scanAbort(ScanInformation info) throws ScanningException {
		delegate.scanAbort(info);
	}

	@Override
	@ScanFault
	public void scanFault(ScanInformation info) throws ScanningException {
		delegate.scanFault(info);
	}

	@Override
	@ScanFinally
	public void scanFinally(ScanInformation info) throws ScanningException {
		delegate.scanFinally(info);
	}

	@Override
	@ScanPause
	public void scanPaused() throws ScanningException {
		delegate.scanPause();
	}

	@Override
	@ScanResume
	public void scanResumed() throws ScanningException {
		delegate.scanResume();
	}

	// Class methods

	public AbstractAreaDetectorRunnableDeviceDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(AbstractAreaDetectorRunnableDeviceDelegate delegate) {
		this.delegate = delegate;
		if (delegate.getRunnableDeviceProxy() != this ) {
			throw new RuntimeException("Delegates runnable device is not this!");
		}
	}

	public ADDetector getDetector() {
		return detector;
	}
}

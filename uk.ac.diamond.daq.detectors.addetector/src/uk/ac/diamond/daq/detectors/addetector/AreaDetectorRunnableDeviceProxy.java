/**
 *
 */
package uk.ac.diamond.daq.detectors.addetector;

import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.IScanAttributeContainer;
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
import org.eclipse.scanning.api.device.IActivatable;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;

import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * @author voo82358
 *
 */
public class AreaDetectorRunnableDeviceProxy implements
		IRunnableEventDevice<AreaDetectorRunnableDeviceModel>,
		IModelProvider<AreaDetectorRunnableDeviceModel>,
		IScanAttributeContainer,
		IPositionListenable,
		IActivatable,
		IWritableDetector<AreaDetectorRunnableDeviceModel>,
		INexusDevice<NXdetector> {

	private AbstractAreaDetectorRunnableDevice rDevice;

	// interface INameable

	private String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	// proxies AbstractRunnableDevice<AreaDetectorRunnableDeviceModel>

	@Override
	public boolean isActivated() {
		return rDevice.isActivated();
	}

	@Override
	public boolean setActivated(boolean activated) throws ScanningException {
		return rDevice.setActivated(activated);
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		rDevice.addPositionListener(listener);
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		rDevice.removePositionListener(listener);
	}

	// interface IRunnableDevice (AbstractRunnableDevice)

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		rDevice.run(position);
	}

	// interface IWritableDetector

	@Override
	public boolean write(IPosition position) throws ScanningException {
		return rDevice.write(position);
	}

	// interface IWritableDetector interfaces

	@Override
	public void addRunListener(IRunListener l) throws ScanningException {
		rDevice.addRunListener(l);
	}

	@Override
	public void removeRunListener(IRunListener l) throws ScanningException {
		rDevice.removeRunListener(l);
	}

	@Override
	public void fireRunWillPerform(IPosition position) throws ScanningException {
		rDevice.fireRunWillPerform(position);
	}

	@Override
	public void fireRunPerformed(IPosition position) throws ScanningException {
		rDevice.fireRunPerformed(position);
	}

	@Override
	public void fireWriteWillPerform(IPosition position) throws ScanningException {
		rDevice.fireWriteWillPerform(position);
	}

	@Override
	public void fireWritePerformed(IPosition position) throws ScanningException {
		rDevice.fireWritePerformed(position);
	}

	@Override
	public void pause() throws ScanningException {
		rDevice.pause();
	}

	@Override
	public void seek(int stepNumber) throws ScanningException {
		rDevice.seek(stepNumber);
	}

	@Override
	public void resume() throws ScanningException {
		rDevice.resume();
	}

	@Override
	public DeviceState getDeviceState() throws ScanningException {
		return rDevice.getDeviceState();
	}

	@Override
	public String getDeviceStatus() throws ScanningException {
		return rDevice.getDeviceStatus();
	}

	@Override
	public boolean isDeviceBusy() throws ScanningException {
		return rDevice.isDeviceBusy();
	}

	@Override
	public void abort() throws ScanningException {
		rDevice.abort();
	}

	@Override
	public void disable() throws ScanningException {
		rDevice.disable();
	}

	@Override
	public AreaDetectorRunnableDeviceModel getModel() {
		return rDevice.getModel();
	}

	@Override
	public Object getAttribute(String attribute) throws ScanningException {
		return rDevice.getAttribute(attribute);
	}

	@Override
	public <A> List<A> getAllAttributes() throws ScanningException {
		return rDevice.getAllAttributes();
	}

	@Override
	public DeviceRole getRole() {
		if (rDevice == null) {
			return DeviceRole.HARDWARE;
		}
		return rDevice.getRole();
	}

	@Override
	public void setRole(DeviceRole role) {
		rDevice.setRole(role);
	}

	@Override
	public Set<ScanMode> getSupportedScanModes() {
		return rDevice.getSupportedScanModes();
	}

	@Override
	public void setLevel(int level) {
		rDevice.setLevel(level);
	}

	@Override
	public int getLevel() {
		return rDevice.getLevel();
	}

	@Override
	public void configure(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		rDevice.configure(model);
	}

	@Override
	public void reset() throws ScanningException {
		rDevice.reset();
	}

	// interface INexusDevice

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		return rDevice.getNexusProvider(info);
	}

	@PreConfigure
	public void preConfigure(Object model) throws ScanningException {
		rDevice.preConfigure(model);
	}

	@PostConfigure
	public void postConfigure(Object model) throws ScanningException {
		rDevice.postConfigure(model);
	}

	@LevelStart
	public void levelStart(LevelInformation info) throws ScanningException {
		rDevice.levelStart(info);
	}

	@LevelEnd
	public void levelEnd(LevelInformation info) throws ScanningException {
		rDevice.levelEnd(info);
	}

	@PointStart
	public void pointStart(IPosition point) throws ScanningException {
		rDevice.pointStart(point);
	}

	@PointEnd
	public void pointEnd(IPosition point) throws ScanningException {
		rDevice.pointEnd(point);
	}

	@ScanStart
	public void scanStart(ScanInformation info) throws ScanningException {
		rDevice.scanStart(info);
	}

	@ScanEnd
	public void scanEnd(ScanInformation info) throws ScanningException {
		rDevice.scanEnd(info);
	}

	@ScanAbort
	public void scanAbort(ScanInformation info) throws ScanningException {
		rDevice.scanAbort(info);
	}

	@ScanFault
	public void scanFault(ScanInformation info) throws ScanningException {
		rDevice.scanFault(info);
	}

	@ScanFinally
	public void scanFinally(ScanInformation info) throws ScanningException {
		rDevice.scanFinally(info);
	}

	@ScanPause
	public void scanPaused() throws ScanningException {
		rDevice.scanPaused();
	}

	@ScanResume
	public void scanResumed() throws ScanningException {
		rDevice.scanResumed();
	}

	// Class methods

	public AbstractAreaDetectorRunnableDevice getRunnableDevice() {
		return rDevice;
	}

	public void setRunnableDevice(AbstractAreaDetectorRunnableDevice areaDetectorRunnableDevice) {
		this.rDevice = areaDetectorRunnableDevice;
		this.rDevice.setRunnableDeviceService(ServiceHolder.getRunnableDeviceService());
	}

}

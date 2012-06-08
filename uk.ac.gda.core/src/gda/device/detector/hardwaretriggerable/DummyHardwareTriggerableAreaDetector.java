/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.hardwaretriggerable;

import gda.device.Detector;
import gda.device.DeviceException;

import java.text.MessageFormat;

public class DummyHardwareTriggerableAreaDetector extends DummyHardwareTriggerableDetectorBase implements
		HardwareTriggerableDetector {

	private int fileNumber = 0;

	private int fileNumberForReadout = 0;

	public DummyHardwareTriggerableAreaDetector()
	{
		
	}

	public DummyHardwareTriggerableAreaDetector(String name) {
		setName(name);
		setInputNames(new String[] { name });
		setExtraNames(new String[] {});
		setOutputFormat(new String[] { "%s" });
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		fileNumberForReadout = fileNumber;
	}

	@Override
	public void collectData() throws DeviceException {
		if (isHardwareTriggering()) {
			fileNumberForReadout += 1;
		} else {
			triggerSingleImageCollection();
		}
	}

	@Override
	public Object readout() throws DeviceException {
		if (isHardwareTriggering()) {
			return generateFilePath(fileNumberForReadout);
		}
		return generateFilePath(fileNumber);
	}

	private String generateFilePath(int n) {
		return getName() + "/image" + n + ".img";
	}

	@Override
	public String getDescription() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "";
	}

	//
	private void triggerSingleImageCollection() throws DeviceException {
		final double deltaT = getCollectionTime();
		setStatus(Detector.BUSY);
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep((long) (deltaT * 1000));
					fileNumber += 1;
				} catch (InterruptedException e) {
					terminal.print("DummyHardwareTriggerableAreaDetector interupted while collecting single image\n");
				} finally {
					terminal.print(MessageFormat.format("{0} {1}s --> {2}\n", getName(), deltaT,
							generateFilePath(fileNumber)));
					setStatus(Detector.IDLE);
				}
			}
		}.start();
	}

	@Override
	void simulatedTriggerRecieved() {
		terminal.print(MessageFormat.format("{0} triggered --> {2}\n", getName(), generateFilePath(fileNumber)));
		fileNumber += 1;
	}
}
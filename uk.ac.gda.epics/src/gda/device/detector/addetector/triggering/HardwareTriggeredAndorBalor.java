/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;
import gda.scan.ScanInformation;

public class HardwareTriggeredAndorBalor extends HardwareTriggeredStandard {

	private TriggerMode triggerMode = TriggerMode.EXTERNAL;

	private ImageMode imageMode = ImageMode.FIXED;

	/** In milliseconds */
	private long triggerModeSleep;

	public HardwareTriggeredAndorBalor(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	public HardwareTriggeredAndorBalor(ADBase adBase) {
		super(adBase, -1);
	}


	public enum TriggerMode {
		INTERNAL, SOFTWARE, EXTERNAL, EXTERNAL_START, EXTERNAL_EXPOSURE
	}

	public enum ImageMode {
		FIXED, CONTINUOUS
	}

	@Override
	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(triggerMode.ordinal());
		Thread.sleep(triggerModeSleep);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setImageMode(imageMode.ordinal());
		configureTriggerMode();
		getAdBase().setNumImages(numImages);
		enableOrDisableCallbacks();
	}

	public TriggerMode getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(TriggerMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	public ImageMode getImageMode() {
		return imageMode;
	}

	public void setImageMode(ImageMode imageMode) {
		this.imageMode = imageMode;
	}

	public long getTriggerModeSleep() {
		return triggerModeSleep;
	}

	public void setTriggerModeSleep(long triggerModeSleep) {
		this.triggerModeSleep = triggerModeSleep;
	}

}

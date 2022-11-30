/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.ADBase;
import gda.scan.ScanInformation;

/**
 * This collection strategy will set the configured trigger mode for an Xspress3 detector.
 * If no Xspress3TriggerMode is set, the default of SOFTWARE will be used.
 * <br><br>
 * Example config:
 * <pre>
 * {@code	<bean id="software_triggered" class="gda.device.detector.addetector.triggering.Xspress3TriggeringStrategy">}
 * {@code		<constructor-arg ref="xsp3_adbase" />}
 * {@code		<property name="triggerMode">}
 * {@code			<value type="gda.device.detector.addetector.triggering.Xspress3TriggeringStrategy.Xspress3TriggerMode">SOFTWARE</value>}
 * {@code		</property>}
 * {@code	</bean>}
 *
 * {@code	<bean id="xsp3" class="gda.device.detector.addetector.ADDetector">}
 * {@code		<property name="adBase" ref="xsp3_adbase" />}
 * {@code		...}
 * {@code		<property name="collectionStrategy" ref="software_triggered" />}
 * {@code	</bean>}
 * </pre>
 */
public class Xspress3TriggeringStrategy extends SimpleAcquire {

	private static final Logger logger = LoggerFactory.getLogger(Xspress3TriggeringStrategy.class);

	public enum Xspress3TriggerMode {
		SOFTWARE, HARDWARE, BURST, TTL_VETO_ONLY, IDC, SOFTWARE_START_STOP, TTL_BOTH, LVDS_VETO_ONLY, LVDS_BOTH
	}

	public Xspress3TriggeringStrategy(ADBase adBase) {
		super(adBase, 0.);
	}

	private Xspress3TriggerMode triggerMode;

	public void setTriggerMode(Xspress3TriggerMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, numImages, scanInfo);
		if (triggerMode == null) {
			logger.info("No Xspress3TriggerMode set — using default (SOFTWARE)");
			triggerMode = Xspress3TriggerMode.SOFTWARE;
		}
		logger.debug("Setting trigger mode to {}", triggerMode);
		getAdBase().setTriggerMode(triggerMode.ordinal());

	}

}

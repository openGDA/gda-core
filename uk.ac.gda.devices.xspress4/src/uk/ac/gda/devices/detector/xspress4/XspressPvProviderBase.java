/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.ReadOnlyPV;
import gda.epics.connection.EpicsChannelManager;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

public abstract class XspressPvProviderBase {

	private static final Logger logger = LoggerFactory.getLogger(XspressPvProviderBase.class);

	private String prefix = "";
	private Map<XspressPvName, String> pvNameMap = new EnumMap<>(XspressPvName.class);

	public abstract void createPvs();

	protected String getPvName(XspressPvName pv) {
		return prefix + pvNameMap.getOrDefault(pv, pv.pvName());
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPvNameMap(Map<XspressPvName, String> pvNameMap) {
		this.pvNameMap = pvNameMap;
	}

	public void checkPvsExist() {
		getPvs().map(ReadOnlyPV::getPvName).forEach(XspressPvProviderBase::pvExists);
	}

	protected abstract Stream<ReadOnlyPV<?>> getPvs();

	public static boolean pvExists(String pvName) {
		try {
			EpicsChannelManager manager = new EpicsChannelManager();
			Channel channel = manager.createChannel(pvName, false);
			manager.creationPhaseCompleted();
			manager.tryInitialize(100);
			logger.debug("{} connection state = {}", pvName,  channel.getConnectionState());
			return channel.getConnectionState() == Channel.CONNECTED;
		} catch(IllegalStateException | CAException ex) {
			logger.error("Problem checking if PV {} exists", pvName, ex);
		}
		return false;
	}
}

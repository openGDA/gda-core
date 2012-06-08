/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.motor;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide a channel selection mechanism for McLennan motion controllers.
 */
public class McLennanChannelSelectorFactory {
	private static final Logger logger = LoggerFactory.getLogger(McLennanChannelSelectorFactory.class);

	private static Set<McLennanChannelSelector> set = new HashSet<McLennanChannelSelector>();

	/**
	 * returns the relevant ChannelSelector
	 * 
	 * @param controller
	 *            McLennanController
	 * @param axis
	 *            controller axis number
	 * @param channel
	 *            number
	 * @return ChannelSelector object
	 */
	public static ChannelSelector createChannelSelector(IMcLennanController controller, int axis, int channel) {
		if (channel == 0) {
			logger.debug("createchannelselector channel zero");
			return new DummyChannelSelector();
		}

		logger.debug("createchannelselector channel NOT zero");
		String nameString = controller.getName() + axis;

		logger.debug("createchannelselector name is " + nameString);

		for (McLennanChannelSelector mclChannelSelector : set) {
			logger.debug("createchannelselector mclchannelselector name" + mclChannelSelector.getName());

			if (mclChannelSelector.getName().equals(nameString)) {
				logger.debug("createchannelselector names equal");
				return mclChannelSelector;
			}
		}

		McLennanChannelSelector mclChannelSelector = new McLennanChannelSelector(controller, axis);

		logger.debug("createchannelselector channel selector created");

		set.add(mclChannelSelector);

		logger.debug("createchannelselector added mclchannel selector ok");

		return mclChannelSelector;
	}

}

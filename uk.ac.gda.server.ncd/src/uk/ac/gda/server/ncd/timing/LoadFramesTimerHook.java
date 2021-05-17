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

package uk.ac.gda.server.ncd.timing;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Timer;


/** Wrapper to allow hooks to be added to the Timer#loadFrameSets method */
public class LoadFramesTimerHook extends TimerWrapper {
	private static final Logger logger = LoggerFactory.getLogger(LoadFramesTimerHook.class);
	private Collection<Runnable> hooks = new ArrayList<>();

	public LoadFramesTimerHook(Timer timer) {
		super(timer);
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		super.loadFrameSets();
		logger.info("{} - Running {} hooks after loading frame sets", getName(), hooks.size());
		hooks.stream().forEach(h -> {
			try {
				h.run();
			} catch (Exception e) {
				logger.error("Error running loadFrameHook", e);
			}
		});
	}

	public void setHooks(Collection<Runnable> hooks) {
		this.hooks.clear();
		if (hooks != null) {
			this.hooks.addAll(hooks);
		}
	}

	public void addHook(Runnable hook) {
		hooks.add(hook);
	}

	public void removeHook(Runnable hook) {
		hooks.remove(hook);
	}

	@Override
	public String toString() {
		return "LoadFramesTimerHook(" + super.toString() + ")";
	}
}

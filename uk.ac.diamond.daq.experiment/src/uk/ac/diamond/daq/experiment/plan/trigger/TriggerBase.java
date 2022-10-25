/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan.trigger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.scanning.api.event.IdBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public abstract class TriggerBase extends FindableBase implements ITrigger {

	private static final Logger logger = LoggerFactory.getLogger(TriggerBase.class);

	private final Payload payload;
	private final ISampleEnvironmentVariable sev;
	private final IPlanRegistrar registrar;

	private ExecutorService executorService;
	private boolean enabled;
	private volatile boolean evaluating;

	private PayloadService payloadService;

	TriggerBase(IPlanRegistrar registrar, Payload payload, ISampleEnvironmentVariable sev) {
		this.registrar = registrar;
		this.payload = payload;
		this.sev = sev;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			// already in requested state
			// so we can ignore the call
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			enable();
		} else {
			disable();
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	protected void enable() {
		executorService = Executors.newSingleThreadExecutor();
		sev.addListener(this);
	}

	protected void disable() {
		sev.removeListener(this);
		executorService.shutdownNow();
	}

	protected ISampleEnvironmentVariable getSEV() {
		return sev;
	}

	protected Payload getPayload() {
		return payload;
	}

	@Override
	public String getSampleEnvironmentName() {
		return sev.getName();
	}

	@Override
	public synchronized void signalChanged(double signal) {
		if (evaluating) {
			logger.debug("Signal {} ignored by trigger '{}' because it is currently evaluating a previous one", signal, getName());
		} else {
			evaluating = true;
			try {
				if (evaluateTriggerCondition(signal)) { // FIXME all implementations should be purely functional
														// to move this outside synchronised method
					logger.debug("Trigger '{}' now triggering due to signal {}", getName(), signal);
					executorService.execute(()->{
						final TriggerEvent event = new TriggerEvent(signal);
						registrar.triggerOccurred(this);

						try {
							final Object id = getPayloadService().handle(payload);
							if (id instanceof IdBean) {
								event.setId(((IdBean) id).getUniqueId());
							}
						} catch (Exception e) {
							logger.error("Problem while executing trigger '{}'", getName(), e);
							event.setFailed(true);
						} finally {
							registrar.triggerComplete(this, event, getSEV().getName());
						}
					});
				}
			} finally {
				evaluating = false;
			}
		}
	}

	private PayloadService getPayloadService() {
		if (payloadService == null) {
			payloadService = SpringApplicationContextFacade.getBean(PayloadService.class);
		}
		return payloadService;
	}

	/**
	 * Determine whether the broadcasted signal should trigger us.
	 * Called from a synchronised method so it should be fast.
	 */
	protected abstract boolean evaluateTriggerCondition(double signal);

}

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

package uk.ac.diamond.daq.experiment.plan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.server.servlet.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.FindableBase;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.experiment.api.EventConstants;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.ConveniencePlanFactory;
import uk.ac.diamond.daq.experiment.api.plan.IExperimentRecord;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.IPlanFactory;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent.Transition;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public class Plan extends FindableBase implements IPlan, IPlanRegistrar, ConveniencePlanFactory {

	private static final Logger logger = LoggerFactory.getLogger(Plan.class);

	private final List<ISegment> segments = new LinkedList<>();
	private Optional<IExperimentDriver<?>> experimentDriver = Optional.empty();

	private Queue<ISegment> segmentChain;
	private ExperimentRecord experimentRecord;

	private IPlanFactory factory;
	private ISampleEnvironmentVariable lastDefinedSev;

	private ISegment activeSegment;

	protected ExperimentController experimentController;

	private ISubscriber<IBeanListener<ExperimentEvent>> controllerSubscriber;

	public Plan(String name) {
		super.setName(name);
		setFactory(new PlanFactory());

		createExperimentControllerSubscriber();
	}

	private void createExperimentControllerSubscriber() {
		try {
			URI activeMqUri = new URI(LocalProperties.getActiveMQBrokerURI());
			controllerSubscriber = Services.getEventService().createSubscriber(activeMqUri, EventConstants.EXPERIMENT_CONTROLLER_TOPIC);
		} catch (URISyntaxException e) {
			logger.error("Error creating experiment controller subscriber", e);
		}
	}

	private void disconnectExperimentControllerSubscriber() {
		try {
			controllerSubscriber.disconnect();
		} catch (EventException e) {
			logger.error("Error disconnecting experiment controller subscriber");
		}
	}

	@Override
	public void start() {
		experimentRecord = new ExperimentRecord(getName());

		try {
			validatePlan();
		} catch (IllegalStateException e) {
			logError("Invalid plan: " + e.getMessage(), e);
			return;
		}

		if (experimentDriver.isPresent()) {
			IExperimentDriver<?> driver = experimentDriver.get();
			experimentRecord.setDriverNameAndProfile(driver.getName(), driver.getModel().getName());
		}

		try {
			getExperimentController().startExperiment(getName());
		} catch (ExperimentControllerException e) {
			String message = "Error starting experiment: " + e.getMessage();
			logError(message, e);
			return;
		}

		experimentRecord.start();

		logger.info("Plan '{}' execution started", getName());
		printBanner("Plan '" + getName() + "' execution started");

		activateNextSegment();

		if (experimentDriver.isPresent()) {
			experimentDriver.get().start();
		}
		try {
			controllerSubscriber.addListener(event -> {
				ExperimentEvent stateChange = event.getBean();
				if (stateChange.getTransition().equals(Transition.STOPPED)) {
					abort();
				}
			});
		} catch (EventException e) {
			logger.error("Error attaching experiment controller listener", e);
		}
	}

	@Override
	public void abort() {
		if (!isRunning()) throw new IllegalStateException("Plan is not running");

		abortActiveSegment();
		experimentDriver.ifPresent(IExperimentDriver::abort);
		experimentRecord.aborted();
		endExperiment();
		disconnectExperimentControllerSubscriber();
	}

	private void abortActiveSegment() {
		activeSegment.abort();
		activeSegment = null;
		stopSegmentMultipartAcquisition();
	}

	private void validatePlan() {

		if (segments.isEmpty()) throw new IllegalStateException("No segments defined!");
		segmentChain = new LinkedList<>(segments);

		// Ensure all segments have unique names
		long uniqueSegmentNames = segments.stream().map(Findable::getName).distinct().count();
		if (segments.size() != uniqueSegmentNames) throw new IllegalStateException("Segments should have unique names!");

		// Ensure all triggers have unique names
		List<ITrigger> triggers = getTriggers();
		long uniqueTriggerNames = triggers.stream().map(Findable::getName).distinct().count();
		if (triggers.size() != uniqueTriggerNames) throw new IllegalStateException("Triggers should have unique names!");
	}

	private ExperimentController getExperimentController() {
		if (experimentController == null) {
			experimentController = SpringApplicationContextFacade.getBean(ExperimentController.class);
		}
		return experimentController;
	}

	private List<ITrigger> getTriggers() {
		return segments.stream().map(ISegment::getTriggers).flatMap(List::stream).distinct().collect(Collectors.toList());
	}

	private void activateNextSegment() {
		if (activeSegment != null) {
			stopSegmentMultipartAcquisition();
		}

		activeSegment = segmentChain.poll();

		if (activeSegment == null) {
			experimentRecord.complete();
			endExperiment();
		} else {
			experimentRecord.segmentActivated(activeSegment.getName(), activeSegment.getSampleEnvironmentName());

			startSegmentMultipartAcquisition(activeSegment.getName());

			activeSegment.activate();
		}
	}

	private void stopSegmentMultipartAcquisition() {
		try {
			getExperimentController().stopMultipartAcquisition();
		} catch (ExperimentControllerException e) {
			logError("Error stopping previous multipart acquisition", e);
		}
	}

	private void startSegmentMultipartAcquisition(String segmentName) {
		try {
			getExperimentController().startMultipartAcquisition(segmentName);
		} catch (ExperimentControllerException e) {
			String message = String.format("Could not start multipart acquisition for segment '%s'. This may result in a flat experiment structure.", segmentName);
			logError(message, e);
		}
	}

	private void endExperiment() {
		String summary = experimentRecord.summary();
		logger.info("End of experiment'{}'", getName());
		logger.info(summary);

		printBanner("Plan '" + getName() + "' execution complete");

		if (getExperimentController().isExperimentInProgress()) {
			try {
				getExperimentController().stopExperiment();
			} catch (ExperimentControllerException e) {
				logError("Error stopping experiment", e);
			}
		}
	}

	private void logError(String msg, Exception exception) {
		logger.error(msg, exception);
		experimentRecord.failed(msg);
		printBanner(msg);
	}

	private void printBanner(String msg) {
		String horizontal = Collections.nCopies(msg.length() + 4, "#").stream().collect(Collectors.joining());
		String banner = new StringBuilder("\n")
							.append(horizontal)
							.append('\n')
							.append("# ")
							.append(msg)
							.append(" #\n")
							.append(horizontal).toString();

		InterfaceProvider.getTerminalPrinter().print(banner);
	}

	@Override
	public boolean isRunning() {
		return segments.stream().anyMatch(ISegment::isActivated);
	}

	@Override
	public void triggerOccurred(ITrigger trigger) {
		experimentRecord.triggerOccurred(trigger.getName());
	}

	@Override
	public void triggerComplete(ITrigger trigger, TriggerEvent event, String sampleEnvironmentName) {
		experimentRecord.triggerComplete(trigger.getName(), event, sampleEnvironmentName);
	}

	@Override
	public void segmentActivated(ISegment segment, String sampleEnvironmentName) {
		experimentRecord.segmentActivated(segment.getName(), sampleEnvironmentName);
	}

	@Override
	public void segmentComplete(ISegment completedSegment, double terminatingSignal) {
		experimentRecord.segmentComplete(completedSegment.getName(), terminatingSignal);
		activateNextSegment();
	}

	@Override
	public void setFactory(IPlanFactory factory) {
		this.factory = factory;
		factory.setRegistrar(this);
	}

	@Override
	public void setRegistrar(IPlanRegistrar registrar) {
		throw new IllegalStateException("For internal use only");
	}

	@Override
	public void setDriver(IExperimentDriver<?> experimentDriver) {
		this.experimentDriver = Optional.of(experimentDriver);
	}

	@Override
	public ISampleEnvironmentVariable addSEV(Scannable scannable) {
		ISampleEnvironmentVariable sev = factory.addSEV(scannable);
		lastDefinedSev = sev;
		return sev;
	}

	@Override
	public ISampleEnvironmentVariable addSEV(DoubleSupplier signalSource) {
		ISampleEnvironmentVariable sev = factory.addSEV(signalSource);
		lastDefinedSev = sev;
		return sev;
	}

	@Override
	public ISampleEnvironmentVariable addTimer() {
		return factory.addTimer();
	}

	@Override
	public ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers) {
		ISegment segment = factory.addSegment(name, sev, limit, triggers);
		segments.add(segment);
		return segment;
	}

	@Override
	public ISegment addSegment(String name, ISampleEnvironmentVariable sev, double duration, ITrigger... triggers) {
		ISegment segment = factory.addSegment(name, sev, duration, triggers);
		segments.add(segment);
		return segment;
	}

	private ISampleEnvironmentVariable lastDefinedSEV() {
		if (lastDefinedSev == null) throw new IllegalArgumentException("No SEVs defined!");
		return lastDefinedSev;
	}

	@Override
	public IExperimentRecord getExperimentRecord() {
		if (isRunning()) throw new IllegalStateException("Experiment " + getName() + " is still running!");
		return experimentRecord;
	}

	@Override
	public String toString() {
		return "Plan [name=" + getName() + ", segments=" + segments + "]";
	}

	@Override
	public ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double target,
			double tolerance) {
		return factory.addTrigger(name, payload, sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double target,
			double tolerance) {
		return factory.addTrigger(name, payload, sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double interval) {
		return factory.addTrigger(name, payload, sev, interval);
	}

	@Override
	public ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double interval) {
		return factory.addTrigger(name, payload, sev, interval);
	}

	@Override
	public ITrigger addTrigger(String name, Payload payload, double target, double tolerance) {
		return addTrigger(name, payload, lastDefinedSEV(), target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Object payload, double target, double tolerance) {
		return addTrigger(name, payload, lastDefinedSEV(), target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Payload payload, double interval) {
		return addTrigger(name, payload, lastDefinedSEV(), interval);
	}

	@Override
	public ITrigger addTrigger(String name, Object payload, double interval) {
		return addTrigger(name, payload, lastDefinedSEV(), interval);
	}

	@Override
	public ISegment addSegment(String name, LimitCondition limit, ITrigger... triggers) {
		return addSegment(name, lastDefinedSEV(), limit, triggers);
	}

	@Override
	public ISegment addSegment(String name, double duration, ITrigger... triggers) {
		return addSegment(name, lastDefinedSEV(), duration, triggers);
	}

	/**
	 * Used instead of {@link #addSegment(String, double, ITrigger...)} etc.
	 *
	 * @see PlanCreator
	 */
	protected void setSegments(List<ISegment> segments) {
		this.segments.clear();
		this.segments.addAll(segments);
	}

}

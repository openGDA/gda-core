package uk.ac.diamond.daq.experiment.plan;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.DoubleSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.FindableBase;
import gda.jython.InterfaceProvider;
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
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;

public class Plan extends FindableBase implements IPlan, IPlanRegistrar, ConveniencePlanFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(Plan.class);
	
	private final List<ISegment> segments = new LinkedList<>();
	private Optional<IExperimentDriver> experimentDriver = Optional.empty();
	
	private Queue<ISegment> segmentChain;
	private ISegment activeSegment;
	private ExperimentRecord record;
	
	private IPlanFactory factory;	
	private ISampleEnvironmentVariable lastDefinedSev;
	
	private String dataDirBeforeExperiment;
	private String experimentDataDir;
	
	public Plan(String name) {
		super.setName(name);
		setFactory(new PlanFactory());
	}
	
	@Override
	public void start() {
		validatePlan();
		
		// set subdirectory
		dataDirBeforeExperiment = LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR);
		experimentDataDir = Paths.get(dataDirBeforeExperiment, validName(getName())).toString();
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, experimentDataDir);
		
		record = new ExperimentRecord(getName());
		record.start();
		
		logger.info("Plan '{}' execution started", getName());
		printBanner("Plan '" + getName() + "' execution started");
		
		if (experimentDriver.isPresent()) {
			IExperimentDriver driver = experimentDriver.get();
			record.setDriverNameAndProfile(driver.getName(), driver.getModel().getName());
		}
		
		activateNextSegment();
		
		if (experimentDriver.isPresent()) {
			experimentDriver.get().start();
		}
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
	
	private List<ITrigger> getTriggers() {
		return segments.stream().map(ISegment::getTriggers).flatMap(List::stream).distinct().collect(Collectors.toList());
	}
	
	private void activateNextSegment() {
		activeSegment = segmentChain.poll();
		if (activeSegment == null) {
			terminateExperiment();
		} else {
			record.segmentActivated(activeSegment.getName(), activeSegment.getSampleEnvironmentName());
			activeSegment.activate();
		}
	}	
	
	private void terminateExperiment() {
		record.complete();
		String summary = record.summary();
		logger.info("End of experiment'{}'", getName());
		logger.info(summary);
		
		printBanner("Plan '" + getName() + "' execution complete");
		
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, dataDirBeforeExperiment);
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
		switchToTriggerSubdirectory(activeSegment, trigger);
		record.triggerOccurred(trigger.getName());
	}
	
	@Override
	public void triggerComplete(ITrigger trigger, TriggerEvent event, String sampleEnvironmentName) {
		record.triggerComplete(trigger.getName(), event, sampleEnvironmentName);
	}
	
	@Override
	public void segmentActivated(ISegment segment, String sampleEnvironmentName) {
		record.segmentActivated(segment.getName(), sampleEnvironmentName);
	}

	@Override
	public void segmentComplete(ISegment completedSegment, double terminatingSignal) {
		record.segmentComplete(completedSegment.getName(), terminatingSignal);
		activateNextSegment();
	}
	
	private void switchToTriggerSubdirectory(ISegment segment, ITrigger trigger) {
		if (segment == null) {
			// FIXME unfortunate race condition:
			// trigger fires in final segment and segment terminates
			// before this method is called
			segment = segments.get(segments.size()-1);
			
		}
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, Paths.get(experimentDataDir, validName(segment), validName(trigger)).toString());
	}
	
	private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_]");
	
	private String validName(Findable findable) {
		return INVALID_CHARACTERS_PATTERN.matcher(findable.getName()).replaceAll("_");
	}
	
	private String validName(String name) {
		return INVALID_CHARACTERS_PATTERN.matcher(name).replaceAll("_");
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
	public void setDriver(IExperimentDriver experimentDriver) {
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
		return record;
	}

	@Override
	public String toString() {
		return "Plan [name=" + getName() + ", segments=" + segments + "]";
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double target,
			double tolerance) {
		return factory.addTrigger(name, triggerable, sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double interval) {
		return factory.addTrigger(name, triggerable, sev, interval);
	}

	@Override
	public ISegment addSegment(String name, LimitCondition limit, ITrigger... triggers) {
		return addSegment(name, lastDefinedSEV(), limit, triggers);
	}

	@Override
	public ISegment addSegment(String name, double duration, ITrigger... triggers) {
		return addSegment(name, lastDefinedSEV(), duration, triggers);
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, double target, double tolerance) {
		return addTrigger(name, triggerable, lastDefinedSEV(), target, tolerance);
	}

	@Override

	public ITrigger addTrigger(String name, Triggerable triggerable, double interval) {
		return addTrigger(name, triggerable, lastDefinedSEV(), interval);
	}
}

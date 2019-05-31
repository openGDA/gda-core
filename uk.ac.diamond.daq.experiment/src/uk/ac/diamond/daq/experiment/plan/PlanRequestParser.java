package uk.ac.diamond.daq.experiment.plan;

import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class PlanRequestParser {
	
	private final ExperimentService experimentService;
	
	private IPlan plan;
	private IExperimentDriver<DriverModel> driver;
	
	public PlanRequestParser(ExperimentService experimentService) {
		Objects.requireNonNull(experimentService);
		this.experimentService = experimentService;
	}

	public IPlan parsePlanRequest(PlanRequest planRequest) throws DeviceException {
		plan = new Plan(planRequest.getPlanName());
		
		if (planRequest.getExperimentDriverName() != null) {
			driver = Finder.getInstance().find(planRequest.getExperimentDriverName());
			driver.setModel(experimentService.getDriverProfile(driver.getName(), planRequest.getExperimentDriverProfile(), planRequest.getPlanName()));
			plan.setDriver(driver);
		}
		
		planRequest.getSegmentRequests().forEach(this::addSegment);
		
		return plan;
	}
	
	private ISegment addSegment(SegmentRequest request) {
		ITrigger[] triggers = request.getTriggerRequests().stream()
				.map(this::convertToTrigger)
				.collect(Collectors.toList()).toArray(new ITrigger[0]);
		
		switch (request.getSignalSource()) {
		case TIME:
			return plan.addSegment(request.getName(), plan.addTimer(), request.getDuration(), triggers);
		
		case POSITION:
			ISampleEnvironmentVariable sev = plan.addSEV(driver.getReadout(request.getSampleEnvironmentVariableName()));
			return plan.addSegment(request.getName(), sev, request.getInequality().getLimitCondition(request.getInequalityArgument()), triggers);

		default:
			throw new IllegalStateException("Not a recognised signal source (" + request.getSignalSource() + ")");
		}
	}
	
	private ITrigger convertToTrigger(TriggerRequest request) {
		
		ScanRequest<IROI> scanRequest = experimentService.getScan(request.getScanName(), plan.getName());
		ISampleEnvironmentVariable sev;
		
		switch (request.getSignalSource()) {
		case POSITION:
			sev = plan.addSEV(driver.getReadout(request.getSampleEnvironmentVariableName()));
			break;
			
		case TIME:
			sev = plan.addTimer();
			break;
			
		default:
			throw new IllegalStateException("Unrecognised signal source ('" + request.getSignalSource() + "')");
		}
		
		switch (request.getExecutionPolicy()) {
		case REPEATING:
			return plan.addTrigger(request.getName(), scanRequest, sev, request.getInterval());
			
		case SINGLE:
			return plan.addTrigger(request.getName(), scanRequest, sev, request.getTarget(), request.getTolerance());
			
		default:
			throw new IllegalStateException("Unrecognised execution policy ('" + request.getExecutionPolicy() + "')");
		}
	}

}

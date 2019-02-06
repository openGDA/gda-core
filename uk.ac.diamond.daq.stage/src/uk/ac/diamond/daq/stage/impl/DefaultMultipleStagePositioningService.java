package uk.ac.diamond.daq.stage.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.diamond.daq.stage.StageConfiguration;
import uk.ac.diamond.daq.stage.StageException;
import uk.ac.diamond.daq.stage.StageGroup;
import uk.ac.diamond.daq.stage.StageGroupListener;
import uk.ac.diamond.daq.stage.StageGroupService;
import uk.ac.diamond.daq.stage.StageListener;
import uk.ac.diamond.daq.stage.event.StageEvent;
import uk.ac.diamond.daq.stage.event.StageGroupEvent;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(MultipleStagePositioningService.class)
public class DefaultMultipleStagePositioningService
		implements MultipleStagePositioningService, StageGroupListener, StageListener {
	private static final Logger log = LoggerFactory.getLogger(DefaultMultipleStagePositioningService.class);

	private String name;
	private ObservableComponent observableComponent = new ObservableComponent();

	private StageGroup currentStageGroup;
	private List<StageGroup> stageGroups;

	public DefaultMultipleStagePositioningService(StageGroup stageGroup) {
		stageGroups = new ArrayList<>();
		stageGroups.add(stageGroup);
		currentStageGroup = stageGroup;
	}

	public DefaultMultipleStagePositioningService(StageGroupService stageGroupService, List<StageGroup> stageGroups)
			throws StageException {
		this.stageGroups = stageGroups;
		if (stageGroups.isEmpty()) {
			throw new StageException("No stage groups defined");
		}
		setCurrentStageGroup(stageGroupService.currentStageGroup(), null);
		stageGroupService.addListener(this);
	}

	private void setCurrentStageGroup(String stageGroupName, StageGroupEvent stageGroupEvent) throws StageException {
		StageGroup nextStageGroup = null;
		for (StageGroup stageGroup : stageGroups) {
			if (stageGroup.getName().equals(stageGroupName)) {
				nextStageGroup = stageGroup;
				break;
			}
		}
		if (nextStageGroup == null) {
			throw new StageException("Unkown stage group - " + stageGroupName);
		}
		if (currentStageGroup != null) {
			currentStageGroup.removeStageListener(this);
		}
		currentStageGroup = nextStageGroup;
		currentStageGroup.addStageListener(this);
		if (stageGroupEvent != null) {
			observableComponent.notifyIObservers(this, stageGroupEvent);
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public boolean isStageEnabled(String stageName) {
		return currentStageGroup.contains(stageName);
	}

	@Override
	public List<StageConfiguration> getCurrentPositions() throws DeviceException {
		return currentStageGroup.getCurrentPositions();
	}

	@Override
	public Object getPosition(String stageName) throws DeviceException, StageException {
		return currentStageGroup.getPosition(stageName);
	}

	@Override
	public void setPosition(String stageName, Object position) throws DeviceException, StageException {
		currentStageGroup.setPosition(stageName, position);
	}

	@Override
	public String getStageGroup() {
		return currentStageGroup.getName();
	}

	@Override
	public void stageGroupChanged(StageGroupEvent event) {
		try {
			setCurrentStageGroup(event.getStageGroupName(), event);
		} catch (StageException e) {
			log.error("Unable to update group", e);
		}
	}

	@Override
	public void stageStatusChanged(StageEvent stageEvent) {
		observableComponent.notifyIObservers(this, stageEvent);
	}
}

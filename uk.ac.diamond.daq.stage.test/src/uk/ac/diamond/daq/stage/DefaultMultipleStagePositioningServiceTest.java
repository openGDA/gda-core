package uk.ac.diamond.daq.stage;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.observable.IObserver;
import uk.ac.diamond.daq.stage.event.StageGroupEvent;
import uk.ac.diamond.daq.stage.impl.DefaultMultipleStagePositioningService;

public class DefaultMultipleStagePositioningServiceTest {
	private static final String NO_STAGE_GROUP = "NoStageGroup";
	private static final String STAGE_GROUP_A_NAME = "StageGroupA";
	private static final String STAGE_GROUP_B_NAME = "StageGroupB";

	@Mock
	private StageGroupService stageGroupService;

	private List<StageGroup> stageGroups;

	@Mock
	private StageGroup stageGroupA;

	@Mock
	private StageGroup stageGroupB;
	
	@Mock
	private IObserver observer;
	
	private DefaultMultipleStagePositioningService service;
	
	@Before
	public void setup () {
		MockitoAnnotations.initMocks(this);

		stageGroups = new ArrayList<>();
		stageGroups.add(stageGroupA);
		stageGroups.add(stageGroupB);
		
		when(stageGroupService.currentStageGroup()).thenReturn(STAGE_GROUP_A_NAME);
		
		when(stageGroupA.getName()).thenReturn(STAGE_GROUP_A_NAME);
		when(stageGroupB.getName()).thenReturn(STAGE_GROUP_B_NAME);
	}
	
	@Test (expected=StageException.class)
	public void initalise_noStageGroups () throws StageException {
		new DefaultMultipleStagePositioningService(stageGroupService, new ArrayList<StageGroup>());
	}
	
	@Test (expected=StageException.class)
	public void initalise_stageGroupNotInService () throws StageException {
		when(stageGroupService.currentStageGroup()).thenReturn(NO_STAGE_GROUP);
		
		new DefaultMultipleStagePositioningService(stageGroupService, stageGroups);
	}
	
	@Test
	public void initalise_stageGroupInService () throws StageException {
		service = new DefaultMultipleStagePositioningService(stageGroupService, stageGroups);
		
		assertSame("Current Stage Group", STAGE_GROUP_A_NAME, service.getStageGroup());
	}
	
	@Test
	public void stageGroupChanged_stageGroupNotInService () throws StageException {
		StageGroupEvent event = new StageGroupEvent(NO_STAGE_GROUP);
		
		service = new DefaultMultipleStagePositioningService(stageGroupService, stageGroups);
		assertSame("Current Stage Group Before", STAGE_GROUP_A_NAME, service.getStageGroup());
		service.stageGroupChanged(event);
		assertSame("Current Stage Group After", STAGE_GROUP_A_NAME, service.getStageGroup());

		ArgumentCaptor<Object> source = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<StageGroupEvent> arg = ArgumentCaptor.forClass(StageGroupEvent.class);
		verify(observer, times(0)).update(source.capture(), arg.capture());
	}
	
	@Test
	public void stageGroupChanged_stageGroupInService () throws StageException {
		StageGroupEvent event = new StageGroupEvent(STAGE_GROUP_B_NAME);

		service = new DefaultMultipleStagePositioningService(stageGroupService, stageGroups);
		service.addIObserver(observer);
		
		assertSame("Current Stage Group Before", STAGE_GROUP_A_NAME, service.getStageGroup());
		service.stageGroupChanged(event);
		assertSame("Current Stage Group After", STAGE_GROUP_B_NAME, service.getStageGroup());
		
		ArgumentCaptor<Object> source = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<StageGroupEvent> arg = ArgumentCaptor.forClass(StageGroupEvent.class);
		verify(observer, times(1)).update(source.capture(), arg.capture());
		assertSame("Listeners updated", STAGE_GROUP_B_NAME, arg.getValue().getStageGroupName());
	}
}

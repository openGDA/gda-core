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

import uk.ac.diamond.daq.stage.event.StageGroupEvent;
import uk.ac.diamond.daq.stage.impl.DefaultStageGroupService;

public class DefaultStageGroupServiceTest {
	private static final String NO_STAGE_GROUP = "NoStageGroup";
	private static final String STAGE_GROUP_A_NAME = "StageGroupA";
	private static final String STAGE_GROUP_B_NAME = "StageGroupB";

	private List<StageGroup> stageGroups;

	@Mock
	private StageGroup stageGroupA;

	@Mock
	private StageGroup stageGroupB;
	
	@Mock 
	private StageGroupListener listener;
	
	private DefaultStageGroupService service;
	
	@Before
	public void setup () {
		MockitoAnnotations.initMocks(this);

		stageGroups = new ArrayList<>();
		stageGroups.add(stageGroupA);
		stageGroups.add(stageGroupB);
		
		when(stageGroupA.getName()).thenReturn(STAGE_GROUP_A_NAME);
		when(stageGroupB.getName()).thenReturn(STAGE_GROUP_B_NAME);
	}
	
	@Test (expected=StageException.class)
	public void initalise_noStageGroups () throws StageException {
		new DefaultStageGroupService(new ArrayList<StageGroup>());
	}
	
	@Test
	public void initalise_stageGroupInService () throws StageException {
		service = new DefaultStageGroupService(stageGroups);
		service.addListener(listener);
		
		assertSame("Current Stage Group", STAGE_GROUP_A_NAME, service.currentStageGroup());
	}
	
	@Test (expected=StageException.class)
	public void stageGroupChanged_stageGroupNotInService () throws StageException {		
		service = new DefaultStageGroupService(stageGroups);
		service.addListener(listener);
		
		assertSame("Current Stage Group Before", STAGE_GROUP_A_NAME, service.currentStageGroup());
		service.changeStageGroup(NO_STAGE_GROUP);
		assertSame("Current Stage Group After", STAGE_GROUP_A_NAME, service.currentStageGroup());
		ArgumentCaptor<StageGroupEvent> event = ArgumentCaptor.forClass(StageGroupEvent.class);
		verify(listener, times(0)).stageGroupChanged(event.capture());
	}
	
	@Test
	public void stageGroupChanged_stageGroupInService () throws StageException {
		service = new DefaultStageGroupService(stageGroups);
		service.addListener(listener);
		
		assertSame("Current Stage Group Before", STAGE_GROUP_A_NAME, service.currentStageGroup());
		service.changeStageGroup(STAGE_GROUP_B_NAME);
		assertSame("Current Stage Group After", STAGE_GROUP_B_NAME, service.currentStageGroup());
		
		ArgumentCaptor<StageGroupEvent> event = ArgumentCaptor.forClass(StageGroupEvent.class);
		verify(listener, times(1)).stageGroupChanged(event.capture());
		assertSame("Listeners updated", STAGE_GROUP_B_NAME, event.getValue().getStageGroupName());
	}
}

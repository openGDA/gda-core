package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.diamond.daq.stage.Stage;
import uk.ac.diamond.daq.stage.event.StageGroupEvent;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.rcp.views.NudgePositionerComposite;

public class PositionValueControlComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(PositionValueControlComposite.class);

	private class MultipleStagePositioningServiceListerner implements IObserver {
		@Override
		public void update(Object source, Object arg) {
			Display.getDefault().asyncExec(() -> {
				if (arg instanceof StageGroupEvent) {
					String newStageGroupName = ((StageGroupEvent) arg).getStageGroupName();
					if (newStageGroupName.equals(stageGroupName)) {
						log.debug("New Stage Group ({}) same as the old Stage Group", stageGroupName);
						return;
					}
					log.debug("Moving from Stage Group ({}) to Stage Group ({})", stageGroupName, newStageGroupName);
					for (NudgePositionerComposite positioner : positioners) {
						positioner.dispose();
					}
					positioners.clear();
					log.debug("Rebuilding Sample Positions Axis GUI components");
					rebuildAxises();
				}
			});
		}
	}

	private MultipleStagePositioningService multipleStagePositioningService;
	private MultipleStagePositioningServiceListerner listerner;
	private Composite positionComposite;
	private String stageGroupName;
	
	private List<NudgePositionerComposite> positioners;

	public PositionValueControlComposite(Composite parent, 
			MultipleStagePositioningService multipleStagePositioningService, int style) {
		super(parent, style);

		listerner = new MultipleStagePositioningServiceListerner();
		this.multipleStagePositioningService = multipleStagePositioningService;
		multipleStagePositioningService.addIObserver(listerner);
		
		positioners = new ArrayList<>();

		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);

		positionComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(positionComposite);
		rebuildAxises();

		GridDataFactory.fillDefaults().grab(true, true).applyTo(positionComposite);

		addListener(SWT.Dispose, e -> multipleStagePositioningService.deleteIObserver(listerner));
	}

	private void rebuildAxises() {
		try {
			stageGroupName = multipleStagePositioningService.getStageGroup();
			List<Stage> stages = multipleStagePositioningService.getCurrentStages();
			for (Stage stage : stages) {
				NudgePositionerComposite positioner = new NudgePositionerComposite(positionComposite, SWT.HORIZONTAL);
				positioner.setScannable(Finder.getInstance().find(stage.getScannableName()));
				positioner.setDisplayName(stage.getName());
				positioners.add(positioner);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(positioner);
			}
			positionComposite.layout(true, true);
		} catch (DeviceException e) {
			log.error("Failed to get Axises", e);
		}
	}
}

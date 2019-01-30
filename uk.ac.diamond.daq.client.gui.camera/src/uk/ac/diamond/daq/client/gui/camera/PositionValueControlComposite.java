package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.diamond.daq.stage.StageConfiguration;
import uk.ac.diamond.daq.stage.StageException;
import uk.ac.diamond.daq.stage.event.StageEvent;
import uk.ac.diamond.daq.stage.event.StageGroupEvent;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.observable.IObserver;

public class PositionValueControlComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(PositionValueControlComposite.class);

	private class PositionValueComponentGroup {
		private String axisName;
		private double lastValidValue;
		private Label label;
		private Button minusButton;
		private Text text;
		private Button plusButton;

		private PositionValueComponentGroup(String axisName, Object position, Object incrementValue) {
			this.axisName = axisName;

			label = new Label(positionComposite, SWT.NONE);
			label.setText(axisName + ":");
			GridDataFactory.fillDefaults().applyTo(label);

			minusButton = new Button(positionComposite, SWT.ARROW | SWT.LEFT);
			minusButton.addListener(SWT.Selection, event -> {
				lastValidValue -= (Double)incrementValue;
				setPosition(lastValidValue, true);
			});
			GridDataFactory.fillDefaults().applyTo(minusButton);

			text = new Text(positionComposite, SWT.RIGHT);
			text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			text.addListener(SWT.FocusOut, event -> {
				try {
					lastValidValue = Double.parseDouble(text.getText());
					setPosition(lastValidValue, true);
				} catch (NumberFormatException e) {
					text.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				}
			});
			GridDataFactory.fillDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(text);

			plusButton = new Button(positionComposite, SWT.ARROW | SWT.RIGHT);
			plusButton.addListener(SWT.Selection, event -> {
				lastValidValue += (Double)incrementValue;
				setPosition(lastValidValue, true);
			});
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(plusButton);

			if (position instanceof Double) {
				setPosition((Double) position, false);
			} else {
				text.setText(position.toString());
				text.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				setEnabled(false);
			}
		}

		private void setPosition(double position, boolean updateMotor) {
			text.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			text.setText(String.format("%.2f", position));

			if (updateMotor) {
				try {
					multipleStagePositioningService.setPosition(axisName, position);
				} catch (DeviceException | StageException e) {
					text.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					log.error("Error moving axis " + axisName, e);
				}
			} else {
				lastValidValue = position;
			}
		}

		private void dispose() {
			label.dispose();
			minusButton.dispose();
			text.dispose();
			plusButton.dispose();
		}

		private void setEnabled(boolean enabled) {
			label.setEnabled(enabled);
			minusButton.setEnabled(enabled);
			text.setEnabled(enabled);
			plusButton.setEnabled(enabled);
		}
	}

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
					for (PositionValueComponentGroup axis : positions) {
						axis.dispose();
					}
					positions.clear();
					log.debug("Rebuilding Sample Positions Axis GUI components");
					rebuildAxises();
				} else if (arg instanceof StageEvent) {
					StageEvent event = (StageEvent) arg;
					for (PositionValueComponentGroup axis : positions) {
						if (axis.axisName.equals(event.getStageName())) {
							axis.setPosition(event.getPosition(), false);
							axis.setEnabled(!event.isMoving());
						}
					}
				}
			});
		}
	}

	private MultipleStagePositioningService multipleStagePositioningService;
	private MultipleStagePositioningServiceListerner listerner;
	private List<PositionValueComponentGroup> positions = new ArrayList<>();
	private Composite positionComposite;
	private String stageGroupName;

	public PositionValueControlComposite(Composite parent, 
			MultipleStagePositioningService multipleStagePositioningService, int style) {
		super(parent, style);

		listerner = new MultipleStagePositioningServiceListerner();
		this.multipleStagePositioningService = multipleStagePositioningService;
		multipleStagePositioningService.addIObserver(listerner);

		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);

		positionComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(positionComposite);
		rebuildAxises();

		GridDataFactory.fillDefaults().grab(true, true).applyTo(positionComposite);

		addListener(SWT.Dispose, e -> multipleStagePositioningService.deleteIObserver(listerner));
	}

	private void rebuildAxises() {
		try {
			stageGroupName = multipleStagePositioningService.getStageGroup();
			List<StageConfiguration> axisValues = multipleStagePositioningService.getCurrentPositions();
			for (StageConfiguration stageConfiguration : axisValues) {
				positions.add(new PositionValueComponentGroup(stageConfiguration.getStageName(), 
						stageConfiguration.getInitalValue(), stageConfiguration.getIncrementAmount()));
			}
			positionComposite.layout(true, true);
		} catch (DeviceException e) {
			log.error("Failed to get Axises", e);
		}
	}
}

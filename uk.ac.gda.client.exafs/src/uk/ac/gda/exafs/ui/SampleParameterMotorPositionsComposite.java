/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.b18.SampleParameterMotorPosition;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

/**
 * Class with GUI controls to allow user to edit generic motor parameters. i.e. List of {@link SampleParameterMotorPosition}s
 * - parameters for scannables that will be moved at start of a scan.
 * Used in {@link B18SampleParametersUIEditor}
 * @since 1/2/2018.
 */
public class SampleParameterMotorPositionsComposite implements IFieldWidget {

	private static final Logger logger = LoggerFactory.getLogger(SampleParameterMotorPositionsComposite.class);

	private Composite parent;
	private List<SampleParameterMotorPosition> motorPositionsList;
	private List<ControlsForMotor> widgetsList;
	private RichBeanEditorPart parentEditor;
	private Composite mainComposite;

	public void setParentEditor(final RichBeanEditorPart parentEditor) {
		this.parentEditor = parentEditor;
	}

	public SampleParameterMotorPositionsComposite(Composite parent, List<SampleParameterMotorPosition> motorPositionsList) {
		this.parent = parent;
		this.motorPositionsList = motorPositionsList;
	}

	// Add composite with GUI controls to parent composite
	public void makeComposite() {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		mainComposite.setLayout(new GridLayout(5, false));
		widgetsList = new ArrayList<>();
		if (motorPositionsList != null) {
			for (SampleParameterMotorPosition motorPos : motorPositionsList) {
				ControlsForMotor control = new ControlsForMotor(mainComposite, motorPos);
				control.updateWidgetFromParam();
				widgetsList.add(control);
			}
			GridUtils.layoutFull(parent);
		}
	}

	public Composite getMainComposite() {
		return mainComposite;
	}

	public void updateGuiFromMotorPositions() {
		for(ControlsForMotor control : widgetsList) {
			control.updateWidgetFromParam();
		}
	}

	/**
	 * Update motor position values with latest values from GUI, return the motor position list
	 */
	@Override
	public Object getValue() {
		for(ControlsForMotor control : widgetsList) {
			control.updateParamFromWidget();
		}
		return motorPositionsList;
	}

	/**
	 * 	Set the GUI up for given set of values loaded from XML bean
	 */
	@Override
	public void setValue(Object value) {
		if (value instanceof List) {
			motorPositionsList = (List<SampleParameterMotorPosition>)value;
			mainComposite.dispose();
			makeComposite();
		}
	}

	/**
	 * Set of widgets to show and control settings for one motor :<p>
	 * <li>Labels and tooltip showing description of motor and name of underlying scannable
	 * <li>Widgets to updates the moveToPosition true/false status, demandPosition value, When these are modified,
	 * the parent {@link RichBeanEditorPart} is notified that they have changed and that the updated XML can be saved.
	 * <li>'Read position' button - to fill demandPosition box with current value from the underlying scannable.
	 */
	private class ControlsForMotor {
		private Label description;
		private Button moveToPosition;
		private Text demandPosition;
		private Button readPosition;
		private SampleParameterMotorPosition param;
		private DecimalFormat numberFormatter = new DecimalFormat("0.0#####");

		public ControlsForMotor(Composite parent, final SampleParameterMotorPosition param) {
			this.param = param;

			GridDataFactory factory = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,false);

			Label label = new Label(parent, SWT.NONE);
			label.setText("Use motor");
			moveToPosition = new Button(parent, SWT.CHECK);
			moveToPosition.setToolTipText("Select to move the motor to the specifed position at start of the scan");
			factory.applyTo(moveToPosition);

			description = new Label(parent, SWT.NONE);
			factory.applyTo(description);

			demandPosition = new Text(parent, SWT.BORDER);
			description.setText(param.getDescription());
			factory.applyTo(demandPosition);

			readPosition = new Button(parent, SWT.PUSH);
			readPosition.setText("Get current value");
			factory.applyTo(readPosition);

			readPosition.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateWithCurrentValue();
				}
			});
			updateWidgetFromParam();
			GridUtils.layoutFull(parent);

			// Add listeners to notify parent richbean editor that change has occurred
			moveToPosition.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					parentEditor.valueChangePerformed(new ValueEvent(param, "moveToPosition" ));
				}
			});
			demandPosition.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String positionFromBean = numberFormatter.format(param.getDemandPosition());
					if (!demandPosition.getText().equals(positionFromBean)) {
						logger.debug("Value for {} modified : {}", param.getScannableName(), demandPosition.getText());
						parentEditor.valueChangePerformed(new ValueEvent(param, "demandPosition" ));
					}
				}
			});
		}

		/**
		 * Update demand position textbox with current value from scannable
		 */
		private void updateWithCurrentValue() {
			Scannable scn = Finder.getInstance().find(param.getScannableName());
			if (scn==null) {
				logger.warn("No scannable called {} found on server");
				return;
			}
			try {
				Object currentPos = scn.getPosition();
				Double[] position = ScannableUtils.objectToArray(currentPos);
				if (position != null && position.length>0) {
					logger.debug("Current position for {} is {}", scn.getName(), position[0]);
					demandPosition.setText(numberFormatter.format(position[0]));
				}
			} catch (DeviceException e) {
				logger.warn("Problem getting position of scannable {}", scn.getName(), e);
			}
		}

		public void updateWidgetFromParam() {
			moveToPosition.setSelection(param.getDoMove());
			description.setText(param.getDescription());
			String scannableToolipText = "Name of scannable : "+param.getScannableName();
			description.setToolTipText(scannableToolipText);
			demandPosition.setText(numberFormatter.format(param.getDemandPosition()));
			demandPosition.setToolTipText(scannableToolipText);
		}

		// Update underlying SampleParameterMotorPosition object with demand position and moveToPosition flag values from widget
		public void updateParamFromWidget() {
			param.setDoMove(moveToPosition.getSelection());
			try {
				param.setDemandPosition(numberFormatter.parse(demandPosition.getText()).doubleValue());
			} catch (ParseException e) {
				logger.warn("Unable to convert demand position '{}' for {} to number", demandPosition.getText(), param.getDescription());
			}
		}
	}

	@Override
	public void dispose() {
		mainComposite.dispose();
	}

	@Override
	public boolean isOn() {
		return false;
	}

	@Override
	public void off() {
	}

	@Override
	public void on() {
	}

	@Override
	public boolean isActivated() {
		return false;
	}

	@Override
	public void addValueListener(ValueListener listener) {
	}

	@Override
	public void removeValueListener(ValueListener listener) {
	}

	@Override
	public void fireValueListeners() {
	}

	@Override
	public void fireBoundsUpdaters() {
	}

	@Override
	public String getFieldName() {
		return null;
	}

	@Override
	public void setFieldName(String fieldName) {
	}

	@Override
	public void setEnabled(boolean isEnabled) {
	}

}

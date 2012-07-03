/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.viewer;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.ui.internal.viewer.ScannableRotationSource;

/**
 * A concrete viewer that displays position information about an underlying rotating motor.
 * Users may use the viewer to change position of the motor, in which case this 
 * viewer displays the target position as well as the updating position.
 * <p>
 * This class is designed to be instantiated with a pre-existing{@link Scannable}
 * which supplies the position information. The viewer registers a listener to receive
 * position updates from the underlying scannable. 
 * </p>
 * This viewer provides an option to display shortcut buttons for +/- a big step and little
 * step buttons. By default these are not shown, but can be configured by calling
 * <code>configureFixedStepButtons</code>
 * <p>
 * <dl>
 * <dt><b>Optional Styles (depending on ui configuration):</b></dt>
 * <dd>SINGLE</dd>
 * </dl>
 * This class is  not intended to be subclassed outside the viewer framework.
 * </p>
 */
public class RotationViewer {
	private static final Logger logger = LoggerFactory.getLogger(RotationViewer.class);
	
	private IRotationSource motor;
	private Scannable scannable;
	
	private boolean showFixedSteps;
	private boolean singleLineLayout = false;
	private double standardStep;
	private double littleStep;
	private double bigStep;
	private String motorLabel;

	private ScaleBox nudgeSizeBox;
	
	private Button plusBigButton;
	private Button plusLittleButton;
	private Button minusBigButton;
	private Button minusLittleButton;	
	private Button posNudgeButton;
	private Button negNudgeButton;

	private MotorPositionViewer motorPositionViewer;
	
	private static final int ACCEPTED_STYLES = SWT.SINGLE;
	
	/**
	 * Creates a new rotation viewer for the given scannable.
	 * 
	 * @param scannable the scannable for this viewer
	 */
	public RotationViewer (Scannable scannable){	
		this(scannable, 10.0);
	}
	
	/**
	 * Creates a new rotation viewer for the given scannable.
	 * 
	 * @param scannable the scannable for this viewer
	 */
	public RotationViewer (Scannable scannable, double stepSize){	
		if (scannable instanceof ScannableMotionUnits) {
			this.motor = new ScannableRotationSource((ScannableMotionUnits)scannable);
		}
		this.scannable = scannable;
		this.showFixedSteps = false;
		this.standardStep = stepSize;
	}
	
	public RotationViewer(Scannable scannable, String motorLabel) {
		this(scannable);
		this.motorLabel = motorLabel;
	}
	
	/**
	 * Configure the standard stepSize for the nudge buttons. 
	 * This method should only be called if a different default is required.
	 * This method must be called before invoking <code>createControls</code>
	 * @param stepSize default stepSize
	 */
	public void configureStandardStep(double stepSize){
		this.standardStep = stepSize;		
	}
	/**
	 * Show shortcut step buttons for a fixed size small step and a 
	 * fixed size big step. This method must be called before invoking <code>createControls</code>
	 * <p>
	 * Adds four additional buttons to this viewer. Two for +ve and -ve
	 * small step and two for +ve and -ve big step.
	 * <p>
	 * @param smallStep value of small step size, ignored if enable is false
	 * @param bigStep value of big step size, ignored if enable is true
	 */
	public void configureFixedStepButtons(double smallStep, double bigStep){
		this.showFixedSteps = true;
		this.littleStep = smallStep;
		this.bigStep = bigStep;
	}
	
	/**
	 * Creates the UI elements for this viewer
	 * <p>
	 * The number of columns in the parent layout will influence the layout of this viewer.
	 * <p>
	 * @param parent
	 *            the parent composite
	 * @param style
	 *           supported styles 
	 * <UL>
	 * <LI>NONE - default style,step buttons are displayed in 2 rows </LI>
	 * <LI>SINGLE - step buttons displayed in 1 row, provided configureFixedStepButtons has not been set</LI>
	 * </UL>	 *           
	 */
	public void createControls(Composite parent, int style){	
		createWidgets(parent, checkStyle(style));
		
		if (motor == null) {
			logger.warn("rotation viewer for '" + motorLabel + "' does not have a motor");
		}
		
		final IUnitsDescriptor descriptor = (motor == null) ? null : motor.getDescriptor();
		final String unit = (descriptor == null) ? null : descriptor.getUnit();
		nudgeSizeBox.setUnit(unit);

		nudgeSizeBox.setValue(standardStep);
		nudgeSizeBox.on();
		addNudgeListeners();
		
		if (showFixedSteps){
			addFixedStepButtonsListeners();
		}
	}

	/**
	 * As createControls(Composite,int) except that the buttons appear on the same line as the textfield when
	 * singleLineLayout is set to true.
	 * 
	 * @param parent
	 * @param style
	 * @param singleLineLayout
	 */
	public void createControls(Composite parent, int style,boolean singleLineLayout) {
		this.singleLineLayout = singleLineLayout;
		createControls(parent,style);
	}

	
	private static int checkStyle(int style) {
		if ((style & ~ACCEPTED_STYLES) != 0)
			throw new IllegalArgumentException(
					"Invalid style being set on RotationViewer"); //$NON-NLS-1$
		return style;
	}
	
	/**
	 * Add the +/- button listeners
	 */
	private void addNudgeListeners() {	
		posNudgeButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(true,nudgeSizeBox.getNumericValue());
			}
			
		});	
		
		negNudgeButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(false,nudgeSizeBox.getNumericValue());
			}
			
		});
	}

	/**
	 * Add the fixed step button listeners
	 */
	private void addFixedStepButtonsListeners() {
		plusBigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(true,bigStep);
			}
		});
		
		plusLittleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(true,littleStep);
			}
		});
		
		minusBigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(false,bigStep);
			}
		});
		
		minusLittleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveMotor(false,littleStep);
			}
		});		
	}

	private void moveMotor(final boolean dir, final double step){
		final String msg = "Moving " + motor.getDescriptor().getLabelText() + " by " + step;
		final double targetVal = calculateTargetPosition(dir, step);
		motorPositionViewer.getDemandBox().setNumericValue(targetVal);
		motorPositionViewer.getDemandBox().demandBegin(targetVal);

		Job job = new Job(msg){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					((IPositionSource)motor).setPosition(targetVal);
				} catch (DeviceException e) {
					logger.error("Exception when " + msg + ":" + e.getMessage(),e);
				}
				return Status.OK_STATUS; 
			}			
		};
		job.setUser(true);
		job.schedule();		
	}

	/**
	 * Utility function to calculate the target position
	 * @param dir direction of requested move, true is +ve, false is -ve
	 * @param step amount to move by
	 * @return expected target position
	 */
	private double calculateTargetPosition(boolean dir, double step){
		double target=0.0;
		try {
			if (dir){
				target= motor.calcMovePlusRelative(step);
			} else{
				target= motor.calcMoveMinusRelative(step);
			}
		} catch (DeviceException e1) {
			logger.error("Error setting current value of demandBox", e1);
		} 
		return target;
	}
	
	/**
	 * Create widgets
	 * @param parent composite
	 * @param style 
	 */
	private void createWidgets(Composite parent, int style) {
		int numColumns = singleLineLayout ? 5 : 4;
	    final Composite rotationGroup = new Composite(parent, SWT.NONE);
		rotationGroup.setLayout(new GridLayout(numColumns, false));
		
		if (motorLabel == null)
			motorLabel = scannable.getName();
		
		motorPositionViewer = new MotorPositionViewer(rotationGroup, scannable, motorLabel);

	    final Composite stepGroup;
	    if (singleLineLayout) {
	    	stepGroup = new Composite(rotationGroup, SWT.NONE);
	    } else {
	    	stepGroup = new Composite(parent, SWT.NONE);
	    }
	    
		GridData data = new GridData();
		data.widthHint = 60;
		data.horizontalAlignment = GridData.CENTER;	
		DecimalFormat df = new DecimalFormat("###");
		
		if (showFixedSteps){
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(stepGroup);
			final Composite buttonGroup = new Composite(stepGroup, SWT.NONE);
			buttonGroup.setLayout(new GridLayout(2, false));
			buttonGroup.setLayoutData(GridDataFactory.fillDefaults().create());
			                       
			plusLittleButton = createButton(buttonGroup, "+"+df.format(littleStep), null, data);
			plusBigButton  = createButton(buttonGroup, "+"+df.format(bigStep), null, data);
			minusLittleButton  = createButton(buttonGroup, "-"+df.format(littleStep), null, data);
			minusBigButton  = createButton(buttonGroup, "-"+df.format(bigStep), null, data);

		} else {
			GridLayoutFactory.fillDefaults().applyTo(stepGroup);
		}

		Composite inOutButtonsComp = new Composite(stepGroup, SWT.NONE);
		
		if ((!showFixedSteps && ((style & SWT.SINGLE) != 0))) {
			inOutButtonsComp.setLayout(new GridLayout(3, false));
			inOutButtonsComp.setLayoutData(GridDataFactory.fillDefaults().create());
			data.widthHint = 40;
			negNudgeButton  = createButton(inOutButtonsComp, "-", null, data);
			posNudgeButton  = createButton(inOutButtonsComp, "+", null, data);
			nudgeSizeBox = new ScaleBox(inOutButtonsComp, SWT.NONE);
			GridDataFactory.fillDefaults().align(GridData.END, GridData.CENTER).applyTo(nudgeSizeBox);
		} else {
			inOutButtonsComp.setLayout(new GridLayout(2, true));
			inOutButtonsComp.setLayoutData(GridDataFactory.fillDefaults().create());
			posNudgeButton  = createButton(inOutButtonsComp, "+", null, data);
			negNudgeButton  = createButton(inOutButtonsComp, "-", null, data);
	
			Label nudgeSizeLabel = new Label(inOutButtonsComp, SWT.NONE);
			nudgeSizeLabel.setText("Size");
			nudgeSizeBox = new ScaleBox(inOutButtonsComp, SWT.NONE);
			GridDataFactory.createFrom(data).align(GridData.END, GridData.CENTER).applyTo(nudgeSizeLabel);
			GridDataFactory.createFrom(data).applyTo(nudgeSizeBox);
		}
		nudgeSizeBox.setDecimalPlaces(4);

	}
	
	/**
	 * Utility method for creating buttons
	 * @param nudgeButtons
	 * @param text button text, or null if none
	 * @param image button image, or null if no image
	 * @param datac GridData to apply (a copy of this object will be used)
	 * @return the newly created button
	 */
	protected static Button createButton(Composite nudgeButtons, String text, final Image image, GridData datac) {
		final Button button =new Button(nudgeButtons, SWT.PUSH);
		
		if (text != null) button.setText(text);
		if (image != null){
			button.setImage(image);		
			button.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					button.dispose();	
				}
			});			
		}
		GridDataFactory.createFrom(datac).applyTo(button);
		return button;
	}
	
	/**
	 * Set the number of decimal places displayed by the nudge box
	 * @param decimalPlaces
	 */
	public void setNudgeSizeBoxDecimalPlaces(int decimalPlaces) {
		nudgeSizeBox.setDecimalPlaces(decimalPlaces);
	}
	
	/**
	 * Set the number of decimal places displayed by the motorpositionviewer
	 * @param decimalPlaces
	 */
	public void setMotorPositionViewerDecimalPlaces(int decimalPlaces) {
		motorPositionViewer.setDecimalPlaces(decimalPlaces);
	}
	
	public void setEnabled(boolean enabled) {
		nudgeSizeBox.setEnabled(enabled);
		
		plusBigButton.setEnabled(enabled);
		plusLittleButton.setEnabled(enabled);
		minusBigButton.setEnabled(enabled);
		minusLittleButton.setEnabled(enabled);
		posNudgeButton.setEnabled(enabled);
		negNudgeButton.setEnabled(enabled);

		motorPositionViewer.setEnabled(enabled);
	}
}

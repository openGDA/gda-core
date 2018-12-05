package uk.ac.diamond.daq.experiment.ui.driver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.NudgePositionerComposite;

public class CalibrationPage extends WizardPage {

	private Scannable demoScannable;
	private ScheduledExecutorService executorService;
	
	private GridDataFactory stretch = GridDataFactory.fillDefaults().grab(true, false);
	
	CalibrationPage() {
		super("Calibration");
		demoScannable = Finder.getInstance().find("stagex");
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(mainComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainComposite);
		
		Label title = new Label(mainComposite, SWT.NONE);
		title.setText("Calibration");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(title);
		
		FontData[] fontData = title.getFont().getFontData();
		fontData[0].setHeight(14);
		title.setFont(new Font(Display.getDefault(), fontData[0]));
		
		createMotionControl(mainComposite);
		createReadout(mainComposite);
		
		setControl(mainComposite);
		
		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(()->Display.getDefault().asyncExec(this::refreshReadout), 100, 100, TimeUnit.MILLISECONDS);
		
	}
	
	private void createMotionControl(Composite parent) {
		Group motionControl = new Group(parent, SWT.NONE);
		motionControl.setText("Motion control");
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(motionControl);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(motionControl);
		
		new Label(motionControl, SWT.NONE).setText("Position");
		NudgePositionerComposite positioner = new NudgePositionerComposite(motionControl, SWT.NONE);
		positioner.setScannable(demoScannable);
		stretch.applyTo(positioner);
		
		// This is clearly not right, but needed because NudgePositionerComposite is naughty
		motionControl.setBackground(null);
		// A control should not modify its parent.
		
		Button toggleProtection = new Button(motionControl, SWT.CHECK);
		toggleProtection.setText("Load protection");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(toggleProtection);
		
		new Label(motionControl, SWT.NONE).setText("Max load");
		
		Text maxLoad = new Text(motionControl, SWT.BORDER);
		maxLoad.setText("0");
		maxLoad.setEnabled(false);
		stretch.applyTo(maxLoad);
		
		toggleProtection.addListener(SWT.Selection, event -> maxLoad.setEnabled(toggleProtection.getSelection()));
	}
	
	private Text loadBox, displacementBox, offsetBox;
	
	private void createReadout(Composite parent) {
		Group readout = new Group(parent, SWT.NONE);
		readout.setText("Readout");
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(readout);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(readout);
		
		new Label(readout, SWT.NONE).setText("Load");
		loadBox = new Text(readout, SWT.BORDER | SWT.READ_ONLY);
		stretch.applyTo(loadBox);
		
		new Label(readout, SWT.NONE).setText("Displacement");
		displacementBox = new Text(readout, SWT.BORDER | SWT.READ_ONLY);
		stretch.applyTo(displacementBox);
		
		Button toggleOffset = new Button(readout, SWT.CHECK);
		toggleOffset.setText("Offset");
		offsetBox = new Text(readout, SWT.BORDER);
		offsetBox.setText("0");
		offsetBox.setEnabled(false);
		stretch.applyTo(offsetBox);
		
		toggleOffset.addListener(SWT.Selection, event -> offsetBox.setEnabled(toggleOffset.getSelection()));
		
		Button zero = new Button(readout, SWT.PUSH);
		zero.setText("Zero");
		zero.addListener(SWT.Selection, event -> {
			loadCal = Double.parseDouble(loadBox.getText());
			dispCal = Double.parseDouble(displacementBox.getText());
		});
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.RIGHT, SWT.CENTER).applyTo(zero);
	}
	
	private double loadCal;
	private double dispCal;
	
	private void refreshReadout() {
		// completely arbitrary, for demo
		try {
			double position = (double) demoScannable.getPosition();
			double offset = offsetBox.isEnabled() ? Double.valueOf(offsetBox.getText()) : 0;
			
			String load = String.valueOf((position * 0.3265 + offset)-loadCal);
			String displacement = String.valueOf((position * 2.22546 + offset)-dispCal);
			
			loadBox.setText(load);
			displacementBox.setText(displacement);
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		executorService.shutdownNow();
		super.dispose();
	}

}

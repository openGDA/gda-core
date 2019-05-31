package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;

public class DriverSelectionPage extends WizardPage {

	private String driverName;
	
	private Label stateLabel;
	private Text readouts;
	private Label profile;

	public DriverSelectionPage() {
		super(DriverSelectionPage.class.getCanonicalName());
		setTitle("Experiment driver selection");
		setDescription("Select the experiment driver to configure / view");
	}

	@Override
	public void createControl(Composite parent) {
		Composite driverComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(driverComposite);
		
		new Label(driverComposite, SWT.NONE).setText("Experiment driver");
		
		ComboViewer driverCombo = new ComboViewer(driverComposite);
		STRETCH.applyTo(driverCombo.getControl());
		
		driverCombo.addSelectionChangedListener(event -> {
			driverName = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
			updateDetails();
			setPageComplete(driverName != null);
		});
		driverCombo.setContentProvider(ArrayContentProvider.getInstance());
		driverCombo.setInput(Finder.getInstance().getFindablesOfType(IExperimentDriver.class).keySet());
		
		Composite details = new Composite(driverComposite, SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(20, 20).equalWidth(true).applyTo(details);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(details);
		details.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		details.setBackgroundMode(SWT.INHERIT_FORCE);
		
		new Label(details, SWT.NONE).setText("Profile");
		profile = new Label(details, SWT.NONE);
		STRETCH.copy().span(2, 1).applyTo(profile);

		new Label(details, SWT.NONE).setText("State");
		stateLabel = new Label(details, SWT.NONE);
		STRETCH.copy().span(2, 1).applyTo(stateLabel);
		
		Label readoutsLabel = new Label(details, SWT.NONE);
		readoutsLabel.setText("Readout(s)");
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(readoutsLabel);
		readouts = new Text(details, SWT.READ_ONLY | SWT.MULTI);
		STRETCH.copy().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(readouts);
		
		setControl(driverComposite);
	}
	
	private void updateDetails() {
		final IExperimentDriver<DriverModel> driver = Finder.getInstance().find(driverName);

		stateLabel.setText(driver.getState().toString());
		
		profile.setText(driver.getModel() != null ? driver.getModel().getName() : "Not configured");
		
		readouts.setText(
			driver.getReadoutNames().stream()
				.map(Finder.getInstance()::find)
				.filter(Scannable.class::isInstance)
				.map(Scannable.class::cast)
				.map(scannable -> scannable.getName() + "\t:\t\t" + getScannablePosition(scannable))
				.collect(Collectors.joining("\n"))
		);
	}
	
	private String getScannablePosition(Scannable scannable) {
		try {
			return scannable.getPosition().toString();
		} catch (DeviceException e) {
			return "Unknown";
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return driverName != null;
	}
	
	@Override
	public IWizardPage getNextPage() {
		IWizardPage page = super.getNextPage();
		if (page instanceof ProfileManagerPage) {
			((ProfileManagerPage) page).setDriverName(driverName);
		}
		
		IWizard wizard = getWizard();
		if (wizard instanceof ExperimentDriverWizard) {
			((ExperimentDriverWizard)wizard).setDriverName(driverName);
		}
		
		return page;
	}

}

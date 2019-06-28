package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.CONFIGURE_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.RUN_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;
import uk.ac.gda.client.viewer.ThreeStateDisplay;

/**
 * This page manages profiles associated with a particular driver.
 */
public class ProfileManagerPage extends WizardPage {
	
	private static final Logger logger = LoggerFactory.getLogger(ProfileManagerPage.class);
	
	private final String experimentId;
	private final ExperimentService experimentService;
	
	private ListWithCustomEditor profilesViewer;

	private String driverName;
	
	private Button loadButton;
	private Button startButton;
	private ThreeStateDisplay colourState;
	private Label updateLabel;
	
	private Optional<DriverModel> selectedProfile = Optional.empty();
	
	public ProfileManagerPage(ExperimentService experimentService, String experimentId) {
		super(ProfileManagerPage.class.getCanonicalName());
		this.experimentService = experimentService;
		this.experimentId = experimentId;
		setTitle("Manage and load driver profiles");
		setDescription("A selected profile can be edited in the next page");
	}
	
	@Override
	public void createControl(final Composite parent) {
		
		Composite base = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(base);
		STRETCH.applyTo(base);
		
		Composite profileListComposite = new Composite(base, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(profileListComposite);
		STRETCH.applyTo(profileListComposite);
		
		profilesViewer = new ListWithCustomEditor();
		profilesViewer.setListHeight(300);
		profilesViewer.setTemplate(new SingleAxisLinearSeries().createDefault());
		profilesViewer.create(profileListComposite);
		profilesViewer.setElementEditor(new PretendElementEditor());
		profilesViewer.addDeleteHook(this::deleteProfile);
		
		Composite rightPart = new Composite(base, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(10, 10).numColumns(2).equalWidth(true).applyTo(rightPart);
		STRETCH.copy().align(SWT.FILL, SWT.TOP).applyTo(rightPart);
		
		GridDataFactory fatButtonGridData = GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 60).align(SWT.FILL, SWT.FILL).grab(true, true);
		
		loadButton = new Button(rightPart, SWT.NONE);
		loadButton.setText("Send profile to driver");
		loadButton.setImage(ExperimentUiUtils.getImage(CONFIGURE_ICON));
		loadButton.setEnabled(false);
		loadButton.addListener(SWT.Selection, e -> sendProfileToDriver());
		fatButtonGridData.applyTo(loadButton);
		
		startButton = new Button(rightPart, SWT.NONE);
		startButton.setText("Run driver profile");
		startButton.setImage(ExperimentUiUtils.getImage(RUN_ICON));
		startButton.setEnabled(false);
		startButton.addListener(SWT.Selection, e -> sendProfileAndStart());
		fatButtonGridData.applyTo(startButton);
		
		Composite statusComposite = new Composite(rightPart, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(statusComposite);
		STRETCH.copy().span(2, 1).applyTo(statusComposite);

		new Label(statusComposite, SWT.NONE).setText("Driver status:");
		colourState = new ThreeStateDisplay(statusComposite, "Ready", "Configured", "Busy");
		updateLabel = new Label(statusComposite, SWT.NONE);
		STRETCH.copy().span(2, 1).applyTo(updateLabel);
		
		Composite hintComposite = new Composite(base, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(hintComposite);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.RIGHT, SWT.BOTTOM).grab(true, true).applyTo(hintComposite);
		Label editInNextPageHint = new Label(hintComposite, SWT.NONE);
		editInNextPageHint.setText("Click 'Next' to edit/view selected profile");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.BOTTOM).applyTo(editInNextPageHint);
		
		setPageComplete(selectedProfile.isPresent());
		setControl(base);
	}
	
	private void sendProfileToDriver() {
		if (selectedProfile.isPresent()) {
			DriverModel model = selectedProfile.get();
			IExperimentDriver<DriverModel> driver = Finder.getInstance().find(driverName);
			try {
				driver.setModel(model);
				updateLabel.setText("Sent '" + model.getName() + "'");
				colourState.setYellow();
				logger.info("Sent profile '{}' to driver '{}'", model.getName(), driverName);
			} catch (DeviceException e) {
				updateLabel.setText("Error configuring driver");
				logger.error("Error sending profile '{}' to driver '{}'", model.getName(), driverName, e);
				colourState.setRed();
			}
		}
	}
	
	private void sendProfileAndStart() {
		if (selectedProfile.isPresent()) {
			String profileName = selectedProfile.get().getName();
			sendProfileToDriver();
			
			if (MessageDialog.openConfirm(getShell(), "Run profile?", "Do you want to run profile '" + profileName + "' now?")) {
				IExperimentDriver<DriverModel> driver = Finder.getInstance().find(driverName);
				colourState.setRed();
				updateLabel.setText("Running '" + profileName + "'");
				loadButton.setEnabled(false);
				startButton.setEnabled(false);
				Async.execute(()->{
					logger.info("Started profile '{}' on driver '{}'", profileName, driverName);
					driver.start(); // blocking
					if (!updateLabel.isDisposed()) { // assume no widget is disposed
						Display.getDefault().asyncExec(()->{
							colourState.setGreen();
							updateLabel.setText("Execution complete");
							loadButton.setEnabled(true);
							startButton.setEnabled(true);
						});
					}
				});
			}
		}
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
		profilesViewer.setList(getProfilesForDriver(driverName));
		profilesViewer.refresh();
		initiateColourState();
	}
	
	private void initiateColourState() {
		IExperimentDriver<DriverModel> driver = Finder.getInstance().find(driverName);
		switch (driver.getState()) {
		case IDLE:
			colourState.setGreen();
			break;
		case PAUSED:
			// run through to running
		case RUNNING:
			colourState.setRed();
			break;
		default:
			break;
		}

	}
	
	private List<EditableWithListWidget> getProfilesForDriver(String driverName) {
		return experimentService.getDriverProfileNames(driverName, experimentId).stream()
			.map(name -> getDriverModel(name, driverName))
			.collect(Collectors.toList());
	}
	
	private DriverModel getDriverModel(String modelName, String driverName) {
		this.driverName = driverName;
		return experimentService.getDriverProfile(driverName, modelName, experimentId);
	}
	
	private void deleteProfile(EditableWithListWidget bean) {
		if (MessageDialog.openConfirm(getShell(), "Delete profile", "Are you sure you want to delete profile '"+bean.getLabel()+"'?")) {
			DriverModel profile = (DriverModel) bean;
			experimentService.deleteDriverProfile(profile, driverName, experimentId);
		} else {
			List<EditableWithListWidget> list = profilesViewer.getList();
			list.add(bean);
			profilesViewer.setList(list);
		}
	}
	
	@Override
	public IWizardPage getNextPage() {
		IWizardPage page = super.getNextPage();
		if (page instanceof ProfileEditorPage && selectedProfile.isPresent()) {
			IExperimentDriver<DriverModel> driver = Finder.getInstance().find(driverName);
			((ProfileEditorPage) page).setProfile((SingleAxisLinearSeries) selectedProfile.get(), driver.getQuantityName(), driver.getQuantityUnits());
		}
		return page;
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return selectedProfile.isPresent();
	}
	
	private class PretendElementEditor implements ElementEditor {

		@Override
		public void createControl(Composite parent) {
			// No gui thanks, just want to grab the selection
		}

		@Override
		public void load(EditableWithListWidget element) {
			selectedProfile = Optional.of((DriverModel) element);
			loadButton.setEnabled(true);
			startButton.setEnabled(true);
			setPageComplete(true);
		}

		@Override
		public void clear() {
			selectedProfile = Optional.empty();
			loadButton.setEnabled(false);
			startButton.setEnabled(true);
			setPageComplete(false);
		}
	}

}

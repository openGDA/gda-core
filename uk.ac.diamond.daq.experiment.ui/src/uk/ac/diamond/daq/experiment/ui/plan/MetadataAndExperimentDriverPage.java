package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;


public class MetadataAndExperimentDriverPage extends WizardPage {
	
	private ExperimentService experimentService;
	private String experimentId;

	MetadataAndExperimentDriverPage(ExperimentService experimentService, String experimentId) {
		super(MetadataAndExperimentDriverPage.class.getSimpleName());
		setTitle("Metadata and experiment driver");
		setDescription("Add a title and description to your plan, and select experiment driver configuration if required");
		
		this.experimentService = experimentService;
		this.experimentId = experimentId;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		createMetadataSection(composite);
		
		createExperimentDriverSection(composite);
		
		setPageComplete(isPageComplete());
		
		setControl(composite);
	}
	
	private GridLayoutFactory containerLayout = GridLayoutFactory.swtDefaults().margins(20, 20);
	private GridDataFactory fill = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true);
	
	private void createMetadataSection(Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		containerLayout.applyTo(composite);
		
		composite.setText("Metadata");
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Plan name");
		
		Text planNameText = new Text(composite, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(planNameText);
		planNameText.addListener(SWT.Modify, e -> {
			planName = planNameText.getText();
			setPageComplete(isPageComplete());
		});
		
		new Label(composite, SWT.NONE); // space
		
		new Label(composite, SWT.NONE).setText("Description");
		
		Text planDescriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		planDescriptionText.addListener(SWT.Modify, e -> planDescription = planDescriptionText.getText());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 40).applyTo(planDescriptionText);
	}
	
	private Button useDriver;
	private List<Button> driverButtons;
	private Combo config;
	
	private DriverProfilePreview profilePlot;
	
	private String planName;
	private String planDescription;
	private Optional<IExperimentDriver> selectedDriver = Optional.empty();
	private String driverConfiguration;
	
	
	private void createExperimentDriverSection(Composite parent) {
		Group driverGroup = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(driverGroup);
		containerLayout.copy().numColumns(3).equalWidth(true).applyTo(driverGroup);
		driverGroup.setText("Experiment driver");
		
		Composite controls = new Composite(driverGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(controls);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(false, true).applyTo(controls);
		
		useDriver = new Button(controls, SWT.CHECK);
		useDriver.setText("Use experiment driver");
		useDriver.setSelection(!experimentDriverConfigurations.isEmpty());
		
		new Label(controls, SWT.NONE); // space
		driverButtons = new ArrayList<>();
		for (Map.Entry<IExperimentDriver,Set<String>> experimentDriver : experimentDriverConfigurations.entrySet()) {
			Button button = new Button(controls, SWT.RADIO);
			button.setText(experimentDriver.getKey().getName());
			
			button.addListener(SWT.Selection, e -> {
				selectedDriver = Optional.of(experimentDriver.getKey());
				config.setItems(experimentDriver.getValue().toArray(new String[0]));
				if (config.getItemCount() == 1) {
					config.select(0);
				}
				setPageComplete(isPageComplete());
			});
			
			driverButtons.add(button);
		}
		
		new Label(controls, SWT.NONE); // space
		
		new Label(controls, SWT.NONE).setText("Configuration");
		
		config = new Combo(controls, SWT.READ_ONLY);
		STRETCH.applyTo(config);
		
		Composite plotComposite = new Composite(driverGroup, SWT.NONE);
		fill.copy().span(2, 1).applyTo(plotComposite);
		GridLayoutFactory.swtDefaults().applyTo(plotComposite);
		
		profilePlot = new DriverProfilePreview(plotComposite);
		
		useDriver.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			boolean used = useDriver.getSelection();
			config.setEnabled(used);
			if (!used) {
				selectedDriver = Optional.empty();
				driverButtons.forEach(button -> button.setSelection(false));
				config.deselectAll();
			}
			setPageComplete(!used);
			plot(config.getText());
		}));
		
		config.addListener(SWT.Selection, e -> {
			if (config.getText() != null && !config.getText().isEmpty()) {
				driverConfiguration = config.getText();
				plot(config.getText());
			}
			setPageComplete(isPageComplete());
		});
		
	}
	
	private void plot(String profileName) {
		List<DriverProfileSection> profile;
		if (!selectedDriver.isPresent() || profileName == null || profileName.isEmpty()) {
			profile = Collections.emptyList();
		} else {
			profile = experimentService.getDriverProfile(selectedDriver.get().getName(),
												profileName, experimentId).getProfile();
		}
		profilePlot.plot(profile);
	}
	
	@Override
	public boolean isPageComplete() {
		return (planName != null && !planName.isEmpty()) && (!useDriver.getSelection() || !config.getText().isEmpty());
	}
	
	private Map<IExperimentDriver, Set<String>> experimentDriverConfigurations;
	
	public void setExperimentDriverConfigurations(Map<IExperimentDriver, Set<String>> driverConfigs) {
		experimentDriverConfigurations = driverConfigs;
	}
	
	@Override
	public IWizardPage getNextPage() {
		SegmentsAndTriggersPage nextPage = (SegmentsAndTriggersPage) super.getNextPage();
		if (selectedDriver.isPresent()) {
			nextPage.setSevs(selectedDriver.get().getReadoutNames());
			nextPage.plotProfile(experimentService.getDriverProfile(selectedDriver.get().getName(),
					config.getText(), experimentId).getProfile());
		} else {
			nextPage.plotProfile(Collections.emptyList());
		}
		return nextPage;
	}

	public String getPlanName() {
		return planName;
	}
	
	public String getPlanDescription() {
		return planDescription;
	}
	
	public String getExperimentDriverName() {
		if (selectedDriver.isPresent()) {
			return selectedDriver.get().getName();
		} else {
			return null;
		}
	}

	public String getExperimentDriverProfile() {
		return driverConfiguration;
	}

}

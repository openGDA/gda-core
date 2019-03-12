package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
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
	private Combo config;
	
	IPlottingSystem<Composite> plottingSystem;
	
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
		List<Button> driverButtons = new ArrayList<>();
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
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComposite, "Preview", null, PlotType.XY, null);
			fill.applyTo(plottingSystem.getPlotComposite());
		} catch (Exception e) {
			new Label(plotComposite, SWT.NONE).setText("Preview cannot be displayed");
		}
		
		useDriver.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			boolean used = useDriver.getSelection();
			config.setEnabled(used);
			driverButtons.forEach(button -> button.setEnabled(used));
			if (!used) selectedDriver = Optional.empty();
			setPageComplete(!used);
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
		if (!selectedDriver.isPresent()) {
			// we should not have ended up in this method
			return;
		}
		plottingSystem.clear();
		List<DriverProfileSection> sections = experimentService.getDriverProfile(selectedDriver.get().getName(),
																					profileName, experimentId).getProfile();
		// create dataset from model
		if (sections.isEmpty()) return;
		
		double[] x = new double[sections.size()+1];
		double[] y = new double[sections.size()+1];
		
		x[0] = 0;
		y[0] = sections.get(0).getStart();
		
		for (int i = 0; i < sections.size(); i++) {
			x[i+1] = sections.get(i).getDuration() + x[i];
			y[i+1] = sections.get(i).getStop();
		}
		
		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		
		xDataset.setName("Time (min)");
		
		plottingSystem.createPlot1D(xDataset, Arrays.asList(yDataset), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
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

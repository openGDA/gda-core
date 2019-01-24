package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
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

import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;


public class MetadataAndExperimentDriverPage extends WizardPage {

	MetadataAndExperimentDriverPage() {
		super(MetadataAndExperimentDriverPage.class.getSimpleName());
		setTitle("Metadata and experiment driver");
		setDescription("Add a title and description to your plan, and select experiment driver configuration if required");
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
	
	private Text planName;
	
	private void createMetadataSection(Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		containerLayout.applyTo(composite);
		
		composite.setText("Metadata");
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText("Plan name");
		
		planName = new Text(composite, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(planName);
		planName.addListener(SWT.Modify, e -> setPageComplete(isPageComplete()));
		
		new Label(composite, SWT.NONE); // space
		
		new Label(composite, SWT.NONE).setText("Description");
		
		Text planDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 40).applyTo(planDescription);
	}
	
	private Button useDriver;
	private Combo config;
	private IExperimentDriver selectedDriver;
	
	
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
		useDriver.setSelection(true);
		
		new Label(controls, SWT.NONE); // space
		List<Button> driverButtons = new ArrayList<>();
		for (Map.Entry<IExperimentDriver, List<String>> experimentDriver : experimentDriverConfigurations.entrySet()) {
			Button button = new Button(controls, SWT.RADIO);
			button.setText(experimentDriver.getKey().getName());
			
			button.addListener(SWT.Selection, e -> {
				selectedDriver = experimentDriver.getKey();
				config.setItems(experimentDriver.getValue().toArray(new String[0]));
				setPageComplete(isPageComplete());
			});
			
			driverButtons.add(button);
		}
		
		new Label(controls, SWT.NONE); // space
		
		new Label(controls, SWT.NONE).setText("Configuration");
		
		config = new Combo(controls, SWT.READ_ONLY);
		STRETCH.applyTo(config);
		
		config.addListener(SWT.Selection, e -> setPageComplete(isPageComplete()));

		
		Composite plotComposite = new Composite(driverGroup, SWT.NONE);
		fill.copy().span(2, 1).applyTo(plotComposite);
		GridLayoutFactory.swtDefaults().applyTo(plotComposite);
		
		IPlottingSystem<Composite> plottingSystem;
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
			setPageComplete(!used);
		}));
	}
	
	@Override
	public boolean isPageComplete() {
		return (planName.getText() != null && !planName.getText().isEmpty()) && (!useDriver.getSelection() || !config.getText().isEmpty());
	}
	
	private Map<IExperimentDriver, List<String>> experimentDriverConfigurations;
	
	public void setExperimentDriverConfigurations(Map<IExperimentDriver, List<String>> configs) {
		experimentDriverConfigurations = configs;
	}
	
	@Override
	public IWizardPage getNextPage() {
		SegmentsAndTriggersPage nextPage = (SegmentsAndTriggersPage) super.getNextPage();
		if (useDriver.getSelection()) nextPage.setSevs(selectedDriver.getReadouts());
		return nextPage;
	}

}

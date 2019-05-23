package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class ProfileEditorPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(ProfileEditorPage.class);
	private SingleAxisLinearSeries model;
	
	private Text profileNameText;
	private Button saveButton;
	private final Predicate<DriverModel> profileSaver;
	private boolean profileChanged;
	
	protected ProfileEditorPage(Predicate<DriverModel> profileSaver) {
		super(ProfileEditorPage.class.getCanonicalName());
		setTitle("Editing driver profile");
		setDescription("Don't forget to save your changes!");
		this.profileSaver = profileSaver;
	}

	private ListWithCustomEditor listEditor = new ListWithCustomEditor();
	private DriverProfileSectionEditor elementEditor = new DriverProfileSectionEditor();
	
	private PropertyChangeListener modelChanged = event -> updatePlot();
	
	private IPlottingSystem<Composite> plottingSystem;
	private String quantityName;
	private String quantityUnits;
	
	@Override
	public void createControl(Composite parent) {
		
		Composite base = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(base);
		STRETCH.applyTo(base);
		
		Composite topBit = new Composite(base, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(10, 10).numColumns(10).equalWidth(true).applyTo(topBit);
		STRETCH.copy().span(2, 1).applyTo(topBit);
		
		Composite nameComposite = new Composite(topBit, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(nameComposite);
		STRETCH.copy().span(8, 1).applyTo(nameComposite);
		
		new Label(nameComposite, SWT.NONE).setText("Profile name");
		profileNameText = new Text(nameComposite, SWT.BORDER);
		profileNameText.addListener(SWT.Modify, event -> updateName());
		STRETCH.applyTo(profileNameText);
		
		saveButton = new Button(topBit, SWT.BORDER);
		saveButton.setText("Save profile");
		GridDataFactory.swtDefaults().span(2, 2).grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(saveButton);
		saveButton.addListener(SWT.Selection, event -> {
			boolean saved = profileSaver.test(model);
			saveButton.setEnabled(!saved);
			setPageComplete(saved);
			profileChanged |= saved;
		});
		saveButton.setEnabled(false);
		
		// a separator
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				new Label(base, SWT.SEPARATOR | SWT.HORIZONTAL));
		
		Composite listEditorComposite = new Composite(base, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(false, true).applyTo(listEditorComposite);
		GridLayoutFactory.swtDefaults().applyTo(listEditorComposite);
		
		listEditorComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		listEditorComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		new Label(listEditorComposite, SWT.NONE).setText("Sections");
		
		listEditor.setMinimumElements(1);
		listEditor.setListHeight(150);
		listEditor.setTemplate(new DriverProfileSection());

		listEditor.setElementEditor(elementEditor);
		listEditor.addListListener(event -> listChanged());
		listEditor.create(listEditorComposite);
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create plot", e);
			return;
		}
		plottingSystem.createPlotPart(base, "profile", null, PlotType.XY, null);
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		listEditor.addListListener(modelChanged);
		updatePlot();
		
		setControl(base);
	}
	
	private void listChanged() {
		if (model != null) {
			model.setProfile(listEditor.getList().stream().map(DriverProfileSection.class::cast).collect(Collectors.toList()));
			saveButton.setEnabled(true);
			setPageComplete(false);
			updatePlot();
		}
	}
	
	void updatePlot() {
		plottingSystem.clear();
		
		// create dataset from model
		List<DriverProfileSection> segments = listEditor.getList().stream().map(DriverProfileSection.class::cast).collect(Collectors.toList());
		if (segments.isEmpty()) return;
		
		double[] x = new double[segments.size()+1];
		double[] y = new double[segments.size()+1];
		
		x[0] = 0;
		y[0] = segments.get(0).getStart();
		
		for (int i = 0; i < segments.size(); i++) {
			x[i+1] = segments.get(i).getDuration() + x[i];
			y[i+1] = segments.get(i).getStop();
		}
		
		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		
		xDataset.setName("Time (min)");
		yDataset.setName(getQuantityName() + " (" + getQuantityUnits() + ")");
		
		plottingSystem.createPlot1D(xDataset, Arrays.asList(yDataset), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
	}
	
	private void updateName() {
		if (model != null) {
			model.setName(profileNameText.getText());
			saveButton.setEnabled(true);
		}
	}
	
	private String getQuantityName() {
		return quantityName != null ? quantityName : "Unknown";
	}
	private String getQuantityUnits() {
		return quantityUnits != null ? quantityUnits : "a.u.";
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		if (profileChanged && getWizard() instanceof ExperimentDriverWizard) {
			((ExperimentDriverWizard) getWizard()).shouldReloadData();
		}
		return super.getPreviousPage();
	}
	
	void setProfile(SingleAxisLinearSeries profile, String quantityName, String quantityUnits) {
		this.model = profile;
		profileNameText.setText(profile.getName());
		listEditor.setList(profile.getProfile().stream().map(EditableWithListWidget.class::cast).collect(Collectors.toList()));
		listEditor.refresh();
		saveButton.setEnabled(false);
		profileChanged = false;
		this.quantityName = quantityName;
		this.quantityUnits = quantityUnits;
		elementEditor.setUnits(quantityUnits);
		updatePlot();
	}
	
}

package uk.ac.diamond.daq.experiment.ui.plan;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.CONFIGURE_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.MINUS_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.PLUS_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.RUN_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.getImage;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanException;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;

public class PlanManagerView extends ViewPart {
	
	private static final String PLAN_SELECT_TOOLTIP = "Click on a plan below to select it";
	
	private PlanRequestHandler handler = Finder.getInstance().findSingleton(PlanRequestHandler.class);
	private ExperimentService experimentService = Finder.getInstance().findSingleton(ExperimentService.class);
	
	private Composite base;
	private Text selectedPlan;
	private Button runButton;
	private TreeViewer viewer;
	private Button edit;
	private Button remove;
	
	@Override
	public void createPartControl(Composite parent) {
		
		base = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(base);
		STRETCH.applyTo(base);
		
		buildTopSection(base);
		buildMiddleSection(base);
		buildBottomSection(base);
		
		update();
	}
	
	/**
	 * Shows currently selected plan, with a button to start it.
	 */
	private void buildTopSection(Composite base) {
		Composite section = new Composite(base, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(section);
		STRETCH.applyTo(section);
		
		Label planLabel = new Label(section, SWT.NONE);
		planLabel.setText("Selected plan:");
		planLabel.setToolTipText(PLAN_SELECT_TOOLTIP);
		
		selectedPlan = new Text(section, SWT.BORDER | SWT.READ_ONLY);
		selectedPlan.setToolTipText(PLAN_SELECT_TOOLTIP);
		
		STRETCH.applyTo(selectedPlan);
		
		runButton = new Button(section, SWT.NONE);
		runButton.setText("Start plan");
		runButton.setImage(getImage(RUN_ICON));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.FILL).grab(false, true).applyTo(runButton);
		
		runButton.addSelectionListener(widgetSelectedAdapter(event -> run()));
	}
	
	/**
	 * A list of available plans
	 */
	private void buildMiddleSection(Composite composite) {
		Composite section = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(section);
		
		GridDataFactory fillSpace = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true);
		
		fillSpace.applyTo(section);
		
		PatternFilter filter = new PatternFilter();
		FilteredTree tree = new FilteredTree(section, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE, filter, true);
		fillSpace.applyTo(tree);
		
		viewer = tree.getViewer();
		viewer.setContentProvider(new FlatArrayContentProvider());
		viewer.addSelectionChangedListener(event -> {
			StructuredSelection selection = (StructuredSelection) event.getSelection();
			if (selection.getFirstElement() != null) {
				selectedPlan.setText((String) selection.getFirstElement());
			}
			updateButtons();
		});
	}
	
	/**
	 * Buttons to add, edit, and remove plans
	 */
	private void buildBottomSection(Composite composite) {
		Composite section = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(section);
		STRETCH.applyTo(section);
		
		Button add = new Button(section, SWT.PUSH);
		add.setText("New");
		add.setImage(getImage(PLUS_ICON));
		add.addSelectionListener(widgetSelectedAdapter(event -> add()));
		STRETCH.applyTo(add);
		
		edit = new Button(section, SWT.PUSH);
		edit.setText("Edit");
		edit.setImage(getImage(CONFIGURE_ICON));
		edit.addSelectionListener(widgetSelectedAdapter(event -> edit(selectedPlan.getText())));
		STRETCH.applyTo(edit);
		
		remove = new Button(section, SWT.PUSH);
		remove.setText("Remove");
		remove.setImage(getImage(MINUS_ICON));
		remove.addSelectionListener(widgetSelectedAdapter(event -> remove(selectedPlan.getText())));
		STRETCH.applyTo(remove);
	}
	
	private void run() {
		if (selectedPlan.getText() == null || selectedPlan.getText().isEmpty()) {
			throw new ExperimentPlanException("There is no plan selected. The UI should prevent this method from being called!");
		}
		ExperimentPlanBean plan = experimentService.getExperimentPlan(selectedPlan.getText());
		try {
			handler.submit(plan);
		} catch (DeviceException e) {
			throw new ExperimentPlanException("Error executing experiment plan '" + plan.getPlanName() + "'", e);
		}
	}
	
	private void add() {
		ExperimentPlanBean bean = new ExperimentPlanBean();
		if (openWizard(bean)) {
			experimentService.saveExperimentPlan(bean);
			update();
			select(bean.getPlanName());
		}
	}
	
	private void edit(String planName) {
		ExperimentPlanBean bean = experimentService.getExperimentPlan(planName);
		final String originalName = bean.getPlanName(); // the edit could change this
		if (openWizard(bean)) {
			if (!bean.getPlanName().equals(originalName)) {
				experimentService.deleteExperimentPlan(originalName);
			}
			experimentService.saveExperimentPlan(bean);
			update();
			select(bean.getPlanName());
		}
	}
	
	private void remove(String planName) {
		if (MessageDialog.openConfirm(base.getShell(), "Delete experiment plan",
				"Do you want to delete experiment plan '" + planName + "'?")) {
			experimentService.deleteExperimentPlan(planName);
			selectedPlan.setText("");
			update();
		}
	}
	
	private boolean openWizard(ExperimentPlanBean planBean) {
		PlanSetupWizard planWizard = new PlanSetupWizard(experimentService, "", planBean); // TODO should be the experimentId
		WizardDialog wizardDialog = new WizardDialog(base.getShell(), planWizard);
		return wizardDialog.open() == Window.OK;
	}
	
	private void select(String planName) {
		viewer.setSelection(new StructuredSelection(planName));
	}
	
	/**
	 * refresh viewer input, and update enabled state of buttons
	 */
	private void update() {
		updateInput();
		updateButtons();
	}
	
	private void updateInput() {
		viewer.setInput(experimentService.getExperimentPlanNames());
	}
	
	private void updateButtons() {
		// run, edit, and remove buttons enabled only when a plan is selected
		final boolean planSelected = selectedPlan.getText() != null && !selectedPlan.getText().isEmpty();
		runButton.setEnabled(planSelected);
		edit.setEnabled(planSelected);
		remove.setEnabled(planSelected);
	}

	@Override
	public void setFocus() {
		base.setFocus();
	}
	
	
	/**
	 * We are only pretending to be a tree so that we can filter.
	 * Delegates to {@link ArrayContentProvider}.
	 */
	private class FlatArrayContentProvider implements ITreeContentProvider {

		private final IStructuredContentProvider provider = ArrayContentProvider.getInstance();
		
		@Override
		public Object[] getElements(Object inputElement) {
			return provider.getElements(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}
		
	}
}

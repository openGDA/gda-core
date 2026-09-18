package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularCutsHelper;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularImageCutsComposite;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * Window for a plot view containing two additional plot views showing
 * perpendicular cuts of the image from the main view.
 * <p>
 * Also has the option to incrementally sum the incoming data.
 */
public class ArpesLivePerpCutsViewE4 extends BaseLivePlotViewE4{
	private static final Logger logger = LoggerFactory.getLogger(ArpesLivePerpCutsViewE4.class);

	private final ReadWriteLock sumLock = new ReentrantReadWriteLock();
	private volatile boolean isSum = false;

	private ImageWithAxes sum; // Guarded by sumLock
	private LiveDataPlotUpdate lastDataUpdate;


	@Inject
	public ArpesLivePerpCutsViewE4(IEclipseContext context) {
		this.context = context;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		try {
			validateDependencies();
			createUIComponents(parent);
		} catch (Exception e) {
			logger.error("Failed to create composite", e);
			throw new RuntimeException("Failed to initialize view", e);
		}
	}

	private void createUIComponents(Composite parent) throws Exception {
		SashForm inner = new SashForm(parent, SWT.HORIZONTAL);
		inner.setLayout(new GridLayout(2, true));
		Composite leftComposite = new Composite(inner, SWT.NONE);
		leftComposite.setLayout(new GridLayout());
		// Create button panel
		createButtonPanel(leftComposite);
		// Create plotting system
		createPlottingSystem(leftComposite);
		// Add some DAWN tools
		configureToolbar(parent);
		// Create perpendicular cuts composite
		PerpendicularImageCutsComposite cutsComposite = createCutsComposite(inner);
		createPerpendicularCutsComposite(cutsComposite);
		addTransposeAction(parent);
	}

	private void createButtonPanel(Composite parent) {
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		buttonPanel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		buttonPanel.setLayout(new GridLayout(3, false));
		Button liveButton = new Button(buttonPanel, SWT.RADIO);
		liveButton.setText("Live");
		liveButton.setSelection(!isSum);
		liveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isSum = !liveButton.getSelection();
				clearSum();
			}
		});
		Button sumButton = new Button(buttonPanel, SWT.RADIO);
		sumButton.setText("Sum");
		sumButton.setSelection(isSum);
		Button clearButton = new Button(buttonPanel, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearSum();
			}
		});
	}

	private PerpendicularImageCutsComposite createCutsComposite(Composite parent) throws Exception {
		IPlottingService plottingService = context.get(IPlottingService.class);
		if (plottingService == null) {
			throw new IllegalStateException("IPlottingService not available in context");
		}
		PerpendicularImageCutsComposite composite = new PerpendicularImageCutsComposite(
			parent, SWT.NONE, plottingService);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
		return composite;
	}

	@Override
	protected void createPlottingSystem(Composite parent) throws Exception {
		super.createPlottingSystem(parent);
		plottingSystem.getPlotComposite().setLayoutData(
			GridDataFactory.fillDefaults().grab(true, true).create());
	}

	private void createPerpendicularCutsComposite(PerpendicularImageCutsComposite cutsComposite) {
		PerpendicularCutsHelper helper = new PerpendicularCutsHelper(plottingSystem);
		helper.activate(cutsComposite);
	}

	private void clearSum() {
		sumLock.writeLock().lock();
		try {
			sum = null;
		} finally {
			sumLock.writeLock().unlock();
		}
	}

	private Dataset updateSum(IDataset data, IDataset xAxisValues, IDataset yAxisValues) {
		if (data == null) {
			return null;
		}
		sumLock.writeLock().lock();
		try {
			if (sum == null) {
				sum = new ImageWithAxes(data, xAxisValues, yAxisValues);
				return (Dataset) data;
			}
			if (sum.hasSameAxes(xAxisValues, yAxisValues)) {
				return sum.addToSum(data);
			} else {
				// Axes changed, start new sum
				sum = new ImageWithAxes(data, xAxisValues, yAxisValues);
				return (Dataset) data;
			}
		} finally {
			sumLock.writeLock().unlock();
		}
	}

	@Override
	protected void updatePlot(LiveDataPlotUpdate dataUpdate) {
		try {
			final IDataset xAxisValues = dataUpdate.getxAxis();
			final IDataset yAxisValues = dataUpdate.getyAxis();
			final IDataset plotData;
			if (isSum) {
				plotData = updateSum(dataUpdate.getData(), xAxisValues, yAxisValues);
			} else {
				plotData = dataUpdate.getData();
			}
			List<IDataset> axes = Arrays.asList(xAxisValues, yAxisValues);
			cacheLastDataUpdate(plotData, axes);
			doUpdate(lastDataUpdate);

		} catch (Exception e) {
			logger.error("Error handling plot update", e);
		}
	}

	private void cacheLastDataUpdate(IDataset plotData, List<IDataset>axes) {
		this.lastDataUpdate = new LiveDataPlotUpdate();
		lastDataUpdate.setData(plotData);
		lastDataUpdate.setxAxis(axes.get(0));
		lastDataUpdate.setyAxis(axes.get(1));
	}


	private void addTransposeAction(Composite parent) {
		Action transposeImage = new Action("Transpose", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				transposePreference = isChecked();
				if (lastDataUpdate!=null) {
					doUpdate(lastDataUpdate);
				}
			}
		};
		transposeImage.setId(PlottingConstants.IMAGE_TRANSPOSE_ID);
		transposeImage.setToolTipText("Swap axes about image origin");
		transposeImage.setChecked(false);
		IActionBars actionBars = plottingSystem.getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(transposeImage);
		ActionContributionItem item = new ActionContributionItem(transposeImage);
		item.fill(findToolbarComposite(parent));
		toolBarManager.update(true);
	}

	/**
	 * Inner class to hold image data with its axes.
	 * Thread-safe when used with external synchronization.
	 */
	private static class ImageWithAxes {
		private final Dataset image;
		private final IDataset xAxis;
		private final IDataset yAxis;
		public ImageWithAxes(IDataset data, IDataset xAxisValues, IDataset yAxisValues) {
			this.image = DatasetUtils.cast(DoubleDataset.class, data);
			this.xAxis = xAxisValues;
			this.yAxis = yAxisValues;
		}
		public boolean hasSameAxes(IDataset xAxisValues, IDataset yAxisValues) {
			return Objects.equals(this.xAxis, xAxisValues) && Objects.equals(this.yAxis, yAxisValues);
		}
		public Dataset addToSum(IDataset data) {
			return this.image.iadd(data);
		}
	}

	@Focus
	public void setFocus() {
		if (plottingSystem != null) {
			plottingSystem.setFocus();
		}
	}

	@Override
	@PreDestroy
	public void dispose() {
		// Clear sum data
		clearSum();
		super.dispose();
	}
}

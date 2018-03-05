package uk.ac.gda.devices.hidenrga.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.hidenrga.HidenRGA;
import gda.device.hidenrga.HidenRGAScannable;
import gda.factory.Finder;
import gda.observable.IObserver;
/**
 * This View component requires the HidenRGA scannable on the GDA server to be called 'rga'.
 *
 *
 */
public class RGASetup extends ViewPart implements IObserver {

	public static final String ID = "uk.ac.gda.devices.hidenrga.rgasetup";

	private static final Logger logger = LoggerFactory.getLogger(RGASetup.class);

	private Composite massesComposite;
	private Composite[] massesComposites;
	private HidenRGA rga;
	private Spinner[] massChoices;
	private Spinner sprNumberMasses;

	private ScrolledComposite mainScrolledComposite;

	private Label lblMessages;

	private Spinner sprCollectionRate;
	private int collectionSpinnerNumDP = 1; // Number of decimal places to use for collection rate spinner
	private int collectionSpinnerConv = (int) Math.pow(10, collectionSpinnerNumDP); // multiplier to convert from decimal number to spinner value.

	private int maxMassNumber = 21;
	private int numMassesPerColumn = 5;

	protected IPlottingSystem<Composite> myPlotter;
	private ILineTrace ratesTrace;
	private GridDataFactory gridDataForSpinners = GridDataFactory.swtDefaults().hint(30,  SWT.DEFAULT);

	public RGASetup() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());
		mainScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
		mainScrolledComposite.setExpandHorizontal(true);
		mainScrolledComposite.setExpandVertical(true);

		Composite topComposite = new Composite(mainScrolledComposite, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		topComposite.setLayout(new GridLayout());

		boolean found = findRGA();
		if (!found) {
			Label lblError = new Label(topComposite, SWT.NONE);
			lblError.setLayoutData(GridDataFactory.swtDefaults().create());
			lblError.setText("Hiden RGA could not be found!");
			lblError.setForeground(PlatformUI.getWorkbench().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			return;
		}

		maxMassNumber = rga.getNumberOfMassChannels();

		lblMessages = new Label(topComposite, SWT.NONE);
		lblMessages.setText("");

		createCollectionRateMassesControls(topComposite);

		createMassesComposite(topComposite);

		updateMasses();

		createBarChartView(topComposite);

		setVisibleItems(sprNumberMasses.getSelection());

		mainScrolledComposite.setContent(topComposite);
		mainScrolledComposite.setMinSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createCollectionRateMassesControls(Composite parent) {
		Composite widgetComposite = new Composite(parent, SWT.NONE);
		widgetComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

		String collectionRateToolTip = "The time interval between measurements when recording data (in seconds). " +
										"If set to 0 then the RGA will be run as fast as possible.";

		Label lbCollectionRate = new Label(widgetComposite, SWT.NONE);
		lbCollectionRate.setLayoutData(GridDataFactory.swtDefaults().create());
		lbCollectionRate.setText("Collection rate (s):");
		lbCollectionRate.setToolTipText(collectionRateToolTip);

		sprCollectionRate = new Spinner(widgetComposite, SWT.READ_ONLY);
		sprCollectionRate.setLayoutData(gridDataForSpinners.create());
		sprCollectionRate.setDigits(collectionSpinnerNumDP);
		sprCollectionRate.setMinimum(0);
		sprCollectionRate.setMaximum(60*collectionSpinnerConv);
		sprCollectionRate.setIncrement(1);
		sprCollectionRate.setSelection(1*collectionSpinnerConv);
		sprCollectionRate.setToolTipText(collectionRateToolTip);
		sprCollectionRate.addModifyListener( modifyEvent -> rga.setCollectionRate((double) sprCollectionRate.getSelection() / collectionSpinnerConv));

		Label lblNumberMasses = new Label(widgetComposite, SWT.NONE);
		lblNumberMasses.setLayoutData(GridDataFactory.swtDefaults().create());
		lblNumberMasses.setText("Number masses:");

		sprNumberMasses = new Spinner(widgetComposite, SWT.READ_ONLY);
		sprNumberMasses.setLayoutData(gridDataForSpinners.create());
		sprNumberMasses.setMinimum(1);
		sprNumberMasses.setMaximum(maxMassNumber);
		sprNumberMasses.setIncrement(1);
		sprNumberMasses.setSelection(1);
		sprNumberMasses.addModifyListener( modifyEvent -> setVisibleItems(sprNumberMasses.getSelection()) );
	}

	private void updateMasses() {
		Set<Integer> masses = rga.getMasses();
		setVisibleItems(masses.size());

		int index = 0;
		for (Integer mass : masses) {
			massChoices[index].setSelection(mass);
			index++;
		}
		sprNumberMasses.setSelection(masses.size());
		sprCollectionRate.setSelection((int)(rga.getCollectionRate()*collectionSpinnerConv));
	}

	private boolean findRGA() {
		rga = Finder.getInstance().find("rga");
		if (rga != null) {
			rga.addIObserver(this);
		}
		return rga != null;
	}

	private void createMassesComposite(Composite mainComposite) {
		int numColumns = 1 + maxMassNumber / numMassesPerColumn;
		massesComposite = new Composite(mainComposite, SWT.NONE);
		massesComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(numColumns).create());

		massesComposites = new Composite[maxMassNumber];
		massChoices = new Spinner[maxMassNumber];

		Composite columnComposite = null;

		for (int i = 0; i < maxMassNumber; i++) {

			// Put controls in series of columns
			if (i % numMassesPerColumn == 0) {
				columnComposite = new Composite(massesComposite,SWT.NONE);
				columnComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
			}

			massesComposites[i] = new Composite(columnComposite, SWT.NONE);
			massesComposites[i].setLayoutData(GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).create());
			massesComposites[i].setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

			Label massLabel = new Label(massesComposites[i], SWT.NONE);
			massLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			massLabel.setText((i + 1) + ":");

			massChoices[i] = new Spinner(massesComposites[i], SWT.NONE);
			massChoices[i].setLayoutData(gridDataForSpinners.create());
			massChoices[i].setMinimum(0);
			massChoices[i].setMaximum(128);

			Label amuLabel = new Label(massesComposites[i], SWT.NONE);
			amuLabel.setLayoutData(GridDataFactory.fillDefaults().create());
			amuLabel.setText("amu");
		}
	}

	protected void setVisibleItems(int selection) {
		for (int i = 1; i <= maxMassNumber; i++) {
			massesComposites[i - 1].setVisible(i <= selection);
		}
		massesComposite.layout();
		massesComposite.pack();
	}

	@Override
	public void setFocus() {
		if (massesComposites != null && massesComposites.length > 0 && massesComposites[0] != null) {
			massesComposites[0].setFocus();
		}
	}

	/**
	 * Refresh the UI from the server-side object
	 */
	public void refresh() {
		if (rga != null) {
			updateMasses();
		}
	}

	/**
	 * Apply the masses shown in the UI to the server-side object
	 */
	public void apply() {
		if (rga != null) {
			int[] masses = new int[sprNumberMasses.getSelection()];
			for (int i = 0; i < sprNumberMasses.getSelection(); i++) {
				masses[i] = massChoices[i].getSelection();
			}
			rga.setMasses(masses);
			rga.setCollectionRate((double) sprCollectionRate.getSelection() / collectionSpinnerConv);
		}
	}

	/**
	 * Start/stop recording masses to a file. This button could become out of
	 * sync if the recording is stop/started from the Jython commandline.
	 */
	public void toggleRecording() {
		try {
			if (!rga.isBusy()) {
				rga.startRecording();
			} else {
				rga.stopRecording();
			}
		} catch (Exception e) {
			logger.error("Exception when trying to toggle RGA recording", e);
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg.toString().compareTo(HidenRGAScannable.RECORDING_STARTED) == 0) {
			writeMessage("Recording");
		} else if (arg.toString().compareTo(HidenRGAScannable.RECORDING_STOPPED) == 0) {
			writeMessage("");
		}

	}

	private void writeMessage(final String string) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			lblMessages.setText(string);
			mainScrolledComposite.layout();
		});
	}

	private void createBarChartView(Composite parent) {
		try {
			myPlotter = PlottingFactory.createPlottingSystem();

			int numElements = rga.getNumBarChartPressures();
			IDataset xvals = DatasetFactory.createRange(DoubleDataset.class, 0.0, numElements, 1.0);
			IDataset yvals = DatasetFactory.zeros(DoubleDataset.class, xvals.getShape());

			Composite barChartComposite = new Composite(parent, SWT.NONE);
			barChartComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.minimumHeight = 200;
			barChartComposite.setLayoutData(gridData);

			myPlotter.createPlotPart(barChartComposite, "Masses", null, PlotType.XY, null);
			myPlotter.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			IAxis primaryAxis = myPlotter.getSelectedXAxis();

			myPlotter.setTitle("RGA Bar Chart pressures");

			ratesTrace = myPlotter.createLineTrace("RGA Bar Chart Pressures [bars]");
			ratesTrace.setTraceType(TraceType.HISTO);
			ratesTrace.setLineWidth(4);
			ratesTrace.setTraceColor(new Color(null, 0, 0, 128));
			ratesTrace.setData(xvals, yvals);

			myPlotter.addTrace(ratesTrace);
			primaryAxis.setRange(0, numElements);
			primaryAxis.setTitle("Mass [amu]");
			myPlotter.getSelectedYAxis().setTitle("log10 Pressure [bars]");
			myPlotter.getSelectedYAxis().setRange(0,1);

			myPlotter.setSelectedXAxis(primaryAxis);
			myPlotter.setShowLegend(true);

			Button getBarCharMassesButton = new Button(barChartComposite, SWT.PUSH);
			getBarCharMassesButton.setText("Get pressures from RGA");
			getBarCharMassesButton.setToolTipText("Collect values from Epics RGA 'Bar' view");
			getBarCharMassesButton.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateBarChartPressures();
				}
			});

		} catch (Exception e) {
			logger.error("Problem creating bar chart view ", e);
		}
	}

	/**
	 * Generate list of random values to use for bar chart plot
	 * (evenly distributed on log scale from 1..1e-10)
	 * @param numValues
	 * @return
	 */
	private List<Double> getDummyPressures(int numValues) {
		Random rng = new Random();
		List<Double> pressures = new ArrayList<>();
		for(int i=0; i<numValues; i++) {
			double rand = rng.nextDouble()*10;
			double pressure = Math.pow(10, -rand);
			pressures.add(pressure);
		}
		return pressures;
	}

	public void updateBarChartPressures() {
		try {
			List<Double> pressures = null;
			if (LocalProperties.isDummyModeEnabled()) {
				pressures = getDummyPressures(200);
			} else {
				pressures = rga.getBarChartPressures();
			}

			IDataset yvals = DatasetFactory.createFromList(pressures);
			IDataset xvals = DatasetFactory.createRange(DoubleDataset.class, 0.0, pressures.size(), 1.0);

			ratesTrace.setData(xvals, yvals);
			myPlotter.getSelectedYAxis().setLog10(true);

			myPlotter.setRescale(false);
			myPlotter.repaint();

		} catch (IOException e) {
			logger.warn("Problem updating barchart with pressures from RGA ", e);
		}
	}

	@Override
	public void dispose() {
		myPlotter.dispose();
	}

}
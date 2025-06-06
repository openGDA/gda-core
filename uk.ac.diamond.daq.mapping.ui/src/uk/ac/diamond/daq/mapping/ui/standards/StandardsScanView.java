package uk.ac.diamond.daq.mapping.ui.standards;

import static gda.jython.JythonStatus.RUNNING;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_CUSTOM_PARAMS;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_STOP;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createModelFromEdgeSelection;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getXanesElementsList;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.StandardsScanParams;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LineToTrack;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanPathEditor;
import uk.ac.diamond.daq.mapping.ui.xanes.XanesEdgeCombo;

public class StandardsScanView {
	private static final Logger logger = LoggerFactory.getLogger(StandardsScanView.class);

	private static final String SCANNABLE_NAME = "dcm_enrg";
	private static final String SCRIPT_FILE = "scanning/submit_standards_scan.py";
	private static final String BUTTON_NAME = "Submit XANES standards scan";
	private static final Color BUTTON_COLOUR = new Color(Display.getDefault(), new RGB(255, 209, 71));

	@Inject
	private IEclipseContext injectionContext;

	private ScanPathEditor scanPathEditor;
	private Text exposureTimeText;
	private Button submitButton;
	private Button reverseCheckBox;

	private LineToTrack lineToTrack;
	private ComboViewer xasComboViewer;

	private IJobQueue<StatusBean> jobQueueProxy;

	private XasPosition selectedXasPosition;

	public enum XasPosition {
	    OUT(0, "Out: 0 mm"),
	    POSITION_1(19, "Pos 1: 19 mm"),
	    POSITION_2(36.5, "Pos 2: 36.5 mm"),
	    POSITION_3(54, "Pos 3: 54 mm"),
	    POSITION_4(71.5, "Pos 4: 71.5 mm"),
	    POSITION_5(89, "Pos 5: 89 mm");

	    private final double position;
	    private final String label;

	    XasPosition(double position, String label) {
	        this.position = position;
	        this.label = label;
	    }

	    public double getPosition() {
	        return position;
	    }

	    public String getLabel() {
	        return label;
	    }
	}

	@PostConstruct
	public void createView(Composite parent) {
		GridDataFactory.swtDefaults().applyTo(parent);
		GridLayoutFactory.swtDefaults().applyTo(parent);
		createScannableEditor(parent);
		createSubmitSection(parent);
		final IEventService eventService = injectionContext.get(IEventService.class);
		try {
			final URI jmsURI = new URI(LocalProperties.getBrokerURI());
			jobQueueProxy = eventService.createJobQueueProxy(jmsURI, EventConstants.SUBMISSION_QUEUE,
					EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logger.error("Error creating job queue proxy", e);
		}
	}
	/**
	 * Create the GUI elements to edit the energy scannable.<br>
	 * This consists of
	 * <ul>
	 * <li>the specification of the energy ranges</li>
	 * <li>a drop-down box to choose the edge</li>
	 * <li>a text box for the exposure time</li>
	 * <li>a check box to specify whether the energy steps should be run in reverse order</li>
	 * </ul>
	 * In general, the energy ranges should be set by choosing the edge, though they can also be edited directly.
	 *
	 * @param parent
	 *            the parent composite to draw these controls on
	 */
	private void createScannableEditor(Composite parent) {
		// Energy scannable
		final Composite editorComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(editorComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(editorComposite);

		// Scannable name
		final String energyScannableName = SCANNABLE_NAME;
		final Label nameLabel = new Label(editorComposite, SWT.NONE);
		nameLabel.setText(energyScannableName);
		// Scan path editor to display & edit energy values
		final IScanModelWrapper<IAxialModel> scannableWrapper = new ScanPathModelWrapper<>(energyScannableName, null, false);
		scanPathEditor = new ScanPathEditor(editorComposite, SWT.NONE, scannableWrapper);
		// Edge, exposure, reverse check box
		final Composite edgeAndExposureComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(edgeAndExposureComposite);
	    GridLayoutFactory.swtDefaults().numColumns(4).margins(5, 5).spacing(5, 20).applyTo(edgeAndExposureComposite);
		// List of energy/edge combinations

		var elementList = getXanesElementsList();
		if (elementList.isPresent()) {
			final XanesEdgeCombo edgeCombo = new XanesEdgeCombo(edgeAndExposureComposite, elementList.get());
			edgeCombo.addSelectionChangedListener(e -> {
				final IAxialModel scanPathModel = createModelFromEdgeSelection(edgeCombo.getSelectedEnergy(), energyScannableName);
				scanPathEditor.setScanPathModel(scanPathModel);
				EdgeToEnergy selection = (EdgeToEnergy) edgeCombo.getSelection().getFirstElement();
				String edge = selection.getEdge();
				String element = edge.split("-")[0];
				String line = edge.split("-")[1];
				lineToTrack = new LineToTrack(element, line);
			});
		}

		// Exposure time
		final Label exposureTimeLabel = new Label(edgeAndExposureComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(exposureTimeLabel);
		exposureTimeLabel.setText("Exposure time");
		exposureTimeText = new Text(edgeAndExposureComposite, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(exposureTimeText);
		// Reverse check box
		reverseCheckBox = new Button(edgeAndExposureComposite, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(reverseCheckBox);
		reverseCheckBox.setText("Reverse scan");

		final Composite xasComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(xasComposite);
	    GridLayoutFactory.swtDefaults().numColumns(4).margins(5, 5).spacing(5, 20).applyTo(xasComposite);
		final Label xasLabel = new Label(xasComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(exposureTimeLabel);
		xasLabel.setText("Xas Position");
		xasComboViewer = new ComboViewer(xasComposite);
		xasComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		xasComboViewer.setInput(XasPosition.values());
		xasComboViewer.setLabelProvider(new LabelProvider() {
		    @Override
		    public String getText(Object element) {
		        return ((XasPosition) element).getLabel();
		    }
		});
		xasComboViewer.addSelectionChangedListener(e -> {
			XasPosition selection =  (XasPosition) xasComboViewer.getStructuredSelection().getFirstElement();
			selectedXasPosition = selection;
		});
		xasComboViewer.setSelection(new StructuredSelection(XasPosition.OUT));
	}
	/**
	 * Buttons to, respectively, submit and stop the scan
	 *
	 * @param parent
	 *            the parent composite to draw these controls on
	 */
	private void createSubmitSection(Composite parent) {
		final Composite submitComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(submitComposite);
		submitButton = new Button(submitComposite, SWT.PUSH);
		submitButton.setText(getButtonName());
		submitButton.setBackground(getButtonColour());
		submitButton.addSelectionListener(widgetSelectedAdapter(e -> submitScan()));
		final Button stopButton = new Button(submitComposite, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop all scripts and the current scan");
		stopButton.setImage(Activator.getImage(IMG_STOP));
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> stopScan()));
	}
	/**
	 * Set the parameters of the scan in the Jython namespace and call a script to process them.
	 */
	private void submitScan() {
		final String scanPath = scanPathEditor.getAxisText();
		if (scanPath == null || scanPath.isEmpty()) {
			displayError("Scan path empty", "No scan path has been defined");
			return;
		}
		try {
			final StandardsScanParams scanParams = new StandardsScanParams();
			scanParams.setScanPath(scanPathEditor.getAxisText());
			scanParams.setExposureTime(Double.parseDouble(exposureTimeText.getText()));
			scanParams.setReverseScan(reverseCheckBox.getSelection());
			scanParams.setLineToTrack(lineToTrack);
			scanParams.setXasPosition(selectedXasPosition.getPosition());
			final IScriptService scriptService = injectionContext.get(IScriptService.class);
			final IMarshallerService marshallerService = injectionContext.get(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(scanParams));
		} catch (Exception e) {
			displayError("Submit error", "Error submitting scan: " + e.getMessage());
			return;
		}
		Async.execute(() -> {
			// Run the script, disabling the submit button while it is running
			final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
			try {
				setSubmitButtonEnabled(false);
				logger.info("Running standards scan script: {}", getScriptFile());
				jythonServerFacade.runScript(new File(jythonServerFacade.locateScript(getScriptFile())));
				while (jythonServerFacade.getScriptStatus() == RUNNING) {
					Thread.sleep(500);
				}
				logger.info("Finished running standards scan script");
			} catch (Exception e) {
				logger.error("Error running standards scan script", e);
			} finally {
				setSubmitButtonEnabled(true);
			}
		});
	}
	private void setSubmitButtonEnabled(boolean enabled) {
		Display.getDefault().syncExec(() -> submitButton.setEnabled(enabled));
	}
	private void stopScan() {
		logger.info("Stopping standards scan script & job");
		// Stop the script
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		jythonServerFacade.abortCommands();
		try {
			// Stop the currently-running job
			final List<StatusBean> currentJobs = jobQueueProxy.getRunningAndCompleted();
			for (StatusBean job : currentJobs) {
				if (job.getStatus() == Status.RUNNING) {
					jobQueueProxy.terminateJob(job);
				}
			}
		} catch (EventException e) {
			logger.error("Error accessing queue", e);
		}
	}
	private void displayError(String title, String message) {
		final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openError(activeShell, title, message);
	}
	@PreDestroy
	public void onDispose() {
		scanPathEditor.dispose();
	}

	protected String getScriptFile() {
        return SCRIPT_FILE;
    }

	protected String getButtonName() {
		return BUTTON_NAME;
	}

	protected Color getButtonColour() {
		return BUTTON_COLOUR;
	}
}



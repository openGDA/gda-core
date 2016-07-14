package uk.ac.gda.tomography.scan.presentation;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * A viewer's model consists of elements, represented by objects. A viewer
 * defines and implements generic infrastructure for handling model input,
 * updates, and selections in terms of elements. Input is obtained by querying
 * an <code>IContentProvider</code> which returns elements. The elements
 * themselves are not displayed directly. They are mapped to labels, containing
 * text and/or an image, using the viewer's <code>ILabelProvider</code>. </p>
 * <p>
 * Implementing a concrete content viewer typically involves the following
 * steps:
 * <ul>
 * <li>
 * create SWT controls for viewer (in constructor) (optional)</li>
 * <li>
 * initialize SWT controls from input (inputChanged)</li>
 * <li>
 * define viewer-specific update methods</li>
 * <li>
 * support selections (<code>setSelection</code>, <code>getSelection</code>)
 * </ul>
 */
public class ParametersComposite extends Composite {

	private Text start, stop, step;
	private AdapterFactoryEditingDomain editingDomain;
	private EContentAdapter adapter;
	private Text imagesPerDark;
	private Text imagesPerFlat;
	private Text darkFieldInterval;
	private Text flatFieldInterval;
	private Text title;
	private Text exposure;
	private Text inBeamX;
	private Text outBeamX;
	private Text minI;
	private Button flyscan;
	private Button extraFlatsAtEnd;
	private Text numFlyScans;
	private Text flyScanDelay;
	private Text approxCentreOfRotation;

	public ParametersComposite(Composite parent) {
		super(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.spacing = 5;
		formLayout.marginWidth = 5;
		formLayout.marginTop = 5;
		formLayout.marginRight = 5;
		formLayout.marginLeft = 5;
		formLayout.marginHeight = 5;
		formLayout.marginBottom = 5;
		setLayout(formLayout);

		Label lblTopTitle = new Label(this, SWT.WRAP);
		lblTopTitle.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		lblTopTitle.setAlignment(SWT.CENTER);
		FormData fd_lblTitle = new FormData();
		fd_lblTitle.top = new FormAttachment(0);
		fd_lblTitle.right = new FormAttachment(100);
		fd_lblTitle.left = new FormAttachment(0);
		lblTopTitle.setLayoutData(fd_lblTitle);
		lblTopTitle.setText("Tomography Scan Parameters");

		Group miscGroup = new Group(this, SWT.NONE);
		FormData fd_miscGroup = new FormData();
		fd_miscGroup.top = new FormAttachment(lblTopTitle);
		fd_miscGroup.right = new FormAttachment(100);
		fd_miscGroup.left = new FormAttachment(0);
		miscGroup.setLayoutData(fd_miscGroup);
		miscGroup.setLayout(new GridLayout(2, false));



		Label lblTitle = new Label(miscGroup, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title");

		title = new Text(miscGroup, SWT.BORDER);
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label lblExposures = new Label(miscGroup, SWT.NONE);
		lblExposures.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblExposures.setText("Exposure/s");

		exposure = new Text(miscGroup, SWT.BORDER);
		exposure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMinI = new Label(miscGroup, SWT.NONE);
		lblMinI.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMinI.setText("Min. i");

		minI = new Text(miscGroup, SWT.BORDER);
		minI.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group motorGroup = new Group(this, SWT.NONE);
		motorGroup.setText("Sample Positions");
		FormData fd_motorGroup = new FormData();
		fd_motorGroup.top = new FormAttachment(miscGroup);
		fd_motorGroup.right = new FormAttachment(100);
		fd_motorGroup.left = new FormAttachment(0);
		motorGroup.setLayoutData(fd_motorGroup);
		motorGroup.setLayout(new GridLayout(2, false));

		Label lblInBeamX = new Label(motorGroup, SWT.NONE);
		lblInBeamX.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInBeamX.setText("In Beam X");

		inBeamX = new Text(motorGroup, SWT.BORDER);
		inBeamX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblOutOfBeam = new Label(motorGroup, SWT.NONE);
		lblOutOfBeam.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOutOfBeam.setText("Out of Beam X");

		outBeamX = new Text(motorGroup, SWT.BORDER);
		outBeamX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group rotationAngleGroup = new Group(this, SWT.NONE);
		FormData fd_rotationAngleGroup = new FormData();
		fd_rotationAngleGroup.top = new FormAttachment(motorGroup);
		fd_rotationAngleGroup.right = new FormAttachment(100);
		fd_rotationAngleGroup.left = new FormAttachment(0);
		rotationAngleGroup.setLayoutData(fd_rotationAngleGroup);
		rotationAngleGroup.setText("Rotation Angle");
		rotationAngleGroup.setLayout(new GridLayout(2, false));


		Label lblStart = new Label(rotationAngleGroup, SWT.NONE);
		lblStart.setText("Start");
		lblStart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		start = new Text(rotationAngleGroup, SWT.BORDER);
		start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStop = new Label(rotationAngleGroup, SWT.NONE);
		lblStop.setText("Stop");
		lblStop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		stop = new Text(rotationAngleGroup, SWT.BORDER);
		stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStep = new Label(rotationAngleGroup, SWT.NONE);
		lblStep.setText("Step");
		lblStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		step = new Text(rotationAngleGroup, SWT.BORDER);
		step.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group grpDarksFlats = new Group(this, SWT.NONE);
		grpDarksFlats.setLayout(new GridLayout(2, false));
		FormData fd_grpDarksFlats = new FormData();
		fd_grpDarksFlats.top = new FormAttachment(rotationAngleGroup);
		fd_grpDarksFlats.right = new FormAttachment(100);
		fd_grpDarksFlats.left = new FormAttachment(0);
		grpDarksFlats.setLayoutData(fd_grpDarksFlats);
		grpDarksFlats.setText("Darks && Flats");

		Label lblImagesperDark = new Label(grpDarksFlats, SWT.NONE);
		lblImagesperDark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblImagesperDark.setText("Images per Dark");

		imagesPerDark = new Text(grpDarksFlats, SWT.BORDER);
		imagesPerDark.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStepsPerDark = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerDark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepsPerDark.setText("Steps per Dark");

		darkFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		darkFieldInterval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblImagesPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblImagesPerFlat.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblImagesPerFlat.setText("Images per Flat");

		imagesPerFlat = new Text(grpDarksFlats, SWT.BORDER);
		imagesPerFlat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStepsPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerFlat.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepsPerFlat.setText("Steps per Flat");

		flatFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		flatFieldInterval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group grpFlyScan = new Group(this, SWT.NONE);
		grpFlyScan.setLayout(new GridLayout(2, false));
		FormData fd_grpFlyScan = new FormData();
		fd_grpFlyScan.top = new FormAttachment(grpDarksFlats);
		fd_grpFlyScan.right = new FormAttachment(100);
		fd_grpFlyScan.left = new FormAttachment(0);
		grpFlyScan.setLayoutData(fd_grpFlyScan);
		grpFlyScan.setText("Fly Scan");

		flyscan = new Button(grpFlyScan, SWT.CHECK);
		flyscan.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		flyscan.setText("Fly Scan");
		flyscan.setToolTipText("The scan can be performed as a step scan or fly scan");

		extraFlatsAtEnd = new Button(grpFlyScan, SWT.CHECK);
		extraFlatsAtEnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		extraFlatsAtEnd.setText("Extra Flats at End");
		extraFlatsAtEnd.setToolTipText("Collect additional flat images at end of scan");

		Group grpMultipleFlyScans = new Group(this, SWT.NONE);
		grpMultipleFlyScans.setLayout(new GridLayout(2, false));
		FormData fd_grpMultipleFlyScans = new FormData();
		fd_grpMultipleFlyScans.top = new FormAttachment(grpFlyScan);
		fd_grpMultipleFlyScans.right = new FormAttachment(100);
		fd_grpMultipleFlyScans.left = new FormAttachment(0);
		grpMultipleFlyScans.setLayoutData(fd_grpMultipleFlyScans);
		grpMultipleFlyScans.setText("Multiple Fly Scans");

		Label lblNumFlyScans = new Label(grpMultipleFlyScans, SWT.NONE);
		lblNumFlyScans.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNumFlyScans.setText("Number of Fly Scans");

		numFlyScans = new Text(grpMultipleFlyScans, SWT.BORDER);
		numFlyScans.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblFlyScanDelay = new Label(grpMultipleFlyScans, SWT.NONE);
		lblFlyScanDelay.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFlyScanDelay.setText("Delay Between Scans/s");

		flyScanDelay = new Text(grpMultipleFlyScans, SWT.BORDER);
		flyScanDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		flyScanDelay.setToolTipText("Delay in seconds between multiple fly scans");

		Group grpReconstruction = new Group(this, SWT.NONE);
		grpReconstruction.setLayout(new GridLayout(2, false));
		FormData fd_grpReconstruction = new FormData();
		fd_grpReconstruction.top = new FormAttachment(grpMultipleFlyScans);
		fd_grpReconstruction.right = new FormAttachment(100);
		fd_grpReconstruction.left = new FormAttachment(0);
		grpReconstruction.setLayoutData(fd_grpReconstruction);
		grpReconstruction.setText("Reconstruction");

		Label lblApproxCentreOfRotation = new Label(grpReconstruction, SWT.NONE);
		lblApproxCentreOfRotation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblApproxCentreOfRotation.setText("Approx. centre of rotation/px");

		approxCentreOfRotation = new Text(grpReconstruction, SWT.BORDER);
		approxCentreOfRotation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		approxCentreOfRotation.setToolTipText("Approximate centre of rotation (pixel)");

		// -----------------------------------------------------------------------------------

		addTextToTextListeners(title, ScanPackage.eINSTANCE.getParameters_Title());
		addTextToDoubleListeners(exposure, ScanPackage.eINSTANCE.getParameters_ExposureTime());
		addTextToDoubleListeners(minI, ScanPackage.eINSTANCE.getParameters_MinI());

		addTextToDoubleListeners(inBeamX, ScanPackage.eINSTANCE.getParameters_InBeamPosition());
		addTextToDoubleListeners(outBeamX, ScanPackage.eINSTANCE.getParameters_OutOfBeamPosition());

		addTextToIntegerListeners(imagesPerDark, ScanPackage.eINSTANCE.getParameters_ImagesPerDark());
		addTextToIntegerListeners(darkFieldInterval, ScanPackage.eINSTANCE.getParameters_DarkFieldInterval());
		addTextToIntegerListeners(imagesPerFlat, ScanPackage.eINSTANCE.getParameters_ImagesPerFlat());
		addTextToIntegerListeners(flatFieldInterval, ScanPackage.eINSTANCE.getParameters_FlatFieldInterval());

		addTextToDoubleListeners(start, ScanPackage.eINSTANCE.getParameters_Start());
		addTextToDoubleListeners(stop, ScanPackage.eINSTANCE.getParameters_Stop());
		addTextToDoubleListeners(step, ScanPackage.eINSTANCE.getParameters_Step());

		addButtonSelectionListeners(flyscan, ScanPackage.eINSTANCE.getParameters_FlyScan());
		addButtonSelectionListeners(extraFlatsAtEnd, ScanPackage.eINSTANCE.getParameters_ExtraFlatsAtEnd());

		addTextToIntegerListeners(numFlyScans, ScanPackage.eINSTANCE.getParameters_NumFlyScans());
		addTextToDoubleListeners(flyScanDelay, ScanPackage.eINSTANCE.getParameters_FlyScanDelay());

		addTextToIntegerListeners(approxCentreOfRotation, ScanPackage.eINSTANCE.getParameters_ApproxCentreOfRotation());

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if( editingDomain != null){
					Resource resource = editingDomain.getResourceSet().getResources()
							.get(0);
					if( adapter != null){
						resource.eAdapters().remove(adapter);
						adapter = null;
					}
					editingDomain = null;
				}
			}
		});
	}

	private void addButtonSelectionListeners(final Button btn, final EAttribute eAttribute) {
		btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,btn.getSelection());
			}
		});

		btn.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,btn.getSelection());
			}
		});
	}


	private void addTextToDoubleListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,Double.valueOf(text.getText()));
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,Double.valueOf(text.getText()));
			}
		});
	}

	private void addTextToTextListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,text.getText());
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,text.getText());
			}
		});
	}

	private void addTextToIntegerListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,Integer.valueOf(text.getText()));
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,Integer.valueOf(text.getText()));
			}
		});
	}

	private void addModelUpdateCommand(Object itemToBeUpdated, Object newValue) {
		Command setCommand = SetCommand.create(editingDomain,
				editingDomain.getResourceSet().getResources().get(0)
						.getContents().get(0),
				itemToBeUpdated, newValue
				);
		editingDomain.getCommandStack().execute(setCommand);
	}


	public void setInput(AdapterFactoryEditingDomain editingDomain) {
		if (this.editingDomain != null)
			throw new IllegalStateException(
					"setInput should only be called once");
		this.editingDomain = editingDomain;
		Resource resource = editingDomain.getResourceSet().getResources()
				.get(0);
		Parameters x = (Parameters) (resource.getContents().get(0));
		title.setText(x.getTitle());
		exposure.setText(Double.toString(x.getExposureTime()));
		minI.setText(Double.toString(x.getMinI()));

		start.setText(Double.toString(x.getStart()));
		stop.setText(Double.toString(x.getStop()));
		step.setText(Double.toString(x.getStep()));

		outBeamX.setText(Double.toString(x.getOutOfBeamPosition()));
		inBeamX.setText(Double.toString(x.getInBeamPosition()));

		imagesPerDark.setText(Integer.toString(x.getImagesPerDark()));
		darkFieldInterval.setText(Integer.toString(x.getDarkFieldInterval()));
		imagesPerFlat.setText(Integer.toString(x.getImagesPerFlat()));
		flatFieldInterval.setText(Integer.toString(x.getFlatFieldInterval()));

		flyscan.setSelection(x.isFlyScan());
		extraFlatsAtEnd.setSelection(x.getExtraFlatsAtEnd());

		numFlyScans.setText(Integer.toString(x.getNumFlyScans()));
		flyScanDelay.setText(Double.toString(x.getFlyScanDelay()));

		if (x.getApproxCentreOfRotation() != null) {
			approxCentreOfRotation.setText(Integer.toString(x.getApproxCentreOfRotation()));
		}

		adapter = new EContentAdapter() {

			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				Object notifier = notification.getNotifier();
				if (notifier instanceof Parameters) {
					Object feature = notification.getFeature();
					if( feature != null){
						Parameters parameters = (Parameters) notifier;
						if( feature.equals(ScanPackage.eINSTANCE.getParameters_Start())){
							start.setText(Double.toString(parameters.getStart()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Stop())){
							stop.setText(Double.toString(parameters.getStop()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Step())){
							step.setText(Double.toString(parameters.getStep()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Title())){
							title.setText(parameters.getTitle());
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ExposureTime())){
							exposure.setText(Double.toString(parameters.getExposureTime()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_MinI())){
							minI.setText(Double.toString(parameters.getMinI()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_OutOfBeamPosition())){
							outBeamX.setText(Double.toString(parameters.getOutOfBeamPosition()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_InBeamPosition())){
							inBeamX.setText(Double.toString(parameters.getInBeamPosition()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ImagesPerDark())){
							imagesPerDark.setText(Integer.toString(parameters.getImagesPerDark()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_DarkFieldInterval())){
							darkFieldInterval.setText(Integer.toString(parameters.getDarkFieldInterval()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ImagesPerFlat())){
							imagesPerFlat.setText(Integer.toString(parameters.getImagesPerFlat()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_FlatFieldInterval())){
							flatFieldInterval.setText(Integer.toString(parameters.getFlatFieldInterval()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_FlyScan())){
							flyscan.setSelection(parameters.isFlyScan());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_ExtraFlatsAtEnd())) {
							extraFlatsAtEnd.setSelection(parameters.getExtraFlatsAtEnd());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_NumFlyScans())) {
							numFlyScans.setText(Integer.toString(parameters.getNumFlyScans()));
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_FlyScanDelay())) {
							flyScanDelay.setText(Double.toString(parameters.getFlyScanDelay()));
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_ApproxCentreOfRotation())) {
							approxCentreOfRotation.setText(Integer.toString(parameters.getApproxCentreOfRotation()));
						}
					}

				}
			}
		};
		resource.eAdapters().add(adapter);

	}
}

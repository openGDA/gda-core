/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.tomography.datacollection.ui.adviewer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Queue;
import gda.device.scannable.DummyUnitsScannable;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.StageCompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderCompositeFactory;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.composites.MJPeg;
import uk.ac.gda.epics.adviewer.views.MJPegView;
import uk.ac.gda.tomography.scan.editor.ScanParameterDialog;

public class DataCollectionMJPEGViewComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(DataCollectionMJPEGViewComposite.class);

	private DataCollectionADControllerImpl adControllerImpl;
	private EnumPositionerComposite lensComposite;
	private EnumPositionerComposite binningXComposite;
	private EnumPositionerComposite binningYComposite;
	private DataCollectionMJPegViewInitialiser mJPegViewInitialiser;
	private Button btnDragX;
	private Button btnDragY;

	private Image sinogram_image;
	private Image normalizedImage_image;

	private MJPeg mJPeg;

	private Label statusField;
	private Group grpDrag;
	
	public DataCollectionMJPEGViewComposite(final Composite parent, CompositeFactory configTabsFactory) throws Exception {
		super(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(this);

		//---------------------------------------------------------------------------------------------------------
		// The top part of the view contains tabs for setting motor positions and buttons to bring up scan dialogs
		//---------------------------------------------------------------------------------------------------------
		final Composite top = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).span(1, 1).applyTo(top);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(20,  1).applyTo(top);

		// Top left are the tabs to configure various sets of motors
		// Most of the work is done by the CompositeFactory passed into this class.
		final Composite configTabs = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(configTabs);

		if (configTabsFactory == null) {
			configTabsFactory = createDummyConfigCompositeFactory();
		}
		configTabsFactory.createComposite(configTabs, SWT.NONE);

		// Top right are buttons to bring up scan dialogs, and lens/drag axis configuration
		final Composite rhs = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(5, 5).applyTo(rhs);

		// Scan dialog buttons
		final Composite scanButtons = new Composite(rhs, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(scanButtons);

		final Button showNormalisedImage = new Button(scanButtons, SWT.PUSH);
		showNormalisedImage.setToolTipText("Get Normalised Image");
		showNormalisedImage.setImage(createImage("icons/normalisedImage.gif"));
		showNormalisedImage.setText("Normalised\nImage...");
		showNormalisedImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showNormalisedImageDialog();
			}
		});

		final Button openScanDlg = new Button(scanButtons, SWT.PUSH);
		openScanDlg.setToolTipText("Start a tomography data scan");
		openScanDlg.setImage(createImage("icons/sinogram.gif"));
		openScanDlg.setText("Tomography\nScan...");
		openScanDlg.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final ScanParameterDialog scanParameterDialog = new ScanParameterDialog(e.display.getActiveShell());
					scanParameterDialog.setBlockOnOpen(true);
					scanParameterDialog.open();
				} catch (Exception ex) {
					logger.error("Error displaying dialog", ex);
				}
			}
		});

		// Drop-down boxes to configure lens and horizontal/vertical binning
		final Composite lensConfig = new Composite(rhs, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(5).applyTo(lensConfig);

		lensComposite = new EnumPositionerComposite(lensConfig, SWT.NONE, "Lens",
				"Are you sure you want to change the camera lens to '%s'", "Changing lens",
				"tomodet.setCameraLens('%s')");
		GridDataFactory.swtDefaults().applyTo(lensComposite);

		binningXComposite = new EnumPositionerComposite(lensConfig, SWT.NONE, "H Bin",
				"Are you sure you want to change the binning to '%s'. The detector will respond when acquisition is restarted.",
				"Changing bin x", null);
		GridDataFactory.swtDefaults().applyTo(binningXComposite);

		binningYComposite = new EnumPositionerComposite(lensConfig, SWT.NONE, "V Bin",
				"Are you sure you want to change the binning to '%s'. The detector will respond when acquisition is restarted.",
				"Changing bin y", null);
		GridDataFactory.swtDefaults().applyTo(binningYComposite);

		// Buttons to set drag axis
		grpDrag = new Group(rhs, SWT.NONE);
		grpDrag.setText("Drag Axis");
		grpDrag.setLayout(new FillLayout(SWT.HORIZONTAL));

		btnDragX = new Button(grpDrag, SWT.NORMAL);
		btnDragX.setText("Sample x");
		btnDragX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus("Drag the image to the desired position - ESC to cancel.");
				mJPegViewInitialiser.handleDragAxisBtn(true);
			}
		});

		btnDragY = new Button(grpDrag, SWT.NORMAL);
		btnDragY.setText("Sample y");
		btnDragY.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus("Drag the image to the desired position - ESC to cancel.");
				mJPegViewInitialiser.handleDragAxisBtn(false);
			}
		});

		//---------------------------------------------------------------------------------------------------------
		// The bottom part of the view contains camera controls and the image returned by the camera.
		// Most of the work is done by the MJPeg class
		//---------------------------------------------------------------------------------------------------------
		statusField = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(statusField);
		statusField.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));

		final Composite cameraView = new Composite(this, SWT.NONE);
		cameraView.setLayout(new FillLayout(SWT.HORIZONTAL));
		cameraView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mJPeg = new MJPeg(cameraView, SWT.BORDER);
		mJPeg.showLeft(true);

		//---------------------------------------------------------------------------------------------------------
		// Tidy up when this view is closed
		//---------------------------------------------------------------------------------------------------------
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (mJPegViewInitialiser != null) {
					mJPegViewInitialiser.dispose();
					mJPegViewInitialiser = null;
				}
				if (sinogram_image != null) {
					sinogram_image.dispose();
					sinogram_image = null;
				}
				if (normalizedImage_image != null) {
					normalizedImage_image.dispose();
					normalizedImage_image = null;
				}
			}
		});
	}
	
	private CompositeFactory createDummyConfigCompositeFactory() throws Exception {
		final DummyUnitsScannable scannable = new DummyUnitsScannable("test", 0.0, "mm", "mm");
		scannable.configure();

		final StageCompositeDefinition definition = new StageCompositeDefinition();
		definition.setScannable(scannable);
		definition.setStepSize(.1);

		final StageCompositeFactory scf = new StageCompositeFactory();
		scf.setStageCompositeDefinitions(new StageCompositeDefinition[] { definition });
		
		final TabCompositeFactoryImpl tab = new TabCompositeFactoryImpl();
		tab.setCompositeFactory(scf);
		tab.setLabel("tab");

		final TabFolderCompositeFactory tabs = new TabFolderCompositeFactory();
		tabs.setFactories(new TabCompositeFactory[] { tab });
		tabs.afterPropertiesSet();

		return tabs;
	}

	private void showNormalisedImageDialog() {
		final Dialog dlg = new Dialog(Display.getCurrent().getActiveShell()) {

			private Text outBeamX;
			private Text exposureTime;

			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setText("Get Normalised Image");
			}

			@Override
			protected void createButtonsForButtonBar(final Composite parent) {
				// create OK and Cancel buttons by default
				createButton(parent, IDialogConstants.OK_ID, "Run", false);
				createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite cmp = new Composite(parent, SWT.NONE);
				GridDataFactory.fillDefaults().applyTo(cmp);
				cmp.setLayout(new GridLayout(2, false));
				Label lblOutOfBeam = new Label(cmp, SWT.NONE);
				lblOutOfBeam.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblOutOfBeam.setText("Out of beam position/mm");
				outBeamX = new Text(cmp, SWT.BORDER);
				outBeamX.setText("0.0");
				GridData outBeamXLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				outBeamXLayoutData.minimumWidth = 50;
				outBeamX.setLayoutData(outBeamXLayoutData);

				Label lblExposure = new Label(cmp, SWT.NONE);
				lblExposure.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblExposure.setText("Exposure time/s");
				exposureTime = new Text(cmp, SWT.BORDER);
				exposureTime.setText("1.0");
				exposureTime.setLayoutData(outBeamXLayoutData);
				return cmp;
			}

			@Override
			protected void okPressed() {
				final String cmd = String.format(adControllerImpl.getShowNormalisedImageCmd(), outBeamX.getText(), exposureTime.getText());
				try {
					Queue queue = CommandQueueViewFactory.getQueue();
					if (queue != null) {
						queue.addToTail(new JythonCommandCommandProvider(cmd, "Running command '" + cmd + "'", null));
						CommandQueueViewFactory.showView();
					} else {
						throw new Exception("Queue not found");
					}
				} catch (Exception e1) {
					MJPegView.reportErrorToUserAndLog("Error showing normalised image", e1);
				}
				super.okPressed();
			}

		};
		dlg.open();
	}
	
	private Image createImage(final String imageLocation) {
		final ImageDescriptor desc = TomoClientActivator.getImageDescriptor(imageLocation);
		if (desc == null) {
			return new Image(Display.getCurrent(), 50, 50);
		} else {
			return desc.createImage();
		}
	}

	public void setADController(final ADController adController, final MJPegView mjPegView) {
		if (!(adController instanceof DataCollectionADControllerImpl)) {
			throw new IllegalArgumentException("ADController must be of type DataCollectionADControllerImpl");
		}
		adControllerImpl = (DataCollectionADControllerImpl) adController;
		mJPegViewInitialiser = new DataCollectionMJPegViewInitialiser(adControllerImpl, mJPeg, mjPegView, this);
		lensComposite.setEnumPositioner(adControllerImpl.getLensEnum());
		binningXComposite.setEnumPositioner(adControllerImpl.getBinningXEnum());
		binningYComposite.setEnumPositioner(adControllerImpl.getBinningYEnum());

		if (adControllerImpl.getSampleCentringXMotor() == null) {
			grpDrag.setVisible(false);
		}

		mJPeg.setADController(adController);
	}

	public MJPeg getMJPeg() {
		return mJPeg;
	}

	public void updateStatus(final String status) {
		statusField.setText(status);
		statusField.getParent().layout();
	}
}

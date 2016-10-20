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

import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Queue;
import gda.device.scannable.DummyUnitsScannable;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.StageCompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderCompositeFactory;

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
/*	private EnumPositionerComposite regionSizeXComposite;
	private EnumPositionerComposite regionSizeYComposite;
*/	private DataCollectionMJPegViewInitialiser mJPegViewInitialiser;
	private Button btnDragX;
	private Button btnDragY;
	// private Button btnDragROI;

//	private Button btnVertMoveOnClick;

	private Image sinogram_image;

	private Image normalizedImage_image;

	private MJPeg mJPeg;

/*	private Button btnHorzMoveOnClick;

	private Button btnShowRotAxis;
*/
	private Label statusField;

	private Group grpDrag;

	public DataCollectionMJPEGViewComposite(Composite parent, CompositeFactory cf) throws Exception {
		super(parent, SWT.NONE);
		GridLayoutFactory fillDefaults = GridLayoutFactory.fillDefaults().spacing(1, 1);
		fillDefaults.applyTo(this);
		Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		GridLayout topLayout = fillDefaults.create();
		topLayout.numColumns = 2;
		top.setLayout(topLayout);
		Composite c = new Composite(top, SWT.NONE);
		// c.setLayout(new GridLayout(1, false));
		fillDefaults.applyTo(c);

		if (cf == null) {
			StageCompositeFactory scf = new StageCompositeFactory();
			StageCompositeDefinition[] stageCompositeDefinitions = new StageCompositeDefinition[1];
			StageCompositeDefinition definition = new StageCompositeDefinition();
			stageCompositeDefinitions[0] = definition;
			DummyUnitsScannable scannable = new DummyUnitsScannable("test", 0.0, "mm", "mm");
			scannable.configure();
			definition.setScannable(scannable);
			definition.setStepSize(.1);
			scf.setStageCompositeDefinitions(stageCompositeDefinitions);
			TabFolderCompositeFactory tabs = new TabFolderCompositeFactory();
			TabCompositeFactoryImpl tab = new TabCompositeFactoryImpl();
			tab.setCompositeFactory(scf);
			tab.setLabel("tab");
			tabs.setFactories(new TabCompositeFactory[] { tab });
			tabs.afterPropertiesSet();
			cf = tabs;
		}
		cf.createComposite(c, SWT.NONE);

		Composite rhs = new Composite(top, SWT.NONE);
		fillDefaults.applyTo(rhs);

		Composite scans = new Composite(rhs, SWT.NONE);
		GridLayout scansLayout = fillDefaults.create();
		scansLayout.numColumns = 2;
		scans.setLayout(scansLayout);

		Button showNormalisedImage = new Button(scans, SWT.PUSH);
		showNormalisedImage.setToolTipText("Get Normalised Image");

		Button openScanDlg = new Button(scans, SWT.PUSH);
		openScanDlg.setToolTipText("Start a tomography data scan");
		openScanDlg.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					ScanParameterDialog scanParameterDialog = new ScanParameterDialog(e.display.getActiveShell());
					scanParameterDialog.setBlockOnOpen(true);
					scanParameterDialog.open();
				} catch (Exception ex) {
					logger.error("Error displaying dialog", ex);
				}
			}
		});
		showNormalisedImage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dlg = new Dialog(Display.getCurrent().getActiveShell()) {

					private Text outBeamX;
					private Text exposureTime;

					@Override
					protected void configureShell(Shell newShell) {
						super.configureShell(newShell);
						newShell.setText("Get Normalised Image");
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
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
		});
		{
			ImageDescriptor desc = TomoClientActivator.getImageDescriptor("icons/normalisedImage.gif");
			if (desc != null) {
				normalizedImage_image = desc.createImage();
				showNormalisedImage.setImage(normalizedImage_image);
			}
			showNormalisedImage.setText("Normalised\nImage...");
		}
		{
			ImageDescriptor desc = TomoClientActivator.getImageDescriptor("icons/sinogram.gif");
			if (desc != null) {
				sinogram_image = desc.createImage();
				openScanDlg.setImage(sinogram_image);
			}
			openScanDlg.setText("Tomography\nScan...");
		}

		Composite btnLens = new Composite(rhs, SWT.NONE);
		GridLayout btnLensLayout = fillDefaults.create();
		btnLensLayout.numColumns = 5;
		btnLens.setLayout(btnLensLayout);

		lensComposite = new EnumPositionerComposite(btnLens, SWT.NONE, "Lens", "Are you sure you want to change the camera lens to '%s'", "Changing lens",
				"tomodet.setCameraLens('%s')");
		GridDataFactory.swtDefaults().applyTo(lensComposite);
		binningXComposite = new EnumPositionerComposite(btnLens, SWT.NONE, "H Bin",
				"Are you sure you want to change the binning to '%s'. The detector will respond when acquisition is restarted.", "Changing bin x", null);
		GridDataFactory.swtDefaults().applyTo(binningXComposite);
		binningYComposite = new EnumPositionerComposite(btnLens, SWT.NONE, "V Bin",
				"Are you sure you want to change the binning to '%s'. The detector will respond when acquisition is restarted.", "Changing bin y", null);
		GridDataFactory.swtDefaults().applyTo(binningYComposite);
/*		regionSizeXComposite = new EnumPositionerComposite(btnLens, SWT.NONE, "H Size",
				"Are you sure you want to change the region to '%s'. The detector will respond when acquisition is restarted.",
				"Changing region x", null);
		GridDataFactory.swtDefaults().applyTo(regionSizeXComposite);
		regionSizeYComposite = new EnumPositionerComposite(btnLens, SWT.NONE, "V Size",
				"Are you sure you want to change the region to '%s'. The detector will respond when acquisition is restarted.",
				"Changing region y", null);
		GridDataFactory.swtDefaults().applyTo(regionSizeYComposite);
*/

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

/*		btnDragROI = new Button(grpDrag, SWT.NORMAL);
		btnDragROI.setText("ROI");
		btnDragROI.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus("Drag the ROI to the desired position/size - ESC to cancel or RET to complete");
				mJPegViewInitialiser.handleDragROIBtn();
			}

		});
*/

		statusField = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(statusField);
		statusField.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));

		Composite composite_2 = new Composite(this, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mJPeg = new MJPeg(composite_2, SWT.BORDER);
		mJPeg.showLeft(true);

/*		Group grpSampleMoveOn = new Group(composite, SWT.NONE);
		grpSampleMoveOn.setText("Sample Move On Click");
		grpSampleMoveOn.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnVertMoveOnClick = new Button(grpSampleMoveOn, SWT.CHECK);
		btnVertMoveOnClick.setText("Move Vertical\n to Image Center");
		btnVertMoveOnClick
				.setToolTipText("When enabled the point clicked on in the image is moved to the rotation axis position");

		btnHorzMoveOnClick = new Button(grpSampleMoveOn, SWT.CHECK);
		btnHorzMoveOnClick.setBounds(0, 0, 93, 20);
		btnHorzMoveOnClick.setText("Move Horizontal\n to Rotation Axis");
		btnVertMoveOnClick.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if( btnVertMoveOnClick.getSelection() && !btnShowRotAxis.getSelection()){
					btnShowRotAxis.setSelection(true);
					actOnShowRotAxisSelection();
				}
				setVertMoveOnClick();
			}
		});
		btnHorzMoveOnClick.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if( btnVertMoveOnClick.getSelection() && !btnShowRotAxis.getSelection()){
					actOnShowRotAxisSelection();
					btnShowRotAxis.setSelection(true);
				}
				setHorzMoveOnClick();
			}
		});
*/
		// mJPegViewInitialiser = new DataCollectionMJPegViewInitialiser(adControllerImpl, mJPeg, mjPegView);
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (mJPegViewInitialiser != null)
					mJPegViewInitialiser.dispose();
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

/*	protected void setVertMoveOnClick() {
		if (mJPegViewInitialiser != null)
			mJPegViewInitialiser.setVertMoveOnClick(btnVertMoveOnClick.getSelection());
	}
	protected void setHorzMoveOnClick() {
		if (mJPegViewInitialiser != null)
			mJPegViewInitialiser.setHorzMoveOnClick(btnHorzMoveOnClick.getSelection());
	}
*/

	public void setADController(ADController adController, MJPegView mjPegView) {
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
/*		regionSizeXComposite.setEnumPositioner(adControllerImpl.getRegionSizeXEnum());
		regionSizeYComposite.setEnumPositioner(adControllerImpl.getRegionSizeYEnum());
*/
		mJPeg.setADController(adController);
	}

	public MJPeg getMJPeg() {
		return mJPeg;
	}

/*	private void actOnShowRotAxisSelection() {
		try {
			if (mJPegViewInitialiser != null) {
				mJPegViewInitialiser.setRotationAxisAction(btnShowRotAxis.getSelection());
				mJPegViewInitialiser.setImageCenterAction(btnShowRotAxis.getSelection());
			}
		} catch (Exception e1) {
			logger.error("Error showing rot axis or beam center", e1);
		}
	}
*/

	public void updateStatus(String status) {
		statusField.setText(status);
		statusField.getParent().layout();
	}
}

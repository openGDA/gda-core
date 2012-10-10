/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;

public class TomoAlignmentLeftPanelComposite extends Composite {

	private static final String FAST_PREVIEW = "Fast Preview";
	private static final String SINGLE = "Single";
	private static final String STREAM = "Stream";
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentLeftPanelComposite.class);
	private List<ITomoAlignmentLeftPanelListener> leftPanelListeners;
	// font
	private static final String BOLD_TEXT_10 = "bold_10";
	private FontRegistry fontRegistry;
	//
	private Button btnFlatDarkCorrection;
	private Button btnFlatShow;
	//
	private Button btnDarkShow;
	//
	private Button btnSaturation;
	private Button btnProfile;
	//
	private Button btnTakeFlatAndDark;
	//
	private ControlButton btnSampleIn;
	private ControlButton btnSampleOut;

	private ZoomButtonComposite zoomComposite;
	private Button btnStream;
	private Button btnSingle;
	private Button btnFastPreview;
	private Button btnCrossHair;
	private Button btnHistogram;

	private static final String FLAT_EXP_TIME_CAPTURED_shortmsg = "Exposure time \r %.3g (s)";
	private static final String FLAT_AND_DARK_UNAVAILABLE_shortdesc = "Flat and Dark Images have not been captured. Click 'Take Flat && Dark' to capture Flat and dark images";

	public TomoAlignmentLeftPanelComposite(Composite parent, int style) {
		super(parent, style);
		initializeFontRegistry();

		FormToolkit toolkit = new FormToolkit(getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);

		GridLayout gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.marginWidth = 2;
		gl.marginHeight = 2;
		this.setBackground(ColorConstants.black);
		this.setLayout(gl);

		Composite leftPanelComposite = toolkit.createComposite(this);
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.verticalSpacing = 8;
		gl.marginHeight = 2;
		leftPanelComposite.setLayout(gl);
		leftPanelComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		btnStream = toolkit.createButton(leftPanelComposite, STREAM, SWT.PUSH);
		btnStream.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnSingle = toolkit.createButton(leftPanelComposite, SINGLE, SWT.PUSH);
		btnSingle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnFastPreview = toolkit.createButton(leftPanelComposite, FAST_PREVIEW, SWT.PUSH);
		btnFastPreview.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite zoomBorderCmp = toolkit.createComposite(leftPanelComposite);
		zoomBorderCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.marginHeight = 2;
		zoomBorderCmp.setLayout(gl);
		zoomBorderCmp.setBackground(ColorConstants.black);

		Composite zoomCmp = new ZoomButtonComposite(zoomBorderCmp, toolkit);
		zoomCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnHistogram = toolkit.createButton(leftPanelComposite, "Histogram", SWT.PUSH);
		btnHistogram.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnProfile = toolkit.createButton(leftPanelComposite, "Profile", SWT.PUSH);
		btnProfile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnSaturation = toolkit.createButton(leftPanelComposite, "Saturation", SWT.PUSH);
		btnSaturation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnCrossHair = toolkit.createButton(leftPanelComposite, "Crosshair", SWT.PUSH);
		btnCrossHair.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite flatDarkCmp = toolkit.createComposite(leftPanelComposite);
		flatDarkCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.verticalSpacing = 8;
		flatDarkCmp.setLayout(gl);

		Composite cmpHorizontalSeparator = toolkit.createComposite(flatDarkCmp);
		cmpHorizontalSeparator.setLayout(new GridLayout());
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.heightHint = 2;
		cmpHorizontalSeparator.setLayoutData(layoutData2);
		cmpHorizontalSeparator.setBackground(ColorConstants.black);

		btnSampleIn = new ControlButton(toolkit, flatDarkCmp, "Sample In", SWT.WRAP);
		btnSampleIn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnSampleOut = new ControlButton(toolkit, flatDarkCmp, "Sample Out", SWT.WRAP);
		btnSampleOut.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnTakeFlatAndDark = toolkit.createButton(flatDarkCmp, "Take Flat && Dark", SWT.PUSH | SWT.WRAP);
		btnTakeFlatAndDark.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnFlatDarkCorrection = toolkit.createButton(flatDarkCmp, "Correct Flat && Dark", SWT.PUSH | SWT.WRAP);
		btnFlatDarkCorrection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnFlatShow = toolkit.createButton(flatDarkCmp, "Show Flat", SWT.PUSH | SWT.WRAP);
		btnFlatShow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnDarkShow = toolkit.createButton(flatDarkCmp, "Show Dark", SWT.PUSH | SWT.WRAP);
		btnDarkShow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblFlatDarkExpTime = toolkit.createLabel(flatDarkCmp, "Exposure time: 0.05 s", SWT.PUSH | SWT.WRAP
				| SWT.CENTER);
		lblFlatDarkExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		leftPanelListeners = new ArrayList<ITomoAlignmentLeftPanelListener>();
	}

	public void addLeftPanelListener(ITomoAlignmentLeftPanelListener listener) {
		leftPanelListeners.add(listener);
	}

	public void removeLeftPanelListener(ITomoAlignmentLeftPanelListener listener) {
		leftPanelListeners.remove(listener);
	}

	private void initializeFontRegistry() {
		if (getDisplay() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_10, new FontData[] { new FontData(fontName, 10, SWT.BOLD) });
		}
	}

	/**
	 * @param layout
	 */
	private void setDefaultLayoutSettings(GridLayout layout) {
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
	}

	/**
	 * @return {@link ZOOM_LEVEL} selected
	 * @see ZoomButtonComposite#getSelectedZoom()
	 */
	public ZOOM_LEVEL getSelectedZoomLevel() {
		return zoomComposite.getSelectedZoom();
	}

	/**
	 * Force the GUI control to toggle the right button
	 * 
	 * @param zoomLevel
	 */
	public void setZoom(ZOOM_LEVEL zoomLevel) {
		zoomComposite.setZoomLevel(zoomLevel);
	}

	/**
	 * This is called when the "Stream" button is de-selected from the GDA GUI.
	 */
	public void stopStream() {
		if (this != null && !this.isDisposed()) {
			this.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						deSelectControl(btnStream);
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.stream(false);
						}
						if (!ZOOM_LEVEL.NO_ZOOM.equals(getSelectedZoomLevel())) {
							setZoom(ZOOM_LEVEL.NO_ZOOM);
						}
					} catch (Exception ex) {
						logger.error("stopStreaming:", ex);
					}
				}
			});
		}
	}

	public void selectSampleOutButton() {
		selectControl(btnSampleOut);
		deSelectControl(btnSampleIn);
	}

	public void selectSampleInButton() {
		selectControl(btnSampleIn);
		deSelectControl(btnSampleOut);
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	private static void selectControl(final Button btnCntrl) {
		btnCntrl.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(ColorConstants.red);
				btnCntrl.setBackground(ColorConstants.lightGray);

			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	private static void selectControl(final ControlButton btnCntrl) {
		btnCntrl.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(ColorConstants.red);
				btnCntrl.setBackground(ColorConstants.lightGray);

			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	private static void deSelectControl(final Button btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				}
			});
		}
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	private static void deSelectControl(final ControlButton btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				}
			});
		}
	}

	/**
	 * @throws Exception
	 */
	public void startStreaming() throws Exception {
		selectStreamButton();
		try {
			for (ITomoAlignmentLeftPanelListener lis : leftPanelListeners) {
				lis.stream(true);
			}
		} catch (Exception ex) {
			logger.error("startStreaming:" + ex);
			deSelectControl(btnStream);
			throw new Exception("Unable to start streaming:", ex);
		}
	}

	// Stream options
	public void selectStreamButton() {
		selectControl(btnStream);
	}

	/**
	 * @return true if saturation button is selected else returns false.
	 */
	public boolean isProfileSelected() {
		return isSelected(btnProfile);
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	private boolean isSelected(final Button button) {
		final boolean[] isSelected = new boolean[1];
		if (button != null && !button.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (ColorConstants.red.equals(button.getForeground())
							&& ColorConstants.lightGray.equals(button.getBackground())) {
						isSelected[0] = true;
					} else {
						isSelected[0] = false;
					}
				}
			});
		}
		return isSelected[0];
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	private boolean isSelected(final ControlButton button) {
		final boolean[] isSelected = new boolean[1];
		if (button != null && !button.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (ColorConstants.red.equals(button.getForeground())
							&& ColorConstants.lightGray.equals(button.getBackground())) {
						isSelected[0] = true;
					} else {
						isSelected[0] = false;
					}
				}
			});
		}
		return isSelected[0];
	}

	// TODO - Fix Ravi
	public void enableAll() {

	}

	public void disableAll() {
		// TODO Auto-generated method stub

	}

	/**
	 * selection listener for the buttons
	 */
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			Object sourceObj = event.getSource();
			if (sourceObj == btnStream) {
				if (!isSelected(btnStream)) {
					logger.debug("'Stream' is selected");
					/**/
					try {
						startStreaming();
					} catch (Exception e) {
						// showError(ERR_START_STREAM, e);
						showErrorDialog(e);
						logger.error("exception selecting stream", e);
					}
					selectControl(btnStream);
				} else {
					logger.debug("'Stream' is de-selected");
					deselectStream();
				}
			} else if (sourceObj == btnHistogram) {
				if (!isSelected(btnHistogram)) {
					startHistogram();
				} else {
					logger.debug("'btnSampleHistogram' is de-selected");
					stopHistogram();
				}
			} else if (sourceObj == btnSingle) {
				if (!isSelected(btnSingle)) {
					logger.debug("'Flat single' is selected");
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.single(isFlatCorrectionSelected());
						}
					} catch (Exception e) {
						showErrorDialog(e);
						logger.error("Flat single capturing has problems", e);
					}
				} else {
					logger.debug("'Flat Single' is de-selected");
					deSelectControl(btnSingle);
				}
			} else if (sourceObj == btnSampleIn) {
				if (!isSelected(btnSampleIn)) {
					logger.debug("'btnSampleIn' is selected");
					selectSampleInButton();
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.moveSampleIn();
						}
					} catch (IllegalStateException s) {
						// showError("Cannot Move Sample In", s);
						showErrorDialog(s);
						selectSampleOutButton();
					} catch (Exception e1) {
						// showError("Cannot Move Sample In", e1);
						showErrorDialog(e1);
						selectSampleOutButton();
					}
				}
			} else if (sourceObj == btnSampleOut) {
				if (!isSelected(btnSampleOut)) {
					logger.debug("'btnSampleOut' is selected");
					logger.debug("'btnSampleIn' is selected");
					selectSampleOutButton();
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.moveSampleOut();
						}
					} catch (IllegalStateException s) {
						// showError("Cannot Move Sample In", s);
						showErrorDialog(s);
						selectSampleInButton();
					} catch (Exception e1) {
						// showError("Cannot Move Sample In", e1);
						showErrorDialog(e1);
						selectSampleInButton();
					}
				}
			} else if (sourceObj == btnSaturation) {
				if (!isSelected(btnSaturation)) {
					logger.debug("'Saturation' is selected");
					selectControl(btnSaturation);
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.saturation(true);
						}
					} catch (Exception e) {
						deSelectControl(btnSaturation);
						showErrorDialog(e);
					}
				} else {
					logger.debug("'Saturation' is de-selected");
					deSelectControl(btnSaturation);
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.saturation(false);
						}
					} catch (Exception e) {
						deSelectControl(btnSaturation);
						// showError(ERR_WHILE_SATURATION, e);
						showErrorDialog(e);
					}
				}
			} else if (sourceObj == btnTakeFlatAndDark) {
				logger.debug("'btnTakeFlatAndDark' is selected");
				try {
					for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
						lpl.takeFlatAndDark();
					}
				} catch (InvocationTargetException ex) {
					// showError("Error while taking flat images", ex);
					showErrorDialog(ex);
				} catch (InterruptedException e) {
					// showError("Error while taking flat images", e);
					showErrorDialog(e);
				}
			} else if (sourceObj == btnFlatDarkCorrection) {
				if (!isSelected(btnFlatDarkCorrection)) {
					selectControl(btnFlatDarkCorrection);
					logger.debug("'Flat Correction' is selected");
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.correctFlatAndDark(true);
						}
					} catch (Exception ex) {
						deSelectControl(btnFlatDarkCorrection);
					}
				} else {
					deSelectControl(btnFlatDarkCorrection);
					logger.debug("'Flat Correction' is de-selected");
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.correctFlatAndDark(false);
						}
					} catch (Exception ex) {
						deSelectControl(btnFlatDarkCorrection);
					}
				}
			} else if (sourceObj == btnFlatShow) {
				logger.debug("'Show Flat' is selected");
				for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
					try {
						lpl.showFlat();
					} catch (Exception e) {
						logger.error("Problem showing flat image", e);
					}
				}
			} else if (sourceObj == btnDarkShow) {
				logger.debug("'Show Dark' is selected");
				for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
					try {
						lpl.showDark();
					} catch (Exception e) {
						logger.error("Problem showing flat image", e);
					}
				}
			} else if (sourceObj == btnProfile) {
				if (!isSelected(btnProfile)) {
					logger.debug("'Profile' is selected");
					stopStream();
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.profile(true);
						}
					} catch (Exception ex) {
						deSelectControl(btnProfile);
						// showError(ERR_PROFILING, ex);
						showErrorDialog(ex);
					}
					selectControl(btnProfile);
				} else {
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.profile(false);
						}
					} catch (Exception e) {
						logger.error("Error Profiling ;{}", e.getMessage());
					}
					deSelectControl(btnProfile);
				}
			}
		}
	};

	public void startHistogram() {
		logger.debug("'btnSampleHistogram' is selected");
		selectControl(btnHistogram);
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.histogram(true);
			}
		} catch (Exception e1) {
			logger.debug("Error setting exposure time", e1);
		}
	}

	public void stopHistogram() {

	}

	public void deselectProfileButton() {
		deSelectControl(btnProfile);
	}

	/**
	 * Starts the sample single
	 * 
	 * @throws Exception
	 */
	public void startSingle() throws Exception {
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.single(isFlatCorrectionSelected());
			}
		} catch (Exception ex) {
			logger.error("Start sample single" + ex);
			throw ex;
		}
	}

	public boolean isFlatCorrectionSelected() {
		return isSelected(btnFlatDarkCorrection);
	}

	/**
	 * Deselects both the streams
	 */
	public void deselectStream() {
		deSelectControl(btnStream);
	}

	private void showErrorDialog(Exception ex) {
		MessageDialog.openError(getShell(), "Error during alignment", ex.getMessage());
	}

	public void setFlatFieldCorrection(final boolean enabled) {
		if (this != null && !this.isDisposed()) {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (enabled) {
						selectControl(btnFlatDarkCorrection);
					} else {
						deSelectControl(btnFlatDarkCorrection);
					}
				}
			});
		}
	}

	/**
	 * To be called only if the saturation button needs to be show as de-selected
	 */
	public void deSelectSaturationButton() {
		deSelectControl(btnSaturation);
	}

	public void setFlatCaptured(boolean flatCaptured, double flatCapturedExposureTime) {
		// if (flatCaptured) {
		// // relayout the flat dark composite to show the 'show/correct flat/dark button'
		// lblFlatExpTime.setText(String.format(FLAT_EXP_TIME_CAPTURED_shortmsg, flatCapturedExposureTime));
		// this.flatCapturedExposureTime = flatCapturedExposureTime;
		// } else {
		// flatAndDarkCompositeStackLayout.topControl = lblFlatDarkNotAvailable;
		// flatCapturedExposureTime = -1;
		// }
		// flatAndDarkContainerComposite.layout();
	}

	/**
	 * To be called if the saturation button has to be shown de-selected and further processing needs to be done on the
	 * image in the image displayed window.
	 */
	public void saturationOff() {
		deSelectSaturationButton();
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.saturation(false);
			}
		} catch (Exception ex) {
			logger.error("Exception switching off saturation");
		}
	}
	
	/**
	 * @return true if saturation button is selected else returns false.
	 */
	public boolean isSaturationSelected() {
		return isSelected(btnSaturation);
	}

}

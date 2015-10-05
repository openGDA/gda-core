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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.PageBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;

public class TomoAlignmentLeftPanelComposite extends Composite {
	private static final String EXPOSURE_TIME_FORMAT = "%.3g";
	private static final String INVALID_VALUE = "Invalid value ";
	private static final String DIFFERENT_FLAT_EXPOSURE_TIME_lbl = "Different Flat Exposure Time";
	private static final String SECONDS_lbl = "s";
	private static final String BLANK = "";
	private static final String EXPOSURE_TIME_lbl = "EXPOSURE TIME";
	private static final String FLAT_lbl = "Flat";
	private static final String SAMPLE_lbl = "Sample";
	private static final String SHOW_FLAT = "Show Flat";
	private static final String SHOW_DARK = "Show Dark";
	private static final String FAST_PREVIEW = "Fast Preview";
	private static final String SINGLE = "Single";
	private static final String STREAM = "Stream";
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentLeftPanelComposite.class);
	private List<ITomoAlignmentLeftPanelListener> leftPanelListeners;
	// font
	private static final String BOLD_TEXT_10 = "bold_10";
	private static final String NORMAL_TEXT_7 = "normal_7";

	private boolean flatDarkTaken = false;

	private FontRegistry fontRegistry;
	//
	private Button btnFlatDarkCorrection;
	private Button btnFlatShow;
	//
	private Button btnDarkShow;
	//
	private Button btnDifferentFlatExpTime;
	private Button btnSaturation;
	private Button btnProfile;
	//
	private Button btnTakeFlatAndDark;
	//
	private Button btnSampleIn;
	private Button btnSampleOut;

	private ZoomButtonComposite zoomComposite;
	private Button btnStream;
	private Button btnSingle;
	private Button btnFastPreview;
	private Button btnCrossHair;
	private Button btnHistogram;
	private boolean streamButtonSelected;
	private Button btnSample;
	private Button btnFlat;

	private Text txtSampleExposureTime;

	private Text txtFlatExpTime;

	private double flatExposureTime;
	private double sampleExposureTime;
	private SAMPLE_OR_FLAT streamState = SAMPLE_OR_FLAT.SAMPLE;

	private static final String FLAT_EXP_TIME_CAPTURED_shortmsg = "Exposure time \r %.3g (s)";
	private static final String FLAT_AND_DARK_UNAVAILABLE_shortdesc = "Flat and Dark Images have not been captured. Click 'Take Flat && Dark' to capture Flat and dark images";

	private boolean isDifferentFlatExposureTime = false;
	private Label lblSample;

	private Composite pgLblSampleCmp;
	private Composite pgBtnSampleCmp;
	private PageBook pgBook_sampleBtnLbl;
	private PageBook pgBook_flatBtnLbl;
	private Label lblFlat;
	private Composite pgLblFlatCmp;
	private Composite pgBtnFlatCmp;

	private PageBook pgBook_flatDark;
	private Composite pg_flatDark_Blank;
	private Composite pg_flatDark_Buttons;

	private boolean ampFactor = false;

	public enum SAMPLE_OR_FLAT {
		SAMPLE, FLAT;
	}

	public SAMPLE_OR_FLAT getStreamState() {
		return streamState;
	}

	public synchronized void setStreamState(SAMPLE_OR_FLAT streamState) {
		this.streamState = streamState;
	}

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
		gl.verticalSpacing = 1;
		gl.marginHeight = 2;
		leftPanelComposite.setLayout(gl);
		leftPanelComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		btnStream = toolkit.createButton(leftPanelComposite, STREAM, SWT.PUSH);
		btnStream.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnStream.addSelectionListener(buttonSelectionListener);

		btnSingle = toolkit.createButton(leftPanelComposite, SINGLE, SWT.PUSH);
		btnSingle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSingle.addSelectionListener(buttonSelectionListener);

		btnFastPreview = toolkit.createButton(leftPanelComposite, FAST_PREVIEW, SWT.PUSH);
		btnFastPreview.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFastPreview.addSelectionListener(buttonSelectionListener);

		Composite zoomBorderCmp = toolkit.createComposite(leftPanelComposite);
		zoomBorderCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.marginHeight = 2;
		zoomBorderCmp.setLayout(gl);
		zoomBorderCmp.setBackground(ColorConstants.black);

		zoomComposite = new ZoomButtonComposite(zoomBorderCmp, toolkit);
		zoomComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnHistogram = toolkit.createButton(leftPanelComposite, "Histogram", SWT.PUSH);
		btnHistogram.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnHistogram.addSelectionListener(buttonSelectionListener);

		btnProfile = toolkit.createButton(leftPanelComposite, "Profile", SWT.PUSH);
		btnProfile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnProfile.addSelectionListener(buttonSelectionListener);

		btnSaturation = toolkit.createButton(leftPanelComposite, "Saturation", SWT.PUSH);
		btnSaturation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSaturation.addSelectionListener(buttonSelectionListener);

		btnCrossHair = toolkit.createButton(leftPanelComposite, "Crosshair", SWT.PUSH);
		btnCrossHair.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCrossHair.addSelectionListener(buttonSelectionListener);

		Composite flatDarkCmp = toolkit.createComposite(leftPanelComposite);
		flatDarkCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.verticalSpacing = 1;
		flatDarkCmp.setLayout(gl);

		Composite cmpHorizontalSeparator = toolkit.createComposite(flatDarkCmp);
		cmpHorizontalSeparator.setLayout(new GridLayout());
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.heightHint = 2;
		cmpHorizontalSeparator.setLayoutData(layoutData2);
		cmpHorizontalSeparator.setBackground(ColorConstants.black);

		Label lblSampleStage = toolkit.createLabel(flatDarkCmp, "SAMPLE STAGE", SWT.WRAP | SWT.CENTER);
		lblSampleStage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblSampleStage.setFont(fontRegistry.get(BOLD_TEXT_10));

		Composite cmpSampleStageInOut = toolkit.createComposite(flatDarkCmp);
		cmpSampleStageInOut.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl1 = new GridLayout(2, false);
		setDefaultLayoutSettings(gl1);
		cmpSampleStageInOut.setLayout(gl1);

		btnSampleIn = toolkit.createButton(cmpSampleStageInOut, "IN", SWT.WRAP);
		btnSampleIn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleIn.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleIn.setFont(fontRegistry.get(NORMAL_TEXT_7));
		ButtonSelectionUtil.decorateControlButton(btnSampleIn);

		btnSampleOut = toolkit.createButton(cmpSampleStageInOut, "OUT", SWT.WRAP);
		btnSampleOut.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleOut.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleOut.setFont(fontRegistry.get(NORMAL_TEXT_7));
		ButtonSelectionUtil.decorateControlButton(btnSampleOut);

		cmpHorizontalSeparator = toolkit.createComposite(flatDarkCmp);
		cmpHorizontalSeparator.setLayout(new GridLayout());
		layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.heightHint = 2;
		cmpHorizontalSeparator.setLayoutData(layoutData2);
		cmpHorizontalSeparator.setBackground(ColorConstants.black);

		btnTakeFlatAndDark = toolkit.createButton(flatDarkCmp, "Take Flat && Dark", SWT.PUSH | SWT.WRAP);
		btnTakeFlatAndDark.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnTakeFlatAndDark.addSelectionListener(buttonSelectionListener);

		pgBook_flatDark = new PageBook(flatDarkCmp, SWT.None);

		pg_flatDark_Blank = toolkit.createComposite(pgBook_flatDark);
		pg_flatDark_Blank.setLayout(new GridLayout());
		Label lblFlatDarkNotTaken = toolkit.createLabel(pg_flatDark_Blank, "Flat and Dark images not taken yet.",
				SWT.CENTER | SWT.WRAP);
		lblFlatDarkNotTaken.setLayoutData(new GridData(GridData.FILL_BOTH));

		pg_flatDark_Buttons = toolkit.createComposite(pgBook_flatDark);
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		gl.verticalSpacing = 1;
		pg_flatDark_Buttons.setLayout(gl);

		btnFlatDarkCorrection = toolkit.createButton(pg_flatDark_Buttons, "Correct Flat && Dark", SWT.PUSH | SWT.WRAP);
		btnFlatDarkCorrection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFlatDarkCorrection.addSelectionListener(buttonSelectionListener);
		int heightHint = btnFlatDarkCorrection.computeSize(SWT.DEFAULT, 60).y;

		btnFlatShow = toolkit.createButton(pg_flatDark_Buttons, SHOW_FLAT, SWT.PUSH | SWT.WRAP);
		btnFlatShow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFlatShow.addSelectionListener(buttonSelectionListener);
		heightHint += btnFlatShow.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		btnDarkShow = toolkit.createButton(pg_flatDark_Buttons, SHOW_DARK, SWT.PUSH | SWT.WRAP);
		btnDarkShow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnDarkShow.addSelectionListener(buttonSelectionListener);
		heightHint += btnDarkShow.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = heightHint;

		pgBook_flatDark.setLayoutData(layoutData);
		pgBook_flatDark.showPage(pg_flatDark_Blank);

		cmpHorizontalSeparator = toolkit.createComposite(flatDarkCmp);
		cmpHorizontalSeparator.setLayout(new GridLayout());
		layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.heightHint = 2;
		cmpHorizontalSeparator.setLayoutData(layoutData2);
		cmpHorizontalSeparator.setBackground(ColorConstants.black);

		Label lblExposureTime = toolkit.createLabel(leftPanelComposite, EXPOSURE_TIME_lbl, SWT.WRAP | SWT.CENTER);
		lblExposureTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblExposureTime.setFont(fontRegistry.get(BOLD_TEXT_10));

		pgBook_sampleBtnLbl = new PageBook(leftPanelComposite, SWT.None);

		pgBtnSampleCmp = toolkit.createComposite(pgBook_sampleBtnLbl);
		pgBtnSampleCmp.setLayout(new FillLayout());

		btnSample = toolkit.createButton(pgBtnSampleCmp, SAMPLE_lbl, SWT.WRAP | SWT.CENTER);
		btnSample.addSelectionListener(buttonSelectionListener);
		ButtonSelectionUtil.setButtonSelected(btnSample);

		pgLblSampleCmp = toolkit.createComposite(pgBook_sampleBtnLbl);
		GridLayout gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		pgLblSampleCmp.setLayout(gridLayout);

		lblSample = toolkit.createLabel(pgLblSampleCmp, SAMPLE_lbl, SWT.CENTER);
		lblSample.setFont(fontRegistry.get(BOLD_TEXT_10));
		lblSample.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_BOTH));

		pgBook_sampleBtnLbl.showPage(pgLblSampleCmp);

		GridData layoutData1 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData1.heightHint = btnSample.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		pgBook_sampleBtnLbl.setLayoutData(layoutData1);

		//
		Composite cmpSampleExpTime = toolkit.createComposite(leftPanelComposite);
		cmpSampleExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.verticalSpacing = 0;
		cmpSampleExpTime.setLayout(layout);

		txtSampleExposureTime = toolkit.createText(cmpSampleExpTime, BLANK, SWT.WRAP | SWT.CENTER | SWT.SINGLE);
		txtSampleExposureTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSampleExposureTime.addKeyListener(textboxKeyListener);
		txtSampleExposureTime.addFocusListener(focusListener);

		toolkit.createLabel(cmpSampleExpTime, SECONDS_lbl, SWT.WRAP | SWT.CENTER).setLayoutData(new GridData());

		pgBook_flatBtnLbl = new PageBook(leftPanelComposite, SWT.None);
		pgBtnFlatCmp = toolkit.createComposite(pgBook_flatBtnLbl);
		pgBtnFlatCmp.setLayout(new FillLayout());

		btnFlat = toolkit.createButton(pgBtnFlatCmp, FLAT_lbl, SWT.WRAP | SWT.CENTER);
		btnFlat.addSelectionListener(buttonSelectionListener);
		btnFlat.setEnabled(false);

		pgLblFlatCmp = toolkit.createComposite(pgBook_flatBtnLbl);
		gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		pgLblFlatCmp.setLayout(gridLayout);

		lblFlat = toolkit.createLabel(pgLblFlatCmp, FLAT_lbl, SWT.CENTER);
		lblFlat.setFont(fontRegistry.get(BOLD_TEXT_10));
		lblFlat.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_BOTH));

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = btnFlat.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		pgBook_flatBtnLbl.setLayoutData(layoutData);

		pgBook_sampleBtnLbl.showPage(pgLblSampleCmp);
		pgBook_flatBtnLbl.showPage(pgLblFlatCmp);

		//
		Composite cmpFlatExpTime = toolkit.createComposite(leftPanelComposite);
		cmpFlatExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.verticalSpacing = 0;
		cmpFlatExpTime.setLayout(layout);

		txtFlatExpTime = toolkit.createText(cmpFlatExpTime, BLANK, SWT.WRAP | SWT.CENTER | SWT.SINGLE);
		txtFlatExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFlatExpTime.setEnabled(false);
		txtFlatExpTime.addKeyListener(textboxKeyListener);
		txtFlatExpTime.addFocusListener(focusListener);

		toolkit.createLabel(cmpFlatExpTime, SECONDS_lbl, SWT.WRAP | SWT.CENTER).setLayoutData(new GridData());

		btnDifferentFlatExpTime = toolkit.createButton(leftPanelComposite, DIFFERENT_FLAT_EXPOSURE_TIME_lbl, SWT.PUSH
				| SWT.CENTER | SWT.WRAP);
		btnDifferentFlatExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnDifferentFlatExpTime.setFont(fontRegistry.get(NORMAL_TEXT_7));
		btnDifferentFlatExpTime.addSelectionListener(buttonSelectionListener);

		leftPanelListeners = new ArrayList<ITomoAlignmentLeftPanelListener>();
	}

	private KeyAdapter textboxKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			// Sample exposure time
			if (e.getSource().equals(txtSampleExposureTime)) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtSampleExposureTime.getText())) {

						sampleExposureTime = Double.parseDouble(txtSampleExposureTime.getText());
						txtSampleExposureTime.setText(String.format(EXPOSURE_TIME_FORMAT, sampleExposureTime));
						try {
							for (ITomoAlignmentLeftPanelListener leftPanelLis : leftPanelListeners) {
								leftPanelLis.sampleExposureTimeChanged(sampleExposureTime);
							}

							btnSample.setFocus();
							// If the flat exposure time is expected to be the same as the sample exposure time
							if (!isDifferentFlatExposureTime) {
								txtFlatExpTime.setText(txtSampleExposureTime.getText());
								flatExposureTime = sampleExposureTime;
								for (ITomoAlignmentLeftPanelListener leftPanelLis : leftPanelListeners) {
									leftPanelLis.flatExposureTimeChanged(flatExposureTime);
								}
							}
						} catch (Exception e1) {
							logger.debug("Error setting exposure time", e1);
						}
					} else {
						showErrorDialog(new IllegalArgumentException(INVALID_VALUE));
						txtSampleExposureTime.setText(String.format(EXPOSURE_TIME_FORMAT, sampleExposureTime));
					}
				}
			} else if (e.getSource().equals(txtFlatExpTime)) {
				// Flat exposure time
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtFlatExpTime.getText())) {
						flatExposureTime = Double.parseDouble(txtFlatExpTime.getText());
						txtFlatExpTime.setText(String.format(EXPOSURE_TIME_FORMAT, flatExposureTime));
						try {
							for (ITomoAlignmentLeftPanelListener leftPanelLis : leftPanelListeners) {
								leftPanelLis.flatExposureTimeChanged(Double.parseDouble(txtFlatExpTime.getText()));
							}
						} catch (Exception e1) {
							logger.error("Error setting exposure time", e1);
						}
						btnFlat.setFocus();
					} else {
						showErrorDialog(new IllegalArgumentException(INVALID_VALUE));
						txtFlatExpTime.setText(String.format(EXPOSURE_TIME_FORMAT, flatExposureTime));
					}
				}
			}
		}
	};

	@SuppressWarnings("rawtypes")
	private boolean isValid(Class cls, String expTime) {
		String expr = null;
		if (Double.class.equals(cls)) {
			expr = "(\\d)*.?(\\d)*";
		} else if (Integer.class.equals(cls)) {
			expr = "(\\d)*";
		}
		if (expr != null) {
			if (expTime == null || expTime.length() < 1) {
				return false;
			}

			if (!expTime.matches(expr)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtSampleExposureTime)) {
				logger.debug("sample exposure time focus lost");
				txtSampleExposureTime.setText(String.format(EXPOSURE_TIME_FORMAT, sampleExposureTime));
			} else if (focusEvent.getSource().equals(txtFlatExpTime)) {
				logger.debug("flat exposure time focus lost");
				txtFlatExpTime.setText(String.format(EXPOSURE_TIME_FORMAT, flatExposureTime));
			}
		}
	};

	public void addLeftPanelListener(ITomoAlignmentLeftPanelListener listener) {
		leftPanelListeners.add(listener);
		zoomComposite.addZoomButtonActionListener(listener);
	}

	public void removeLeftPanelListener(ITomoAlignmentLeftPanelListener listener) {
		leftPanelListeners.remove(listener);
		zoomComposite.removeZoomButtonActionListener(listener);
	}

	private void initializeFontRegistry() {
		if (getDisplay() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_10, new FontData[] { new FontData(fontName, 10, SWT.BOLD) });
			fontRegistry.put(NORMAL_TEXT_7, new FontData[] { new FontData(fontName, 8, SWT.NORMAL) });
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
		if (!this.isDisposed()) {
			this.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						deselectStreamButton();
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
		ButtonSelectionUtil.setControlButtonSelected(btnSampleOut);
		ButtonSelectionUtil.setControlButtonDeselected(btnSampleIn);
	}

	public void selectSampleInButton() {
		ButtonSelectionUtil.setControlButtonSelected(btnSampleIn);
		ButtonSelectionUtil.setControlButtonDeselected(btnSampleOut);
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
			deselectStreamButton();
			throw new Exception("Unable to start streaming:", ex);
		}
	}

	// Stream options
	public void selectStreamButton() {
		ButtonSelectionUtil.setButtonSelected(btnStream);
		streamButtonSelected = true;
	}

	/**
	 * @return true if saturation button is selected else returns false.
	 */
	public boolean isProfileSelected() {
		return ButtonSelectionUtil.isButtonSelected(btnProfile);
	}

	// TODO - Fix Ravi
	public void enableAll() {

	}

	public void disableAll() {

	}

	/**
	 * selection listener for the buttons
	 */
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			Object sourceObj = event.getSource();
			if (sourceObj == btnSample) {
				if (streamState != SAMPLE_OR_FLAT.SAMPLE) {
					selectSampleButton();
				}
			} else if (sourceObj == btnFlat) {
				if (streamState != SAMPLE_OR_FLAT.FLAT) {
					ButtonSelectionUtil.setButtonSelected(btnFlat);
					ButtonSelectionUtil.setButtonDeselected(btnSample);
					try {

						for (ITomoAlignmentLeftPanelListener lis : leftPanelListeners) {
							lis.exposureStateChanged(SAMPLE_OR_FLAT.FLAT);
						}
						streamState = SAMPLE_OR_FLAT.FLAT;
					} catch (Exception e1) {
						logger.error("Error setting state", e1);
					}
				}
			} else if (sourceObj == btnStream) {
				if (!ButtonSelectionUtil.isButtonSelected(btnStream)) {
					logger.debug("'Stream' is selected");
					/**/
					try {
						startStreaming();
					} catch (Exception e) {
						showErrorDialog(e);
						logger.error("exception selecting stream", e);
					}
					ButtonSelectionUtil.setButtonSelected(btnStream);
				} else {
					logger.debug("'Stream' is de-selected");
					stopStream();
				}
			} else if (sourceObj == btnHistogram) {
				if (!ButtonSelectionUtil.isButtonSelected(btnHistogram)) {
					startHistogram();
				} else {
					logger.debug("'btnHistogram' is de-selected");
					stopHistogram();
				}
			} else if (sourceObj == btnSingle) {
				logger.debug("'single' is selected");
				try {
					for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
						lpl.single(isFlatCorrectionSelected());
					}
				} catch (Exception e) {
					showErrorDialog(e);
					logger.error("single capturing has problems", e);
				}
			} else if (sourceObj == btnFastPreview) {
				if (!ButtonSelectionUtil.isButtonSelected(btnFastPreview)) {
					switchOnFastPreview();
				} else {
					switchOffFastPreview();
				}
			} else if (sourceObj == btnSaturation) {
				if (!ButtonSelectionUtil.isButtonSelected(btnSaturation)) {
					logger.debug("'Saturation' is selected");
					ButtonSelectionUtil.setButtonSelected(btnSaturation);
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.saturation(true);
						}
					} catch (Exception e) {
						ButtonSelectionUtil.setButtonDeselected(btnSaturation);
						showErrorDialog(e);
					}
				} else {
					logger.debug("'Saturation' is de-selected");
					ButtonSelectionUtil.setButtonDeselected(btnSaturation);
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.saturation(false);
						}
					} catch (Exception e) {
						ButtonSelectionUtil.setButtonDeselected(btnSaturation);
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
					showErrorDialog(ex);
				} catch (InterruptedException e) {
					showErrorDialog(e);
				}
			} else if (sourceObj == btnFlatDarkCorrection) {
				if (!ButtonSelectionUtil.isButtonSelected(btnFlatDarkCorrection)) {
					ButtonSelectionUtil.setButtonSelected(btnFlatDarkCorrection);
					logger.debug("'Flat Correction' is selected");
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.correctFlatAndDark(true);
						}
					} catch (Exception ex) {
						ButtonSelectionUtil.setButtonDeselected(btnFlatDarkCorrection);
					}
				} else {
					ButtonSelectionUtil.setButtonDeselected(btnFlatDarkCorrection);
					logger.debug("'Flat Correction' is de-selected");
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.correctFlatAndDark(false);
						}
					} catch (Exception ex) {
						ButtonSelectionUtil.setButtonDeselected(btnFlatDarkCorrection);
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
				if (!ButtonSelectionUtil.isButtonSelected(btnProfile)) {
					logger.debug("'Profile' is selected");
					stopStream();
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.profile(true);
						}
					} catch (Exception ex) {
						ButtonSelectionUtil.setButtonDeselected(btnProfile);
						showErrorDialog(ex);
					}
					ButtonSelectionUtil.setButtonSelected(btnProfile);
				} else {
					try {
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.profile(false);
						}
					} catch (Exception e) {
						logger.error("Error Profiling ;{}", e.getMessage());
					}
					ButtonSelectionUtil.setButtonDeselected(btnProfile);
				}
			} else if (sourceObj == btnCrossHair) {
				if (!ButtonSelectionUtil.isButtonSelected(btnCrossHair)) {
					logger.debug("'btnCrossHair' is selected");
					try {
						ButtonSelectionUtil.setButtonSelected(btnCrossHair);
						for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
							lpl.crosshair(true);
						}
					} catch (Exception ex) {
						ButtonSelectionUtil.setButtonDeselected(btnCrossHair);
						showErrorDialog(ex);
					}
				} else {
					switchOffCrosshair();
				}

			} else if (sourceObj == btnDifferentFlatExpTime) {
				if (!ButtonSelectionUtil.isButtonSelected(btnDifferentFlatExpTime)) {
					logger.debug("'btnDifferentFlatExpTime' is selected");
					ButtonSelectionUtil.setButtonSelected(btnDifferentFlatExpTime);
					btnFlat.setEnabled(true);
					txtFlatExpTime.setEnabled(true);
					isDifferentFlatExposureTime = true;

					pgBook_sampleBtnLbl.showPage(pgBtnSampleCmp);
					pgBook_flatBtnLbl.showPage(pgBtnFlatCmp);
				} else {
					logger.debug("'btnDifferentFlatExpTime' is deselected");
					ButtonSelectionUtil.setButtonDeselected(btnDifferentFlatExpTime);
					btnFlat.setEnabled(false);
					txtFlatExpTime.setEnabled(false);
					if (!txtFlatExpTime.getText().equals(txtSampleExposureTime.getText())) {
						txtFlatExpTime.setText(txtSampleExposureTime.getText());
						try {
							for (ITomoAlignmentLeftPanelListener leftPanelLis : leftPanelListeners) {
								leftPanelLis.flatExposureTimeChanged(Double.parseDouble(txtFlatExpTime.getText()));
							}
						} catch (Exception e) {
							showErrorDialog(e);
						}
					}
					if (ButtonSelectionUtil.isButtonSelected(btnFlat)) {
						selectSampleButton();
					}
					pgBook_sampleBtnLbl.showPage(pgLblSampleCmp);
					pgBook_flatBtnLbl.showPage(pgLblFlatCmp);
					isDifferentFlatExposureTime = false;
				}
			}
		}

	};

	private void switchOffFastPreview() {
		ButtonSelectionUtil.setButtonDeselected(btnFastPreview);
		logger.debug("'fast preview' is selected");
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.fastPreview(false, isFlatCorrectionSelected());
			}
		} catch (Exception e) {
			showErrorDialog(e);
			logger.error("single capturing has problems", e);
		}
		ampFactor = false;
	}

	private void switchOnFastPreview() {
		ButtonSelectionUtil.setButtonSelected(btnFastPreview);
		logger.debug("'fast preview' is selected");
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.fastPreview(true, isFlatCorrectionSelected());
			}
		} catch (Exception e) {
			showErrorDialog(e);
			logger.error("single capturing has problems", e);
		}
		ampFactor = true;
	}

	private void selectSampleButton() {
		ButtonSelectionUtil.setButtonSelected(btnSample);
		ButtonSelectionUtil.setButtonDeselected(btnFlat);
		try {

			for (ITomoAlignmentLeftPanelListener lis : leftPanelListeners) {
				lis.exposureStateChanged(SAMPLE_OR_FLAT.SAMPLE);
			}
			streamState = SAMPLE_OR_FLAT.SAMPLE;
		} catch (Exception e1) {
			logger.error("Error setting state", e1);
		}
	}

	public void setPreferredFlatExposureTime(final double preferredFlatExposureTime) {
		flatExposureTime = preferredFlatExposureTime;
		if (txtFlatExpTime != null && !txtFlatExpTime.isDisposed()) {
			txtFlatExpTime.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					txtFlatExpTime.setText(String.format(EXPOSURE_TIME_FORMAT, preferredFlatExposureTime));
				}
			});
		}
	}

	public void setPreferredSampleExposureTime(final double preferredExposureTime) {
		sampleExposureTime = preferredExposureTime;
		if (txtSampleExposureTime != null && !txtSampleExposureTime.isDisposed()) {
			txtSampleExposureTime.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					txtSampleExposureTime.setText(String.format(EXPOSURE_TIME_FORMAT, preferredExposureTime));
				}
			});
		}
		if (!isDifferentFlatExposureTime) {
			setPreferredFlatExposureTime(preferredExposureTime);
		}
	}

	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	/**
	 * listener for buttons and slider - but this is control masked - the "Ctrl" key needs to be pressed
	 */
	private Listener ctrlMouseListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			// to check whether the control key is pressend along with the mouse button click
			if (event.stateMask == SWT.CTRL) {
				Object sourceObj = event.widget;
				if (btnSampleIn.equals(sourceObj)) {
					if (!ButtonSelectionUtil.isCtrlButtonSelected(btnSampleIn)) {
						logger.debug("'btnSampleIn' is selected");
						selectSampleInButton();
						try {
							for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
								lpl.moveSampleIn();
							}
						} catch (IllegalStateException s) {
							showErrorDialog(s);
							selectSampleOutButton();
						} catch (Exception e1) {
							showErrorDialog(e1);
							selectSampleOutButton();
						}
					}
				} else if (btnSampleOut.equals(sourceObj)) {
					if (!ButtonSelectionUtil.isCtrlButtonSelected(btnSampleOut)) {
						logger.debug("'btnSampleOut' is selected");
						logger.debug("'btnSampleIn' is selected");
						selectSampleOutButton();
						try {
							for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
								lpl.moveSampleOut();
							}
						} catch (IllegalStateException s) {
							showErrorDialog(s);
							selectSampleInButton();
						} catch (Exception e1) {
							showErrorDialog(e1);
							selectSampleInButton();
						}
					}
				}
			}
		}
	};

	public void startHistogram() {
		logger.debug("'btnSampleHistogram' is selected");
		ButtonSelectionUtil.setButtonSelected(btnHistogram);
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.histogram(true);
			}
		} catch (Exception e1) {
			logger.debug("Error starting histogram", e1);
			showErrorDialog(e1);
		}
	}

	public void stopHistogram() {
		if (ButtonSelectionUtil.isButtonSelected(btnHistogram)) {
			ButtonSelectionUtil.setButtonDeselected(btnHistogram);
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				try {
					lpl.histogram(false);
				} catch (Exception e) {
					logger.error("Error switching off histogram", e);
					showErrorDialog(e);
				}
			}
		}

	}

	public void deselectProfileButton() {
		ButtonSelectionUtil.setButtonDeselected(btnProfile);
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
		return isFlatDarkTaken() && ButtonSelectionUtil.isButtonSelected(btnFlatDarkCorrection);
	}

	/**
	 * Deselects both the streams
	 */
	public void deselectStreamButton() {
		ButtonSelectionUtil.setButtonDeselected(btnStream);
		streamButtonSelected = false;
	}

	private void showErrorDialog(Exception ex) {
		MessageDialog.openError(getShell(), "Error during alignment", ex.getMessage());
	}

	public void setFlatFieldCorrection(final boolean enabled) {
		if (!this.isDisposed()) {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (enabled) {
						ButtonSelectionUtil.setButtonSelected(btnFlatDarkCorrection);
					} else {
						ButtonSelectionUtil.setButtonDeselected(btnFlatDarkCorrection);
					}
				}
			});
		}
	}

	/**
	 * To be called only if the saturation button needs to be show as de-selected
	 */
	public void deSelectSaturationButton() {
		ButtonSelectionUtil.setButtonDeselected(btnSaturation);
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
		return ButtonSelectionUtil.isButtonSelected(btnSaturation);
	}

	public boolean isStreamButtonSelected() {
		return streamButtonSelected;
	}

	public void switchOffCrosshair() {
		try {
			for (ITomoAlignmentLeftPanelListener lpl : leftPanelListeners) {
				lpl.crosshair(false);
			}
			ButtonSelectionUtil.setButtonDeselected(btnCrossHair);
		} catch (Exception e) {
			logger.error("Error displaying crosshair ;{}", e.getMessage());
		}
	}

	public boolean isAmplified() {
		return ampFactor;
	}

	public void flatDarkTaken(final boolean flatDarkTaken) {

		this.flatDarkTaken = flatDarkTaken;
		if (pgBook_flatDark != null && !pgBook_flatDark.isDisposed()) {
			pgBook_flatDark.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (flatDarkTaken) {
						pgBook_flatDark.showPage(pg_flatDark_Buttons);
					} else {
						pgBook_flatDark.showPage(pg_flatDark_Blank);
					}
				}
			});
		}

	}

	public boolean isFlatDarkTaken() {
		return flatDarkTaken;
	}

}

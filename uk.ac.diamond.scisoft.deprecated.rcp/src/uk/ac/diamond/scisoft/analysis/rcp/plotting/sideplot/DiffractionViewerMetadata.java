/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import java.text.DecimalFormat;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;


/**
 * Composite that that shows the metadata of the experiment
 */
public class DiffractionViewerMetadata extends Composite {

	private Map<String, ? extends Object> metadata;
	public static int NUM_DEC_PLACES = 4;
	public static double scale = Math.pow(10.0, NUM_DEC_PLACES);
	private Text detectorSizeX;
	private Text detectorSizeY;
	private Text PixelSizeX;
	private Text PixelSizeY;
	private Spinner wavelength;
	private Text phiStart;

	private DiffractionViewer diffView;
	private Text phiStop;
	private Text phiRange;
	private Spinner distanceToDetector;

	private Text ExposureTime;
	private Text maxPxVal;

	private Text minPxVal;

	private Text meanPxVal;
	private Button showBeam;
	private Spinner xBeam;
	private Spinner yBeam;

	private static double arrowMovement = 1;
	public Vector3d beamCorrection;
	private DetectorProperties cachedDetConfig;
	private DiffractionCrystalEnvironment cachedDiffEnv;
	private Button save;

	private int zBeamValue;
	private int xBeamValue;
	private int yBeamValue;

	private boolean lockSettings = false;
	private Text overload;
	private DecimalFormat decimaPlaces;
	public static boolean CACHED_METADATA;
	public static boolean GUI_LOADED = false;

	public enum beamMovement {
		BEAM_LEFT, BEAM_UP, BEAM_RIGHT, BEAM_DOWN, BEAM_DISTANCE
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param diffractionViewer
	 */

	@SuppressWarnings("unused")
	public DiffractionViewerMetadata(Composite parent, int style, DiffractionViewer diffractionViewer) {
		super(parent, style);
		this.diffView = diffractionViewer;

		this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		this.setLayout(new GridLayout(1, false));

		ScrolledComposite scrComp = new ScrolledComposite(this, SWT.HORIZONTAL | SWT.VERTICAL);
		scrComp.setLayoutData(this.getLayoutData());
		scrComp.setLayout(this.getLayout());

		Composite comp = new Composite(scrComp, SWT.FILL);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		comp.setLayout(new GridLayout(2, false));

		Group experimentmetadata = new Group(comp, SWT.NONE);
		experimentmetadata.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		experimentmetadata.setLayout(new GridLayout(3, false));
		experimentmetadata.setText("Experimental Information");

		{
			Label lblWavelength = new Label(experimentmetadata, SWT.NONE);
			lblWavelength.setText("Wavelength");
		}
		{
			wavelength = new Spinner(experimentmetadata, SWT.NONE);
			wavelength.setToolTipText("wavelength");
			wavelength.setDigits(NUM_DEC_PLACES);
			wavelength.setMaximum(1000000);
			wavelength.setMinimum(0);
			wavelength.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (GUI_LOADED) {
						cacheMetadata();
						modifyWavelength();
					}
				}
			});
		}
		new Label(experimentmetadata, SWT.NONE).setText("\u00c5");
		{
			Label lblStart = new Label(experimentmetadata, SWT.NONE);
			lblStart.setText("Start");
		}
		{
			phiStart = new Text(experimentmetadata, SWT.READ_ONLY);
			phiStart.setBackground(experimentmetadata.getBackground());
			//phiStart.setUnit("\u00B0");
			//phiStart.setDecimalPlaces(NUM_DEC_PLACES);
		}
		new Label(experimentmetadata, SWT.NONE).setText("\u00B0");
		{
			Label lblStop = new Label(experimentmetadata, SWT.NONE);
			lblStop.setText("Stop");
		}
		{
			phiStop = new Text(experimentmetadata, SWT.READ_ONLY);
			phiStop.setBackground(experimentmetadata.getBackground());
			//phiStop.setUnit("\u00B0");
			//phiStop.setDecimalPlaces(NUM_DEC_PLACES);

		}
		new Label(experimentmetadata, SWT.NONE).setText("\u00B0");
		{
			Label lblOscillationRange = new Label(experimentmetadata, SWT.NONE);
			lblOscillationRange.setText("Oscillation Range");
		}
		{
			phiRange = new Text(experimentmetadata, SWT.READ_ONLY);
			phiRange.setBackground(experimentmetadata.getBackground());
			//phiRange.setUnit("\u00B0");
			//phiRange.setDecimalPlaces(NUM_DEC_PLACES);
		}
		new Label(experimentmetadata, SWT.NONE).setText("\u00B0");

		Group detectorMetadata = new Group(comp, SWT.NONE);
		detectorMetadata.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));
		detectorMetadata.setLayout(new GridLayout(3, false));
		detectorMetadata.setText("Detector Metadata");
		{
			Label lblDistance = new Label(detectorMetadata, SWT.NONE);
			lblDistance.setText("Distance");
		}
		{
			distanceToDetector = new Spinner(detectorMetadata, SWT.NONE);
			distanceToDetector.setToolTipText("distance");
			distanceToDetector.setDigits(NUM_DEC_PLACES);
			distanceToDetector.setMaximum(100000000);
			distanceToDetector.setMinimum(0);
			distanceToDetector.setIncrement(100);
			distanceToDetector.setSelection(0);
			distanceToDetector.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (GUI_LOADED) {
						cacheMetadata();
						moveDetectorOrigin(beamMovement.BEAM_DISTANCE, distanceToDetector.getSelection());
					}
				}
			});
		}
		new Label(detectorMetadata, SWT.NONE).setText("mm");
		{
			Label lblSizex = new Label(detectorMetadata, SWT.NONE);
			lblSizex.setText("Size (x)");
		}
		{
			detectorSizeX = new Text(detectorMetadata, SWT.READ_ONLY);
			detectorSizeX.setBackground(detectorMetadata.getBackground());
			//detectorSizeX.setUnit("mm ");
		}
		new Label(detectorMetadata, SWT.NONE).setText("mm");
		{
			Label lblSizey = new Label(detectorMetadata, SWT.NONE);
			lblSizey.setText("Size (y)");
		}
		{
			detectorSizeY = new Text(detectorMetadata, SWT.READ_ONLY);
			detectorSizeY.setBackground(detectorMetadata.getBackground());
			//detectorSizeY.setUnit("mm ");
		}
		new Label(detectorMetadata, SWT.NONE).setText("mm");
		{
			Label lblPixelSizex = new Label(detectorMetadata, SWT.NONE);
			lblPixelSizex.setText("Pixel Size (x)");
		}
		{
			PixelSizeX = new Text(detectorMetadata, SWT.READ_ONLY);
			PixelSizeX.setBackground(detectorMetadata.getBackground());
			//PixelSizeX.setUnit("mm ");
		}
		new Label(detectorMetadata, SWT.NONE).setText("mm");
		{
			Label lblPixelSizey = new Label(detectorMetadata, SWT.NONE);
			lblPixelSizey.setText("Pixel Size (y)");
		}
		{
			PixelSizeY = new Text(detectorMetadata, SWT.READ_ONLY);
			PixelSizeY.setBackground(detectorMetadata.getBackground());
			//PixelSizeY.setUnit("mm ");
		}
		new Label(detectorMetadata, SWT.NONE).setText("mm");
		{
			Label lblTime = new Label(detectorMetadata, SWT.NONE);
			lblTime.setText("Exposure Time");
		}
		{
			ExposureTime = new Text(detectorMetadata, SWT.READ_ONLY);
			ExposureTime.setBackground(detectorMetadata.getBackground());
			//ExposureTime.setUnit("s");
		}
		new Label(detectorMetadata, SWT.NONE).setText("s");
		{
			Label lblMaxPxVal = new Label(detectorMetadata, SWT.NONE);
			lblMaxPxVal.setText("Maximum Value");
			lblMaxPxVal.setToolTipText("Maximum pixel value of the dataset being plotted.");
		}
		{
			maxPxVal = new Text(detectorMetadata, SWT.READ_ONLY);
			maxPxVal.setBackground(detectorMetadata.getBackground());
		}
		new Label(detectorMetadata, SWT.NONE);
		{
			Label lblMinPxVal = new Label(detectorMetadata, SWT.NONE);
			lblMinPxVal.setText("Minimum Value");
			lblMinPxVal.setToolTipText("Minimum pixel value of the dataset being plotted.");
		}
		{
			minPxVal = new Text(detectorMetadata, SWT.READ_ONLY);
			minPxVal.setBackground(detectorMetadata.getBackground());
		}
		new Label(detectorMetadata, SWT.NONE);
		{
			Label lblMeanPxVal = new Label(detectorMetadata, SWT.NONE);
			lblMeanPxVal.setText("Mean Value");
			lblMeanPxVal.setToolTipText("Mean pixel value of the dataset being plotted.");
		}
		{
			meanPxVal = new Text(detectorMetadata, SWT.READ_ONLY);
			meanPxVal.setBackground(detectorMetadata.getBackground());
		}
		new Label(detectorMetadata, SWT.NONE);
		{
			Label lblThreashold = new Label(detectorMetadata, SWT.NONE);
			lblThreashold.setText("Overload Value");
			lblThreashold.setToolTipText("Displays the maximum possible pixel value");
		}
		{
			overload = new Text(detectorMetadata, SWT.READ_ONLY);
			overload.setBackground(detectorMetadata.getBackground());
		}
		
		new Label(detectorMetadata, SWT.NONE);
		{
			Group grpBeamCentreControls = new Group(comp, SWT.NONE);
			grpBeamCentreControls.setLayout(new GridLayout(2, false));
			grpBeamCentreControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			grpBeamCentreControls.setText("Beam Centre");
			{
				Composite beamCentreControls = new Composite(grpBeamCentreControls, SWT.NONE);
				beamCentreControls.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
				beamCentreControls.setLayout(new GridLayout(3, false));
				new Label(beamCentreControls, SWT.NONE);
				{
					Button upBeam = new Button(beamCentreControls, SWT.NONE);
					upBeam.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							cacheMetadata();
							if ((e.stateMask & SWT.CTRL) != 0)
								moveDetectorOrigin(beamMovement.BEAM_UP, arrowMovement / 10);
							else
								moveDetectorOrigin(beamMovement.BEAM_UP, arrowMovement);
						}
					});
					upBeam.setImage(AnalysisRCPActivator.getImageDescriptor("/icons/arrow_up.png").createImage());
				}
				new Label(beamCentreControls, SWT.NONE);
				{
					Button leftBeam = new Button(beamCentreControls, SWT.NONE);
					leftBeam.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							cacheMetadata();
							if ((e.stateMask & SWT.CTRL) != 0)
								moveDetectorOrigin(beamMovement.BEAM_LEFT, arrowMovement / 10);
							else
								moveDetectorOrigin(beamMovement.BEAM_LEFT, arrowMovement);
						}
					});
					leftBeam.setImage(AnalysisRCPActivator.getImageDescriptor("/icons/arrow_left.png").createImage());
				}
				{
					showBeam = new Button(beamCentreControls, SWT.TOGGLE);
					showBeam.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							diffView.drawBeamCentre(showBeam.getSelection());
							diffView.beamVisible = showBeam.getSelection();
						}
					});
					showBeam.setToolTipText("Show beam centre");
					showBeam.setImage(AnalysisRCPActivator.getImageDescriptor("icons/asterisk_yellow.png")
							.createImage());
				}
				{
					Button rightBeam = new Button(beamCentreControls, SWT.NONE);
					rightBeam.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							cacheMetadata();
							if ((e.stateMask & SWT.CTRL) != 0)
								moveDetectorOrigin(beamMovement.BEAM_RIGHT, arrowMovement / 10);
							else
								moveDetectorOrigin(beamMovement.BEAM_RIGHT, arrowMovement);

						}
					});
					rightBeam.setImage(AnalysisRCPActivator.getImageDescriptor("/icons/arrow_right.png").createImage());
				}
				new Label(beamCentreControls, SWT.NONE);
				{
					Button downBeam = new Button(beamCentreControls, SWT.NONE);
					downBeam.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							cacheMetadata();
							if ((e.stateMask & SWT.CTRL) != 0)
								moveDetectorOrigin(beamMovement.BEAM_DOWN, arrowMovement / 10);
							else
								moveDetectorOrigin(beamMovement.BEAM_DOWN, arrowMovement);
						}
					});
					downBeam.setImage(AnalysisRCPActivator.getImageDescriptor("/icons/arrow_down.png").createImage());
				}
				new Label(beamCentreControls, SWT.NONE);

				Composite beamSpinners = new Composite(grpBeamCentreControls, SWT.FILL);
				beamSpinners.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
				beamSpinners.setLayout(new GridLayout(2, false));

				Label labXBeam = new Label(beamSpinners, SWT.NONE);
				labXBeam.setText("Beam X");

				xBeam = new Spinner(beamSpinners, SWT.NONE);
				xBeam.setToolTipText("xbeam");
				xBeam.setDigits(NUM_DEC_PLACES);
				xBeam.setMaximum(50000000);
				xBeam.setMinimum(0);
				xBeam.setIncrement((int) (scale / 10));
				xBeam.addSelectionListener(beamSpinnerListener);

				Label labYBeam = new Label(beamSpinners, SWT.NONE);
				labYBeam.setText("Beam Y");

				yBeam = new Spinner(beamSpinners, SWT.NONE);
				yBeam.setToolTipText("ybeam");
				yBeam.setDigits(NUM_DEC_PLACES);
				yBeam.setMaximum(50000000);
				yBeam.setMinimum(0);
				yBeam.setIncrement((int) (scale / 10));
				yBeam.addSelectionListener(beamSpinnerListener);
			}
		}
		{
			save = new Button(comp, SWT.TOGGLE);
			save.setText("&Lock Settings");
			save.setToolTipText("Applies the changes to metatdata to remainder of session");
			save.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					lockSettings = save.getSelection();
				}

			});
		}

		{
			Button btnReset = new Button(comp, SWT.NONE);
			btnReset.setToolTipText("Resets the metadata to the image metadata");
			btnReset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			btnReset.setText("&Reset");
			btnReset.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					retrieveSettings();
					save.setSelection(false);
				}
			});
		}
		scrComp.setContent(comp);
		final Point controlsSize = comp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		comp.setSize(controlsSize);
		decimaPlaces = new DecimalFormat();
		decimaPlaces.setMaximumFractionDigits(NUM_DEC_PLACES);
		GUI_LOADED = true;
	}

	private SelectionListener beamSpinnerListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			xBeamValue = xBeam.getSelection();
			yBeamValue = yBeam.getSelection();
			modifyDetectorOrigin();
		}
	};

	protected void modifyWavelength() {
		if (diffView.diffEnv == null)
			return;
		diffView.diffEnv.setWavelength(wavelength.getSelection() / scale);
		diffView.updateDiffractionObjects(showBeam.getSelection());

	}

	protected void moveDetectorOrigin(beamMovement movement, double dist) {

		switch (movement) {
		case BEAM_UP:
			yBeamValue -= dist * scale;
			break;
		case BEAM_DOWN:
			yBeamValue += dist * scale;
			break;
		case BEAM_LEFT:
			xBeamValue -= dist * scale;
			break;
		case BEAM_RIGHT:
			xBeamValue += dist * scale;
			break;
		case BEAM_DISTANCE:
			zBeamValue = distanceToDetector.getSelection();
			break;
		}
		modifyDetectorOrigin();
	}

	private void modifyDetectorOrigin() {
		if (diffView.detConfig == null || diffView.diffEnv == null)
			return;
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				xBeam.setSelection(xBeamValue);
				yBeam.setSelection(yBeamValue);
				distanceToDetector.setSelection(zBeamValue);
			}

		});
		beamCorrection = new Vector3d(xBeamValue / scale, yBeamValue / scale, zBeamValue / scale);
		diffView.detConfig.setOrigin(beamCorrection);
		diffView.updateDiffractionObjects(showBeam.getSelection());
	}

	protected void cacheMetadata() {
		if (!CACHED_METADATA && (diffView.detConfig != null || diffView.diffEnv != null)) {
			cachedDetConfig = diffView.detConfig.clone();
			cachedDiffEnv = diffView.diffEnv.clone();
			CACHED_METADATA = true;
		}
	}

	protected void retrieveSettings() {
		if (cachedDetConfig != null || cachedDiffEnv != null) {
			diffView.detConfig = cachedDetConfig;
			diffView.diffEnv = cachedDiffEnv;
			setupSpinners();
			diffView.updateDiffractionObjects(showBeam.getSelection());
			CACHED_METADATA = false;
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {

	}

	public void setMetadata() {
		if (diffView.detConfig != null && diffView.diffEnv != null) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					double start = diffView.diffEnv.getPhiStart();
					double range = diffView.diffEnv.getPhiRange();
					phiStart.setText(decimaPlaces.format(start));
					phiRange.setText(decimaPlaces.format(range));
					phiStop.setText(decimaPlaces.format(start + range));
					ExposureTime.setText(decimaPlaces.format(diffView.diffEnv.getExposureTime()));
					detectorSizeX.setText(decimaPlaces.format(diffView.detConfig.getDetectorSizeH()));
					detectorSizeY.setText(decimaPlaces.format(diffView.detConfig.getDetectorSizeV()));
					PixelSizeX.setText(decimaPlaces.format(diffView.detConfig.getHPxSize()));
					PixelSizeY.setText(decimaPlaces.format(diffView.detConfig.getVPxSize()));
				}
			});
			if (lockSettings) {
				CACHED_METADATA = false;
				cacheMetadata();
				diffView.detConfig.setOrigin(beamCorrection);
				diffView.diffEnv.setWavelength(wavelength.getSelection() / scale);
			}

		}
		else{
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					phiStart.setText("0.0");
					phiRange.setText("0.0");
					phiStop.setText("0.0");
					ExposureTime.setText("0.0");
					detectorSizeX.setText("0.0");
					detectorSizeY.setText("0.0");
					PixelSizeX.setText("0.0");
					PixelSizeY.setText("0.0");
				}
			});
		}
		setupSpinners();
	}

	protected void setupSpinners() {
		final int lamba;
		if (diffView.detConfig == null || diffView.diffEnv == null) {
			xBeamValue = 0;
			yBeamValue = 0;
			zBeamValue = 0;
			lamba = 0;
			beamCorrection = new Vector3d(xBeamValue / scale, yBeamValue / scale, zBeamValue / scale);

		} else {
			xBeamValue = (int) Math.floor(diffView.detConfig.getOrigin().x * scale);
			yBeamValue = (int) Math.floor(diffView.detConfig.getOrigin().y * scale);
			zBeamValue = (int) Math.floor(diffView.detConfig.getOrigin().z * scale);
			lamba = (int) Math.floor(diffView.diffEnv.getWavelength() * scale);
			beamCorrection = new Vector3d(xBeamValue / scale, yBeamValue / scale, zBeamValue / scale);
		}
		// assign to spinners
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				wavelength.setSelection(lamba);
				xBeam.setSelection(xBeamValue);
				yBeam.setSelection(yBeamValue);
				distanceToDetector.setSelection(zBeamValue);
			}
		});
		diffView.updateDiffractionObjects(showBeam.getSelection());
	}

	@SuppressWarnings("unused")
	private double getDoubleFromMetadata(String key) {
		Object data = metadata.get(key);

		if (data == null)
			return 0;
		if (data instanceof Number)
			return ((Number) data).doubleValue();
		if (data instanceof String)
			return Double.parseDouble((String) data);

		return 0;
	}

	public void setDatasetInformation(final Number maxVal, final Number minVal, final Number meanVal) {
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				minPxVal.setText(decimaPlaces.format(minVal.doubleValue()));
				maxPxVal.setText(decimaPlaces.format(maxVal.doubleValue()));
				meanPxVal.setText(decimaPlaces.format(meanVal.doubleValue()));
			}
		});

	}

	public void updateBeamPositionFromDragging() {
		cacheMetadata();
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				int xbeamPos = (int) Math.floor((diffView.detConfig.getOrigin().x * scale));
				int ybeampos = (int) Math.floor((diffView.detConfig.getOrigin().y * scale));
				xBeam.setSelection(xbeamPos);
				yBeam.setSelection(ybeampos);
				xBeamValue = xbeamPos;
				yBeamValue = ybeampos;
				modifyDetectorOrigin();
			}
		});
	}

	public void showBeamCentre(final boolean visible) {
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				showBeam.setSelection(visible);
			}
		});

	}

	public boolean isBeamCentreToggled(){
		return showBeam.getSelection();
	}
	
	public void setThreshold(final double threshold) {
		getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				overload.setText(decimaPlaces.format(threshold));
			}
		});
		
	}
}

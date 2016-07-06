/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.MaskingBean;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DConsumer;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DProvider2;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayImage;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.MCView.PaintMode;
@Deprecated
class xyPointList {
	double[] xlist;
	double[] ylist;

	public xyPointList(Double[] x, Double[] y) {
		xlist = ArrayUtils.toPrimitive(x);
		ylist = ArrayUtils.toPrimitive(y);
	}
}
@Deprecated
public class MaskCreator extends SidePlot implements Overlay2DConsumer {

	private Logger logger = LoggerFactory.getLogger(MaskCreator.class);
	private Overlay2DProvider2 provider;
	private OverlayImage oi;
	private Dataset mainDataSet;
	private BooleanDataset maskDataSet;
	private int noOfMaskedPixels = 0;
	private MCView mcv;
	private BooleanDataset oldMaskDataSet;
	private int[] lastPoint;
	private boolean clearing = false;
	private Integer minthres;
	private Integer maxthres;

	public MaskCreator() {
		super();
		mcv = new MCView(this);
	}
	
	private void populateOI() {
		IndexIterator bogusIterator = maskDataSet.getIterator(true);
		final int[] pos = bogusIterator.getPos();
		oi.zap();
		noOfMaskedPixels = 0;
		while (bogusIterator.hasNext()) {
			if (!maskDataSet.get(pos)) {
				noOfMaskedPixels++;
				if (mcv.othercolor) {
					oi.putPixel(pos[1], pos[0], (short) 255, (short) 128, (short) 255, (short) 128);
				} else {
					oi.putPixel(pos[1], pos[0], (short) 0, (short) 128, (short) 0, (short) 128);
				}
			}
		}
	}

	@Override
	public void hideOverlays() {
		logger.debug("hideoverlay");
		if (!mcv.keepVisible) {
			provider.begin(OverlayType.IMAGE);
			if (oi != null) {
				oi.zap();
			}
			provider.end(OverlayType.IMAGE);
		}
	}

	@Override
	public void showOverlays() {
		logger.debug("showerlay");
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider2) provider;
		drawOverlay();
	}

	@Override
	public void unregisterProvider() {
		this.provider = null;
		oi = null;
	}

	protected void drawOverlay() {
		try {
			final String statusText;
			
			if (maskDataSet == null || maskDataSet.all()) {
				statusText =" No pixels masked.";
			} else {
				final int nopoints = noOfMaskedPixels;
				final float percent = 100.0f * nopoints / maskDataSet.getSize();
				statusText = String.format("%d pixel%s masked (%2.2f%% of the detector area).",	nopoints, nopoints == 1 ? "" : "s", percent);
			}
			
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					mcv.statusLabel.setText(statusText);
				}
			});
			
			if (provider != null) {
				if (maskDataSet == null || maskDataSet.all()) {
					provider.begin(OverlayType.IMAGE);
					if (oi != null) {
						oi.zap();
					}
					provider.end(OverlayType.IMAGE);
				} else {
					provider.begin(OverlayType.IMAGE);
					if (oi == null || !Arrays.equals(maskDataSet.getShape(), oi.getShape())) {
						oi = provider.registerOverlayImage(maskDataSet.getShape()[1], maskDataSet.getShape()[0]);
					}
					populateOI();
					provider.end(OverlayType.IMAGE);
				}
			}

		} catch (Exception e) {
			logger.error("do bad {}", e);
		}
	}

	@Override
	public void removePrimitives() {
		logger.debug("rp");
		if (provider == null) {
			return;
		}
		oi = null;
	}

	private void toggle(PaintMode paintMode, int[] pixelpos) {
		for (int i : pixelpos) {
			if (i < 0) {
				return;
			}
		}
		try {
			if (paintMode == PaintMode.DRAW) {
				maskDataSet.set(false, pixelpos);
				return;
			}
			if (paintMode == PaintMode.ERASE) {
				maskDataSet.set(true, pixelpos);
				return;
			}

			maskDataSet.set(!maskDataSet.get(pixelpos), pixelpos);
		} catch (Exception e) {
		}
	}

	private void manipulate(int[] imagepos) {
		PaintMode paintMode = mcv.getPaintMode();
		int ps = mcv.getPenSize() - 1;
		int hps = ps / 2;
		boolean square = mcv.isSquarePen();
		for (int i = -hps; i <= hps; i++) {
			for (int j = -hps; j <= hps; j++) {
				if (square || Math.sqrt(i * i + j * j) <= hps) {
					toggle(paintMode, new int[] { imagepos[1] + i, imagepos[0] + j });
				}
			}
		}
	}

	public void lineBresenham(int[] from, int[] to) {
		int x0 = from[0], x1 = to[0], y0 = from[1], y1 = to[1];
		int dy = y1 - y0;
		int dx = x1 - x0;
		int stepx, stepy;

		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}
		dy <<= 1; // dy is now 2*dy
		dx <<= 1; // dx is now 2*dx

		manipulate(new int[] { x0, y0 });
		if (dx > dy) {
			int fraction = dy - (dx >> 1); // same as 2*dy - dx
			while (x0 != x1) {
				if (fraction >= 0) {
					y0 += stepy;
					fraction -= dx; // same as fraction -= 2*dx
				}
				x0 += stepx;
				fraction += dy; // same as fraction -= 2*dy
				manipulate(new int[] { x0, y0 });
			}
		} else {
			int fraction = dx - (dy >> 1);
			while (y0 != y1) {
				if (fraction >= 0) {
					x0 += stepx;
					fraction -= dy;
				}
				y0 += stepy;
				fraction += dx;
				manipulate(new int[] { x0, y0 });
			}
		}
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		save();
		if (maskDataSet == null || !maskDataSet.isCompatibleWith(mainDataSet)) {
			maskDataSet = DatasetFactory.zeros(BooleanDataset.class, mainDataSet.getShape());
			maskDataSet.setName("mask");
			maskDataSet.fill(true);
		}
		lastPoint = event.getImagePosition();
		manipulate(lastPoint);
	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		lineBresenham(lastPoint, event.getImagePosition());
		lastPoint = event.getImagePosition();
		drawOverlay();
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		drawOverlay();
		updateBean();
	}

	@Override
	public void addToHistory() {
	}

	@Override
	public void removeFromHistory() {
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		mcv.createPartControl(parent);
		container = mcv.container;
		mcv.btnUndo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				undo();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		mcv.btnClear.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearMask();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public Action createSwitchAction(int index, IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setText("Create Mask");
		action.setToolTipText("Switch side plot into mask creation mode");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Gas-Mask-icon.png"));

		return action;
	}

	@Override
	public void processPlotUpdate() {
		mainDataSet = DatasetUtils.convertToDataset(getMainPlotter().getCurrentDataSet());

		if (mainDataSet != null) {
			if (maskDataSet != null && !mainDataSet.isCompatibleWith(maskDataSet)) {
				maskDataSet = null;
			} 
		} else 
			maskDataSet = null;
		drawOverlay();
		mcv.setMinMax(mainDataSet.min().intValue(), mainDataSet.max().intValue());
	}

	@Override
	public void showSidePlot() {
		processPlotUpdate();
		if (guiUpdateManager != null) {
			updateGUI(guiUpdateManager.getGUIInfo());
		}
		drawOverlay();
	}

	private void updateBean() {
		if (guiUpdateManager != null) {
			guiUpdateManager.putGUIInfo(GuiParameters.MASKING, new MaskingBean(maskDataSet, minthres, maxthres));
		}
	}

	@Override
	public int updateGUI(GuiBean bean) {
		logger.debug("update {}", bean);
		if (!bean.containsKey(GuiParameters.MASKING)) {
			return 0;
		}
		MaskingBean mb = (MaskingBean) bean.get(GuiParameters.MASKING);
		if (mainDataSet != null && mb != null && mb.mask != null && mainDataSet.isCompatibleWith(mb.mask)) {
			save();
			maskDataSet = mb.mask;
			drawOverlay();
		}
		return 0;
	}

	@Override
	public void generateToolActions(IToolBarManager manager) {
	}

	@Override
	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site) {
	}

	private void save() {
		if (maskDataSet != null)
			oldMaskDataSet = maskDataSet.clone();
		else
			oldMaskDataSet = null;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				mcv.btnUndo.setEnabled(true);
				mcv.btnUndo.setText("Undo");
			}
		});
	}

	private void undo() {
		BooleanDataset swap = maskDataSet;
		maskDataSet = oldMaskDataSet;
		oldMaskDataSet = swap;

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if ("Undo".equals(mcv.btnUndo.getText())) {
					mcv.btnUndo.setText("Redo");
				} else {
					mcv.btnUndo.setText("Undo");
				}
			}
		});

		drawOverlay();
		updateBean();
	}

	private void clearMask() {
		clearing = true;
		try {
			save();
			maskDataSet = null;
			mcv.btnMaskAbove.setSelection(false);
			mcv.btnMaskBelow.setSelection(false);
			drawOverlay();
		} finally {
			clearing = false;
		}
		updateBean();
	}

	public void updateThreshold(Integer min, Integer max) {
		if (clearing) {
			return;
		}
		this.minthres = min;
		this.maxthres = max;
		if (max == null && min == null) {
			return;
		}

		save();

		IndexIterator bogusIterator = mainDataSet.getIterator(true);
		final int[] pos = bogusIterator.getPos();
		
		if (maskDataSet == null || !maskDataSet.isCompatibleWith(mainDataSet)) {
			maskDataSet = DatasetFactory.zeros(BooleanDataset.class, mainDataSet.getShape());
			maskDataSet.setName("mask");
			maskDataSet.fill(true);
		}
		
		while (bogusIterator.hasNext()) {
			if (min != null && (mainDataSet.getElementLongAbs(bogusIterator.index) < min)) {
				maskDataSet.set(false, pos);
			} else if (max != null && (mainDataSet.getElementLongAbs(bogusIterator.index) > max)) {
				maskDataSet.set(false, pos);
			}
		}

		drawOverlay();
		updateBean();
	}
}

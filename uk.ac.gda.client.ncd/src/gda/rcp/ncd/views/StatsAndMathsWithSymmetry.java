/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.MaskingBean;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotViewStatsAndMaths;

public class StatsAndMathsWithSymmetry extends PlotViewStatsAndMaths {
	private static final Logger logger = LoggerFactory.getLogger(StatsAndMathsWithSymmetry.class);

	private Button foldingButton;
	private Button rawDataButton;
	private Button udButton;
	private Button lrButton;
	private Button fourButton;

	private boolean weSentUpdate = false;

	private OriginalDataAndSettings odas;

	protected class DatasetWithCentre {
		double x,y;
		Dataset dataset;
		DatasetWithCentre(double x, double y, Dataset dataset) {
			this.x = x;
			this.y = y;
			this.dataset = dataset;
		}
	}

	protected class OriginalDataAndSettings {
		Dataset image;
		BooleanDataset mask;
		SectorROI sectorROI;

		public OriginalDataAndSettings() throws Exception {
			image = currentBean.getData().get(0).getData();
			mask = getMaskDataset(image);
			sectorROI = (SectorROI) getRoi(GuiParameters.ROIDATA, SectorROI.class);
			if (image == null || mask == null || sectorROI == null)
				throw new IllegalStateException("cannot get required data, is a sector defined?");
		}

		public DatasetWithCentre getMaskDWC() {
			return new DatasetWithCentre(sectorROI.getPoint()[0], sectorROI.getPoint()[1], mask);
		}

		public DatasetWithCentre getImageDWC() {
			return new DatasetWithCentre(sectorROI.getPoint()[0], sectorROI.getPoint()[1], image);
		}

		private BooleanDataset getMaskDataset(Dataset image) throws Exception {
			BooleanDataset md;
			MaskingBean mb = (MaskingBean) getRoi(GuiParameters.MASKING, MaskingBean.class);
			if (mb == null || mb.mask == null || !image.isCompatibleWith(mb.mask)) {
				// in case be get a dodgy mask, we create our own empty one
				md = DatasetFactory.zeros(BooleanDataset.class, image.getShape());
				md.setName("mask");
				md.fill(true);
			} else {
				md = mb.mask;
			}
			return md;
		}
	}

	private SelectionListener folder = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (foldingButton.getSelection())
				plotSymmetry();
			else if (e.widget.equals(foldingButton)) {
				pushToPlotView(odas);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		foldingButton = new Button(composite, SWT.TOGGLE);
		foldingButton.setText("Display folded symmetry");
		foldingButton.addSelectionListener(folder);

		rawDataButton = new Button(composite, SWT.CHECK);
		rawDataButton.setText("display raw data");
		rawDataButton.addSelectionListener(folder);

		Composite radioComposite = new Composite(parent, SWT.NONE);
		radioComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		udButton = new Button(radioComposite, SWT.RADIO);
		udButton.setText("up/down");
		udButton.addSelectionListener(folder);

		lrButton = new Button(radioComposite, SWT.RADIO);
		lrButton.setText("left/right");
		lrButton.addSelectionListener(folder);

		fourButton = new Button(radioComposite, SWT.RADIO);
		fourButton.setText("4 quadrant");
		fourButton.setSelection(true);
		fourButton.addSelectionListener(folder);
	}

	@Override
	protected void processData(DataBean bean) {
		super.processData(bean);

		if (weSentUpdate) {
			weSentUpdate = false;
			return;
		}

		odas = null;

		List<DatasetWithAxisInformation> dc = bean.getData();
		final Dataset d = dc.get(0).getData();

		boolean buttonEnabled = false;

		if (d.getRank() == 2) {
			buttonEnabled = true;
		} else {
			buttonEnabled = false;
		}

		final boolean finalBuEn = buttonEnabled;
		parentComp.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				foldingButton.setSelection(false);
				foldingButton.setEnabled(finalBuEn);
			}
		});
	}

	private Object getRoi(GuiParameters key, @SuppressWarnings("rawtypes") Class clazz) throws Exception {

		GuiBean guiBean = SDAPlotter.getGuiBean(plotView.getPlotViewName());
		Serializable obj = key.equals(GuiParameters.ROIDATA) ? guiBean.getROI() : guiBean.get(key);
		if (clazz.isInstance(obj)) {
			return obj;
		}
		throw new Exception(String.format("no roi of type %s found with key %s", clazz.getSimpleName(), key));
	}

	private void setRoi(GuiParameters key, Serializable thing) {
		try {
			GuiBean guiBean = SDAPlotter.getGuiBean(plotView.getPlotViewName());
			guiBean.put(key, thing);
			SDAPlotter.setGuiBean(plotView.getPlotViewName(), guiBean);
		} catch (Exception e) {
			logger.warn("cannot adjust ROI after plotting symmetry", e);
		}
	}

	private void plotSymmetry() {
		try {
			if (odas ==null) odas = new OriginalDataAndSettings();

			DatasetWithCentre transformedDWC;
			if (rawDataButton.getSelection()) {
				transformedDWC = getTransformedDWC(odas.getImageDWC());
				DatasetWithCentre maskDWC = getTransformedDWC(odas.getMaskDWC());
				transformedDWC.dataset = Maths.dividez(transformedDWC.dataset, maskDWC.dataset);
			} else {
				transformedDWC = getTransformedDWC(odas.getMaskDWC());
			}
			pushToPlotView(transformedDWC);

		} catch (Exception e) {
			logger.error("error generating symmetry data to plot", e);
		}
	}

	protected DatasetWithCentre getTransformedDWC(DatasetWithCentre dwc) {
		if (fourButton.getSelection()) {
			return getUpDown(getLeftRight(dwc));
		}
		if (udButton.getSelection()) {
			return getUpDown(dwc);
		}
		if (lrButton.getSelection()) {
			return getLeftRight(dwc);
		}
		return dwc;
	}

	protected DatasetWithCentre getUpDown(DatasetWithCentre dwc) {
		int[] shape = dwc.dataset.getShape();

		int[] newshape = new int[] { getNewLength(shape[0], dwc.y), shape[1] };
		Dataset sumdata = DatasetFactory.zeros(IntegerDataset.class, newshape);

		int unflippedOffset = getUnflippedOffsetInNewDataset(shape[0], dwc.y);
		int flippedOffset = getFlippedOffsetInNewDataset(shape[0], dwc.y);

		// TODO this could be made into one set of loops, but that is not guaranteed to be faster. Check.
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
					sumdata.set(dwc.dataset.getInt(i,j), i+unflippedOffset, j);
			}
		}
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
					sumdata.set(dwc.dataset.getInt(shape[0]-i-1,j)+sumdata.getInt(i+flippedOffset, j), i+flippedOffset, j);
			}
		}
		return new DatasetWithCentre(dwc.x, dwc.y+unflippedOffset, sumdata);
	}

	protected DatasetWithCentre getLeftRight(DatasetWithCentre dwc) {
		int[] shape = dwc.dataset.getShape();

		int[] newshape = new int[] {  shape[0], getNewLength(shape[1], dwc.x) };
		Dataset sumdata = DatasetFactory.zeros(IntegerDataset.class, newshape);

		int unflippedOffset = getUnflippedOffsetInNewDataset(shape[1], dwc.x);
		int flippedOffset = getFlippedOffsetInNewDataset(shape[1], dwc.x);

		// TODO this could be made into one set of loops, but that is not guaranteed to be faster. Check.
		// unflipped loop
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
					sumdata.set(dwc.dataset.getInt(i,j), i, j+unflippedOffset);
			}
		}
		// loop for flipped image
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
					sumdata.set(dwc.dataset.getInt(i,shape[1]-j-1)+sumdata.getInt(i, j+flippedOffset), i, j+flippedOffset);
			}
		}
		return new DatasetWithCentre(dwc.x+unflippedOffset, dwc.y, sumdata);
	}

	protected int getNewLength(int length, double centre) {
			return centre > (length/2.0) ?  (int) Math.ceil(centre*2) : (int) Math.ceil((length - centre)*2);
	}

	protected int getUnflippedOffsetInNewDataset(int length, double x) {
		if (x > (length/2.0)) return 0;
		return getNewLength(length, x) - length;
	}

	public int getFlippedOffsetInNewDataset(int length, double x) {
		return getNewLength(length, x) - getUnflippedOffsetInNewDataset(length, x) - length;
	}

	protected void pushToPlotView(DatasetWithCentre datasetWithCentre) {
		DataBean result = new DataBean();
		List<DatasetWithAxisInformation> coll = new ArrayList<DatasetWithAxisInformation>();
		DatasetWithAxisInformation dswai = new DatasetWithAxisInformation();
		dswai.setData(datasetWithCentre.dataset);
		coll.add(dswai);
		result.setData(coll);
		weSentUpdate = true;
		super.pushToPlotView(result);
		SectorROI thing = odas.sectorROI.copy();
		thing.setPoint(datasetWithCentre.x, datasetWithCentre.y);
		setRoi(GuiParameters.ROIDATA, thing);
	}

	private void pushToPlotView(OriginalDataAndSettings odas) {
		DataBean result = new DataBean();
		List<DatasetWithAxisInformation> coll = new ArrayList<DatasetWithAxisInformation>();
		DatasetWithAxisInformation dswai = new DatasetWithAxisInformation();
		dswai.setData(odas.image);
		coll.add(dswai);
		result.setData(coll);
		super.pushToPlotView(result);
		setRoi(GuiParameters.ROIDATA, odas.sectorROI);
		MaskingBean mb = new MaskingBean(odas.mask, 0, 0);
		setRoi(GuiParameters.MASKING, mb);
	}
}

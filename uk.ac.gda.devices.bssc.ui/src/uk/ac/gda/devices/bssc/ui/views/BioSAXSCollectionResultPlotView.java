/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.SliceObject;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSCollectionResultPlotView extends ViewPart {
	public static String ID = "uk.ac.gda.devices.bssc.views.BioSAXSCollectionResultPlotView";
	private IPlottingSystem<Composite> plotting;
	private Logger logger = LoggerFactory.getLogger(BioSAXSCollectionResultPlotView.class);
	private String plotName;
	private Composite plotComposite;
	private ISAXSProgress sampleProgress;
	private LabelledSlider slider;
	private SliceObject sliceObject;
	protected ILazyDataset lz;
	protected String dataSetPath = "/entry1/instrument/detector/data";
	protected IDataHolder dh;
	private List<String> filePath;
	private int frame;
	private List<Button> fileRadios = new ArrayList<Button>();
	private int activeRadio;

	final Job loadJob = new Job("Load Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ILoaderService loaderService = (ILoaderService) ServiceManager
						.getService(ILoaderService.class);

				dh = loaderService.getData(filePath.get(activeRadio), new ProgressMonitorWrapper(monitor));
				lz = dh.getLazyDataset(dataSetPath);
				int[] shape = lz.getShape();

				final int maxframes = shape[1] - 1;

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						slider.setMinMax(0, maxframes, "0", String.valueOf(maxframes));
						slider.slider.setToolTipText(String.valueOf(frame));
						slider.setValue(frame);
					}
				});

				sliceJob.schedule();
			} catch (Exception e) {
				logger.error("Exception creating 2D plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating 2D plot", e);
			}

			return Status.OK_STATUS;
		}
	};

	final Job sliceJob = new Job("Slice Plot Data") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				String name = sampleProgress.getSampleName();

				if (activeRadio == 0) {
					name = name + " (buffer before)";
				} else if (activeRadio == 2) {
					name = name + " (buffer after)";
				}
				sliceObject.setName(name);

				int[] shape = lz.getShape();
				sliceObject.setFullShape(shape);
				sliceObject.setShapeMessage("");

				sliceObject.setSliceStart(new int[] { 0, frame, 0, 0 });
				sliceObject.setSliceStop(new int[] { 1, frame+1, shape[2], shape[3] });
				sliceObject.setSliceStep(null);

				final IDataset dataSet = SliceUtils.getSlice(lz, sliceObject, monitor);

				List<IDataset> dataSetList = new ArrayList<IDataset>();
				dataSetList.add(dataSet.squeeze());
				plot(dataSetList);

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						slider.slider.setToolTipText(String.valueOf(frame));
					}
				});

			} catch (Exception e) {
				logger.error("Exception creating 2D plot", e);
			} catch (Throwable e) {
				logger.error("Throwing exception creating 2D plot", e);
			}

			return Status.OK_STATUS;
		}
	};

	public BioSAXSCollectionResultPlotView() {
		try {
			this.plotting = PlottingFactory.createPlottingSystem();

			sliceObject = new SliceObject();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
	}

	public void setName(String plotName) {
		this.plotName = plotName;
		this.setPartName(plotName);
	}

	@Override
	public void createPartControl(Composite parent) {
		plotComposite = new Composite(parent, SWT.NONE);
		GridLayout gl_plotComposite = new GridLayout();
		plotComposite.setLayout(gl_plotComposite);

		Composite sliderComposite = new Composite(plotComposite, SWT.NONE);
		sliderComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		GridLayout sliderCompositeGL = new GridLayout();
		sliderCompositeGL.verticalSpacing = 10;
		sliderCompositeGL.marginWidth = 10;
		sliderCompositeGL.marginHeight = 10;
		sliderCompositeGL.horizontalSpacing = 10;
		sliderCompositeGL.numColumns = 6;

		Label lblFrames = new Label(sliderComposite, SWT.NONE);
		sliderComposite.setLayout(sliderCompositeGL);
		lblFrames.setText("Frame ");
		lblFrames.setLayoutData(new GridData(SWT.NONE));

		slider = new LabelledSlider(sliderComposite, SWT.HORIZONTAL);
		slider.setValue(frame);
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				frame = slider.getValue();
				sliceJob.schedule();
			}
		});
		slider.setIncrements(1, 1);
		slider.setToolTipText("Starting position");

		GridData gd_slider = new GridData(SWT.FILL, SWT.CENTER, true, true);
		slider.setLayoutData(gd_slider);

		for (int i = 0; i < 3; i++) {
			Button button = new Button(sliderComposite, SWT.RADIO);
			button.setEnabled(false);
			button.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final int oldActive = activeRadio;
					for (int j = 0; j < fileRadios.size(); j++) {
						if (fileRadios.get(j).equals(e.getSource())) {
							activeRadio = j;
							fileRadios.get(j).setSelection(true);

						} else {
							fileRadios.get(j).setSelection(false);
						}
					}
					if (oldActive != activeRadio)
						loadJob.schedule();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			fileRadios.add(button);
		}
		fileRadios.get(0).setText("buffer before");
		fileRadios.get(1).setText("sample");
		fileRadios.get(2).setText("buffer after");

		plotting.createPlotPart(plotComposite, plotName, getViewSite().getActionBars(), PlotType.IMAGE, this);
		GridData plotGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		plotGD.horizontalSpan = 2;
		plotting.getPlotComposite().setLayoutData(plotGD);
	}

	@Override
	public void setFocus() {
		plotting.setFocus();
	}

	@Override
	public Object getAdapter(final Class clazz) {
		if (IPlottingSystem.class == clazz)
			return plotting;
		if (IToolPageSystem.class == clazz)
			return plotting;
		return super.getAdapter(clazz);
	}

	public void setPlot(final ISAXSProgress sampleProgress) {
		this.sampleProgress = sampleProgress;
		filePath = this.sampleProgress.getCollectionFileNames();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (int j = 0; j < fileRadios.size(); j++) {
					fileRadios.get(j).setEnabled(j<filePath.size());
				}
				fileRadios.get(2).setSelection(false);
				fileRadios.get(1).setSelection(filePath.size() > 1);
				fileRadios.get(0).setSelection(filePath.size() == 1);
				activeRadio = filePath.size() > 1 ? 1 : 0;
				loadJob.schedule();
			}
		});

	}

	private boolean plot(List<IDataset> list) {
		plotting.clear();
		if (list == null || list.isEmpty())
			return false;

		if (list.get(0).getShape().length == 1) {
			plotting.createPlot1D(null, list, null);
		} else if (list.get(0).getShape().length == 2) {
			// Average the images, then plot
			Dataset added = Maths.add(list, list.size() > 1);
			plotting.createPlot2D(added, null, null);
		}
		return true;
	}
}
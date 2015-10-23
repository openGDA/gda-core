package org.opengda.lde.ui.views;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.events.DataFileEvent;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.ui.providers.ProgressLabelProvider;
import org.opengda.lde.ui.providers.SampleGroupViewContentProvider;
import org.opengda.lde.ui.providers.SampleGroupViewLabelProvider;
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.ui.utils.AnimatedTableItemFeedback;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.pixium.events.ScanEndEvent;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

/**
 * This sample view shows data obtained from the EMF model. 
 * The view is connected to the model using a content provider {@link SampleGroupViewContentProvider}.
 * <p>
 * The view uses a label provider {@link SampleGroupViewLabelProvider}to define how model
 * objects should be presented in the view. 
 * <p>
 */

public class SampleGroupView extends ViewPart implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SampleGroupView.class);
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.opengda.lde.ui.views.SampleGroupView";
	private LDEResourceUtil resUtil;
	private TableViewer viewer;
	private List<Sample> samples;
	private Resource resource;
	private String eventAdminName;
	private Scriptcontroller eventAdmin;
	private TableViewerColumn progressColumn;
	private Sample currentSample;
	private Image[] images;
	protected AnimationEngine animation=null;
	
	private final String sampleColumnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.PROGRESS, SampleTableConstants.ACTIVE, 
			SampleTableConstants.SAMPLE_NAME, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, SampleTableConstants.SAMPLE_X_STEP, 
			SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.COMMAND, SampleTableConstants.COMMENT, SampleTableConstants.DATA_FILE,
			SampleTableConstants.VISIT_ID, SampleTableConstants.CELL_ID, SampleTableConstants.CALIBRANT_NAME, 
			SampleTableConstants.CALIBRANT_X, SampleTableConstants.CALIBRANT_Y, SampleTableConstants.CALIBRANT_EXPOSURE, 
			SampleTableConstants.ENV_SCANNABLE_NAMES, SampleTableConstants.EMAIL, SampleTableConstants.START_DATE, SampleTableConstants.END_DATE, 
			SampleTableConstants.CALIBRATION_FILE, SampleTableConstants.STAGE_ID, SampleTableConstants.DETECTOR_X, SampleTableConstants.DETECTOR_Y, SampleTableConstants.DETECTOR_Z
			};

	private ColumnWeightData sampleColumnLayouts[] = { new ColumnWeightData(10, 50, false),new ColumnWeightData(10, 70, false), new ColumnWeightData(10, 35, false),
			new ColumnWeightData(80, 110, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), 
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),	
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 300, true), new ColumnWeightData(50, 300, true), new ColumnWeightData(50, 300, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 110, true), 
			new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 200, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true),
			new ColumnWeightData(50, 300, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true) 
			};
	private List<Cell> cells;
	
	/**
	 * The constructor.
	 */
	public SampleGroupView() {
		setTitleToolTip("Display all samples to be processed on the server for data collection.");
		setPartName("Server Samples");
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialise it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());
		
		viewer = new TableViewer(rootComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = viewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.heightHint = 386;
		gd_table.widthHint = 1000;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		createColumns(viewer, sampleColumnHeaders,sampleColumnLayouts);
		
		viewer.setContentProvider(new SampleGroupViewContentProvider(getResUtil()));
		viewer.setLabelProvider(new SampleGroupViewLabelProvider());
		
		try {
			resource = getResUtil().getResource();
			resource.eAdapters().add(notifyListener);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: "+getResUtil().getFileName(), e2);
		}

		initialisation();
		
	}
	

	private void initialisation() {
		if (getResUtil() != null) {
			try {
				samples=getResUtil().getSamples();
				cells=getResUtil().getCells();
			} catch (Exception e) {
				logger.error("Cannot get sample list from resource.", e);
			}
		}
		viewer.setInput(samples);
		
		progressColumn= new TableViewerColumn(viewer, viewer.getTable().getColumn(1));
		ProgressLabelProvider progressLabelProvider = new ProgressLabelProvider(viewer, samples);
		
		if (getEventAdminName()!=null) {
			eventAdmin = Finder.getInstance().find(getEventAdminName());
			if (eventAdmin!=null) {
				eventAdmin.addIObserver(this);
				progressLabelProvider.setEventAdmin(eventAdmin);
				eventAdmin.addIObserver(progressLabelProvider);
			}
		}
		progressColumn.setLabelProvider(progressLabelProvider);
		
		images = loadAnimatedGIF(viewer.getControl().getDisplay(), ImageConstants.ICON_RUNNING);

	}

	private Image[] loadAnimatedGIF(Display display, String imagePath) {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(imagePath), null);
		ImageLoader imageLoader = new ImageLoader();
		try {
			imageLoader.load(url.openStream());
		} catch (IOException e) {
			logger.error("Cannot load animated gif file {}", url.getPath());
		}
		Image[] images = new Image[imageLoader.data.length];
		for (int i = 0; i < imageLoader.data.length; ++i) {
			ImageData nextFrameData = imageLoader.data[i];
			images[i] = new Image(display, nextFrameData);
		}
		return images;
	}
	
	@Override
	public void update(Object source, final Object arg) {
		if (source==eventAdmin) {
			if (arg instanceof ScanEndEvent) {
				Display.getDefault().asyncExec(new Runnable() {

					@SuppressWarnings("restriction")
					@Override
					public void run() {
						if (currentSample!=null) {
							updateSampleStatus(currentSample, STATUS.COMPLETED);
						}
						if (animation!=null) {
							animation.cancelAnimation();
						}
					}
				});
			} else if (arg instanceof SampleChangedEvent) {
				SampleChangedEvent event = (SampleChangedEvent)arg;
				final String sampleID = event.getSampleID();
				logger.debug("sample update to {}",sampleID);
				Display.getDefault().asyncExec(new Runnable() {
					@SuppressWarnings("restriction")
					@Override
					public void run() {
						for (Sample sample : samples) {
							if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
								if (currentSample != sample) {
									updateSampleStatus(currentSample, STATUS.COMPLETED);
								}
								currentSample = sample;
							}
						}
						viewer.setSelection(new StructuredSelection(currentSample));
						if (animation!=null) {
							animation.cancelAnimation();
						}
						try {
							TableItem tableItem = viewer.getTable().getItem(samples.indexOf(currentSample));
							AnimatedTableItemFeedback feedback = new AnimatedTableItemFeedback(viewer.getControl().getShell(),images, tableItem,SampleTableConstants.COL_STATUS);
							animation= new AnimationEngine(feedback,-1,100);
							animation.schedule();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else if (arg instanceof SampleStatusEvent) {
				SampleStatusEvent event = (SampleStatusEvent)arg;
				final String sampleID = event.getSampleID();
				final STATUS status = event.getStatus();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						logger.debug("sample {} update to {}",sampleID, status);
						for (Sample sample : samples) {
							if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
								updateSampleStatus(sample, status);
							}
						}
						if (status==STATUS.PAUSED) {
							animation.sleep();
						} else if (status==STATUS.RUNNING) {
							animation.wakeUp();
						}
					}
				});
			} else if (arg instanceof DataFileEvent) {
				DataFileEvent event = ((DataFileEvent)arg);
				String id = event.getId();
				boolean calibration = event.isCalibration();
				String filename = event.getFilename();
				if (calibration) {
					for (Cell cell : cells) {
						if (cell.getCellID().equalsIgnoreCase(id)) {
							setCalibrationDataFile4Samples(cell,filename);
						}
					}
				} else {
					for (Sample sample : samples) {
						if (sample.getSampleID().equalsIgnoreCase(id)) {
							setDataFile4Sample(sample, filename);
						}
					}
				}
			} 
		}
	}
	
	private void setCalibrationDataFile4Samples(final Cell cell, final String filename) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				for (Sample sample : cell.getSample()) {
					if (sample.isActive()) {
						sample.setCalibrationFilePath(filename);
					}
				}
				viewer.refresh();
			}
		});
	}

	private void setDataFile4Sample(final Sample sample, final String filename) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				sample.setDataFilePath(filename);
				viewer.refresh();
			}
		});
		
	}

	protected void updateSampleStatus(final Sample sample, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				sample.setStatus(status);
				viewer.refresh();
			}
		});
	}
	

	protected List<String> getSampleNames() {
		List<String> sampleNames=new ArrayList<String>();
		for (Sample sample : samples) {
			sampleNames.add(sample.getName());
		}
		return sampleNames;
	}

	/**
	 * create table columns using customised Content Provider, Label Provider, and Input using List or array of Objects to be displayed
	 * @param tableViewer
	 * @param columnHeaders
	 * @param columnLayouts
	 * @param firstElement
	 */
	private void createColumns(TableViewer tableViewer, String[] columnHeaders, ColumnWeightData[] columnLayouts) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);
			column.setWidth(columnLayouts[i].minimumWidth);
		}
	}
	
	private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
					&& (!notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_Status()))) {
				//TODO delete?
			}
		}
	};

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}


	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	public void refreshTable(String seqFileName, boolean newFile) {
		logger.debug("refresh table with file: {}{}", FilenameUtils.getFullPath(seqFileName), FilenameUtils.getName(seqFileName));

		try {
			resource.eAdapters().remove(notifyListener); // remove old resource listener
			resUtil.setFileName(seqFileName);
			if (newFile) {
				resUtil.createExperiments()
				;
			}
			Resource sequenceRes = resUtil.getResource();
			viewer.setInput(sequenceRes);
			// update the resource in this view.
			resource = sequenceRes;
			resource.eAdapters().add(notifyListener);

			// update existing regions list
			samples = resUtil.getSamples();
			for (Sample sample : samples) {
				if (sample.isActive()) {
					currentSample=sample;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Cannot refresh table.", e);
		}
	}
	
	@Override
	public void dispose() {
		try {
			resUtil.getResource().eAdapters().remove(notifyListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
	}
	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}


	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}
}

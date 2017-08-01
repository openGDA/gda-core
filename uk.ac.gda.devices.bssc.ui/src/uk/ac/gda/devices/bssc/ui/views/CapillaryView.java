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

import gda.commandqueue.CommandProgress;
import gda.commandqueue.Processor;
import gda.commandqueue.QueueChangeEvent;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueView;
import uk.ac.gda.core.GDACoreActivator;
import uk.ac.gda.video.views.BasicCameraComposite;
import uk.ac.gda.video.views.ICameraConfig;

public class CapillaryView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(CapillaryView.class);

	public static final String ID = "uk.ac.gda.devices.bssc.views.CapillaryView"; //$NON-NLS-1$

	private BasicCameraComposite bcc;

	private Composite progressComposite;

	private IObserver processorObserver;

	private String progressBarText;

	public CapillaryView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite com = new Composite(parent, SWT.FILL);
		com.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		com.setLayout(new GridLayout());

		bcc = new BasicCameraComposite(com, SWT.DOUBLE_BUFFERED);
		GridData gd_bcc = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_bcc.exclude = true;
		bcc.setLayoutData(gd_bcc);
		try {
			ServiceReference<ICameraConfig> ref = GDACoreActivator.getBundleContext().getServiceReference(ICameraConfig.class);
			if (ref != null) {
				ICameraConfig conf = GDACoreActivator.getBundleContext().getService(ref);
				if (conf != null && conf.getCameras().length > 0) {
					bcc.playURL(conf.getCameras()[0].getMjpegURL());
				}
			}
		} catch (FactoryException e) {
			logger.error("cannot configure mjpeg stream", e);
		}

		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				bcc.zoomFit();
			}
		});

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		CommandQueueView view = (CommandQueueView) page.findView("uk.ac.gda.client.CommandQueueViewFactory");

		progressComposite = new Composite(com, SWT.NONE);
		progressComposite.setLayout(new FillLayout());
		progressComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		final ProgressBar progressBar = new ProgressBar(progressComposite, SWT.NONE);
		progressBar.setMinimum(0);
		progressBar.setMaximum(2000);
		progressBarText = "";
		
		progressBar.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point point = progressBar.getSize();
				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * progressBarText.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				e.gc.drawString(progressBarText, (point.x - width) / 2, (point.y - height) / 2, true);
			}
		});

		if (view.getProcessor() != null) {
			processorObserver = new IObserver() {
				@Override
				public void update(Object source, final Object arg) {
					if (arg instanceof Processor.STATE) {
						// CommandProcessorComposite.this.updateStateAndDescription((Processor.STATE) arg);
					} else if (arg instanceof QueueChangeEvent) {
						// do nothing
					} else if (arg instanceof CommandProgress) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								CommandProgress progress = (CommandProgress) arg;
								progressBarText = progress.getMsg();
								progressBar.setSelection((int) (progress.getPercentDone() * 20));
								progressBar.redraw();// this is needed to cope with a change in text but no change in
														// percentage
							}
						});
					}
				}
			};
			view.getProcessor().addIObserver(processorObserver);
		}
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}
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

package uk.ac.gda.client.tomo.configuration.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoAlignmentConfigurationHolder;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;
import uk.ac.gda.client.tomo.configuration.view.xviewer.TomoConfigContentProvider;
import uk.ac.gda.client.tomo.configuration.view.xviewer.TomoConfigXViewerFactory;
import uk.ac.gda.client.tomo.configuration.view.xviewer.TomoConfigurationLabelProvider;
import uk.ac.gda.tomography.parameters.TomoExperiment;

/**
 *
 */
public class TomoConfigurationView extends ViewPart {

	private String viewPartName;
	private FormToolkit toolkit;
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigurationView.class);
	private Label lblTime;
	private Label lblDate;

	private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);

		Composite rootComposite = toolkit.createComposite(parent);

		rootComposite.setLayout(new GridLayout());
		//
		Composite btnAndTableViewerComposite = toolkit.createComposite(rootComposite);
		GridLayout layout = new GridLayout(2, false);
		setLayoutSettings(layout, 0, 0, 0, 0);
		// os
		btnAndTableViewerComposite.setLayout(layout);
		btnAndTableViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		//
		Composite xViewerContainer = toolkit.createComposite(btnAndTableViewerComposite);
		GridLayout l = new GridLayout();
		setLayoutSettings(l, 0, 0, 0, 0);
		xViewerContainer.setLayout(l);
		xViewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		XViewer viewer = new XViewer(xViewerContainer, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION,
				new TomoConfigXViewerFactory("tomo.config"));
		viewer.setContentProvider(new TomoConfigContentProvider());
		viewer.setLabelProvider(new TomoConfigurationLabelProvider(viewer));
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH | GridData.HORIZONTAL_ALIGN_BEGINNING));

		//
		Composite deleteBtnContainer = toolkit.createComposite(btnAndTableViewerComposite);
		deleteBtnContainer.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		deleteBtnContainer.setLayout(new GridLayout());

		Button btnDeleteAll = toolkit.createButton(deleteBtnContainer, "Delete All Configs", SWT.PUSH);
		btnDeleteAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button btnDeleteSelected = toolkit.createButton(deleteBtnContainer, "Delete Selected Configs", SWT.PUSH);
		btnDeleteSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		Composite middleRowComposite = toolkit.createComposite(rootComposite);
		GridLayout layout2 = new GridLayout(6, true);
		setLayoutSettings(layout2, 0, 0, 2, 0);
		middleRowComposite.setLayout(layout2);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 75;
		middleRowComposite.setLayoutData(layoutData);

		//
		Composite timeNowCompositeContainer = toolkit.createComposite(middleRowComposite);
		timeNowCompositeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		timeNowCompositeContainer.setLayout(new GridLayout());
		timeNowCompositeContainer.setBackground(ColorConstants.black);

		Composite innerCompositeTimeNow = toolkit.createComposite(timeNowCompositeContainer);
		innerCompositeTimeNow.setLayout(new GridLayout());
		innerCompositeTimeNow.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblNow = toolkit.createLabel(innerCompositeTimeNow, "Now", SWT.WRAP | SWT.CENTER);
		lblNow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblDate = toolkit.createLabel(innerCompositeTimeNow, "", SWT.WRAP | SWT.CENTER);
		lblDate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblTime = toolkit.createLabel(innerCompositeTimeNow, "", SWT.WRAP | SWT.CENTER);
		lblTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		//
		Composite estEndTimeCompositeContainer = toolkit.createComposite(middleRowComposite);
		estEndTimeCompositeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		estEndTimeCompositeContainer.setLayout(new GridLayout());
		estEndTimeCompositeContainer.setBackground(ColorConstants.black);
		//
		Composite innerCompositeEstEndTime = toolkit.createComposite(estEndTimeCompositeContainer);
		innerCompositeEstEndTime.setLayout(new GridLayout());
		innerCompositeEstEndTime.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblEstEndTime = toolkit
				.createLabel(innerCompositeEstEndTime, "Estimated End Time", SWT.WRAP | SWT.CENTER);
		lblEstEndTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		//
		// Start Tomo Runs
		Button btnStartTomoRuns = toolkit.createButton(middleRowComposite, "Start Tomo Runs", SWT.PUSH);
		btnStartTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Interrupt Tomo Runs
		Button btnInterruptTomoRuns = toolkit.createButton(middleRowComposite, "Interrupt Tomo Runs", SWT.PUSH);
		btnInterruptTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Interrupt Tomo Runs
		Button btnStopTomoRuns = toolkit.createButton(middleRowComposite, "Stop Tomo Runs", SWT.PUSH);
		btnStopTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Stats
		Button btnStats = toolkit.createButton(middleRowComposite, "Display Stats", SWT.PUSH);
		btnStats.setLayoutData(new GridData(GridData.FILL_BOTH));

		//
		Composite imageDisplayComposite = toolkit.createComposite(rootComposite);
		GridLayout layout3 = new GridLayout(2, false);
		setLayoutSettings(layout3, 2, 0, 4, 0);
		imageDisplayComposite.setLayout(layout3);
		imageDisplayComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		FixedImageViewerComposite img0DegComposite = new FixedImageViewerComposite(imageDisplayComposite, SWT.None);
		img0DegComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		FixedImageViewerComposite img90DegComposite = new FixedImageViewerComposite(imageDisplayComposite, SWT.None);
		img90DegComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Resource alignmentConfigResource = null;
		try {
			alignmentConfigResource = TomoAlignmentConfigurationHolder.getAlignmentConfigResource(null, false);
		} catch (CoreException e) {
			logger.error("TODO put description of error here", e);
		}
		TomoExperiment tomoExperiment = null;

		if (alignmentConfigResource != null) {
			if (alignmentConfigResource.getContents() != null && !alignmentConfigResource.getContents().isEmpty()) {
				EObject eObject = alignmentConfigResource.getContents().get(0);
				if (eObject instanceof TomoExperiment) {
					tomoExperiment = (TomoExperiment) eObject;
				}
			}
		}

		final int time = 1000;// 1 sec delay
		final Display display = getViewSite().getShell().getDisplay();
		Runnable timer = new Runnable() {

			@Override
			public void run() {
				lblDate.setText(dateFormat.format(new Date()));
				lblTime.setText(timeFormat.format(new Date()));
				display.timerExec(time, this);
			}
		};
		display.timerExec(time, timer);
		viewer.setInput(tomoExperiment);
	}

	private void setLayoutSettings(GridLayout layout, int marginWidth, int marginHeight, int horizontalSpacing,
			int verticalSpacing) {
		layout.marginWidth = marginWidth;
		layout.marginHeight = marginHeight;
		layout.horizontalSpacing = horizontalSpacing;
		layout.verticalSpacing = verticalSpacing;
	}

	@Override
	public void setFocus() {

	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public String getPartName() {
		if (viewPartName != null) {
			return viewPartName;
		}
		return super.getPartName();
	}

	@Override
	public Image getTitleImage() {
		return TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_TOMO_CONFIG);
	}
}
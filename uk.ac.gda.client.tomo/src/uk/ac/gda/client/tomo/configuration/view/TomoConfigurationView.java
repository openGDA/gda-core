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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerTreeReport;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.nebula.widgets.xviewer.customize.IXViewerCustomizations;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoAlignmentConfigurationHolder;
import uk.ac.gda.client.tomo.TomoClientActivator;
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

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);

		Composite rootComposite = toolkit.createComposite(parent);

		rootComposite.setLayout(new GridLayout());

		XViewer viewer = new XViewer(rootComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION,
				new TomoConfigXViewerFactory("tomo.config"));
		viewer.setContentProvider(new TomoConfigContentProvider());
		viewer.setLabelProvider(new TomoConfigurationLabelProvider(viewer));
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH | GridData.HORIZONTAL_ALIGN_BEGINNING));

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

		viewer.setInput(tomoExperiment);
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
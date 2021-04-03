/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.composites;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.Browser;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;

/**
 * A factory to generate a browser for {@link AcquisitionConfigurationResourceType} objects.
 * @param <T> the type of object displayed in the {@link TreeViewer}
 *
 * @author Maurizio Nagni
 */
public class AcquisitionsBrowserCompositeFactory<T extends Document> implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionsBrowserCompositeFactory.class);

	private final Browser<T> browser;

	public AcquisitionsBrowserCompositeFactory(Browser<T> browser) {
		super();
		this.browser = browser;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		Composite container = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
		standardMarginHeight(container.getLayout());
		standardMarginWidth(container.getLayout());

		TreeViewerBuilder<AcquisitionConfigurationResource<T>> builder = browser.getTreeViewBuilder();
		browser.addColumns(builder);
		builder.addContentProvider(browser.getContentProvider());
		builder.addDoubleClickListener(browser.getDoubleClickListener());

		MenuManager contextMenu = new MenuManager("#ViewerMenu"); //$NON-NLS-1$
		builder.addMenuManager(contextMenu);
		builder.addSelectionListener(browser.getISelectionChangedListener(contextMenu));
		builder.build(container);
		logger.debug("Created {}", this);
		return container;
	}
}

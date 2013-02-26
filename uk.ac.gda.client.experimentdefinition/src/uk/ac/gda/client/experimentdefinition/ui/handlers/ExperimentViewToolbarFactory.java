/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.swtdesigner.ResourceManager;

public class ExperimentViewToolbarFactory extends ExtensionContributionFactory {

	public ExperimentViewToolbarFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

		CommandContributionItemParameter collapseParameters = new CommandContributionItemParameter(serviceLocator, null,
				"uk.ac.gda.client.experimentdefinition.ExperimentViewCollapseAll", null,
				ResourceManager.getImageDescriptor(ExperimentViewToolbarFactory.class, "/collapseall.gif"), null,
				null, "Collapse All", null, "Collapse All", CommandContributionItem.STYLE_PUSH, null, true);

		additions.addContributionItem(new CommandContributionItem(collapseParameters), null);

		CommandContributionItemParameter expandParameters = new CommandContributionItemParameter(serviceLocator, null,
				"uk.ac.gda.client.experimentdefinition.ExperimentViewExpandAll", null,
				ResourceManager.getImageDescriptor(ExperimentViewToolbarFactory.class, "/expandall.gif"), null,
				null, "Expand All", null, "Expand All", CommandContributionItem.STYLE_PUSH, null, true);

		additions.addContributionItem(new CommandContributionItem(expandParameters), null);
	}

}

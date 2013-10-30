/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.util.Map;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IntroConfigurer;
import org.eclipse.ui.intro.config.IntroElement;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.ui.event.PartAdapter;

public class ExafsIntroConfigurer extends IntroConfigurer {

	private final static Logger logger = LoggerFactory.getLogger(ExafsIntroConfigurer.class);
	
	private IPartListener partListener;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void init(final IIntroSite site, Map themeProperties) {
    	super.init(site, themeProperties);
        partListener = new PartAdapter() {
			@Override
			public void partClosed(IWorkbenchPart part) {
				if (part instanceof ViewPart) {
					try {
						ExperimentFactory.getExperimentEditorManager();
					} catch (Exception e) {
						logger.error("Cannnot initiate ExafsWorkspace for SimplePerspective", e);
					}
				}
				site.getPage().removePartListener(partListener);
			}
        };
        site.getPage().addPartListener(partListener);
 	}
	
	@Override
	public IntroElement[] getGroupChildren(String pageId, String groupId) {
		return null;
	}

	@Override
	public String getVariable(String variableName) {
		return null;
	}

	@Override
	public String resolvePath(String extensionId, String path) {
		return null;
	}

}

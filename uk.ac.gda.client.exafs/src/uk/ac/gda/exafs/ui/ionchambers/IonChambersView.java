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

package uk.ac.gda.exafs.ui.ionchambers;

import java.io.File;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.beans.exafs.IonChambersBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class IonChambersView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(IonChambersView.class);
	private String path;
	private IonChambersBean bean = null;
	private IonChamber ionChamber;

	private IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {}
		@Override
		public void partBroughtToTop(IWorkbenchPart part) {}
		@Override
		public void partClosed(IWorkbenchPart part) {}
		@Override
		public void partDeactivated(IWorkbenchPart part) {
			if(part instanceof IonChambersView){
				try {
					XMLHelpers.writeToXML(IonChambersBean.mappingURL, bean, path);
				} catch (Exception e) {
					logger.error("Problem writing settings to xml file "+path+" : "+e.getMessage() );
				}
			}
		}
		@Override
		public void partOpened(IWorkbenchPart part) {}
	};

	public IonChambersView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		path = LocalProperties.getConfigDir() + File.separator+ "templates" + File.separator+ "ionChambers.xml";
        try {
			bean = (IonChambersBean) XMLHelpers.createFromXML(IonChambersBean.mappingURL, IonChambersBean.class, IonChambersBean.schemaURL, path);
        } catch (Exception e) {
			logger.error("Could not load xml " + path + " into bean", e);
		}
		ionChamber = new IonChamber(parent, bean);
		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose(){
		getSite().getPage().removePartListener(partListener);
	}

}
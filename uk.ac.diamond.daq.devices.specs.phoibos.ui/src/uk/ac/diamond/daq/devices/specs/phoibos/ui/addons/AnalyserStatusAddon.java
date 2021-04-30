/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;

public class AnalyserStatusAddon {

	@Inject
	MApplication application;

	private MToolControl control;

    @PostConstruct
    public void init(IEclipseContext context) {
        // injected IEclipseContext comes from the application

    	if (!controlExists()) {
    		MTrimBar trimBar = getBottomTrimBar();
    		control = MMenuFactory.INSTANCE.createToolControl();
    		control.setElementId(AnalyserStatusUI.ID);
    		control.setContributionURI(AnalyserStatusUI.CLASS_URI);
    		if(trimBar != null) {
    			trimBar.getChildren().add(0, control);
    		}
    	}
    }

    private boolean controlExists() {
		MTrimBar trimBar = getBottomTrimBar();
		if (trimBar != null) {
			for (MTrimElement trimElement : trimBar.getChildren()) {
				if (AnalyserStatusUI.ID.equals(trimElement.getElementId())) {
					return true;
				}
			}
		}
		return false;
	}

    private MTrimBar getBottomTrimBar() {
		for (MWindow window : application.getChildren()) {
			if (window instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) window).getTrimBars()) {
					if (trimBar.getSide() == SideValue.BOTTOM) {
						return trimBar;
					}
				}
			}
		}
		return null;
	}

}

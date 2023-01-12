/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControl;

public class SynopticView extends ViewPart {
	public static final String ID = "uk.ac.gda.ui.views.synoptic.SynopticView";
	private static final Logger logger = LoggerFactory.getLogger(SynopticView.class);

	private String viewConfigName = ""; //Full name of class with composite to be opened


	public SynopticView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		String configName = getViewSite().getSecondaryId();
		if (!viewConfigName.isEmpty()) {
			configName = viewConfigName;
		}
		if (StringUtils.isEmpty(configName)) {
			selectFromList(parent);
			return;
		}
		var viewConfig = Finder.findOptionalOfType(configName, SynopticViewConfiguration.class);
		if (viewConfig.isPresent()) {
			logger.info("Creating synoptic view from client configuration {}", viewConfig.get().getName());
			createPartControl(parent, viewConfig.get());
			return;
		}

	}

	private void selectFromList(Composite parent) {
		var findables = Finder.getFindablesOfType(SynopticViewConfiguration.class);
		if (findables.isEmpty()) {
			MessageDialog.openWarning(parent.getShell(),"No Synoptic views available", "No Synoptic views are available to display");
			return;
		}

		ListDialog listDialog = new ListDialog(parent.getShell());
		listDialog.setTitle("Select Synoptic view to open");
		listDialog.setContentProvider(new ArrayContentProvider());
		// Label provider returns the name of the view
		listDialog.setLabelProvider(LabelProvider.createTextProvider(obj -> {
			if (obj instanceof SynopticViewConfiguration viewConfig) {
				return viewConfig.getViewName();
			}
			return "";
		}));

		listDialog.setInput(findables.values());
		listDialog.setBlockOnOpen(true);
		if (listDialog.open() == Window.OK) {
			Object[] res = listDialog.getResult();
			if (res != null && res.length > 0 && res[0] instanceof SynopticViewConfiguration config) {
				logger.info("Synoptic view selected : {} ({})", config.getViewName(), config.getName());
				createPartControl(parent, config);
			}
		}
	}

	@Override
	public void setFocus() {
	}

	/**
	 * Set the name of the client side SynopticViewConfiguration object to be used to generate the view
	 *
	 * @param viewConfigName
	 */
	public void setViewConfigName(String viewConfigName) {
		this.viewConfigName = viewConfigName;
	}

	private void createPartControl(Composite parent, SynopticViewConfiguration viewConfig) {
		setPartName(viewConfig.getViewName());
		SynopticGuiComposite composite = new SynopticGuiComposite();
		composite.setViewConfig(viewConfig);
		composite.createControls(parent, null);
	}

	private class SynopticGuiComposite extends HardwareDisplayComposite {
		private SynopticViewConfiguration viewConfig;

		public void setViewConfig(SynopticViewConfiguration viewConfig) {
			this.viewConfig = viewConfig;
		}

		@Override
		protected void createControls(Composite parent) throws Exception {
			this.parent = parent;
			super.setViewName(viewConfig.getViewName());
			if (!StringUtils.isEmpty(viewConfig.getBackgroundImage())) {
				Image img = getImageFromPlugin(viewConfig.getBackgroundImage());
				int xsize = (int) (img.getBounds().width*viewConfig.getImageScaleFactor());
				int ysize = (int) (img.getBounds().height*viewConfig.getImageScaleFactor());
				Image img2 = new Image(Display.getDefault(), img.getImageData().scaledTo(xsize, ysize));
				parent.addDisposeListener(d -> img2.dispose());
				img.dispose();
				super.setBackgroundImage(img2, viewConfig.getImageStart());
			}
			parent.setBackgroundMode(SWT.INHERIT_FORCE);

			for(var liveControl : viewConfig.getControls().entrySet()) {
				LiveControl cont = liveControl.getKey();

				Composite group;
				if (StringUtils.isNotEmpty(cont.getGroup())) {
					group = new Group(parent, SWT.NONE);
					((Group)group).setText(cont.getGroup());
				} else {
					group = new Composite(parent, SWT.NONE);
				}

				group.setLayout(new GridLayout());

				cont.createControl(group);

				Point position = liveControl.getValue();
				setAbsoluteWidgetPosition(group, position.x, position.y);
			}

			addResizeListener(parent);

			if (viewConfig.isShowCoordinates()) {
				addMousePositionOutput(parent);
			}
		}
	}
}
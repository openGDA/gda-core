package uk.ac.gda.beamline.synoptics.composites;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.beamline.synoptics.Activator;

public class ImageButtonCompositeFactory implements CompositeFactory, InitializingBean {

	static final Logger logger = LoggerFactory.getLogger(ImageButtonCompositeFactory.class);

	private String label;
	private String tooltipText;
	private String imagePath;
	private String actionId;


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getActionId() == null)
			throw new IllegalArgumentException("actionId is null");
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new ImageButtonComposite(parent, style, label, tooltipText, getImagePath(), actionId);
	}

	public String getTooltipText() {
		return tooltipText;
	}

	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}

class ImageButtonComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ImageButtonComposite.class);
	private Canvas canvas;

	public ImageButtonComposite(Composite parent, int style,
			String label, String tooltip, String imagePath,	final String actionId) {
		super(parent, style);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		Group grp = new Group(this, style);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(grp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(grp);
		grp.setText(label);

		canvas = new Canvas(grp, SWT.NONE);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL,GridData.VERTICAL_ALIGN_FILL, true,true);
		gridData.widthHint = 80;
		gridData.heightHint = 73;
		canvas.setLayoutData(gridData);
		canvas.setBackgroundImage(Activator.getImageDescriptor(imagePath).createImage());
		canvas.setToolTipText(tooltip);

		canvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent event) {
				if (event.button == 1) {
					if (event.button == 1) {
						ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
						IHandlerService hdlService = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
						Command cmd = cmdService.getCommand(actionId);
						try {
							hdlService.executeCommand(cmd.getId(), null);
						} catch (Exception ex) {
							logger.error("Error executing command " + actionId, ex);
						}
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}

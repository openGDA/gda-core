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

package gda.rcp.views;

import gda.rcp.GDAClientActivator;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ActionBtnCompositeFactory implements CompositeFactory, InitializingBean{

	
	private String tooltipText;
	private String buttonText;
	private String buttonImagePath;
	private Integer foregroundColorId; //e.g. SWT.COLOR_RED
	private String actionId;

	
	
	
	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}


	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}


	public void setButtonImagePath(String buttonImagePath) {
		this.buttonImagePath = buttonImagePath;
	}


	public void setForegroundColorId(Integer foregroundColorId) {
		this.foregroundColorId = foregroundColorId;
	}


	public void setActionId(String actionId) {
		this.actionId = actionId;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if( actionId == null)
			throw new IllegalArgumentException("actionId is null");
	}

	
	@Override
	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
		Image buttonImage = buttonImagePath != null ? GDAClientActivator.getImageDescriptor(buttonImagePath).createImage() : null;
		return new ActionBtnComposite(parent, iWorkbenchPartSite, buttonText, tooltipText, buttonImage, foregroundColorId != null ? parent.getDisplay().getSystemColor(foregroundColorId): null,
				actionId);
	}

}

class ActionBtnComposite extends Composite{
	private static final Logger logger = LoggerFactory.getLogger(ActionBtnComposite.class);
	public ActionBtnComposite(Composite parent, final IWorkbenchPartSite iWorkbenchPartSite, String buttonText, String tooltipText, Image buttonImage, Color foregroundColor, final String actionId) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		Button btn = new Button(this, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(btn);
		if( buttonText != null)
			btn.setText(buttonText);
		if( tooltipText != null)
			btn.setToolTipText(tooltipText);
		if( buttonImage != null)
			btn.setImage(buttonImage);
		if( foregroundColor != null)
			btn.setForeground(foregroundColor);
		btn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ICommandService cmdService = (ICommandService)iWorkbenchPartSite.getService(ICommandService.class);
				IHandlerService hdlService = (IHandlerService)iWorkbenchPartSite.getService(IHandlerService.class);
				Command cmd = cmdService.getCommand(actionId);
				try {
					hdlService.executeCommand(cmd.getId(), null);
				} catch (Exception ex) {
					logger.error("Error executing command " + actionId, ex);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
}
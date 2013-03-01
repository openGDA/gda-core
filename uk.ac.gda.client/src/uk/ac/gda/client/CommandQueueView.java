/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.rcp.GDAClientActivator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class CommandQueueView extends ViewPart {

	/**
	 * The primary ID of the view (one per class)
	 */
	private Processor processor;
	private Queue queue;
	private CommandProcessorComposite commandProcessorComposite;
	private CommandQueueComposite commandQueueComposite;
	
	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}
	

	public Processor getProcessor() {
		return processor;
	}

	public Queue getQueue() {
		return queue;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new FormLayout());
		commandProcessorComposite = new CommandProcessorComposite(top, SWT.NONE, getViewSite(), processor);
		FormData fd_commandProcessorComposite = new FormData();
		fd_commandProcessorComposite.top = new FormAttachment(0);
		fd_commandProcessorComposite.left = new FormAttachment(0);
		fd_commandProcessorComposite.right = new FormAttachment(100);
		commandProcessorComposite.setLayoutData(fd_commandProcessorComposite);
		commandQueueComposite = new CommandQueueComposite(top, SWT.NONE, getSite(), queue);
		FormData formData = new FormData();
		formData.top = new FormAttachment(commandProcessorComposite, 0, SWT.BOTTOM);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		commandQueueComposite.setLayoutData(formData);

		setTitleImage(GDAClientActivator.getImageDescriptor("icons/table_multiple.png").createImage());
		setPartName("Command Queue");
	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void handleCopy(ExecutionEvent event) throws Exception {
		commandQueueComposite.handleCopy(event);
	}

}

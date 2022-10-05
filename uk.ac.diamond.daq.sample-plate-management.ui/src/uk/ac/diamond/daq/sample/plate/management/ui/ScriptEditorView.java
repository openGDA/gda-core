/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;
public class ScriptEditorView {
	private static final Logger logger = LoggerFactory.getLogger(ScriptEditorView.class);

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	private Text scriptText;

	private EventHandler buildScriptHandler = event -> {
		String script = (String) event.getProperty(IEventBroker.DATA);
    	scriptText.setText(script);
    };

    private EventHandler buildAndRunScriptHandler = event -> {
    	String script = (String) event.getProperty(IEventBroker.DATA);
    	scriptText.setText(script);
		ParameterizedCommand command = commandService.createCommand("uk.ac.diamond.daq.sample-plate-management.ui.command.run");
		handlerService.executeHandler(command);
    };

	@Inject
	public ScriptEditorView() {
		logger.trace("Constructor called");
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		Composite child = new Composite(parent, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(child);

		scriptText = addText(child, GridDataFactory.fillDefaults().span(1, 2).grab(true, true), true);

		eventBroker.subscribe(PathscanConfigConstants.TOPIC_BUILD_SCRIPT, buildScriptHandler);
		eventBroker.subscribe(PathscanConfigConstants.TOPIC_BUILD_AND_RUN_SCRIPT, buildAndRunScriptHandler);
	}

	public String getScript() {
		return scriptText.getText();
	}

	private Text addText(Composite parent, GridDataFactory layout, boolean textEnabled) {
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setEnabled(textEnabled);
		layout.applyTo(text);
		return text;
	}
}
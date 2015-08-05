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

package uk.ac.gda.exafs.ui.composites;

import gda.jython.JythonServerFacade;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.ClientManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.ui.components.QueuedCommandWidget;

/**
 * Please set the scriptName and the editor before using the widget.
 */
public final class EstimatePointsWidget extends QueuedCommandWidget {
	private String             scriptName;
	private RichBeanEditorPart editorPart;
	private boolean timeMode = false;

	/**
	 * @param parent
	 * @param style
	 */
	public EstimatePointsWidget(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Designed to be run in another thread.
	 * @return m
	 */
	@Override
	protected String runCommand() throws Exception {
		String command = getCommandLine();
		if (ClientManager.isTestingMode())
			return "Test";
		// Run command and block.
        JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
        Map<String, Serializable> jythonObjects = getBean();
        if (jythonObjects.isEmpty()) throw new Exception("Cannot determine editing bean.");
        for (Map.Entry<String, Serializable> pair : jythonObjects.entrySet())
			jythonServerFacade.placeInJythonNamespace(pair.getKey(), pair.getValue());
		return jythonServerFacade.evaluateCommand(command);
	}

	/**
	 * Designed to be run in another thread.
	 * @return m
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Serializable> getBean() {
		if (!checkActive(this))
			return Collections.EMPTY_MAP;

		final Map<String, Serializable> bean = new HashMap<String, Serializable>(1);
		final File file = EclipseUtils.getFile(editorPart.getEditorInput());

		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					bean.put(getFileKey(file.getName()), (Serializable)editorPart.updateFromUIAndReturnEditingBean());
				} catch (Exception e) {
					// TODO Find out why this sometimes gives unwanted errors.
					//logger.error("Cannot determine editing bean.", e);
				}
			}
		});
		return bean;
	}

	private String getCommandLine() {
		final File file = EclipseUtils.getFile(editorPart.getEditorInput());
		if (timeMode)
			return scriptName+" "+getFileKey(file.getName())+" 1 True";
		return scriptName+" "+getFileKey(file.getName());
	}

	@Override
	public void setLabelText(final String status) {
		if (timeMode) {
			Date date  = getDate(Integer.parseInt(status));
			DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK);
			format.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
			label.setText(format.format(date));
		}
		else
			super.setLabelText(status);
	}

	private Date getDate(long seconds) {
		return new Date(seconds * 1000);
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public RichBeanEditorPart getEditor() {
		return editorPart;
	}

	public void setEditor(RichBeanEditorPart editor) {
		this.editorPart = editor;
	}

	private String getFileKey(final String fileName) {
		return fileName.substring(0,fileName.indexOf("."));
	}

	public void setTimeMode(boolean b) {
		this.timeMode = b;
	}

}
/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.util.function.Consumer;

import org.eclipse.ui.part.ViewPart;

import gda.rcp.views.FindableViewFactoryBase;
import gda.rcp.views.ViewUtils;

public class QxesScanViewFactory extends FindableViewFactoryBase {

	private String templateFileName;
	private String saveOffsetsCommand;
	private String stopScanCommand;
	private String runScanFunctionName;
	private String initialXmlSubFolderName;
	private String viewLabel = "QXes scan view";

	@Override
	public ViewPart createView() {
		QxesScanView qxesScanView = new QxesScanView();
		setIfNotNull(qxesScanView::setTemplateFileName, templateFileName);
		setIfNotNull(qxesScanView::setSaveOffsetsCommand, saveOffsetsCommand);
		setIfNotNull(qxesScanView::setStopScanCommand, stopScanCommand);
		setIfNotNull(qxesScanView::setRunScanFunctionName, runScanFunctionName);
		setIfNotNull(qxesScanView::setInitialXmlSubFolderName, initialXmlSubFolderName);
		ViewUtils.setViewName(qxesScanView, viewLabel);
		return qxesScanView;
	}

	private void setIfNotNull(Consumer<String> consumer, String value) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}

	public void setSaveOffsetsCommand(String saveOffsetsCommand) {
		this.saveOffsetsCommand = saveOffsetsCommand;
	}

	public void setStopScanCommand(String stopScanCommand) {
		this.stopScanCommand = stopScanCommand;
	}

	public void setRunScanFunctionName(String runScanFunctionName) {
		this.runScanFunctionName = runScanFunctionName;
	}

	public void setInitialXmlSubFolderName(String initialXmlSubFolderName) {
		this.initialXmlSubFolderName = initialXmlSubFolderName;
	}
}

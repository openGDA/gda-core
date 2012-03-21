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

package uk.ac.gda.client.microfocus.ui.commands;


import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.beans.exafs.i18.SampleStageParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentProjectNature;
import uk.ac.gda.client.microfocus.util.DisplayMessages;
import uk.ac.gda.client.microfocus.views.ExafsSelectionView;
import uk.ac.gda.exafs.ui.data.ScanObject;

public class UpdateExafsScanCommandHandler extends AbstractHandler implements IHandler {

	private final static Logger logger = LoggerFactory.getLogger(UpdateExafsScanCommandHandler.class);
	protected final IExperimentEditorManager controller = ExperimentFactory.getExperimentEditorManager();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getActivePart(event).getClass().equals(ExafsSelectionView.class)) {
			ExafsSelectionView exafsView = ((ExafsSelectionView) HandlerUtil.getActivePart(event));
			String[] selectedScans = exafsView.getScanSelection();

			if (isValid(selectedScans)) {
				File project = controller.getProjectFolder();
				String folder = selectedScans[0].substring(selectedScans[0].indexOf(")") + 1, selectedScans[0]
						.indexOf(File.separator));
				IExperimentObjectManager newScanManager = null;
				try {
					newScanManager = ExperimentProjectNature.createNewEmptyScan(controller.getIFolder(folder), exafsView
							.getNewMultiScanName(), null);
				} catch (Exception e) {
					logger.error("Unable to create a new MultiScan ");
					return null;
				}
				// create a new mulktiscan
				for (String sScan : selectedScans) {// TODO need to change it to more eclipse way add a seperate class
					// to provide the content
					String xyz = sScan.substring(sScan.indexOf("(") + 1, sScan.indexOf(")"));
					sScan = sScan.substring(sScan.indexOf(")") + 1);
					File scanFile = new File(project.getAbsolutePath() + File.separator + sScan);
					try {
						IExperimentObjectManager scManager = ExperimentFactory
								.getManager(controller.getIFile(scanFile));
						List<IExperimentObject> scanList = scManager.getExperimentList();
						for (IExperimentObject exptScan : scanList) {
							ScanObject scan = (ScanObject)exptScan;
							I18SampleParameters samPam = (I18SampleParameters) scan.getSampleParameters();
							SampleStageParameters sstagePam = samPam.getSampleStageParameters();
							StringTokenizer tokens = new StringTokenizer(xyz, ",");
							double sampleParams[] = new double[tokens.countTokens()];
							int tokenCount = 0;
							while (tokens.hasMoreTokens()) {
								sampleParams[tokenCount] = Double.valueOf(tokens.nextToken());
								tokenCount++;
							}
							sstagePam.setX(sampleParams[0]);
							sstagePam.setY(sampleParams[1]);
							sstagePam.setZ(sampleParams[2]);
							String newSampleFileName = scan.getSampleFileName().substring(0,
									scan.getSampleFileName().indexOf(".xml"))
									+ sampleParams[0] + "_" + sampleParams[1] + "_" + sampleParams[2];
							newSampleFileName = newSampleFileName.replace('.', '-') + ".xml";
							String newSampleFile = project.getAbsolutePath() + File.separator + folder + File.separator
									+ newSampleFileName;
							I18SampleParameters.writeToXML(samPam, newSampleFile);
							ScanObject newScan = (ScanObject) scManager.createCopyOfExperiment(scan);
							newScan.setSampleFileName(newSampleFileName);
							if (newScanManager != null){
								newScan.setRunName(scan.getRunName() + "[" + xyz + "]");
								newScanManager.addExperiment(newScan);
							}
						};
						newScanManager.write();
					} catch (Exception e) {
						logger.error("Unable to create/update the new Multiscan");
						e.printStackTrace();
						return null;
					}
				}
				controller.refreshViewers();
			} else {
				logger.info("All selected scans must be from the same experiment");
				DisplayMessages.showErrorMessage("All selected scans must be from the same experiment");
				return null;
			}
		}

		return null;
	}

	private boolean isValid(String[] selectedScans) {
		String toMatch = selectedScans[0].substring(selectedScans[0].indexOf(")") + 1, selectedScans[0]
				.indexOf(File.separator));
		for (String selection : selectedScans) {
			if (!selection.substring(selection.indexOf(")") + 1, selection.indexOf(File.separator)).equals(toMatch))
				return false;
		}
		return true;

	}

}

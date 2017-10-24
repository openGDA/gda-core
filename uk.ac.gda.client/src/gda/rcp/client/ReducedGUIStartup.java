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

package gda.rcp.client;

import gda.configuration.properties.LocalProperties;
import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.rcp.BatonManagerOnlyPerspective;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReducedGUIStartup implements IStartup {

	private static final Logger logger = LoggerFactory.getLogger(ReducedGUIStartup.class);

	@Override
	public void earlyStartup() {
		// check if conditions are correct for reduced GUI
		if (!checkRunFullGUI()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					closeAllButReducedPerspective();
					// add a listener to prevent other perspectives from opening
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(
							new IPerspectiveListener() {
								@Override
								public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
										String changeId) {
									if (!JythonServerFacade.getInstance().amIBatonHolder()
											&& !perspective.getId().equals(BatonManagerOnlyPerspective.ID)) {
										String message = "You are running a reduced GUI and so you do not have permission to open other perspectives";
										logger.error(message);
										closeAllButReducedPerspective();
									}
								}

								@Override
								public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

									if (!JythonServerFacade.getInstance().amIBatonHolder()
											&& !perspective.getId().equals(BatonManagerOnlyPerspective.ID)) {
										String message = "You are running a reduced GUI and so you do not have permission to open other perspectives!";
										logger.error(message);
										closeAllButReducedPerspective();
									}
								}
							});
				}

				private void closeAllButReducedPerspective() {
					try {
						// class all perspectives in this thread and open reduced GUI perspective
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllPerspectives(
								false, false);
						// open the reduced perspective
						PlatformUI.getWorkbench().showPerspective(BatonManagerOnlyPerspective.ID,
								PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});

		}

	}

	private boolean checkRunFullGUI() {

		// Override any baton behaviour if member of local staff and *do* run full gui.
		try {
			if (AuthoriserProvider.getAuthoriser().isLocalStaff(UserAuthentication.getUsername())) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			logger.warn("Exception while testing if user should run the reduced GUI so test has been skipped.", e);
		}

		// If baton management is enabled and the baton is held by someone on a different visit then *do not* run full gui.
		if (LocalProperties.isBatonManagementEnabled()){

			// return false if not beamline staff and the baton is held by someone on a different visit
			IBatonStateProvider batonStateProvider = InterfaceProvider.getBatonStateProvider();
			if (batonStateProvider.isBatonHeld()
					&& !(batonStateProvider.getBatonHolder().getVisitID().equals(batonStateProvider.getMyDetails()
							.getVisitID()))) {
				logger.warn("Baton held by a user on a different visit. A reduced GUI will be run.");
				return false;
			}
		}

		return true;
	}

}

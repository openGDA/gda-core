/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.gda.client.tomo.test;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.client.tomo.ViewerDisplayMode;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView;
import uk.ac.gda.client.tomo.perspective.TomographyPerspective;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.MenuItemLocator;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.PerspectiveLocator;

public class PluginTestHelpers {
	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	static public void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		}
		// Otherwise, perform a simple sleep.

		else {
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	static public void waitForJobs() {
		while (!Job.getJobManager().isIdle())
			delay(1000);
	}

	public static void openPerspective(IUIContext ui)
			throws WidgetSearchException {
		IWidgetLocator widgetLocator = ui.click(new MenuItemLocator(
				"Window/&Open Perspective/&Other..."));
		System.out.println("WW->" + widgetLocator);

		ui.wait(new ShellShowingCondition("Open Perspective"));

		ui.click(new TableItemLocator("Tomography"));

		ui.click(new ButtonLocator("OK"));

		ui.wait(new PerspectiveLocator(TomographyPerspective.ID).isActive());
	}

	public static boolean isStreaming() {
		final boolean[] val = new boolean[1];
				
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				ViewerDisplayMode dm = ((TomoAlignmentView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().getActivePart())
						.getLeftWindowViewerDisplayMode();
				val[0] =  dm == ViewerDisplayMode.FLAT_STREAM_LIVE
						|| dm == ViewerDisplayMode.SAMPLE_STREAM_LIVE;
			}
		});
		
		return val[0];
	}

}

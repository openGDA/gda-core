package uk.ac.diamond.tomography.reconstruction.views;

import java.io.IOException;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.Test;

public class ParameterViewTest {

	private ParameterView view;

	@Test
	public void test1() throws IOException, PartInitException {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		this.view = (ParameterView)window.getActivePage().showView(ParameterView.ID);
		window.getActivePage().activate(view);
		
		ActionFactory.IWorkbenchAction  maximizeAction = ActionFactory.MAXIMIZE.create(window);
		maximizeAction.run(); // Will maximize the active part		
		waitForJobs();
		delay(3000);		
		
		delay(150000); //time to 'play with the graph if wanted
	}

	
	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private void delay(long waitTimeMillis) {
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

	/**
	 * Wait until all background tasks are complete.
	 */
	public void waitForJobs() {
		while (Job.getJobManager().currentJob() != null)
			delay(1000);
	}



}

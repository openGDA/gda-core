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

package uk.ac.gda.ui.dialog;

import static org.junit.Assert.assertEquals;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

/**
 * Not ready for including in a test suite yet!
 */
public class VisitIDDialogPluginTest {
	private VisitIDDialog visitDialog;

	@Before
	public void setUp() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
//		Display display = new Display();
//		final Shell shell = new Shell(display,SWT.SHELL_TRIM);
////		window.getShell()

		String[][] ids = new String[2][];
		ids[0] = new String[] { "mx-123_1", "mx-123", " experiment 123" };
		ids[1] = new String[] { "mx-456_2", "mx-456", " experiment 456" };

		visitDialog = new VisitIDDialog(window.getShell().getDisplay(), ids);
//		visitDialog = new VisitIDDialog(display, ids);
		
	
//		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
//			public void run(){
//				visitDialog.open();
//				
//			}
//		});
//
//		new Thread(new Runnable(){
//			public void run(){
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				TableItem item = visitDialog.userTable.getTable().getItem(0);
//				visitDialog.userTable.setSelection(new StructuredSelection(item.getData()));
//
//				visitDialog.buttonPressed(IDialogConstants.OK_ID);
//				
//			}
//		}).start();
	}

	@Test
	public void testSelection() {
//		try {
//			Thread.sleep(1500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		visitDialog.open();
		String chosenVisit = visitDialog.getChoosenID();
		assertEquals("mx-123_1", chosenVisit);
		
		
	}
}

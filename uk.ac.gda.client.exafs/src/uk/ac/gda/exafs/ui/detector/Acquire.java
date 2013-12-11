/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;

import com.swtdesigner.SWTResourceManager;

public class Acquire {
	protected Button acquireBtn;
	protected boolean saveMcaOnAcquire;
	protected Button autoSave;
	protected Label acquireFileLabel;
	
	public Acquire() {
	}
	
	public void acquire(IProgressMonitor monitor, double collectionTimeValue) throws Exception {
		
	}
	
	public void acquireStarted() {
		acquireBtn.setText("Stop");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/stop.png"));
		autoSave.setEnabled(false);
		acquireFileLabel.setText("										");
		//live.setEnabled(false);
	}

	public void acquireFinished() {
		acquireBtn.setText("Acquire");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/application_side_expand.png"));
		autoSave.setEnabled(false);
		//live.setEnabled(true);
	}
	
}
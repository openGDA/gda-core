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

package uk.ac.gda.components.wrappers;

import gda.factory.Finder;
import gda.jython.JythonServerFacade;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

import com.swtdesigner.SWTResourceManager;

/**
 * Classes in this package are extending the base widgets in common.rcp but they connect to parts of the GDA subsystem
 * and are not independent of GDA. This class allows the user to type the name of a findable and shows an error if it is
 * not findable. You can also set a custom icon to show if the findable is the required class.
 */
public class FindableNameWrapper extends TextWrapper {

	protected CLabel messageLabel;
	protected Image errorImage, nameImage, checkingImage;
	protected Class<? extends Object> findableClass;
	private ModifyListener modifyListener;
	private boolean found = false;
	private boolean labelOnRight;

	public FindableNameWrapper(Composite parent, int style, final Class<? extends Object> findableClass) {
		this(parent, style, findableClass, true);
	}
	
	/**
	 * Create widget which checks name defined is a findable instance of the class passed in.
	 * 
	 * @param parent
	 * @param style
	 * @param findableClass
	 */
	public FindableNameWrapper(Composite parent, int style, final Class<? extends Object> findableClass,  boolean labelOnRight) {

		super(parent, style);
		
		this.labelOnRight = labelOnRight;
		
		this.findableClass = findableClass;
		if (labelOnRight) {
			this.messageLabel = new CLabel(this, SWT.NONE);
			messageLabel.setLayoutData(BorderLayout.EAST);
		}

		this.errorImage = SWTResourceManager.getImage(FindableNameWrapper.class, "/icons/error.png");
		this.nameImage = SWTResourceManager.getImage(FindableNameWrapper.class, "/icons/tick.png");
		this.checkingImage = SWTResourceManager.getImage(FindableNameWrapper.class, "/icons/eye.png");

		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				checkName();
			}
		};
		text.addModifyListener(modifyListener);
	}
	

	@Override
	public void dispose() {
		if (text != null && !text.isDisposed()) {
			text.removeModifyListener(modifyListener);
			text.dispose();
		}
		super.dispose();
	}

	@Override
	public void setValue(final Object value) {
		super.setValue(value);
		checkName();
	}

	/** One finderRule per instance of FindableNameWrapper */
	private final ISchedulingRule finderRule = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	private String mostRecentStarted;

	private class FinderJob extends Job {
		private String name;

		public FinderJob(String name) {
			super("Finding " + name);
			if (name == null) {
				throw new NullPointerException("name can't be null");
			}
			this.name = name;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Object ob = Finder.getInstance().findNoWarn(name);
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if (name.equals(mostRecentStarted)) {
						// Only do the update if there isn't another job about to come along behind us
						if (ob != null) {
							if (!(findableClass.isInstance(ob))) {
								setWrongNameError(name);
							} else {
								setRightName(name);
							}
						} else {
							Object jythonServerResult = JythonServerFacade.getInstance().evaluateCommand(name);
							if (jythonServerResult != null)
								setRightName(name);
							else
								setNotFindableError(name);
						}
					}
				}
			});

			return Status.OK_STATUS;
		}

		private boolean worthRunning() {
			return name.equals(mostRecentStarted);
		}

		@Override
		public boolean shouldSchedule() {
			return super.shouldSchedule() && worthRunning();
		}

		@Override
		public boolean shouldRun() {
			return super.shouldRun() && worthRunning();
		}

	}

	/**
	 * Refresh by trying to find, omit checking name during internal transitions
	 */
	protected void checkName() {
		if (!isActivated() || !isOn())
			return;
		refresh();
	}

	/**
	 * Refresh by trying to find. This is a forced refresh that will run regardless of state.
	 */
	public void refresh() {
		final Object newValue = getValue();
		if (newValue == null || "".equals(newValue)) {
			mostRecentStarted = null;
			setNotFindableError(null);
			return;
		}

		setCheckingName();
		final String name = (String) newValue;
		mostRecentStarted = name;

		FinderJob finderJob = new FinderJob(name);
		finderJob.setRule(finderRule);
		finderJob.setUser(false);
		finderJob.schedule();
	}

	public boolean isFound() {
		return found;
	}

	protected void setRightName(String name) {
		if (labelOnRight)
			messageLabel.setImage(getNameImage());
		text.setForeground(BLACK);
		setToolTipText("The name '" + name + "' exists and is the correct type.");
		GridUtils.layout(this);
		found = true;
	}

	protected void setCheckingName() {
		if (labelOnRight)
			messageLabel.setImage(getCheckingImage());
		text.setForeground(DARK_RED);
		setToolTipText("The name is being searched.");
		GridUtils.layout(this);
		found = false;
	}

	protected void setWrongNameError(String name) {
		if(labelOnRight)
			messageLabel.setImage(getErrorImage());
		setToolTipText("The name '" + name + "' references the wrong type of GDA object.");
		text.setForeground(RED);
		GridUtils.layout(this);
		found = false;
	}

	protected void setNotFindableError(final String name) {
		if(labelOnRight)
			messageLabel.setImage(getErrorImage());
		if (name == null) {
			setToolTipText("Please enter a name.");
		} else {
			setToolTipText("Cannot find '" + name + "'");
		}
		text.setForeground(RED);
		GridUtils.layout(this);
		found = false;
	}

	@Override
	public void setToolTipText(final String text) {
		super.setToolTipText(text);
		if(labelOnRight)
			this.messageLabel.setToolTipText(text);
		this.text.setToolTipText(text);
	}

	/**
	 * @return Returns the errorImage.
	 */
	public Image getErrorImage() {
		return errorImage;
	}

	/**
	 * @param errorImage
	 *            The errorImage to set.
	 */
	public void setErrorImage(Image errorImage) {
		this.errorImage = errorImage;
	}

	/**
	 * @return Returns the nameImage.
	 */
	public Image getNameImage() {
		return nameImage;
	}

	/**
	 * @param nameImage
	 *            The nameImage to set.
	 */
	public void setNameImage(Image nameImage) {
		this.nameImage = nameImage;
	}

	/**
	 * @return Returns the checkingImage.
	 */
	public Image getCheckingImage() {
		return checkingImage;
	}

	/**
	 * @param image
	 *            The checkingImage to set.
	 */
	public void setCheckingImage(Image image) {
		this.checkingImage = image;
	}
}

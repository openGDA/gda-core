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

package gda.rcp.views.dashboard;

import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannableStatus;
import gda.observable.IObserver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.ui.PlatformUI;

/**
 * A class to hold information about the object viewed in the dashboard.
 * 
 * Will be serialized to XML.
 */
public abstract class ServerObject implements IObserver {
	
	// Only one 
	private static transient Timer                timer;
	
	// Not XML serializable.
	protected transient Set<ServerObjectListener> listeners;
	protected transient TimerTask currentTask;
	protected transient volatile Object value;
	protected transient Object maximum;
	protected transient Object minimum;

	// Serialized
	protected String tooltip;
	protected String  label,unit;
	protected boolean error;
	protected String  className;
	protected String  description;
	
	// Abstract
	protected abstract void updateValue(final Object scannable);
    protected abstract void connect() throws Exception;
 	
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setObservableValue(Object value) {
		if (value instanceof double[]) {
			final double[] valueArray = (double[])value;
			final Double[] da = new Double[valueArray.length];
			for (int i = 0; i < da.length; i++) da[i] = valueArray[i];
			value = Arrays.asList(da).toString();
		}
		this.value = value;
	}
	/**
	 * @return Returns the unit.
	 */
	public String getUnit() {
		return unit;
	}
	/**
	 * @param unit The unit to set.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * @param l
	 */
	public void addServerObjectListener(ServerObjectListener l) {
		if (listeners==null) listeners = new HashSet<ServerObjectListener>(7);
		listeners.add(l);
	}
	
	/**
	 * @param l
	 */
	public void removeServerObjectListener(ServerObjectListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	/**
	 * May be called by any thread, ends up notifying on the display thread.
	 * @param evt
	 */
	protected void notifyServerObjectListeners(final ServerObjectEvent evt) {
		if (listeners==null) return;
		if (PlatformUI.getWorkbench().getDisplay().isDisposed()) return;
		if (PlatformUI.getWorkbench().getDisplay().getThread()!=Thread.currentThread()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					notifyUprotected(evt);
				}
			});
		} else {
			notifyUprotected(evt);
		}
	}
	private void notifyUprotected(ServerObjectEvent evt) {
		for (ServerObjectListener l : listeners) l.serverObjectChangePerformed(evt);
	}
		
	@Override
	public void update(final Object theObserved, final Object changeCode) {

		if (changeCode instanceof ScannableStatus) {
			final ScannableStatus status = (ScannableStatus)changeCode;
		    if (status.getStatus()!=ScannableStatus.BUSY) {
		    	stopUpdateTask(theObserved);
		    } else {
		    	updateValue(theObserved);
		    }
		    
		} else if (changeCode instanceof EnumPositionerStatus) {
		    doSingleTask(theObserved);
		}
	}
	
	private void stopUpdateTask(Object theObserved) {
		if (currentTask!=null) {
			currentTask.cancel();
			currentTask = null;
		}
		
		// Sometimes we get told it stopped moving without being
		// told it started.
		doSingleTask(theObserved);
	}
	
	protected void doSingleTask(final Object theObserved) {
		// Creates thread and has it wait for tasks so
		// we only create the timer if it is really needed.
		// The thread only exists while the object is added to the dashboard.
		// If the user deletes it, the thread is terminated. The thread
		// is not paused using a sleep, it only wakes up when the 
		// timer task runs. 
		if (timer==null) timer = new Timer("Dashboard Timer", true); 
   	    currentTask = new TimerTask() {
    		@Override
			public void run() {
    			updateValue(theObserved);
    		}
    	};
    	timer.schedule(currentTask, 0);
	}
	
	
	protected static void cancelTimer() {
		if (timer!=null) {
			timer.cancel();
		}
		timer = null;
	}

	/**
	 * Override if required.
	 */
    protected void disconnect()  {
    	if (currentTask!=null) {
    		currentTask.cancel();
    	}
    }
	
	/**
	 * Called if the object is deleted from the dashboard.
	 * Stops any monitor threads.
	 */
	public void delete() {
		if (currentTask!=null) currentTask.cancel();
	}
	/**
	 * @return Returns the tooltip.
	 */
	public String getTooltip() {
		return tooltip;
	}
	/**
	 * @param tooltip The tooltip to set.
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	/**
	 * @return Returns the error.
	 */
	public boolean isError() {
		return error;
	}
	/**
	 * @param error The error to set.
	 */
	public void setError(boolean error) {
		this.error = error;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/**
	 * @return Returns the maximum.
	 */
	public Object getMaximum() {
		return maximum;
	}
	/**
	 * @param maximum The maximum to set.
	 */
	public void setMaximum(Object maximum) {
		this.maximum = maximum;
	}
	/**
	 * @return Returns the minimum.
	 */
	public Object getMinimum() {
		return minimum;
	}
	/**
	 * @param minimum The minimum to set.
	 */
	public void setMinimum(Object minimum) {
		this.minimum = minimum;
	}
	/**
	 * @return Returns the className.
	 */
	protected String getClassName() {
		return className;
	}
	/**
	 * @param className The className to set.
	 */
	protected void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return Returns the description.
	 */
	protected String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	protected void setDescription(String description) {
		this.description = description;
	}

}
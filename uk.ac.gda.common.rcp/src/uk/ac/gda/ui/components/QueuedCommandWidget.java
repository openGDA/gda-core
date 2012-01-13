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

package uk.ac.gda.ui.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 * Widget for showing the result of a command. The command is 
 * designed to be a Jython command run with the evaluateCommand
 * call but can be any command at all. The runCommand() method must
 * be implemented and is called on the QueueThread. This thread is
 * static to all QueuedCommandWidget to avoid too many threads existing
 * by default. Alternatively the class may be overridden to use a 
 * different thread.
 * 
 * This widget can be used by implementors of IFieldWidget.
 */
public abstract class QueuedCommandWidget extends Composite {

	private static Logger logger = LoggerFactory.getLogger(QueuedCommandWidget.class);
	
	private static Thread                        mainQueueThread;
	private static BlockingQueue<QueuedCommandWidget> queue;
	
	protected Label label;
	protected Link  runCommand;

	private SelectionAdapter selectionListener;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public QueuedCommandWidget(Composite parent, int style) {
		
		super(parent, style);
		
		initQueue();
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		label      = new Label(this, SWT.NONE);
		runCommand = new Link(this, SWT.NONE);
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = null;
				try {
					text = runCommand();
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), "Cannot Connect to Command", e1.getMessage());
				}
				setLabelText(text);
				QueuedCommandWidget.this.layout();
			}

		};
		runCommand.addSelectionListener(selectionListener);
	}
	
	@Override
	public void dispose() {
		runCommand.removeSelectionListener(selectionListener);
		super.dispose();
	}

	/**
	 * Override to use different thread or queue. Not recommended.
	 */
	protected static void initQueue() {
		if (queue==null) {
			queue           = new ArrayBlockingQueue<QueuedCommandWidget>(3);
		}
		if (mainQueueThread==null) {
			mainQueueThread = uk.ac.gda.util.ThreadManager.getThread(getRunnable(), "QueuedCommandWidget thread. Used to updated all "+QueuedCommandWidget.class.getName()+"'s");
			mainQueueThread.start();
		}	
	}
	
	protected static void clear() {
		if (mainQueueThread!=null) {
			try {
				mainQueueThread.interrupt();
			} catch (Exception ignored) {
				// We are done with the thread.
			}
		}
		mainQueueThread = null;
		queue.clear(); // No point making a new one.
	}
	
	/**
	 * Override if required, defines the work done by the queue thread.
	 * @return Runnable
	 */
	protected static Runnable getRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						final QueuedCommandWidget req  = queue.take();
						if (!checkActive(req)) return;
						final String       status = req.runCommand();
						if (req.isDisposed()) return;
						req.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								req.setLabelText(status);
								req.fireValueChangeListeners(status);
								req.layout();
							}
						});
					} catch (Exception e) {
						logger.error("Cannot run command in "+QueuedCommandWidget.class.getName(), e);
					}
				}
			}

		};
	}
	
	
	protected Collection<ValueListener> listeners;

	protected void fireValueChangeListeners(String status) {
		if (listeners==null) return;
		final ValueEvent e = new ValueEvent(this,null);
		e.setValue(status);
		for (ValueListener l : listeners) l.valueChangePerformed(e);	
	}
	
	/**
	 * @param l
	 */
	public void addValueChangeListener(ValueListener l) {
		if (listeners==null) listeners = new HashSet<ValueListener>(3);
		listeners.add(l);
	}

	protected static boolean checkActive(QueuedCommandWidget req) {
		if (Thread.currentThread().isInterrupted()||req.isDisposed()) {
			clear();
			return false;
		}
		try {
			req.getDisplay().isDisposed();
		} catch (Exception ne) {
			clear();
			return false;
		}
		return true;
	}
	/**
	 * Please implement to run the command which should set status in the label.
	 * If you throw an exception the message is shown to the user as a dialog, so
	 * please catch possible command exceptions if their message is not user worthy.
	 * 
	 * NOTE This method is not called in the UI thread. It should not do UI work and
	 * should interact with a service on the server that takes a while to run. Very long
	 * running tasks should not use this class as no feedback is replied to the user about
	 * if the task has completed.
	 * 
	 * @return string, result of the command.
	 */
	protected abstract String runCommand() throws Exception;
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected Link getRunCommand() {
		return runCommand;
	}
	
	protected Label getLabel() {
		return label;
	}
	
    /**
     * @param text
     */
    public void setLinkText(final String text) {
    	runCommand.setText("<a>"+text+"</a>");
    }
    
    /**
     * @param status
     */
    public void setLabelText(final String status) {
		if (getUnit()==null) {
			label.setText(status);
		} else {
			label.setText(status+" "+getUnit());
		}
    }
    
    /**
     * @param text
     */
    public void setLinkTooltip(final String text) {
    	runCommand.setToolTipText(text);
    }
    
    /**
     * @param text
     */
    public void setLabelTooltip(final String text) {
    	label.setToolTipText(text);
    }
    /**
     * @return s
     */
    public String getLinkText() {
    	return runCommand.getText();
    }
    
    /**
     * @return s
     */
    public String getLabelText() {
    	return label.getText();
    }
    
    /**
     * @return s
     */
    public String getLinkTooltip() {
    	return runCommand.getToolTipText();
    }
    
    /**
     * @return s
     */
    public String getLabelTooltip() {
    	return label.getToolTipText();
    }
        
    /**
     * Call this method to request an update of the value at some point in the 
     * future. This will call runCommand() on a thread used to process the 
     * commands and set the text of the label field.
     */
	public void updateValue() {
		try {
			initQueue();
    	    queue.add(this);
		} catch (IllegalStateException full) {
			// Do nothing, you cannot have more than three.
		}
    }
	
	protected String unit;
	/**
	 * @return f
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

}

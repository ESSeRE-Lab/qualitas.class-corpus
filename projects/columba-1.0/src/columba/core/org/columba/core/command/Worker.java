// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.command;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

import org.columba.api.command.IWorkerStatusChangeListener;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.command.WorkerStatusChangedEvent;
import org.columba.api.exception.IExceptionListener;
import org.columba.core.base.SwingWorker;

/**
 * Worker additionally sends status information updates to the
 * {@link TaskManager}.
 * <p>
 * This updates get displayed in the StatusBar.
 * <p>
 * Note that {@link Command}objects get {@link Worker}objects only when
 * executed.
 * 
 * @author fdietz
 */
public class Worker extends SwingWorker implements IWorkerStatusController {

	private static final Logger LOG = Logger
			.getLogger("org.columba.api.command");

	/**
	 * Constant definining the delay used when using
	 * clearDisplayTextWithDelay(). Defined to be 500 millisec.
	 */
	private static final int CLEAR_DELAY = 500;

	protected Command op;

	protected int operationMode;

	protected CommandProcessor boss;

	protected String displayText;

	protected int progressBarMax;

	protected int progressBarValue;

	protected boolean cancelled;

	protected List workerStatusChangeListeners;

	private int timeStamp;

	protected EventListenerList listenerList = new EventListenerList();

	public Worker(CommandProcessor parent) {
		super();

		this.boss = parent;

		displayText = "";
		progressBarValue = 0;
		progressBarMax = 0;

		cancelled = false;

		workerStatusChangeListeners = new Vector();
	}

	public void process(Command op, int operationMode, int timeStamp) {
		this.op = op;
		this.operationMode = operationMode;
		this.timeStamp = timeStamp;
	}

	public int getPriority() {
		return op.getPriority();
	}

	private void returnLocks(int opMode) {
		op.releaseAllFolderLocks();
	}

	/**
	 * Method runs in background. Every Command.execute method is wrapped here.
	 * <p>
	 * All general exceptions are caught here, nice error dialogs shown to the
	 * users.
	 * 
	 * @see org.columba.core.base.SwingWorker#construct()
	 */
	public Object construct() {

		try {
			op.process(this);

		} catch (CommandCancelledException e) {
			LOG.info("Command cancelled: " + this);
		} catch (Exception e) {

			// exception handler should handle all error handling stuff
			fireExceptionOccured(e);
		}

		returnLocks(operationMode);

		return null;
	}

	public void finished() {
		try {
			op.finish();
		} catch (Exception e) {
			// Must create a ExceptionProcessor
			e.printStackTrace();
		}

		unregister();
		boss.operationFinished(op, this);
	}

	private void unregister() {
		TaskManager.getInstance().unregister(threadVar);

		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent(this,
				getTimeStamp());
		e.setType(WorkerStatusChangedEvent.FINISHED);
		fireWorkerStatusChanged(e);
		workerStatusChangeListeners.clear();
		displayText = "";
		progressBarValue = 0;
		progressBarMax = 0;
	}

	/**
	 * Sets the maximum value for the progress bar.
	 * 
	 * @param max
	 *            New max. value for progress bar
	 */
	public void setProgressBarMaximum(int max) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent(this,
				getTimeStamp());
		e.setType(WorkerStatusChangedEvent.PROGRESSBAR_MAX_CHANGED);
		e.setOldValue(new Integer(progressBarMax));

		progressBarMax = max;

		e.setNewValue(new Integer(progressBarMax));
		fireWorkerStatusChanged(e);
	}

	/**
	 * Sets the current value of the progress bar.
	 * 
	 * @param value
	 *            New current value of progress bar
	 */
	public void setProgressBarValue(int value) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent(this,
				getTimeStamp());
		e.setType(WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED);
		e.setOldValue(new Integer(progressBarValue));

		progressBarValue = value;

		e.setNewValue(new Integer(progressBarValue));
		fireWorkerStatusChanged(e);
	}

	/**
	 * Sets the progress bar value to zero, i.e. clears the progress bar. This
	 * is the same as calling setProgressBarValue(0)
	 */
	public void resetProgressBar() {
		setProgressBarValue(0);
	}

	/**
	 * Returns the max. value for the progress bar
	 */
	public int getProgessBarMaximum() {
		return progressBarMax;
	}

	/**
	 * Returns the current value for the progress bar
	 */
	public int getProgressBarValue() {
		return progressBarValue;
	}

	/**
	 * Returns the text currently displayed in the status bar
	 */
	public String getDisplayText() {
		return displayText;
	}

	/**
	 * Set the text to be displayed in the status bar
	 * 
	 * @param text
	 *            Text to display in status bar
	 */
	public void setDisplayText(String text) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent(this,
				getTimeStamp());
		e.setType(WorkerStatusChangedEvent.DISPLAY_TEXT_CHANGED);
		e.setOldValue(displayText);

		displayText = text;

		e.setNewValue(displayText);
		fireWorkerStatusChanged(e);
	}

	/**
	 * Clears the text displayed in the status bar - without any delay
	 */
	public void clearDisplayText() {
		clearDisplayText(0);
	}

	/**
	 * Clears the text displayed in the status bar - with a given delay. The
	 * delay used is 500 ms. <br>
	 * If a new text is set within this delay, the text is not cleared.
	 */
	public void clearDisplayTextWithDelay() {
		clearDisplayText(CLEAR_DELAY);
	}

	/**
	 * Clears the text displayed in the status bar - with a given delay. If a
	 * new text is set within this delay, the text is not cleared.
	 * 
	 * @param delay
	 *            Delay in milliseconds before clearing the text
	 */
	private void clearDisplayText(int delay) {
		// init event
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent(this,
				getTimeStamp());
		e.setType(WorkerStatusChangedEvent.DISPLAY_TEXT_CLEARED);

		// "new value" is used to pass on the delay
		e.setNewValue(new Integer(delay));

		// set display text stored here to an empty string (~ cleared)
		displayText = "";

		// fire event
		fireWorkerStatusChanged(e);
	}

	public void addWorkerStatusChangeListener(IWorkerStatusChangeListener l) {
		workerStatusChangeListeners.add(l);
	}

	public void removeWorkerStatusChangeListener(IWorkerStatusChangeListener l) {
		workerStatusChangeListeners.remove(l);
	}

	protected void fireWorkerStatusChanged(WorkerStatusChangedEvent e) {
		// if we use the commented statement, the exceptio
		// java.util.ConcurrentModificationException
		// is thrown ... is the worker not thread save?
		// for (Iterator it = workerStatusChangeListeners.iterator();
		// it.hasNext();) {
		// ((WorkerStatusChangeListener) it.next()).workerStatusChanged(e);
		for (int i = 0; i < workerStatusChangeListeners.size(); i++) {
			((IWorkerStatusChangeListener) workerStatusChangeListeners.get(i))
					.workerStatusChanged(e);
		}
	}

	public void cancel() {
		cancelled = true;
	}

	public boolean cancelled() {
		return cancelled;
	}

	/**
	 * Returns the timeStamp.
	 * 
	 * @return int
	 */
	public int getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Adds a listener.
	 */
	public void addExceptionListener(IExceptionListener l) {
		listenerList.add(IExceptionListener.class, l);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeExceptionListener(IExceptionListener l) {
		listenerList.remove(IExceptionListener.class, l);
	}

	/**
	 * Notify all listeners of the exception.
	 * 
	 * @param e
	 *            exception
	 */
	private void fireExceptionOccured(Exception e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IExceptionListener.class) {
				((IExceptionListener) listeners[i + 1]).exceptionOccured(e);
			}
		}
	}

	/**
	 * @see org.columba.core.base.SwingWorker#start()
	 */
	public Thread start() {
		TaskManager.getInstance().register(this);

		return super.start();
	}
}
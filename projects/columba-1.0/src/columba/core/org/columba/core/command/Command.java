//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.command;

import org.columba.api.command.ICommand;
import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.base.Lock;

/**
 * A Command uses the information provided from {@link DefaultCommandReference}
 * to execute itself.
 * <p>
 * TODO: remove IFrameMediator dependency
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public abstract class Command implements ICommand {

	/**
	 * Commands that can not be undone but previous commands can be undone, e.g.
	 * view message (default) line for constructor: commandType =
	 * Command.NORMAL_OPERATION;
	 */
	public static final int NORMAL_OPERATION = 1;

	/**
	 * Priorities: Commands that are started by an automated process, e.g.
	 * auto-check for new messages
	 */
	public static final int DAEMON_PRIORITY = -10;

	/**
	 * Normal priority for e.g. copying (default)
	 */
	public static final int NORMAL_PRIORITY = 0;

	/**
	 * Commands that the user waits for to finish, e.g. view message
	 */
	public static final int REALTIME_PRIORITY = 10;

	/**
	 * Never Use this!! - internally highest priority
	 */
	public static final int DEFINETLY_NEXT_OPERATION_PRIORITY = 20;

	/**
	 * Never use these!!! - for internal state control only
	 */
	public static final int FIRST_EXECUTION = 0;

	protected int priority;

	protected int commandType;

	protected boolean synchronize;

	protected int timeStamp;

	protected Lock[] folderLocks;

	private ICommandReference reference;

	public Command(ICommandReference reference) {
		this.reference = reference;

		commandType = NORMAL_OPERATION;
		priority = NORMAL_PRIORITY;
	}

	public void process(Worker worker) throws Exception {
		setTimeStamp(worker.getTimeStamp());
		execute(worker);
	}

	/* (non-Javadoc)
	 * @see org.columba.api.command.ICommand#updateGUI()
	 */
	public void updateGUI() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.columba.api.command.ICommand#execute(org.columba.api.command.IWorkerStatusController)
	 */
	public abstract void execute(IWorkerStatusController worker)
			throws Exception;

	public boolean canBeProcessed() {

		boolean success = reference.tryToGetLock(this);
		if (!success) {
			releaseAllFolderLocks();
		}
		return success;

	}

	public void releaseAllFolderLocks() {

		reference.releaseLock(this);

	}

	/** *********** Methods for interacting with the Operator ************ */

	public int getCommandType() {
		return commandType;
	}

	public int getPriority() {
		return priority;
	}

	public void incPriority() {
		priority++;
	}

	public boolean isSynchronize() {
		return synchronize;
	}

	public void setSynchronize(boolean synchronize) {
		this.synchronize = synchronize;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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
	 * Sets the timeStamp.This method is for testing only!
	 * 
	 * @param timeStamp
	 *            The timeStamp to set
	 */
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	/* (non-Javadoc)
	 * @see org.columba.api.command.ICommand#getReference()
	 */
	public ICommandReference getReference() {
		return reference;
	}

	public void finish() throws Exception {
		updateGUI();
	}
}
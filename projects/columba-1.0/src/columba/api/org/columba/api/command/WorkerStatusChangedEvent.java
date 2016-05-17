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
package org.columba.api.command;

public class WorkerStatusChangedEvent {
	public final static int DISPLAY_TEXT_CHANGED = 0;

	public final static int PROGRESSBAR_VALUE_CHANGED = 1;

	public final static int PROGRESSBAR_MAXANDVALUE_CHANGED = 2;

	public final static int PROGRESSBAR_MAX_CHANGED = 3;

	public final static int FINISHED = 4;

	public final static int DISPLAY_TEXT_CLEARED = 5;

	private int type;

	private Object oldValue;

	private Object newValue;

	private int timeStamp;

	private IWorkerStatusController source;

	public WorkerStatusChangedEvent(IWorkerStatusController source,
			int timeStamp) {
		this.timeStamp = timeStamp;
		this.source = source;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * @return Returns the timeStamp.
	 */
	public int getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @return Returns the source.
	 */
	public IWorkerStatusController getSource() {
		return source;
	}
}

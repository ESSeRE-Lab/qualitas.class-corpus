/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.bridge;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
  * Wrap message with any associated throwable or source location.
  */
public interface IMessage {
	/** no messages */
	public static final IMessage[] RA_IMessage = new IMessage[0];

	// int values must sync with KINDS order below
	public static final Kind WEAVEINFO = new Kind("weaveinfo",5);
	public static final Kind INFO = new Kind("info", 10);
	public static final Kind DEBUG = new Kind("debug", 20);
	public static final Kind TASKTAG = new Kind("task",25); // represents a 'TODO' from eclipse - producted by the compiler and consumed by AJDT
	public static final Kind WARNING = new Kind("warning", 30);
	public static final Kind ERROR = new Kind("error", 40);
	public static final Kind FAIL = new Kind("fail", 50);
	public static final Kind ABORT = new Kind("abort", 60);
	// XXX prefer another Kind to act as selector for "any",
	// but can't prohibit creating messages with it.
	//public static final Kind ANY = new Kind("any-selector", 0);

	/** list of Kind in precedence order. 0 is less than 
	 * IMessage.Kind.COMPARATOR.compareTo(KINDS.get(i), KINDS.get(i + 1))
	 */
	public static final List KINDS =
		Collections.unmodifiableList(
			Arrays.asList(
				new Kind[] { WEAVEINFO, INFO, DEBUG, TASKTAG, WARNING, ERROR, FAIL, ABORT }));

	/** @return non-null String with simple message */
	String getMessage();

	/** @return the kind of this message */
	Kind getKind();

	/** @return true if this is an error */
	boolean isError();

	/** @return true if this is a warning */
	boolean isWarning();

	/** @return true if this is an internal debug message */
	boolean isDebug();

	/** @return true if this is information for the user  */
	boolean isInfo();

	/** @return true if the process is aborting  */
	boolean isAbort(); // XXX ambiguous

	/** @return true if this is a task tag message */
	boolean isTaskTag();
	
	/** @return true if something failed   */
	boolean isFailed();

	/** Caller can verify if this message came about because of a DEOW */
	boolean getDeclared();
	
	/** Return the ID of the message where applicable, see IProblem for list of valid IDs */
	int getID();
	
	/** Return the start position of the problem (inclusive), or -1 if unknown. */
	int getSourceStart();
	
	/** Return the end position of the problem (inclusive), or -1 if unknown. */
	int getSourceEnd();
	
	/** @return Throwable associated with this message, or null if none */
	Throwable getThrown();

	/** @return source location associated with this message, or null if none */
	ISourceLocation getSourceLocation();

	public static final class Kind implements Comparable {
		public static final Comparator COMPARATOR = new Comparator() {
			public int compare(Object o1, Object o2) {
				Kind one = (Kind) o1;
				Kind two = (Kind) o2;
				if (null == one) {
					return (null == two ? 0 : -1);
				} else if (null == two) {
					return 1;
				} else if (one == two) {
					return 0;
				} else {
					return (one.precedence - two.precedence);
				}
			}
		};
        
        /**
         * @param kind the Kind floor
         * @return false if kind is null or this
         *         has less precedence than kind,
         *         true otherwise.
         */
		public boolean isSameOrLessThan(Kind kind) {
            return (0 >= COMPARATOR.compare(this, kind));
		}
        
		public int compareTo(Object other) {
			return COMPARATOR.compare(this, other);
		}

		private final int precedence;
		private final String name;

		private Kind(String name, int precedence) {
			this.name = name;
			this.precedence = precedence;
		}
		public String toString() {
			return name;
		}
	}

	/**
	 * @return	Detailed information about the message. For example, for declare 
	 * error/warning messages this returns information about the corresponding 
	 * join point's static part. 
	 */
	public String getDetails();
    
	
	/** 
	 * @return List of <code>ISourceLocation</code> instances that indicate 
	 * additional source locations relevent to this message as specified by the 
	 * message creator. The list should not include the primary source location 
	 * associated with the message which can be obtained from 
	 * <code>getSourceLocation()<code>. 
	 * <p>   
	 * An example of using extra locations would be in a warning message that 
	 * flags all shadow locations that will go unmatched due to a pointcut definition 
	 * being based on a subtype of a defining type. 
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=41952">AspectJ bug 41952</a> 
	 */ 
    public List getExtraSourceLocations();
}

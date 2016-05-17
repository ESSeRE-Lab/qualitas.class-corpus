/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {
	 public final static int

     ERROR_SYMBOL      = 117,
     MAX_NAME_LENGTH   = 41,
     NUM_STATES        = 1132,

     NT_OFFSET         = 117,
     SCOPE_UBOUND      = 181,
     SCOPE_SIZE        = 182,
     LA_STATE_OFFSET   = 15535,
     MAX_LA            = 1,
     NUM_RULES         = 885,
     NUM_TERMINALS     = 117,
     NUM_NON_TERMINALS = 372,
     NUM_SYMBOLS       = 489,
     START_STATE       = 927,
     EOFT_SYMBOL       = 74,
     EOLT_SYMBOL       = 74,
     ACCEPT_ACTION     = 15534,
     ERROR_ACTION      = 15535;
}

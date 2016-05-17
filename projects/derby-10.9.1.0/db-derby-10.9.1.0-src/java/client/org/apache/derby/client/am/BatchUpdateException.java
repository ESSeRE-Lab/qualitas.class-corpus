/*

   Derby - Class org.apache.derby.client.am.BatchUpdateException

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.apache.derby.client.am;

import org.apache.derby.shared.common.i18n.MessageUtil;
import org.apache.derby.shared.common.error.ExceptionUtil;

public class BatchUpdateException extends java.sql.BatchUpdateException {

    /** 
     *  The message utility instance we use to find messages
     *  It's primed with the name of the client message bundle so that
     *  it knows to look there if the message isn't found in the
     *  shared message bundle.
     */
    private static final MessageUtil msgutil_ =
        SqlException.getMessageUtil();

    public BatchUpdateException(LogWriter logWriter, ClientMessageId msgid,
        Object[] args, int[] updateCounts, SqlException cause)
    {
        super(
            msgutil_.getCompleteMessage(
                msgid.msgid,
                args),
            ExceptionUtil.getSQLStateFromIdentifier(msgid.msgid),
            ExceptionUtil.getSeverityFromIdentifier(msgid.msgid),
            updateCounts);

        if (logWriter != null) {
            logWriter.traceDiagnosable(this);
        }

        if (cause != null) {
            initCause(cause);
            setNextException(cause.getSQLException());
        }
    }
    
    // Syntactic sugar constructors to make it easier to create
    // a BatchUpdateException with substitution parameters
    public BatchUpdateException(LogWriter logWriter, ClientMessageId msgid,
        Object[] args, int[] updateCounts) {
        this(logWriter, msgid, args, updateCounts, null);
    }

    public BatchUpdateException(LogWriter logWriter, ClientMessageId msgid,
        int[] updateCounts)
    {
        this(logWriter, msgid, (Object [])null, updateCounts);
    }
    
    public BatchUpdateException(LogWriter logWriter, ClientMessageId msgid,
        Object arg1, int[] updateCounts)
    {
        this(logWriter, msgid, new Object[] {arg1}, updateCounts);
    }
}

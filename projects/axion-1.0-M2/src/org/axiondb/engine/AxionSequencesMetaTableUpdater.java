/*
 * $Id: AxionSequencesMetaTableUpdater.java,v 1.4 2003/05/01 16:39:00 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The names "Tigris", "Axion", nor the names of its contributors may
 *    not be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. Products derived from this software may not be called "Axion", nor
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.ColumnIdentifier;
import org.axiondb.ComparisonOperator;
import org.axiondb.Database;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.TableIdentifier;
import org.axiondb.engine.commands.UpdateCommand;
import org.axiondb.event.BaseDatabaseModificationListener;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.DatabaseSequenceEvent;
import org.axiondb.event.SequenceModificationListener;

/**
 * Updates the <code>AXION_SEQUENCES</code> meta table
 * 
 * @version $Revision: 1.4 $ $Date: 2003/05/01 16:39:00 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionSequencesMetaTableUpdater extends BaseDatabaseModificationListener implements DatabaseModificationListener, SequenceModificationListener {
    private static Log _log = LogFactory.getLog(AxionSequencesMetaTableUpdater.class);
    private Database _db = null;

    public AxionSequencesMetaTableUpdater(Database db) {
        _db = db;
    }

    public void sequenceAdded(DatabaseSequenceEvent e) {
        SimpleRow row = new SimpleRow(2);
        row.set(0, e.getName());
        row.set(1, e.getValue());
        try {
            _db.getTable("AXION_SEQUENCES").addRow(row);
        } catch (AxionException ex) {
            _log.error("Unable to mention sequence in system tables", ex);
        }
    }

    public void sequenceIncremented(DatabaseSequenceEvent e) {
        ColumnIdentifier col = new ColumnIdentifier("SEQUENCE_NAME");
        Literal lit = new Literal(e.getName());
        ComparisonOperator op = ComparisonOperator.EQUAL;

        UpdateCommand cmd = new UpdateCommand();
        cmd.setTable(new TableIdentifier("AXION_SEQUENCES"));
        cmd.setWhere(new LeafWhereNode(col, op, lit));
        cmd.addColumn(new ColumnIdentifier("SEQUENCE_VALUE"));
        cmd.addValue(new Literal(e.getValue()));
        try {
            cmd.executeUpdate(_db);
        } catch (AxionException ex) {
            _log.error("Unable to update sequence value in system tables", ex);
        }
    }
}

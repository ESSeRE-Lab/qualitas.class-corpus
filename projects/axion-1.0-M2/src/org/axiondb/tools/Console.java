/*
 * $Id: Console.java,v 1.3 2003/03/27 19:14:07 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

package org.axiondb.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.axiondb.jdbc.AxionDriver;

/**
 * Simple console-based Axion client application.
 *
 * <p> Invoke via <code>java org.axiondb.tools.Console <i>dbname</i>
 * [<i>location</i>]</code>.  </p>
 *
 * @version $Revision: 1.3 $ $Date: 2003/03/27 19:14:07 $
 * @author Chuck Burdick
 */
public class Console {
    public Console(String dbName, PrintWriter writer) throws SQLException {
        this(dbName, null, writer);
    }

    public Console(String dbName, String dbLoc, PrintWriter writer) throws SQLException {
        if (dbName == null) {
            throw new NullPointerException("Must provide database name");
        }
        if (dbName == null) {
            throw new NullPointerException("Must provide PrintWriter for output");
        }
        _writer = writer;
        StringBuffer buf = new StringBuffer();
        buf.append(AxionDriver.URL_PREFIX);
        buf.append(dbName);
        if (dbLoc != null) {
            buf.append(":");
            buf.append(dbLoc);
        }
        try {
            _conn = DriverManager.getConnection(buf.toString());
            _stmt = _conn.createStatement();
        } catch (SQLException e) {
            cleanUp();
            throw e;
        }
    }

    public Connection getConnection() {
        return _conn;
    }

    public void execute(String input) {
        if(input != null) {
            input = input.trim();
            if(input.length() != 0) {
                if(input.toUpperCase().startsWith("DESCRIBE TABLE")) {
                    String tablename = input.substring("DESCRIBE TABLE".length()).trim();
                    if(tablename.endsWith(";")) {
                        tablename = tablename.substring(0,tablename.length()-1).trim();
                    }
                    describeTable(tablename);
                } else if(input.toUpperCase().startsWith("LIST")) {
                    String tabletype = input.substring("LIST".length()).trim();
                    if(tabletype.endsWith(";")) {
                        tabletype = tabletype.substring(0,tabletype.length()-1).trim();
                    }
                    if(tabletype.toUpperCase().endsWith("S")) {
                        tabletype = tabletype.substring(0,tabletype.length()-1).trim();
                    }
                    listTables(tabletype);
                } else if (input.startsWith("@")) {
                    String filename = input.substring(1);
                    File batch = null;
                    BufferedReader reader = null;
                    try {
                        batch = new File(filename);
                        reader = new BufferedReader(new FileReader(batch));
                        BatchSqlCommandRunner runner = new BatchSqlCommandRunner(getConnection());
                        runner.runCommands(reader);
                        _writer.println("Successfully loaded file " + batch);
                    } catch (IOException e) {
                        _writer.println("Error reading file " + filename);
                        _writer.println(e.getMessage());
                    } catch (SQLException e) {
                        _writer.println(e.getMessage());
                    } finally {
                        try { reader.close(); } catch (Exception e) {}
                        reader = null;
                        batch = null;
                    }
                } else {
                    executeSql(input);
                }
            }
        }
    }

    private void describeTable(String table) {
        /*
        // here's the generic but verbose form            
        try {
            DatabaseMetaData dbmd = _conn.getMetaData();
            _rset = dbmd.getColumns(null,null,table.toUpperCase(),"%");
        } catch (SQLException e) {
            _writer.println(e.getMessage());
        }
        */
        // here's the axion-centric but terse form            
        String query = "select COLUMN_NAME, TYPE_NAME, IS_NULLABLE from AXION_COLUMNS " +
                       "where TABLE_NAME = '" + table.toUpperCase() + "' order by ORDINAL_POSITION";
        executeSql(query);
    }

    private void listTables(String type) {
        // here's the axion-centric but terse form            
        String query = "select TABLE_NAME, TABLE_TYPE from AXION_TABLES " +
                       "where TABLE_TYPE = '" + type.toUpperCase() + "' " +
                       "order by TABLE_NAME";
        executeSql(query);
    }

    private void executeSql(String sql) {
        try {
            _stmt.execute(sql);
            _rset = _stmt.getResultSet();
            printResultSet(_rset);
        } catch (SQLException e) {
            _writer.println(e.getMessage());
        }
    }

    private void printResultSet(ResultSet rset) throws SQLException {
        if (rset != null) {
            ResultSetMetaData meta = rset.getMetaData();
            int count = meta.getColumnCount();
            int[] colWidths = new int[count];
            List[] colValues = new List[count];
            for (int i = 0; i < count; i++) {
                String label = meta.getColumnLabel(i+1);
                if (label != null) {
                    colWidths[i] = label.length();
                }
                colValues[i] = new ArrayList();
            }
            // find column values and widths
            while (rset.next()) {
                for (int i = 0; i < count; i++) {
                    String val = rset.getString(i+1);
                    if(rset.wasNull()) {
                        val = "NULL";
                    }
                    if(val.length() > colWidths[i]) {
                        colWidths[i] = val.length();
                    }
                    colValues[i].add(val);
                }
            }
            // table header
            printBoundary('=', colWidths);
            for (int i = 0; i < count; i++) {
                String label = meta.getColumnLabel(i+1);
                _writer.print("|");
                printCentered(label, colWidths[i]);
            }
            _writer.println("|");
            printBoundary('=', colWidths);
            // values
            for (int i = 0; i < colValues[0].size(); i++) {
                // for each row
                for (int j = 0; j < count; j++) {
                    // for each column
                    String value = (String)colValues[j].get(i);
                    _writer.print("|");
                    printRight(value, colWidths[j]);
                }
                _writer.println("|");
                printBoundary('-', colWidths);
            }
        }
    }

    public void cleanUp() {
        try { _rset.close(); } catch (Exception e) {}
        try { _stmt.close(); } catch (Exception e) {}
        try { _conn.close(); } catch (Exception e) {}
    }

    public void printBoundary(char boundaryChar, int[] colWidths) {
        for (int i = 0; i < colWidths.length; i++) {
            _writer.print("+");
            for (int j = 0; j < colWidths[i] + 2; j++) {
                _writer.print(boundaryChar);
            }
        }
        _writer.println("+");
    }

    public void printCentered(String value, int length) {
        _writer.print(" ");
        int diff = length - value.length();
        int left = diff/2;
        int right = left;
        if (diff % 2 == 1) {
            left++;
        }
        for (int j = 0; j < left; j++) {
            _writer.print(" ");
        }
        _writer.print(value);
        for (int j = 0; j < right; j++) {
            _writer.print(" ");
        }
        _writer.print(" ");
    }

    public void printRight(String value, int length) {
        _writer.print(" ");
        int diff = length - value.length();
        for (int j = 0; j < diff; j++) {
            _writer.print(" ");
        }
        _writer.print(value);
        _writer.print(" ");
    }

    public static void main(String[] args) {
        String input = null;
        boolean quit = false;

        if (args.length < 1) {
            System.out.println("Usage: java org.axiondb.tools.Console dbName [location]");
        } else {
            Console axion = null;
            try {
                PrintWriter out = new PrintWriter(System.out, true);
                if (args.length > 1) {
                    axion = new Console(args[0], args[1], out);
                } else {
                    axion = new Console(args[0], out);
                }
                System.out.println();
                System.out.println("Type 'quit' to quit the program.");
                while (!quit) {
                    System.out.print(_PROMPT);
                    input = _in.readLine();
                    quit = ("quit".equals(input) || "exit".equals(input));
                    if (!quit && input != null && !"".equals(input.trim())) {
                        axion.execute(input);
                    }
                }
            } catch (SQLException sqle) {
                System.out.println("Unable to connect to database");
                sqle.printStackTrace();
            } catch (IOException ioe) {
                System.out.println("Error while reading input");
                ioe.printStackTrace();
            } finally {
                try { axion.cleanUp(); } catch (Exception e) {}
            }
        }
    }

    private Connection _conn = null;
    private Statement _stmt = null;
    private ResultSet _rset = null;
    private PrintWriter _writer = null;

    private static final String _PROMPT = "axion> ";
    private static BufferedReader _in = new BufferedReader(new InputStreamReader(System.in));
    static {
        try { 
            Class.forName("org.axiondb.jdbc.AxionDriver"); 
        } catch (ClassNotFoundException e) {
        }
    }
}

/*
 * $Id: Base64EncodeFunction.java,v 1.1 2003/05/14 22:28:40 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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

package org.axiondb.functions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.apache.commons.codec.base64.Base64;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.Selectable;
import org.axiondb.types.StringType;

/**
 * <tt>BASE64ENCODE(byte[])</tt>: returns a string
 * representing the Base64 encoded value of the given <i>byte[]</i>
 * or Blob.
 *
 * @version $Revision: 1.1 $ $Date: 2003/05/14 22:28:40 $
 * @author Rodney Waldhoff
 */
public class Base64EncodeFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    public Base64EncodeFunction() {
        super("BASE64ENCODE");
    }

    public ConcreteFunction makeNewInstance() {
        return new Base64EncodeFunction();
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        Selectable sel = getArgument(0);
        Object val = sel.evaluate(row);
        if(val instanceof byte[]) {
            return new String(Base64.encode((byte[])val));            
        } else if(null == val) {
            return null;
        } else {
            try {
                val = sel.getDataType().toBlob(val);
                return new String(Base64.encode(readBlob((Blob)val)));            
            } catch(SQLException e) {
                throw new AxionException(e);
            } catch(IOException e) {
                throw new AxionException(e);
            }
        }
    }

    public boolean isValid() {
        return getArgumentCount() == 1;
    }

    private byte[] readBlob(Blob blob) throws IOException, SQLException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            for(int b = in.read(); b != -1; b = in.read()) {
                buf.write((byte)b);
            }
        } finally {
            try { in.close(); } catch(Exception e) { }
        }
        return buf.toByteArray();
    }
    
    private static final DataType RETURN_TYPE = new StringType();
}

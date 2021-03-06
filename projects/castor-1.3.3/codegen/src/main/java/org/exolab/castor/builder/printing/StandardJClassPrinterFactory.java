/*
 * Copyright 2005-2008 Werner Guttmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exolab.castor.builder.printing;

/**
 * {@link JClassPrinterFactory} instance that returns standard 'Writer'-based 
 * {@link JClassPrinter} instances. This is currently the default {@link JClassPrinterFactory} 
 * instance used for code generation, but will be replaced in the foreseeable future. 
 *  
 * @since 1.2.1
 */
public class StandardJClassPrinterFactory implements JClassPrinterFactory {

    /** 
     * The name of the factory.
     */
    private static final String NAME = "standard";

    /**
     * {@inheritDoc}
     * 
     * @see org.exolab.castor.builder.printing.JClassPrinterFactory#getJClassPrinter()
     */
    public JClassPrinter getJClassPrinter() {
        return new WriterJClassPrinter();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.exolab.castor.builder.printing.JClassPrinterFactory#getName()
     */
    public String getName() {
        return NAME;
    }

}

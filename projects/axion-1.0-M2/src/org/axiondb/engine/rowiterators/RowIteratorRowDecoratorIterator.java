/*
 * $Id: RowIteratorRowDecoratorIterator.java,v 1.6 2002/12/16 23:34:54 rwald Exp $
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

package org.axiondb.engine.rowiterators;

import java.util.NoSuchElementException;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowDecoratorIterator;
import org.axiondb.RowIterator;

/**
 * A {@link RowDecoratorIterator} based upon some 
 * {@link RowDecorator decorator} and {@link RowIterator}.
 * 
 * @version $Revision: 1.6 $ $Date: 2002/12/16 23:34:54 $
 * @author Rodney Waldhoff
 */
public class RowIteratorRowDecoratorIterator implements RowDecoratorIterator {

    public RowIteratorRowDecoratorIterator() {
    }

    public RowIteratorRowDecoratorIterator(RowIterator iterator, RowDecorator decorator) {
        setIterator(iterator);
        setDecorator(decorator);
    }

    public RowDecorator getDecorator() {
        return _decorator;
    }

    public void setDecorator(RowDecorator decorator) {
        _decorator = decorator;
    }

    public RowIterator getIterator() {
        return _iterator;
    }

    public void setIterator(RowIterator iterator) {
        _iterator = iterator;
    }

    public RowDecorator current() throws NoSuchElementException {
        return decorate(_iterator.current());
    }

    public RowDecorator next() throws NoSuchElementException, AxionException  {
        return decorate(_iterator.next());
    }

    public RowDecorator previous() throws NoSuchElementException, AxionException  {
        return decorate(_iterator.previous());
    }

    public boolean hasNext() {
        return _iterator.hasNext();
    }

    public boolean hasPrevious() {
        return _iterator.hasPrevious();
    }

    public boolean hasCurrent() {
        return _iterator.hasCurrent();
    }

    public RowDecorator first() throws NoSuchElementException, AxionException  {
        return decorate(_iterator.first());
    }

    public RowDecorator last() throws NoSuchElementException, AxionException  {
        return decorate(_iterator.last());
    }

    public int nextIndex() {
        return _iterator.nextIndex();
    }

    public int previousIndex() {
        return _iterator.previousIndex();
    }

    public void remove() throws AxionException {
        _iterator.remove();
    }

    public void set(Row row) throws AxionException {
        _iterator.set(row);
    }

    public void add(Row row) throws AxionException {
        _iterator.add(row);
    }

    public void reset() throws AxionException {
        _iterator.reset();
    }

    private RowDecorator decorate(Row row) {
        _decorator.setRow(_iterator.currentIndex(),row);
        return _decorator;
    }

    private RowDecorator _decorator = null;
    private RowIterator _iterator = null;
}


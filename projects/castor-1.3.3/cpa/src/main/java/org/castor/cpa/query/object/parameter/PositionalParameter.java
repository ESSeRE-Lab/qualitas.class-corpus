/*
 * Copyright 2008 Udai Gupta, Ralf Joachim
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
package org.castor.cpa.query.object.parameter;

/**
 * Final immutable class that represents a positional parameter.
 *
 * @author <a href="mailto:mailtoud AT gmail DOT com">Udai Gupta</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8994 $ $Date: 2011-08-02 01:40:59 +0200 (Di, 02 Aug 2011) $
 * @since 1.3
 */
public final class PositionalParameter extends AbstractParameter {
    //--------------------------------------------------------------------------

    /** Position of the positional parameter. */
    private final int _position;

    //--------------------------------------------------------------------------

    /**
     * Construct positional parameter with given position.
     * 
     * @param position Position of the positional parameter.
     */
    public PositionalParameter(final int position) {
        _position = position;
    }
    
    //--------------------------------------------------------------------------

    /**
     * Get the position of the positional parameter.
     *
     * @return Position of the positional parameter.
     */
    public int getPosition() {
        return _position;
    }

    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public StringBuilder toString(final StringBuilder sb) {
        return sb.append('?').append(_position);
    }

    //--------------------------------------------------------------------------
}

/*

   Derby - Class org.apache.derby.impl.sql.compile.OrderedColumn

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package	org.apache.derby.impl.sql.compile;

import org.apache.derby.iapi.services.sanity.SanityManager;

/**
 * An ordered column has position.   It is an
 * abstract class for group by and order by
 * columns.
 *
 */
public abstract class OrderedColumn extends QueryTreeNode 
{
	protected static final int UNMATCHEDPOSITION = -1;
	protected int	columnPosition = UNMATCHEDPOSITION;

	/**
	 * Indicate whether this column is ascending or not.
	 * By default assume that all ordered columns are
	 * necessarily ascending.  If this class is inherited
	 * by someone that can be desceneded, they are expected
	 * to override this method.
	 *
	 * @return true
	 */
	public boolean isAscending()
	{
		return true;
	}

	/**
	 * Indicate whether this column should be ordered NULLS low.
	 * By default we assume that all ordered columns are ordered
	 * with NULLS higher than non-null values. If this class is inherited
	 * by someone that can be specified to have NULLs ordered lower than
         * non-null values, they are expected to override this method.
	 *
	 * @return false
	 */
	public boolean isNullsOrderedLow()
	{
		return false;
	}

	/**
	 * Convert this object to a String.  See comments in QueryTreeNode.java
	 * for how this should be done for tree printing.
	 *
	 * @return	This object as a String
	 */
	public String toString() 
	{
		if (SanityManager.DEBUG)
		{
			return "columnPosition: " + columnPosition + "\n" +
				super.toString();
		}
		else
		{
			return "";
		}
	}

	/**
	 * Get the position of this column
	 *
	 * @return	The position of this column
	 */
	public int getColumnPosition() 
	{
		return columnPosition;
	}

	/**
	 * Set the position of this column
	 */
	public void setColumnPosition(int columnPosition) 
	{
		this.columnPosition = columnPosition;
		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(columnPosition > 0,
				"Column position is " + columnPosition +
				". This is a problem since the code to generate " +
				" ordering columns assumes it to be one based -- i.e. "+
				" it subtracts one");

		}
	}
}

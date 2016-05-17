package org.compiere.db;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;

import org.compiere.util.*;

public final class QueryUtil {

	private static final CLogger log = CLogger.getCLogger(QueryUtil.class);

	public static Object[][] executeQuery(String SQL, Object[] params) {
		try {
			return executeQuery(SQL, params, 0, 1000000);
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return null;
	}

	public static int executeQueryInt(String SQL, Object[] params) {

		Object[][] result = executeQuery(SQL, params);
		if (result == null || result.length == 0)
			return -1;
		return ((BigDecimal) result[0][0]).intValue();
	}

	/**
	 * 
	 * @param SQL
	 * @param params
	 * @param startRow
	 *            The row (zero-based) to start with
	 * @param rowCount
	 *            The number of rows to return
	 * @return
	 * @throws SQLException
	 */
	public static Object[][] executeQuery(String SQL, Object[] params, int startRow, int rowCount) throws SQLException {
		StringBuffer logBuffer = new StringBuffer();

		logBuffer.append("SQL: " + SQL + "\n");

		PreparedStatement pstmt = null;
		Object[][] result = new Object[0][];
		try {
			pstmt = DB.prepareStatement(SQL, (Trx) null);
			pstmt.setMaxRows(startRow + rowCount);

			if (params != null) {
				int i = 1;
				for (Object obj : params) {
					logBuffer.append("  params[" + i + "]: " + params[i - 1]);
					if (params[i - 1] != null)
						logBuffer.append(" (" + params[i - 1].getClass().getSimpleName() + ")");
					logBuffer.append("\n ");

					if (obj instanceof Number) {
						BigDecimal n = new BigDecimal(((Number) obj).toString());
						try {
							pstmt.setInt(i, n.intValueExact());
						} catch (ArithmeticException e) {
							pstmt.setBigDecimal(i, n);
						}
					} else if (obj instanceof Date)
						pstmt.setTimestamp(i, new Timestamp(((Date) obj).getTime()));
					else if (obj instanceof Boolean)
						pstmt.setString(i, ((Boolean) obj).booleanValue() ? "Y" : "N");
					else if (obj instanceof String)
						pstmt.setString(i, (String) obj);
					else
						pstmt.setObject(i, obj);
					++i;
				}

			}
			log.log(Level.FINE, logBuffer.toString());
			result = executeQuery(pstmt, startRow, rowCount);
		} catch (SQLException e) {
			log.log(Level.SEVERE, logBuffer.toString());
			throw e;
		} finally {
			if (pstmt != null)
				pstmt.close();
		}
		return result;

	}

	private static Object[][] executeQuery(PreparedStatement pstmt, int startRow, int rowCount) throws SQLException {
		ArrayList<Object[]> result = new ArrayList<Object[]>();
		ResultSet rs = null;
		try {
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int rowNum = 0;
			while (rowNum < startRow && rs.next()) {
				++rowNum;
			}
			while (rowNum < startRow + rowCount && rs.next()) {
				ArrayList<Object> row = new ArrayList<Object>();
				for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
					Object obj = rs.getObject(i);
					if (obj instanceof Number)
						row.add(rs.getBigDecimal(i));
					else if (obj instanceof Date)
						row.add(rs.getTimestamp(i));
					else
						row.add(rs.getString(i));
				}
				result.add(row.toArray());
				++rowNum;
			}
		} finally {
			if (rs != null)
				rs.close();
		}

		return result.toArray(new Object[0][]);
	}

}

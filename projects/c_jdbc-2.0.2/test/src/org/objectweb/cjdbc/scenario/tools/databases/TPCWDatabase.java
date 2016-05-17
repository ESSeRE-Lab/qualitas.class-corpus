/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet
 * Contributor(s): Mathieu Peltier
 */

package org.objectweb.cjdbc.scenario.tools.databases;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;

/**
 * Represents the TPCW benchmark database.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 */
public class TPCWDatabase extends AbstractDatabase
{
  /**
   * Creates a new <code>TPCWDatabase</code> instance.
   */
  public TPCWDatabase()
  {
    super();
    DatabaseTable t;

    t = new DatabaseTable("country", 4);
    t.addColumn(new DatabaseColumn("co_id", true));
    t.addColumn(new DatabaseColumn("co_name", false));
    t.addColumn(new DatabaseColumn("co_exchange", false));
    t.addColumn(new DatabaseColumn("co_currency", false));
    schema.addTable(t);

    t = new DatabaseTable("author", 2);
    t.addColumn(new DatabaseColumn("a_id", true));
    t.addColumn(new DatabaseColumn("a_fname", false));
    t.addColumn(new DatabaseColumn("a_lname", false));
    t.addColumn(new DatabaseColumn("a_mname", false));
    t.addColumn(new DatabaseColumn("a_dob", false));
    t.addColumn(new DatabaseColumn("a_bio", false));
    schema.addTable(t);

    t = new DatabaseTable("customer", 17);
    t.addColumn(new DatabaseColumn("c_id", true));
    t.addColumn(new DatabaseColumn("c_uname", false));
    t.addColumn(new DatabaseColumn("c_passwd", false));
    t.addColumn(new DatabaseColumn("c_fname", false));
    t.addColumn(new DatabaseColumn("c_lname", false));
    t.addColumn(new DatabaseColumn("c_addr_id", false));
    t.addColumn(new DatabaseColumn("c_phone", false));
    t.addColumn(new DatabaseColumn("c_email", false));
    t.addColumn(new DatabaseColumn("c_since", false));
    t.addColumn(new DatabaseColumn("c_last_login", false));
    t.addColumn(new DatabaseColumn("c_login", false));
    t.addColumn(new DatabaseColumn("c_expiration", false));
    t.addColumn(new DatabaseColumn("c_discount", false));
    t.addColumn(new DatabaseColumn("c_balance", false));
    t.addColumn(new DatabaseColumn("c_ytd_pmt", false));
    t.addColumn(new DatabaseColumn("c_birthdate", false));
    t.addColumn(new DatabaseColumn("c_data", false));
    schema.addTable(t);

    t = new DatabaseTable("items", 22);
    t.addColumn(new DatabaseColumn("i_id", true));
    t.addColumn(new DatabaseColumn("i_title", false));
    t.addColumn(new DatabaseColumn("i_a_id", false));
    t.addColumn(new DatabaseColumn("i_pub_date", false));
    t.addColumn(new DatabaseColumn("i_publisher", false));
    t.addColumn(new DatabaseColumn("i_subject", false));
    t.addColumn(new DatabaseColumn("i_desc_blob", false));
    t.addColumn(new DatabaseColumn("i_related1", false));
    t.addColumn(new DatabaseColumn("i_related2", false));
    t.addColumn(new DatabaseColumn("i_related3", false));
    t.addColumn(new DatabaseColumn("i_related4", false));
    t.addColumn(new DatabaseColumn("i_related5", false));
    t.addColumn(new DatabaseColumn("i_thumbnail", false));
    t.addColumn(new DatabaseColumn("i_image", false));
    t.addColumn(new DatabaseColumn("i_srp", false));
    t.addColumn(new DatabaseColumn("i_cost", false));
    t.addColumn(new DatabaseColumn("i_avail", false));
    t.addColumn(new DatabaseColumn("i_stock", false));
    t.addColumn(new DatabaseColumn("i_isbn", false));
    t.addColumn(new DatabaseColumn("i_page", false));
    t.addColumn(new DatabaseColumn("i_backing", false));
    t.addColumn(new DatabaseColumn("i_dimensions", false));
    schema.addTable(t);

    t = new DatabaseTable("orders", 11);
    t.addColumn(new DatabaseColumn("o_id", true));
    t.addColumn(new DatabaseColumn("o_c_id", false));
    t.addColumn(new DatabaseColumn("o_date", false));
    t.addColumn(new DatabaseColumn("o_sub_total", false));
    t.addColumn(new DatabaseColumn("o_tax", false));
    t.addColumn(new DatabaseColumn("o_total", false));
    t.addColumn(new DatabaseColumn("o_ship_type", false));
    t.addColumn(new DatabaseColumn("o_ship_date", false));
    t.addColumn(new DatabaseColumn("o_bill_addr_id", false));
    t.addColumn(new DatabaseColumn("o_ship_addr_id", false));
    t.addColumn(new DatabaseColumn("o_status", false));
    schema.addTable(t);

    t = new DatabaseTable("order_line", 6);
    t.addColumn(new DatabaseColumn("ol_id", true));
    t.addColumn(new DatabaseColumn("ol_o_id", true));
    t.addColumn(new DatabaseColumn("ol_i_id", false));
    t.addColumn(new DatabaseColumn("ol_qty", false));
    t.addColumn(new DatabaseColumn("ol_discount", false));
    t.addColumn(new DatabaseColumn("ol_comments", false));
    schema.addTable(t);

    t = new DatabaseTable("cc_xacts", 9);
    t.addColumn(new DatabaseColumn("cx_o_id", true));
    t.addColumn(new DatabaseColumn("cx_type", false));
    t.addColumn(new DatabaseColumn("cx_num", false));
    t.addColumn(new DatabaseColumn("cx_name", false));
    t.addColumn(new DatabaseColumn("cx_expiry", false));
    t.addColumn(new DatabaseColumn("cx_auth_id", false));
    t.addColumn(new DatabaseColumn("cx_xact_amt", false));
    t.addColumn(new DatabaseColumn("cx_xact_date", false));
    t.addColumn(new DatabaseColumn("cx_co_id", false));
    schema.addTable(t);

    t = new DatabaseTable("address", 7);
    t.addColumn(new DatabaseColumn("addr_id", true));
    t.addColumn(new DatabaseColumn("addr_street1", false));
    t.addColumn(new DatabaseColumn("addr_street2", false));
    t.addColumn(new DatabaseColumn("addr_city", false));
    t.addColumn(new DatabaseColumn("addr_state", false));
    t.addColumn(new DatabaseColumn("addr_zip", false));
    t.addColumn(new DatabaseColumn("addr_co_id", false));
    schema.addTable(t);

    t = new DatabaseTable("shopping_cart", 2);
    t.addColumn(new DatabaseColumn("sc_id", true));
    t.addColumn(new DatabaseColumn("sc_time", false));
    schema.addTable(t);

    t = new DatabaseTable("shopping_cart_line", 3);
    t.addColumn(new DatabaseColumn("scl_sc_id", true));
    t.addColumn(new DatabaseColumn("scl_i_id", true));
    t.addColumn(new DatabaseColumn("scl_qty", false));
    schema.addTable(t);
  }
}

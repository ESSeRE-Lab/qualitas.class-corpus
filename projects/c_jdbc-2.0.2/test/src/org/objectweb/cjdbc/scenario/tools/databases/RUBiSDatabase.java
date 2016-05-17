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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.scenario.tools.databases;

import java.sql.Types;

import org.objectweb.cjdbc.common.sql.schema.DatabaseColumn;
import org.objectweb.cjdbc.common.sql.schema.DatabaseTable;

/**
 * Represents the RUBiS benchmark database (see http://rubis.objectweb.org/).
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 */
public class RUBiSDatabase extends AbstractDatabase
{
  /**
   * Creates a new <code>RUBiSDatabase</code> instance.
   */
  public RUBiSDatabase()
  {
    super();
    DatabaseTable t;
    
    t = new DatabaseTable("selections",2);
    schema.addTable(t);

    //CREATE TABLE categories (
    //   id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //   name VARCHAR(50),
    //   PRIMARY KEY(id)
    //);

    t = new DatabaseTable("categories", 2);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));
    schema.addTable(t);

    //    CREATE TABLE regions(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      name VARCHAR(25),
    //      PRIMARY KEY(id));

    t = new DatabaseTable("regions", 2);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));
    schema.addTable(t);

    //    CREATE TABLE users(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      firstname VARCHAR(20),
    //      lastname VARCHAR(20),
    //      nickname VARCHAR(20) NOT NULL UNIQUE,
    //      password VARCHAR(20) NOT NULL,
    //      email VARCHAR(50) NOT NULL,
    //      rating INTEGER,
    //      balance FLOAT,
    //      creation_date DATETIME,
    //      region INTEGER UNSIGNED NOT NULL,
    //      PRIMARY KEY(id),
    //      INDEX auth(nickname, password),
    //      INDEX region_id(region));

    t = new DatabaseTable("users", 10);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("firstname", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("lastname", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("nickname", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("password", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("email", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("rating", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("balance", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("creation_date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("region", false, Types.INTEGER));
    schema.addTable(t);

    //    CREATE TABLE items(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      name VARCHAR(100),
    //      description TEXT,
    //      initial_price FLOAT UNSIGNED NOT NULL,
    //      quantity INTEGER UNSIGNED NOT NULL,
    //      reserve_price FLOAT UNSIGNED DEFAULT 0,
    //      buy_now FLOAT UNSIGNED DEFAULT 0,
    //      nb_of_bids INTEGER UNSIGNED DEFAULT 0,
    //      max_bid FLOAT UNSIGNED DEFAULT 0,
    //      start_date DATETIME,
    //      end_date DATETIME,
    //      seller INTEGER UNSIGNED NOT NULL,
    //      category INTEGER UNSIGNED NOT NULL,
    //      PRIMARY KEY(id),
    //      INDEX seller_id(seller),
    //      INDEX category_id(category));

    t = new DatabaseTable("items", 12);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("description", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("initial_price", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("quantity", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("reserve_price", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("buy_now", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("nb_of_bids", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("max_bid", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("start_date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("end_date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("seller", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("category", false, Types.INTEGER));
    schema.addTable(t);

    //    CREATE TABLE old_items(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE,
    //      name VARCHAR(100),
    //      description TEXT,
    //      initial_price FLOAT UNSIGNED NOT NULL,
    //      quantity INTEGER UNSIGNED NOT NULL,
    //      reserve_price FLOAT UNSIGNED DEFAULT 0,
    //      buy_now FLOAT UNSIGNED DEFAULT 0,
    //      nb_of_bids INTEGER UNSIGNED DEFAULT 0,
    //      max_bid FLOAT UNSIGNED DEFAULT 0,
    //      start_date DATETIME,
    //      end_date DATETIME,
    //      seller INTEGER UNSIGNED NOT NULL,
    //      category INTEGER UNSIGNED NOT NULL,
    //      PRIMARY KEY(id),
    //      INDEX seller_id(seller),
    //      INDEX category_id(category));

    t = new DatabaseTable("old_items", 12);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("name", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("description", false, Types.VARCHAR));
    t.addColumn(new DatabaseColumn("initial_price", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("quantity", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("reserve_price", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("buy_now", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("nb_of_bids", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("max_bid", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("start_date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("end_date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("seller", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("category", false, Types.INTEGER));
    schema.addTable(t);

    //    CREATE TABLE bids(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      user_id INTEGER UNSIGNED NOT NULL,
    //      item_id INTEGER UNSIGNED NOT NULL,
    //      qty INTEGER UNSIGNED NOT NULL,
    //      bid FLOAT UNSIGNED NOT NULL,
    //      max_bid FLOAT UNSIGNED NOT NULL,
    //      date DATETIME,
    //      PRIMARY KEY(id),
    //      INDEX item(item_id),
    //      INDEX user(user_id));

    t = new DatabaseTable("bids", 7);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("user_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("item_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("qty", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("bid", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("max_bid", false, Types.FLOAT));
    t.addColumn(new DatabaseColumn("date", false, Types.TIMESTAMP));
    schema.addTable(t);

    //    CREATE TABLE comments(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      from_user_id INTEGER UNSIGNED NOT NULL,
    //      to_user_id INTEGER UNSIGNED NOT NULL,
    //      item_id INTEGER UNSIGNED NOT NULL,
    //      rating INTEGER,
    //      date DATETIME,
    //      comment TEXT,
    //      PRIMARY KEY(id),
    //      INDEX from_user(from_user_id),
    //      INDEX to_user(to_user_id),
    //      INDEX item(item_id));

    t = new DatabaseTable("comments", 7);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("from_user_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("to_user_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("item_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("rating", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("date", false, Types.TIMESTAMP));
    t.addColumn(new DatabaseColumn("comment", false, Types.VARCHAR));
    schema.addTable(t);

    //    CREATE TABLE buy_now(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT,
    //      buyer_id INTEGER UNSIGNED NOT NULL,
    //      item_id INTEGER UNSIGNED NOT NULL,
    //      qty INTEGER UNSIGNED NOT NULL,
    //      date DATETIME,
    //      PRIMARY KEY(id),
    //      INDEX buyer(buyer_id),
    //      INDEX item(item_id));

    t = new DatabaseTable("buy_now", 5);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("buyer_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("item_id", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("qty", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("date", false, Types.TIMESTAMP));
    schema.addTable(t);

    //    CREATE TABLE ids(
    //      id INTEGER UNSIGNED NOT NULL UNIQUE,
    //      category INTEGER UNSIGNED NOT NULL,
    //      region INTEGER UNSIGNED NOT NULL,
    //      users INTEGER UNSIGNED NOT NULL,
    //      item INTEGER UNSIGNED NOT NULL,
    //      comment INTEGER UNSIGNED NOT NULL,
    //      bid INTEGER UNSIGNED NOT NULL,
    //      buyNow INTEGER UNSIGNED NOT NULL,
    //      PRIMARY KEY(id));

    t = new DatabaseTable("ids", 8);
    t.addColumn(new DatabaseColumn("id", true, Types.INTEGER));
    t.addColumn(new DatabaseColumn("category", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("region", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("users", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("item", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("comment", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("bid", false, Types.INTEGER));
    t.addColumn(new DatabaseColumn("buyNow", false, Types.INTEGER));
    schema.addTable(t);
  }
}

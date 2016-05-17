--
--   Licensed to the Apache Software Foundation (ASF) under one or more
--   contributor license agreements.  See the NOTICE file distributed with
--   this work for additional information regarding copyright ownership.
--   The ASF licenses this file to You under the Apache License, Version 2.0
--   (the "License"); you may not use this file except in compliance with
--   the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--   See the License for the specific language governing permissions and
--   limitations under the License.
--
-- Test various functions

create table alltypes(
  id int not null primary key,
  smallIntCol smallint,
  intCol int,
  bigIntCol bigint,
  floatCol float,
  float1Col float(1),
  float26Col float(26),
  realCol real,
  doubleCol double,
  decimalCol decimal,
  decimal10Col decimal(10),
  decimal11Col decimal(11),
  numeric10d2Col numeric(10,2),
  charCol char,
  char32Col char(32),
  charForBitCol char(16) for bit data,
  varcharCol varchar(64),
  varcharForBitCol varchar(64) for bit data,
  longVarcharCol long varchar,
  blobCol blob(10k),
  clobCol clob(10k),
  dateCol date,
  timeCol time,
  timestampCol timestamp);
insert into allTypes(id) values(1),(2);
update allTypes set smallIntCol = 2 where id = 1;
update allTypes set intCol = 2 where id = 1;
update allTypes set bigIntCol = 3 where id = 1;
update allTypes set floatCol = 4.1 where id = 1;
update allTypes set float1Col = 5 where id = 1;
update allTypes set float26Col = 6.1234567890123456 where id = 1;
update allTypes set realCol = 7.2 where id = 1;
update allTypes set doubleCol = 8.2 where id = 1;
update allTypes set decimalCol = 9 where id = 1;
update allTypes set decimal10Col = 1234 where id = 1;
update allTypes set decimal11Col = 1234 where id = 1;
update allTypes set numeric10d2Col = 11.12 where id = 1;
update allTypes set charCol = 'a' where id = 1;
update allTypes set char32Col = 'abc' where id = 1;
update allTypes set charForBitCol = X'ABCD' where id = 1;
update allTypes set varcharCol = 'abcde' where id = 1;
update allTypes set varcharForBitCol = X'ABCDEF' where id = 1;
update allTypes set longVarcharCol = 'abcdefg' where id = 1;
update allTypes set blobCol = cast( X'0031' as blob(10k)) where id = 1;
update allTypes set clobCol = 'clob data' where id = 1;
update allTypes set dateCol = date( '2004-3-13') where id = 1;
update allTypes set timeCol = time( '16:07:21') where id = 1;
update allTypes set timestampCol = timestamp( '2004-3-14 17:08:22.123456') where id = 1;

select id, length(smallIntCol) from allTypes order by id;
select id, length(intCol) from allTypes order by id;
select id, length(bigIntCol) from allTypes order by id;
select id, length(floatCol) from allTypes order by id;
select id, length(float1Col) from allTypes order by id;
select id, length(float26Col) from allTypes order by id;
select id, length(realCol) from allTypes order by id;
select id, length(doubleCol) from allTypes order by id;
select id, length(decimalCol) from allTypes order by id;
select id, length(decimal10Col) from allTypes order by id;
select id, length(decimal11Col) from allTypes order by id;
select id, length(numeric10d2Col) from allTypes order by id;
select id, length(charCol) from allTypes order by id;
select id, length(char32Col) from allTypes order by id;
select id, length(charForBitCol) from allTypes order by id;
select id, length(varcharCol) from allTypes order by id;
select id, length(varcharForBitCol) from allTypes order by id;
select id, length(longVarcharCol) from allTypes order by id;
select id, length(blobCol) from allTypes order by id;
select id, length(clobCol) from allTypes order by id;
select id, length(dateCol) from allTypes order by id;
select id, length(timeCol) from allTypes order by id;
select id, length(timestampCol) from allTypes order by id;

-- try length of constants
values( length( 1), length( 720176), length( 12345678901));
values( length( 2.2E-1));
values( length( 1.), length( 12.3), length( 123.4), length( 123.45));
values( length( '1'), length( '12'));
values( length( X'00'), length( X'FF'), length( X'FFFF'));
values( length( date('0001-1-1')), length( time('0:00:00')), length( timestamp( '0001-1-1 0:00:00')));

-- try a length in the where clause
select id from allTypes where length(smallIntCol) > 5 order by id;
select id from allTypes where length(intCol) > 5 order by id;
select id from allTypes where length(bigIntCol) > 5 order by id;
select id from allTypes where length(floatCol) > 5 order by id;
select id from allTypes where length(float1Col) > 5 order by id;
select id from allTypes where length(float26Col) > 5 order by id;
select id from allTypes where length(realCol) > 5 order by id;
select id from allTypes where length(doubleCol) > 5 order by id;
select id from allTypes where length(decimalCol) > 5 order by id;
select id from allTypes where length(decimal10Col) > 5 order by id;
select id from allTypes where length(decimal11Col) > 5 order by id;
select id from allTypes where length(numeric10d2Col) > 5 order by id;
select id from allTypes where length(charCol) > 5 order by id;
select id from allTypes where length(char32Col) > 5 order by id;
select id from allTypes where length(charForBitCol) > 5 order by id;
select id from allTypes where length(varcharCol) > 5 order by id;
select id from allTypes where length(varcharForBitCol) > 5 order by id;
select id from allTypes where length(longVarcharCol) > 5 order by id;
select id from allTypes where length(blobCol) > 5 order by id;
select id from allTypes where length(clobCol) > 5 order by id;
select id from allTypes where length(dateCol) > 5 order by id;
select id from allTypes where length(timeCol) > 5 order by id;
select id from allTypes where length(timestampCol) > 5 order by id;

-- try an expression
select id, length( charCol || 'abc') from allTypes order by id;


-- bug 5761 & 5627
-- JDBC escape length function has the following behavior
-- LENGTH (RTRIM (xxxx))
values {FN LENGTH('xxxx                    ')};
values {FN LENGTH(' xxxx                    ')};
values {FN LENGTH('  xxxx                    ')};
values {FN LENGTH('   xxxx                    ')};

CREATE FUNCTION COUNT_ROWS(P1 VARCHAR(128), P2 VARCHAR(128)) RETURNS INT
READS SQL DATA
EXTERNAL NAME 'org.apache.derbyTesting.functionTests.util.ProcedureTest.countRows'
LANGUAGE JAVA PARAMETER STYLE JAVA;

CREATE FUNCTION FN_ABS(P1 INT) RETURNS INT
NO SQL
RETURNS NULL ON NULL INPUT
EXTERNAL NAME 'java.lang.Math.abs'
LANGUAGE JAVA PARAMETER STYLE JAVA;

select FN_ABS(i) FROM SV_TAB;
select COUNT_ROWS(CURRENT SCHEMA, 'SV_TAB') from SV_TAB;
select FN_ABS(i), COUNT_ROWS(CURRENT SCHEMA, 'SV_TAB') from SV_TAB;

DROP FUNCTION SV_RNNI;
DROP FUNCTION SV_CNI;
DROP FUNCTION SV_DEF;
DROP FUNCTION MAX_RNNI;
DROP FUNCTION MAX_CNI;
DROP FUNCTION MAX_DEF;

DROP FUNCTION FN_ABS;
DROP FUNCTION COUNT_ROWS;

DROP TABLE SV_TAB;

-- function definition without parameter names are valid
CREATE FUNCTION NONAME1(INT, INT) RETURNS INT EXTERNAL NAME 'java.lang.Math.max' LANGUAGE JAVA PARAMETER STYLE JAVA;
CREATE FUNCTION NONAME2(P1 INT, INT) RETURNS INT EXTERNAL NAME 'java.lang.Math.max' LANGUAGE JAVA PARAMETER STYLE JAVA;

VALUES NONAME1(99, -45);
VALUES NONAME2(99, -45);

DROP FUNCTION NONAME1;
DROP FUNCTION NONAME2;

-- check MODIFIES SQL DATA not allowed with FUNCTION
CREATE FUNCTION COUNT_ROWS(P1 VARCHAR(128), P2 VARCHAR(128)) RETURNS INT
MODIFIES SQL DATA
EXTERNAL NAME 'org.apache.derbyTesting.functionTests.util.ProcedureTest.countRows'
LANGUAGE JAVA PARAMETER STYLE JAVA;

CREATE FUNCTION SIGNATURE_BUG_DERBY_258_D(P_VAL INT, P_RADIX INT) RETURNS VARCHAR(20)
LANGUAGE JAVA PARAMETER STYLE JAVA NO SQL
EXTERNAL NAME 'java.lang.Integer.toString(int, int)';
CREATE FUNCTION SIGNATURE_BUG_DERBY_258_NS(P_VAL INT, P_RADIX INT) RETURNS VARCHAR(20)
LANGUAGE JAVA PARAMETER STYLE JAVA NO SQL
EXTERNAL NAME 'java.lang.Integer.toString';
CREATE FUNCTION SIGNATURE_BUG_DERBY_258_E() RETURNS VARCHAR(20)
LANGUAGE JAVA PARAMETER STYLE JAVA NO SQL
EXTERNAL NAME 'java.lang.Integer.toXXString()';

-- these are ok
VALUES SIGNATURE_BUG_DERBY_258_NS(2356, 16);
VALUES SIGNATURE_BUG_DERBY_258_NS(2356, 10);
VALUES SIGNATURE_BUG_DERBY_258_NS(2356, 2);

-- Must resolve as above
VALUES SIGNATURE_BUG_DERBY_258_D(2356, 16);
-- no method to resolve to (with specified signature)
VALUES SIGNATURE_BUG_DERBY_258_E();

DROP FUNCTION SIGNATURE_BUG_DERBY_258_D;
DROP FUNCTION SIGNATURE_BUG_DERBY_258_E;
DROP FUNCTION SIGNATURE_BUG_DERBY_258_NS;


-- SYSFUN functions (unqualifed functions are automatically resolved
-- to the in-memory SYSFUN functions if the function does not exist
-- in the current schema.

-- SYSFUN math functions
create table SYSFUN_MATH_TEST (d double);
insert into SYSFUN_MATH_TEST values null;
insert into SYSFUN_MATH_TEST values 0.67;
insert into SYSFUN_MATH_TEST values 1.34;

select cast (SYSFUN.ACOS(d) as DECIMAL(6,3)) AS SYSFUN_ACOS FROM SYSFUN_MATH_TEST;

CREATE VIEW VSMT AS SELECT SIN(d) sd, PI() pi FROM SYSFUN_MATH_TEST;
select cast (sd as DECIMAL(6,3)), cast (pi as DECIMAL(6,3)) from VSMT;
drop view VSMT;

drop table SYSFUN_MATH_TEST;

drop function SYSFUN.ACOS;

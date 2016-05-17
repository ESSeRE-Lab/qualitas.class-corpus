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
-- ** insert implicitConversionsPositive.sql
-- tests for implicit conversions between string and non-string types
-- and vice versa

-- create an all types table
create table all1(si smallint, i int, li bigint, r real, 
				  dp double precision, dc decimal(5,1), num numeric(5,1),
				  b char(2) for bit data, bv varchar(2) for bit data,
				  lbv long varchar FOR bit data,
				  dt date, tm time, tms timestamp,
				  c char(1), vc varchar(1), lvc long varchar);

-- populate table
insert into all1 values (2, 3, 4, 5.5, 6.6, 7.7, 8.8,
				  		 X'0020', X'0020', X'0020',
				  		 date('1996-09-09'), time('12:12:12'), 
				  		 timestamp('1996-09-09 12:12:12.5'),
				  		 '1', '2', '33333333');

-- unions between string and non-string types
values cast(1 as smallint), cast('2' as char(1));
values cast(1 as smallint), cast('2' as varchar(1));
values cast(1 as smallint), cast('2' as long varchar);
values cast('2' as char(1)), cast(1 as smallint);
values cast('2' as varchar(1)), cast(1 as smallint);
values cast('2' as long varchar), cast(1 as smallint);
values cast(1 as int), cast('2' as char(1));
values cast(1 as int), cast('2' as varchar(1));
values cast(1 as int), cast('2' as long varchar);
values cast('2' as char(1)), cast(1 as int);
values cast('2' as varchar(1)), cast(1 as int);
values cast('2' as long varchar), cast(1 as int);
values cast(1 as bigint), cast('2' as char(1));
values cast(1 as bigint), cast('2' as varchar(1));
values cast(1 as bigint), cast('2' as long varchar);
values cast('2' as char(1)), cast(1 as bigint);
values cast('2' as varchar(1)), cast(1 as bigint);
values cast('2' as long varchar), cast(1 as bigint);
values cast(1.1 as real), cast('2' as char(1));
values cast(1.1 as real), cast('2' as varchar(1));
values cast(1.1 as real), cast('2' as long varchar);
values cast('2' as char(1)), cast(1.1 as real);
values cast('2' as varchar(1)), cast(1.1 as real);
values cast('2' as long varchar), cast(1.1 as real);
values cast(1.1 as double precision), cast('2' as char(1));
values cast(1.1 as double precision), cast('2' as varchar(1));
values cast(1.1 as double precision), cast('2' as long varchar);
values cast('2' as char(1)), cast(1.1 as double precision);
values cast('2' as varchar(1)), cast(1.1 as double precision);
values cast('2' as long varchar), cast(1.1 as double precision);
values cast(1.1 as decimal(5,1)), cast('2' as char(1));
values cast(1.1 as decimal(5,1)), cast('2' as varchar(1));
values cast(1.1 as decimal(5,1)), cast('2' as long varchar);
values cast('2' as char(1)), cast(1.1 as decimal(5,1));
values cast('2' as varchar(1)), cast(1.1 as decimal(5,1));
values cast('2' as long varchar), cast(1.1 as decimal(5,1));
values cast(1.1 as decimal(5,1)), '0.002';
values cast(1.1 as decimal(5,1)), '0.002';
values '0.002', cast(1.1 as decimal(5,1));
values '0.002', cast(1.1 as decimal(5,1));
values 'abcde', 'fghij';
values 'abcde', cast('fghij' as varchar(5));
values 'abcde', cast('fghij' as long varchar);
values cast('abcde' as varchar(5)), 'fghij';
values cast('abcde' as long varchar), 'fghij';
-- DB2 UDB allows comparisons between hex constants and character constants
-- DB2 CS does not allow comparisons between hex constants and character constants
values X'01', '3';
values X'01', cast('3' as varchar(5));
values X'01', cast('3' as long varchar);
values '3', X'01';
values cast('3' as varchar(5)), X'01';
values cast('3' as long varchar), X'01';
values date('1996-09-09'), '1995-08-08';
values date('1996-09-09'), cast('1995-08-08' as varchar(10));
values date('1996-09-09'), cast('1995-08-08' as long varchar);
values '1995-08-08', date('1996-09-09');
values cast('1995-08-08' as varchar(10)), date('1996-09-09');
values cast('1995-08-08' as long varchar), date('1996-09-09');
values time('12:12:12'), '11:11:11';
values time('12:12:12'), cast('11:11:11' as varchar(8));
values time('12:12:12'), cast('11:11:11' as long varchar);
values '11:11:11', time('12:12:12');
values cast('11:11:11' as varchar(8)), time('12:12:12');
values cast('11:11:11' as long varchar), time('12:12:12');
values timestamp('1996-09-09 12:12:12.5'), '1996-08-08 11:11:11.1';
values timestamp('1996-09-09 12:12:12.5'), cast('1996-08-08 11:11:11.1' as varchar(30));
values timestamp('1996-09-09 12:12:12.5'), cast('1996-08-08 11:11:11.1' as long varchar);
values '1996-08-08 11:11:11.1', timestamp('1996-09-09 12:12:12.5');
values cast('1996-08-08 11:11:11.1' as varchar(30)), timestamp('1996-09-09 12:12:12.5');
values cast('1996-08-08 11:11:11.1' as long varchar), timestamp('1996-09-09 12:12:12.5');

-- comparisons at the language level
select si from all1 where cast(1 as smallint) = '1';
select si from all1 where cast(1 as smallint) > '2';
select si from all1 where cast(1 as smallint) >= '2';
select si from all1 where cast(1 as smallint) < '2';
select si from all1 where cast(1 as smallint) <= '2';
select si from all1 where cast(1 as smallint) <> '2';
select si from all1 where cast(1 as smallint) = cast(null as char);
select si from all1 where cast(1 as smallint) > cast(null as char);
select si from all1 where cast(1 as smallint) >= cast(null as char);
select si from all1 where cast(1 as smallint) < cast(null as char);
select si from all1 where cast(1 as smallint) <= cast(null as char);
select si from all1 where cast(1 as smallint) <> cast(null as char);
select si from all1 where '1' = cast(1 as smallint);
select si from all1 where '2' > cast(1 as smallint);
select si from all1 where '2' >= cast(1 as smallint);
select si from all1 where '2' < cast(1 as smallint);
select si from all1 where '2' <= cast(1 as smallint);
select si from all1 where '2' <> cast(1 as smallint);
select si from all1 where cast(null as char) = cast(1 as smallint);
select si from all1 where cast(null as char) > cast(1 as smallint);
select si from all1 where cast(null as char) >= cast(1 as smallint);
select si from all1 where cast(null as char) < cast(1 as smallint);
select si from all1 where cast(null as char) <= cast(1 as smallint);
select si from all1 where cast(null as char) <> cast(1 as smallint);
select si from all1 where cast(1 as int) = '1';
select si from all1 where cast(1 as int) > '2';
select si from all1 where cast(1 as int) >= '2';
select si from all1 where cast(1 as int) < '2';
select si from all1 where cast(1 as int) <= '2';
select si from all1 where cast(1 as int) <> '2';
select si from all1 where cast(1 as int) = cast(null as char);
select si from all1 where cast(1 as int) > cast(null as char);
select si from all1 where cast(1 as int) >= cast(null as char);
select si from all1 where cast(1 as int) < cast(null as char);
select si from all1 where cast(1 as int) <= cast(null as char);
select si from all1 where cast(1 as int) <> cast(null as char);
select si from all1 where '1' = cast(1 as int);
select si from all1 where '2' > cast(1 as int);
select si from all1 where '2' >= cast(1 as int);
select si from all1 where '2' < cast(1 as int);
select si from all1 where '2' <> cast(1 as int);
select si from all1 where '2' <= cast(1 as int);
select si from all1 where cast(null as char) = cast(1 as int);
select si from all1 where cast(null as char) > cast(1 as int);
select si from all1 where cast(null as char) >= cast(1 as int);
select si from all1 where cast(null as char) < cast(1 as int);
select si from all1 where cast(null as char) <> cast(1 as int);
select si from all1 where cast(null as char) <= cast(1 as int);
select si from all1 where cast(1 as bigint) = '1';
select si from all1 where cast(1 as bigint) > '2';
select si from all1 where cast(1 as bigint) >= '2';
select si from all1 where cast(1 as bigint) < '2';
select si from all1 where cast(1 as bigint) <= '2';
select si from all1 where cast(1 as bigint) <> '2';
select si from all1 where cast(1 as bigint) = cast(null as char);
select si from all1 where cast(1 as bigint) > cast(null as char);
select si from all1 where cast(1 as bigint) >= cast(null as char);
select si from all1 where cast(1 as bigint) < cast(null as char);
select si from all1 where cast(1 as bigint) <= cast(null as char);
select si from all1 where cast(1 as bigint) <> cast(null as char);
select si from all1 where '1' = cast(1 as bigint);
select si from all1 where '2' > cast(1 as bigint);
select si from all1 where '2' >= cast(1 as bigint);
select si from all1 where '2' < cast(1 as bigint);
select si from all1 where '2' <= cast(1 as bigint);
select si from all1 where '2' <> cast(1 as bigint);
select si from all1 where cast(null as char) = cast(1 as bigint);
select si from all1 where cast(null as char) > cast(1 as bigint);
select si from all1 where cast(null as char) >= cast(1 as bigint);
select si from all1 where cast(null as char) < cast(1 as bigint);
select si from all1 where cast(null as char) <= cast(1 as bigint);
select si from all1 where cast(null as char) <> cast(1 as bigint);
select si from all1 where cast(1 as real) = '1';
select si from all1 where cast(1 as real) > '2';
select si from all1 where cast(1 as real) >= '2';
select si from all1 where cast(1 as real) < '2';
select si from all1 where cast(1 as real) <> '2';
select si from all1 where cast(1 as real) <= '2';
select si from all1 where cast(1 as real) = cast(null as char);
select si from all1 where cast(1 as real) > cast(null as char);
select si from all1 where cast(1 as real) >= cast(null as char);
select si from all1 where cast(1 as real) < cast(null as char);
select si from all1 where cast(1 as real) <> cast(null as char);
select si from all1 where cast(1 as real) <= cast(null as char);
select si from all1 where '1' = cast(1 as real);
select si from all1 where '2' > cast(1 as real);
select si from all1 where '2' >= cast(1 as real);
select si from all1 where '2' < cast(1 as real);
select si from all1 where '2' <= cast(1 as real);
select si from all1 where '2' <> cast(1 as real);
select si from all1 where cast(null as char) = cast(1 as real);
select si from all1 where cast(null as char) > cast(1 as real);
select si from all1 where cast(null as char) >= cast(1 as real);
select si from all1 where cast(null as char) < cast(1 as real);
select si from all1 where cast(null as char) <= cast(1 as real);
select si from all1 where cast(null as char) <> cast(1 as real);
select si from all1 where cast(1 as double precision) = '1';
select si from all1 where cast(1 as double precision) > '2';
select si from all1 where cast(1 as double precision) >= '2';
select si from all1 where cast(1 as double precision) < '2';
select si from all1 where cast(1 as double precision) <= '2';
select si from all1 where cast(1 as double precision) <> '2';
select si from all1 where cast(1 as double precision) = cast(null as char);
select si from all1 where cast(1 as double precision) > cast(null as char);
select si from all1 where cast(1 as double precision) >= cast(null as char);
select si from all1 where cast(1 as double precision) < cast(null as char);
select si from all1 where cast(1 as double precision) <= cast(null as char);
select si from all1 where cast(1 as double precision) <> cast(null as char);
select si from all1 where '1' = cast(1 as double precision);
select si from all1 where '2' > cast(1 as double precision);
select si from all1 where '2' >= cast(1 as double precision);
select si from all1 where '2' < cast(1 as double precision);
select si from all1 where '2' <= cast(1 as double precision);
select si from all1 where '2' <> cast(1 as double precision);
select si from all1 where cast(null as char) = cast(1 as double precision);
select si from all1 where cast(null as char) > cast(1 as double precision);
select si from all1 where cast(null as char) >= cast(1 as double precision);
select si from all1 where cast(null as char) < cast(1 as double precision);
select si from all1 where cast(null as char) <= cast(1 as double precision);
select si from all1 where cast(null as char) <> cast(1 as double precision);
select si from all1 where cast(1 as numeric) = '1';
select si from all1 where cast(1 as numeric) > '2';
select si from all1 where cast(1 as numeric) >= '2';
select si from all1 where cast(1 as numeric) < '2';
select si from all1 where cast(1 as numeric) <= '2';
select si from all1 where cast(1 as numeric) <> '2';
select si from all1 where cast(1 as numeric) = cast(null as char);
select si from all1 where cast(1 as numeric) > cast(null as char);
select si from all1 where cast(1 as numeric) >= cast(null as char);
select si from all1 where cast(1 as numeric) < cast(null as char);
select si from all1 where cast(1 as numeric) <= cast(null as char);
select si from all1 where cast(1 as numeric) <> cast(null as char);
select si from all1 where '1' = cast(1 as numeric);
select si from all1 where '2' > cast(1 as numeric);
select si from all1 where '2' >= cast(1 as numeric);
select si from all1 where '2' < cast(1 as numeric);
select si from all1 where '2' <= cast(1 as numeric);
select si from all1 where '2' <> cast(1 as numeric);
select si from all1 where cast(null as char) = cast(1 as numeric);
select si from all1 where cast(null as char) > cast(1 as numeric);
select si from all1 where cast(null as char) >= cast(1 as numeric);
select si from all1 where cast(null as char) < cast(1 as numeric);
select si from all1 where cast(null as char) <= cast(1 as numeric);
select si from all1 where cast(null as char) <> cast(1 as numeric);
-- the following queries return 1 if the search condition is satisfied
-- and returns nothing if the search condition is not satisfied
select 1 from all1 where '1996-09-09' = date('1996-09-09');
select 1 from all1 where '1996-9-10' > date('1996-09-09');
select 1 from all1 where '1996-9-10' >= date('1996-09-09');
select 1 from all1 where '1996-9-10' < date('1996-09-09');
select 1 from all1 where '1996-9-10' <= date('1996-09-09');
select 1 from all1 where '1996-9-10' <> date('1996-09-09');
select 1 from all1 where cast(null as char) = date('1996-09-09');
select 1 from all1 where cast(null as char)> date('1996-09-09');
select 1 from all1 where cast(null as char)>= date('1996-09-09');
select 1 from all1 where cast(null as char)< date('1996-09-09');
select 1 from all1 where cast(null as char)<= date('1996-09-09');
select 1 from all1 where cast(null as char)<> date('1996-09-09');
select 1 from all1 where date('1996-09-09') = '1996-09-09';
select 1 from all1 where date('1996-9-10') > '1996-09-09';
select 1 from all1 where date('1996-9-10') >= '1996-09-09';
select 1 from all1 where date('1996-9-10') < '1996-09-09';
select 1 from all1 where date('1996-9-10') <= '1996-09-09';
select 1 from all1 where date('1996-9-10') <> '1996-09-09';
select 1 from all1 where date('1996-09-09') = cast(null as char);
select 1 from all1 where date('1996-9-10') > cast(null as char);
select 1 from all1 where date('1996-9-10') >= cast(null as char);
select 1 from all1 where date('1996-9-10') < cast(null as char);
select 1 from all1 where date('1996-9-10') <= cast(null as char);
select 1 from all1 where date('1996-9-10') <> cast(null as char);
select 1 from all1 where '12:12:12' = time('12:12:12');
select 1 from all1 where '12:13:12' > time('12:12:12');
select 1 from all1 where '12:13:12' >= time('12:12:12');
select 1 from all1 where '12:13:12' < time('12:12:12');
select 1 from all1 where '12:13:12' <= time('12:12:12');
select 1 from all1 where '12:13:12' <> time('12:12:12');
select 1 from all1 where cast(null as char) = time('12:12:12');
select 1 from all1 where cast(null as char) > time('12:12:12');
select 1 from all1 where cast(null as char) >= time('12:12:12');
select 1 from all1 where cast(null as char) < time('12:12:12');
select 1 from all1 where cast(null as char) <= time('12:12:12');
select 1 from all1 where cast(null as char) <> time('12:12:12');
select 1 from all1 where time('12:12:12') = '12:12:12';
select 1 from all1 where time('12:13:12') > '12:12:12';
select 1 from all1 where time('12:13:12') >= '12:12:12';
select 1 from all1 where time('12:13:12') < '12:12:12';
select 1 from all1 where time('12:13:12') <= '12:12:12';
select 1 from all1 where time('12:13:12') <> '12:12:12';
select 1 from all1 where time('12:12:12') = cast(null as char);
select 1 from all1 where time('12:13:12') > cast(null as char);
select 1 from all1 where time('12:13:12') >= cast(null as char);
select 1 from all1 where time('12:13:12') < cast(null as char);
select 1 from all1 where time('12:13:12') <= cast(null as char);
select 1 from all1 where time('12:13:12') <> cast(null as char);
select 1 from all1 where '1996-09-09 12:12:12.4' = timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where '1996-09-09 12:12:12.5' > timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where '1996-09-09 12:12:12.5' >= timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where '1996-09-09 12:12:12.5' < timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where '1996-09-09 12:12:12.5' <= timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where '1996-09-09 12:12:12.5' <> timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) = timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) > timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) >= timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) < timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) <= timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where cast(null as char) <> timestamp('1996-09-09 12:12:12.4');
select 1 from all1 where timestamp('1996-09-09 12:12:12.4' )= '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )> '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )>= '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )< '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )<= '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )<> '1996-09-09 12:12:12.4';
select 1 from all1 where timestamp('1996-09-09 12:12:12.4' )= cast(null as char);
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )> cast(null as char);
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )>= cast(null as char);
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )< cast(null as char);
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )<= cast(null as char);
select 1 from all1 where timestamp('1996-09-09 12:12:12.5' )<> cast(null as char);
select si from all1 where ' ' = X'0020';
select si from all1 where ' ' > X'001F';
select si from all1 where ' ' >= X'001F';
select si from all1 where ' ' < X'001F';
select si from all1 where ' ' <= X'001F';
select si from all1 where ' ' <> X'001F';
select si from all1 where cast(null as char) = X'0020';
select si from all1 where cast(null as char) > X'001F';
select si from all1 where cast(null as char) >= X'001F';
select si from all1 where cast(null as char) < X'001F';
select si from all1 where cast(null as char) <= X'001F';
select si from all1 where cast(null as char) <> X'001F';
select si from all1 where X'0020' = ' ';
select si from all1 where X'001F' > ' ';
select si from all1 where X'001F' >= ' ';
select si from all1 where X'001F' < ' ';
select si from all1 where X'001F' <= ' ';
select si from all1 where X'001F' <> ' ';
select si from all1 where X'0020' = cast(null as char);
select si from all1 where X'001F' > cast(null as char);
select si from all1 where X'001F' >= cast(null as char);
select si from all1 where X'001F' < cast(null as char);
select si from all1 where X'001F' <= cast(null as char);
select si from all1 where X'001F' <> cast(null as char);

create table all_c1(c_ti char(3), c_si char(10), c_i char(30), c_li char(30), 
					c_r char(50), c_dp char(50), c_dc char(10), 
					c_num char(50), c_bool char(5), c_b char(8), 
					vc_bv varchar(16), vc_lbv varchar(16), c_dt char(10),
					c_tm char(16), c_tms char(21), c_c char(30),
					vc_vc char(30), c_lvc char(30));
insert into all_c1 values ('1', '2', '3', '4', '5.5', '6.6', '7.7', '8.8',
						   'true', ' ', ' ', ' ', '1996-09-09', '12:12:12',
						   '1996-09-09 12:12:12.5', '1', '2', '33333333');

-- test qualifiers with conversion from (var)char
select 1 from all_c1, all1 where si = c_si;
select 1 from all_c1, all1 where si > c_si;
select 1 from all_c1, all1 where si >= c_si;
select 1 from all_c1, all1 where si < c_si;
select 1 from all_c1, all1 where si <= c_si;
select 1 from all_c1, all1 where si <> c_si;
select 1 from all_c1, all1 where i = c_i;
select 1 from all_c1, all1 where i > c_i;
select 1 from all_c1, all1 where i >= c_i;
select 1 from all_c1, all1 where i < c_i;
select 1 from all_c1, all1 where i <= c_i;
select 1 from all_c1, all1 where i <> c_i;
select 1 from all_c1, all1 where li = c_li;
select 1 from all_c1, all1 where li > c_li;
select 1 from all_c1, all1 where li >= c_li;
select 1 from all_c1, all1 where li < c_li;
select 1 from all_c1, all1 where li <= c_li;
select 1 from all_c1, all1 where li <> c_li;
select 1 from all_c1, all1 where r = c_r;
select 1 from all_c1, all1 where r > c_r;
select 1 from all_c1, all1 where r >= c_r;
select 1 from all_c1, all1 where r < c_r;
select 1 from all_c1, all1 where r <= c_r;
select 1 from all_c1, all1 where r <> c_r;
select 1 from all_c1, all1 where dp = c_dp;
select 1 from all_c1, all1 where dp > c_dp;
select 1 from all_c1, all1 where dp >= c_dp;
select 1 from all_c1, all1 where dp < c_dp;
select 1 from all_c1, all1 where dp <= c_dp;
select 1 from all_c1, all1 where dp <> c_dp;
select 1 from all_c1, all1 where dc = c_dc;
select 1 from all_c1, all1 where dc > c_dc;
select 1 from all_c1, all1 where dc >= c_dc;
select 1 from all_c1, all1 where dc < c_dc;
select 1 from all_c1, all1 where dc <= c_dc;
select 1 from all_c1, all1 where dc <> c_dc;
select 1 from all_c1, all1 where b = c_b;
select 1 from all_c1, all1 where b > c_b;
select 1 from all_c1, all1 where b >= c_b;
select 1 from all_c1, all1 where b < c_b;
select 1 from all_c1, all1 where b <= c_b;
select 1 from all_c1, all1 where b <> c_b;
select 1 from all_c1, all1 where bv = vc_bv;
select 1 from all_c1, all1 where bv > vc_bv;
select 1 from all_c1, all1 where bv >= vc_bv;
select 1 from all_c1, all1 where bv < vc_bv;
select 1 from all_c1, all1 where bv <= vc_bv;
select 1 from all_c1, all1 where bv <> vc_bv;
select 1 from all_c1, all1 where lbv = vc_bv;
select 1 from all_c1, all1 where lbv > vc_bv;
select 1 from all_c1, all1 where lbv >= vc_bv;
select 1 from all_c1, all1 where lbv < vc_bv;
select 1 from all_c1, all1 where lbv <= vc_bv;
select 1 from all_c1, all1 where lbv <> vc_bv;
select 1 from all_c1, all1 where dt = c_dt;
select 1 from all_c1, all1 where dt > c_dt;
select 1 from all_c1, all1 where dt >= c_dt;
select 1 from all_c1, all1 where dt < c_dt;
select 1 from all_c1, all1 where dt <= c_dt;
select 1 from all_c1, all1 where dt <> c_dt;
select 1 from all_c1, all1 where tm = c_tm;
select 1 from all_c1, all1 where tm > c_tm;
select 1 from all_c1, all1 where tm >= c_tm;
select 1 from all_c1, all1 where tm < c_tm;
select 1 from all_c1, all1 where tm <= c_tm;
select 1 from all_c1, all1 where tm <> c_tm;
select 1 from all_c1, all1 where tms = c_tms;
select 1 from all_c1, all1 where tms > c_tms;
select 1 from all_c1, all1 where tms >= c_tms;
select 1 from all_c1, all1 where tms < c_tms;
select 1 from all_c1, all1 where tms <= c_tms;
select 1 from all_c1, all1 where tms <> c_tms;
select 1 from all_c1, all1 where lvc = c_lvc;
select 1 from all_c1, all1 where lvc > c_lvc;
select 1 from all_c1, all1 where lvc >= c_lvc;
select 1 from all_c1, all1 where lvc < c_lvc;
select 1 from all_c1, all1 where lvc <= c_lvc;
select 1 from all_c1, all1 where lvc <> c_lvc;

autocommit off;
delete from all_c1;
insert into all_c1 (c_ti) values (null);

select 1 from all_c1, all1 where si = c_si;
select 1 from all_c1, all1 where si > c_si;
select 1 from all_c1, all1 where si >= c_si;
select 1 from all_c1, all1 where si < c_si;
select 1 from all_c1, all1 where si <= c_si;
select 1 from all_c1, all1 where si <> c_si;
select 1 from all_c1, all1 where i = c_i;
select 1 from all_c1, all1 where i > c_i;
select 1 from all_c1, all1 where i >= c_i;
select 1 from all_c1, all1 where i < c_i;
select 1 from all_c1, all1 where i <= c_i;
select 1 from all_c1, all1 where i <> c_i;
select 1 from all_c1, all1 where li = c_li;
select 1 from all_c1, all1 where li > c_li;
select 1 from all_c1, all1 where li >= c_li;
select 1 from all_c1, all1 where li < c_li;
select 1 from all_c1, all1 where li <= c_li;
select 1 from all_c1, all1 where li <> c_li;
select 1 from all_c1, all1 where r = c_r;
select 1 from all_c1, all1 where r > c_r;
select 1 from all_c1, all1 where r >= c_r;
select 1 from all_c1, all1 where r < c_r;
select 1 from all_c1, all1 where r <= c_r;
select 1 from all_c1, all1 where r <> c_r;
select 1 from all_c1, all1 where dp = c_dp;
select 1 from all_c1, all1 where dp > c_dp;
select 1 from all_c1, all1 where dp >= c_dp;
select 1 from all_c1, all1 where dp < c_dp;
select 1 from all_c1, all1 where dp <= c_dp;
select 1 from all_c1, all1 where dp <> c_dp;
select 1 from all_c1, all1 where dc = c_dc;
select 1 from all_c1, all1 where dc > c_dc;
select 1 from all_c1, all1 where dc >= c_dc;
select 1 from all_c1, all1 where dc < c_dc;
select 1 from all_c1, all1 where dc <= c_dc;
select 1 from all_c1, all1 where dc <> c_dc;
select 1 from all_c1, all1 where b = c_b;
select 1 from all_c1, all1 where b > c_b;
select 1 from all_c1, all1 where b >= c_b;
select 1 from all_c1, all1 where b < c_b;
select 1 from all_c1, all1 where b <= c_b;
select 1 from all_c1, all1 where b <> c_b;
select 1 from all_c1, all1 where bv = vc_bv;
select 1 from all_c1, all1 where bv > vc_bv;
select 1 from all_c1, all1 where bv >= vc_bv;
select 1 from all_c1, all1 where bv < vc_bv;
select 1 from all_c1, all1 where bv <= vc_bv;
select 1 from all_c1, all1 where bv <> vc_bv;
select 1 from all_c1, all1 where lbv = vc_bv;
select 1 from all_c1, all1 where lbv > vc_bv;
select 1 from all_c1, all1 where lbv >= vc_bv;
select 1 from all_c1, all1 where lbv < vc_bv;
select 1 from all_c1, all1 where lbv <= vc_bv;
select 1 from all_c1, all1 where lbv <> vc_bv;
select 1 from all_c1, all1 where dt = c_dt;
select 1 from all_c1, all1 where dt > c_dt;
select 1 from all_c1, all1 where dt >= c_dt;
select 1 from all_c1, all1 where dt < c_dt;
select 1 from all_c1, all1 where dt <= c_dt;
select 1 from all_c1, all1 where dt <> c_dt;
select 1 from all_c1, all1 where tm = c_tm;
select 1 from all_c1, all1 where tm > c_tm;
select 1 from all_c1, all1 where tm >= c_tm;
select 1 from all_c1, all1 where tm < c_tm;
select 1 from all_c1, all1 where tm <= c_tm;
select 1 from all_c1, all1 where tm <> c_tm;
select 1 from all_c1, all1 where tms = c_tms;
select 1 from all_c1, all1 where tms > c_tms;
select 1 from all_c1, all1 where tms >= c_tms;
select 1 from all_c1, all1 where tms < c_tms;
select 1 from all_c1, all1 where tms <= c_tms;
select 1 from all_c1, all1 where tms <> c_tms;
select 1 from all_c1, all1 where lvc = c_lvc;
select 1 from all_c1, all1 where lvc > c_lvc;
select 1 from all_c1, all1 where lvc >= c_lvc;
select 1 from all_c1, all1 where lvc < c_lvc;
select 1 from all_c1, all1 where lvc <= c_lvc;
select 1 from all_c1, all1 where lvc <> c_lvc;

rollback;

-- test start and stop positions for conversions to (var)char
create index all_c1_ti on all_c1(c_ti);
create index all_c1_si on all_c1(c_si);
create index all_c1_i on all_c1(c_i);
create index all_c1_li on all_c1(c_li);
create index all_c1_r on all_c1(c_r);
create index all_c1_dp on all_c1(c_dp);
create index all_c1_dc on all_c1(c_dc);
create index all_c1_num on all_c1(c_num);
create index all_c1_bool on all_c1(c_bool);
create index all_c1_b on all_c1(c_b);
create index all_c1_bv on all_c1(vc_bv);
create index all_c1_dt on all_c1(c_dt);
create index all_c1_tm on all_c1(c_tm);
create index all_c1_tms on all_c1(c_tms);
create index all_c1_lvc on all_c1(c_lvc);

select 1 from all_c1, all1 where si = c_si;
select 1 from all_c1, all1 where si > c_si;
select 1 from all_c1, all1 where si >= c_si;
select 1 from all_c1, all1 where si < c_si;
select 1 from all_c1, all1 where si <= c_si;
select 1 from all_c1, all1 where i = c_i;
select 1 from all_c1, all1 where i > c_i;
select 1 from all_c1, all1 where i >= c_i;
select 1 from all_c1, all1 where i < c_i;
select 1 from all_c1, all1 where i <= c_i;
select 1 from all_c1, all1 where li = c_li;
select 1 from all_c1, all1 where li > c_li;
select 1 from all_c1, all1 where li >= c_li;
select 1 from all_c1, all1 where li < c_li;
select 1 from all_c1, all1 where li <= c_li;
select 1 from all_c1, all1 where r = c_r;
select 1 from all_c1, all1 where r > c_r;
select 1 from all_c1, all1 where r >= c_r;
select 1 from all_c1, all1 where r < c_r;
select 1 from all_c1, all1 where r <= c_r;
select 1 from all_c1, all1 where dp = c_dp;
select 1 from all_c1, all1 where dp > c_dp;
select 1 from all_c1, all1 where dp >= c_dp;
select 1 from all_c1, all1 where dp < c_dp;
select 1 from all_c1, all1 where dp <= c_dp;
select 1 from all_c1, all1 where dc = c_dc;
select 1 from all_c1, all1 where dc > c_dc;
select 1 from all_c1, all1 where dc >= c_dc;
select 1 from all_c1, all1 where dc < c_dc;
select 1 from all_c1, all1 where dc <= c_dc;
select 1 from all_c1, all1 where b = c_b;
select 1 from all_c1, all1 where b > c_b;
select 1 from all_c1, all1 where b >= c_b;
select 1 from all_c1, all1 where b < c_b;
select 1 from all_c1, all1 where b <= c_b;
select 1 from all_c1, all1 where bv = vc_bv;
select 1 from all_c1, all1 where bv > vc_bv;
select 1 from all_c1, all1 where bv >= vc_bv;
select 1 from all_c1, all1 where bv < vc_bv;
select 1 from all_c1, all1 where bv <= vc_bv;
select 1 from all_c1, all1 where lbv = vc_bv;
select 1 from all_c1, all1 where lbv > vc_bv;
select 1 from all_c1, all1 where lbv >= vc_bv;
select 1 from all_c1, all1 where lbv < vc_bv;
select 1 from all_c1, all1 where lbv <= vc_bv;
select 1 from all_c1, all1 where dt = c_dt;
select 1 from all_c1, all1 where dt > c_dt;
select 1 from all_c1, all1 where dt >= c_dt;
select 1 from all_c1, all1 where dt < c_dt;
select 1 from all_c1, all1 where dt <= c_dt;
select 1 from all_c1, all1 where tm = c_tm;
select 1 from all_c1, all1 where tm > c_tm;
select 1 from all_c1, all1 where tm >= c_tm;
select 1 from all_c1, all1 where tm < c_tm;
select 1 from all_c1, all1 where tm <= c_tm;
select 1 from all_c1, all1 where tms = c_tms;
select 1 from all_c1, all1 where tms > c_tms;
select 1 from all_c1, all1 where tms >= c_tms;
select 1 from all_c1, all1 where tms < c_tms;
select 1 from all_c1, all1 where tms <= c_tms;
select 1 from all_c1, all1 where lvc = c_lvc;
select 1 from all_c1, all1 where lvc > c_lvc;
select 1 from all_c1, all1 where lvc >= c_lvc;
select 1 from all_c1, all1 where lvc < c_lvc;
select 1 from all_c1, all1 where lvc <= c_lvc;

delete from all_c1;
insert into all_c1 (c_ti) values (null);

select 1 from all_c1, all1 where si = c_si;
select 1 from all_c1, all1 where si > c_si;
select 1 from all_c1, all1 where si >= c_si;
select 1 from all_c1, all1 where si < c_si;
select 1 from all_c1, all1 where si <= c_si;
select 1 from all_c1, all1 where i = c_i;
select 1 from all_c1, all1 where i > c_i;
select 1 from all_c1, all1 where i >= c_i;
select 1 from all_c1, all1 where i < c_i;
select 1 from all_c1, all1 where i <= c_i;
select 1 from all_c1, all1 where li = c_li;
select 1 from all_c1, all1 where li > c_li;
select 1 from all_c1, all1 where li >= c_li;
select 1 from all_c1, all1 where li < c_li;
select 1 from all_c1, all1 where li <= c_li;
select 1 from all_c1, all1 where r = c_r;
select 1 from all_c1, all1 where r > c_r;
select 1 from all_c1, all1 where r >= c_r;
select 1 from all_c1, all1 where r < c_r;
select 1 from all_c1, all1 where r <= c_r;
select 1 from all_c1, all1 where dp = c_dp;
select 1 from all_c1, all1 where dp > c_dp;
select 1 from all_c1, all1 where dp >= c_dp;
select 1 from all_c1, all1 where dp < c_dp;
select 1 from all_c1, all1 where dp <= c_dp;
select 1 from all_c1, all1 where dc = c_dc;
select 1 from all_c1, all1 where dc > c_dc;
select 1 from all_c1, all1 where dc >= c_dc;
select 1 from all_c1, all1 where dc < c_dc;
select 1 from all_c1, all1 where dc <= c_dc;
select 1 from all_c1, all1 where b = c_b;
select 1 from all_c1, all1 where b > c_b;
select 1 from all_c1, all1 where b >= c_b;
select 1 from all_c1, all1 where b < c_b;
select 1 from all_c1, all1 where b <= c_b;
select 1 from all_c1, all1 where bv = vc_bv;
select 1 from all_c1, all1 where bv > vc_bv;
select 1 from all_c1, all1 where bv >= vc_bv;
select 1 from all_c1, all1 where bv < vc_bv;
select 1 from all_c1, all1 where bv <= vc_bv;
select 1 from all_c1, all1 where lbv = vc_bv;
select 1 from all_c1, all1 where lbv > vc_bv;
select 1 from all_c1, all1 where lbv >= vc_bv;
select 1 from all_c1, all1 where lbv < vc_bv;
select 1 from all_c1, all1 where lbv <= vc_bv;
select 1 from all_c1, all1 where dt = c_dt;
select 1 from all_c1, all1 where dt > c_dt;
select 1 from all_c1, all1 where dt >= c_dt;
select 1 from all_c1, all1 where dt < c_dt;
select 1 from all_c1, all1 where dt <= c_dt;
select 1 from all_c1, all1 where tm = c_tm;
select 1 from all_c1, all1 where tm > c_tm;
select 1 from all_c1, all1 where tm >= c_tm;
select 1 from all_c1, all1 where tm < c_tm;
select 1 from all_c1, all1 where tm <= c_tm;
select 1 from all_c1, all1 where tms = c_tms;
select 1 from all_c1, all1 where tms > c_tms;
select 1 from all_c1, all1 where tms >= c_tms;
select 1 from all_c1, all1 where tms < c_tms;
select 1 from all_c1, all1 where tms <= c_tms;
select 1 from all_c1, all1 where lvc = c_lvc;
select 1 from all_c1, all1 where lvc > c_lvc;
select 1 from all_c1, all1 where lvc >= c_lvc;
select 1 from all_c1, all1 where lvc < c_lvc;
select 1 from all_c1, all1 where lvc <= c_lvc;

-- drop the indexes;
rollback;


-- test qualifiers with conversion to (var)char
select 1 from all1, all_c1 where si = c_si;
select 1 from all1, all_c1 where si > c_si;
select 1 from all1, all_c1 where si >= c_si;
select 1 from all1, all_c1 where si < c_si;
select 1 from all1, all_c1 where si <= c_si;
select 1 from all1, all_c1 where si <> c_si;
select 1 from all1, all_c1 where i = c_i;
select 1 from all1, all_c1 where i > c_i;
select 1 from all1, all_c1 where i >= c_i;
select 1 from all1, all_c1 where i < c_i;
select 1 from all1, all_c1 where i <= c_i;
select 1 from all1, all_c1 where i <> c_i;
select 1 from all1, all_c1 where li = c_li;
select 1 from all1, all_c1 where li > c_li;
select 1 from all1, all_c1 where li >= c_li;
select 1 from all1, all_c1 where li < c_li;
select 1 from all1, all_c1 where li <= c_li;
select 1 from all1, all_c1 where li <> c_li;
select 1 from all1, all_c1 where r = c_r;
select 1 from all1, all_c1 where r > c_r;
select 1 from all1, all_c1 where r >= c_r;
select 1 from all1, all_c1 where r < c_r;
select 1 from all1, all_c1 where r <= c_r;
select 1 from all1, all_c1 where r <> c_r;
select 1 from all1, all_c1 where dp = c_dp;
select 1 from all1, all_c1 where dp > c_dp;
select 1 from all1, all_c1 where dp >= c_dp;
select 1 from all1, all_c1 where dp < c_dp;
select 1 from all1, all_c1 where dp <= c_dp;
select 1 from all1, all_c1 where dp <> c_dp;
select 1 from all1, all_c1 where dc = c_dc;
select 1 from all1, all_c1 where dc > c_dc;
select 1 from all1, all_c1 where dc >= c_dc;
select 1 from all1, all_c1 where dc < c_dc;
select 1 from all1, all_c1 where dc <= c_dc;
select 1 from all1, all_c1 where dc <> c_dc;
select 1 from all1, all_c1 where b = c_b;
select 1 from all1, all_c1 where b > c_b;
select 1 from all1, all_c1 where b >= c_b;
select 1 from all1, all_c1 where b < c_b;
select 1 from all1, all_c1 where b <= c_b;
select 1 from all1, all_c1 where b <> c_b;
select 1 from all1, all_c1 where bv = vc_bv;
select 1 from all1, all_c1 where bv > vc_bv;
select 1 from all1, all_c1 where bv >= vc_bv;
select 1 from all1, all_c1 where bv < vc_bv;
select 1 from all1, all_c1 where bv <= vc_bv;
select 1 from all1, all_c1 where bv <> vc_bv;
select 1 from all1, all_c1 where lbv = vc_bv;
select 1 from all1, all_c1 where lbv > vc_bv;
select 1 from all1, all_c1 where lbv >= vc_bv;
select 1 from all1, all_c1 where lbv < vc_bv;
select 1 from all1, all_c1 where lbv <= vc_bv;
select 1 from all1, all_c1 where lbv <> vc_bv;
select 1 from all1, all_c1 where dt = c_dt;
select 1 from all1, all_c1 where dt > c_dt;
select 1 from all1, all_c1 where dt >= c_dt;
select 1 from all1, all_c1 where dt < c_dt;
select 1 from all1, all_c1 where dt <= c_dt;
select 1 from all1, all_c1 where dt <> c_dt;
select 1 from all1, all_c1 where tm = c_tm;
select 1 from all1, all_c1 where tm > c_tm;
select 1 from all1, all_c1 where tm >= c_tm;
select 1 from all1, all_c1 where tm < c_tm;
select 1 from all1, all_c1 where tm <= c_tm;
select 1 from all1, all_c1 where tm <> c_tm;
select 1 from all1, all_c1 where tms = c_tms;
select 1 from all1, all_c1 where tms > c_tms;
select 1 from all1, all_c1 where tms >= c_tms;
select 1 from all1, all_c1 where tms < c_tms;
select 1 from all1, all_c1 where tms <= c_tms;
select 1 from all1, all_c1 where tms <> c_tms;
select 1 from all1, all_c1 where lvc = c_lvc;
select 1 from all1, all_c1 where lvc > c_lvc;
select 1 from all1, all_c1 where lvc >= c_lvc;
select 1 from all1, all_c1 where lvc < c_lvc;
select 1 from all1, all_c1 where lvc <= c_lvc;
select 1 from all1, all_c1 where lvc <> c_lvc;

delete from all1;
insert into all1 (si) values (null);

select 1 from all1, all_c1 where si = c_si;
select 1 from all1, all_c1 where si > c_si;
select 1 from all1, all_c1 where si >= c_si;
select 1 from all1, all_c1 where si < c_si;
select 1 from all1, all_c1 where si <= c_si;
select 1 from all1, all_c1 where si <> c_si;
select 1 from all1, all_c1 where i = c_i;
select 1 from all1, all_c1 where i > c_i;
select 1 from all1, all_c1 where i >= c_i;
select 1 from all1, all_c1 where i < c_i;
select 1 from all1, all_c1 where i <= c_i;
select 1 from all1, all_c1 where i <> c_i;
select 1 from all1, all_c1 where li = c_li;
select 1 from all1, all_c1 where li > c_li;
select 1 from all1, all_c1 where li >= c_li;
select 1 from all1, all_c1 where li < c_li;
select 1 from all1, all_c1 where li <= c_li;
select 1 from all1, all_c1 where li <> c_li;
select 1 from all1, all_c1 where r = c_r;
select 1 from all1, all_c1 where r > c_r;
select 1 from all1, all_c1 where r >= c_r;
select 1 from all1, all_c1 where r < c_r;
select 1 from all1, all_c1 where r <= c_r;
select 1 from all1, all_c1 where r <> c_r;
select 1 from all1, all_c1 where dp = c_dp;
select 1 from all1, all_c1 where dp > c_dp;
select 1 from all1, all_c1 where dp >= c_dp;
select 1 from all1, all_c1 where dp < c_dp;
select 1 from all1, all_c1 where dp <= c_dp;
select 1 from all1, all_c1 where dp <> c_dp;
select 1 from all1, all_c1 where dc = c_dc;
select 1 from all1, all_c1 where dc > c_dc;
select 1 from all1, all_c1 where dc >= c_dc;
select 1 from all1, all_c1 where dc < c_dc;
select 1 from all1, all_c1 where dc <= c_dc;
select 1 from all1, all_c1 where dc <> c_dc;
select 1 from all1, all_c1 where b = c_b;
select 1 from all1, all_c1 where b > c_b;
select 1 from all1, all_c1 where b >= c_b;
select 1 from all1, all_c1 where b < c_b;
select 1 from all1, all_c1 where b <= c_b;
select 1 from all1, all_c1 where b <> c_b;
select 1 from all1, all_c1 where bv = vc_bv;
select 1 from all1, all_c1 where bv > vc_bv;
select 1 from all1, all_c1 where bv >= vc_bv;
select 1 from all1, all_c1 where bv < vc_bv;
select 1 from all1, all_c1 where bv <= vc_bv;
select 1 from all1, all_c1 where bv <> vc_bv;
select 1 from all1, all_c1 where lbv = vc_bv;
select 1 from all1, all_c1 where lbv > vc_bv;
select 1 from all1, all_c1 where lbv >= vc_bv;
select 1 from all1, all_c1 where lbv < vc_bv;
select 1 from all1, all_c1 where lbv <= vc_bv;
select 1 from all1, all_c1 where lbv <> vc_bv;
select 1 from all1, all_c1 where dt = c_dt;
select 1 from all1, all_c1 where dt > c_dt;
select 1 from all1, all_c1 where dt >= c_dt;
select 1 from all1, all_c1 where dt < c_dt;
select 1 from all1, all_c1 where dt <= c_dt;
select 1 from all1, all_c1 where dt <> c_dt;
select 1 from all1, all_c1 where tm = c_tm;
select 1 from all1, all_c1 where tm > c_tm;
select 1 from all1, all_c1 where tm >= c_tm;
select 1 from all1, all_c1 where tm < c_tm;
select 1 from all1, all_c1 where tm <= c_tm;
select 1 from all1, all_c1 where tm <> c_tm;
select 1 from all1, all_c1 where tms = c_tms;
select 1 from all1, all_c1 where tms > c_tms;
select 1 from all1, all_c1 where tms >= c_tms;
select 1 from all1, all_c1 where tms < c_tms;
select 1 from all1, all_c1 where tms <= c_tms;
select 1 from all1, all_c1 where tms <> c_tms;
select 1 from all1, all_c1 where lvc = c_lvc;
select 1 from all1, all_c1 where lvc > c_lvc;
select 1 from all1, all_c1 where lvc >= c_lvc;
select 1 from all1, all_c1 where lvc < c_lvc;
select 1 from all1, all_c1 where lvc <= c_lvc;
select 1 from all1, all_c1 where lvc <> c_lvc;

rollback;

-- test start and stop positions for conversions to (var)char
create index all1_si on all1(si);
create index all1_i on all1(i);
create index all1_li on all1(li);
create index all1_r on all1(r);
create index all1_dp on all1(dp);
create index all1_dc on all1(dc);
create index all1_num on all1(num);
create index all1_b on all1(b);
create index all1_bv on all1(bv);
create index all1_lbv on all1(lbv);
create index all1_dt on all1(dt);
create index all1_tm on all1(tm);
create index all1_tms on all1(tms);

select 1 from all1, all_c1 where si = c_si;
select 1 from all1, all_c1 where si > c_si;
select 1 from all1, all_c1 where si >= c_si;
select 1 from all1, all_c1 where si < c_si;
select 1 from all1, all_c1 where si <= c_si;
select 1 from all1, all_c1 where i = c_i;
select 1 from all1, all_c1 where i > c_i;
select 1 from all1, all_c1 where i >= c_i;
select 1 from all1, all_c1 where i < c_i;
select 1 from all1, all_c1 where i <= c_i;
select 1 from all1, all_c1 where li = c_li;
select 1 from all1, all_c1 where li > c_li;
select 1 from all1, all_c1 where li >= c_li;
select 1 from all1, all_c1 where li < c_li;
select 1 from all1, all_c1 where li <= c_li;
select 1 from all1, all_c1 where r = c_r;
select 1 from all1, all_c1 where r > c_r;
select 1 from all1, all_c1 where r >= c_r;
select 1 from all1, all_c1 where r < c_r;
select 1 from all1, all_c1 where r <= c_r;
select 1 from all1, all_c1 where dp = c_dp;
select 1 from all1, all_c1 where dp > c_dp;
select 1 from all1, all_c1 where dp >= c_dp;
select 1 from all1, all_c1 where dp < c_dp;
select 1 from all1, all_c1 where dp <= c_dp;
select 1 from all1, all_c1 where dc = c_dc;
select 1 from all1, all_c1 where dc > c_dc;
select 1 from all1, all_c1 where dc >= c_dc;
select 1 from all1, all_c1 where dc < c_dc;
select 1 from all1, all_c1 where dc <= c_dc;
select 1 from all1, all_c1 where b = c_b;
select 1 from all1, all_c1 where b > c_b;
select 1 from all1, all_c1 where b >= c_b;
select 1 from all1, all_c1 where b < c_b;
select 1 from all1, all_c1 where b <= c_b;
select 1 from all1, all_c1 where bv = vc_bv;
select 1 from all1, all_c1 where bv > vc_bv;
select 1 from all1, all_c1 where bv >= vc_bv;
select 1 from all1, all_c1 where bv < vc_bv;
select 1 from all1, all_c1 where bv <= vc_bv;
select 1 from all1, all_c1 where lbv = vc_bv;
select 1 from all1, all_c1 where lbv > vc_bv;
select 1 from all1, all_c1 where lbv >= vc_bv;
select 1 from all1, all_c1 where lbv < vc_bv;
select 1 from all1, all_c1 where lbv <= vc_bv;
select 1 from all1, all_c1 where dt = c_dt;
select 1 from all1, all_c1 where dt > c_dt;
select 1 from all1, all_c1 where dt >= c_dt;
select 1 from all1, all_c1 where dt < c_dt;
select 1 from all1, all_c1 where dt <= c_dt;
select 1 from all1, all_c1 where tm = c_tm;
select 1 from all1, all_c1 where tm > c_tm;
select 1 from all1, all_c1 where tm >= c_tm;
select 1 from all1, all_c1 where tm < c_tm;
select 1 from all1, all_c1 where tm <= c_tm;
select 1 from all1, all_c1 where tms = c_tms;
select 1 from all1, all_c1 where tms > c_tms;
select 1 from all1, all_c1 where tms >= c_tms;
select 1 from all1, all_c1 where tms < c_tms;
select 1 from all1, all_c1 where tms <= c_tms;
select 1 from all1, all_c1 where lvc = c_lvc;
select 1 from all1, all_c1 where lvc > c_lvc;
select 1 from all1, all_c1 where lvc >= c_lvc;
select 1 from all1, all_c1 where lvc < c_lvc;
select 1 from all1, all_c1 where lvc <= c_lvc;

delete from all1;
insert into all1(si) values (null);

select 1 from all1, all_c1 where si = c_si;
select 1 from all1, all_c1 where si > c_si;
select 1 from all1, all_c1 where si >= c_si;
select 1 from all1, all_c1 where si < c_si;
select 1 from all1, all_c1 where si <= c_si;
select 1 from all1, all_c1 where i = c_i;
select 1 from all1, all_c1 where i > c_i;
select 1 from all1, all_c1 where i >= c_i;
select 1 from all1, all_c1 where i < c_i;
select 1 from all1, all_c1 where i <= c_i;
select 1 from all1, all_c1 where li = c_li;
select 1 from all1, all_c1 where li > c_li;
select 1 from all1, all_c1 where li >= c_li;
select 1 from all1, all_c1 where li < c_li;
select 1 from all1, all_c1 where li <= c_li;
select 1 from all1, all_c1 where r = c_r;
select 1 from all1, all_c1 where r > c_r;
select 1 from all1, all_c1 where r >= c_r;
select 1 from all1, all_c1 where r < c_r;
select 1 from all1, all_c1 where r <= c_r;
select 1 from all1, all_c1 where dp = c_dp;
select 1 from all1, all_c1 where dp > c_dp;
select 1 from all1, all_c1 where dp >= c_dp;
select 1 from all1, all_c1 where dp < c_dp;
select 1 from all1, all_c1 where dp <= c_dp;
select 1 from all1, all_c1 where dc = c_dc;
select 1 from all1, all_c1 where dc > c_dc;
select 1 from all1, all_c1 where dc >= c_dc;
select 1 from all1, all_c1 where dc < c_dc;
select 1 from all1, all_c1 where dc <= c_dc;
select 1 from all1, all_c1 where b = c_b;
select 1 from all1, all_c1 where b > c_b;
select 1 from all1, all_c1 where b >= c_b;
select 1 from all1, all_c1 where b < c_b;
select 1 from all1, all_c1 where b <= c_b;
select 1 from all1, all_c1 where bv = vc_bv;
select 1 from all1, all_c1 where bv > vc_bv;
select 1 from all1, all_c1 where bv >= vc_bv;
select 1 from all1, all_c1 where bv < vc_bv;
select 1 from all1, all_c1 where bv <= vc_bv;
select 1 from all1, all_c1 where lbv = vc_bv;
select 1 from all1, all_c1 where lbv > vc_bv;
select 1 from all1, all_c1 where lbv >= vc_bv;
select 1 from all1, all_c1 where lbv < vc_bv;
select 1 from all1, all_c1 where lbv <= vc_bv;
select 1 from all1, all_c1 where dt = c_dt;
select 1 from all1, all_c1 where dt > c_dt;
select 1 from all1, all_c1 where dt >= c_dt;
select 1 from all1, all_c1 where dt < c_dt;
select 1 from all1, all_c1 where dt <= c_dt;
select 1 from all1, all_c1 where tm = c_tm;
select 1 from all1, all_c1 where tm > c_tm;
select 1 from all1, all_c1 where tm >= c_tm;
select 1 from all1, all_c1 where tm < c_tm;
select 1 from all1, all_c1 where tm <= c_tm;
select 1 from all1, all_c1 where tms = c_tms;
select 1 from all1, all_c1 where tms > c_tms;
select 1 from all1, all_c1 where tms >= c_tms;
select 1 from all1, all_c1 where tms < c_tms;
select 1 from all1, all_c1 where tms <= c_tms;
select 1 from all1, all_c1 where lvc = c_lvc;
select 1 from all1, all_c1 where lvc > c_lvc;
select 1 from all1, all_c1 where lvc >= c_lvc;
select 1 from all1, all_c1 where lvc < c_lvc;
select 1 from all1, all_c1 where lvc <= c_lvc;

-- drop the indexes;
rollback;



delete from all_c1;
-- insert with implicit conversions to (var)char
insert into all_c1 select * from all1;
select c_ti, si, c_si, i, c_i from all1, all_c1;
select li, c_li, r, c_r, dp, c_dp from all1, all_c1;
select dc, c_dc, num, c_num, c_bool from all1, all_c1;
select b, c_b, bv, vc_bv, lbv, vc_lbv, dt, c_dt from all1, all_c1;
select tm, c_tm, tms, c_tms, c, c_c from all1, all_c1;
select vc, vc_vc, lvc, c_lvc from all1, all_c1;

-- insert with implicit conversions from (var)char
insert into all1 select c_ti, c_si, c_i, c_li, c_r, c_dp,
					    c_dc, c_num, c_bool, ' ', ' ', ' ',
					    c_dt, c_tm, c_tms, '1', '2' from all_c1;
select c_ti, si, c_si, i, c_i from all1, all_c1;
select li, c_li, r, c_r, dp, c_dp from all1, all_c1;
select dc, c_dc, num, c_num, c_bool from all1, all_c1;
select b, c_b, bv, vc_bv, lbv, vc_lbv, dt, c_dt from all1, all_c1;
select tm, c_tm, tms, c_tms, c, c_c from all1, all_c1;
select vc, vc_vc, lvc, c_lvc from all1, all_c1;

rollback;

-- more insert conversions
create table t5_2(dc decimal(5,2), num numeric(5,2));
-- bug 827, NormalizeResultSet with char->decimal conversions
insert into t5_2 values ('11.95', '95.11');
select * from t5_2;
rollback;

-- update tests
alter table all1 add column c30 char(30) ;
alter table all1 add column vc30 varchar(30) ;
alter table all1 add column lvc2 long varchar ;
select * from all1;
update all1 set si = '11';
update all1 set i = '11';
update all1 set li = '11';
update all1 set r = '11.11';
update all1 set dp = '11.11';
update all1 set dc = '11.11';
update all1 set num = '11.11';
update all1 set b = X'21';
update all1 set bv = X'21';
update all1 set lbv = X'21';
update all1 set dt = '1900-01-01';
update all1 set tm = '08:08:08';
update all1 set tms = '1990-01-01 08:08:08.6';
update all1 set lvc = '44444444';

select * from all1;

select c30, vc30, lvc2 from all1;
update all1 set c30 = si, vc30 = si, lvc2 = si;
update all1 set c30 = i, vc30 = i, lvc2 = i;
update all1 set c30 = li, vc30 = li, lvc2 = li;
update all1 set c30 = r, vc30 = r, lvc2 = r;
select c30, vc30, lvc2 from all1;
update all1 set c30 = dp, vc30 = dp, lvc2 = dp;
update all1 set c30 = dc, vc30 = dc, lvc2 = dc;
update all1 set c30 = num, vc30 = num, lvc2 = num;
select c30, vc30, lvc2 from all1;
update all1 set c30 = b, vc30 = b, lvc2 = b;
select c30, vc30, lvc2 from all1;
update all1 set c30 = bv, vc30 = bv, lvc2 = bv;
update all1 set c30 = lbv, vc30 = lbv, lvc2 = lbv;
update all1 set c30 = dt, vc30 = dt, lvc2 = dt;
update all1 set c30 = tm, vc30 = tm, lvc2 = tm;
update all1 set c30 = tms, vc30 = tms, lvc2 = tms;
select c30, vc30, lvc2 from all1;

rollback;
autocommit off;

-- bug 5838 - arithmetic operators should not be applied to character strings
-- the following arithmetic operations should fail
values 1 + '2';
values 1 - '2';
values 1 * '2';
values 4 / '2';
values 1.1 + '2';
values 1.1 - '2';
values 1.1 * '2';
values 4.4 / '2';
values 1.1 + '2.2';
values 1.1 - '2.2';
values 1.1 * '2.2';
values 4.4 / '2.2';

-- concatentation
values '$' || cast(1 as smallint) || '$';
values '$' || 1 || '$';
values '$' || cast(1 as bigint) || '$';
values '$' || cast(1.1 as real) || '$';
values '$' || cast(1.1 as double precision) || '$';
values '$' || 1.1 || '$';
values '$' || cast(1.1 as decimal(8,3)) || '$';
values '$' || 'abcd' || '$';
values '$' || date('1996-09-09') || '$';
values '$' || time('10:11:12') || '$';
values '$' || timestamp('1996-09-09 10:11:12.4' )|| '$';

-- length functions
values length(cast(1 as smallint));
values length(cast(1 as int));
values length(cast(1 as bigint));
values length(cast(1.1 as real));
values length(cast(1.1 as double precision));
values length(1.1);
values length(cast(1.1 as decimal(8,3)));
values length('four');
values length(date('1996-09-10'));
values length(time('10:11:12'));
values length(timestamp('1996-09-10 10:11:12.4'));

-- extract
values year( '1996-01-10');
values month( '1996-01-10');
values day( '1996-01-10');
values hour( '10:11:12');
values minute( '10:11:12');
values second( '10:11:12');

-- like
select si from all1 where 1 like '%';
-- bug 5845
select 1 from all1 where date('1996-09-10') like '19%';
select si from all1 where '1' like 1;
-- integer 1 gets converted to 1 followed by 0 spaces
-- so for kicks put a single space and make sure it is
-- not the same
select si from all1 where '1 ' like 1;
select si from all1 where '1996-09-10' like date('1996-09-10');

prepare p1 as 'select 1 from all1 where si like ?';
execute p1 using 'values 1';
execute p1 using 'values ''1''';

-- conversions involving non-canonical date, time, and timestamp strings
create table t (d date, t time, ts timestamp);
create index txd on t(d);
create index txt on t(t);
create index txts on t(ts);
insert into t values (CHAR('2000-01-07'), 
		      CHAR('20:06:58'), 
		      CHAR('2000-01-07 20:06:58.9000'));
insert into t values (CHAR('2000-1-06'), 
		      CHAR('20:06:57'), 
		      CHAR('2000-01-7 20:06:58.8000'));
VALUES SYSCS_UTIL.SYSCS_CHECK_TABLE('APP', 'T');

-- bug 2247, make sure that constant retyping
-- (avoiding unnecessary normalization at execution)
-- does not screw up implicit conversions
create table x(x varchar(10));
insert into x values 123;
select * from x;

-- clean up 
drop table all_c1;
drop table t;
drop table x;
commit;

-- ** insert implicitConversionsNegative.sql
-- negate tests for implicit conversions
-- to/from (var)char

-- union

autocommit on;
values cast(1 as smallint), 'a';
values 'a', cast(1 as smallint);
values cast(1 as smallint), '1.1';
values '1.1', cast(1 as smallint);
values 1, 'a';
values 'a', 1;
values 1, '1.1';
values '1.1', a;
values cast(1 as bigint), 'a';
values 'a', cast(1 as bigint);
values cast(1 as bigint), '1.1';
values '1.1', cast(1 as bigint);

values cast(1.1 as real), 'a';
values 'a', cast(1.1 as real);
values cast(1.1 as double precision), 'a';
values 'a', cast(1.1 as double precision);
values 1.1, 'a';
values 'a', 1.1;

values true, 'a';
values 'a', true;

values date('1996-09-09'), 'a';
values 'a', date('1996-09-09');
values time('11:11:11'), 'a';
values 'a', time('11:11:11');
values timestamp('1996-09-09 11:11:11.5'), 'a';
values 'a', timestamp('1996-09-09 11:11:11.5');

-- comparisons at the language level
select si from all1 where cast(1 as smallint) = 'a';
select si from all1 where cast(1 as smallint) = '1.1';
select si from all1 where cast(1 as smallint) > 'a';
select si from all1 where cast(1 as smallint) > '1.1';
select si from all1 where cast(1 as smallint) >= 'a';
select si from all1 where cast(1 as smallint) >= '1.1';
select si from all1 where cast(1 as smallint) < 'a';
select si from all1 where cast(1 as smallint) < '1.1';
select si from all1 where cast(1 as smallint) <= 'a';
select si from all1 where cast(1 as smallint) <= '1.1';
select si from all1 where cast(1 as smallint) <> 'a';
select si from all1 where cast(1 as smallint) <> '1.1';
select si from all1 where 'a' = cast(1 as smallint);
select si from all1 where '1.1' = cast(1 as smallint);
select si from all1 where 'a' > cast(1 as smallint);
select si from all1 where '1.1' > cast(1 as smallint);
select si from all1 where 'a' >= cast(1 as smallint);
select si from all1 where '1.1' >= cast(1 as smallint);
select si from all1 where 'a' < cast(1 as smallint);
select si from all1 where '1.1' < cast(1 as smallint);
select si from all1 where 'a' <= cast(1 as smallint);
select si from all1 where '1.1' <= cast(1 as smallint);
select si from all1 where 'a' <> cast(1 as smallint);
select si from all1 where '1.1' <> cast(1 as smallint);
select si from all1 where cast(1 as int) = 'a';
select si from all1 where cast(1 as int) = '1.1';
select si from all1 where cast(1 as int) > 'a';
select si from all1 where cast(1 as int) > '1.1';
select si from all1 where cast(1 as int) >= 'a';
select si from all1 where cast(1 as int) >= '1.1';
select si from all1 where cast(1 as int) < 'a';
select si from all1 where cast(1 as int) < '1.1';
select si from all1 where cast(1 as int) <= 'a';
select si from all1 where cast(1 as int) <= '1.1';
select si from all1 where cast(1 as int) <> 'a';
select si from all1 where cast(1 as int) <> '1.1';
select si from all1 where 'a' = cast(1 as int);
select si from all1 where '1.1' = cast(1 as int);
select si from all1 where 'a' > cast(1 as int);
select si from all1 where '1.1' > cast(1 as int);
select si from all1 where 'a' >= cast(1 as int);
select si from all1 where '1.1' >= cast(1 as int);
select si from all1 where 'a' < cast(1 as int);
select si from all1 where '1.1' < cast(1 as int);
select si from all1 where 'a' <= cast(1 as int);
select si from all1 where '1.1' <= cast(1 as int);
select si from all1 where 'a' <> cast(1 as int);
select si from all1 where '1.1' <> cast(1 as int);
select si from all1 where cast(1 as bigint) = 'a';
select si from all1 where cast(1 as bigint) = '1.1';
select si from all1 where cast(1 as bigint) > 'a';
select si from all1 where cast(1 as bigint) > '1.1';
select si from all1 where cast(1 as bigint) >= 'a';
select si from all1 where cast(1 as bigint) >= '1.1';
select si from all1 where cast(1 as bigint) < 'a';
select si from all1 where cast(1 as bigint) < '1.1';
select si from all1 where cast(1 as bigint) <= 'a';
select si from all1 where cast(1 as bigint) <= '1.1';
select si from all1 where cast(1 as bigint) <> 'a';
select si from all1 where cast(1 as bigint) <> '1.1';
select si from all1 where 'a' = cast(1 as bigint);
select si from all1 where '1.1' = cast(1 as bigint);
select si from all1 where 'a' > cast(1 as bigint);
select si from all1 where '1.1' > cast(1 as bigint);
select si from all1 where 'a' >= cast(1 as bigint);
select si from all1 where '1.1' >= cast(1 as bigint);
select si from all1 where 'a' < cast(1 as bigint);
select si from all1 where '1.1' < cast(1 as bigint);
select si from all1 where 'a' <= cast(1 as bigint);
select si from all1 where '1.1' <= cast(1 as bigint);
select si from all1 where 'a' <> cast(1 as bigint);
select si from all1 where '1.1' <> cast(1 as smallint);

select si from all1 where cast(1.1 as real) = 'a';
select si from all1 where cast(1.1 as real) > 'a';
select si from all1 where cast(1.1 as real) >= 'a';
select si from all1 where cast(1.1 as real) < 'a';
select si from all1 where cast(1.1 as real) <= 'a';
select si from all1 where cast(1.1 as real) <> 'a';
select si from all1 where 'a' = cast(1.1 as real);
select si from all1 where 'a' > cast(1.1 as real);
select si from all1 where 'a' >= cast(1.1 as real);
select si from all1 where 'a' < cast(1.1 as real);
select si from all1 where 'a' <= cast(1.1 as real);
select si from all1 where 'a' <> cast(1.1 as real);
select si from all1 where cast(1.1 as double precision) = 'a';
select si from all1 where cast(1.1 as double precision) > 'a';
select si from all1 where cast(1.1 as double precision) >= 'a';
select si from all1 where cast(1.1 as double precision) < 'a';
select si from all1 where cast(1.1 as double precision) <= 'a';
select si from all1 where cast(1.1 as double precision) <> 'a';
select si from all1 where 'a' = cast(1.1 as double precision);
select si from all1 where 'a' > cast(1.1 as double precision);
select si from all1 where 'a' >= cast(1.1 as double precision);
select si from all1 where 'a' < cast(1.1 as double precision);
select si from all1 where 'a' <= cast(1.1 as double precision);
select si from all1 where 'a' <> cast(1.1 as double precision);
select si from all1 where 1.1 = 'a';
select si from all1 where 1.1 > 'a';
select si from all1 where 1.1 >= 'a';
select si from all1 where 1.1 < 'a';
select si from all1 where 1.1 <= 'a';
select si from all1 where 1.1 <> 'a';
select si from all1 where 'a' = 1.1; 
select si from all1 where 'a' > 1.1;
select si from all1 where 'a' >= 1.1; 
select si from all1 where 'a' < 1.1; 
select si from all1 where 'a' <= 1.1; 
select si from all1 where 'a' <> 1.1; 

select si from all1 where date('1996-09-09') = 'a';
select si from all1 where date('1996-09-09') > 'a';
select si from all1 where date('1996-09-09') >= 'a';
select si from all1 where date('1996-09-09') < 'a';
select si from all1 where date('1996-09-09') <= 'a';
select si from all1 where date('1996-09-09') <> 'a';
select si from all1 where 'a' = date('1996-09-09');
select si from all1 where 'a' > date('1996-09-09');
select si from all1 where 'a' >= date('1996-09-09');
select si from all1 where 'a' < date('1996-09-09');
select si from all1 where 'a' <= date('1996-09-09');
select si from all1 where 'a' <> date('1996-09-09');
select si from all1 where time('11:11:11') = 'a';
select si from all1 where time('11:11:11') > 'a';
select si from all1 where time('11:11:11') >= 'a';
select si from all1 where time('11:11:11') < 'a';
select si from all1 where time('11:11:11') <= 'a';
select si from all1 where time('11:11:11') <> 'a';
select si from all1 where 'a' = time('11:11:11');
select si from all1 where 'a' > time('11:11:11');
select si from all1 where 'a' >= time('11:11:11');
select si from all1 where 'a' < time('11:11:11');
select si from all1 where 'a' <= time('11:11:11');
select si from all1 where 'a' <> time('11:11:11');
select si from all1 where timestamp('1996-09-09 11:11:11.4' )= 'a';
select si from all1 where timestamp('1996-09-09 11:11:11.4' )> 'a';
select si from all1 where timestamp('1996-09-09 11:11:11.4' )>= 'a';
select si from all1 where timestamp('1996-09-09 11:11:11.4' )< 'a';
select si from all1 where timestamp('1996-09-09 11:11:11.4' )<= 'a';
select si from all1 where timestamp('1996-09-09 11:11:11.4' )<> 'a';
select si from all1 where 'a' = timestamp('1996-09-09 11:11:11.4');
select si from all1 where 'a' > timestamp('1996-09-09 11:11:11.4');
select si from all1 where 'a' >= timestamp('1996-09-09 11:11:11.4');
select si from all1 where 'a' < timestamp('1996-09-09 11:11:11.4');
select si from all1 where 'a' <= timestamp('1996-09-09 11:11:11.4');
select si from all1 where 'a' <> timestamp('1996-09-09 11:11:11.4');


-- create an all types table
drop table all1;
create table all1(si smallint, i int, li bigint, r real, 
				  dp double precision, dc decimal(5,1), num numeric(5,1),
				  b char for bit data, bv varchar(1) for bit data,
				  lbv long varchar for bit data,
				  dt date, tm time, tms timestamp,
				  c char(1), vc varchar(1), lvc long varchar);

-- populate table
insert into all1 values (2, 3, 4, 5.5, 6.6, 7.7, 8.8,
				  		 X'20', X'20', X'20',
				  		 date('1996-09-09'), time('12:12:12'), 
				  		 timestamp('1996-09-09 12:12:12.5'),
				  		 '1', '2', '333333333333333333');

-- tests for comparisons as qualifiers
select 1 from all1 where si = 'a';
select 1 from all1 where si = '1.1';
select 1 from all1 where si > 'a';
select 1 from all1 where si > '1.1';
select 1 from all1 where si >= 'a';
select 1 from all1 where si >= '1.1';
select 1 from all1 where si < 'a';
select 1 from all1 where si < '1.1';
select 1 from all1 where si <= 'a';
select 1 from all1 where si <= '1.1';
select 1 from all1 where si <> 'a';
select 1 from all1 where si <> '1.1';
select 1 from all1 where 'a' = si;
select 1 from all1 where '1.1' = si;
select 1 from all1 where 'a' > si;
select 1 from all1 where '1.1' > si;
select 1 from all1 where 'a' >= si;
select 1 from all1 where '1.1' >= si;
select 1 from all1 where 'a' < si;
select 1 from all1 where '1.1' < si;
select 1 from all1 where 'a' <= si;
select 1 from all1 where '1.1' <= si;
select 1 from all1 where 'a' <> si;
select 1 from all1 where '1.1' <> si;
select 1 from all1 where i = 'a';
select 1 from all1 where i = '1.1';
select 1 from all1 where i > 'a';
select 1 from all1 where i > '1.1';
select 1 from all1 where i >= 'a';
select 1 from all1 where i >= '1.1';
select 1 from all1 where i < 'a';
select 1 from all1 where i < '1.1';
select 1 from all1 where i <= 'a';
select 1 from all1 where i <= '1.1';
select 1 from all1 where i <> 'a';
select 1 from all1 where i <> '1.1';
select 1 from all1 where 'a' = i;
select 1 from all1 where '1.1' = i;
select 1 from all1 where 'a' > i;
select 1 from all1 where '1.1' > i;
select 1 from all1 where 'a' >= i;
select 1 from all1 where '1.1' >= i;
select 1 from all1 where 'a' < i;
select 1 from all1 where '1.1' < i;
select 1 from all1 where 'a' <= i;
select 1 from all1 where '1.1' <= i;
select 1 from all1 where 'a' <> i;
select 1 from all1 where '1.1' <> i;
select 1 from all1 where li = 'a';
select 1 from all1 where li = '1.1';
select 1 from all1 where li > 'a';
select 1 from all1 where li > '1.1';
select 1 from all1 where li >= 'a';
select 1 from all1 where li >= '1.1';
select 1 from all1 where li < 'a';
select 1 from all1 where li < '1.1';
select 1 from all1 where li <= 'a';
select 1 from all1 where li <= '1.1';
select 1 from all1 where li <> 'a';
select 1 from all1 where li <> '1.1';
select 1 from all1 where 'a' = li;
select 1 from all1 where '1.1' = li;
select 1 from all1 where 'a' > li;
select 1 from all1 where '1.1' > li;
select 1 from all1 where 'a' >= li;
select 1 from all1 where '1.1' >= li;
select 1 from all1 where 'a' < li;
select 1 from all1 where '1.1' < li;
select 1 from all1 where 'a' <= li;
select 1 from all1 where '1.1' <= li;
select 1 from all1 where 'a' <> li;
select 1 from all1 where '1.1' <> li;
select 1 from all1 where r = 'a';
select 1 from all1 where r > 'a';
select 1 from all1 where r >= 'a';
select 1 from all1 where r < 'a';
select 1 from all1 where r <= 'a';
select 1 from all1 where r <> 'a';
select 1 from all1 where 'a' = r;
select 1 from all1 where 'a' > r;
select 1 from all1 where 'a' >= r;
select 1 from all1 where 'a' < r;
select 1 from all1 where 'a' <= r;
select 1 from all1 where 'a' <> r;
select 1 from all1 where dp = 'a';
select 1 from all1 where dp > 'a';
select 1 from all1 where dp >= 'a';
select 1 from all1 where dp < 'a';
select 1 from all1 where dp <= 'a';
select 1 from all1 where dp <> 'a';
select 1 from all1 where 'a' = dp;
select 1 from all1 where 'a' > dp;
select 1 from all1 where 'a' >= dp;
select 1 from all1 where 'a' < dp;
select 1 from all1 where 'a' <= dp;
select 1 from all1 where 'a' <> dp;
select 1 from all1 where dc = 'a';
select 1 from all1 where dc > 'a';
select 1 from all1 where dc >= 'a';
select 1 from all1 where dc < 'a';
select 1 from all1 where dc <= 'a';
select 1 from all1 where dc <> 'a';
select 1 from all1 where 'a' = dc;
select 1 from all1 where 'a' > dc;
select 1 from all1 where 'a' >= dc;
select 1 from all1 where 'a' < dc;
select 1 from all1 where 'a' <= dc;
select 1 from all1 where 'a' <> dc;
select 1 from all1 where num = 'a';
select 1 from all1 where num > 'a';
select 1 from all1 where num >= 'a';
select 1 from all1 where num < 'a';
select 1 from all1 where num <= 'a';
select 1 from all1 where num <> 'a';
select 1 from all1 where 'a' = num;
select 1 from all1 where 'a' > num;
select 1 from all1 where 'a' >= num;
select 1 from all1 where 'a' < num;
select 1 from all1 where 'a' <= num;
select 1 from all1 where 'a' <> num;
select 1 from all1 where dt = 'a';
select 1 from all1 where dt > 'a';
select 1 from all1 where dt >= 'a';
select 1 from all1 where dt < 'a';
select 1 from all1 where dt <= 'a';
select 1 from all1 where dt <> 'a';
select 1 from all1 where 'a' = dt;
select 1 from all1 where 'a' > dt;
select 1 from all1 where 'a' >= dt;
select 1 from all1 where 'a' < dt;
select 1 from all1 where 'a' <= dt;
select 1 from all1 where 'a' <> dt;
select 1 from all1 where tm = 'a';
select 1 from all1 where tm > 'a';
select 1 from all1 where tm >= 'a';
select 1 from all1 where tm < 'a';
select 1 from all1 where tm <= 'a';
select 1 from all1 where tm <> 'a';
select 1 from all1 where 'a' = tm;
select 1 from all1 where 'a' > tm;
select 1 from all1 where 'a' >= tm;
select 1 from all1 where 'a' < tm;
select 1 from all1 where 'a' <= tm;
select 1 from all1 where 'a' <> tm;
select 1 from all1 where tms = 'a';
select 1 from all1 where tms > 'a';
select 1 from all1 where tms >= 'a';
select 1 from all1 where tms < 'a';
select 1 from all1 where tms <= 'a';
select 1 from all1 where tms <> 'a';
select 1 from all1 where 'a' = tms;
select 1 from all1 where 'a' > tms;
select 1 from all1 where 'a' >= tms;
select 1 from all1 where 'a' < tms;
select 1 from all1 where 'a' <= tms;
select 1 from all1 where 'a' <> tms;

autocommit off;
-- test start and stop positions for conversions to (var)char
create index all1_si on all1(si);
create index all1_i on all1(i);
create index all1_li on all1(li);
create index all1_r on all1(r);
create index all1_dp on all1(dp);
create index all1_dc on all1(dc);
create index all1_num on all1(num);
create index all1_b on all1(b);
create index all1_bv on all1(bv);
create index all1_lbv on all1(lbv);
create index all1_dt on all1(dt);
create index all1_tm on all1(tm);
create index all1_tms on all1(tms);

select 1 from all1 where si = 'a';
select 1 from all1 where si = '1.1';
select 1 from all1 where si > 'a';
select 1 from all1 where si > '1.1';
select 1 from all1 where si >= 'a';
select 1 from all1 where si >= '1.1';
select 1 from all1 where si < 'a';
select 1 from all1 where si < '1.1';
select 1 from all1 where si <= 'a';
select 1 from all1 where si <= '1.1';
select 1 from all1 where 'a' = si;
select 1 from all1 where '1.1' = si;
select 1 from all1 where 'a' > si;
select 1 from all1 where '1.1' > si;
select 1 from all1 where 'a' >= si;
select 1 from all1 where '1.1' >= si;
select 1 from all1 where 'a' < si;
select 1 from all1 where '1.1' < si;
select 1 from all1 where 'a' <= si;
select 1 from all1 where '1.1' <= si;
select 1 from all1 where i = 'a';
select 1 from all1 where i = '1.1';
select 1 from all1 where i > 'a';
select 1 from all1 where i > '1.1';
select 1 from all1 where i >= 'a';
select 1 from all1 where i >= '1.1';
select 1 from all1 where i < 'a';
select 1 from all1 where i < '1.1';
select 1 from all1 where i <= 'a';
select 1 from all1 where i <= '1.1';
select 1 from all1 where 'a' = i;
select 1 from all1 where '1.1' = i;
select 1 from all1 where 'a' > i;
select 1 from all1 where '1.1' > i;
select 1 from all1 where 'a' >= i;
select 1 from all1 where '1.1' >= i;
select 1 from all1 where 'a' < i;
select 1 from all1 where '1.1' < i;
select 1 from all1 where 'a' <= i;
select 1 from all1 where '1.1' <= i;
select 1 from all1 where li = 'a';
select 1 from all1 where li = '1.1';
select 1 from all1 where li > 'a';
select 1 from all1 where li > '1.1';
select 1 from all1 where li >= 'a';
select 1 from all1 where li >= '1.1';
select 1 from all1 where li < 'a';
select 1 from all1 where li < '1.1';
select 1 from all1 where li <= 'a';
select 1 from all1 where li <= '1.1';
select 1 from all1 where 'a' = li;
select 1 from all1 where '1.1' = li;
select 1 from all1 where 'a' > li;
select 1 from all1 where '1.1' > li;
select 1 from all1 where 'a' >= li;
select 1 from all1 where '1.1' >= li;
select 1 from all1 where 'a' < li;
select 1 from all1 where '1.1' < li;
select 1 from all1 where 'a' <= li;
select 1 from all1 where '1.1' <= li;
select 1 from all1 where r = 'a';
select 1 from all1 where r > 'a';
select 1 from all1 where r >= 'a';
select 1 from all1 where r < 'a';
select 1 from all1 where r <= 'a';
select 1 from all1 where 'a' = r;
select 1 from all1 where 'a' > r;
select 1 from all1 where 'a' >= r;
select 1 from all1 where 'a' < r;
select 1 from all1 where 'a' <= r;
select 1 from all1 where dp = 'a';
select 1 from all1 where dp > 'a';
select 1 from all1 where dp >= 'a';
select 1 from all1 where dp < 'a';
select 1 from all1 where dp <= 'a';
select 1 from all1 where 'a' = dp;
select 1 from all1 where 'a' > dp;
select 1 from all1 where 'a' >= dp;
select 1 from all1 where 'a' < dp;
select 1 from all1 where 'a' <= dp;
select 1 from all1 where dc = 'a';
select 1 from all1 where dc > 'a';
select 1 from all1 where dc >= 'a';
select 1 from all1 where dc < 'a';
select 1 from all1 where dc <= 'a';
select 1 from all1 where 'a' = dc;
select 1 from all1 where 'a' > dc;
select 1 from all1 where 'a' >= dc;
select 1 from all1 where 'a' < dc;
select 1 from all1 where 'a' <= dc;
select 1 from all1 where num = 'a';
select 1 from all1 where num > 'a';
select 1 from all1 where num >= 'a';
select 1 from all1 where num < 'a';
select 1 from all1 where num <= 'a';
select 1 from all1 where 'a' = num;
select 1 from all1 where 'a' > num;
select 1 from all1 where 'a' >= num;
select 1 from all1 where 'a' < num;
select 1 from all1 where 'a' <= num;
select 1 from all1 where dt = 'a';
select 1 from all1 where dt > 'a';
select 1 from all1 where dt >= 'a';
select 1 from all1 where dt < 'a';
select 1 from all1 where dt <= 'a';
select 1 from all1 where 'a' = dt;
select 1 from all1 where 'a' > dt;
select 1 from all1 where 'a' >= dt;
select 1 from all1 where 'a' < dt;
select 1 from all1 where 'a' <= dt;
select 1 from all1 where tm = 'a';
select 1 from all1 where tm > 'a';
select 1 from all1 where tm >= 'a';
select 1 from all1 where tm < 'a';
select 1 from all1 where tm <= 'a';
select 1 from all1 where 'a' = tm;
select 1 from all1 where 'a' > tm;
select 1 from all1 where 'a' >= tm;
select 1 from all1 where 'a' < tm;
select 1 from all1 where 'a' <= tm;
select 1 from all1 where tms = 'a';
select 1 from all1 where tms > 'a';
select 1 from all1 where tms >= 'a';
select 1 from all1 where tms < 'a';
select 1 from all1 where tms <= 'a';
select 1 from all1 where 'a' = tms;
select 1 from all1 where 'a' > tms;
select 1 from all1 where 'a' >= tms;
select 1 from all1 where 'a' < tms;
select 1 from all1 where 'a' <= tms;


rollback;

-- insert tests
insert into all1(si) values 'a';
insert into all1(si) values '1.1';
insert into all1(i) values 'a';
insert into all1(i) values '1.1';
insert into all1(li) values 'a';
insert into all1(li) values '1.1';
insert into all1(r) values 'a';
insert into all1(dp) values 'a';
insert into all1(dc) values 'a';
insert into all1(num) values 'a';
insert into all1(dt) values 'a';
insert into all1(tm) values 'a';
insert into all1(tms) values 'a';

-- update tests
update all1 set si = 'a';
update all1 set si = '1.1';
update all1 set i = 'a';
update all1 set i = '1.1';
update all1 set li = 'a';
update all1 set li = '1.1';
update all1 set r = 'a';
update all1 set dp = 'a';
update all1 set dc = 'a';
update all1 set num = 'a';
update all1 set dt = 'a';
update all1 set tm = 'a';
update all1 set tms = 'a';

rollback;

-- arithmetic
-- arithmetic on 2 non-numeric strings
values '1' + '2';

-- non-numeric strings
values 1 + 'a';
values 1 + '1a';
values 'a' + 1;
values '1a' + 1;

-- numeric string of wrong type
values 1 + '1.1';
values '1.1' + 1;

-- extract
-- non-date strings
values year( '1');
values month( '1');
values day( '1');
values hour( '1');
values minute( '1');
values second( '1');

-- string does not match default
-- date for year/month/day
-- time for hour/minute/second
values year( '1996-09-10 10:11:12.5');
values year( '10:11:12.5');
values month( '1996-09-10 10:11:12.5');
values month( '10:11:12.5');
values day( '1996-09-10 10:11:12.5');
values day( '10:11:12.5');
values hour( '1996-09-10 10:11:12.5');
values hour( '1996-09-10');
values minute( '1996-09-10 10:11:12.5');
values minute( '1996-09-10');
values second( '1996-09-10 10:11:12.5');
values second( '1996-09-10');

-- sum and avg not supported on string types
select sum('1') from all1;
select avg('1') from all1;

-- joins
select * from all1 a, all1 b 
where a.si = b.c;
select * from all1 a, all1 b 
where a.si = b.vc;
select * from all1 a, all1 b 
where a.si = b.lvc;
select * from all1 a, all1 b 
where b.si = a.c;
select * from all1 a, all1 b 
where b.si = a.vc;
select * from all1 a, all1 b 
where b.si = a.lvc;
select * from all1 a, all1 b 
where a.dt = b.c;
select * from all1 a, all1 b 
where a.dt = b.vc;
select * from all1 a, all1 b 
where a.dt = b.lvc;
select * from all1 a, all1 b 
where b.dt = a.c;
select * from all1 a, all1 b 
where b.dt = a.vc;
select * from all1 a, all1 b 
where b.dt = a.lvc;

-- clean up 
drop table all1;
commit;

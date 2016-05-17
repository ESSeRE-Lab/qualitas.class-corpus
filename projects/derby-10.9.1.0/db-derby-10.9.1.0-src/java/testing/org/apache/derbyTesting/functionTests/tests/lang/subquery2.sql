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
--
-- subquery tests (ANY and ALL subqueries)
--
CREATE FUNCTION ConsistencyChecker() RETURNS VARCHAR(128)
EXTERNAL NAME 'org.apache.derbyTesting.functionTests.util.T_ConsistencyChecker.runConsistencyChecker'
LANGUAGE JAVA PARAMETER STYLE JAVA;
autocommit off;

autocommit off;

-- create the all type tables
create table s (i int, s smallint, c char(30), vc char(30), b bigint);
create table t (i int, s smallint, c char(30), vc char(30), b bigint);
create table tt (ii int, ss smallint, cc char(30), vcvc char(30), b bigint);
create table ttt (iii int, sss smallint, ccc char(30), vcvcvc char(30));

-- populate the tables
insert into s values (null, null, null, null, null);
insert into s values (0, 0, '0', '0', 0);
insert into s values (1, 1, '1', '1', 1);

insert into t values (null, null, null, null, null);
insert into t values (0, 0, '0', '0', 0);
insert into t values (1, 1, '1', '1', 1);
insert into t values (1, 1, '1', '1', 1);
insert into t values (2, 2, '2', '2', 1);

insert into tt values (null, null, null, null, null);
insert into tt values (0, 0, '0', '0', 0);
insert into tt values (1, 1, '1', '1', 1);
insert into tt values (1, 1, '1', '1', 1);
insert into tt values (2, 2, '2', '2', 1);

insert into ttt values (null, null, null, null);
insert into ttt values (11, 11, '11', '11');
insert into ttt values (11, 11, '11', '11');
insert into ttt values (22, 22, '22', '22');

-- ANY subqueries

-- negative tests
-- select * subquery
select * from s where s = ANY (select * from s);
-- incompatable types
select * from s where s >= ANY (select b from t);
-- invalid operator
select * from s where s * ANY (select c from t);
-- ? in select list of subquery
select * from s where s = ANY (select ? from s);

-- positive tests

-- constants on left side of subquery
select * from s where 1 = ANY (select s from t);
select * from s where -1 = ANY (select i from t);
select * from s where '1' = ANY (select vc from t);
select * from s where 0 = ANY (select b from t);
select * from s where 1 <> ANY (select s from t);
select * from s where -1 <> ANY (select i from t);
select * from s where '1' <> ANY (select vc from t);
select * from s where 0 <> ANY (select b from t);
select * from s where 1 >= ANY (select s from t);
select * from s where -1 >= ANY (select i from t);
select * from s where '1' >= ANY (select vc from t);
select * from s where 0 >= ANY (select b from t);
select * from s where 1 > ANY (select s from t);
select * from s where -1 > ANY (select i from t);
select * from s where '1' > ANY (select vc from t);
select * from s where 0 > ANY (select b from t);
select * from s where 1 <= ANY (select s from t);
select * from s where -1 <= ANY (select i from t);
select * from s where '1' <= ANY (select vc from t);
select * from s where 0 <= ANY (select b from t);
select * from s where 1 < ANY (select s from t);
select * from s where -1 < ANY (select i from t);
select * from s where '1' < ANY (select vc from t);
select * from s where 0 < ANY (select b from t);

-- Try a ? parameter on the LHS of a subquery.
prepare subq1 as 'select * from s where ? = ANY (select s from t)';
execute subq1 using 'values (1)';
remove subq1;

-- constants in subquery select list
select * from s where i = ANY (select 1 from t);
select * from s where i = ANY (select -1 from t);
select * from s where c = ANY (select '1' from t);
select * from s where b = ANY (select 1 from t);
select * from s where i <> ANY (select 1 from t);
select * from s where i <> ANY (select -1 from t);
select * from s where c <> ANY (select '1' from t);
select * from s where b <> ANY (select 1 from t);
select * from s where i >= ANY (select 1 from t);
select * from s where i >= ANY (select -1 from t);
select * from s where c >= ANY (select '1' from t);
select * from s where b >= ANY (select 1 from t);
select * from s where i > ANY (select 1 from t);
select * from s where i > ANY (select -1 from t);
select * from s where c > ANY (select '1' from t);
select * from s where b > ANY (select 1 from t);
select * from s where i <= ANY (select 1 from t);
select * from s where i <= ANY (select -1 from t);
select * from s where c <= ANY (select '1' from t);
select * from s where b <= ANY (select 1 from t);
select * from s where i < ANY (select 1 from t);
select * from s where i < ANY (select -1 from t);
select * from s where c < ANY (select '1' from t);
select * from s where b < ANY (select 1 from t);

-- constants on both sides
select * from s where 1 = ANY (select 0 from t);
select * from s where 0 = ANY (select 0 from t);
select * from s where 1 <> ANY (select 0 from t);
select * from s where 0 <> ANY (select 0 from t);
select * from s where 1 >= ANY (select 0 from t);
select * from s where 0 >= ANY (select 0 from t);
select * from s where 1 > ANY (select 0 from t);
select * from s where 0 > ANY (select 0 from t);
select * from s where 1 <= ANY (select 0 from t);
select * from s where 0 <= ANY (select 0 from t);
select * from s where 1 < ANY (select 0 from t);
select * from s where 0 < ANY (select 0 from t);

-- compatable types
select * from s where c = ANY (select vc from t);
select * from s where vc = ANY (select c from t);
select * from s where i = ANY (select s from t);
select * from s where s = ANY (select i from t);
select * from s where c <> ANY (select vc from t);
select * from s where vc <> ANY (select c from t);
select * from s where i <> ANY (select s from t);
select * from s where s <> ANY (select i from t);
select * from s where c >= ANY (select vc from t);
select * from s where vc >= ANY (select c from t);
select * from s where i >= ANY (select s from t);
select * from s where s >= ANY (select i from t);
select * from s where c > ANY (select vc from t);
select * from s where vc > ANY (select c from t);
select * from s where i > ANY (select s from t);
select * from s where s > ANY (select i from t);
select * from s where c <= ANY (select vc from t);
select * from s where vc <= ANY (select c from t);
select * from s where i <= ANY (select s from t);
select * from s where s <= ANY (select i from t);
select * from s where c < ANY (select vc from t);
select * from s where vc < ANY (select c from t);
select * from s where i < ANY (select s from t);
select * from s where s < ANY (select i from t);

-- empty subquery result set
select * from s where i = ANY (select i from t where 1 = 0);
select * from s where i <> ANY (select i from t where 1 = 0);
select * from s where i >= ANY (select i from t where 1 = 0);
select * from s where i > ANY (select i from t where 1 = 0);
select * from s where i <= ANY (select i from t where 1 = 0);
select * from s where i < ANY (select i from t where 1 = 0);

-- subquery under an or
select i from s where i = -1 or i = ANY (select i from t);
select i from s where i = 0 or i = ANY (select i from t where i = -1);
select i from s where i = -1 or i = ANY (select i from t where i = -1 or i = 1);
select i from s where i = -1 or i <> ANY (select i from t);
select i from s where i = 0 or i >= ANY (select i from t where i = -1);
select i from s where i = -1 or i < ANY (select i from t where i = -1 or i = 1);
select i from s where i = -1 or i >= ANY (select i from t);
select i from s where i = 0 or i > ANY (select i from t where i = -1);
select i from s where i = -1 or i <> ANY (select i from t where i = -1 or i = 1);

-- correlated subqueries
select * from s where i > ANY (select i from t where s.s > t.s);
select * from s where i >= ANY (select i from t where s.s >= t.s);
select * from s where i < ANY (select i from t where s.s < t.s);
select * from s where i <= ANY (select i from t where s.s <= t.s);
select * from s where i = ANY (select i from t where s.s = t.s);
select * from s where i <> ANY (select i from t where s.s <> t.s);


-- ALL/NOT IN and NOTs
-- create tables
create table s_3rows (i int);
create table t_1 (i int);
create table u_null (i int);
create table v_empty (i int);
create table w_2 (i int);

-- populate tables
insert into s_3rows values(NULL);
insert into s_3rows values(1);
insert into s_3rows values(2);

insert into u_null values(NULL);

insert into t_1 values(1);

insert into w_2 values(2);

-- test ALLs
select * from s_3rows where s_3rows.i not in (select i from t_1);
select * from s_3rows where s_3rows.i <> ALL (select i from t_1);
select * from s_3rows where s_3rows.i >= ALL (select i from t_1);
select * from s_3rows where s_3rows.i > ALL (select i from t_1);
select * from s_3rows where s_3rows.i <= ALL (select i from t_1);
select * from s_3rows where s_3rows.i < ALL (select i from t_1);
select * from s_3rows where s_3rows.i = ALL (select i from t_1);

select * from s_3rows where s_3rows.i not in (select i from u_null);
select * from s_3rows where s_3rows.i <> ALL (select i from u_null);
select * from s_3rows where s_3rows.i >= ALL (select i from u_null);
select * from s_3rows where s_3rows.i > ALL (select i from u_null);
select * from s_3rows where s_3rows.i <= ALL (select i from u_null);
select * from s_3rows where s_3rows.i < ALL (select i from u_null);
select * from s_3rows where s_3rows.i = ALL (select i from u_null);

select * from s_3rows where s_3rows.i not in (select i from v_empty);
select * from s_3rows where s_3rows.i <> ALL (select i from v_empty);
select * from s_3rows where s_3rows.i >= ALL (select i from v_empty);
select * from s_3rows where s_3rows.i > ALL (select i from v_empty);
select * from s_3rows where s_3rows.i <= ALL (select i from v_empty);
select * from s_3rows where s_3rows.i < ALL (select i from v_empty);
select * from s_3rows where s_3rows.i = ALL (select i from v_empty);

select * from s_3rows where s_3rows.i not in (select i from w_2);
select * from s_3rows where s_3rows.i <> ALL (select i from w_2);
select * from s_3rows where s_3rows.i >= ALL (select i from w_2);
select * from s_3rows where s_3rows.i > ALL (select i from w_2);
select * from s_3rows where s_3rows.i <= ALL (select i from w_2);
select * from s_3rows where s_3rows.i < ALL (select i from w_2);
select * from s_3rows where s_3rows.i = ALL (select i from w_2);

select * from w_2 where w_2.i = ALL (select i from w_2);

-- NOT = ANY <=> <> ALL
select * from s_3rows where NOT s_3rows.i = ANY (select i from w_2);
select * from s_3rows where s_3rows.i <> ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i = ANY (select i from v_empty);
select * from s_3rows where s_3rows.i <> ALL (select i from v_empty);
-- NOT <> ANY <=> = ALL
select * from s_3rows where NOT s_3rows.i <> ANY (select i from w_2);
select * from s_3rows where s_3rows.i = ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i <> ANY (select i from v_empty);
select * from s_3rows where s_3rows.i = ALL (select i from v_empty);
-- NOT >= ANY <=> < ALL
select * from s_3rows where NOT s_3rows.i >= ANY (select i from w_2);
select * from s_3rows where s_3rows.i < ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i >= ANY (select i from v_empty);
select * from s_3rows where s_3rows.i < ALL (select i from v_empty);
-- NOT > ANY <=> <= ALL
select * from s_3rows where NOT s_3rows.i > ANY (select i from w_2);
select * from s_3rows where s_3rows.i <= ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i > ANY (select i from v_empty);
select * from s_3rows where s_3rows.i <= ALL (select i from v_empty);
-- NOT <= ANY <=> > ALL
select * from s_3rows where NOT s_3rows.i <= ANY (select i from w_2);
select * from s_3rows where s_3rows.i > ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i <= ANY (select i from v_empty);
select * from s_3rows where s_3rows.i > ALL (select i from v_empty);
-- NOT < ANY <=> >= ALL
select * from s_3rows where NOT s_3rows.i < ANY (select i from w_2);
select * from s_3rows where s_3rows.i >= ALL (select i from w_2);
select * from s_3rows where NOT s_3rows.i < ANY (select i from v_empty);
select * from s_3rows where s_3rows.i >= ALL (select i from v_empty);

-- NOT = ALL <=> <> ANY
select * from s_3rows where NOT s_3rows.i = ALL (select i from w_2);
select * from s_3rows where s_3rows.i <> ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i = ALL (select i from v_empty);
select * from s_3rows where s_3rows.i <> ANY (select i from v_empty);
-- NOT <> ALL <=> = ANY
select * from s_3rows where NOT s_3rows.i <> ALL (select i from w_2);
select * from s_3rows where s_3rows.i = ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i <> ALL (select i from v_empty);
select * from s_3rows where s_3rows.i = ANY (select i from v_empty);
-- NOT >= ALL <=> < ANY
select * from s_3rows where NOT s_3rows.i >= ALL (select i from w_2);
select * from s_3rows where s_3rows.i < ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i >= ALL (select i from v_empty);
select * from s_3rows where s_3rows.i < ANY (select i from v_empty);
-- NOT > ALL <=> <= ANY
select * from s_3rows where NOT s_3rows.i > ALL (select i from w_2);
select * from s_3rows where s_3rows.i <= ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i > ALL (select i from v_empty);
select * from s_3rows where s_3rows.i <= ANY (select i from v_empty);
-- NOT <= ALL <=> > ANY
select * from s_3rows where NOT s_3rows.i <= ALL (select i from w_2);
select * from s_3rows where s_3rows.i > ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i <= ALL (select i from v_empty);
select * from s_3rows where s_3rows.i > ANY (select i from v_empty);
-- NOT < ALL <=> >= ANY
select * from s_3rows where NOT s_3rows.i < ALL (select i from w_2);
select * from s_3rows where s_3rows.i >= ANY (select i from w_2);
select * from s_3rows where NOT s_3rows.i < ALL (select i from v_empty);
select * from s_3rows where s_3rows.i >= ANY (select i from v_empty);

-- test skipping of generating is null predicates for non-nullable columns
create table t1 (c1 int not null, c2 int);
create table t2 (c1 int not null, c2 int);
insert into t1 values(1, 2);
insert into t2 values(0, 3);
select * from t1 where c1 not in (select c2 from t2);
select * from t1 where c2 not in (select c1 from t2);
select * from t1 where c1 not in (select c1 from t2);
drop table t1;
drop table t2;

-- update
create table u (i int, s smallint, c char(30), vc char(30), b bigint);
insert into u select * from s;
select * from u;

update u set b = exists (select * from t)
where vc < ANY (select vc from s);
select * from u;

delete from u;
insert into u select * from s;

-- delete
delete from u where c < ANY (select c from t);
select * from u;

-- do consistency check on scans, etc.
values ConsistencyChecker();

-- reset autocommit
autocommit on;

-- drop the tables
drop table s;
drop table t;
drop table tt;
drop table ttt;
drop table u;
drop table s_3rows;
drop table t_1;
drop table u_null;
drop table v_empty;
drop table w_2;

-- DERBY-634: Dynamic subquery materialization can cause stack overflow

create table parentT ( i int, j int, k int);
create table childT ( i int, j int, k int);

-- Load some data
insert into parentT values (1,1,1), (2,2,2), (3,3,3), (4,4,4);
insert into parentT select i+4, j+4, k+4 from parentT;
insert into parentT select i+8, j+8, k+8 from parentT;
insert into parentT select i+16, j+16, k+16 from parentT;
insert into parentT select i+32, j+32, k+32 from parentT;
insert into parentT select i+64, j+64, k+64 from parentT;
insert into parentT select i+128, j+128, k+128 from parentT;
insert into parentT select i+256, j+256, k+256 from parentT;
insert into parentT select i+512, j+512, k+512 from parentT;
insert into parentT select i+1024, j+1024, k+1024 from parentT;
insert into parentT select i+2048, j+2048, k+2048 from parentT;
insert into parentT select i+4096, j+4096, k+4096 from parentT;
insert into parentT select i+8192, j+8192, k+8192 from parentT;

-- Try with three different sizes of subquery results.
update parentT set j = j /10;
update parentT set k = k /100;
create unique index parentIdx on parentT(i);

insert into childT select * from parentT;

select count(*) from parentT where i < 10 and i not in (select i from childT);

select count(*) from parentT where i< 10 and exists (select i from childT where childT.i=parentT.i);

select count(*) from parentT where i< 10 and j not in (select distinct j from childT);

select count(*) from parentT where i< 10 and exists (select distinct j from childT where childT.j=parentT.j);

select count(*) from parentT where i< 10 and k not in (select distinct k from childT);

select count(*) from parentT where i< 10 and exists (select distinct k from childT where childT.k=parentT.k);

drop table childT;
drop table parentT;


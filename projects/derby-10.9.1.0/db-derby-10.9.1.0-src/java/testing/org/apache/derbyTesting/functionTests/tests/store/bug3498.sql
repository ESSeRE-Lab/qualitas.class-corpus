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
-- Track 3498
--
-- test case for a post commit queued during an nested user transaction.  This
-- is ugly but works.  This case happens if the autoincrement field expansion
-- when the first insert causes the row in the system catalog to become a long
-- row.  Make this happen by just creating A LOT of tables and making them
-- all expand.  One of them will hit the problem, this test reproducibly
-- showed the problem which was fixed by p4 change 16867.

-- separate test because this counts heavily on the layout on the pages,
-- of the system catalogs to make the right circumstance come about.

autocommit off;
-- create a test table to make sure rollback works right.
create table testabort (a int);

-- create a lot of tables so that there is at least one page in SYSTABLES
-- with just these rows.  The subsequent INSERT will expand every row, making
-- one of them move part of the row off the page.
create table foo000000 (keycol int, a000 int generated always as identity);
create table foo000001 (keycol int, a000 int generated always as identity);
create table foo000002 (keycol int, a000 int generated always as identity);
create table foo000003 (keycol int, a000 int generated always as identity);
create table foo000004 (keycol int, a000 int generated always as identity);
create table foo000005 (keycol int, a000 int generated always as identity);
create table foo000006 (keycol int, a000 int generated always as identity);
create table foo000007 (keycol int, a000 int generated always as identity);
create table foo000008 (keycol int, a000 int generated always as identity);
create table foo000009 (keycol int, a000 int generated always as identity);
create table foo000010 (keycol int, a000 int generated always as identity);
create table foo000011 (keycol int, a000 int generated always as identity);
create table foo000012 (keycol int, a000 int generated always as identity);
create table foo000013 (keycol int, a000 int generated always as identity);
create table foo000014 (keycol int, a000 int generated always as identity);
create table foo000015 (keycol int, a000 int generated always as identity);
create table foo000016 (keycol int, a000 int generated always as identity);
create table foo000017 (keycol int, a000 int generated always as identity);
create table foo000018 (keycol int, a000 int generated always as identity);
create table foo000019 (keycol int, a000 int generated always as identity);
create table foo000020 (keycol int, a000 int generated always as identity);
create table foo000021 (keycol int, a000 int generated always as identity);
create table foo000022 (keycol int, a000 int generated always as identity);
create table foo000023 (keycol int, a000 int generated always as identity);
create table foo000024 (keycol int, a000 int generated always as identity);
create table foo000025 (keycol int, a000 int generated always as identity);
create table foo000026 (keycol int, a000 int generated always as identity);
create table foo000027 (keycol int, a000 int generated always as identity);
create table foo000028 (keycol int, a000 int generated always as identity);
create table foo000029 (keycol int, a000 int generated always as identity);
create table foo000030 (keycol int, a00001111 int generated always as identity);
create table foo000031 (keycol int, a000 int generated always as identity);
create table foo000032 (keycol int, a000 int generated always as identity);
create table foo000033 (keycol int, a000 int generated always as identity);
create table foo000034 (keycol int, a000 int generated always as identity);
create table foo000035 (keycol int, a000 int generated always as identity);
create table foo000036 (keycol int, a000 int generated always as identity);
create table foo000037 (keycol int, a000 int generated always as identity);
create table foo000038 (keycol int, a000 int generated always as identity);
create table foo000039 (keycol int, a000 int generated always as identity);
create table foo000040 (keycol int, a000 int generated always as identity);
create table foo000041 (keycol int, a000 int generated always as identity);
create table foo000042 (keycol int, a000 int generated always as identity);
create table foo000043 (keycol int, a000 int generated always as identity);
create table foo000044 (keycol int, a000 int generated always as identity);
create table foo000045 (keycol int, a000 int generated always as identity);
create table foo000046 (keycol int, a000 int generated always as identity);
create table foo000047 (keycol int, a000 int generated always as identity);
create table foo000048 (keycol int, a000 int generated always as identity);
create table foo000049 (keycol int, a000 int generated always as identity);
create table foo000050 (keycol int, a000 int generated always as identity);
create table foo000051 (keycol int, a000 int generated always as identity);
create table foo000052 (keycol int, a000 int generated always as identity);
create table foo000053 (keycol int, a000 int generated always as identity);
create table foo000054 (keycol int, a000 int generated always as identity);
create table foo000055 (keycol int, a000 int generated always as identity);
create table foo000056 (keycol int, a000 int generated always as identity);
create table foo000057 (keycol int, a000 int generated always as identity);
create table foo000058 (keycol int, a000 int generated always as identity);
create table foo000059 (keycol int, a000 int generated always as identity);
create table foo000060 (keycol int, a000 int generated always as identity);
create table foo000061 (keycol int, a000 int generated always as identity);
create table foo000062 (keycol int, a000 int generated always as identity);
create table foo000063 (keycol int, a000 int generated always as identity);
create table foo000064 (keycol int, a000 int generated always as identity);
create table foo000065 (keycol int, a000 int generated always as identity);
create table foo000066 (keycol int, a000 int generated always as identity);
create table foo000067 (keycol int, a000 int generated always as identity);
create table foo000068 (keycol int, a000 int generated always as identity);
create table foo000069 (keycol int, a000 int generated always as identity);
commit;

insert into testabort values (1);
insert into foo000000 (keycol) values (1);
insert into foo000001 (keycol) values (1);
insert into foo000002 (keycol) values (1);
insert into foo000003 (keycol) values (1);
insert into foo000004 (keycol) values (1);
insert into foo000005 (keycol) values (1);
insert into foo000006 (keycol) values (1);
insert into foo000007 (keycol) values (1);
insert into foo000008 (keycol) values (1);
insert into foo000009 (keycol) values (1);
insert into foo000010 (keycol) values (1);
insert into foo000011 (keycol) values (1);
insert into foo000012 (keycol) values (1);
insert into foo000013 (keycol) values (1);
insert into foo000014 (keycol) values (1);
insert into foo000015 (keycol) values (1);
insert into foo000016 (keycol) values (1);
insert into foo000017 (keycol) values (1);
insert into foo000018 (keycol) values (1);
insert into foo000019 (keycol) values (1);
insert into foo000020 (keycol) values (1);
insert into foo000021 (keycol) values (1);
insert into foo000022 (keycol) values (1);
insert into foo000023 (keycol) values (1);
insert into foo000024 (keycol) values (1);
insert into foo000025 (keycol) values (1);
insert into foo000026 (keycol) values (1);
insert into foo000027 (keycol) values (1);
insert into foo000028 (keycol) values (1);
insert into foo000029 (keycol) values (1);
insert into foo000030 (keycol) values (1);
insert into foo000031 (keycol) values (1);
insert into foo000032 (keycol) values (1);
insert into foo000033 (keycol) values (1);
insert into foo000034 (keycol) values (1);
insert into foo000035 (keycol) values (1);
insert into foo000036 (keycol) values (1);
insert into foo000037 (keycol) values (1);
insert into foo000038 (keycol) values (1);
insert into foo000039 (keycol) values (1);
insert into foo000040 (keycol) values (1);
insert into foo000041 (keycol) values (1);
insert into foo000042 (keycol) values (1);
insert into foo000043 (keycol) values (1);
insert into foo000044 (keycol) values (1);
insert into foo000045 (keycol) values (1);
insert into foo000046 (keycol) values (1);
insert into foo000047 (keycol) values (1);
insert into foo000048 (keycol) values (1);
insert into foo000049 (keycol) values (1);
insert into foo000050 (keycol) values (1);
insert into foo000051 (keycol) values (1);
insert into foo000052 (keycol) values (1);
insert into foo000053 (keycol) values (1);
insert into foo000054 (keycol) values (1);
insert into foo000055 (keycol) values (1);
insert into foo000056 (keycol) values (1);
insert into foo000057 (keycol) values (1);
insert into foo000058 (keycol) values (1);
insert into foo000059 (keycol) values (1);
insert into foo000060 (keycol) values (1);
insert into foo000061 (keycol) values (1);
insert into foo000062 (keycol) values (1);
insert into foo000063 (keycol) values (1);
insert into foo000064 (keycol) values (1);
insert into foo000065 (keycol) values (1);
insert into foo000066 (keycol) values (1);
insert into foo000067 (keycol) values (1);
insert into foo000068 (keycol) values (1);
insert into foo000069 (keycol) values (1);

rollback;

select * from testabort;

commit;

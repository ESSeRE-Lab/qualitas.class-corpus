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
drop table localtab;
create table localtab(	ndec decimal(5,3), 
							ndatec date , 
							ntimec time , 
							ncharc varchar(500));
insert into localtab values(12.345, date('2000-05-25'),'15:30:15','���٥�ȥ��顼�बͭ���Ǥ���');
insert into localtab values(32.432, date('2000-05-18'),'15:32:10','DR:DRAUTO��0 (Off)�Ǥ���');
insert into localtab values(54.846, date('2000-01-28'),'15:32:28','INFORMIX-OnLine����������졢�ǥ������ν��������λ���ޤ�����');
insert into localtab values(39.003, date('2000-11-11'),'15:32:29','�����å��ݥ���Ȥ���λ���ޤ���:��³���֤� 0�äǤ���');
insert into localtab values(79.406, date('2000-06-05'),'15:32:29','���٤Ƥ�DB�ΰ�Υǡ��������åפϸ��ߥ��դˤʤäƤ��ޤ���');
insert into localtab values(94.999, date('2000-07-30'),'15:32:30','On-Line�⡼��');
insert into localtab values(18.849, date('2000-10-22'),'15:32:31','sysmaster�ǡ����١����������Ǥ�...');
insert into localtab values(35.444, date('2000-02-29'),'15:33:22','������ 1����λ���ޤ�����');
insert into localtab values(84.391, date('2000-01-21'),'15:33:23','�꥿���󥳡��� 1���ᤷ�ƥץ�������λ���ޤ���:/bin/sh /bin/sh -c /work1/MOSES_7.22.UC1A5_27/sqldist/etc/log_full.sh 2 23 ������ 1����λ���ޤ����� ����');
insert into localtab values(56.664, date('2000-04-16'),'15:33:40','������ 2����λ���ޤ�����');
insert into localtab values(22.393, date('2000-03-01'),'15:33;42','�꥿���󥳡��� 1���ᤷ�ƥץ�������λ���ޤ���:/bin/sh /bin/sh -c /work1/MOSES_7.22.UC1A5_27/sqldist/etc/log_full.sh 2 23 ������ 2����λ���ޤ����� ����');
insert into localtab values(90.007, date('2000-11-27'),'15:33:43','�����å��ݥ���Ȥ���λ���ޤ���:��³���֤� 2�äǤ���');
insert into localtab values(30.496, date('2000-04-03'),'15:34:29','������ 3����λ���ޤ�����');
insert into localtab values(66.295, date('2000-10-15'),'15:34:30','�꥿���󥳡��� 1���ᤷ�ƥץ�������λ���ޤ���:/bin/sh /bin/sh -c /work1/MOSES_7.22.UC1A5_27/sqldist/etc/log_full.sh 2 23 ������ 3����λ���ޤ����� ����');
insert into localtab values(54.332, date('2000-09-01'),'15:35:35','sysmaster�ǡ����١����κ����ϴ�λ���ޤ�����');
insert into localtab values(11.105, date('2000-07-09'),'15:39:10','�����å��ݥ���Ȥ���λ���ޤ���:��³���֤� 8�äǤ���');

-- display in non localized format
select * from localtab;

-- display in localized format
LOCALIZEDDISPLAY ON;
select * from localtab;

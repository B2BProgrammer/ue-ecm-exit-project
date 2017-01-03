-- Please run this statement as a "Script" in SQL Developer.
-- Creates a Table -  CONDENASTTEMPLATESECUSRGRP

DECLARE 
 -- Used to store the return count of Tables exists or not
  table_count number;
  
  -- Statement to create a TB - CONDENASTTEMPLATESECUSRGRP
  sql_create varchar2(1000) := 'CREATE TABLE CONDENASTTEMPLATESECUSRGRP
                                (	TEMPLATE_NAME VARCHAR2(128 CHAR) NOT NULL ENABLE, 
                                  OWNING_ORGANIZATION_NAME VARCHAR2(800 CHAR) NOT NULL ENABLE, 
                                  USER_GROUP_NAME VARCHAR2(1024 CHAR) NOT NULL ENABLE, 
                                  PERMISSION_GROUP VARCHAR2(300 CHAR) NOT NULL ENABLE )  ';
              
  
 
BEGIN
  -- To find the wheather the TB - CONDENASTTEMPLATESECUSRGRP exists or not
  select count(*) into table_count from user_tables where table_name='CONDENASTTEMPLATESECUSRGRP';
  -- Table_Count is Zero - means table is not available therefore create a new TB
  IF table_count=0 THEN
    EXECUTE IMMEDIATE sql_create;
  END IF;
END;

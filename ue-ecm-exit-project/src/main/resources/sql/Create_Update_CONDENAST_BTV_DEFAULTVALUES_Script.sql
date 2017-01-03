-- Please run this statement as a "Script" in SQL Developer.
-- Creates a Table -  CONDENAST_BTV_DEFAULTVALUES

DECLARE 
 -- Used to store the return count of Tables exists or not
  table_count number;
  
  -- Statement to create a TB - CONDENAST_BTV_DEFAULTVALUES
  sql_create varchar2(1000) := 'CREATE TABLE CONDENAST_BTV_DEFAULTVALUES 
                              ( TEMPLATE_NAME VARCHAR2(128 CHAR) NOT NULL ENABLE, 
                                OWNING_ORGANIZATION_NAME VARCHAR2(800 CHAR) NOT NULL ENABLE, 
                                TERM_INTERNAL_NAME VARCHAR2(60 CHAR) NOT NULL ENABLE, 
                                DEFAULT_DATE_TERM_VALUE DATE, 
                                DEFAULT_NUMERIC_TERM_VALUE NUMBER(32,10), 
                                DEFAULT_STRING_TERM_VALUE VARCHAR2(2000 CHAR), 
                                MASTER_LOV_TERM VARCHAR2(60 CHAR))';
              
  
 
BEGIN
  -- To find the wheather the TB - CONDENAST_BTV_DEFAULTVALUES exists or not
  select count(*) into table_count from user_tables where table_name='CONDENAST_BTV_DEFAULTVALUES';
  -- Table_Count is Zero - means table is not available therefore create a new TB
  IF table_count=0 THEN
    EXECUTE IMMEDIATE sql_create;
  END IF;
END;

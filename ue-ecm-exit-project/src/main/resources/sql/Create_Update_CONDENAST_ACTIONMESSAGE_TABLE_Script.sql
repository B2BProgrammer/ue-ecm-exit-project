-- Please run this statement as a "Script" in SQL Developer.
-- Creates a Table -  CONDENAST_ACTIONMESSAGE_TABLE

DECLARE 
 -- Used to store the return count of Tables exists or not
  table_count number;
  
  -- Statement to create a TB - CONDENAST_ACTIONMESSAGE_TABLE
  sql_create varchar2(1000) := 'CREATE TABLE CONDENAST_ACTIONMESSAGE_TABLE
                               (CODE VARCHAR2(20 CHAR) NOT NULL ENABLE, 
                                CONTRACT_NUMBER NUMBER(10,0) NOT NULL ENABLE,
                                WIZARD_ID VARCHAR2(32 CHAR), 
                                CONTRACT_OWNER VARCHAR2(1024 CHAR) NOT NULL ENABLE, 
                              	MESSAGE VARCHAR2(1024 CHAR) NOT NULL ENABLE, 
                                TIMESTAMP DATE NOT NULL ENABLE) ';
              
  
 
BEGIN
  -- To find the wheather the TB - CONDENAST_ACTIONMESSAGE_TABLE exists or not
  select count(*) into table_count from user_tables where table_name='CONDENAST_ACTIONMESSAGE_TABLE';
  -- Table_Count is Zero - means table is not available therefore create a new TB
  IF table_count=0 THEN
    EXECUTE IMMEDIATE sql_create;
  END IF;
END;



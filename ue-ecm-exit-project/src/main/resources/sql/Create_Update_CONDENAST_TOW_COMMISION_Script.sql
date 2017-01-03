-- Please run this statement as a "Script" in SQL Developer.
-- Creates a Table -  CONDENAST_TOW_COMMISION


DECLARE 

  table_count number;
  column_new_count number;
  
  sql_create varchar2(1000) := 'CREATE TABLE CONDENAST_TOW_COMMISION
   (	TEMPLATE_NAME VARCHAR2(128 CHAR) NOT NULL ENABLE, 
			OWNING_ORGANIZATION VARCHAR2(300 CHAR) NOT NULL ENABLE, 
			TYPE_OF_WORK VARCHAR2(50 CHAR) NOT NULL ENABLE, 
			COMMISSION_STATUS VARCHAR2(50 CHAR) NOT NULL ENABLE, 
			STATUS VARCHAR2(32 CHAR) NOT NULL ENABLE, 
			GAP VARCHAR2(1 CHAR) NOT NULL ENABLE, 
			COMMENTS VARCHAR2(100 CHAR),
			LINE_SEQ NUMBER(6,0)   )';
  
  sql_alter varchar2(1000) := 'alter table CONDENAST_TOW_COMMISION ADD LINE_SEQ number(6)';           
  
 
BEGIN
  select count(*) into table_count from user_tables where table_name='CONDENAST_TOW_COMMISION';
  IF table_count=0 THEN
    EXECUTE IMMEDIATE sql_create;    
  ELSE 
   select count(*) into column_new_count from all_tab_columns where table_name like 'CONDENAST_TOW_COMMISION' and column_name like 'LINE_SEQ';
   IF column_new_count = 0  THEN
    EXECUTE IMMEDIATE sql_alter;
   END IF;
  END IF;     
END;

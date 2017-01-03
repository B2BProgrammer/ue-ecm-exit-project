-- Please run this statement as a "Script" in SQL Developer.
-- Creates a Table -  CONDENAST_OU_DEPARTMENT

DECLARE 
 -- Used to store the return count of Tables exists or not
  table_count number;
  column_new_count number;
  
  -- Statement to create a TB - CONDENAST_OU_DEPARTMENT
  sql_create varchar2(1000) := 'CREATE TABLE CONDENAST_OU_DEPARTMENT 
                              (	TEMPLATE_NAME VARCHAR2(128 CHAR) NOT NULL ENABLE, 
                                OWNING_ORGANIZATION VARCHAR2(300 CHAR) NOT NULL ENABLE, 
                                OPERATING_UNIT_DEPT VARCHAR2(50 CHAR) NOT NULL ENABLE, 
                                STATUS VARCHAR2(32 CHAR) NOT NULL ENABLE, 
                                GAP VARCHAR2(1 CHAR) NOT NULL ENABLE, 
                                COMMENTS VARCHAR2(100 CHAR),
                                LINE_SEQ NUMBER(6,0))  ';
  
  -- Statement to add a coloumn LINE_SEQ into TB -  CONDENAST_OU_DEPARTMENT                          
  sql_alter varchar2(1000) := 'alter table CONDENAST_OU_DEPARTMENT ADD LINE_SEQ number(6)';           
  
 
BEGIN
  
  select count(*) into table_count from user_tables where table_name='CONDENAST_OU_DEPARTMENT';
 
 
  IF table_count=0 THEN
    EXECUTE IMMEDIATE sql_create;    
  ELSE 
   -- If column_new_count is Zero -- means coloumn LINE_SEQ is not available in the TB - CONDENAST_OU_DEPARTMENT
   select count(*) into column_new_count from all_tab_columns where table_name like 'CONDENAST_OU_DEPARTMENT' and column_name like 'LINE_SEQ';
   IF column_new_count = 0  THEN
    EXECUTE IMMEDIATE sql_alter;
   END IF;
  END IF;     
END;

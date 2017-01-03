package com.emptoris.ecm.exit.condenast.constants;

/**
 * Interface used to load all the data from the Configuration file to specific requirements
 * 
 * @author Ajith.Ajjarani
 *
 */
public interface ConfigurationConstants {	

	// Requirement 1 - configuration Constants & Terms
	String CNE_WS_ENDPT = "cn_ue_contract_creation_notification_webservice_endpoint";
	String CNE_WS_TIMOT = "cn_ue_contract_creation_notification_webservice_timeout";

	String CNE_RIG_CODE =  "cn_ue_custom_term_rights_code";	
	String CNE_CTS_CALL = "cn_ue_custom_term_ctsCallType";
	String CNE_PAY_STYL = "cn_ue_custom_term_paymentStyle";
	

	// Requirement 2 - configurations constants & terms
	String CNE_LINE_TOW     = "cn_ue_populate_lines_linedefinition_tow";	
	String CNE_LINE_TOW_DF1 = "cn_ue_populate_lines_data_definition_tow";
	String CNE_LINE_TOW_DF2 = "cn_ue_populate_lines_data_definition_tow_commission_status";
	String CNE_LINE_TOW_DF3	= "cn_ue_populate_lines_data_definition_tow_status";
	String CNE_LINE_TOW_DF4	= "cn_ue_populate_lines_data_definition_tow_gap";
	String CNE_LINE_TOW_DF5 = "cn_ue_populate_lines_data_definition_tow_comments";


	String CNE_LINE_DEPT     =	"cn_ue_populate_lines_linedefinition_ou_dept";
	String CNE_LINE_DEPT_DF1 = 	"cn_ue_populate_lines_data_definition_ou_department";
	String CNE_LINE_DEPT_DF2 =  "cn_ue_populate_lines_data_definition_ou_department_status";
	String CNE_LINE_DEPT_DF3 =  "cn_ue_populate_lines_data_definition_ou_department_gap";
	String CNE_LINE_DEPT_DF4 =  "cn_ue_populate_lines_data_definition_ou_department_comments";


	// Requirement 3 - configuration Constants
	String CNE_TEMP_UG_VAL = "cn_ue_assign_contract_instance_security_indicator_custom_term_usergroup_value";	
	String CNE_TEMPLATE_UG ="cn_ue_assign_contract_instance_security_indicator_custom_term_usergroup";



	// Requirement 4 - configuration Constants & Terms	
	String CNE_PS_CONT_ID = "cn_ue_custom_term_ps_contract_id";
	
}

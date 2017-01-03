package com.emptoris.ecm.exit.condenast.constants;

/**
 * Interface used to bring all the Boolean Flag for the requirements
 * 
 * @author Ajith.Ajjarani
 *
 */

public interface EnabledFlags {

	// Boolean Flag for Req 1
	String CNE_REQ_1_FLAG = "cn_ue_contract_creation_notification_call_enabled";
	
	// Boolean Flag for Req 1
	String CNE_REQ_2_FLAG = "cn_ue_populate_lines_on_contract_create_enabled";

	// Boolean Flag for Req 3
	String CNE_REQ_3_FLAG ="cn_ue_assign_contract_instance_security_enabled";


	// Boolean Flag for Req 4
	String CNE_REQ_4_1_FLAG = "cn_ue_ps_contract_number_assign_on_create_enabled";
	String CNE_REQ_4_2_FLAG = "cn_ue_ps_contract_number_read_only_for_save_enabled";



	// Boolean Flag for Req 5
	String CNE_REQ_5_1_FLAG = "cn_ue_custom_master_term_assign_default_values_on_create_enabled";
	String CNE_REQ_5_2_FLAG = "cn_ue_custom_master_term_auto_update_values_on_save_enabled";

	// Boolean Flag for Req 6
	String CNE_REQ_6_FLAG = "cn_ue_custom_send_contract_to_sap";

}

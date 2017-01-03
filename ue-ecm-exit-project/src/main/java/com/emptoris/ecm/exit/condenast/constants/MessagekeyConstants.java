/**
 * 
 */
package com.emptoris.ecm.exit.condenast.constants;

/**
 * Interface used to bring all the Error/Warning messages from Exit_messages properties file
 * 
 * @author Ajith.Ajjarani
 *
 */
public interface MessagekeyConstants {
	
	
	String GENERAL_ECMAPI_ERROR = "general.ecmapi.error";

	// Null & Exception common to All requirements 
	String MSG_CNE_EXE = "condenast_ue_er_ex";
	String MSG_CNE_NULL = "condenast_ue_er_nu";

	// requirement 1 - Message Constants
	String MSG_CC_AM1 = "condenast_ccn_am_1";
	String MSG_CC_AM2 = "condenast_ccn_am_2";
	String MSG_CC_AM3 = "condenast_ccn_am_3";
	String MSG_CC_AM4 = "condenast_ccn_am_4";
	String MSG_CC_AM5 = "condenast_ccn_am_5";
	String MSG_CC_AM6 = "condenast_ccn_am_6";
	String MSG_CC_AM7 = "condenast_ccn_am_7";
	
	
	// Requirement 2 - 
	String MSG_PE_AM1 = "condenast_pe_am_1";
	String MSG_PE_AM2 = "condenast_pe_am_2";
	String MSG_PE_AM3 = "condenast_pe_am_3";
	String MSG_PE_AM4 = "condenast_pe_am_4";
	String MSG_PE_AM5 = "condenast_pe_am_5";
	String MSG_PE_AM6 = "condenast_pe_am_6";
	String MSG_PE_AM7 = "condenast_pe_am_7";



	// requirement 3 - Message Constants
	String MSG_CS_AM1 = "condenast_cs_am_1"; 
	String MSG_CS_AM2 = "condenast_cs_am_2";
	String MSG_CS_AM3 = "condenast_cs_am_3";
	String MSG_CS_AM4 = "condenast_cs_am_4";
	String MSG_CS_AM5 = "condenast_cs_am_5";
	
	String MSG_CS_AM6 = "condenast_cs_am_6";
	String MSG_CS_AM7 ="condenast_cs_am_7";
	

	// requirement 4 - Message Constants
	String MSG_PS_AM_1 = "condenast_ps_am_1";
	String MSG_PS_AM_2 = "condenast_ps_am_2"; 
	String MSG_PS_AM_3 = "condenast_ps_am_3";
	String MSG_PS_AM_4 = "condenast_ps_am_4"; 
	String MSG_PS_AM_5 = "condenast_ps_am_5"; 
	String MSG_PS_AM_6 = "condenast_ps_am_6"; 
	String MSG_PS_AM_7 = "condenast_ps_am_7";		

	
	// warning message
	String MSG_PS_WM_1 = "condenast_ps_contract_id_warning_message";
	
	
	
	// requirement 5 - Message Constants
	String MSG_BTV_AM1 ="condenast_btv_am_1";
	String MSG_BTV_AM2 ="condenast_btv_am_2";
	String MSG_BTV_AM3 ="condenast_btv_am_3";
	String MSG_BTV_AM4 ="condenast_btv_am_4";
	String MSG_BTV_AM5 ="condenast_btv_am_5";
	String MSG_BTV_AM6 ="condenast_btv_am_6";
	
	
	// warning message
	String MSG_BTV_WM1 = "condenast_btv_term_added_warning_message";
	
	//Messages constants for UI
	String MSG_BTV_UIE1 ="condenast_btv_default_value_missing_term";
	String MSG_BTV_UIE2 ="condenast_btv_term_value_update_not_allowed";	
	String MSG_BTV_UIE3 ="condenast_btv_term_datatype_mismatch";	

}

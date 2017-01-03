/**
 * 
 */
package com.emptoris.ecm.exit.condenast.implthree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import persistence.CondeNastSecurityUserGroup;
import persistence.CondeNastTemplateSecUsrGrpPersister;
import persistence.SeqSecurityIterator;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.CustomExitContext;
import com.emptoris.ecm.exit.SubroutineResult;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;

/**
 * Business Implementation class for - providing the Security features -  relevant UserGroups & permission groups
 * for each Contract Instance
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractSecurity extends CustomUserExitBase {
	
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ContractSecurity.class);
	
	// class level variable to store all the required terms for BIC implementation
	private List<String> requiredConstants = new ArrayList<String>();
	private List<String> requiredTerms = new ArrayList<String>();
	
	
	
	/**
	 * Constructor where - all required configurations are preloaded to test validation
	 * 
	 * @param exitContext
	 */
	public ContractSecurity(ExitContext exitContext) {
		super(exitContext);			
		LOG.info("Start --> Constructor of ContractSecurity");
		// required Terms for this BIC Implementation
		requiredTerms.add(ConfigurationConstants.CNE_TEMPLATE_UG.trim());
		
		// required constants for this BIC Implementation		
		requiredConstants.add(ConfigurationConstants.CNE_TEMP_UG_VAL.trim());		
		LOG.info("End --> Constructor of ContractSecurity");		
	}
	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {		
		return EnabledFlags.CNE_REQ_3_FLAG.trim();
	}

	@Override
	protected List<String> getRequiredTermsConfigKeys() {
		return requiredTerms;
	}

	@Override
	protected List<String> getRequiredConfigConstants() {		
		return requiredConstants;
	}
	
	
	@Override
	protected String getMessageConfigValueNotAvail() {		
		return MessagekeyConstants.MSG_CS_AM1.trim();
	}
	
	
	@Override
	protected String getErrorCodeForConfigValueNotAvail() {		
		return ErrorCodes.CS_AM_1;
	}


	@Override
	protected String getMessageTermNotAvailInECM() {		
		return MessagekeyConstants.MSG_CS_AM2.trim();
	}


	@Override
	protected String getErrorCodeForTermNotAvailInECM() {	
		return ErrorCodes.CS_AM_2;
	}
	
	

	

	/**
	 * Helper method called to check the Flag is Enabled or Not
	 * 
	 * @return
	 */
	public boolean checkFlag() {
		LOG.info("Starting --> Boolean Flag check of ContractSecurity");
		boolean isFlagEnabled = isFlagEnabled();

		LOG.info("The Flag for the BIC is Enabled --> "+isFlagEnabled);
		return isFlagEnabled;	
	}
	
	
	/**
	 * General Helper Method - To check all the preconditions set for entering Business Logic
	 * @param exitContext
	 * @return
	 */
	public boolean isAllPreConditionsSatisfied(ExitContext exitContext) {	
		LOG.info("Starting --> isAllPreConditionsSatisfied of ContractSecurity" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}
	
	
	
	/**
	 * Method called from the Handler class - to do all the flow operations 
	 * 
	 * @param exitContext
	 */
	public void executeLogic(ExitContext exitContext) {
		LOG.info("Enter --> Execution of Core Logic for ContractSecurity");
		if(isTemplateUserGroupCheckedNYes(exitContext)) {
			getTemplateNOwningOrganization(exitContext);
		} else {
			LOG.info("Template UserGroup is not Set OR not having value  - YES");
		}
		LOG.info("Exit --> Execution of Core Logic for ContractSecurity");
	}
	
	


	/**
	 * Helper method to check - whether the CNE_TEMPLATE_UG is available & have preset value of YES
	 * @param exitContext
	 * @return
	 */
	private boolean isTemplateUserGroupCheckedNYes(ExitContext exitContext) {	
		LOG.info("Start --> Method for isTemplateUserGroupCheckedNYes");
		boolean isTempalateUGChecked = false;
			
		try {
			// Term configured in Template - which acts as indicator for Contract Security functionality to work
			String cneTemplateUGTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_TEMPLATE_UG.trim());			
			if(ContractTermsUtil.isTermAvailableInContract(exitContext, cneTemplateUGTermName)){
				LOG.info("Contract security indicator term" +cneTemplateUGTermName+ " Available in Template");
				
				// Value provided from Term
				String cneTemplateUGTermValue = (String) ContractTermsUtil.getTermValue(exitContext, cneTemplateUGTermName);
				LOG.info("Value available in Term : "+cneTemplateUGTermValue);
				
				// Value provided in TermConfiguration file [should be -YES]
				String configFileValueCNETempUG  = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_TEMP_UG_VAL.trim());
				LOG.info("Value available in Configuration File : "+configFileValueCNETempUG);
				
				// checking the YES value 
				if(cneTemplateUGTermValue.equalsIgnoreCase(configFileValueCNETempUG.trim())) {
					LOG.debug("YES Matched & Term exists in the contract" +cneTemplateUGTermName);
					LOG.debug("The Contract Number for Security will be introduced" + exitContext.getContract().getNumber());
					isTempalateUGChecked = true;					
				} else {
					LOG.debug("YES value mismatch - Need to Bypass the requirememnt");
					return false;
				}
				
			} else {
				LOG.error("condenast_template_usergroup -->" +cneTemplateUGTermName+" Term Not Available in Contract Instance");
				LOG.error("Need to Bypass the requirement");
				return false;
			}
				
		} catch (ConfigurationException e) {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("Configuration Exception while accessing the Term - condenast_template_usergroup_value");
			
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ConfigurationException_While_accesing_Template_UG_Group"); 					;
			String errorCode = ErrorCodes.CS_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			
			isTempalateUGChecked = false;			
		}	
		LOG.info("End --> Method for isTemplateUserGroupCheckedNYes");
		return isTempalateUGChecked;
	}


	




	/**
	 * Helper method fetches 
	 * a) Template name 
	 * b) Owning organization 
	 * & calls for DB Operation
	 * 
	 * @param exitContext
	 */
	private void getTemplateNOwningOrganization(ExitContext exitContext) {
		LOG.info("Start --> Method for getTemplateNOwningOrganization");
		
		// 1 - Fetching the Template name
		String templateName = null;
		try {
			templateName = exitContext.getContract().getTemplate().getName();
			LOG.info("The Retrieved Template Name of the contract" +templateName );
			
		} catch (ContractException e) {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("ContractException while accesing Template Name - Contract Number" + exitContext.getContract().getNumber());		
			
			// Error Code & Error message for DB to dump  => CS_EM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException_While_Template_Name");
			String errorCode = ErrorCodes.CS_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
			
		}
		
		
		// 2 - Fetching the owning Organization name
		String owningOrganizationName = null;		
		try {
			owningOrganizationName = exitContext.getContract().getOwningOrganization().getName();
			LOG.info("The Retrieved OWNING Organization of the contract" +owningOrganizationName );
			
			
		} catch (ContractException e) {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("ContractException exception while accesing the owning Organization Name - Contract Number" + exitContext.getContract().getNumber());
			
			// Error Code & Error message for DB to dump  => CS_EM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE, null , "ContractException_While_OwningOrganization");
			String errorCode = ErrorCodes.CS_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
		}
		
		
		if(templateName != null && owningOrganizationName != null 
			|| !(templateName.isEmpty() && owningOrganizationName.isEmpty())){
			
			// Call the method - do operation for getting UserGroup name & permissions Group
			 dbOperation(templateName,owningOrganizationName , exitContext);
			
		} else {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("Template name or OwniOganization is NULL" + exitContext.getContract().getNumber());
			
			// Error Code & Error message for DB to dump  => CS_EM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_NULL, null , "TemplateName_OR_OwningOrganizaion_NULL");
			String errorCode = ErrorCodes.CS_ER_NU;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
			
		}
		LOG.info("End --> Method for getTemplateNOwningOrganization");
	}




	/**
	 * Core Logic operation which sends Template Name & Organization name to get details from the Database
	 * 
	 * @param templateName
	 * @param owningOrganizationName
	 */
	private void dbOperation(String templateName , String owningOrganizationName , ExitContext exitContext) {
		LOG.info("Start --> Method for dbOperation");
		HashMap<String, ArrayList<String>> hMapUGwithPG = new HashMap<String, ArrayList<String>>();

		SecurityAssignmentSubroutine securityAssignmentSubroutine = null;
		SubroutineResult result = new SubroutineResult();
		try {
			securityAssignmentSubroutine = new SecurityAssignmentSubroutine(exitContext, CustomExitContext.getInstance());

			// Creation of Value object
			CondeNastSecurityUserGroup condenastsecurityVO = new CondeNastSecurityUserGroup();
			condenastsecurityVO.setTemplatename(templateName);
			condenastsecurityVO.setOwningorganizationname(owningOrganizationName);

			CondeNastTemplateSecUsrGrpPersister secUGpersister = CondeNastTemplateSecUsrGrpPersister.getInstance();		


			if(secUGpersister != null){	
				SeqSecurityIterator seqsecurityIter = (SeqSecurityIterator) secUGpersister.getUserPermissionGroup(condenastsecurityVO);

				if(seqsecurityIter != null) {					
					seqsecurityIter.prefetch();
					// No Rows available from Database Tables
					if(seqsecurityIter.count() != 0) {
						ArrayList<CondeNastSecurityUserGroup> dbValue = seqsecurityIter.prefetch();

						for(CondeNastSecurityUserGroup eachRow : dbValue){
							String uGName =  eachRow.getUsergroupname();
							String pGName =  eachRow.getPermissiongroup();
							addDatatoMap(uGName, pGName, hMapUGwithPG);
						}

						Set<String> userGroupnameSet  = hMapUGwithPG.keySet();
						for (String eachUserGrpname : userGroupnameSet) {
							ArrayList<String> permissionGroupList = hMapUGwithPG.get(eachUserGrpname);
							for (String permissionGroup : permissionGroupList) {
								securityAssignmentSubroutine.addUserOrUserGroupToContract(exitContext, result, eachUserGrpname, permissionGroup);
							}
						}	
					} else {
						LOG.error("Contract security requirement Bypassed - due to number of rows returned to 0 ");
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM6.trim(), null , null); 
						String errorCode = ErrorCodes.CS_AM_6;
						
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);
						
						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);	
						return;									
					}
					LOG.debug("Contract Security : User Group/ Permission Group Retrieved from the database");
				} else {
					// LOG to DB - Iterator Object is NULL
					LOG.error("Contract Creation requirement Bypassed - due to Iterator Object is NULL");
					String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_NULL.trim(), null , "Iterator Object is NULL"); 
					String errorCode = ErrorCodes.CS_ER_NU;
					
					LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
					LOG.error("The Error Code going to Action LOG TB :" + errorCode);
					
					// Creation of Error object containing all the information need to be dumped to DB
					List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
					// Adding the resultant error object to List handled after the completion of business requirement
					errorListTODB.add(resultErrorObject);	
					return;				
					
				}

			} else {
				// LOG to DB - persister Object is NULL
				LOG.error("Contract security requirement Bypassed - due persister Object is NULL");
				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_NULL.trim(), null , "persister Object is NULL"); 
				String errorCode = ErrorCodes.CS_ER_NU;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);	
				return;
				
			}


		} catch(PersistenceException e){	
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("Contract Security : No User Group/ Permission Group Available in the database" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM7.trim(), null , null); 
			String errorCode = ErrorCodes.CS_AM_7;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);	
			return;	
			
		} catch (PluginException e) {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("PluginException exception while accesing == addUserOrUserGroupToContract  "	+ exitContext.getContract().getNumber());
			
			// Error Code & Error message for DB to dump 
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PluginException"); 
			String errorCode = ErrorCodes.CS_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
		} catch (WarningMessageException e) {
			LOG.error("Contract Security Process - Requirement 3 ================================== SKIPPED");
			LOG.error("WarningMessageException exception while accesing == addUserOrUserGroupToContract  "	+ exitContext.getContract().getNumber());
			
			// Error Code & Error message for DB to dump 
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "WarningMessageException"); 
			String errorCode = ErrorCodes.CS_ER_EX;		
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
		} catch (ConfigurationException e) {
			LOG.error("Contract Security requirement of Contract Creation process " +
					  "Requirement 3 ================================== SKIPPED");
			LOG.error("ConfigurationException exception while accesing == addUserOrUserGroupToContract  "
					+ exitContext.getContract().getNumber());
			
			// Error Code & Error message for DB to dump  => CS_EM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ConfigurationException"); 
			String errorCode = ErrorCodes.CS_ER_EX;		
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			
			// return this method - no more proceeding with this requirement
			return;
		}	
		LOG.info("End --> Method for dbOperation");
	}
	
	
	
	/**
	 * Method to security groups to a map.
	 * @param groupAskey
	 * @param permissionAsValue
	 * @param hMapUGwithPGList
	 */
	private void addDatatoMap(String groupAskey, String permissionAsValue,	
							HashMap<String, ArrayList<String>> hMapUGwithPGList ) {
		
		LOG.info("Start of method ==> addDatatoMap for contract Instance");
		if(null == hMapUGwithPGList) {
			hMapUGwithPGList = new HashMap<String, ArrayList<String>>();
			LOG.debug("Contract Security : Adding User Group to a Map");
		}
		
		if(hMapUGwithPGList.containsKey(groupAskey)) {
			ArrayList<String> permissionValues = hMapUGwithPGList.get(groupAskey);
			permissionValues.add(permissionAsValue);
			hMapUGwithPGList.put(groupAskey, permissionValues);
			LOG.debug("Contract Security : Adding User Group to a Map");
		} 
		if(!hMapUGwithPGList.containsKey(groupAskey)) {
			ArrayList<String> permissionValues = new ArrayList<String>();
			permissionValues.add(permissionAsValue);
			hMapUGwithPGList.put(groupAskey, permissionValues);
			LOG.debug("Contract Security : Adding UserGroup to a Map");
		} 
		LOG.info("End of method ==> addDatatoMap for contract Instance");	
	}	


	

}

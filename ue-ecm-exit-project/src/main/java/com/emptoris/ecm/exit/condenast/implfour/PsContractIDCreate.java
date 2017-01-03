/**
 * 
 */
package com.emptoris.ecm.exit.condenast.implfour;

import java.util.ArrayList;
import java.util.List;

import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.Helper.ContractHelper;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.exception.EcmApiException;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.ContractConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;
import com.emptoris.ecm.exit.condenast.util.GeneralUtility;


/**
 * Business Implementation class for - Checking PsContractID Logic while on PostContractCreate
 * 
 * @author Ajith.Ajjarani
 *
 */
public class PsContractIDCreate extends CustomUserExitBase{
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(PsContractIDCreate.class);
	
	// class level variable to store all the required terms for BIC implementation
	private List<String> requiredTerms = new ArrayList<String>();
	

	/**
	 * Constructor, where all required Terms & constants for BIC are initialized
	 * @param exitContext
	 */
	public PsContractIDCreate(ExitContext exitContext) {		
		super(exitContext);
		LOG.info("Start --> Constructor of PsContractIDCreate");
		
		// required Terms for this BIC Implementation
		requiredTerms.add(ConfigurationConstants.CNE_PS_CONT_ID.trim());	
		
		LOG.info("End --> Constructor of PsContractIDCreate");
	}
	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */

	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_4_1_FLAG.trim();
	}

	@Override
	protected List<String> getRequiredTermsConfigKeys() {		
		return requiredTerms;
	}

	@Override
	protected List<String> getRequiredConfigConstants() {		
		return null;
	}
	
	
	@Override
	protected String getMessageConfigValueNotAvail() {
		return MessagekeyConstants.MSG_PS_AM_1.trim();
	}
	
	@Override
	protected String getErrorCodeForConfigValueNotAvail() {
		return ErrorCodes.PS_AM_1;
	}
	

	@Override
	protected String getMessageTermNotAvailInECM() {
		return MessagekeyConstants.MSG_PS_AM_2.trim();
	}	
	
	@Override
	protected String getErrorCodeForTermNotAvailInECM() {
		return ErrorCodes.PS_AM_2;
	}
	

	/**Helper method called to check the Flag is Enabled or Not
	 * @return
	 */
	public boolean checkFlag() {
		LOG.info("Starting --> Boolean Flag check of PsContractIDCreate");
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
		LOG.info("Starting --> isAllPreConditionsSatisfied of PsContractIDCreate" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}
	
	
	
	

	/**
	 * Public method - accessed from Handler Class, which decides the flow operations
	 * @param exitContext
	 * @param isFromSaveAs 
	 */
	public void executeLogic(ExitContext exitContext, boolean isFromSaveAs) {
		LOG.info("Enter --> Execution of Core Logic for PsContractIDCreate");
		//Calling the helper method to check certain term & updating psContractId value
		psContractIdCoreLogic(exitContext , isFromSaveAs);
		LOG.info("Exit --> Execution of Core Logic for PsContractIDCreate");
	}
	
	

	/**
	 * Helper method - Used to populate the term with a value of contract Number prefixed wiht 
	 * ZERO's so that the length should be 10
	 * 
	 * Eg - contract number - 671
	 * psContractID value - 0000000671
	 * 
	 * However - when contract is created is using SAVEAs 
	 * -The new ContractNumber's value will be populated into psContractId
	 * 
	 * 
	 * @param exitContext
	 * @param isFromSaveAs 
	 */
	private void psContractIdCoreLogic(ExitContext exitContext, boolean isFromSaveAs) {
		LOG.info("Enter --> Execution of psContractIdCoreLogic Method : ");
		
		// Fetching psContractId name as available in Configuration File
		String psContractIDTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_PS_CONT_ID.trim());
		try {			
			if(ContractTermsUtil.isTermAvailableInContract(exitContext, psContractIDTermName)){
				// psContractId is Available in the Contract Instance			
				
				LOG.debug("psContractId is Available in the Contract Instance"+ psContractIDTermName);
				String psContractIdTermValue = (String) ContractTermsUtil.getTermValue(exitContext, psContractIDTermName);	
				if( psContractIdTermValue != null && !psContractIdTermValue.trim().isEmpty()){
					// psContractId term Value is Available -- Check for SAVEAs logic	
					if( isFromSaveAs){
						// new contract is Created from SAVE-AS
						LOG.info("Contract is created from SAVE-AS");						
						toUpdateWithContractNumber(exitContext, psContractIDTermName);
					} 
				} else {					
					// Fresh contract is being created - psContractId term Value is Empty or NULL 
					LOG.info("psContractId term Value is Empty or NULL");
					toUpdateWithContractNumber(exitContext, psContractIDTermName);					
				}
			} else {
				//psContractId is NOT Available in the Contract Instance				
				// psContractId term Value is Empty or NULL
				LOG.debug("psContractId is NOT Available in the Contract Instance"+ psContractIDTermName);				
				int contractNumber = exitContext.getContract().getNumber();
				String contractNumbStr =  String.valueOf(contractNumber);
				String prefixContractNumString = GeneralUtility.padCharsLeft(contractNumbStr, 
																			ContractConstants.PREFIX_CHAR, 
																			ContractConstants.PS_CONT_LEN);				
				exitContext.getContract().addStringBtvToContract(psContractIDTermName, prefixContractNumString, exitContext.getUser().getId());
				
				LOG.info("The psContractId is added & Value added is :" +prefixContractNumString);
			}

		} catch (ContractException e) {						
			LOG.error("PS_AM_3 ==> PsContractId Create requirement Bypassed - due to ContractException " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> PS_AM_3	
			ArrayList<String> toDynamicMessage = new ArrayList<String>();
			toDynamicMessage.add("ContractException");
			toDynamicMessage.add(e.getLocalizedMessage());			
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_3.trim(), toDynamicMessage, null);			
			String errorCode = ErrorCodes.PS_AM_3;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} 		
		
		LOG.info("Exit --> Execution of psContractIdCoreLogic Method: ");
		
	}
	
	

	/**
	 * Helper method - used to construct a 10 digit contract number prefixed  with zeros &
	 * add the following to psContractId term
	 * 
	 * 
	 * @param exitContext
	 * @param psContractIDTermName
	 */
	private void toUpdateWithContractNumber(ExitContext exitContext, String psContractIDTermName) {
		LOG.info("Enter method -> toUpdateWithContractNumber for psContractId");
		
		try {
			int contractNumber = exitContext.getContract().getNumber();
			String contractNumbStr =  String.valueOf(contractNumber);
			String prefixContractNumString = GeneralUtility.padCharsLeft(contractNumbStr, 
																		ContractConstants.PREFIX_CHAR, 
																		ContractConstants.PS_CONT_LEN);
			ContractHelper.updateTerm(exitContext.getContract(), psContractIDTermName, prefixContractNumString);
			
			LOG.info("The Value of contract number updated to psContractId" +prefixContractNumString);
		} catch (EcmApiException e) {
			LOG.error("PS_AM_4 ==> PsContractId Create requirement Bypassed - due to EcmApiException " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> PS_AM_4
			ArrayList<String> toDynamicMessage = new ArrayList<String>();
			toDynamicMessage.add("EcmApiException");
			toDynamicMessage.add(e.getLocalizedMessage());			
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_4.trim(), toDynamicMessage, null);								
			String errorCode = ErrorCodes.PS_AM_4;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}
		
		LOG.info("Exit method -> toUpdateWithContractNumber for psContractId");
		
	}

	
}

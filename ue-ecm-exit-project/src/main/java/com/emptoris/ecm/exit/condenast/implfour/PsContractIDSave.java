/**
 * 
 */
package com.emptoris.ecm.exit.condenast.implfour;

import java.util.ArrayList;
import java.util.List;

import com.dicarta.appfound.contract.ContractData;
import com.dicarta.appfound.contract.NegotiatorException;
import com.dicarta.appfound.contract.server.bo.ContractBO;
import com.dicarta.appfound.contractmanager.btv.common.BTVException;
import com.dicarta.appfound.contractmanager.btv.common.ContractBTVData;
import com.dicarta.appfound.contractmanager.btv.server.bo.BTVMgrBO;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.exception.TermValueSetException;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.TermValue;
import com.emptoris.ecm.api.intf.TermValueSet;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;

/**
 * Business Implementation class for - Checking PsContractID Logic while on PreContractSave
 * 
 * @author Ajith.Ajjarani
 *
 */
public class PsContractIDSave extends CustomUserExitBase {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(PsContractIDSave.class);	
	
	// class level variable to store all the required terms for BIC implementation
	private List<String> requiredTerms = new ArrayList<String>();
	

	/**
	 * Constructor, where all required Terms & constants for BIC are initialized
	 * 
	 * @param exitContext
	 */
	public PsContractIDSave(ExitContext exitContext) {
		super(exitContext);		
		LOG.info("Start --> Constructor of PsContractIDSave");
		
		// required Terms for this BIC Implementation
		requiredTerms.add(ConfigurationConstants.CNE_PS_CONT_ID.trim());
		
		LOG.info("End --> Constructor of PsContractIDSave");
	}

	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_4_2_FLAG.trim();
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
		LOG.info("Starting --> isAllPreConditionsSatisfied of PsContractIDSave" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}
	
	
	
	
	/**
	 * Public method - accessed from Handler Class, which decides the flow operations
	 * @param exitContext
	 * @throws WarningMessageException 
	 */
	public void executeLogic(ExitContext exitContext) throws WarningMessageException{
		LOG.info("Enter --> Execution of Core Logic for PsContractIDSave");	
		//Calling the helper method to check certain term & its value
		executePsContractIDSaveLogic(exitContext);				
		LOG.info("Exit --> Execution of Core Logic for PsContractIDSave");
	}
	
	

	

	/**
	 * Helper method - Does the core Save Logic operations
	 * Checks weather psContract Id value is changed or not & takes actions accordingly
	 * 
	 * @param exitContext
	 * @throws WarningMessageException 
	 */
	private void executePsContractIDSaveLogic(ExitContext exitContext) throws WarningMessageException {
		LOG.info("Enter --> Execution of method executePsContractIDSaveLogic");
		
		//psContract ID Term name as in Configuration file
		String psContractIDTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_PS_CONT_ID.trim());

		//Is psContractId term available in the contract
		if(ContractTermsUtil.isTermAvailableInContract(exitContext, psContractIDTermName)){
			LOG.info("psContractId term is available in Draft revision of Contract");
			if(isBTVValueChanged(exitContext , psContractIDTermName)){
				// Operations to pull Earlier value - [Latest revision number value]
				ContractData latestRevisionContract = getlatestRevisionContractData(exitContext);		
				int latestrevision = latestRevisionContract.getRevision();
				String previousTermValue = getValueFromPreviousVersionNumber(exitContext, 
																			exitContext.getInternalUserObject().getId(),
																			latestrevision,
																			psContractIDTermName);

				if(previousTermValue != null && !previousTermValue.trim().equalsIgnoreCase("")){				
					revertORUpdateTermValue(exitContext,psContractIDTermName, previousTermValue);
				} else {
					LOG.debug("The Earlier value of the BTV is NULL or Empty --> Bypass the requirement");
				}			
			} else {
				LOG.debug("Exit the requirement as the Term Value is Not Changed");
			}
		} else {
			// BTV is not available in the Contract Instance -- add BTV & set Contract Number into the BTV
			LOG.info("psContractId term is NOT available in Draft revision of Contract");
			addBTVtoContractInstanceNSetContrNum(exitContext , psContractIDTermName);
		}
		
		LOG.info("Exit --> Execution of method executePsContractIDSaveLogic");

	}


	/**
	 * Helper method - which adds the psContractID to ContractInstance & set the contract Number into that term value
	 * 
	 * @param exitContext
	 * @param psContractIDTermName
	 */
	private void addBTVtoContractInstanceNSetContrNum(ExitContext exitContext, String psContractIDTermName ) {
		LOG.info("Enter --> Execution of method addBTVtoContractInstanceNSetContrNum");
		
		try {
			ContractData latestRevisionContract = getlatestRevisionContractData(exitContext);		
			int latestrevision = latestRevisionContract.getRevision();
			String previousTermValue = getValueFromPreviousVersionNumber(exitContext, 
																		exitContext.getInternalUserObject().getId(),
																		latestrevision,
																		psContractIDTermName);
			
			if(previousTermValue != null){
				// previous value is available for psContractId -> retrieve & set that value to term
				LOG.info("previous value is available for psContractId -> retrieve & set that value to term"+ previousTermValue);
				exitContext.getContract().addStringBtvToContract(psContractIDTermName, previousTermValue, exitContext.getUser().getId());
			} else {
				// previous value is NOT available for psContractId -> retrieve & set that value to term
				exitContext.getContract().addStringBtvToContract(psContractIDTermName, " ", exitContext.getUser().getId());
			}			
			
		} catch (ContractException e) {					
			// Error Code & Error message for DB to dump ==> PS_AM_5
			ArrayList<String> toDynamicMessage = new ArrayList<String>();
			toDynamicMessage.add("ContractException");
			toDynamicMessage.add(e.getLocalizedMessage());
			
			LOG.error("PS_AM_5 ==> PsContractId Save requirement Bypassed - due to ContractException " + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_5.trim(), toDynamicMessage, null);			
			String errorCode = ErrorCodes.PS_AM_5;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}
		
		LOG.info("Exit --> Execution of method addBTVtoContractInstanceNSetContrNum");
		
	}	
	
	
	

	/**
	 * Reverting the psContracID value to old value only - if some one has tried to edit this value & show an error message
	 * 
	 * @param exitContext
	 * @param psContractIDTermName
	 * @param previousTermValue
	 * @throws WarningMessageException 
	 */
	private void revertORUpdateTermValue(ExitContext exitContext, String psContractIDTermName, String previousTermValue) throws WarningMessageException {	
		LOG.info("Enter --> Execution of method revertORUpdateTermValue");
		
		try {
			// New value & old Value are NOT same
			TermValueSet termValueSet = null;
			termValueSet = exitContext.getContract().getTermValues();
			termValueSet.update(psContractIDTermName, previousTermValue);
			
			// Show the warning message
			LOG.info("Showing the warning message on UI"+MessagekeyConstants.MSG_PS_WM_1.trim());
			ErrorUtility.throwWarningExceptionOnUI(MessagekeyConstants.MSG_PS_WM_1.trim(), new Object[]{}, null, exitContext);
		} catch (ContractException e) {						
			// Error Code & Error message for DB to dump ==> PS_AM_7	
			ArrayList<String> toDynamicMessage = new ArrayList<String>();
			toDynamicMessage.add("ContractException");
			toDynamicMessage.add(e.getLocalizedMessage());
			
			LOG.error("PS_AM_7 ==> PsContractId Save requirement Bypassed - due to ContractException " + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_7.trim(), toDynamicMessage, null);			
			String errorCode = ErrorCodes.PS_AM_7;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} catch (TermValueSetException e) {						
			// Error Code & Error message for DB to dump ==> PS_AM_7
			ArrayList<String> toDynamicMessage = new ArrayList<String>();
			toDynamicMessage.add("TermValueSetException");
			toDynamicMessage.add(e.getLocalizedMessage());
			
			LOG.error("PS_AM_7 ==> PsContractId Save requirement Bypassed - due to TermValueSetException " + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_7, toDynamicMessage, null);			
			String errorCode = ErrorCodes.PS_AM_7;
			
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}
		
		LOG.info("Exit --> Execution of method revertORUpdateTermValue");
	}
	
	
	
	/**
	 * Helper method - To check whether the BTV value is changed/edited
	 * 
	 * @param exitContext
	 * @param psContractIDTermName
	 * @return
	 */
	private boolean isBTVValueChanged(ExitContext exitContext, String psContractIDTermName) {
		LOG.info("Enter --> Execution of method isBTVValueChanged");
		
		boolean haschanged = false;

		//Get the value after SAVE was clicked - [DRAFT revision Number Value]
		TermValue draftRevTerm = ContractTermsUtil.getTermFromDraftRevContract(exitContext,psContractIDTermName);
		String currentTermValue = null;
		if (null != draftRevTerm) {
			currentTermValue =  draftRevTerm.getTextValue();
		}
		LOG.info("The psContractId term value edited/updated while on SAVE" + currentTermValue);
		

		// Operations to pull Earlier value - [Latest revision number value]
		ContractData latestRevisionContract = getlatestRevisionContractData(exitContext);		
		int latestrevision = latestRevisionContract.getRevision();
		String previousTermValue = getValueFromPreviousVersionNumber(exitContext, 
																	exitContext.getInternalUserObject().getId(),
																	latestrevision,
																	psContractIDTermName);
		LOG.info("The psContractId term value added to contract while creating" + previousTermValue);
		
		
		if( previousTermValue != null && currentTermValue != null) {
			// Compare [SAVE] clicked & [Earlier CREATED] value
			if(!previousTermValue.equals(currentTermValue)){			
				haschanged = true;
			}	
		} else {
			haschanged = false;
		}
		LOG.info("The value of psContract Id is changed" +haschanged);
		LOG.info("Exit --> Execution of method isBTVValueChanged");
		
		return haschanged;	
		
	}
	
	
	
	

	

	

	/**
	 * Helper method - This will provide latest revision contractData 
	 * 
	 * @param exitContext
	 * @return
	 */
	private ContractData getlatestRevisionContractData(ExitContext exitContext) {
		LOG.info("Enter --> Execution of method getlatestRevisionContractData");
		
		String contractid = exitContext.getContract().getId();
		ContractData latestRevisionContract = null;
		if(contractid != null){			
			ContractBO contractBo = ContractBO.getContractInstance();		
			try {
				int latestRev = contractBo.getLatestRevision(contractid);
				latestRevisionContract = contractBo.getContract(contractid, latestRev);
				
			} catch (NegotiatorException e) {						
				// Error Code & Error message for DB to dump ==> PS_AM_6	
				ArrayList<String> toDynamicMessage = new ArrayList<String>();
				toDynamicMessage.add("NegotiatorException");
				toDynamicMessage.add(e.getLocalizedMessage());
				
				LOG.error("PS_AM_6 ==> PsContractId Save requirement Bypassed - due to NegotiatorException " + e.getLocalizedMessage());
				String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_6.trim(), toDynamicMessage, null);			
				String errorCode = ErrorCodes.PS_AM_6;
				
				LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);
				return null;
			} catch (ObjectNotFoundException e) {						
				// Error Code & Error message for DB to dump ==> PS_AM_6	
				ArrayList<String> toDynamicMessage = new ArrayList<String>();
				toDynamicMessage.add("ObjectNotFoundException");
				toDynamicMessage.add(e.getLocalizedMessage());
				
				LOG.error("PS_AM_6 ==> PsContractId Save requirement Bypassed - due to ObjectNotFoundException " + e.getLocalizedMessage());
				String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_6, toDynamicMessage, null);			
				String errorCode = ErrorCodes.PS_AM_6;
				
				
				LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);
				return null;
			}
		} else {
			return null;
		}	
		
		LOG.info("Exit --> Execution of method getlatestRevisionContractData");
		return latestRevisionContract;
	}
	
	
	/**
	 * Helper method  - To get the psContractID's previous version value
	 * 
	 * @param exitContext
	 * @param userID
	 * @param lastrevision
	 * @param psContractIDTermName
	 * @return
	 */
	private String getValueFromPreviousVersionNumber(ExitContext exitContext, String userID,	int lastrevision, String psContractIDTermName) {
		LOG.info("Enter --> Execution of method getValueFromPreviousVersionNumber");
		
		BTVMgrBO btvMgrBO = null;
		btvMgrBO = BTVMgrBO.getInstance();
		ContractBTVData internalTermValue;
		String previousNumber = null;

		if (null != btvMgrBO) {
			ArrayList<ContractBTVData> btvValues;
			try {
				btvValues = btvMgrBO.getBTVsForContract(userID,	exitContext.getContract().getId(), lastrevision);
				for (int i = 0; i < btvValues.size(); i++) {
					internalTermValue = (ContractBTVData) btvValues.get(i);
					if(null!= internalTermValue){
						String internalName = internalTermValue.getInternalName();
						if(null != internalName & internalName.equals(psContractIDTermName)){
							previousNumber = internalTermValue.getValueString();
							LOG.debug("Previous PS Contract ID Value" + previousNumber);
						}
					}
				}
			}
			catch (BTVException e) {						
				// Error Code & Error message for DB to dump ==> PS_AM_6	
				LOG.error("PS_AM_6 ==> PsContractId Save requirement Bypassed - due to BTVException " + e.getLocalizedMessage());
				String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PS_AM_6.trim(), null, "BTVException");			
				String errorCode = ErrorCodes.PS_AM_6;
				
				
				LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);
				return null;
			}
		}

		LOG.info("Exit --> Execution of method getValueFromPreviousVersionNumber");
		return previousNumber;
	}


}

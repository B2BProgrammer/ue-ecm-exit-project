/**
 * 
 */
package com.emptoris.ecm.exit.condenast.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.common.config.EmpConfiguration;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.impl.LineDefinitionSetImpl;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.LineDefinition;
import com.emptoris.ecm.api.intf.LineDefinitionIterator;
import com.emptoris.ecm.exit.common.config.ExitConfig;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.ContractConstants;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.util.ConfigurationFilesUtil;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;
import com.emptoris.ecm.exit.condenast.util.GeneralUtility;

/**
 * Base Abstract Class - inherited by all Business Implementation Classes
 * Does - most of redundant activities of each BIC's like below -
 * 
 * a) Validation operations on Constants
 * b) Validation operations on Terms
 * c) Error handling framework
 * 
 * @author Ajith.Ajjarani
 *
 */
public abstract class CustomUserExitBase {
	
	
	private static final ILogger LOG = Logger.getLogger(CustomUserExitBase.class);
	
	// config Variable - load the configuration file, used in all BIC's
	private static EmpConfiguration config = null;
	
	public List<List<Object>> errorListTODB = new ArrayList<List<Object>>();	
	
	
	
	/**
	 * Constructor Loads the Configuration file - used in all next subsequent operations.
	 * 
	 * @param exitContext
	 */
	public CustomUserExitBase(ExitContext exitContext) {
		super();	
		loadConfigurationProperties(exitContext);
	}
	
	
	
	/**
	 * Load the configuration using the ExitConfig.getExitConfig()
	 * @param exitContext 
	 * 
	 * @throws PluginException
	 */
	private void loadConfigurationProperties(ExitContext exitContext) {
		try {
			config = ExitConfig.getExitConfig();
			
		} catch (ConfigurationException e) {			
			LOG.error("Configuration Exception while loading Configuration File" + e.getLocalizedMessage());
			String dynamicPart = "loadConfigurationProperties_ConfigurationException";
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE, null , dynamicPart); 					;
			String errorCode = ErrorCodes.CS_ER_EX;
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;		
		}

	}
		
	/**
	 * 
	 * @return EmpConfiguration
	 */
	protected final EmpConfiguration getEmpConfig() {
		return config;
	}
	
	
	
	
////////////////////////////////////////////////////////////////////////////////////
//                    ABSTRACT Methods implemented in child classes
///////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * To Load the Flag Configuration Keys
	 * @return
	 */
	protected abstract String getFlagConfigKey();
	
	
	
	/**
	 * To Load all the required Configuration TERM keys for requirement Implementation
	 * @return
	 */
	protected abstract List<String> getRequiredTermsConfigKeys();
	
	
	
	/**
	 * To Load all the required Configuration CONSTANT keys for requirement Implementation
	 * @return
	 */
	protected abstract List<String> getRequiredConfigConstants();
	
	
	/**
	 *  To Load all the Messages from the Messages file for Configuration key's value NOT available
	 * 
	 * @return
	 */
	protected abstract String getMessageConfigValueNotAvail();
	
	/**
	 *  To Load all the Messages from the Messages file for Term is Not Available in ECM application
	 * @return
	 */
	protected abstract String getMessageTermNotAvailInECM();
	
	
	/**
	 * To Load all the Error Code for Configuration key's value NOT available
	 * @return
	 */
	protected abstract String getErrorCodeForConfigValueNotAvail();
	
	/**
	 * To Load all the Error Code for Term is Not Available in ECM application
	 * @return
	 */
	protected abstract String getErrorCodeForTermNotAvailInECM();
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	//                      FLAG Enabled Checking 
	///////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * To Check the Flag Enabled for the requirement
	 * @return
	 */
	protected final boolean isFlagEnabled() {		
		String configKey = getFlagConfigKey();		

		try {
			String configValue = config.getRequiredStringConfiguration(configKey);
			
			if (configValue.trim().equalsIgnoreCase("true")) {
				LOG.info("Configuration for " + configKey + " is set to TRUE value; Logic will be Enabled");
				LOG.info("===== isEnabled - End ");
				return true;
			} else {
				return false;
			}
		} catch (ConfigurationException e) {			
			LOG.debug("Configuration for " + configKey	+ " is not avaiable so assuming the logic is disabled");
			LOG.info("===== isEnabled - End ");
			return false;

		}
	}



	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	//                      Validation operation Checking
	//						Terms Checking
	//						Constants Checking
	///////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * General Method called from the Handler Class - which calls respective private methods to do validation on
	 * Constants & Terms
	 * 
	 * @param exitContext
	 * @throws PluginException 
	 */
	public boolean validateConfigurations(ExitContext exitContext)  {
		LOG.info("START --> Validation operations of each BIC of contract" + exitContext.getContract().getNumber());
		boolean isAllValidation = false;
		
		if(validationOperationsOnConstants(exitContext)){
			LOG.debug("Validation on Constants was successfull");
			if(validationOperationsOnTerms(exitContext)) {
				LOG.debug("Validation on Terms was successfull");
				isAllValidation = true;
			} else {
				LOG.error("Validation on Terms had issues");
				isAllValidation = false;
				
			}
		} else {
			LOG.error("Validation on Constants had issues");
			isAllValidation = false;
		}
		
		LOG.info("End --> Validation operations of each BIC of contract" + exitContext.getContract().getNumber());
		LOG.info("Execute Logic Enabled --> "+isAllValidation);
		
		return isAllValidation;
		
	}



	/**
	 * Helper method - Does all the validation operations required for each constant defined for the BIC
	 * @param exitContext 
	 * @throws PluginException 
	 * 
	 */
	private boolean validationOperationsOnConstants(ExitContext exitContext) {
		
		LOG.info("Enter --> validationOperationsOnConstants Method");
		boolean isValidationOnConstants = false;
		List<String> allRequiredConstantsL = getRequiredConfigConstants();
		
		if(null != allRequiredConstantsL) {
			// 1-  Checking any null Value for the config Constants
			ArrayList<String> anyNullValuedConfigKeys = ConfigurationFilesUtil.areAllTheseConfigKeysDefinedWitValue( exitContext, allRequiredConstantsL);
			if(anyNullValuedConfigKeys.isEmpty()){
				// This means --> All Correct
				LOG.debug("All the Constants defined in Configuration file are having VALUE");			
				isValidationOnConstants = true;
				
			} else {				
				for (String eachNullValuedConfigConstant : anyNullValuedConfigKeys) {
					// Log these information into DB
					LOG.error("The Value is NOT Available for Constant -> configuration Key" + eachNullValuedConfigConstant);
					//String formatErrorMessageToDB = ErrorUtility.createErrorMessage(MessagekeyConstants.MSG_CC_AM1, anyNullValuedConfigKeys, null);	
					
					String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, getMessageConfigValueNotAvail(), null, eachNullValuedConfigConstant);
					LOG.error("The Error Message Information Going to Database  : " + formatErrorMessageToDB);
					
					String errorCode = getErrorCodeForConfigValueNotAvail();	
					LOG.error("The Error Code Information Going to Database  : " + errorCode);
							
					
					// Creation of Error object containing all the information need to be dumped to DB
					List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

					// Adding the resultant error object to List handled after the completion of business requirement
					errorListTODB.add(resultErrorObject);
				}				
				isValidationOnConstants = false;
			}
		} else {
			isValidationOnConstants = true;
		}	
		
		LOG.info("Exit --> validationOperationsOnConstants Method");		
		return isValidationOnConstants;	
	}



	/**
	 * Helper method - Does all the validation operations required for each Terms defined for the BIC
	 * 
	 * @param exitContext 
	 * 
	 */
	private boolean validationOperationsOnTerms(ExitContext exitContext) {		
		LOG.info("Enter --> validationOperationsOnTerms Method");
		
		boolean isValidationTerms = false;
		List<String> allRequiredTermsL = getRequiredTermsConfigKeys();
		
		if(null != allRequiredTermsL){
			
			// 1 - Checking any null Value for the configuration K-V for Terms
			ArrayList<String> anyNullValuedConfigKeys = ConfigurationFilesUtil.areAllTheseConfigKeysDefinedWitValue( exitContext, allRequiredTermsL);
			
			if(anyNullValuedConfigKeys.isEmpty()){
				// This means --> All Correct
				LOG.debug("All Terms for BIC have VALUE available in Configuration file");
				
			} else {			
				for (String eachNullValuedConfigConstant : anyNullValuedConfigKeys) {
					// Log these information into LOG file
					LOG.error("The Value is NOT Available for Term -> configuration Key" + eachNullValuedConfigConstant);
					String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, getMessageConfigValueNotAvail(), null, eachNullValuedConfigConstant);
					LOG.error("The Error Message Information Going to Database  : " + formatErrorMessageToDB);
					
					String errorCode = getErrorCodeForConfigValueNotAvail(); 					
					LOG.error("The Error Code Information Going to Database  : " + errorCode);
					
					// Creation of Error object containing all the information need to be dumped to DB
					List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

					// Adding the resultant error object to List handled after the completion of business requirement
					errorListTODB.add(resultErrorObject);
				}		
				
				isValidationTerms = false;
				return isValidationTerms;
			}
			
			
			// 2 - Checking ConfigKeyValue[TermName] Available in ECM System
			List<String> termsNotAvailInECM = new ArrayList<String>();
			for (String eachRequiredTerm : allRequiredTermsL) {
				 String termName = ContractTermsUtil.getTermName(exitContext, eachRequiredTerm);
				 	if(ContractTermsUtil.isTermAvailableInECM(termName, exitContext)){
				 		LOG.debug("This Term is available in ECM system" +termName );
				 	} else {
				 		LOG.debug("This Term is NOT navailable in ECM system" +termName );
				 		termsNotAvailInECM.add(termName);
				 	}
				 
			}
			
			
			if(termsNotAvailInECM.isEmpty()){
				//All terms are available in ECM System
				LOG.debug("All Terms are available in ECM system");			
				isValidationTerms = true;
			} else {
				
				for (String eachTermNotAvailableInECM : termsNotAvailInECM) {
					// Error Code & Error message for DB to dump 				
					String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, getMessageTermNotAvailInECM(), null, eachTermNotAvailableInECM);
					LOG.error("The Error Message Information Going to Database  : " + formatErrorMessageToDB);
					
					String errorCode = getErrorCodeForTermNotAvailInECM(); 
					LOG.error("The Error Code Information Going to Database  : " + errorCode);

					// Creation of Error object containing all the information need to be dumped to DB
					List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

					// Adding the resultant error object to List handled after the completion of business requirement
					errorListTODB.add(resultErrorObject);
				}
				isValidationTerms = false;
					
			}			
		} else {
			isValidationTerms = true;
		}		
		
		
		
		// 3 - Checking Are These some Terms Available in Contract Instance
		
		
		LOG.info("Exit --> validationOperationsOnTerms Method");
		return isValidationTerms;

	}
	
	
	
	

	//////////////////////////////////////////////////////////
	//                 ERROR HANDLING - START               //
	//////////////////////////////////////////////////////////
	

	/**
	 * Method called from every where in BIC to created the Error Object
	 * 
	 * @param exitContext
	 * @param errorMessage
	 * @param errorCode
	 */
	public List<Object> addthistoErrorObject(ExitContext exitContext,
									  String errorMessage,
									  String errorCode ) {
		
		LOG.info("Enter --> Creation of addthistoErrorObject Method");
		
		List<Object> errorObject = new ArrayList<Object>();
		
		//1 - Column
		errorObject.add(exitContext.getContract().getNumber());
		
		//2 - Column
		String wizardId = exitContext.getWizardId();			
		
		errorObject.add(wizardId);
		
		//3 - Column
		errorObject.add(errorMessage);
		
		//4	 - Column		
		Date date = new Date();
		errorObject.add(date);
		
		//5 - Column		
		String contractOwner = GeneralUtility.getPrimaryContactName(exitContext);
		//String contractOwner = GeneralUtility.getPrimaryPartyName(exitContext);
		//String contractOwner = GeneralUtility.getUser(exitContext);
		errorObject.add(contractOwner);
		
		//6 - Column
		errorObject.add(errorCode);
		
		LOG.info("Exit --> Creation of addthistoErrorObject Method");
		
		// returning the created errorObject List
		return errorObject;
		
	}
	
	

	/**
	 * Method called from BIC after the execution of requirements to run if any errors available in 
	 * BIC & dump those to the ActionLog Table
	 * 
	 * @param exitContext
	 */
	public void runErrorDumpToDB(ExitContext exitContext) {
		
		LOG.info("Enter -->  runErrorDumpToDB Method");

		int ContractNumber = 0;
		String wizardId = null;
		String errorMessage = null;
		Date timeStamp = null;
		String contractOwner = null;
		String errorCode = null;

		if (!errorListTODB.isEmpty()) {
			for (List<Object> eachErrorObjectList : errorListTODB) {
				// 1
				ContractNumber = ((Integer)eachErrorObjectList.get(0)).intValue();
				LOG.info("The Contract Number going to Action LOG TB " +ContractNumber);
				
				// 2
				wizardId = (String) eachErrorObjectList.get(1);	
				LOG.info("The wizardId going to Action LOG TB " +wizardId);

				
				// 3
				errorMessage = (String) eachErrorObjectList.get(2);
				if (errorMessage.length() >= ContractConstants.ERR_MSG_LEN) {
					errorMessage = errorMessage.substring(0,
							ContractConstants.ERR_MSG_LEN);
				}
				LOG.info("The errorMessage going to Action LOG TB " +errorMessage);

				// 4
				timeStamp = (Date) eachErrorObjectList.get(3);
				LOG.info("The timeStamp going to Action LOG TB " +timeStamp);

				// 5
				contractOwner = (String) eachErrorObjectList.get(4);
				LOG.info("The contractOwner going to Action LOG TB " +contractOwner);

				// 6
				errorCode = (String) eachErrorObjectList.get(5);
				LOG.info("The errorCode going to Action LOG TB " +errorCode);
				
				
				// Function called to dump all information to Database
				LOG.info("Calling the Utility Method to add the Error to Action Table");				
				ErrorUtility.addErrorToTable(ContractNumber, wizardId,
											errorMessage, timeStamp, 
											contractOwner, errorCode);
			}

			
		}
		
		LOG.info("Exit -->  runErrorDumpToDB Method");
	}
	
	
	
	//////////////////////////////////////////////////////////
	//                 ERROR HANDLING - END	               //
	//////////////////////////////////////////////////////////
	
	
	
	
	
	
	//////////////////////////////////////////////////////////
	//                 Populating Entities                  //
	//                    Specific							//
	//////////////////////////////////////////////////////////
	
	
	
	
	/**
	 * Helper method Called for all the Validation operations for Lines
	 * 
	 * @param exitContext
	 * @param requiredLinesConfiguration
	 * @param ErrorCode
	 * @param ErrorMessage
	 * @param isNextValueFlagChecked
	 * @return
	 */
	public boolean generalLineValidator(ExitContext exitContext, List<String> requiredLinesConfiguration, 
								String ErrorCode, String ErrorMessage, boolean isNextValueFlagChecked){
		
		if(null != requiredLinesConfiguration) {
			// 1-  Checking any null Value for the config Constants
			ArrayList<String> anyNullValuedConfigKeys = ConfigurationFilesUtil.areAllTheseConfigKeysDefinedWitValue( exitContext, requiredLinesConfiguration);
			if(anyNullValuedConfigKeys.isEmpty()){
				// This means --> All Correct
				LOG.debug("All the Config keys defined in Configuration file are having VALUE");
				isNextValueFlagChecked = true;
			} else {				
				for (String eachNullValuedConfigConstant : anyNullValuedConfigKeys) {
					// Log these information into DB
					LOG.error("The Value is NOT Available for Constant -> configuration Key" + eachNullValuedConfigConstant);
					//String formatErrorMessageToDB = ErrorUtility.createErrorMessage(MessagekeyConstants.MSG_CC_AM1, anyNullValuedConfigKeys, null);	

					String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, ErrorMessage , null, eachNullValuedConfigConstant);
					String errorCode = ErrorCode;						


					// Creation of Error object containing all the information need to be dumped to DB
					List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

					// Adding the resultant error object to List handled after the completion of business requirement
					errorListTODB.add(resultErrorObject);
				}	
				isNextValueFlagChecked = false;
			}
		} else {
			// This list null => ie. none is loaded			
			isNextValueFlagChecked = false;
		}	

		return isNextValueFlagChecked;
	}
	
	
	
	
	/**
	 * Helper method to check whether the lines are available in Contract Instance
	 * 
	 * @param exitContext
	 * @return
	 */
	public ArrayList<LineDefinition> ValidatingLinesAvailableInContractInstance(ExitContext exitContext) {
		boolean isAllValidation = false;
		// Fetch all the available Line Definitions in the Contract Instance
		ArrayList<LineDefinition> allLinesInContractInstance = getLineDefinitionIncontractInst(exitContext);	
		
		// Put those into ArrayList of Type String
		ArrayList<String> linedefinitionsInStringContractTemplate = new ArrayList<String>();
		for (LineDefinition eachlineDefinition : allLinesInContractInstance) {
			linedefinitionsInStringContractTemplate.add(eachlineDefinition.getName());
		}
		
		// Fetch all the available Line Definitions in the Configuration File
		ArrayList<String> allLinesInConfigFile = getAllLinesDefinitionsAvailableInConfig(exitContext);
		
		// Return List
		ArrayList<LineDefinition> emptyAvailableFinalLineDef = new ArrayList<LineDefinition>();
		
		
		// If configuration files have available Line Definitions
		if(allLinesInConfigFile != null) {
			if(linedefinitionsInStringContractTemplate.size() == 2) {
				for (String eachLineName : allLinesInConfigFile) {
					if(linedefinitionsInStringContractTemplate.contains(eachLineName)) {
					LOG.debug("This Line Definition is Available in Contract Instance" + eachLineName);
						//availableFinalLineDef.add(eachLineName);	
						isAllValidation = true;
					} else {
						outSourcedToConstructErrorMessage(exitContext);
						isAllValidation = false;
					}
				}				
			} else {
				outSourcedToConstructErrorMessage(exitContext);
				isAllValidation = false;
			}		
		}
		
		if(isAllValidation){
			return allLinesInContractInstance;
		} else{
			return emptyAvailableFinalLineDef;
		}		
	}
	
	
	
	

	
	/**
	 * Helper method to get all the Lines defined in the Configuration file
	 * @param exitContext
	 * @return
	 */
	private ArrayList<String> getAllLinesDefinitionsAvailableInConfig(ExitContext exitContext) {
		ArrayList<String> linesDefintionInConfigFile = new ArrayList<String>();
		
		try {
			// Line Definition name for Line_TOW in configuration file
			String lineTow = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW.trim());
			linesDefintionInConfigFile.add(lineTow);			
			
		} catch (ConfigurationException e) {
			LOG.error("ConfigurationException while accessing Line Definitions from Configuration File" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ConfigurationException_While_accessing LineDefinition_Tow");					
			String errorCode = ErrorCodes.PE_ER_EX;	

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			
		}	
		
		

		
		try {
			// Line Definition name for Line_OU_Dept in configuration file
			String lineDept = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT.trim());
			linesDefintionInConfigFile.add(lineDept);
		} catch (ConfigurationException e) {
			LOG.error("ConfigurationException while accessing Line Definitions from Configuration File" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ConfigurationException_While_accessing LineDefinition_OU_Dept");					
			String errorCode = ErrorCodes.PE_ER_EX;	

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return linesDefintionInConfigFile;
		}			
		
		
		return linesDefintionInConfigFile;
		
	}
	
	
	
	/**
	 * Get all the Line Definitions available from a contract Instance
	 * @param _contract
	 * @return
	 * @throws PluginException 
	 */
	private ArrayList<LineDefinition> getLineDefinitionIncontractInst(ExitContext exitContext) {
		LOG.info("Start of method ==> getLineDefinition for contract Instance");
		
		// List to store all the Line Definitions available in Contract Template
		ArrayList<LineDefinition> linedefinitionsLDInContractTemplate = new ArrayList<LineDefinition>();
	
		
		LineDefinitionSetImpl lineDefinitionSet = null;
		try {
			lineDefinitionSet = new LineDefinitionSetImpl(exitContext.getContract().getId());
			
			LineDefinitionIterator iterator = lineDefinitionSet.iterator();
			LineDefinition ld = null;
			while (iterator.hasNext()) {
				ld = iterator.next();
				LOG.debug(" Line definition " +ld.getName()+ "found in the contract");
				linedefinitionsLDInContractTemplate.add(ld);								
			}
			
		} catch (ContractException e){
			LOG.error("ContractException while accessing Line Definitions from Contract Template" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ContractException_WhileLineDefintion_Contract_Template");					
			String errorCode = ErrorCodes.PE_ER_EX;	

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return null;			
		}		
		LOG.info("End of method ==> getLineDefinition for contract Instance");
		// This will have lineDefintions names as defined in Configuration file
		return linedefinitionsLDInContractTemplate;
	}
	
	
	
	/**
	 * Helper method - To Construct a dynamic Error Message
	 * 
	 * @param exitContext
	 * @param DynamicContent
	 */
	private void outSourcedToConstructErrorMessage(ExitContext exitContext) {
		ArrayList<String> dynamicContentList = new ArrayList<String>();
		try {
			String lineTow = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW.trim());
			String lineDept = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT.trim());
			
			dynamicContentList.add(lineTow);
			dynamicContentList.add(lineDept);
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM3.trim(), dynamicContentList, null);					
			String errorCode = ErrorCodes.PE_AM_3;	

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
			
		} catch (ConfigurationException e) {
			LOG.error("ConfigurationException while accessing Line Definitions from Configuration File" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ConfigurationException_While_accessing LineDefinition");					
			String errorCode = ErrorCodes.PE_ER_EX;	

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}		

	}



}

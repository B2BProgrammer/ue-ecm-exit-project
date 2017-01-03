/**
 * 
 */
package com.emptoris.ecm.exit.condenast.util;

import java.util.Date;
import java.util.List;


import persistence.CONDENAST_ACTIONMESSAGE_TABLE;
import persistence.CONDENAST_ACTIONMESSAGE_TABLEPersister;


import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.CustomExitContext;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;

/**
 * Utility Class - For providing all activities around Error handlings
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ErrorUtility {	
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ErrorUtility.class);
	
	
	
	/**
	 * Error utility method - called from all the places in the code to send the error message to action LOG table
	 * 
	 * @param errorcode
	 * @param contractnumber
	 * @param wizardid
	 * @param errormessage
	 * @param date
	 * @param owner
	 */
	public static void addErrorToTable(int contractnumber,String wizardid ,String errormessage ,Date date ,String owner,String errorcode) {
		LOG.info("BEGIN --> Method to add error in message table");
		
		CONDENAST_ACTIONMESSAGE_TABLE errorRowVo = new CONDENAST_ACTIONMESSAGE_TABLE();
		errorRowVo.setCode(errorcode);
		errorRowVo.setContractnumber(contractnumber);
		errorRowVo.setWizardid(wizardid);
		errorRowVo.setMessage(errormessage);
		errorRowVo.setTimestamp(date);
		errorRowVo.setContractowner(owner);		
		
		LOG.info("Record going to be inserted into the Table");
		LOG.info("ErrorCode"     + errorcode + "&"    + "contractNumber" + contractnumber + "&" + "wizardId" +wizardid + "&"  +
				 "errorMessage"  +errormessage + "&"  +  "timeStamp"      +date           + "&" + "Owner"    +owner + "&" );
		
		CONDENAST_ACTIONMESSAGE_TABLEPersister actionTBPersister = CONDENAST_ACTIONMESSAGE_TABLEPersister.getInstance();
		
		try {	
			int recordInserted = actionTBPersister.create(errorRowVo);
			
			if (recordInserted == 1) {					
				LOG.debug("Insertion of record was successfull");
			} else {				
				LOG.debug("Insertion of record was UNSUCESSFULL");				
			}
			
		} catch(PersistenceException e)	{
			LOG.error("MSG_CNE_EXE ==>  Not Available or Check the Database Connection");
			LOG.error("Logging all the information to the LOG file, as database connection is failed");
			LOG.error("ErrorCode"     + errorcode + "&" +
					 "contractNumber" +contractnumber + "&" +
					 "wizardId"       +wizardid + "&"  +
					 "errorMessage"   +errormessage + "&"  +
					 "timeStamp"      +date + "&"  +
					 "Owner"          +owner + "&" );
			return;
			
		}
		
		LOG.info("END --> Method to add error in message table");
		
	}
	
	
	
	/**
	 * Helper method to throw Warning Exception
	 * 
	 * @param messageKey
	 * @param args
	 * @param ex
	 * @param exitContext 
	 * @throws PluginException
	 * @throws WarningMessageException
	 */
	public static void throwWarningExceptionOnUI(String messageKey, Object[] args,	Throwable ex, ExitContext exitContext) throws WarningMessageException  {
		try {
			CustomExitContext cec = CustomExitContext.getInstance();
			cec.throwWarningMessageException(messageKey, args, ex);
			
		} catch (ConfigurationException e) {
			LOG.error("Failed While throwing -> Warning Exception to UI" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ConfigurationException"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return;
		} 
	}
	
	
	
	
	
	
	
	
	/**
	 * Helper method to throw plugin exception
	 * 
	 * @param messageKey
	 * @param args
	 * @param ex
	 * @throws PluginException
	 */
	public static void throwPluginExceptionOnUI(String messageKey, Object[] args,	Throwable ex , ExitContext exitContext) throws PluginException  {
		try {
			CustomExitContext cec = CustomExitContext.getInstance();
			cec.throwPluginException(messageKey, args, ex);
		} catch (ConfigurationException e) {
			LOG.error("Failed While throwing -> Warning Exception to UI" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ConfigurationException"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return;
		}
	}
	




	/**
	 * Error utility method - called from all the places in the code to create the dynamic error message
	 * 
	 * @param exitContext
	 * @param messageConstantString
	 * @param passedArgs
	 * @param eachTermNotAvailableInECM 
	 * @return
	 */
	public static String createErrorMessage(ExitContext exitContext, String messageConstantString , List<String> passedArgs,	String singleParameter) {		
		LOG.info("START --> Method to createErrorMessage");		
		String formattedErrorMessage = null;	

		try{
			if( singleParameter != null){
				// If the single parameter is only passed

				CustomExitContext cec = CustomExitContext.getInstance();
				formattedErrorMessage = cec.formatMessage(messageConstantString, new  Object[] {singleParameter});		

			} else if(passedArgs != null){	
				// If the List is passed as part of argument
				Object[] parmaObject = new Object[passedArgs.size()];			

				int i = 0;
				for (String eachArg : passedArgs) {
					parmaObject[i] = eachArg;
					i++;
				}


				CustomExitContext cec = CustomExitContext.getInstance();
				formattedErrorMessage = cec.formatMessage(messageConstantString, parmaObject);
				//cec.throwPluginException(messageConstantString , new  Object[] {"TestReplacement"}, null );

			} else {
				// Static content pulled from message properties file
				CustomExitContext cec = CustomExitContext.getInstance();
				formattedErrorMessage = cec.formatMessage(messageConstantString, null);
			}

		} catch (ConfigurationException e) {		
			LOG.error("Failed to initiate the instance of CustomExitContext:" + e.getLocalizedMessage());
			String errorMessage = MessagekeyConstants.MSG_CNE_EXE.trim(); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);			
		}
		
		LOG.info("END --> Method to createErrorMessage");
		return formattedErrorMessage;
	}
	
	
	
	/**
	 * @param exitContext
	 * @param errorMessage
	 * @param errorCode
	 */
	public static void callToConstructMessageToDB(ExitContext exitContext, String errorMessage, String errorCode ) {
		
		LOG.info("Enter --> Creation of callToConstructMessageToDB Method");
		
		//1 - Column
		int ContractNumber = exitContext.getContract().getNumber();
		
		//2 - Column
		String wizardId = exitContext.getWizardId();		
		
		//3 - Column - ErrorMessage
		
		
		//4	 - Column		
		Date timeStamp = new Date();
		
		
		//5 - Column
		String contractOwner = GeneralUtility.getPrimaryContactName(exitContext);
		//String contractOwner = GeneralUtility.getPrimaryPartyName(exitContext);
		//String contractOwner = GeneralUtility.getUser(exitContext);
		
		
		//6 - Column - errorCode
		
		
		// Function called to dump all information to Database
		ErrorUtility.addErrorToTable(ContractNumber, wizardId, errorMessage, timeStamp, contractOwner, errorCode);
		
		LOG.info("Exit --> Creation of callToConstructMessageToDB Method");	
		
		
	}
	


}

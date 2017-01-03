/**
 * 
 */
package com.emptoris.ecm.exit.condenast.implone;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.AxisFault;

import com.cn.dsa.emptoris.ContractNotification;
import com.cn.dsa.emptoris.MissingDataFault;

import com.cn.dsa.emptoris.NewContractWs;
import com.cn.dsa.emptoris.NotificationFailureFault;
import com.cn.dsa.emptoris.NotificationResponse;
import com.cn.dsa.emptoris.service.NewContractNotificationLocator;
import com.dicarta.appfound.common.CreationEntryPoint;
import com.dicarta.appfound.common.IDraftable;
import com.dicarta.appfound.contract.server.bo.ContractBO;
import com.dicarta.appfound.contractmanager.btv.common.BTVException;
import com.dicarta.appfound.contractmanager.btv.server.bo.BTVMgrBO;
import com.dicarta.appfound.contractmanager.btv.server.bo.VariableBO;
import com.dicarta.appfound.contractmanager.btv.server.persistence.VariableData;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.api.intf.Contract;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.Exit;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;

/**
 * Business Implementation class for - Contract Creation Webservice notification to Peoplesoft system
 * ctsCallType will be deleted at any cases while exiting this requirement 
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractCreationNotification extends CustomUserExitBase {
	
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ContractCreationNotification.class);
	
	// class level variable to store all the required terms for BIC implementation
	private List<String> requiredConstants = new ArrayList<String>();
	private List<String> requiredTerms = new ArrayList<String>();
		
	
	/**
	 * Constructor where - all required configurations are preloaded to test validation
	 * @param exitContext
	 */
	public ContractCreationNotification(ExitContext exitContext) {
		super(exitContext);		
		LOG.info("Start --> Constructor of ContractCreationNotification");
		
		// required constants for this BIC Implementation
		requiredConstants.add(ConfigurationConstants.CNE_WS_ENDPT);
		requiredConstants.add(ConfigurationConstants.CNE_WS_TIMOT);
		
		// required Terms for this BIC Implementation
		requiredTerms.add(ConfigurationConstants.CNE_RIG_CODE);		
		requiredTerms.add(ConfigurationConstants.CNE_CTS_CALL);
		requiredTerms.add(ConfigurationConstants.CNE_PAY_STYL);	
		
		LOG.info("End --> Constructor of ContractCreationNotification");
	}

	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {		
		return EnabledFlags.CNE_REQ_1_FLAG.trim();
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
		return MessagekeyConstants.MSG_CC_AM1.trim();
	}

	@Override
	protected String getMessageTermNotAvailInECM() {		
		return MessagekeyConstants.MSG_CC_AM2.trim();
	}
	
	
	@Override
	protected String getErrorCodeForConfigValueNotAvail() {		
		return ErrorCodes.CCN_AM_1;
	}
	
	
	@Override
	protected String getErrorCodeForTermNotAvailInECM() {
		return ErrorCodes.CCN_AM_2;
	}
	
	
	

	
	/**Helper method called to check the Flag is Enabled or Not
	 * @return
	 */
	public boolean checkFlag() {
		LOG.info("Starting --> Boolean Flag check of ContractCreationNotification");
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
		LOG.info("Starting --> isAllPreConditionsSatisfied of ContractCreationNotification" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}
	
	

	/**
	 * Method called from the Handler class - to do all the flow operations 
	 * 
	 * @param exitContext
	 * @throws PluginException 
	 */
	public void executeLogic(ExitContext exitContext)  {
		LOG.info("Enter --> Execution of Core Logic for ContractCreationNotification" + exitContext.getContract().getNumber());

		// ctSCallTerm To be deleted from template after the contract Notification
		String ctsCallTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_CTS_CALL.trim());
		

		//contractCreatedChecker - Needed to take decisions according to contract creation process
		CreationEntryPoint contractCreatedChecker = Exit.creationEntryPoint.get();

		if(contractCreatedChecker != null){
			if( contractCreatedChecker.equals(CreationEntryPoint.BLU)){
				//Contract created using a BLU process
				LOG.info("Contract is Created from BLU - Don't proceed with ContractNotification to PeopleSoft");
				
				if(ContractTermsUtil.isTermAvailableInContract(exitContext, ctsCallTermName)){
					// Call the method to delete the CTS callType BTV
					deleteCTSCallTypeBTV(ctsCallTermName,exitContext);
				} else {
					LOG.info("Term" +ctsCallTermName+ " Not available in Contract instance");
				}
				
			} else if(contractCreatedChecker.equals(CreationEntryPoint.WEBSERVICE)) {
				// Contract created using a Webservice process				
				LOG.info("Contract is Created from WebService - Don't proceed with ContractNotification to PeopleSoft");
				
				if(ContractTermsUtil.isTermAvailableInContract(exitContext, ctsCallTermName)){
					// Call the method to delete the CTS callType BTV
					deleteCTSCallTypeBTV(ctsCallTermName,exitContext);
				} else {
					LOG.info("Term" +ctsCallTermName+ " Not available in Contract instance");
				}
				
			} else {
				// Contract created from UI		
				LOG.info("Contract created using UI & using Template");
				collectAllrequiredValuesProceedNotification(exitContext);
			}				
		} else {
			// Contract created from SAVE-AS logic
			LOG.info("Contract created using SaveAs from Older Contract -PreContractCreate - Not triggered");
			collectAllrequiredValuesProceedNotification(exitContext);
		}
		
		LOG.info("Exit --> Execution of Core Logic for ContractCreationNotification" + exitContext.getContract().getNumber());
	}
	
	
	
	
	

	/**Helper Method - 
	 * Used to fetch all the values required for Contract Creation 
	 * & Notification to Peoplesoft system
	 *  
	 * @param exitContext
	 * @throws PluginException 
	 */
	private void collectAllrequiredValuesProceedNotification(ExitContext exitContext) {
		LOG.info("Enter --> Execution of Method - collectAllrequiredValuesProceedNotification");
		try {
			// Get the new exitContext from the new session, as you would have added new terms into the contract instance
			exitContext =  new com.emptoris.ecm.api.impl.ExitContextImpl(
					(com.dicarta.appfound.contract.ContractData) ContractBO.getContractInstance().getContract(exitContext.getContractId(), IDraftable.DRAFT_REVISION),
					(com.dicarta.appfound.common.IUserInfo) exitContext.getInternalUserObject(),
					com.emptoris.ecm.api.intf.ExitContext.ExitTypes.POST_CREATE);


			//1 - Term - CtsCallType
			LOG.info("Begin --> ctsCallTermName Operations");		
			String ctsCallTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_CTS_CALL.trim());
			String ctsCallTermValue = "";
			if(ContractTermsUtil.isTermAvailableInContract(exitContext, ctsCallTermName)){
				ctsCallTermValue = (String) ContractTermsUtil.getTermValue(exitContext, ctsCallTermName);			

				if( null != ctsCallTermValue && !ctsCallTermValue.trim().isEmpty()){
					LOG.debug("The Value is Available for term" + ctsCallTermName);
					if(ctsCallTermValue.equalsIgnoreCase("ExceptionToSkipFromRequirment")){
						// If any error LOG CCN_EM_3						
						LOG.error("CC_AM_3 ==> Contract Creation requirement Bypassed - due to Error while retrieving Term " +ctsCallTermName);

						// Error Code & Error message for DB to dump ==> CC_EM_3	
						String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM3.trim(), null, "ContractException");
						String errorCode = ErrorCodes.CCN_AM_3;

						LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						ctsCallTermValue = "";
					} else {
						LOG.debug("The Value provided for term is Correct value" +ctsCallTermValue);
					}
				} else {
					ctsCallTermValue = "";
					LOG.debug("The Value is NOT Available for term" + ctsCallTermValue);				
				}
			} else {
				ctsCallTermValue = "";
				LOG.debug("This Term is Not Available in Contract Instance : " + ctsCallTermName);
			}
			LOG.info("End --> ctsCallTermName Operations: TermValue" +ctsCallTermValue);
			LOG.info("ctsCallTermName Value : " +ctsCallTermValue);	




			//2- Term - RightsCode
			LOG.info("Begin --> rightsCodeTermName Operations");		
			String rightsCodeTermValue = "";
			String rightsCodeTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_RIG_CODE.trim());
			if(ContractTermsUtil.isTermAvailableInContract(exitContext, rightsCodeTermName)){
				rightsCodeTermValue =  (String) ContractTermsUtil.getTermValue(exitContext, rightsCodeTermName);	

				if( null != rightsCodeTermValue && !rightsCodeTermValue.trim().isEmpty()){
					LOG.debug("The Value is Available for term" + rightsCodeTermName);
					if(rightsCodeTermValue.equalsIgnoreCase("ExceptionToSkipFromRequirment")){
						// If any error LOG CCN_EM_4					
						LOG.error("CC_AM_4 ==> Contract Creation requirement Bypassed - due to Error while retrieving Term " +rightsCodeTermName);
						// Error Code & Error message for DB to dump ==> CC_EM_4					
						String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM4.trim(), null, "ContractException");
						String errorCode = ErrorCodes.CCN_AM_4;

						LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						rightsCodeTermValue = "";
					} else{
						LOG.debug("The Value provided for term is Correct value" +rightsCodeTermValue);
					}
				} else {
					rightsCodeTermValue = "";
					LOG.debug("The Value is NOT Available for term" + rightsCodeTermName);
				}
			} else {			
				rightsCodeTermValue = "";
				LOG.debug("This Term is Not Available in Contract Instance : " + rightsCodeTermName);
			}
			LOG.info("End --> rightsCodeTermName Operations: TermValue" +rightsCodeTermValue);
			LOG.info("rightsCodeTermName Value : " +rightsCodeTermValue);	




			//3-  Term - paymentStyle
			LOG.info("Begin --> paymentStyleTermName Operations");		
			String paymentStyleTermValue = "";
			String paymentStyleTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_PAY_STYL.trim());
			if(ContractTermsUtil.isTermAvailableInContract(exitContext, paymentStyleTermName)){
				paymentStyleTermValue = (String) ContractTermsUtil.getTermValue(exitContext, paymentStyleTermName);
				// If any error LOG CCN_EM_5
				if( null != paymentStyleTermValue && !paymentStyleTermValue.trim().isEmpty()){
					LOG.debug("The Value is Available for term" + paymentStyleTermName);
					if(paymentStyleTermValue.equalsIgnoreCase("ExceptionToSkipFromRequirment")){
						// If any error LOG CCN_EM_5

						LOG.error("CC_AM_5 ==> Contract Creation requirement Bypassed - due to Error while retrieving Term " +paymentStyleTermName);
						// Error Code & Error message for DB to dump ==> CC_EM_5					
						String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM5.trim(), null, "ContractException");					
						String errorCode = ErrorCodes.CCN_AM_5;

						LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						paymentStyleTermValue = " ";
					} else {
						LOG.debug("The Value is NOT Available for term" + paymentStyleTermValue);
					}
				} else {
					paymentStyleTermValue = " ";
					LOG.debug("The Value is NOT Available for term" + paymentStyleTermName);
				}
			} else {		
				paymentStyleTermValue = " ";
				LOG.debug("This Term is Not Available in Contract Instance : " + paymentStyleTermName);
			}
			LOG.info("End --> paymentStyleTermName Operations : TermValue" +paymentStyleTermValue);	
			LOG.info("paymentStyleTermName Value : " +paymentStyleTermValue);	



			//4- Effective Start Date
			Date effectivestartdate = exitContext.getContract().getEffectiveStartDate();	
			LOG.info("effectivestartdate Value : " +effectivestartdate);	

			//5 - Effective End Date
			Date effectiveenddate = exitContext.getContract().getEffectiveEndDate();		
			LOG.info("effectiveenddate Value : " +effectiveenddate);	

			// 6 - Fetching ContractNumber
			String contractNumber = String.valueOf(exitContext.getContract().getNumber());		
			LOG.info("contractNumber Value : " +contractNumber);


			// 7 - Wizardid 
			String wizardid = exitContext.getWizardId();
			LOG.info("wizardid Value : " +wizardid);

			// 8 -Status
			String status = exitContext.getContract().getStatusKey();
			LOG.info("status Value : " +status);

			// 9 - host URL		
			String hostUrl = null;
			try {					
				hostUrl = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_WS_ENDPT.trim());			
			} catch (ConfigurationException e) {
				// This case is catched earlier only - But still adding for more safety
				LOG.error("Configuration Exception while accessing HOST URL" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "HostURLConfigurationException"); 
				String errorCode = ErrorCodes.CCN_ER_EX;

				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return;
			}
			LOG.info("hostUrl Value : " +hostUrl);



			// 10 - webService TimeOut Value
			String wsTimeOutString = null;
			try {			
				wsTimeOutString = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_WS_TIMOT.trim());			
			} catch (ConfigurationException e) {
				// This case is catched earlier only - But still adding for more safety
				LOG.error("Configuration Exception while accessing webService Time Out Value URL" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "WebseviceTimeOutConfigurationException"); 					;
				String errorCode = ErrorCodes.CCN_ER_EX;

				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);
				return;			
			}
			LOG.info("wsTimeOut Value : " +wsTimeOutString);


			LOG.info("All Values required for ContractNotification Fetched - calling websServiceNotification");			

			//calling the people soft web service
			contractNotificationWebServiceInvocation(contractNumber, wizardid, ctsCallTermValue,
					paymentStyleTermValue,rightsCodeTermValue,status,effectiveenddate,
					effectivestartdate, exitContext.getContract() , hostUrl , wsTimeOutString,
					exitContext);


		} catch (PluginException e) {			
			LOG.error("Plugin Exception while accessing new ExitContext object as new terms may be added" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PluginException"); 					;
			String errorCode = ErrorCodes.CCN_ER_EX;

			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;			
		} catch (ObjectNotFoundException e) {
			LOG.error("ObjectNotFoundException  while accessing new ExitContext object as new terms may be added" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException"); 					;
			String errorCode = ErrorCodes.CCN_ER_EX;

			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;			
		}

		LOG.info("Exit --> Execution of Method - collectAllrequiredValuesProceedNotification");

	}
	
	
	
	
	

	/**
	 * Helper Method - used to Call Webservice & Handle all the operations
	 * 
	 * @param contractNumber
	 * @param wizardid
	 * @param ctsCallTermValue
	 * @param paymentStyleTermValue
	 * @param rightsCodeTermValue
	 * @param status
	 * @param effectiveenddate
	 * @param effectivestartdate
	 * @param contract
	 * @param hostUrl 
	 * @param wsTimeOutString 
	 * @param exitContext 
	 * @throws PluginException 
	 */
	private void contractNotificationWebServiceInvocation(
			String contractNumber, String wizardid, String ctsCallTermValue,
			String paymentStyleTermValue, String rightsCodeTermValue,
			String status, Date effectiveenddate,
			Date effectivestartdate, Contract contract, String hostUrl, String wsTimeOutString, ExitContext exitContext)  {
		
		LOG.info("Enter --> Execution of Method - contractNotificationWebServiceInvocation");
		
		
		LOG.info("List Preparation to Send the data to the Action Log Table, if Notification Fails"); 
		String formattedESD = "";
		if(effectivestartdate != null){
			formattedESD = effectivestartdate.toString();
		} else {
			 formattedESD = "";
		}
		
		String formattedEED = "";
		if(effectivestartdate != null){
			formattedEED = effectivestartdate.toString();
		} else {
			formattedEED = "";
		}
		
		
		List<String> contentListToSendDB = new ArrayList<String>();		
		contentListToSendDB.add(contractNumber);
		contentListToSendDB.add(wizardid);
		contentListToSendDB.add(ctsCallTermValue);
		contentListToSendDB.add(rightsCodeTermValue);
		contentListToSendDB.add(paymentStyleTermValue);
		contentListToSendDB.add(formattedESD);
		contentListToSendDB.add(formattedEED);
		contentListToSendDB.add(status);
		
		
		try {
			ContractNotification contractnotification  = new ContractNotification();
			
			// Setting the Contract Notification Fields
			contractnotification.setCtsCallType(ctsCallTermValue);
			contractnotification.setRightsCode(rightsCodeTermValue);
			contractnotification.setPaymentStyle(paymentStyleTermValue);
			contractnotification.setEndDate(effectiveenddate);
			contractnotification.setStartDate(effectivestartdate);
			contractnotification.setStatus(status);
			contractnotification.setContractNumber(contractNumber);
			contractnotification.setWizardId(wizardid);	
					
			//New fields added in new provided WSDL
			contractnotification.setContractId(exitContext.getContract().getId());
			contractnotification.setContractName(exitContext.getContract().getName());
			contractnotification.setContractClass(exitContext.getContract().getContractClass());
			
			// Commented all the UNDECIDED fields of WSDL
		   /*						
			//psusername
			contractnotification.setContractName(exitContext.getContract().getName());
			//dualrights
			//single
			//contractnotification.setCreatedOn(exitContext.getContract().getCreationDate().toString());			
			//unavailSelect
		   */		
			
			
			NewContractNotificationLocator service = new NewContractNotificationLocator();
			NewContractWs newContractWs = service.getnewContractNotification(new URL(hostUrl.trim()));
			
			Stub stub = (Stub)newContractWs;
			stub._setProperty(org.apache.axis.client.Call.CONNECTION_TIMEOUT_PROPERTY, Integer.parseInt(wsTimeOutString));
			
			//Sending Notification to Peoplesoft.
			NotificationResponse notificationResponse = newContractWs.newContractNotification(contractnotification);				
			LOG.debug("Web Service Response Message " + notificationResponse.getResponseMsg());
			
		} catch (MalformedURLException e) {		
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to MalformedURLException " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> CC_EM_6
			contentListToSendDB.add("MalformedURLException");
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB, null);			
			String errorCode = ErrorCodes.CCN_AM_6;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			

		} catch (ServiceException e) {
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to ServiceException " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> CC_EM_6
			contentListToSendDB.add("ServiceException");
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB , null);			
			String errorCode = ErrorCodes.CCN_AM_6;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			

		} catch (NotificationFailureFault e) {
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to NotificationFailureFault " + e.getLocalizedMessage());		
			// Error Code & Error message for DB to dump ==> CC_EM_6
			contentListToSendDB.add("NotificationFailureFault");
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB , null);			
			String errorCode = ErrorCodes.CCN_AM_6;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			
			
		} catch (MissingDataFault e) {
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to MissingDataFault " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> CC_EM_6
			contentListToSendDB.add("MissingDataFault");
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB , null);			
			String errorCode = ErrorCodes.CCN_AM_6;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			

		} catch (AxisFault e) {
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to AxisFault " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> CC_EM_6				
			contentListToSendDB.add("AxisFault; Axis Fault Reason : " +e.getFaultReason());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB , null);			
			String errorCode = ErrorCodes.CCN_AM_6;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			

		} catch (RemoteException e) {
			LOG.error("CC_EM_6 ==> Contract Creation requirement Bypassed - due to RemoteException " + e.getLocalizedMessage());
			// Error Code & Error message for DB to dump ==> CC_EM_6
			contentListToSendDB.add("RemoteException");
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM6.trim(), contentListToSendDB , null);			
			String errorCode = ErrorCodes.CCN_AM_6;			
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
		}
		
		// If below are executed at any situations
		String ctsCallTermName = ContractTermsUtil.getTermName(exitContext, ConfigurationConstants.CNE_CTS_CALL.trim());		
		if(ContractTermsUtil.isTermAvailableInContract(exitContext, ctsCallTermName)){
			// Call the method to delete the CTS callType BTV
			deleteCTSCallTypeBTV(ctsCallTermName,exitContext);
		} else {
			LOG.info("Term" +ctsCallTermName+ " Not available in Contract instance");
		}
		
		
		LOG.info("Exit --> Execution of Method - contractNotificationWebServiceInvocation");

	}	
	
	
	
	/**
	 * This will remove the ctsCallType BTV from the terms
	 * @param ctsCallTermName
	 * @param exitContext
	 */
	private void deleteCTSCallTypeBTV(String ctsCallTermName, ExitContext exitContext) {
		LOG.info("Start --> Execution of Method - deleteCTSCallTypeBTV");
		try {
			BTVMgrBO bo = BTVMgrBO.getInstance();			
			VariableData variableData = VariableBO.getInstance().getVariableByInternalName(ctsCallTermName);
			String variableid = variableData.getVariableId();	
			
			// This will remove the ctsCallType BTV from the terms
			bo.removeContractBTV(exitContext.getContractId(), variableid);
			LOG.info("Deleted the Term " + ctsCallTermName );
			
		} catch (ObjectNotFoundException e) {
			// If any error LOG CCN_AM_7			
			LOG.error("CC_AM_7 ==> Contract Creation requirement Bypassed - due to Error while deleting the  Term " +ctsCallTermName);
			// Error Code & Error message for DB to dump ==> CC_AM_7					
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM7.trim(), null, "ObjectNotFoundException");					
			String errorCode = ErrorCodes.CCN_AM_7;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} catch (BTVException e) {
			// If any error LOG CCN_AM_7			
			LOG.error("CC_AM_7 ==> Contract Creation requirement Bypassed - due to Error while deleting the  Term " +ctsCallTermName);
			// Error Code & Error message for DB to dump ==> CC_AM_7					
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CC_AM7.trim(), null, "BTVException");					
			String errorCode = ErrorCodes.CCN_AM_7;
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}			
			
		
		
		LOG.info("End --> Execution of Method - deleteCTSCallTypeBTV");
	}	
	
}

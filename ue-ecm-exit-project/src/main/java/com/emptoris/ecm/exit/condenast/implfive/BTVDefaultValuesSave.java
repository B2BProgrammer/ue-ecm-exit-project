/**
 * 
 */
package com.emptoris.ecm.exit.condenast.implfive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import persistence.CONDENASTBTVDEFAULTVALUES;
import persistence.CONDENAST_BTV_DEFAULTVALUESPersister;
import persistence.SeqBTVIterator;

import com.dicarta.appfound.common.IDraftable;
import com.dicarta.appfound.contract.NegotiatorException;
import com.dicarta.appfound.contract.server.bo.ContractBO;
import com.dicarta.appfound.contractmanager.btv.common.BTVException;
import com.dicarta.appfound.contractmanager.btv.common.ContractBTVData;
import com.dicarta.appfound.contractmanager.btv.server.bo.BTVMgrBO;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.emptoris.ecm.api.Helper.ContractHelper;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.exception.EcmApiException;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.constants.ContractConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ContractTemplateUtility;
import com.emptoris.ecm.exit.condenast.util.ContractTermsUtil;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;
import com.emptoris.ecm.exit.condenast.util.GeneralUtility;


/**
 * Business Implementation class for - BTV Default Values of Master & Internal Terms
 * While on PRE Contract SAVE Process
 * 
 * This will copy any new edited/updated value from Internal Term value to Master Term
 * Value
 * 
 * @author Ajith.Ajjarani
 *
 */
public class BTVDefaultValuesSave extends CustomUserExitBase {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(BTVDefaultValuesSave.class);

	/**
	 * Constructor, where all required Terms & constants for BIC are initialized
	 * @param exitContext
	 */
	public BTVDefaultValuesSave(ExitContext exitContext) {
		super(exitContext);
		LOG.info("Start the Constructor BTVDefaultValuesSave" + exitContext.getContract().getNumber());

		LOG.info("End the Constructor BTVDefaultValuesSave" + exitContext.getContract().getNumber());

	}
	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_5_2_FLAG.trim();
	}

	@Override
	protected List<String> getRequiredTermsConfigKeys() {		
		return null;
	}

	@Override
	protected List<String> getRequiredConfigConstants() {		
		return null;
	}

	@Override
	protected String getMessageConfigValueNotAvail() {		
		return null;
	}

	@Override
	protected String getMessageTermNotAvailInECM() {		
		return null;
	}

	@Override
	protected String getErrorCodeForConfigValueNotAvail() {		
		return null;
	}

	@Override
	protected String getErrorCodeForTermNotAvailInECM() {		
		return null;
	}

	/**
	 * Helper method called to check the Flag is Enabled or Not
	 * @return
	 */
	public boolean checkFlag() {
		LOG.info("Starting --> Boolean Flag check of BTVDefaultValuesSave");
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
		LOG.info("Starting --> isAllPreConditionsSatisfied of BTVDefaultValuesSave" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}


	/**
	 * Public method - accessed from Handler Class, which decides the flow operations
	 * @param exitContext
	 * @throws PluginException 
	 * @throws WarningMessageException 
	 */
	public void executeLogic(ExitContext exitContext) throws PluginException, WarningMessageException {		
		LOG.info("Enter --> Execution of Core Logic for BTVDefaultValuesSave" + exitContext.getContract().getNumber());
		//Calling the method to fetch Template Name & Owning Organization
		fetchTempNamOwingOrgNCallDB(exitContext);	
		LOG.info("Enter --> Execution of Core Logic for BTVDefaultValuesSave" + exitContext.getContract().getNumber());
	}
	
	
	/**
	 * Helper method - which fetches Template Name & Owning Organization & handles any exception relevant for these 
	 * & next following flow of operations are carried out to fetch information from Database
	 * 
	 * @param exitContext
	 * @throws PluginException 
	 * @throws WarningMessageException 
	 */
	private void fetchTempNamOwingOrgNCallDB(ExitContext exitContext) throws PluginException, WarningMessageException {
		LOG.info("Enter --> Method  fetchTempNamOwingOrgNCallDB");

		//To Store the returned value from the database for supplied TEMPLATE NAME & OWING ORGANIZATION
		ArrayList<CONDENASTBTVDEFAULTVALUES> dbReturnedValue = new ArrayList<CONDENASTBTVDEFAULTVALUES>();


		try {
			// Template Name
			String templateName = exitContext.getContract().getTemplate().getName();
			LOG.info("The Retrieved Template Name of the contract" +templateName );

			// Owning Organization
			String owningOrganizationName = exitContext.getContract().getOwningOrganization().getName();
			LOG.info("The Retrieved OWNING Organization of the contract" +owningOrganizationName );

			if(templateName != null && owningOrganizationName != null 
					|| !(templateName.isEmpty() && owningOrganizationName.isEmpty())){
				// Call the method - do operation for getting INTERNAL & MASTER TERM Names & Default values	
				dbReturnedValue = dbOperation(templateName,owningOrganizationName , exitContext);
			}

			if(dbReturnedValue != null){
				// call the method - which loop through all the fetched Master Terms & add BTV default values to Contract	
				coreLogic(dbReturnedValue , templateName , owningOrganizationName , exitContext);
			}			

		} catch (ContractException e) {
			LOG.error("BTV Default Values requirement 5 ================================== SKIPPED");
			LOG.error("ContractException exception while accesing the Template Name or Owning Organization - Contract Number :"
					+ exitContext.getContract().getNumber());		

			// Error Code & Error message for DB to dump  => BTV_ER_EX
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException_While_Template_Name_OwningOrganization");
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			// return this method - no more proceeding with this requirement
			return ;
		} 	
	}
	
	


	/**
	 * DB Operation to Fetch the information from TB -
	 * CONDENAST_BTV_DEFAULTVALUES
	 * 
	 * 
	 * @param templatename
	 * @param owningorganization
	 * @param exitContext
	 * @return
	 */
	private ArrayList<CONDENASTBTVDEFAULTVALUES> dbOperation(String templateName, String owningOrganizationName, ExitContext exitContext) {
		LOG.info("Start of method ==> dbOperation");
		
		CONDENASTBTVDEFAULTVALUES condenastbtvdefault = new CONDENASTBTVDEFAULTVALUES();
		
		// Creation of Value object having - Template Name & Owning Organization
		condenastbtvdefault.setTemplatename(templateName.trim());
		condenastbtvdefault.setOwningorganizationname(owningOrganizationName.trim());
		
		CONDENAST_BTV_DEFAULTVALUESPersister persister = CONDENAST_BTV_DEFAULTVALUESPersister.getInstance();
		SeqBTVIterator seqbtviterator = null;
		
		ArrayList<CONDENASTBTVDEFAULTVALUES> dbValue = new ArrayList<CONDENASTBTVDEFAULTVALUES>();
		
		try {
			seqbtviterator = (SeqBTVIterator) persister.getBTVSeq(condenastbtvdefault);
			seqbtviterator.prefetch();
			if(seqbtviterator.prefetch().isEmpty()){
				LOG.error("CondeNast_BTV_DefaultValues -> No matching data found for TemplateName - " 
							+ templateName + " Owning Organization - " +owningOrganizationName);				
			} else {
				dbValue = seqbtviterator.prefetch();
				LOG.info("The DB Value was retrieved from TB CONDENAST_BTV_DEFAULTVALUES ");
			}	
		} catch(PersistenceException e) {
			LOG.error("PersistenceException Exception while retrieving data from TB - CONDENAST_BTV_DEFAULTVALUES" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_TB_CONDENAST_BTV_DEFAULTVALUES"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return null;			
		}
		
		LOG.info("End of method ==> dbOperation");
		return dbValue;			
	}
	
	
	

	/**
	 * Helper method - Loop through each row of returned data from the database & do the following
	 * 1) Copy the new added/edited value from INTERNAL/CHILD Term to MASTER Term & set as its new value
	 * 
	 * 
	 * into the contract instance
	 * 
	 * @param btvInContracts
	 * @param btvDefaultValueData
	 * @param templatename
	 * @param owningorgname
	 * @param exitContext
	 * @throws PluginException 
	 * @throws WarningMessageException 
	 */
	private void coreLogic(ArrayList<CONDENASTBTVDEFAULTVALUES> btvDefaultValueData,	
			String templateName, String owningOrganizationName,	ExitContext exitContext) throws PluginException, WarningMessageException {
		LOG.info("Start of method ==> coreLogic");		
		
		//Initializing the Master & Child Term Names
		String masterTermName = "";
		String childTermName = "";
		
		//Initializing the Master & Child Term DataTypes
		String childTermDataType = "";
		String masterTermDataType = "";
		
		
		if(btvDefaultValueData != null && !btvDefaultValueData.isEmpty()) {
			// Loop through each ROW of data from the returned LIST of Database
			for (CONDENASTBTVDEFAULTVALUES cdata : btvDefaultValueData) {
				
				// MASTER & CHILD term names as provided in Database
				masterTermName = cdata.getMasterlovterm();				
				childTermName =  cdata.getTerminternalname();
				LOG.info("Master Term Name from Database : "+masterTermName);
				LOG.info("Child Term Name from Database : "+childTermName);
				
				// Operations pertaining the child & master term label names
				String childTermLabel = "";
				String masterTermLabel = "";
				ArrayList<String> finalLabelNams = getTermLabelsUsed(exitContext, masterTermName , childTermName);
				if(finalLabelNams != null && !finalLabelNams.isEmpty() && finalLabelNams.size() == 2){				  
					childTermLabel = finalLabelNams.get(0);
					masterTermLabel = finalLabelNams.get(1);
				} else {
					// problem in retrieving the label name's as some may not be configured
					childTermLabel = childTermName;
					masterTermLabel = masterTermName;
				}
				

				// DataTypes of CHILD & MASTER Term
				childTermDataType = ContractTermsUtil.getTermDataType(exitContext, childTermName);
				masterTermDataType = ContractTermsUtil.getTermDataType(exitContext, masterTermName);
				LOG.info("Child Term DataType : "+childTermDataType + "Master Term DataType : "+masterTermDataType );				
					

				// Checking whether the MASTER term is available in the Template
				if(ContractTermsUtil.isTermAvailableInContract(exitContext, masterTermName)){
					LOG.info("Master Term : " +masterTermName+ " is available in a Contract");
					
					// checking whether the CHILD term is available in the Template
					if(ContractTermsUtil.isTermAvailableInContract(exitContext, childTermName)){
						LOG.info("BTV Term :" +childTermName+ " is Available in the contract");
						
						// If Child Term DataType & Master Term DataType are same
						if(childTermDataType.equalsIgnoreCase(masterTermDataType)){
							LOG.info("Child Term Type & Master Term Type are Same");
							
							// Retrieving the STATUS of the CONTRACT
							if(GeneralUtility.isContractStatus(exitContext)){	
								// ACTIVE,EXECUTE,TERMINATED,EXPIRED section
								LOG.info("Stauts of Contact is ACTIVE -EXECUTED Or TERMINATED");
								// Below line of code is commented - used if decided that update of master term is never allowed
								//checkEarlierBTVExisted(exitContext, childTermName , masterTermName,childTermLabel, masterTermLabel);				
								
								if(ContractTermsUtil.isThisLanguageTerm(exitContext,masterTermName)){
									// This is Language Term 										
									activitesRelatedToLanguageTerm(exitContext, childTermName , masterTermName,childTermLabel, masterTermLabel , btvDefaultValueData);									
								} else {
									// This is not a language Term - therefore update of master term value is needed
									// when child term value is changed
									if(ContractTermsUtil.isBTVValueChanged(exitContext, childTermName)){
										masterTermValueUpdate(exitContext,templateName,owningOrganizationName,
																masterTermName,childTermName , masterTermDataType);
									}
								}
							} else {
								// Other part [Draft Status]
								if(ContractTermsUtil.isBTVValueChanged(exitContext, childTermName)){
									masterTermValueUpdate(exitContext,templateName,owningOrganizationName,
														masterTermName,childTermName , masterTermDataType);
								}
							}									
						} else {														
							// UI Error BTV_UIE_3			
							ErrorUtility.throwPluginExceptionOnUI(MessagekeyConstants.MSG_BTV_UIE3.trim(), new Object[]{}, null, exitContext);
						}

					} else {
						// UI Error BTV_UIE_1					
						ErrorUtility.throwPluginExceptionOnUI(MessagekeyConstants.MSG_BTV_UIE1.trim(), new Object[]{childTermLabel}, null, exitContext);
						return;
					}
				} else {
					LOG.info("Master Term : " +masterTermName+ " is NOT available in a Contract");
				}

			}
		} else {
			LOG.debug("The database object is NULL");
		}
	} 
		
	
	
	
		
	/**
	 * Helper method - Used to generate the term's label name
	 * @param exitContext
	 * @param masterTermName
	 * @param childTermName
	 * @return
	 */
	private ArrayList<String> getTermLabelsUsed(ExitContext exitContext,	String masterTermName, String childTermName) {
		
		ArrayList<String> finalLabelNams = new ArrayList<String>();
		
		if(ContractTermsUtil.isTermAvailableInECM(childTermName, exitContext)){
			// Used to fetch the child Term label Name
			String childTermLabel = ContractTemplateUtility.getTermLabelName(exitContext, childTermName);
			finalLabelNams.add(childTermLabel);
			LOG.info("The child Term Label Name" +childTermLabel);	
			
		} else {
			LOG.info("The child Term " +childTermName + "is notAvailable");	
		}
		
		
		if(ContractTermsUtil.isTermAvailableInECM(masterTermName, exitContext)){
			// Used to fetch the child Term label Name
			String masterTermLabel = ContractTemplateUtility.getTermLabelName(exitContext, masterTermName);	
			finalLabelNams.add(masterTermLabel);
			LOG.info("The master Term Label Name" +masterTermLabel);
		} else {
			LOG.info("The child Term " +childTermName + "is notAvailable");	
		}
		
		return finalLabelNams;		
	}

	
	/**
	 * Helper method - Used to find the BTV's available in the contract
	 * 
	 * @param exitContext
	 * @param childTermName
	 * @param masterTermName 
	 * @param masterTermLabel 
	 * @param childTermLabel 
	 * @param btvDefaultValueData 
	 * @param indicator 
	 * @throws PluginException 
	 * @throws WarningMessageException 
	 */
	private void activitesRelatedToLanguageTerm(ExitContext exitContext, String childTermName, String masterTermName, 
								   				String childTermLabel, String masterTermLabel,
								   				ArrayList<CONDENASTBTVDEFAULTVALUES> btvDefaultValueData) throws PluginException, WarningMessageException {	
		
		BTVMgrBO btvMgrBO = BTVMgrBO.getInstance();
		 
		try {
			
			// Operations to pull Earlier value - [Latest revision number value]			
			ContractBO contractBo = ContractBO.getContractInstance();
			int latestRev = contractBo.getLatestRevision(exitContext.getContract().getId());			
		
			ArrayList<ContractBTVData> btvValues = btvMgrBO.getBTVsForContract(exitContext.getInternalUserObject().getId(), 
													exitContext.getContract().getId(), 
													latestRev);
			
			
				
			
			// Operations to pull value AFTER SAVE Button is HIT - [Draft revision number]			
			ArrayList<ContractBTVData> btvValues1 = btvMgrBO.getBTVsForContract(exitContext.getInternalUserObject().getId(), 
													exitContext.getContract().getId(), 
													IDraftable.DRAFT_REVISION);
			
			
		
			
			List<String> addedTfermList = new ArrayList<String>();
			
			//To find if the term is added in this revision of contract
			for (ContractBTVData contractBTVDataLatest : btvValues1) {
				if(null != contractBTVDataLatest && contractBTVDataLatest.getInternalName() != null) {
					String newtermName = contractBTVDataLatest.getInternalName();
					System.out.println("Existing terms : " + newtermName);
					boolean istermadded = true;
					for (ContractBTVData contractBTVDataDft : btvValues) {
							System.out.println("new terms" + contractBTVDataDft.getInternalName());
							if(null != contractBTVDataDft && null != contractBTVDataDft.getInternalName()
									&& newtermName.equals(contractBTVDataDft.getInternalName()) ) {
								istermadded = false;
							} 
							
					}
					
					if(istermadded) {
						addedTfermList.add(newtermName);
					}
				}
				
			}
			
		
			if(null != addedTfermList && !addedTfermList.isEmpty()) {		
				Object [] object = new Object[addedTfermList.size() * 2]; 
				int count = 0;
				// Show the warning message - BTV_WM_1
				for (String termname : addedTfermList) {
					
					for (CONDENASTBTVDEFAULTVALUES masterchildpair : btvDefaultValueData) {
						
						if(null != masterchildpair && null != termname && null != masterchildpair.getTerminternalname() 
								&& termname.equals(masterchildpair.getTerminternalname())) {
							
							// if masterchildpair.getMasterlovterm() is language term
							if(ContractTermsUtil.isThisLanguageTerm(exitContext,masterchildpair.getMasterlovterm())) {								
								object[count++] = termname;
								object[count++] = masterchildpair.getMasterlovterm();
							}
							
						}
					}
					
				}
				if(null != object && object.length > 0) {
					ErrorUtility.throwWarningExceptionOnUI(MessagekeyConstants.MSG_BTV_WM1.trim(), object, null, exitContext);
				}
			}
			
			// No Terms are added to contract after executed & this is for Master & Language Term -> check if child term value is changed
			if ((null == addedTfermList) || ( null != addedTfermList &&  addedTfermList.isEmpty() )) {	
				// check if term value changed
				if(ContractTermsUtil.isBTVValueChanged(exitContext, childTermName)){
					// UI Error BTV_UIE_2
					ErrorUtility.throwPluginExceptionOnUI(MessagekeyConstants.MSG_BTV_UIE2.trim(), new Object[]{}, null, exitContext);
				}
			}
			
			
		} catch (BTVException e) {
			LOG.error("BTVException  while accessing BTV's of the Contract" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "BTVException_Access_BTV_Contracts"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return;
		
		}  catch (NegotiatorException e) {
			LOG.error("NegotiatorException  while getting Latest revision" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "NegotiatorException_Latest_revision"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return;
		
		} catch (ObjectNotFoundException e) {
			LOG.error("ObjectNotFoundException while getting Latest revision" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_Latest_revision"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return;
		
		}
		

	}

	
	
	
	
	/**
	 * Helper method - This will the update the Master Term with the new value edited/inputed in Internal Term's new value
	 * 
	 * @param exitContext
	 * @param owningOrganizationName 
	 * @param templateName 
	 * @param childTermName 
	 * @param masterTermName 
	 * @param DataType 
	 */
	private void masterTermValueUpdate(ExitContext exitContext, String templateName, String owningOrganizationName, 
										String masterTermName, String childTermName, String DataType ) {		
		LOG.info("End of method ==> masterTermValueUpdate");

		try {
			// If DataType is String
			if(DataType.equals(ContractConstants.STRING)){
				String termValue = ContractHelper.getStringTermValue(exitContext.getContract(), childTermName);
				LOG.info("The Value available in Child Term" + termValue);
				
				ContractHelper.updateTerm(exitContext.getContract(), masterTermName, termValue);
				LOG.info("The Value updated in Master Term" + termValue);
			}
			// If DataType is Decimal
			if(DataType.equals(ContractConstants.DECIMAL)){
				BigDecimal termValue = ContractHelper.getNumericTermValue(exitContext.getContract(), childTermName);
				LOG.info("The Value available in Child Term" + termValue);
				
				ContractHelper.updateTerm(exitContext.getContract(), masterTermName, termValue);
				LOG.info("The Value updated in Master Term" + termValue);
			}
			
			// If DataType is Date
			if(DataType.equals(ContractConstants.DATE)){
				Date termValue = new Date();
				termValue = ContractHelper.getDateTermValue(exitContext.getContract(), childTermName);
				LOG.info("The Value available in Child Term" + termValue);
				
				ContractHelper.updateTerm(exitContext.getContract(), masterTermName, termValue);
				LOG.info("The Value updated in Master Term" + termValue);
			}

		}  catch (ContractException e) {
			LOG.error("ContractException  while accessing Child Term Value" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException_Child_Term_value"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return;
		} catch (EcmApiException e) {
			LOG.error("EcmApiException  while updating Master Term Value" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "EcmApiException_Master_Term_Update"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return;
		}
		
		LOG.info("End of method ==> masterTermValueUpdate ");
	}	

}

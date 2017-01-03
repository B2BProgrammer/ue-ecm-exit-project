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
import com.dicarta.appfound.contract.ContractTermGroupLoadData;
import com.dicarta.appfound.contract.server.bo.ContractBO;
import com.dicarta.appfound.contractmanager.btv.TermMandatoryOptionEnum;
import com.dicarta.appfound.contractmanager.btv.common.BTVException;
import com.dicarta.appfound.contractmanager.btv.common.BTVLOVDataIterator;
import com.dicarta.appfound.contractmanager.btv.common.BTVValueData;
import com.dicarta.appfound.contractmanager.btv.common.ContractBTVData;
import com.dicarta.appfound.contractmanager.btv.server.bo.BTVMgrBO;
import com.dicarta.appfound.contractmanager.btv.server.bo.VariableBO;
import com.dicarta.appfound.contractmanager.btv.server.persistence.VariableData;
import com.dicarta.appfound.termdefinition.ITermDefinitionEditorData;
import com.dicarta.infra.common.CreateException;
import com.dicarta.infra.common.DuplicateKeyException;
import com.dicarta.infra.common.FindException;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.common.PluginException;
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
 * populates the value available from Database to MASTER & INTERNAL terms of the Contract
 * Instance 
 * 
 * While on POST Contract Create Process
 * 
 * @author Ajith.Ajjarani
 *
 */
public class BTVDefaultValuesCreate extends CustomUserExitBase {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(BTVDefaultValuesCreate.class);

	
	/**Constructor, where all required Terms & constants for BIC are initialized
	 * @param exitContext
	 */
	public BTVDefaultValuesCreate(ExitContext exitContext) {
		super(exitContext);
		LOG.info("Start the Constructor BTVDefaultValuesCreate" + exitContext.getContract().getNumber());
		
		LOG.info("End the Constructor BTVDefaultValuesCreate" + exitContext.getContract().getNumber());
		
	}

	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_5_1_FLAG.trim();
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
		LOG.info("Starting --> Boolean Flag check of BTVDefaultValuesCreate");
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
		LOG.info("Starting --> isAllPreConditionsSatisfied of BTVDefaultValuesCreate" + exitContext.getContract().getNumber());
		return !exitContext.getContract().isAmendment();
	}
	
	

	/**
	 *  Public method - accessed from Handler Class, which decides the flow operations
	 * @param exitContext
	 */
	public void executeLogic(ExitContext exitContext) {
		LOG.info("Enter --> Execution of Core Logic for BTVDefaultValuesCreate" + exitContext.getContract().getNumber());
		//Calling the method to fetch Template Name & Owning Organization
		fetchTempNamOwingOrgNCallDB(exitContext);	
		LOG.info("Enter --> Execution of Core Logic for BTVDefaultValuesCreate" + exitContext.getContract().getNumber());
	}
	
	
	/**
	 * Helper method - which fetches Template Name & Owning Organization & handles any exception relevant for these 
	 * & next following flow of operations are carried out to fetch information from Database
	 * 
	 * @param exitContext
	 */
	private void fetchTempNamOwingOrgNCallDB(ExitContext exitContext) {
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
				addingBTVTermsWithDefaultValues(dbReturnedValue , templateName , owningOrganizationName , exitContext);
			}			

		} catch (ContractException e) {
			LOG.error("BTV Default Values requirement 5 ================================== SKIPPED");
			LOG.error("ContractException while accesing the Template Name or Owning Organization - Contract Number :"
					+ exitContext.getContract().getNumber());		

			// Error Code & Error message for DB to dump  => BTV_ER_EX
			String dynamicPart = "ContractException_While_accessing_Template_Name_OwningOrganization";
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , dynamicPart);
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
		LOG.info("Exit --> Method  fetchTempNamOwingOrgNCallDB");
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
	private ArrayList<CONDENASTBTVDEFAULTVALUES> dbOperation(String templatename, String owningorganization, ExitContext exitContext) {
		LOG.info("Start of method ==> dbOperation");
		
		CONDENASTBTVDEFAULTVALUES condenastbtvdefault = new CONDENASTBTVDEFAULTVALUES();
		
		// Creation of Value object having - Template Name & Owning Organization
		condenastbtvdefault.setTemplatename(templatename.trim());
		condenastbtvdefault.setOwningorganizationname(owningorganization.trim());
		
		CONDENAST_BTV_DEFAULTVALUESPersister persister = CONDENAST_BTV_DEFAULTVALUESPersister.getInstance();
		SeqBTVIterator seqbtviterator = null;
		
		ArrayList<CONDENASTBTVDEFAULTVALUES> dbValue = new ArrayList<CONDENASTBTVDEFAULTVALUES>();
		
		try {
			seqbtviterator = (SeqBTVIterator) persister.getBTVSeq(condenastbtvdefault);
			seqbtviterator.prefetch();
			if(seqbtviterator.prefetch().isEmpty()){
				LOG.error("CondeNast_BTV_DefaultValues -> No matching data found for TemplateName - " 
							+ templatename + " Owning Organization - " +owningorganization);				
			} else {
				dbValue = seqbtviterator.prefetch();
				LOG.info("The DB Value was retrieved from TB CONDENAST_BTV_DEFAULTVALUES ");
			}	
		} catch(PersistenceException e) {
			LOG.error("PersistenceException Exception while retrieving data from TB - CONDENAST_BTV_DEFAULTVALUES" + e.getLocalizedMessage());
			
			String dynamicPart = "PersistenceException_while retrieving data from Table CONDENAST_BTV_DEFAULTVALUES";
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , ""); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return null;			
		}
	
		LOG.info("End of method ==> dbOperation ");
		return dbValue;	
	}
		


	/**
	 * Helper method - Loop through each row of returned data from the database & add the following
	 * 1) MASTER TERM
	 * 2) INTERNAL/CHILD Term
	 * 3) Default value available in DB to both Master Term & Child Term
	 * 
	 * into the contract instance
	 * 
	 * @param btvInContracts
	 * @param btvDefaultValueData
	 * @param templatename
	 * @param owningorgname
	 * @param exitContext
	 */
	private void addingBTVTermsWithDefaultValues(ArrayList<CONDENASTBTVDEFAULTVALUES> btvDefaultValueData, 
												String templatename, String owningorgname, ExitContext exitContext) {
		LOG.debug("Start --> addingBTVTermsWithDefaultValues");
		// Used while logging information to Database
		ArrayList<String> dynamicActionLOGList = new ArrayList<String>();
		
		//Initializing the Master & Child Term Names
		String masterTermName = "";
		String childTermName = "";
		
		//Initializing the Master & Child Term DataTypes
		String childTermDataType = "";
		String masterTermDataType = "";
		
		
	
			if(btvDefaultValueData != null && !btvDefaultValueData.isEmpty()) {
				
				// Loop through each ROW of data from the returned LIST of Database
				for (CONDENASTBTVDEFAULTVALUES cdata : btvDefaultValueData) {	
				boolean isMasterTermUpdate = true;
					try {						
						// SECTION 1 
						//MASTER & CHILD term names as provided in Database
						masterTermName = cdata.getMasterlovterm();				
						childTermName =  cdata.getTerminternalname();					
						LOG.info("Master Term Name from Database : "+masterTermName);
						LOG.info("Child Term Name from Database : "+childTermName);			
						
						
						// SECTION 2
						// Below are Default value of Internal/Child Term as Available in Database
						// 1. STRING Default Value
						String defaultstringtermvalue = cdata.getDefaultstringtermvalue();
						LOG.info("The Default Value availabe in DB in String : "+defaultstringtermvalue);

						// 2. BIGDECIMAL Default Value
						Integer defaultnumerictermvalue   = cdata.getDefaultnumerictermvalue();
						BigDecimal defaultnumeric = new BigDecimal(0);
						if(defaultnumerictermvalue != null){
							defaultnumeric = new BigDecimal(defaultnumerictermvalue);
						}
						LOG.info("The Default Value availabe in DB in Numeric : "+defaultnumeric);

						// 3. DATE Default Value
						Date defaultdatetermvalue = cdata.getDefaultdatetermvalue();
						LOG.info("The Default Value availabe in DB in Date : "+defaultdatetermvalue);
								
						// SECTION 3
						// Get the DataTypes of the CHILD & MASTER terms , which are defined in DB
						childTermDataType = ContractTermsUtil.getTermDataType(exitContext, childTermName);
						masterTermDataType = ContractTermsUtil.getTermDataType(exitContext, masterTermName);							
						LOG.info("Child Term DataType : "+childTermDataType + "Master Term DataType : "+masterTermDataType );
						
						
						// Operations pertaining to check STRING & Numeric LOV types & validating if default value
						// is part of configured value else skip this child term adding & master term update
						//Check whether the CHILD/INTERNAL term is available in the ECM System
						if(ContractTermsUtil.isTermAvailableInECM(childTermName, exitContext)){
							LOG.info("BTV Term :" +childTermName+ " is Available in the ECM System ");
							if(checkForLOVTypeTerm(exitContext, childTermName)) {										
								if(childTermDataType.equals(ContractConstants.STRING)){
									if(isStrDefaultValueDBAvailInConfigValue(exitContext, childTermName, defaultstringtermvalue)) {
										LOG.info("String - child Term : " +childTermName+ "; All correct - DB default value has configured value");
									} else {
										isMasterTermUpdate = false;
										// BTV_AM_6
										constructBTV6ErrorLOG(exitContext, childTermName);
									}
								}
								
								if(childTermDataType.equals(ContractConstants.DECIMAL)){
									if(isNumrDefaultValueDBAvailInConfigValue(exitContext, childTermName, defaultnumeric)){
										LOG.info("Numeric - child Term : " +childTermName+ "; All correct - DB default value has configured value");
									} else {
										isMasterTermUpdate = false;
										// BTV_AM_6
										constructBTV6ErrorLOG(exitContext, childTermName);
									}
								}
							} else {
								LOG.info("BTV term "+childTermName+ " is NOT a LOV Type");
							}
						} else {
							// BTV_AM_1								
							LOG.error("BTV Term :" +childTermName+ " is Not Available in the ECM System ");	
							dynamicActionLOGList.add(childTermName);
							dynamicActionLOGList.add(templatename);
							dynamicActionLOGList.add(owningorgname);							

							// Error Code & Error message for DB to dump  => BTV_AM_1
							String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM1.trim(), dynamicActionLOGList , null);
							String errorCode = ErrorCodes.BTV_AM_1;
							
							LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
							LOG.error("The Error Code going to Action LOG TB :" + errorCode);

							// Creation of Error object containing all the information need to be dumped to DB
							List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

							// Adding the resultant error object to List handled after the completion of business requirement
							errorListTODB.add(resultErrorObject);
							dynamicActionLOGList.clear();	
							isMasterTermUpdate = false;
						}
						
						
						// If flag is true -> means validation of LOV was success full & child Term is present in ECM System
						if(isMasterTermUpdate){
							// Checking whether the MASTER term is available in the Template
							if(ContractTermsUtil.isTermAvailableInContract(exitContext, masterTermName)){
								LOG.info("Master Term : " +masterTermName+ " is available in a Contract");								
									
									// check if child Term and master term data type are same
									if(childTermDataType.equalsIgnoreCase(masterTermDataType)){
										// If Term is NOT available in Template -> add the term to contract										
										if(!ContractTermsUtil.isTermAvailableInContract(exitContext, childTermName)){
											LOG.info("Add the Child Term : "+childTermName + " to the contract having a Master Term : "+masterTermName );									

											// Get the Group ID of the Group having MASTER TERM
											String groupToAddChildTermId = getTheMasterTermsGroupID(exitContext,masterTermName);

											// List containing the Child Term's BTVData to be loaded
											List internalTermList = generateListOfTermsToLoad(exitContext, childTermName, childTermDataType , 
																	defaultstringtermvalue ,defaultnumeric , defaultdatetermvalue);					

											//Adding the Child/Internal Term to the Contract at the specific group
											BTVMgrBO.getInstance().addTermsToContract(exitContext.getInternalUserObject(), 
													exitContext.getContract().getId(), 
													internalTermList, 
													groupToAddChildTermId);
											
											
											// Get the new exitContext from the new session, as you have added the child term into the contract 
											exitContext =  new com.emptoris.ecm.api.impl.ExitContextImpl(
												            (com.dicarta.appfound.contract.ContractData) ContractBO.getContractInstance().getContract(exitContext.getContractId(), IDraftable.DRAFT_REVISION),
												            (com.dicarta.appfound.common.IUserInfo) exitContext.getInternalUserObject(),
												            com.emptoris.ecm.api.intf.ExitContext.ExitTypes.POST_CREATE);
												
												
												if(childTermDataType.equals(ContractConstants.STRING)){
													if(defaultstringtermvalue != null){
														ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultstringtermvalue);
													} else {
														//BTV_AM_3
														constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);
													}

												} else if(childTermDataType.equals(ContractConstants.DECIMAL)){
													if(defaultnumerictermvalue != null ){
														ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultnumeric);
													} else {
														//BTV_AM_3
														constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);
													}								
												} else if(childTermDataType.equals(ContractConstants.DATE)){
													if(defaultdatetermvalue != null){
														ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultdatetermvalue);	
													} else {
														//BTV_AM_3
														constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);							
													}	
												}	
											
											LOG.info("Adding the Child Term was Successfull" );
										} 
										
										



										// Below execution continues -> if CHILD Term is Available in Contract Template
										LOG.info("Child Term : "+childTermName + " already Exists in the Contract Template : "+templatename);
										if(childTermDataType.equals(ContractConstants.STRING)){
											if(defaultstringtermvalue != null){
												ContractHelper.updateTerm(exitContext.getContract(), masterTermName, defaultstringtermvalue);
												ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultstringtermvalue);			
												LOG.info("Child Term : "+childTermName + " & Master Term : " +masterTermName+ " was updated with a value " + defaultstringtermvalue);												
											} else {
												//BTV_AM_3
												constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);
											}

										} else if(childTermDataType.equals(ContractConstants.DECIMAL)){
											if(defaultnumerictermvalue != null ){
												ContractHelper.updateTerm(exitContext.getContract(), masterTermName, defaultnumeric);
												ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultnumeric);	
												LOG.info("Child Term : "+childTermName + " & Master Term : " +masterTermName+ " was updated with a value " + defaultnumeric);
											} else {
												//BTV_AM_3
												constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);
											}								
										} else if(childTermDataType.equals(ContractConstants.DATE)){
											if(defaultdatetermvalue != null){
												ContractHelper.updateTerm(exitContext.getContract(), masterTermName, defaultdatetermvalue);
												ContractHelper.updateTerm(exitContext.getContract(), childTermName, defaultdatetermvalue);		
												LOG.info("Child Term : "+childTermName + " & Master Term : " +masterTermName+ " was updated with a value " + defaultdatetermvalue);
											} else {
												//BTV_AM_3
												constructBTV3ErrorLOG(exitContext ,masterTermName,childTermName,childTermDataType,templatename,owningorgname);							
											}	
										}		


									} else {
										LOG.info("Child Term Type :"+ childTermDataType+" and Master Term Type : " +masterTermDataType+ "are different Logging this to Action LOG Table");
										
										// BTV_AM_5			
										LOG.error("Mismatch of DataTypes between Master Term :" +masterTermName+ " & child Term " +childTermName);		
										dynamicActionLOGList.add(masterTermName);
										dynamicActionLOGList.add(masterTermDataType);
										dynamicActionLOGList.add(childTermName);	
										dynamicActionLOGList.add(childTermDataType);

										// Error Code & Error message for DB to dump  => BTV_AM_5
										String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM5.trim(), dynamicActionLOGList , null);
										String errorCode = ErrorCodes.BTV_AM_5;

										// Creation of Error object containing all the information need to be dumped to DB
										List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

										// Adding the resultant error object to List handled after the completion of business requirement
										errorListTODB.add(resultErrorObject);
										dynamicActionLOGList.clear();
									}	

								
							} else {
								LOG.info("Master Term : " +masterTermName+ " is NOT available in a Contract");
							}
						} else {
							LOG.info("Term was LOV type & has DB value different - no  addition of child term & update even");
						}
						
					} catch (EcmApiException e) {
						// BTV_AM_4				
						LOG.error("Error & Exception  EcmApiException :" + e.getLocalizedMessage() + "while adding value for child Term :" +childTermName+ " & Master Term :" +masterTermName+ "into the Template " +templatename);		
						dynamicActionLOGList.add(masterTermName);
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(childTermDataType);			

						// Error Code & Error message for DB to dump  => BTV_AM_4
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM4.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_4;

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();			
					} catch (DuplicateKeyException e) {
						// BTV_AM_2					
						LOG.error("Error & Exception  DuplicateKeyException :" + e.getLocalizedMessage() + "while adding the child Term :" +childTermName+ " into the Template " +templatename);		
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(templatename);
						dynamicActionLOGList.add(owningorgname);

						// Error Code & Error message for DB to dump  => BTV_AM_2
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM2.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_2;
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();
					} catch (CreateException e) {
						// BTV_AM_2			
						LOG.error("Error & Exception  CreateException :" + e.getLocalizedMessage() + "while adding the child Term :" +childTermName+ " into the Template " +templatename);
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(templatename);
						dynamicActionLOGList.add(owningorgname);

						// Error Code & Error message for DB to dump  => BTV_AM_2
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM2.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_2;
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();
					} catch (FindException e) {
						// BTV_AM_2		
						LOG.error("Error & Exception  FindException :" + e.getLocalizedMessage() + "while adding the child Term :" +childTermName+ " into the Template " +templatename);
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(templatename);
						dynamicActionLOGList.add(owningorgname);

						// Error Code & Error message for DB to dump  => BTV_AM_2
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM2.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_2;
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();
					} catch (PluginException e) {
						// BTV_AM_2					
						LOG.error("Error & Exception  PluginException :" + e.getLocalizedMessage() + "while adding the child Term :" +childTermName+ " into the Template " +templatename);		
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(templatename);
						dynamicActionLOGList.add(owningorgname);

						// Error Code & Error message for DB to dump  => BTV_AM_2
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM2.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_2;
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();
					} catch (ObjectNotFoundException e) {
						// BTV_AM_2					
						LOG.error("Error & Exception  ObjectNotFoundException :" + e.getLocalizedMessage() + "while adding the child Term :" +childTermName+ " into the Template " +templatename);		
						dynamicActionLOGList.add(childTermName);
						dynamicActionLOGList.add(templatename);
						dynamicActionLOGList.add(owningorgname);

						// Error Code & Error message for DB to dump  => BTV_AM_2
						String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM2.trim(), dynamicActionLOGList , null);
						LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
						
						String errorCode = ErrorCodes.BTV_AM_2;
						LOG.error("The Error Code going to Action LOG TB :" + errorCode);

						// Creation of Error object containing all the information need to be dumped to DB
						List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

						// Adding the resultant error object to List handled after the completion of business requirement
						errorListTODB.add(resultErrorObject);
						dynamicActionLOGList.clear();
					} 
				}			
			} else {
				LOG.error("CondeNast_BTV_DefaultValues -> No matching data found for TemplateName - " + templatename + " Owning Organization - " +owningorgname);
			}			

		 
		
		LOG.debug("End --> addingBTVTermsWithDefaultValues");
	}
	
	
	
	
	
	

	/**
	 * 
	 * Helper Method - Used to construct a List having a term's BTVValue Data - that need to be loaded
	 * while adding a Child term to the Contract Instance's required Group
	 * 
	 * @param exitContext
	 * @param childTermName
	 * @param childTermDataType
	 * @param defaultdatetermvalue 
	 * @param defaultnumeric 
	 * @param defaultstringtermvalue 
	 * @return
	 */
	private List generateListOfTermsToLoad(ExitContext exitContext, String childTermName, String childTermDataType, 
								String defaultstringtermvalue, BigDecimal defaultnumeric, Date defaultdatetermvalue) {
		LOG.info("Start Method --> generateList ");
		
		// List Instantiated to store the Terms wiht BTVValueData
		List internalTermList = new ArrayList();
		
		//Calling the utility method to get the variable Data to be loaded		
		ITermDefinitionEditorData termDefEditorData = ContractTemplateUtility.getTermDetails(exitContext, childTermName);
		
		// checking wheather the termDefEditorData is not NuLL
		if(termDefEditorData != null){
			//Variable of Abstract Type btvValueData to store into the list
			BTVValueData btvValData = new BTVValueData();			
		
					
			//1 Setting the INTERNAL NAME of child Term
			btvValData.setInternalName(childTermName);			
			//2 Setting the VARIABLE ID of the Child Term
			btvValData.setVariableId(getVariableID(exitContext, childTermName));
			//3 Setting the MANDATORY FOR of the Child Term		
			com.dicarta.appfound.termdefinition.TermMandatoryOptionEnum mandString = termDefEditorData.getMandatoryToOperation();		
			//String mand = mandString.getName();
			
			if(mandString.getName().equals("NONE"))	{
				btvValData.setMandatoryFor(TermMandatoryOptionEnum.NONE);
			}
			
			if(mandString.getName().equals("EXECUTE"))	{
				btvValData.setMandatoryFor(TermMandatoryOptionEnum.EXECUTE);
			}
			
			if(mandString.getName().equals("PRESENT_AND_EXECUTE")) {
				btvValData.setMandatoryFor(TermMandatoryOptionEnum.PRESENT_AND_EXECUTE);
			}		
			
			//4 Setting the LABEL NAME of the Child Term
			btvValData.setName(termDefEditorData.getLabel());
			
			//5 Setting the  DATATYPE of the Child Term
			btvValData.setType(childTermDataType);
		
			//6 setting the Term Value
			if(childTermDataType.equals(ContractConstants.STRING)){
				if(defaultstringtermvalue != null){
					btvValData.setValueString(defaultstringtermvalue);					
					LOG.info("The Value setting to Child term" +defaultstringtermvalue);
				}
			} else if(childTermDataType.equals(ContractConstants.DECIMAL)){
				if(defaultnumeric != null ){
					btvValData.setValueNumber(defaultnumeric);					
					LOG.info("The Value setting to Child term" +defaultnumeric);
				}								
			} else if(childTermDataType.equals(ContractConstants.DATE)){
				if(defaultdatetermvalue != null){
					btvValData.setValueDate(defaultdatetermvalue);	
					LOG.info("The Value setting to Child term" +defaultdatetermvalue);
				} 
			}		
			
			
			internalTermList.add(btvValData);
		}
		
		
		LOG.info("End Method --> generateList ");
		
		// returning the list having the Internal Terms for the specific Group
		return internalTermList;
		
	}
		
	
	
		
	

	/**
	 * Get Term/Variable ID
	 * @param termName
	 * @return
	 * @throws ObjectNotFoundException
	 */
	private String getVariableID(ExitContext exitContext, String termName) {
		LOG.info("Start of method ==> getVariableID for contract Instance");
		VariableData variableData = null;
		String variableid=null;
        try {
			variableData =  VariableBO.getInstance().getVariableByInternalName(termName);
		} catch (ObjectNotFoundException e) {
			LOG.error("ObjectNotFoundException while getting Variable information" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_VariableBO"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return null;			
		
		}
        variableid = variableData.getVariableId();
        LOG.info("End of method ==> getVariableID for contract Instance");
        return variableid;
	}
	
	

	
	
	
	/**
	 * Private Helper method - used to get the GroupId of the Master name fetched from the Table
	 * 
	 * @param exitContext
	 * @param masterTermName
	 * @return
	 */
	private String getTheMasterTermsGroupID(ExitContext exitContext, String masterTermName) {
		LOG.info("Start Method --> getTheMasterTermsGroupID ");
		// using BTV Manager BO for fetching available Term Groups in Template
		LOG.info("Using BTV Manager BO for fetching available Term Groups in Template");
		
		BTVMgrBO m_btvMgrBO = BTVMgrBO.getInstance();
		List<ContractTermGroupLoadData> termGroupBTVsForContract;
		
		//Initializing the Group relevant variables
		String groupToAddChildTerm = "";
		String groupToAddChildTermId = "";
		try {
			termGroupBTVsForContract = m_btvMgrBO.getTermGroupBTVsForContract(exitContext.getInternalUserObject(),
																			 exitContext.getContract().getId(),
																			 IDraftable.DRAFT_REVISION,null);			
			   
			
			//Loop through all the Groups of the Contract Template & find where the specific term is available
			for (ContractTermGroupLoadData contractTermGroupLoadData : termGroupBTVsForContract) {									
				String termGroupName = contractTermGroupLoadData.getName();	
				 String termGroupId  = contractTermGroupLoadData.getId();
				List<ContractBTVData> contractDataOfTermGroup = contractTermGroupLoadData.getTermValueData();
				for (ContractBTVData contractBTVData : contractDataOfTermGroup) {										
					String presentTermName = contractBTVData.getInternalName();
					if(presentTermName.equalsIgnoreCase(masterTermName)){
						// Group name to Add the child term having Master Term
						groupToAddChildTerm = termGroupName;
						// Group ID to Add the child term having Master Term
						groupToAddChildTermId = termGroupId;
						break;
					}										
				}								
			}
			LOG.info("The Group :" +groupToAddChildTerm+ " having the Master Term "+ masterTermName+ " to which the Child Term will be added ");
			
		} catch (BTVException e) {
			LOG.error("BTVException  while retrieving all the Groups from the Contract Instance" + e.getLocalizedMessage());

			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "BTVException_Groups_retreiveal"); 
			String errorCode = ErrorCodes.BTV_ER_EX;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);
			
			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);		
			return null;	
		}
		
		LOG.info("End Method --> getTheMasterTermsGroupID ");
		
		// Returning the GroupId containing the Master Term
		return groupToAddChildTermId;  		
	}
	
	
	

		
		/**
		 * Helper Method - To Construct Dynamic Message & Log the same to Action Table &
		 * only for BTV_3
		 * 
		 * @param exitContext
		 * @param owningorgname 
		 * @param templatename 
		 * @param childTermDataType 
		 * @param childTermName 
		 * @param masterTermName 
		 */
		private void constructBTV3ErrorLOG(ExitContext exitContext, String masterTermName, String childTermName,
											String childTermDataType, String templatename, String owningorgname) {
			// BTV_AM_3		
			LOG.error("Invalid Value Retrived from Database for populating into Child Term :" +childTermName+ " & Master Term : " +masterTermName);	

			// Used while logging information to Database
			ArrayList<String> dynamicActionLOGList = new ArrayList<String>();

			dynamicActionLOGList.add(masterTermName);
			dynamicActionLOGList.add(childTermName);
			dynamicActionLOGList.add(childTermDataType);
			dynamicActionLOGList.add("NULL");
			dynamicActionLOGList.add(templatename);
			dynamicActionLOGList.add(owningorgname);

			// Error Code & Error message for DB to dump  => BTV_AM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM3.trim(), dynamicActionLOGList , null);
			String errorCode = ErrorCodes.BTV_AM_3;
			
			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			// return this method - no more proceeding with this requirement
			return ;				
		}
		
		
		
		
		

		/**Construct the error message when term is of LOV type & default value in database is not matching w
		 * with any value in terms earlier configured value
		 * 
		 * @param exitContext
		 * @param childTermName
		 */
		private void constructBTV6ErrorLOG(ExitContext exitContext,	String childTermName) {
			// BTV_AM_3
			
			// Error Code & Error message for DB to dump  => BTV_AM_3
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_BTV_AM6.trim(), null, childTermName);
			String errorCode = ErrorCodes.BTV_AM_6;

			LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			// return this method - no more proceeding with this requirement
			return ;		

		}
		
		
		
				
		/**
		 * Helper method - To Check the term is a LOV or not
		 * 
		 * 
		 * @param _contract
		 * @param masterlovterm
		 * @return
		 * @throws FindException
		 * @throws ObjectNotFoundException
		 * @throws PersistenceException
		 */
		public Boolean checkForLOVTypeTerm(ExitContext exitContext ,String masterlovterm) {
			LOG.info("Start of method ==> checkForLOVTypeTerm for contract Instance");
			LOG.debug("Checking if " + masterlovterm + " Term is a LOV Term or not");
			Boolean isTermLOV=false;
			
			try {
				BTVMgrBO bo = BTVMgrBO.getInstance();
				BTVLOVDataIterator iterator;
				iterator = bo.getBTVLOVValues(GeneralUtility.getUser(exitContext), getVariableID(exitContext, masterlovterm));
				
				ArrayList<String> check = new ArrayList<String>();
				
				while (iterator.hasNext()) {
					com.dicarta.appfound.contractmanager.btv.common.BTVLOVData lovData = 
					(com.dicarta.appfound.contractmanager.btv.common.BTVLOVData) iterator.next();
					String lovvalue = lovData.getValue();
					check.add(lovvalue);
				}
				
				if(!check.isEmpty()) {
					isTermLOV = true;
					LOG.debug( masterlovterm + " is a LOV term");
				}
			} catch (FindException e) {
				LOG.error("FindException while getting BTV values in LOV" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "FindException_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return null;	
			
			} catch (ObjectNotFoundException e) {
				LOG.error("ObjectNotFoundException while getting BTV values in LOV" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return null;	
			
			} catch (PersistenceException e) {

				LOG.error("PersistenceException while accessing from Iterating through BTV LOV data" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_Iterating_LOV_Data"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return null;	
			
			}
			
			LOG.info("End of method ==> checkForLOVTypeTerm for contract Instance");
			return isTermLOV;
		}
		
		
		
		
		/**
		 * Helper method - Checking the fetched value from database is one among the earlier configured values
		 * of term type - STRING
		 * 
		 * @param _contract
		 * @param childlovterm
		 * @param termValue
		 * @return
		 * @throws FindException
		 * @throws ObjectNotFoundException
		 * @throws PersistenceException
		 */
		public boolean isStrDefaultValueDBAvailInConfigValue(ExitContext exitContext,String childlovterm,String termValue) {
			LOG.info("Start of method ==> isStrDefaultValueDBAvailInConfigValue ");
			
			Boolean isValuePresent=false;
			
			try {
				BTVMgrBO bo = BTVMgrBO.getInstance();
				BTVLOVDataIterator iterator;
				iterator = bo.getBTVLOVValues(GeneralUtility.getUser(exitContext), getVariableID(exitContext, childlovterm));
				ArrayList<String> masterlovValues = new ArrayList<String>();
				
				
				while (iterator.hasNext()) {
					com.dicarta.appfound.contractmanager.btv.common.BTVLOVData lovData = 
					(com.dicarta.appfound.contractmanager.btv.common.BTVLOVData) iterator.next();
					String lovvalue = lovData.getValue();			
					
					masterlovValues.add(lovvalue);
				}
				for(int i = 0 ; i < masterlovValues.size();i++){
					if(termValue.equalsIgnoreCase(masterlovValues.get(i))){
						isValuePresent = true;
						LOG.debug(" Default value present in the Master LOV Term");
					}
				}
			} catch (FindException e) {
				LOG.error("FindException while getting BTV values in LOV and comparing String value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "FindException_String_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			} catch (ObjectNotFoundException e) {
				LOG.error("ObjectNotFoundException while getting BTV values in LOV and comparing String value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_String_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			} catch (PersistenceException e) {

				LOG.error("PersistenceException while getting BTV values in LOV and comparing String value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_String_Iterating_LOV_Data"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			}
			
			LOG.info("End of method ==> checkDefaultInMasterLOV for contract Instance");
			return isValuePresent;
		}
		
		
		
		
		
		
		
		/**
		 * Helper method - Checking the fetched value from database is one among the earlier configured values
		 * of term type - STRING
		 * 
		 * @param _contract
		 * @param masterlovterm
		 * @param defaultnumeric
		 * @return
		 * @throws FindException
		 * @throws ObjectNotFoundException
		 * @throws PersistenceException
		 */
		public boolean isNumrDefaultValueDBAvailInConfigValue(ExitContext exitContext,String childlovterm,BigDecimal defaultnumeric) {
			LOG.info("Start of method ==> isNumrDefaultValueDBAvailInConfigValue");
			
			Boolean isValuePresent=false;
			
			try {
				BTVMgrBO bo = BTVMgrBO.getInstance();
				BTVLOVDataIterator iterator;
				iterator = bo.getBTVLOVValues(GeneralUtility.getUser(exitContext), getVariableID(exitContext, childlovterm));
				ArrayList<String> masterlovValues = new ArrayList<String>();
				
				
				while (iterator.hasNext()) {
					com.dicarta.appfound.contractmanager.btv.common.BTVLOVData lovData = 
					(com.dicarta.appfound.contractmanager.btv.common.BTVLOVData) iterator.next();
					String lovvalue = lovData.getValue();
					masterlovValues.add(lovvalue);
				}
				boolean checkValue = masterlovValues.contains(String.valueOf(defaultnumeric));
				if(checkValue){
					isValuePresent = true;
					LOG.debug("Term is a LOV type");
				}
				else{
					LOG.error("Numeric default is not available in the Master LOV Term");
				}
			} catch (FindException e) {
				LOG.error("FindException while getting BTV values in LOV and comparing Numeric value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "FindException_Numeric_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			} catch (ObjectNotFoundException e) {
				LOG.error("ObjectNotFoundException while getting BTV values in LOV and comparing Numeric value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_Numeric_BTV_Value_retreival"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			} catch (PersistenceException e) {

				LOG.error("PersistenceException while getting BTV values in LOV and comparing Numeric value in terms value" + e.getLocalizedMessage());

				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_Numeric_Iterating_LOV_Data"); 
				String errorCode = ErrorCodes.BTV_ER_EX;
				
				LOG.error("The Error Message going to Action LOG TB :"+ errorMessage);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);
				
				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , errorMessage, errorCode);
				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);		
				return false;	
			
			}
			
			LOG.info("End of method ==> isNumrDefaultValueDBAvailInConfigValue");
			return isValuePresent;
		}	
		
		
}

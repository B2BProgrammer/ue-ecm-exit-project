/**
 * 
 */
package com.emptoris.ecm.exit.condenast.impltwo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import persistence.CondeNastOUDepartment;
import persistence.CondeNastTOWCommision;
import persistence.CondeNast_OU_DepartmentPersister;
import persistence.CondeNast_TOW_CommisionPersister;
import persistence.SeqDetailsIterator;
import persistence.SeqDetailsTOWIterator;

import com.dicarta.appfound.common.CreationEntryPoint;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.exception.LineValueException;
import com.emptoris.ecm.api.intf.DataDefinition;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.ExitContext.ExitTypes;
import com.emptoris.ecm.api.intf.Line;
import com.emptoris.ecm.api.intf.LineDefinition;
import com.emptoris.ecm.api.intf.LineSet;
import com.emptoris.ecm.api.intf.LineValue;
import com.emptoris.ecm.api.intf.LineValueSet;
import com.emptoris.ecm.exit.Exit;
import com.emptoris.ecm.exit.condenast.constants.ConfigurationConstants;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;

/**
 * Business Implementation class for - adding correct lines details to the contract instance 
 * 
 * @author Ajith.Ajjarani
 *
 */
public class PopulatingEntities extends CustomUserExitBase {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(PopulatingEntities.class);
	
	// Class level variable declared to instantiate & store line details
	private List<String> requiredLineDet1 = new ArrayList<String>();
	private List<String> requiredLineDet2 = new ArrayList<String>();
	
	//List to store all the available Line Definitions in the Contract Template
	private List<LineDefinition> availableFinalLineDef = new ArrayList<LineDefinition>(); 
	
	//Initializing that both lines configuration are correct & if not later changed to false
	private static boolean addTypeOfWorkLineDef = true;
	private static boolean addDepartmentLineDef = true;
	

	/**
	 * Constructor where - all required configurations are preloaded to test validation
	 * @param exitContext
	 */
	public PopulatingEntities(ExitContext exitContext) {
		super(exitContext);
		// Loading all the configurations of Line Definition - TOW for BIC Implementation
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW.trim());
		
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW_DF1.trim());
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW_DF2.trim());
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW_DF3.trim());
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW_DF4.trim());
		requiredLineDet1.add(ConfigurationConstants.CNE_LINE_TOW_DF5.trim());
		
		// Loading all the configurations of Line Definition - OU_DEPT for BIC Implementation
		requiredLineDet2.add(ConfigurationConstants.CNE_LINE_DEPT.trim());
		
		requiredLineDet2.add(ConfigurationConstants.CNE_LINE_DEPT_DF1.trim());
		requiredLineDet2.add(ConfigurationConstants.CNE_LINE_DEPT_DF2.trim());
		requiredLineDet2.add(ConfigurationConstants.CNE_LINE_DEPT_DF3.trim());
		requiredLineDet2.add(ConfigurationConstants.CNE_LINE_DEPT_DF4.trim());
		
		// Need to be executed activity for Line Operations
		requiredActivityForLines(exitContext);
	}
	
	
	
	/**
	 * Important activity of submitDraft need to be run earlier before starting the lines 
	 * activity
	 * 
	 * @param exitContext
	 */
	private void requiredActivityForLines(ExitContext exitContext) {
		LOG.info("Start --> Method requiredActivityForLines ");
		// This step is required because on create there is no entry in the ContractRevision table which is required for the lines stored procedure
		try {
			if (ExitTypes.POST_CREATE == exitContext.getExitType()) {
				LOG.debug(": Contract number=" + exitContext.getContract().getNumber() + ", Performing submitDraft() before checking for attachments and lines");
				exitContext.getContract().submitDraft();
			}
		} catch (ObjectNotFoundException e) {
			LOG.error("ObjectNotFoundException  while executing Populating Entities" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ObjectNotFoundException_Populating_Entities_SubmitDraft");					
			String errorCode = ErrorCodes.PE_ER_EX;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}
		LOG.info("End --> Method requiredActivityForLines ");
	}

	
	/* (non-Javadoc)
	 * Fetching the Boolean flag value from the configuration file
	 * 
	 * @see com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase#getFlagConfigKey()
	 */
	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_2_FLAG.trim();
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
		LOG.info("Starting --> Boolean Flag check of PopulatingEntities");
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
		LOG.info("Start --> Method isAllPreConditionsSatisfied ");

		boolean isPreConditionsSet = false;	

		if(!exitContext.getContract().isAmendment()){
			//contractCreatedChecker - Needed to take decisions according to contract creation process
			CreationEntryPoint contractCreatedChecker = Exit.creationEntryPoint.get();

			if(contractCreatedChecker != null){
				// Lines loading allowed only while on Contract Creation - UI or Interview Wizard
				if(contractCreatedChecker.equals(CreationEntryPoint.UI) || contractCreatedChecker.equals(CreationEntryPoint.INTERVIEW) ){
					isPreConditionsSet = true;
				}
			}		
		}
		
		LOG.info("End --> Method isAllPreConditionsSatisfied ");
		return isPreConditionsSet;
	}
	
	
	
	/**
	 * Called from Handler class to do all the validation operations of lines
	 * 
	 * @param exitContext
	 * @return
	 */
	public boolean validationOfLines(ExitContext exitContext) {
		LOG.info("Start --> Method validationOfLines ");
		
		boolean isFinalValidation = false;
		
		// Calling the method in Super class for configuration file validations of Line Definitions & Data Definitions
		addTypeOfWorkLineDef = generalLineValidator(exitContext,requiredLineDet1, ErrorCodes.PE_AM_1, MessagekeyConstants.MSG_PE_AM1.trim(), addTypeOfWorkLineDef );
		addDepartmentLineDef = generalLineValidator(exitContext,requiredLineDet2, ErrorCodes.PE_AM_2, MessagekeyConstants.MSG_PE_AM2.trim(), addDepartmentLineDef );
		
		// If any one Line definition have invalid Configuration - ByPass the requirement
		if(!addTypeOfWorkLineDef || !addDepartmentLineDef ) {
			return false;
		}
		
		
		//Validation of LINES in the selected Contract Template [Both should be available,else skip the requirement]
		availableFinalLineDef = ValidatingLinesAvailableInContractInstance(exitContext);
		
		if(!availableFinalLineDef.isEmpty()){
			isFinalValidation = true;
		} else {
			isFinalValidation = false;
		}
		
		
		LOG.info("End --> Method validationOfLines ");		
		return isFinalValidation;		
	}
		
	


	
	

	/**
	 * public method called from Handler to do all the respective flow operations
	 * - Fetches the template Name & Owning Organization name & passed to 
	 * respective 
	 * 
	 * @param exitContext
	 * @param isFromSaveAs 
	 */
	public void executeLogic(ExitContext exitContext, boolean isFromSaveAs) {
		LOG.info("Start --> Method executeLogic ");
		
		// If contract Created from SAVEAs - Populatinng entities should not be used
		if( !isFromSaveAs){			
			try {
				// Template Name
				String templateName = exitContext.getContract().getTemplate().getName();
				// Owning Organization
				String owningOrganizationName = exitContext.getContract().getOwningOrganization().getName();

				if(templateName != null && owningOrganizationName != null 
						|| !(templateName.isEmpty() && owningOrganizationName.isEmpty())) {
					// Call the private method to do all the operations respective to TOW_Commission activities
					activitiesOfTOW_CommissionLineDefinition(exitContext , templateName, owningOrganizationName);
					//Call the private method to do all the operations respective to OU_Department activities
					activitiesOfOUDeptLineDefinition(exitContext, templateName, owningOrganizationName);
				}
			} catch (ContractException e) {
				LOG.error("ContractException  while fetching either Template Name or Owning OrganizationName in requirement" +
						" Populating Entities - Error Trace :" + e.getLocalizedMessage());
				String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "ContractException_TemplateName_OwningOrganization");					
				String errorCode = ErrorCodes.PE_ER_EX;	

				LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
				LOG.error("The Error Code going to Action LOG TB :" + errorCode);

				// Creation of Error object containing all the information need to be dumped to DB
				List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

				// Adding the resultant error object to List handled after the completion of business requirement
				errorListTODB.add(resultErrorObject);
				return ;
			} 	
			
		} else {
			LOG.debug("From SAVEAS - populating entities disabled");
		}

		

		LOG.info("End --> Method executeLogic ");
	}

	

	

	/**
	 * Helper method - To do all the operations respective to TOW_Commission activities
	 * 
	 * @param exitContext
	 * @param templateName
	 * @param owningOrganizationName
	 */
	private void activitiesOfTOW_CommissionLineDefinition(ExitContext exitContext, String templateName, String owningOrganizationName) {
		LOG.info("Start --> Method activitiesOfTOW_CommissionLineDefinition ");
		if(addTypeOfWorkLineDef) {
			LOG.info("All configuration is Correct therefore proceed with TOW_Commission_Status");
			//fetch the data from Database - CONDENAST_TOW_COMMISION
			ArrayList<CondeNastTOWCommision> tow_Commission_data =  getDataFromDatabaseForTOW(exitContext, templateName, owningOrganizationName);
			if( tow_Commission_data != null){
				if(!tow_Commission_data.isEmpty()) {
					//AddLines to TOW Line Definition to the contract				
					addTOW_Comm_Lines_ToContract(exitContext, tow_Commission_data);				
				} else {				
					LOG.info("The data is not available in Database for table - CONDENAST_TOW_COMMISION");
				}
			} else {
				LOG.info("Persistence Exceptoin - ByPass the requirement");
			}
			

		} else {
			LOG.info("This is Line Definition is Disabled");
		}
		LOG.info("End --> Method activitiesOfTOW_CommissionLineDefinition ");
	}
	
	
	
	/**
	 * Database Operation to return data from DB Table -
	 * CONDENAST_TOW_COMMISION for respective Template name & Owning Organization name
	 * 
	 * @param exitContext
	 * @param templateName
	 * @param owningOrganizationName
	 */
	private ArrayList<CondeNastTOWCommision> getDataFromDatabaseForTOW(ExitContext exitContext, String templateName, String owningOrganizationName) {

		LOG.info("Start of method ==> getDataFromCondeNastTOWCommissionTable for contract Instance");

		CondeNastTOWCommision condenasttowcomm = new CondeNastTOWCommision();
		// Setting template Name & Owning Organization into Value object
		condenasttowcomm.setTemplatename(templateName);
		condenasttowcomm.setOwningorganization(owningOrganizationName);


		CondeNast_TOW_CommisionPersister persister = CondeNast_TOW_CommisionPersister.getInstance();
		SeqDetailsTOWIterator seqiterator = null;
		ArrayList<CondeNastTOWCommision> dbTOWValue = new ArrayList<CondeNastTOWCommision>();
		try	{
			seqiterator = (SeqDetailsTOWIterator) persister.getTOWSeq(condenasttowcomm);
			seqiterator.prefetch();
			if(seqiterator.count()==0)	{
				LOG.error("No Data Available for this Combination of Template name & OwningOrganization");
			} else {
				dbTOWValue = seqiterator.prefetch();
				LOG.info("Data retrieval was successfull");
			}	
		} catch(PersistenceException e)	{	
			LOG.error("PersistenceException while fetching data from Table CONDENAST_TOW_COMMISION" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "PersistenceException while fetching data from Table CONDENAST_TOW_COMMISION");					
			String errorCode = ErrorCodes.PE_ER_EX;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return null;
		}
		
		LOG.info("End of method ==> getDataFromCondeNastTOWCommissionTable for contract Instance");
		
		//returning data fetched from TB - CONDENAST_TOW_COMMISION
		return dbTOWValue;

	}


	
	
	/**
	 * Method to add lines to contract Instance
	 * Line would be - TOW_Lines_Commission
	 * 
	 * @param _contract
	 * @param linedefname
	 * @param towDataFromDB
	 * @param result
	 * @throws PluginException
	 * @throws ContractException
	 * @throws LineValueException
	 */
	private void addTOW_Comm_Lines_ToContract(ExitContext exitContext , ArrayList<CondeNastTOWCommision> towDataFromDB) {
		LOG.info("Start of method ==> addTOWLines for contract Instance");		

		//dynamicData to populate in ActionLOG Table
		ArrayList<String> dyanmicDataActionLog = new ArrayList<String>();
		ArrayList<String> toErrorDBList = new ArrayList<String>();

		try {
			// Pull all Line Definition & Data Definition names from Configuration file
			// & store into headerFields for next operations	
			ArrayList<String> headerFields = new ArrayList<String>();
			String lineTow = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW.trim());

			String tow = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW_DF1.trim());
			String commissionstatus = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW_DF2.trim());
			String towstatus = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW_DF3.trim());
			String towgap = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW_DF4.trim());
			String towcomments = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_TOW_DF5.trim());

			headerFields.add(tow);
			headerFields.add(commissionstatus);
			headerFields.add(towstatus);
			headerFields.add(towgap);
			headerFields.add(towcomments);

			// Following Operations to Check - weather all correct data definition
			// is available for the Line Definition
			LineDefinition lineDefinition = null;
			for (LineDefinition eachAvailLD : availableFinalLineDef) {				
				if (eachAvailLD.getName().equals(lineTow)) {
					// List to store invalid data definitions
					List<String> pem4 = new ArrayList<String>();		
					for (String eachDataDefintionInConfig : headerFields) {
						DataDefinition dd = eachAvailLD.getField(eachDataDefintionInConfig);
						//String testValue = dd.getName();
						if(dd != null){
							LOG.debug("This DataDefinition is Available in Line Definition :" +eachDataDefintionInConfig);
						} else {
							LOG.debug("The DataDefinition is  NOT Available in Line Definition" +eachDataDefintionInConfig);
							pem4.add(eachDataDefintionInConfig);
						}						
					}

					//If list is NOT empty means some Data Definition are misconfigured, Need to LOG & bypass the requirement
					if(!pem4.isEmpty()) {
						for (String eachMissingConfiguredDataDef : pem4) {
							LOG.error("LineValueException while adding OU_Dept to Lines on Populating Entities");
							toErrorDBList.add(lineTow);
							toErrorDBList.add(eachMissingConfiguredDataDef);
							toErrorDBList.add(exitContext.getContract().getTemplate().getName());

							toErrorDBList.add(exitContext.getContract().getTemplate().getName());							
							String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM4.trim(), toErrorDBList, null );					
							String errorCode = ErrorCodes.PE_AM_4;	

							// Creation of Error object containing all the information need to be dumped to DB
							List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

							// Adding the resultant error object to List handled after the completion of business requirement
							errorListTODB.add(resultErrorObject);
							toErrorDBList.clear();				
						}							
						return;									
					}							
					lineDefinition = eachAvailLD;				
				}
			}




			// Constructing a Hash Map & putting all the data from Database inside this map
			HashMap<Integer, ArrayList<Object>> lineData = new HashMap<Integer, ArrayList<Object>>();
			int count = 1;
			for(CondeNastTOWCommision eachRow : towDataFromDB)	{
				ArrayList<Object> dataTable = new ArrayList<Object>();			

				dataTable.add(eachRow.getTypeofwork());								
				dataTable.add(eachRow.getCommissionstatus());								
				dataTable.add(eachRow.getStatus());				
				dataTable.add(eachRow.getGap());

				// each comments into a dataTable List
				String comments = eachRow.getComments();
				if((comments == null ) || ( null != comments && comments.isEmpty())) {
					comments = "-";
					dataTable.add(comments);
				} else	{
					dataTable.add(comments);
				}
				lineData.put(count,dataTable);
				count++;				
			}




			// Adding a blank lines to lines to the table
			int size = lineData.size();
			for (int j = 0 ; j<size ; j++) {
				exitContext.getContract().addLine(lineDefinition);
			}
			
			//Pulling out of HashMap & populating this data into the Lines
			LOG.debug("Adding Lines to Conde Nast TOW Commission Status");
			LineSet lineSet = exitContext.getContract().getLines(true);
			TreeMap<Integer, Line> lines = lineSet.getLinesByTypeAndNumber().get(lineDefinition.getName());
			for (Integer lineNumber : lines.keySet()) {
				Line line = lines.get(lineNumber);
				LineValueSet lineValues = line.getValues();
				ArrayList<Object> dataListTOW = lineData.get(lineNumber);

				for (int i = 0; i < dataListTOW.size(); i++) {
					Object fieldData = dataListTOW.get(i);
					if (!fieldData.equals(""))	{
						LineValue lineValue = lineValues.get(headerFields.get(i));
						if (lineValue != null) {
							DataDefinition dd = lineValue.getDataDefinition();
							if (dd.getType() == DataDefinition.Types.TEXT) {
								String value = fieldData.toString();
								//dynamicData
								dyanmicDataActionLog.add(value);								
								lineValue.setValue(value);
								if (value.length() > dd.getDataDomainData().getSize()) {
									lineValue.setValue(value);
								}
							}
							else if (isDataTypeMatch(lineValue, fieldData))
								lineValue.setValue(fieldData);
						} 
					} 
				}
				dyanmicDataActionLog.clear();
			}

			exitContext.getContract().updateLines(false);

		} catch (ConfigurationException e) {			
			LOG.error("ConfigurationException while adding TOW_Commission to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(5, "ConfigurationException");
			dyanmicDataActionLog.add(6, e.getLocalizedMessage());
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM5.trim(), dyanmicDataActionLog, null);					
			String errorCode = ErrorCodes.PE_AM_5;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} catch (ContractException e) {			
			LOG.error("ContractException while adding TOW_Commission to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(5,"ContractException");
			dyanmicDataActionLog.add(6, e.getLocalizedMessage());
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM5.trim(), dyanmicDataActionLog, null);				
			String errorCode = ErrorCodes.PE_AM_5;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} catch (LineValueException e) {			
			LOG.error("LineValueException while adding TOW_Commission to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(5, "LineValueException");
			dyanmicDataActionLog.add(6, e.getLocalizedMessage());
			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM5.trim(), dyanmicDataActionLog, null);				
			String errorCode = ErrorCodes.PE_AM_5;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}		

		LOG.info("End of method ==> addTOWLines for contract Instance");
	}
	
	
	
	
	
	/**
	 * To Check the DataType match 
	 * 
	 * @param lineValue
	 * @param obj
	 * @return
	 */
	private static boolean isDataTypeMatch(LineValue lineValue, Object obj) {
    	int type = lineValue.getDataDefinition().getType();
        boolean result =
            (obj == null) ||
            (type == DataDefinition.Types.TEXT    && obj instanceof String) ||
            (type == DataDefinition.Types.NUMERIC && obj instanceof BigDecimal) ||
            (type == DataDefinition.Types.DATE    && obj instanceof Date);
        return result;
    }



	
	
	
	
	

	/**
	 * Helper method - To do all the operations respective to OU_Department activities
	 * 
	 * @param exitContext
	 * @param templateName
	 * @param owningOrganizationName
	 */
	private void activitiesOfOUDeptLineDefinition(ExitContext exitContext, String templateName, String owningOrganizationName) {
		if(addDepartmentLineDef){			
			ArrayList<CondeNastOUDepartment> ow_dept_data =  getDataFromCondeNastOUDepartmentTable(exitContext, templateName, owningOrganizationName);
			if(ow_dept_data != null){
				if(!ow_dept_data.isEmpty()) {
					//Adding Line - OU_Dept Line Definition.
					addOUDepartmentLines(exitContext, ow_dept_data);				
				} else {				
					LOG.info("The data is not available in Database for this table");
				}
			} else {
				LOG.info("Persistence Exceptoin - ByPass the requirement");
			}
			
		} else {
			LOG.info("This is Disabled");
		}
	}
	
	
	
	/**
	 * Get data from Condenast OU Custom Table
	 * CONDENAST_OU_DEPARTMENT
	 * 
	 * @param templateName
	 * @param owningorg
	 * @return
	 * @throws PluginException
	 * @throws ConfigurationException
	 * @throws WarningMessageException 
	 */
	private ArrayList<CondeNastOUDepartment> getDataFromCondeNastOUDepartmentTable(ExitContext exitContext, String templateName, String owningOrganizationName) {
		LOG.info("Start of method ==> getDataFromCondeNastOUDepartmentTable for contract Instance");
		
		CondeNastOUDepartment condenastoudepartment = new CondeNastOUDepartment();
		// Setting Template Name & Owning Organization into value object
		condenastoudepartment.setTemplatename(templateName);
		condenastoudepartment.setOwningorganization(owningOrganizationName);
		
		CondeNast_OU_DepartmentPersister persister = CondeNast_OU_DepartmentPersister.getInstance();
		SeqDetailsIterator seqdetailiterator = null;
		ArrayList<CondeNastOUDepartment> dbValue = new ArrayList<CondeNastOUDepartment>();
		try	{
			seqdetailiterator = (SeqDetailsIterator) persister.getSeq(condenastoudepartment);
			seqdetailiterator.prefetch();
			if(seqdetailiterator.count() == 0) {
				LOG.error("No Data Available for this Combination of Template name & OwningOrganization");
			} else	{
				dbValue = seqdetailiterator.prefetch();
				LOG.info("Data retrieval was successfull");
			}	
		} catch(PersistenceException e) {	
			LOG.error("PersistenceException while fetching data from Table CONDENAST_OU_DEPARTMENT" + e.getLocalizedMessage());
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null, "PersistenceException while fetching data from Table CONDENAST_OU_DEPARTMENT");					
			String errorCode = ErrorCodes.PE_ER_EX;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return null;
		}
		
		LOG.info("End of method ==> getDataFromCondeNastOUDepartmentTable for contract Instance");
		
		return dbValue;		
	}

	
	
	/**
	 * Method to add lines to OU Department Lines Definitions
	 * 
	 * @param _contract
	 * @param linedefname
	 * @param ouDeptDataFromDB
	 * @param result
	 * @throws PluginException
	 * @throws ContractException
	 * @throws LineValueException
	 */
	private void addOUDepartmentLines(ExitContext exitContext , ArrayList<CondeNastOUDepartment> ouDeptDataFromDB) {
		LOG.info("Start of method ==> addOUDepartmentLines for contract Instance");
		
		//dynamicData to populate in ActionLOG Table
		ArrayList<String> dyanmicDataActionLog = new ArrayList<String>();
		ArrayList<String> toErrorDBList = new ArrayList<String>();
		
		try {
			// Pull all Line Definition & Data Definition names from Configuration file
			// & store into headerFields for next operations		
			ArrayList<String> headerFields = new ArrayList<String>();
			String lineOU_Dept = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT.trim());
			
			String oudepartment = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT_DF1.trim());
			String status = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT_DF2.trim());
			String gap = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT_DF3.trim());
			String comments = getEmpConfig().getRequiredStringConfiguration(ConfigurationConstants.CNE_LINE_DEPT_DF4.trim());
			
			
			headerFields.add(oudepartment);
			headerFields.add(status);
			headerFields.add(gap);
			headerFields.add(comments);
			
			// Following Operations to Check - weather all correct data definition
			// is available for the Line Definition
			LineDefinition lineDefinition = null;
			for (LineDefinition eachAvailLD : availableFinalLineDef) {				
				if (eachAvailLD.getName().equals(lineOU_Dept)) {
					// List to store invalid data definitions
					List<String> pem6 = new ArrayList<String>();		
					for (String eachDataDefintionInConfig : headerFields) {
						DataDefinition dd = eachAvailLD.getField(eachDataDefintionInConfig);
						//String testValue = dd.getName();
						if(dd != null){
							LOG.debug("This DataDefinition is Available in Line Definition :" +eachDataDefintionInConfig);
						} else {
							LOG.debug("The DataDefinition is  NOT Available in Line Definition" +eachDataDefintionInConfig);
							pem6.add(eachDataDefintionInConfig);
						}						
					}

					//If list is NOT empty means some DD are misconfigured, Need to LOG & bypass the requirement
					if(!pem6.isEmpty()){
						for (String eachMissingConfiguredDataDef : pem6) {
							LOG.error("LineValueException while adding OU_Dept to Lines on Populating Entities");
							toErrorDBList.add(lineOU_Dept);
							toErrorDBList.add(eachMissingConfiguredDataDef);
							toErrorDBList.add(exitContext.getContract().getTemplate().getName());

							toErrorDBList.add(exitContext.getContract().getTemplate().getName());							
							String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM6.trim(), toErrorDBList, null );					
							String errorCode = ErrorCodes.PE_AM_6;	

							// Creation of Error object containing all the information need to be dumped to DB
							List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

							// Adding the resultant error object to List handled after the completion of business requirement
							errorListTODB.add(resultErrorObject);	
							toErrorDBList.clear();						}						
						return;									
					}							
					lineDefinition = eachAvailLD;				
				}
			}
			
			// Constructing a Hash Map & putting all the data from Database inside this map
			HashMap<Integer, ArrayList<Object>> lineData = new HashMap<Integer, ArrayList<Object>>();
			int count = 1;
			for(CondeNastOUDepartment eachRow : ouDeptDataFromDB) {
				ArrayList<Object> dataTable = new ArrayList<Object>();
				
				dataTable.add(eachRow.getOperating_unit_dept());			
				dataTable.add(eachRow.getStatus());			
				dataTable.add(eachRow.getGap());
				String eachcomments = eachRow.getComments();
				if((eachcomments == null ) 
					|| ( null != eachcomments && eachcomments.isEmpty())) {
					eachcomments = "-";
					dataTable.add(eachcomments);
				} else {
					dataTable.add(eachcomments);
				}
				lineData.put(count,dataTable);
				count++;				
			}	
			
					
			
			//Pulling out of HashMap & populating this data into the Lines
			int size = lineData.size();
			for (int j = 0 ; j<size ; j++) {
				exitContext.getContract().addLine(lineDefinition);
			}
			
			// Adding the lines Information retrieved from Database to Lines in the contract Template			
			LOG.debug("Adding line to OU department line definition");
			LineSet lineSet = exitContext.getContract().getLines(true);
			TreeMap<Integer, Line> lines = lineSet.getLinesByTypeAndNumber().get(lineDefinition.getName());
			for(Integer lineNumber : lines.keySet()) {
				Line line = lines.get(lineNumber);
				LineValueSet lineValues = line.getValues();
				ArrayList<Object> dataList = lineData.get(lineNumber);
				for (int i = 0; i < dataList.size(); i++) {
					Object fieldData = dataList.get(i);
					if (!fieldData.equals("")) {
						LineValue lineValue = lineValues.get(headerFields.get(i));
						if (lineValue != null) 	{
							DataDefinition dd = lineValue.getDataDefinition();
							if (dd.getType() == DataDefinition.Types.TEXT) 	{
								String value = fieldData.toString();								
								//dynamicData
								dyanmicDataActionLog.add(value);
								
								if (value.length() > dd.getDataDomainData().getSize()) {
									lineValue.setValue(value);
								} else{
									lineValue.setValue(value);
								}
									
							}else if (isDataTypeMatch(lineValue, fieldData))
							lineValue.setValue(fieldData);
						}
					}
				}				
				dyanmicDataActionLog.clear();
			}
			
			exitContext.getContract().updateLines(false);
			
			
		} catch (ConfigurationException e) {
			LOG.error("ConfigurationException while adding OU_Dept to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(4,"ConfigurationException");
			dyanmicDataActionLog.add(5, e.getLocalizedMessage());			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM7.trim(), dyanmicDataActionLog, null);					
			String errorCode = ErrorCodes.PE_AM_7;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		} catch (ContractException e) {
			LOG.error("ContractException while adding OU_Dept to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(4, "ContractException");
			dyanmicDataActionLog.add(5, e.getLocalizedMessage());		
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM7.trim(), dyanmicDataActionLog, null);					
			String errorCode = ErrorCodes.PE_AM_7;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);			
			return;
		} catch (LineValueException e) {
			LOG.error("LineValueException while adding OU_Dept to Lines on Populating Entities" + e.getLocalizedMessage());
			dyanmicDataActionLog.add(4, "LineValueException");
			dyanmicDataActionLog.add(5, e.getLocalizedMessage());			
			String formatErrorMessageToDB = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_PE_AM7.trim(), dyanmicDataActionLog, null);					
			String errorCode = ErrorCodes.PE_AM_7;	
			
			LOG.error("The Error Message going to Action LOG TB :"+ formatErrorMessageToDB);
			LOG.error("The Error Code going to Action LOG TB :" + errorCode);

			// Creation of Error object containing all the information need to be dumped to DB
			List<Object> resultErrorObject = addthistoErrorObject(exitContext , formatErrorMessageToDB, errorCode);

			// Adding the resultant error object to List handled after the completion of business requirement
			errorListTODB.add(resultErrorObject);
			return;
		}		
	
		LOG.info("End of method ==> addOUDepartmentLines for contract Instance");
	}

}




/**
 * 
 */
package com.emptoris.ecm.exit.condenast.util;

import java.util.ArrayList;
import java.util.List;

import com.dicarta.appfound.contractmanager.btv.server.bo.VariableBO;
import com.dicarta.appfound.contractmanager.btv.server.persistence.VariableData;
import com.dicarta.appfound.contractmanager.btv.server.persistence.VariablePersister;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.dicarta.infra.persistence.common.iterator.IDataIterator;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.common.config.EmpConfiguration;
import com.emptoris.ecm.api.Helper.ContractHelper;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.exception.TermValueException;
import com.emptoris.ecm.api.intf.Contract;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.TermDefinition;
import com.emptoris.ecm.api.intf.TermValue;
import com.emptoris.ecm.api.intf.TermValueSet;
import com.emptoris.ecm.exit.common.config.ExitConfig;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;

/**
 * Utility Class - For providing all activities around Contract Terms
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractTermsUtil {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ContractTermsUtil.class);
	
	
	
	/**
	 * Check if a term is available in the specified contract
	 * 
	 * @param contract
	 * @return true: if the term value is available in the contract; false: if
	 *         the term value is not available
	 * @throws PluginException
	 * @exception - ContractException
	 */
	public static boolean isTermAvailableInContract(ExitContext exitContext, String termName)  {
		LOG.info("Begin ==> utility Method isTermAvailableInContract");
		
		boolean isTermAvailable = false;
		
		Contract contract = exitContext.getContract();

		if (null == contract || null == termName) {
			LOG.debug("Invalid input parameter for isTermAvaiableForContract; " +
					"Contract "	+ contract + ", Term Name:" + termName);
		}

		LOG.debug("Starting to check if term " + termName
				+ "is available in contract " + contract.getNumber());
		// retrieve the term needed to be checked from the contract
		TermValue termValue;
		try {
			termValue = ContractHelper.getTermValueObj(contract, termName);
			// check if it's available in the contract
			if (null != termValue) {
				LOG.debug("Term " + termName + " is available for contract "
						+ contract.getId());
				isTermAvailable = true;

			} else {
				LOG.debug("Term " + termName + " is NOT available for contract "
						+ contract.getId());			
			}
		} catch (ContractException e) {			
			LOG.error("Failed While checking is termAvailable" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return false;
		}	
		
		LOG.info("End ==> utility Method isTermAvailableInContract");		
		return isTermAvailable;
	}
	
	
	
	
	/**
	 * Utility method to retrieve the value entered for the term
	 * whenever the term is in any of below formats
	 * a) String
	 * b) Date
	 * c) Numeric (BigDecimal)
	 * 
	 * @param contract
	 * @param termName
	 * @return object - as Object being super class for Date, Numeric & String
	 * @throws ContractException
	 */
	public static Object getTermValue(ExitContext exitContext, String termName) {	
		
		Contract contract = exitContext.getContract();		
		Object termResult = null;
		TermValue TermValueObj;
		try {
			TermValueObj = ContractHelper.getTermValueObj(contract, termName);
			if(null != TermValueObj) {
				int termType = TermValueObj.getDefinition().getType();
					if(TermDefinition.Types.TEXT == termType) { 
						termResult = TermValueObj.getTextValue();					
					} else if(TermDefinition.Types.NUMERIC == termType) { 
						termResult = TermValueObj.getNumericValue();					
					} else if(TermDefinition.Types.DATE == termType){
						termResult = TermValueObj.getDateValue();
					}
			}
			
			return termResult;
		} catch (ContractException e) {
			LOG.error("Contract process interuptted due to Contract Exception " +
					"while accessing the Term value");	
			LOG.error("Sending the termResult as -> ExceptionToSkipFromRequirment for further process & Not " +
					"stop the process");					
			termResult = "ExceptionToSkipFromRequirment";
			return termResult;
			
		}
		
		
		
	}




	/**
	 * Utility method used to find with the available list are these terms available in contract template
	 * 
	 * @param exitContext
	 * @param termsToCheckAvailList
	 */
	public static ArrayList<String> areTermsAvailInContract(ExitContext exitContext,
			List<String> termsToCheckAvailList) {
		
		 ArrayList<String> termsNotAvailInList = new ArrayList<String>();
		 
		for (String eachTermKey : termsToCheckAvailList) {
			  
			String eachTermkeyValue = getTermName(exitContext, eachTermKey);
			Contract contract = exitContext.getContract();		
			Object termResult = null;
			TermValue TermValueObj;
			try {
				TermValueObj = ContractHelper.getTermValueObj(contract, eachTermkeyValue);
				if(null != TermValueObj) {
					int termType = TermValueObj.getDefinition().getType();
					if(TermDefinition.Types.TEXT == termType) { 
						termResult = TermValueObj.getTextValue();					
					} else if(TermDefinition.Types.NUMERIC == termType) { 
						termResult = TermValueObj.getNumericValue();					
					} else if(TermDefinition.Types.DATE == termType){
						termResult = TermValueObj.getDateValue();
					}
				} else {
					termsNotAvailInList.add(eachTermkeyValue);
				}
			} catch (ContractException e) {
				LOG.error("Failed While checking is areTermsAvailInContract" + e.getLocalizedMessage());
				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException"); 
				String errorCode = ErrorCodes.CS_ER_EX;			
				ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
				return null;
			}
		}
		
		return termsNotAvailInList;
		
		
	}




	/**
	 * @param eachTermKey
	 * @return
	 */
	public static String getTermName(ExitContext exitContext, String eachTermKey) {
		String configValuePart = null;
		EmpConfiguration empConfig;
		try {
			empConfig = ExitConfig.getExitConfig();			
			configValuePart = empConfig.getRequiredStringConfiguration(eachTermKey);
			
		} catch (ConfigurationException e) {
			LOG.error("Failed While getting -> getTermName" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ConfigurationException"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return null;
		}
		
		// This is Term Name
		return configValuePart;
		
	}	
	
	
		
	
	
	/**Check Term in ECM
	 * 
	 * @param term
	 * @param wizardid
	 * @param _contract
	 * @param user
	 * @return
	 * @throws PluginException
	 * @throws WarningMessageException
	 */
	public static Boolean isTermAvailableInECM(String term, ExitContext exitContext) {
		
		boolean isTermsAvailableInECM = false;
		
	  List<VariableData> variableDataList = null;
	  ArrayList<String> termInternalNames = new ArrayList<String>();
	  termInternalNames.add(term);
	  //Boolean terminecm = true;
     try {                
            String sqlWhere = "internal_name in (";
            StringBuffer sqlWhereSB = new StringBuffer(sqlWhere);
            
            int size = termInternalNames.size();            
            int counter = 0;
            
            for (String termInternalName : termInternalNames) {
                  counter++;
                  sqlWhereSB.append("'" + termInternalName + "'");                     
                  if (counter < size) {
                         sqlWhereSB.append(",");
                  }
                  
            }             
            sqlWhereSB.append(")");           
            sqlWhere = sqlWhereSB.toString();
            
            IDataIterator iter  = VariablePersister.getInstance().findBySQLWhere(sqlWhere);
            int count = 0;
            if (null != iter) {
                  variableDataList =  iter.prefetch(); 
				  count = variableDataList.size(); 	
            }
            if(count == 0){
            	isTermsAvailableInECM = false;
            	
            }
            else{
            	isTermsAvailableInECM = true;
            }

     } catch (PersistenceException e) {
    	 LOG.error("Failed While getting -> isTermAvailableInECM" + e.getLocalizedMessage());
    	 String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException"); 
    	 String errorCode = ErrorCodes.CS_ER_EX;			
    	 ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
    	 
     } 
    
     return isTermsAvailableInECM;
}
	
	
	
	
	/**
	 * @param exitContext
	 * @param termName
	 * @return
	 * @throws PluginException
	 * @throws ContractException
	 */
	public static TermValue getTermFromDraftRevContract(ExitContext exitContext,String termName)	{
		LOG.info("Start of method ==> getTermFromDraftRevContract for contract Instance");
		TermValue termValue = null;
		try {
			TermValueSet termValueSet = exitContext.getContract().getTermValues();
			// check to make sure the contract contains the specified term
			termValue = termValueSet.get(termName);
			if (termValue == null) {
				LOG.debug("No Term value with term name:" + termName);
				termValue = null;
			}			
			
		} catch (ContractException e) {
			LOG.error("Failed While getting -> isTermAvailableInECM" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return null;
		}		
		
		LOG.info("End of method ==> getTermFromDraftRevContract for contract Instance");
		return termValue;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * To get the DataType of the contract's Term
	 * 
	 * @param termname
	 * @return
	 */
	public static String getTermDataType(ExitContext exitContext, String termname) {
		LOG.info("Start of method ==> getTermDataTypeByInternalName for contract Instance");
		LOG.debug("Fetching the term data type");

		String dataType = null;		
		try {
			VariableData variableData = VariableBO.getInstance().getVariableByInternalName(termname);			
			if (null != variableData) {
				dataType = variableData.getType();
			}
		} catch (ObjectNotFoundException e) {
			LOG.error("Failed While getting Term dataType : " + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_getTermDataType"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return null;
		}

		LOG.info("End of method ==> getTermDataTypeByInternalName for contract Instance");
		// returns the dataType of any Term provided
		return dataType;
	}
	
	
	
	
	
	/**
	 * Utility method - To check whether the Term's value is changed or Not
	 * 
	 * @param exitContext
	 * @param termName
	 * @return
	 */
	public static boolean isBTVValueChanged(ExitContext exitContext, String termName){

		boolean isValueChanged = false;

		try {
			//Get the value after SAVE was clicked - [DRAFT revision Number Value]
			TermValue draftRevTerm = ContractTermsUtil.getTermFromDraftRevContract(exitContext,termName);				
			isValueChanged =  draftRevTerm.isValueUpdated();
		} catch (TermValueException e) {
			LOG.error("Failed While checking whether BTV Value is changed : " + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "TermValueException_isBTVValueChanged"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return false;
		}
		
		return isValueChanged;
	}




	/**
	 * Utility Method - To check is the term Language Term
	 * @param exitContext
	 * @param masterTermName
	 * @return
	 */
	public static boolean isThisLanguageTerm(ExitContext exitContext,
			String masterTermName) {
		boolean isLangTerm = false;
		
		TermValue draftRevTerm = ContractTermsUtil.getTermFromDraftRevContract(exitContext,masterTermName);				
		isLangTerm =  draftRevTerm.isLanguageTerm();
		
		return isLangTerm;		
	}
	
	
	
	
	
	

	

}

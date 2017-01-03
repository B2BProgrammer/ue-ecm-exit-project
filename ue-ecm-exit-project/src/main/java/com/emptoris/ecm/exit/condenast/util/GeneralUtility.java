/**
 * 
 */
package com.emptoris.ecm.exit.condenast.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dicarta.appfound.common.IDraftable;
import com.dicarta.appfound.common.IdRevisionData;
import com.dicarta.appfound.contract.server.bo.ContractBO;


import com.dicarta.infra.common.IdData;
import com.dicarta.infra.common.IdName;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.organization.server.bo.OrganizationBO;
import com.dicarta.infra.organization.server.persistence.OrganizationData;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.dicarta.infra.persistence.common.iterator.IDataIterator;
import com.emptoris.ecm.api.exception.ContractException;
import com.emptoris.ecm.api.intf.Contract;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.User;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;


/**
 * @author Ajith.Ajjarani
 *
 */
public class GeneralUtility {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(GeneralUtility.class);

	/**
	 * Method to fetch users
	 * @param _contract
	 * @return
	 */
	public static String getUser(ExitContext exitContext) {
		User user = null;
		try {
			user = exitContext.getContract().getCreator();
		} catch (ContractException e) {				
			LOG.error("Failed to initiate the instance of CustomExitContext:" + e.getLocalizedMessage());
			String errorMessage = MessagekeyConstants.MSG_CNE_EXE.trim(); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);			
		}
		
		return user.getCreatedBy();
		
	}

	/**
	 * @param exitContext
	 * @return
	 */
	public static boolean isContractFromWizard(ExitContext exitContext) {
		boolean isContractFromWizard = false;
		String wizardId = exitContext.getWizardId();
		
		if(wizardId == null || wizardId.isEmpty()){
			isContractFromWizard = false;
		} else {
			isContractFromWizard = true;
		}
		return isContractFromWizard;
	}
	
	
	
	
	
	/**
	 * Method to add prefixed zero to contract number
	 * @param contractnumber
	 * @param zeroFiller
	 * @return
	 */
	public StringBuffer getZeroPrefixedContractNumber(int contractnumber,String zeroFiller) {
		String contractNumberSTR = String.valueOf(contractnumber); 
		StringBuffer tmpStrBuffer = null;
		if(null!=contractNumberSTR){
			int PositionToFill = 10 - contractNumberSTR.length(); 
			if( 0 < PositionToFill) { 
				tmpStrBuffer = new StringBuffer(); 
			} 
			for(int i = 0 ; i < PositionToFill ; i++) { 
				tmpStrBuffer.append(zeroFiller);
			}
		}
		tmpStrBuffer.append(contractNumberSTR);
		return tmpStrBuffer;
	}
	
	
	
	/**
	 * Utility method to pad character to left to input string if totallength
	 * value is less than supplied string then actual input string will be
	 * returned USAGE - UserExitCommonUtil.padCharsLeft("temp", 'A', 6) will
	 * return AAtemp *
	 * 
	 * @param inStr
	 *            - input string to which character will be padded
	 * @param filler
	 *            - character which will be padded
	 * @param totalLength
	 *            - total length after padding
	 * @return string - output string after padding
	 */
	public static String padCharsLeft(String inStr, final char filler,	final int totalLength) {

		StringBuffer tmpStrBuffer = null;
		if (null != inStr) {
			int positionToFill = totalLength - inStr.length();
			if (0 < positionToFill) {
				tmpStrBuffer = new StringBuffer();
			}
			for (int i = 0; i < positionToFill; i++) {
				tmpStrBuffer.append(filler);
			}
		}
		if (null != tmpStrBuffer) {
			return tmpStrBuffer.append(inStr).toString();
		} else {
			return inStr;
		}
	}	
	
	
	
	
	/**check if the input contrat's status is in post execution status
	 * @param contract
	 * @return
	 */
	public static boolean isContractStatus(ExitContext exitContext) {
	
		//if the input contrat's status is in post execution status, return true;
		int contractStatus = exitContext.getContract().getStatus();
		if (contractStatus ==  Contract.Statuses.EXECUTED 
				|| contractStatus == Contract.Statuses.ACTIVE 	
				|| contractStatus ==Contract.Statuses.TERMINATED
				|| contractStatus == Contract.Statuses.EXPIRED) {
			
			return true;
		}
		
		//otherwise, return false
		return false;
	}

	/**
	 * Utility method - To find the internal primary party Name
	 * @param exitContext
	 * @return
	 */
	public static String getPrimaryPartyName(ExitContext exitContext) {

		String contractOwner = "";

		try {
			// To retrieve the Internal primary party -> country name
			List<IdName> parties = new ArrayList<IdName>();
			IdRevisionData iRd = new IdRevisionData(exitContext.getContract().getId(),	IDraftable.DRAFT_REVISION);
			IDataIterator primaryParties = ContractBO.getContractInstance().getContractInternalPrimaryParty(iRd);
			String primaryPartyID = "";
			parties = primaryParties.prefetch();
			
			for (Iterator<IdName> iterator = parties.iterator(); iterator.hasNext();) {
				IdName primaryPartyObj = iterator.next();
				primaryPartyID = primaryPartyObj.getId();
			}
			
			
			

			OrganizationData orgData = OrganizationBO.getInstance().findOrganizationById(new IdData(primaryPartyID));
			contractOwner = orgData.getCreatedByName();
			System.out.println("Created by By Name "+orgData.getCreatedByName());
			System.out.println("Created by By "+orgData.getCreatedBy());			
			LOG.info("The Contract Owner of Contract is :" +contractOwner);
			
			
		} catch (PersistenceException e) {			
			LOG.error("PersistenceException While fetching primary parties and organization ID" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_Primary_Party"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return contractOwner;
		
		}
		
		return contractOwner;		
	}
	
	/**
	 * Utility Method - To find & send the internal Contact of the primary contact on contract
	 * 
	 * @param exitContext
	 * @return
	 */
	public static String getPrimaryContactName(ExitContext exitContext){
		
		String FullName = null;
		try {
			// First Name of Internal Contact
			String FirstName = exitContext.getContract().getInternalContact().getFirstName();
			//Last Name of Internal Contact
			String LastName =  exitContext.getContract().getInternalContact().getLastName();
			
			FullName = FirstName + " " + LastName;
		} catch (ContractException e) {			
			LOG.error("ContractException While fetching InternalContact" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ContractException_InternalContact"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return FullName;
		
		}
		
		// Final Full last name of Internal Contact
		return FullName;
	}
	


}

package com.emptoris.ecm.exit.condenast.implsix;

import java.util.List;

import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.constants.EnabledFlags;
import com.emptoris.ecm.exit.condenast.custom.CustomUserExitBase;

public class ContractSendToSAP extends CustomUserExitBase{

	public ContractSendToSAP(ExitContext exitContext) {
		super(exitContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getFlagConfigKey() {
		return EnabledFlags.CNE_REQ_6_FLAG.trim();	
	}

	@Override
	protected List<String> getRequiredTermsConfigKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<String> getRequiredConfigConstants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getMessageConfigValueNotAvail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getMessageTermNotAvailInECM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getErrorCodeForConfigValueNotAvail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getErrorCodeForTermNotAvailInECM() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Helper method - To validate all the preconditions before executing the logic
	 * 
	 * @param exitContext
	 * @return
	 */
	public boolean isAllPreConditionsSatisfied(ExitContext exitContext) {
		boolean isPreConditionSet = false;		
		
		// Send True - if It is only Contract, not for amendement
		isPreConditionSet = !exitContext.getContract().isAmendment();		
		return isPreConditionSet;
	}

	public boolean checkFlag() {
		boolean isEnabled = isFlagEnabled();
		
		return isEnabled;
	}

	public void executeLogic(ExitContext exitContext) {
		// TODO Auto-generated method stub
		
	}

}

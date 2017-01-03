package com.emptoris.ecm.exit;

import com.dicarta.appfound.common.IUserInfo;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.intf.Contract;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.CustomExitContext;

/**
 * Parent class for operations performed from the main Exit class.
 */
public abstract class ExitSubroutine extends ExitRoutine {
    private final ILogger LOG  = Logger.getLogger(ExitSubroutine.class);

    private static final String EXIT_NAME = "ExitSubroutine: ";
    private static final String LOG_INFO_MISSING = "Contract: ???: ???";

    // Common logging information
    protected String _logInfo = LOG_INFO_MISSING;
    protected String _contractNumber = null;
    protected String _contractName   = null;

    protected Contract          _contract  = null;
    protected IUserInfo 		_user = null;

    public ExitSubroutine(ExitContext context, CustomExitContext cec) {
        super(context, cec);
        // For convenience...
        _contract = _context.getContract();
        _user = _context.getInternalUserObject();
        try {
            this._contractNumber = "" + _contract.getNumber();
            this._contractName   = _contract.getName();
            this._logInfo    = "Contract: " + this._contractNumber + ": " + this._contractName;
        } catch (NullPointerException e) {
            LOG.error(EXIT_NAME + "Error getting contract number or name", e);
        }
    }
}

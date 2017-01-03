try {
    com.emptoris.ecm.api.intf.ExitContext exitContext = null;
    if (WizardId != null) {
        exitContext =
            new com.emptoris.ecm.api.impl.ExitContextImpl(
                (com.dicarta.appfound.contract.ContractData) ContractObject,
                (java.lang.String) WizardId,
                (com.dicarta.appfound.common.IUserInfo) UserObject,
                com.emptoris.ecm.api.intf.ExitContext.ExitTypes.POST_CREATE
        );
    } else {
        exitContext =
            new com.emptoris.ecm.api.impl.ExitContextImpl(
                (com.dicarta.appfound.contract.ContractData) ContractObject,
                (com.dicarta.appfound.common.IUserInfo) UserObject,
                com.emptoris.ecm.api.intf.ExitContext.ExitTypes.POST_CREATE
        );
    }

    com.emptoris.ecm.exit.IExit exit = new com.emptoris.ecm.exit.Exit();
    exit.postContractCreate(exitContext);
} catch ( com.dicarta.infra.common.WarningMessageException ex ) {
    System.out.println(ex.getMessage());
    throw ex;
} catch ( com.dicarta.infra.common.PluginException ex ) {
    System.out.println(ex.getMessage());
    throw ex;
} catch ( Throwable ex ) {
    System.out.println("postContractCreate Throwable");
    ex.printStackTrace(System.out);
    throw ex;
}
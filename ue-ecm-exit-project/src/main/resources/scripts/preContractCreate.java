try {
com.emptoris.ecm.api.intf.ExitContext exitContext =
    new com.emptoris.ecm.api.impl.ExitContextImpl(
        (com.dicarta.appfound.contractmanager.contract.ICreateContractDetails) CreateContractDetails,
        (com.dicarta.appfound.common.IUserInfo) UserObject,
        com.emptoris.ecm.api.intf.ExitContext.ExitTypes.PRE_CREATE
    );
com.emptoris.ecm.exit.IExit exit = new com.emptoris.ecm.exit.Exit();
exit.preContractCreate(exitContext);
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
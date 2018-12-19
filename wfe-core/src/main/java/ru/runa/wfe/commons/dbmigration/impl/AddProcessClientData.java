package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddProcessClientData extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
            getDDLCreateColumn("bpm_process", new BigintColumnDef("client_data")),
            getDDLCreateIndex("bpm_process", "ix_process_client_data", "client_data")
        );
    }
}

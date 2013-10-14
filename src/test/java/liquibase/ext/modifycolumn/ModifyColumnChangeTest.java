package liquibase.ext.modifycolumn;


import liquibase.change.ChangeFactory;
import liquibase.change.ColumnConfig;
import liquibase.change.core.*;
import static org.junit.Assert.*;

import liquibase.database.core.OracleDatabase;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

public class ModifyColumnChangeTest { //extends AbstractChangeTest {

    ModifyColumnChange change;

    @Before
    public void setUp() throws Exception {
        change = new ModifyColumnChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("NAME");
        col1.setType("integer(3)");

        change.addColumn(col1);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("modifyColumn", ChangeFactory.getInstance().getChangeMetaData(change).getName());
    }

    @Test
    public void getRefactoringDescription() throws Exception {
        assertEquals("Modify column definition", ChangeFactory.getInstance().getChangeMetaData(change).getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        OracleDatabase database = new OracleDatabase();
        assertEquals("ALTER TABLE TABLE_NAME MODIFY ( NAME INTEGER )", SqlGeneratorFactory.getInstance().generateSql(change.generateStatements(database)[0], database)[0].toSql());
    }
//
//    @Override
//    @Test
//    public void getConfirmationMessage() throws Exception {
//        assertEquals("Columns NAME(integer(3)) of TABLE_NAME modified", change.getConfirmationMessage());
//    }
//
}

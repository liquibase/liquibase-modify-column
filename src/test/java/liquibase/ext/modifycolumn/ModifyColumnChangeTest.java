package liquibase.ext.modifycolumn;


import liquibase.change.ColumnConfig;
import liquibase.change.core.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public abstract class ModifyColumnChangeTest { //extends AbstractChangeTest {

//    liquibase.change.core.ModifyColumnChange change;
//
//    @Before
//    public void setUp() throws Exception {
//        change = new ModifyColumnChange();
//        change.setTableName("TABLE_NAME");
//
//        ColumnConfig col1 = new ColumnConfig();
//        col1.setName("NAME");
//        col1.setType("integer(3)");
//
//        change.addColumn(col1);
//    }
//
//    @Override
//    @Test
//    public void getRefactoringName() throws Exception {
//        assertEquals("Modify Column", change.getChangeMetaData().getDescription());
//    }
//
////    @Test
////    public void generateStatement() throws Exception {
////        OracleDatabase database = new OracleDatabase();
////        assertEquals("ALTER TABLE TABLE_NAME MODIFY (NAME integer(3))", change.generateStatements(database)[0].getSqlStatement(database));
////    }
//
//    @Override
//    @Test
//    public void getConfirmationMessage() throws Exception {
//        assertEquals("Columns NAME(integer(3)) of TABLE_NAME modified", change.getConfirmationMessage());
//    }
//
}
package liquibase.ext.modifycolumn;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ModifyColumnGenerator extends AbstractSqlGenerator<ModifyColumnStatement> {

    @Override
    public Warnings warn(ModifyColumnStatement modifyColumnStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Warnings();
    }

    public ValidationErrors validate(ModifyColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumns());

        for (ColumnConfig column : statement.getColumns()) {
            if (column.getConstraints() != null && column.getConstraints().isPrimaryKey() && (database instanceof H2Database
                    || database instanceof DB2Database
                    || database instanceof DerbyDatabase
                    || database instanceof SQLiteDatabase)) {
                validationErrors.addError("Adding primary key columns is not supported on "+database.getShortName());
            }
        }

        return validationErrors;
    }

    public Sql[] generateSql(ModifyColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(statement, database);
        }

        List<Sql> sql = new ArrayList<Sql>();
        for (ColumnConfig column : statement.getColumns()) {
            String alterTable = "ALTER TABLE " + database.escapeTableName(null, statement.getSchemaName(), statement.getTableName());

            // add "MODIFY"
            alterTable += " " + getModifyString(database) + " ";

            // add column name
            alterTable += database.escapeColumnName(null, statement.getSchemaName(), statement.getTableName(), column.getName());

            alterTable += getPreDataTypeString(database); // adds a space if nothing else

            // add column type
            alterTable += DataTypeFactory.getInstance().fromDescription(column.getType(), database).toDatabaseDataType(database);

            if (supportsExtraMetaData(database)) {
                if (column.getConstraints() != null && !column.getConstraints().isNullable()) {
                    alterTable += " NOT NULL";
                } else {
                    if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
                        alterTable += " NULL";
                    }
                }

                alterTable += getDefaultClause(column, database);

                if (column.isAutoIncrement() != null && column.isAutoIncrement()) {
                    alterTable += " " + database.getAutoIncrementClause(BigInteger.ONE, BigInteger.ONE);
                }

                if (column.getConstraints() != null && column.getConstraints().isPrimaryKey()) {
                    alterTable += " PRIMARY KEY";
                }
            }

            alterTable += getPostDataTypeString(database);

            sql.add(new UnparsedSql(alterTable));
        }

        return sql.toArray(new Sql[sql.size()]);

    }

    private Sql[] generateStatementsForSQLiteDatabase(final ModifyColumnStatement statement, Database database) {

		// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...

    	List<Sql> statements = new ArrayList<Sql>();

//todo: fixup    	// define alter table logic
//		SQLiteDatabase.AlterTableVisitor rename_alter_visitor =
//		new SQLiteDatabase.AlterTableVisitor() {
//			public ColumnConfig[] getColumnsToAdd() {
//				return new ColumnConfig[0];
//			}
//			public boolean copyThisColumn(ColumnConfig column) {
//				return true;
//			}
//			public boolean createThisColumn(ColumnConfig column) {
//				for (ColumnConfig cur_column: statement.getColumns()) {
//					if (cur_column.getName().equals(column.getName())) {
//						column.setType(cur_column.getType());
//						break;
//					}
//				}
//				return true;
//			}
//			public boolean createThisIndex(Index index) {
//				return true;
//			}
//		};
//
//    	try {
//    		// alter table
//            for (SqlStatement alterStatement : SQLiteDatabase.getAlterTableStatements(
//                    rename_alter_visitor, database, statement.getSchemaName(), statement.getTableName())) {
//                statements.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterStatement, database)));
//            }
//
//		} catch (Exception e) {
//			System.err.println(e);
//			e.printStackTrace();
//		}

    	return statements.toArray(new Sql[statements.size()]);
    }

    /**
     * Whether the ALTER command can take things like "DEFAULT VALUE" or "PRIMARY KEY" as well as type changes
     *
     * @param database
     * @return true/false whether extra information can be included
     */
    private boolean supportsExtraMetaData(Database database) {
        if (database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase) {
            return true;
        }

        return false;
    }

    /**
     * @return either "MODIFY" or "ALTER COLUMN" depending on the current db
     */
    private String getModifyString(Database database) {
        if (database instanceof HsqlDatabase
                 || database  instanceof H2Database
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof MSSQLDatabase) {
            return "ALTER COLUMN";
        } else if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MySQLDatabase) {
            return "MODIFY";
        } else if (database instanceof OracleDatabase) {
            return "MODIFY (";
        } else {
            return "ALTER COLUMN";
        }
    }

    /**
     * @return the string that comes before the column type
     *         definition (like 'set data type' for derby or an open parentheses for Oracle)
     */
    private String getPreDataTypeString(Database database) {
        if (database instanceof DerbyDatabase
                || database instanceof DB2Database) {
            return " SET DATA TYPE ";
        } else if (database instanceof SybaseASADatabase
                || database instanceof SybaseDatabase
                || database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase
                || database instanceof HsqlDatabase
                 || database  instanceof H2Database
                || database instanceof OracleDatabase) {
            return " ";
        } else {
            return " TYPE ";
        }
    }

    /**
     * @return the string that comes after the column type definition (like a close parentheses for Oracle)
     */
    private String getPostDataTypeString(Database database) {
        if (database instanceof OracleDatabase) {
            return " )";
        } else {
            return "";
        }
    }

    private String getDefaultClause(ColumnConfig column, Database database) {
        String clause = "";
        String defaultValue = column.getDefaultValue();
        if (defaultValue != null) {
            if (database instanceof MySQLDatabase) {
                clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            }
        }
        return clause;
    }

}

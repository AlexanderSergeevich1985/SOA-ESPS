package com.soaesps.core.Utils.sql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SQLHelper {
    public enum Constraints {
        NOT_NULL("NOT NULL"),
        UNIQUE("UNIQUE"),
        PRIMARY_KEY("PRIMARY KEY"),
        FOREIGN_KEY("FOREIGN KEY"),
        CHECK("CHECK"),
        DEFAULT("DEFAULT"),
        INDEX("INDEX");

        private String name;

        Constraints(final String name) {
            this.name = name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return this.name;
        }
        public String appendConstrait(String value) {
            return value.concat(" ").concat(name);
        }
    }

    public enum Vendor {
        MySQL("MySQL"),
        Access("Access"),
        SQLServer("SQLServer"),
        ORACLE("ORACLE");

        private String vendor;

        Vendor(final String vendor) {
            this.vendor = vendor;
        }
        public void setVendor(String vendor) {
            this.vendor = vendor;
        }
        public String getVendor() {
            return this.vendor;
        }
    }

    public enum AutoIncrement {
        MySQL(Vendor.MySQL, AutoIncrStr.FOREIGN_KEY),
        Access(Vendor.Access, AutoIncrStr.FOREIGN_KEY),
        SQLServer(Vendor.SQLServer, AutoIncrStr.FOREIGN_KEY),
        Oracle(Vendor.ORACLE, AutoIncrStr.FOREIGN_KEY);

        private Vendor vendor;
        private AutoIncrStr autoIncrStr;

        AutoIncrement(Vendor vendor, AutoIncrStr autoIncrStr) {
            this.vendor = vendor;
            this.autoIncrStr = autoIncrStr;
        }

        public void setVendor(String vendor) {
            this.vendor.setVendor(vendor);
        }
        public String getVendor() {
            return vendor.getVendor();
        }

        public void setAutoIncrStr(String autoIncrStr) {
            this.autoIncrStr.setAutoIncrStr(autoIncrStr);
        }
        public String getAutoIncrStr(String value) {
            return autoIncrStr.appendConstrait(value);
        }

        public enum AutoIncrStr {
            NOT_NULL("NOT NULL AUTO_INCREMENT"),
            UNIQUE("IDENTITY(%s,%s) PRIMARY KEY"),
            PRIMARY_KEY("PRIMARY KEY AUTOINCREMENT"),
            FOREIGN_KEY("CREATE SEQUENCE %s MINVALUE %s START WITH %s INCREMENT BY %s CACHE %s;");

            private String autoIncrStr;

            AutoIncrStr(final String autoIncrStr) {
                this.autoIncrStr = autoIncrStr;
            }
            public void setAutoIncrStr(String autoIncrStr) {
                this.autoIncrStr = autoIncrStr;
            }
            public String getAutoIncrStr() {
                return this.autoIncrStr;
            }
            public String appendConstrait(String value) {
                return value.concat(" ").concat(autoIncrStr);
            }
        }
    }

    static public String SELECT_PATTERN = "SELECT %1$s FROM %2$s WHERE %3$s";

    static public String composeSelect(final String what, final String from, final String condition) {
        return String.format(SELECT_PATTERN, what, from, condition);
    }

    static public String MERGE_PATTERN = "MERGE INTO %1$s USING DUAL ON (%2$s) " +
            "WHEN MATCHED THEN UPDATE SET (%3$s) " +
            "WHEN NOT MATCHED THEN INSERT (%4$s) VALUES (%5$s)";

    static public String composeMerge(final String table, final String on, final String update, final String vars, final String values) {
        return String.format(MERGE_PATTERN, table, on, update, vars, values);
    }

    static public String getWhatUpdate(final Map<String, Object> values) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<String, Object>> iter = values.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, Object> e = iter.next();
            builder.append(e.getKey()).append("=").append("'").append(e.getValue().toString()).append("'");
            if (iter.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    static public String getWhatInsert(final Map<String, Object> values) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iter = values.keySet().iterator();
        while (iter.hasNext()) {
            final String key = iter.next();
            builder.append(key);
            if (iter.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    static public String getValuesInsert(final Map<String, Object> values) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iter = values.values().stream().map(Object::toString).collect(Collectors.toList()).iterator();
        while (iter.hasNext()) {
            final String value = iter.next();
            builder.append("'").append(value).append("'");
            if (iter.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    static public String storedProcedure(String procedureName, String sqlStatement) {
        return String.format("CREATE PROCEDURE %s AS %s;", procedureName, sqlStatement);
    }

    static public String execProcedure(String procedureName) {
        return String.format("EXEC %s;", procedureName);
    }

    static public String wrapStatement(String sqlStatement) {
        return String.format("( %s )", sqlStatement);
    }

    static public String wrapStatementAS(String sqlStatement, String alias) {
        return String.format("( %s ) AS %s", sqlStatement, alias);
    }

    static public String createDatabase(String databaseName) {
        return String.format("CREATE DATABASE %s;", databaseName);
    }

    static public String dropDatabase(String databaseName) {
        return String.format("DROP DATABASE %s;", databaseName);
    }

    static public String createTable(String tableName, LinkedHashMap<String, String> columns) {
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append(tableName);
        builder.append(" (");
        int counter = columns.size();
        for(Map.Entry<String, String> entry: columns.entrySet()) {
            if(counter > 1)
                builder.append(entry.getKey().concat(" ")).append(entry.getValue().concat(", "));
            else
                builder.append(entry.getKey().concat(" ")).append(entry.getValue());
            --counter;
        }
        builder.append(");");
        return builder.toString();
    }

    static public String dropTable(String tableName) {
        return String.format("DROP TABLE %s;", tableName);
    }

    static public String truncateTable(String tableName) {
        return String.format("TRUNCATE TABLE %s;", tableName);
    }

    static public String alterTable(String tableName, String columnName, String dataType) {
        return String.format("ALTER TABLE %s ADD %s %s;", tableName, columnName, dataType);
    }

    static public String alterTable(String tableName, String columnName, String dataType, String vendor) {
        String result = null;
        switch(vendor) {
            case "MSAccess": case "SQLSever":
                result = String.format("ALTER TABLE %s ALTER COLUMN %s %s;", tableName, columnName, dataType);
                break;
            case "MySQL": case "Oracle":
                result = String.format("ALTER TABLE %s MODIFY COLUMN %s %s;", tableName, columnName, dataType);
                break;
            case "Oracle10G+":
                result = String.format("ALTER TABLE %s MODIFY %s %s;", tableName, columnName, dataType);
                break;
        }
        return result;
    }

    static public String alterTable(String tableName, String columnName) {
        return String.format("ALTER TABLE %s DROP COLUMN %s;", tableName, columnName);
    }

    static public String createIndex(String tableName, String indexName, String columnNames) {
        return String.format("CREATE INDEX %s ON %s %s;", indexName, tableName, columnNames);
    }

    static public String createUniqueIndex(String tableName, String indexName, String columnNames) {
        return String.format("CREATE UNIQUE INDEX %s ON %s %s;", indexName, tableName, columnNames);
    }

    static public String dropIndex(String indexName) {
        return String.format("DROP INDEX %s;", indexName);
    }

    static public String dropIndex(String tableName, String indexName, String vendor) {
        String result = null;
        switch(vendor) {
            case "MSAccess":
                result = String.format("DROP INDEX %s ON %s;", indexName, tableName);
                break;
            case "SQLSever":
                result = String.format("DROP INDEX %s.%s;", tableName, indexName);
                break;
            case "MySQL":
                result = String.format("ALTER TABLE %s DROP INDEX %s;", tableName, indexName);
                break;
        }
        return result;
    }

    static public String createView(String viewName, String whatSelect, String tableName, String conditions) {
        return String.format("CREATE VIEW %s AS SELECT %s FROM %s WHERE %s;", viewName, whatSelect, tableName, conditions);
    }

    static public String updateView(String viewName, String whatSelect, String tableName, String conditions) {
        return String.format("CREATE OR REPLACE VIEW %s AS SELECT %s FROM %s WHERE %s;", viewName, whatSelect, tableName, conditions);
    }

    static public String dropView(String viewName) {
        return String.format("DROP VIEW %s;", viewName);
    }

    static public String insertIntoTable(String tableName, List<String> columnNames, List<String> columnValues) {
        if(columnNames == null || columnValues == null || columnNames.size() != columnValues.size()) return null;
        StringBuilder builderColumns = new StringBuilder("(");
        StringBuilder builderValues = new StringBuilder("(");
        Iterator<String> cni = columnNames.iterator();
        Iterator<String> cvi = columnValues.iterator();
        while(cni.hasNext() && cvi.hasNext()) {
            builderColumns.append(cni.next());
            builderValues.append(cvi.next());
            if(cni.hasNext()) {
                builderColumns.append(", ");
                builderValues.append(", ");
            }
        }
        builderColumns.append(") ");
        builderValues.append(")");
        return String.format("INSERT INTO %s %s VALUES %s;", tableName, builderColumns.toString(), builderValues.toString());
    }

    static public String updateTable(String tableName, String columnName, String newValue, String conditions) {
        return String.format("UPDATE %s SET %s = %s WHERE %s;", tableName, columnName, newValue, conditions);
    }

    static public String deleteRecords(String tableName, String conditions) {
        return String.format("DELETE FROM %s WHERE %s;", tableName, conditions);
    }

    static public String columnSum(String columnName, String tableName, String conditions) {
        StringBuilder strBuilder = new StringBuilder(String.format("SELECT SUM(%s) FROM %s", columnName, tableName));
        if(conditions == null || conditions.isEmpty()) {
            return strBuilder.append(";").toString();
        }
        return strBuilder.append(" WHERE ".concat(conditions)).append(";").toString();
    }

    static public String columnAvg(String columnName, String tableName) {
        return String.format("SELECT AVG(%s) FROM %s;", columnName, tableName);
    }

    static public String columnCount(String columnName, String tableName) {
        return String.format("SELECT COUNT(%s) FROM %s;", columnName, tableName);
    }

    static public String selectWithAND(String whatSelect, String tableName, String fcn, String fValue, String scn, String sValue) {
        return String.format("SELECT %s FROM %s WHERE %s = %s AND %s = %s;", whatSelect, tableName, fcn, fValue, scn, sValue);
    }

    static public String selectWithBetween(String whatSelect, String tableName, String columnName, String fValue, String sValue) {
        return String.format("SELECT %s FROM %s WHERE %s BETWEEN %s AND %s;", whatSelect, tableName, columnName, fValue, sValue);
    }

    static public String selectWithLike(String whatSelect, String tableName, String columnName, String pattern) {
        return String.format("SELECT %s FROM %s WHERE %s LIKE %s;", whatSelect, tableName, columnName, pattern);
    }

    static public String selectWithIn(String whatSelect, String tableName, String columnName, List<String> values) {
        StringBuilder builder = new StringBuilder(String.format("SELECT %s FROM %s WHERE %s IN (", whatSelect, tableName, columnName));
        Iterator<String> iterator = values.iterator();
        values.forEach(value -> {
            if(iterator.hasNext()) {
                builder.append(iterator.next());
                if(iterator.hasNext()) builder.append(", ");
            }
        });
        builder.append(");");
        return builder.toString();
    }

    static public String selectWithOrderBY(String whatSelect, String tableName, String columnNames, String order) {
        return (order == null && !order.isEmpty() && (order.equals("ASC") || order.equals("DESC"))) ? String.format("SELECT %s FROM %s ORDER BY %s %s;", whatSelect, tableName, columnNames, order) : String.format("SELECT %s FROM %s ORDER BY %s;", whatSelect, tableName, columnNames);
    }

    static public String selectWithHaving(String whatSelect, String tableName, String condition, String columnNames, String condition2, String orderColumns) {
        return String.format("SELECT %s FROM %s WHERE %s GROUP BY %s HAVING %s ORDER BY %s;", whatSelect, tableName, condition, columnNames, condition2, orderColumns);
    }

    static public String selectDistinct(String whatSelect, String tableName) {
        return String.format("SELECT %s FROM %s;", whatSelect, tableName);
    }

    static public String selectWithUnion(String whatSelect, String tableName, String whatSelect2, String tableName2) {
        return String.format("SELECT %s FROM %s UNION SELECT %s FROM %s;", whatSelect, tableName, whatSelect2, tableName2);
    }
}
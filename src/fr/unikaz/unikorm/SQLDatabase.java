package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.AutoIncrement;
import fr.unikaz.unikorm.annotations.PrimaryKey;
import fr.unikaz.unikorm.annotations.Unsigned;
import fr.unikaz.unikorm.filters.Filter;
import fr.unikaz.unikorm.filters.IFilter;
import fr.unikaz.unikorm.filters.Op;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SQLDatabase extends Database {

    private String databaseName;
    private Connection connection;

    public SQLDatabase(String databaseName, String login, String password) {
        this(databaseName, "127.0.0.1", 3306, login, password);
    }

    public SQLDatabase(String databaseName, String address, int port, String login, String password) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + databaseName +
                            "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
                    , login, password);
            this.databaseName = databaseName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <E> boolean createTable(Class<E> clazz) {
        System.out.println("SQLDatabase.createTable");
        StringBuilder request = new StringBuilder();
        List<String> primaryKeys = new ArrayList<>();
        request.append("create table if not exists ").append(getEntityName(clazz)).append("(");
        for (DataField dataField : getDataFields(clazz)) {
            String fieldName = dataField.getName();
            request.append(fieldName).append(" ");
            // handle basic types
            if (String.class.isAssignableFrom(dataField.field.getType()))
                request.append("text");
            else if (Integer.class.isAssignableFrom(dataField.field.getType()))
                request.append("int");
            else if (Date.class.isAssignableFrom(dataField.field.getType()))
                request.append("datetime");

            // handle annotations
            if (dataField.field.isAnnotationPresent(Unsigned.class))
                request.append(" unsigned");
            if (dataField.field.getDeclaringClass().equals(clazz)) {
                if (dataField.field.isAnnotationPresent(AutoIncrement.class))
                    request.append(" auto_increment");
                if (dataField.field.isAnnotationPresent(PrimaryKey.class))
                    primaryKeys.add(fieldName);
            }

            request.append(", ");
        }
        //append primary keys
        if (!primaryKeys.isEmpty())
            request.append("primary key (").append(String.join(",", primaryKeys)).append(')');
        request.append(')')
                .append("engine=INNODB;");

        try {
            System.out.println(request.toString());
            PreparedStatement preparedStatement = connection.prepareStatement(request.toString());
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <E> List<E> find(Class<E> clazz, IFilter filter) {
        System.out.println("SQLDatabase.find");
        try {
            String req = "Select * from " + getEntityName(clazz);
            if (filter != null) {
                req += " where " + filter.toString() + ";";
            }

            ResultSet resultSet = connection.prepareStatement(req).executeQuery();
            List<E> entities = new ArrayList<>();
            // read results
            while (resultSet.next()) {
                E entity = clazz.newInstance();
                for (DataField dataField : getDataFields(entity)) {
                    dataField.field.setAccessible(true);
                    String dbFieldName = dataField.field.getName();
                    if (!dataField.field.getDeclaringClass().equals(clazz)) {
                        // make a select to request the child element
                        List<?> children = find(dataField.field.getDeclaringClass(), new Filter(dataField.field.getDeclaringClass(), dbFieldName, Op.EQ, resultSet.getInt(dbFieldName)));
                        if (children.size() != 1)
                            System.out.println("Error ! " + children.size() + " child instead of 1 !");//todo make this better
                        else {
                            dataField.getReferTo().set(entity, children.get(0));
                        }
                    } else {
                        // set the casted value
                        dataField.field.set(entity, castValue(dataField.field.getType(), resultSet, dbFieldName));
                    }
                }
                entities.add(entity);
            }
            return entities;

        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <E> boolean insert(E entry) {
        System.out.println("SQLDatabase.insert");
        List<String> fieldList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        try {
            for (DataField dataField : getDataFields(entry, Options.IGNORE_AUTO_INCREMENT)) {
                dataField.field.setAccessible(true);
                fieldList.add(dataField.getName());
                valueList.add(formatValue(dataField.value));
            }
            String fields = String.join(",", fieldList);
            String values = String.join(",", valueList);
            String req = new StringBuilder()
                    .append("Insert into ")
                    .append(databaseName).append('.').append(getEntityName(entry.getClass()))
                    .append(" (")
                    .append(fields)
                    .append(") values (")
                    .append(values)
                    .append(");")
                    .toString();

            System.out.println(req.toString());
            return connection.prepareStatement(req).executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <E> boolean update(E entity) {
        StringBuilder req = new StringBuilder();
        req.append("update ").append(databaseName).append('.').append(getEntityName(entity.getClass()))
                .append(" set ");
        for (DataField dataField : getDataFields(entity, Options.IGNORE_PRIMARY_KEYS)) {
            req.append(dataField.getName()).append(" = ").append(formatValue(dataField.value));
        }
        req.append(" where ");
        for (DataField dataField : getDataFields(entity, Options.ONLY_PRIMARY_KEYS)) {
            req.append(dataField.getName()).append(" = ").append(formatValue(dataField.value));
        }
        req.append(';');
        try {
            int res = connection.prepareStatement(req.toString()).executeUpdate();
            return res > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private String formatValue(Object value) {
        if (value == null)
            return "null";
        else if (value instanceof String)
            return "'" + ((String) value).replace("\'", "\\'") + "'";
        else if (value instanceof Integer)
            return String.valueOf(value);
        else if (value instanceof Date)
            return "'" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(value) + "'";
        else {
            System.out.println("Unknow type " + value.getClass().getCanonicalName());
            return String.valueOf(value);
        }
    }

    private Object castValue(Class<?> type, ResultSet resultSet, String dbFieldName) {
        try {
            if (String.class.equals(type))
                return resultSet.getString(dbFieldName);
            if (Integer.class.equals(type))
                return resultSet.getInt(dbFieldName);
            if(Date.class.equals(type))
                return resultSet.getTimestamp(dbFieldName, Calendar.getInstance());
            System.out.println("Unknown type " + type.getCanonicalName() + " for " + dbFieldName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

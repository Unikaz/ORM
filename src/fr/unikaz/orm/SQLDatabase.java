package fr.unikaz.orm;

import fr.unikaz.orm.annotations.AutoIncrement;
import fr.unikaz.orm.annotations.PrimaryKey;
import fr.unikaz.orm.annotations.Unsigned;
import fr.unikaz.orm.filters.Filter;
import fr.unikaz.orm.filters.IFilter;
import fr.unikaz.orm.filters.Op;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        for (DataField field : getFields(clazz)) {
            String fieldName = field.getName();
            request.append(fieldName).append(" ");
            // handle basic types
            if (String.class.isAssignableFrom(field.field.getType()))
                request.append("text");
            else if (Integer.class.isAssignableFrom(field.field.getType()))
                request.append("int");
            else if (Date.class.isAssignableFrom(field.field.getType()))
                request.append("datetime");

            // handle annotations
            if (field.field.isAnnotationPresent(Unsigned.class))
                request.append(" unsigned");
            if (field.field.getDeclaringClass().equals(clazz)) {
                if (field.field.isAnnotationPresent(AutoIncrement.class))
                    request.append(" auto_increment");
                if (field.field.isAnnotationPresent(PrimaryKey.class))
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
                for (DataField field : getFields(entity)) {
                    field.field.setAccessible(true);
                    String dbFieldName = field.field.getName();
                    if(!field.field.getDeclaringClass().equals(clazz)){
                        // make a select to request the child element
                        List<?> children = find(field.field.getDeclaringClass(), new Filter(field.field.getDeclaringClass(), dbFieldName, Op.EQ, resultSet.getInt(dbFieldName)));
                        if(children.size() != 1)
                            System.out.println("Error ! " + children.size() + " child instead of 1 !");//todo make this better
                        else{
                            field.getReferTo().set(entity, children.get(0));
                        }
                    }else {
                        if (String.class.equals(field.field.getType())) {
                            field.field.set(entity, resultSet.getString(dbFieldName));
                        } else if (Integer.class.equals(field.field.getType())) {
                            field.field.set(entity, resultSet.getInt(dbFieldName));
                        } else {
                            System.out.println("Unknown type " + field.field.getType().getCanonicalName());
                        }
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
            for (DataField field : getFields(entry, Options.IGNORE_AUTO_INCREMENT)) {
                field.field.setAccessible(true);
                fieldList.add(field.getName());
                valueList.add(formatValue(field.value));
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
        for (DataField fieldsAndValue : getFields(entity, Options.IGNORE_PRIMARY_KEYS)) {
            req.append(fieldsAndValue.getName()).append(" = ").append(formatValue(fieldsAndValue.value));
        }
        req.append(" where ");
        for (DataField fieldsAndValue : getFields(entity, Options.ONLY_PRIMARY_KEYS)) {
            req.append(fieldsAndValue.getName()).append(" = ").append(formatValue(fieldsAndValue.value));
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

}

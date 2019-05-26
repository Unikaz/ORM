package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.filters.Filter;
import fr.unikaz.unikorm.filters.IFilter;
import fr.unikaz.unikorm.filters.MultiFilter;
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
            if (String.class.isAssignableFrom(dataField.getType()))
                request.append("text");
            else if (Integer.class.isAssignableFrom(dataField.getType()))
                request.append("int");
            else if (Date.class.isAssignableFrom(dataField.getType()))
                request.append("datetime");


            // handle annotations
            if (dataField.localField.isAnnotationPresent(Field.class)) {
                Field fieldOptions = dataField.localField.getAnnotation(Field.class);
                if (fieldOptions.unsigned())
                    request.append(" unsigned");
                if (dataField.localField.getDeclaringClass().equals(clazz)) {
                    if (fieldOptions.autoIncrement())
                        request.append(" auto_increment");
                    if (fieldOptions.primaryKey())
                        primaryKeys.add(fieldName);
                }
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

            // filters
            if (filter != null) {
                req += " where " + filter.toString() + ";";
            }
            System.out.println("req = " + req);
            ResultSet resultSet = connection.prepareStatement(req).executeQuery();
            List<E> entities = new ArrayList<>();
            // read results
            while (resultSet.next()) { // for each row
                E entity = clazz.newInstance();
                for (DataField dataField : getDataFields(entity)) { // for each localField in the row
                    dataField.localField.setAccessible(true);
                    String dbFieldName = dataField.getName();
                    if (dataField.relativeEntity != null){
                        // make a select to request the child elements
                        List<?> children = find((Class<?>) dataField.relativeEntity.entity(),
                                new Filter(dataField.relativeEntity.targetField(), Op.EQ, resultSet.getInt(dbFieldName)));//dataField.relativeEntity.localField())));
                        if (children.size() != 1)
                            dataField.localField.set(entity, children);
                        else {
                            dataField.localField.set(entity, children.get(0));
                        }
                    } else {
                        // set the casted value
                        dataField.localField.set(entity, castValue(dataField.localField.getType(), resultSet, dbFieldName));
                    }
                }
                entities.add(entity);
            }
            return entities;

        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.out.println("You have to define an empty constructor for entity " + clazz.getCanonicalName());
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
            for (DataField dataField : getDataFields(entry, Option.IGNORE_AUTO_INCREMENT)) {
                dataField.localField.setAccessible(true);
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
        for (DataField dataField : getDataFields(entity, Option.IGNORE_PRIMARY_KEYS)) {
            req.append(dataField.getName()).append(" = ").append(formatValue(dataField.value));
        }
        req.append(" where ");
        for (DataField dataField : getDataFields(entity, Option.ONLY_PRIMARY_KEYS)) {
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

    @Override
    public void fetch(Object object) {
        // create filters from object
        MultiFilter filters = new MultiFilter(MultiFilter.FilterType.AND);
        for (DataField field : getDataFields(object)) {
            if(field.value != null){
                filters.add(new Filter(getFieldName(field.localField), Op.EQ, field.value));
            }
        }
        // call find using filters
        Object object2 = find(object.getClass(), filters).get(0);
        // map the result on the given object
        for (java.lang.reflect.Field field : object.getClass().getDeclaredFields()) {
            try {
                field.set(object, field.get(object2));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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
            if (Integer.class.equals(type) || type.equals(Integer.TYPE))
                return resultSet.getInt(dbFieldName);
            if (Date.class.equals(type))
                return resultSet.getTimestamp(dbFieldName, Calendar.getInstance());
            System.out.println("Unknown type " + type.getCanonicalName() + " for " + dbFieldName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

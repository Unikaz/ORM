package fr.unikaz.orm;

import fr.unikaz.orm.annotations.*;
import fr.unikaz.orm.filters.IFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Database {

    public abstract <E> boolean createTable(Class<E> clazz);

    public <E> List<E> find(Class<E> clazz) {
        return find(clazz, null);
    }

    public abstract <E> List<E> find(Class<E> clazz, IFilter filter);

    public abstract <E> boolean insert(E entry);

    public abstract <E> boolean update(E entity);


    //=====================================
    // Process Annotations
    //=====================================

    public static String getFieldName(Field field) {
        if (field.isAnnotationPresent(FieldName.class)) {
            return field.getAnnotation(FieldName.class).name();
        }
        return field.getName();
    }

    public static <E> String getEntityName(Class<E> clazz) {
        if (clazz.isAnnotationPresent(Entity.class))
            return clazz.getAnnotation(Entity.class).name();
        return clazz.getName();
    }


    public static <E> List<DataField> getDataFields(Class<E> current, Options... opts){
        return getDataFields(current, null, opts);
    }
    public static <E> List<DataField> getDataFields(Object entity, Options... opts){
        return getDataFields(entity.getClass(), entity, opts);
    }
    public static <E> List<DataField> getDataFields(Class<E> current, Object entity, Options... opts){
        // Handle some options
        boolean ignoreAutoIncrements = false;
        boolean ignorePrimaryKeys = false;
        boolean onlyPrimaryKeys = false;
//        boolean prefix = false;
        if (opts != null)
            for (Options opt : opts) {
                if (opt == Options.IGNORE_AUTO_INCREMENT)
                    ignoreAutoIncrements = true;
                if (opt == Options.IGNORE_PRIMARY_KEYS)
                    ignorePrimaryKeys = true;
                if (opt == Options.ONLY_PRIMARY_KEYS)
                    onlyPrimaryKeys = true;
//                if (opt == Options.PREFIXED)
//                    prefix = true;
            }
        // process fields
        List<DataField> dataFields = new ArrayList<>();
        for (Field field : current.getDeclaredFields()) {
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            if (ignoreAutoIncrements && field.isAnnotationPresent(AutoIncrement.class)) continue;
            if (ignorePrimaryKeys && field.isAnnotationPresent(PrimaryKey.class)) continue;
            if (onlyPrimaryKeys && !field.isAnnotationPresent(PrimaryKey.class)) continue;
            try {
                field.setAccessible(true);
                Object value = null;
                if(entity != null)
                    value = field.get(entity);
                if(field.isAnnotationPresent(RelativeEntity.class)){
                    List<DataField> childDataFields = Database.getDataFields(field.getType(), value, Options.ONLY_PRIMARY_KEYS);
                    if(childDataFields.size() == 1 && field.isAnnotationPresent(FieldName.class)){
                        // if the child has only one PK, use the parent custom name for this field
                        DataField childDataField = childDataFields.get(0);
                        childDataField.setSpecificName(field.getAnnotation(FieldName.class).name());
                        childDataField.setReferTo(field); // add a reference to the current field, for filling the right field during select operation
                        dataFields.add(childDataField);
                    }else{
                        // use generated names
                        for (DataField childDataField : childDataFields) {
                            childDataField.setSpecificName(Database.getEntityName(current) + "_" + Database.getFieldName(childDataField.field));
                            dataFields.add(childDataField);
                        }
                    }
                }else{
                    DataField dataField = new DataField(field);
                    dataField.value = value;
                    dataFields.add(dataField);
                }
            }catch (Exception e){}
        }
        return dataFields;
    }
}

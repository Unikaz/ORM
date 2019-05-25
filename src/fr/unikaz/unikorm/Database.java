package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.annotations.IgnoreField;
import fr.unikaz.unikorm.annotations.RelativeEntity;
import fr.unikaz.unikorm.filters.IFilter;

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

    public static String getFieldName(java.lang.reflect.Field field) {
        Field opts = field.getAnnotation(Field.class);
        if (opts != null && !opts.name().equals("")) {
            return opts.name();
        }
        return field.getName();
    }

    public static <E> String getEntityName(Class<E> clazz) {
        if (clazz.isAnnotationPresent(Entity.class))
            return clazz.getAnnotation(Entity.class).name();
        return clazz.getName();
    }


    public static <E> List<DataField> getDataFields(Class<E> current, Option... opts) {
        return getDataFields(current, null, opts);
    }

    public static <E> List<DataField> getDataFields(Object entity, Option... opts) {
        return getDataFields(entity.getClass(), entity, opts);
    }

    public static <E> List<DataField> getDataFields(Class<E> current, Object entity, Option... addOptions) {
        // Handle some options
        Options options = new Options(addOptions);
        boolean ignoreAutoIncrements = options.has(Option.IGNORE_AUTO_INCREMENT);
        boolean ignorePrimaryKeys = options.has(Option.IGNORE_PRIMARY_KEYS);
        boolean onlyPrimaryKeys = options.has(Option.ONLY_PRIMARY_KEYS);


        // process fields
        List<DataField> dataFields = new ArrayList<>();
        for (java.lang.reflect.Field field : current.getDeclaredFields()) {
            Field fieldOptions = field.getAnnotation(Field.class);
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            if (ignoreAutoIncrements && fieldOptions != null && fieldOptions.autoIncrement()) continue;
            if (ignorePrimaryKeys && fieldOptions != null && fieldOptions.primaryKey()) continue;
            if (onlyPrimaryKeys && (fieldOptions == null || !fieldOptions.primaryKey())) continue;
            try {
                field.setAccessible(true);
                Object value = null;
                if (entity != null)
                    value = field.get(entity);
                RelativeEntity relativeEntity = field.getAnnotation(RelativeEntity.class);
                if (relativeEntity != null) {
                    //get localField info from target
                    for (java.lang.reflect.Field declaredField : relativeEntity.entity().getDeclaredFields()) {
                        Field childFieldOptions = declaredField.getAnnotation(Field.class);
                        if ((childFieldOptions != null && childFieldOptions.name().equals(relativeEntity.fieldName()))
                                || declaredField.getName().equals(relativeEntity.fieldName())) {
                            DataField dataField = new DataField(field);
                            dataField.setDistantField(declaredField);
                            dataField.type = declaredField.getType();
                            if(value != null)
                                dataField.value = declaredField.get(value);
                            if (fieldOptions != null && !fieldOptions.name().equals(""))
                                dataField.setSpecificName(fieldOptions.name());
                            else
                                dataField.setSpecificName(field.getName());
                            dataFields.add(dataField);
                            break;
                        }
                    }
                    throw new RuntimeException("Cannot find localField " + relativeEntity.fieldName() + " in " + relativeEntity.entity());
                } else {
                    DataField dataField = new DataField(field);
                    dataField.value = value;
                    dataFields.add(dataField);
                }
            } catch (Exception e) {
            }
        }
        return dataFields;
    }
}

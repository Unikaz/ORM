package fr.unikaz.unikorm.api;

import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.annotations.IgnoreField;
import fr.unikaz.unikorm.annotations.RelativeEntity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class Database {

    //todo create javadoc
    public abstract <E> boolean createTable(Class<E> clazz);

    public <E> List<E> find(Class<E> clazz) throws Exception {
        return find(clazz, null);
    }

    public abstract <E> List<E> find(Class<E> clazz, IFilter filter) throws Exception;

    public abstract <E> boolean insert(E entry) throws Exception;

    public abstract <E> boolean update(E entity) throws Exception;

    public abstract void fetch(Object object) throws Exception;

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
            if (field.isAnnotationPresent(IgnoreField.class) || Modifier.isTransient(field.getModifiers())) continue;
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
                    boolean found = false;
                    for (java.lang.reflect.Field declaredField : relativeEntity.entity().getDeclaredFields()) {
                        Field childFieldOptions = declaredField.getAnnotation(Field.class);
                        if ((childFieldOptions != null && childFieldOptions.name().equals(relativeEntity.targetField()))
                                || declaredField.getName().equals(relativeEntity.targetField())) {
                            DataField dataField = new DataField(field);
                            dataField.setDistantField(declaredField);
                            dataField.type = declaredField.getType();
                            if (value != null)
                                dataField.value = declaredField.get(value);
                            if (fieldOptions != null && !fieldOptions.name().equals(""))
                                dataField.setSpecificName(fieldOptions.name());
                            dataField.relativeEntity = relativeEntity;
                            dataFields.add(dataField);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        throw new RuntimeException("Cannot find localField " + relativeEntity.targetField() + " in " + relativeEntity.entity());
                } else {
                    DataField dataField = new DataField(field);
                    dataField.value = value;
                    dataFields.add(dataField);
                }
            } catch (IllegalAccessException ignored) {
                // no exception raised, as we use the setAccessible(true)
            }
        }
        return dataFields;
    }
}

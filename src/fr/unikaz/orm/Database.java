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
    public static <E> List<Field> getFields(Class<E> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            fields.add(field);
        }
        return fields;
    }

    public static <E> List<Pair<Field, Object>> getFieldsAndValues(E entity, Options... opts) {
        // Handle some options
        boolean ignoreAutoIncrements = false;
        boolean ignorePrimaryKeys = false;
        boolean onlyPrimaryKeys = false;
        if (opts != null)
            for (Options opt : opts) {
                if (opt == Options.IGNORE_AUTO_INCREMENT)
                    ignoreAutoIncrements = true;
                if (opt == Options.IGNORE_PRIMARY_KEYS)
                    ignorePrimaryKeys = true;
                if (opt == Options.ONLY_PRIMARY_KEYS)
                    onlyPrimaryKeys = true;
            }
        // process fields
        List<Pair<Field, Object>> fields = new ArrayList<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            if (ignoreAutoIncrements && field.isAnnotationPresent(AutoIncrement.class)) continue;
            if (ignorePrimaryKeys && field.isAnnotationPresent(PrimaryKey.class)) continue;
            if (onlyPrimaryKeys && !field.isAnnotationPresent(PrimaryKey.class)) continue;
            try {
                field.setAccessible(true);
                fields.add(new Pair<>(field, field.get(entity)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return fields;
    }

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
}

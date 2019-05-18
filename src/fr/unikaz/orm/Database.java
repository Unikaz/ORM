package fr.unikaz.orm;

import fr.unikaz.orm.annotations.AutoIncrement;
import fr.unikaz.orm.annotations.FieldName;
import fr.unikaz.orm.annotations.IgnoreField;
import fr.unikaz.orm.annotations.Entity;
import fr.unikaz.orm.filters.IFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Database {

    public abstract <E extends fr.unikaz.orm.Entity> boolean createTable(Class<E> clazz);

    public <E extends fr.unikaz.orm.Entity> List<E> find(Class<E> clazz) {
        return find(clazz, null);
    }

    public abstract <E extends fr.unikaz.orm.Entity> List<E> find(Class<E> clazz, IFilter filter);

    public abstract <E extends fr.unikaz.orm.Entity> boolean insert(E entry);


    //=====================================
    // Process Annotations
    //=====================================
    public static <E extends fr.unikaz.orm.Entity> List<Field> getFields(Class<E> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            fields.add(field);
        }
        return fields;
    }

    public static <E extends fr.unikaz.orm.Entity> List<Pair<Field, Object>> getFieldsAndValues(E entity, Options... opts) {
        // Handle some options
        boolean ignoreAutoIncrements = false;
        if (opts != null)
            for (Options opt : opts) {
                if (opt == Options.IGNORE_AUTO_INCREMENT)
                    ignoreAutoIncrements = true;
            }
        // process fields
        List<Pair<Field, Object>> fields = new ArrayList<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(IgnoreField.class)) continue;
            if (ignoreAutoIncrements && field.isAnnotationPresent(AutoIncrement.class)) continue;
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

    public static <E extends fr.unikaz.orm.Entity> String getEntityName(Class<E> clazz) {
        if (clazz.isAnnotationPresent(Entity.class))
            return clazz.getAnnotation(Entity.class).name();
        return clazz.getName();
    }
}

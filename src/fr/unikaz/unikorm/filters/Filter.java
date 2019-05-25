package fr.unikaz.unikorm.filters;

import java.lang.reflect.Field;

public class Filter implements IFilter {
    public Field field;
    public String fieldName;
    public Op op;
    public Object o;


    public Filter(Class clazz, String fieldName, Op op, Object o) {
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // if we cannot find the localField directly, try using annotations
            for (Field declaredField : clazz.getDeclaredFields()) {
                fr.unikaz.unikorm.annotations.Field opts = declaredField.getAnnotation(fr.unikaz.unikorm.annotations.Field.class);
                if (opts != null && opts.name().equals(fieldName)) {
                    field = declaredField;
                    break;
                }
            }
            if (field == null)
                e.printStackTrace();
        }
        this.op = op;
        this.o = o;
    }

    public Filter(Field field, Op op, Object o) {
        this.field = field;
        this.field.setAccessible(true);
        this.op = op;
        this.o = o;
    }

    public Filter(String fieldName, Op op, Object o) {
        this.fieldName = fieldName;
        this.op = op;
        this.o = o;
    }

    @Override
    public String toString() {
        String value = o.toString();
        if (o instanceof String)
            value = "'" + o + "'";
        if (field != null)
            return field.getName() + " " + op.get() + " " + value;
        else
            return fieldName + " " + op.get() + " " + value;
    }
}

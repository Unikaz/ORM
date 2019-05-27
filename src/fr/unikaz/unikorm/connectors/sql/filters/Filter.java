package fr.unikaz.unikorm.connectors.sql.filters;

import fr.unikaz.unikorm.api.IFilter;
import fr.unikaz.unikorm.connectors.sql.MySQLDatabase;

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
        try {
            if (op == Op.EQ) {
                String value = MySQLDatabase.formatValue(o);
                return getFieldName() + " " + op.get() + " " + value;
            } else if (op == Op.IN) {
                StringBuilder res = new StringBuilder();
                res.append(getFieldName()).append(" ").append(op.get()).append("(");
                if (o instanceof Iterable) {
                    for (Object v : ((Iterable<?>) o)) {
                        res.append(MySQLDatabase.formatValue(v)).append(",");
                    }
                } else if (o instanceof Object[]) {
                    for (Object item : ((Object[]) o)) {
                        res.append(MySQLDatabase.formatValue(item)).append(",");
                    }
                } else {
                    res.append(MySQLDatabase.formatValue(o)).append(' ');
                }
                return res.toString().substring(0, res.length() - 1) + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFieldName() + " " + op.get() + " " + o.toString();
    }

    public String getFieldName() {
        if (field != null)
            return field.getName();
        else
            return fieldName;
    }
}

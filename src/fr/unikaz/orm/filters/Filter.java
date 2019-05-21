package fr.unikaz.orm.filters;

import fr.unikaz.orm.Database;

import java.lang.reflect.Field;

public class Filter implements IFilter {
    public Field field;
    public Op op;
    public Object o;


    public Filter(Class clazz, String fieldName, Op op, Object o){
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
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

    @Override
    public String toString() {
        String value = o.toString();
        if(o instanceof String)
            value = "'" + o + "'";
        return field.getName() + " " + op.get() + " " + value;
    }
}

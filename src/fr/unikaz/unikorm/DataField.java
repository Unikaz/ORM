package fr.unikaz.unikorm;

import java.lang.reflect.Field;

public class DataField {
    public Field field;
    public Object value;
    private String specificName;
    private Field referTo;

    public DataField(Field field) {
        this.field = field;
    }

    public DataField(Field field, Object value) {
        this.field = field;
        this.value = value;
    }

    public String getName() {
        if (specificName != null)
            return specificName;
        return Database.getFieldName(field);
    }

    public void setSpecificName(String specificName) {
        this.specificName = specificName;
    }

    public Field getReferTo() {
        return referTo;
    }

    public void setReferTo(Field referTo) {
        this.referTo = referTo;
    }

    @Override
    public String toString() {
        return "DataField{" +
                "field=" + field +
                ", value=" + value +
                ", specificName='" + specificName + '\'' +
                '}';
    }
}

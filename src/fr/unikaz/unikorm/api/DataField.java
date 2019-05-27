package fr.unikaz.unikorm.api;

import fr.unikaz.unikorm.annotations.RelativeEntity;

import java.lang.reflect.Field;

public class DataField {
    public Field localField;
    public Field distantField;
    public Class type;

    public Object value;
    public String specificName;

    public RelativeEntity relativeEntity;

    public DataField(Field localField) {
        this.localField = localField;
    }

    public DataField(Field localField, Object value) {
        this.localField = localField;
        this.value = value;
    }

    public String getName() {
        if (specificName != null)
            return specificName;
        if(relativeEntity != null)
            return relativeEntity.localField();
        return Database.getFieldName(localField);
    }

    public String getTargetFieldName(){
        return Database.getFieldName(distantField);
    }

    public void setSpecificName(String specificName) {
        this.specificName = specificName;
    }

    public Field getDistantField() {
        return distantField;
    }

    public void setDistantField(Field distantField) {
        this.distantField = distantField;
    }

    @Override
    public String toString() {
        return "DataField{" +
                "localField=" + localField +
                ", value=" + value +
                ", specificName='" + specificName + '\'' +
                '}';
    }

    public Class<?> getType() {
        if(type == null)
            return localField.getType();
        return type;
    }
}

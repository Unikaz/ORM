package fr.unikaz.orm;


import fr.unikaz.orm.annotations.*;

@fr.unikaz.orm.annotations.Entity(name = "test_entity")
public class EntityTest extends Entity {
    @AutoIncrement
    @PrimaryKey
    @Unsigned
    private Integer id;
    @FieldName(name = "my_value")
    public String somevalue;
    @IgnoreField
    private String ignoredField;

    public EntityTest() {
    }

    public EntityTest(String somevalue, String ignoredField) {
        this.somevalue = somevalue;
        this.ignoredField = ignoredField;
    }
}

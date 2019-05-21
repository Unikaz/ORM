package fr.unikaz.unikorm;


import fr.unikaz.unikorm.annotations.*;

@Entity(name = "user_test")
public class User {
    @AutoIncrement
    @PrimaryKey
    @Unsigned
    public Integer id;
    @FieldName(name = "name")
    public String name;
    @IgnoreField
    private String ignoredField;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ignoredField='" + ignoredField + '\'' +
                '}';
    }
}

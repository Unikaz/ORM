package fr.unikaz.unikorm;


import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.annotations.IgnoreField;

@Entity(name = "user_test")
public class User {
    @Field(autoIncrement = true, primaryKey = true, unsigned = true)
    public Integer id;
    @Field(name = "name")
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

package fr.unikaz.unikorm;


import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;

@Entity(name = "message_test")
public class MessageModel {
    private Integer id;
    @Field(name ="user_id")
    public int userId;

    private String message;

    public MessageModel(){}

    @Override
    public String toString() {
        return "MessageModel{" +
                "id=" + id +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                '}';
    }


}

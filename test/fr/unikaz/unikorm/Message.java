package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.annotations.RelativeEntity;

import java.util.Date;

@Entity(name = "message_test")
public class Message {
    @Field(autoIncrement = true, primaryKey = true, unsigned = true)
    private Integer id;
    @Field(name = "user_id")
    @RelativeEntity(entity = User.class, localField = "user_id", targetField = "id")
    private User user;

    private String message;
    private Date date;

    public Message() {
    }

    public Message(User user, String message, Date date) {
        this.user = user;
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "\nid=" + id +
                ",\n user=" + user +
                ",\n message='" + message + '\'' +
                ",\n date=" + date +
                '}';
    }
}

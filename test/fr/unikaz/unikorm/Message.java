package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.*;

import java.util.Date;

@Entity(name = "message_test")
public class Message {
    @AutoIncrement
    @PrimaryKey
    @Unsigned
    private Integer id;
    @FieldName(name = "user_id")
    @RelativeEntity(target = User.class)
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

package fr.unikaz.unikorm;

import fr.unikaz.unikorm.annotations.Entity;
import fr.unikaz.unikorm.annotations.Field;
import fr.unikaz.unikorm.annotations.RelativeEntity;

import java.util.List;
import java.util.stream.Collectors;

@Entity(name = "user_test")
public class MessageListModel {
    @Field(name = "id")
    int userId;
    String name;

    @RelativeEntity(entity = MessageModel.class, localField = "id", targetField = "user_id")
    List<MessageModel> messages;

    @Override
    public String toString() {
        String messagesString = messages.stream().map(m -> m.toString() + '\n').collect(Collectors.joining());
        return "MessageListModel{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", messages=\n" +
                messagesString +
                '}';
    }
}

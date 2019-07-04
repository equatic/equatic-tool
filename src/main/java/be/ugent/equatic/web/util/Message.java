package be.ugent.equatic.web.util;

/**
 * Describes a message for user.
 * Used in templates.
 */
public class Message {

    private String text;
    private MessageType type;

    public Message(String text, MessageType type) {
        this.text = text;
        this.type = type;
    }

    public static Message success(String text) {
        return new Message(text, MessageType.success);
    }

    public static Message warning(String text) {
        return new Message(text, MessageType.warning);
    }

    public static Message danger(String text) {
        return new Message(text, MessageType.danger);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (!text.equals(message.text)) return false;
        return type == message.type;

    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}

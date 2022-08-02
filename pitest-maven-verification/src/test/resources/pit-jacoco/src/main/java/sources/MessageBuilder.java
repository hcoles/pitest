package sources;

public class MessageBuilder {

    public String getMessage(String name) {

        StringBuilder result = new StringBuilder();

        if (name == null || name.trim().length() == 0) {
            result.append("Stuff!");
        } else {
            result.append("Hello " + name);
        }
        return result.toString();
    }

}

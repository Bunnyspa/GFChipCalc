package main.json;

/**
 *
 * @author Bunnyspa
 */
public class BooleanJson implements Json {

    private final boolean data;

    public BooleanJson(String data) {
        this.data = Boolean.valueOf(data.trim());
    }

    public boolean getBoolean() {
        return data;
    }

    @Override
    public int getType() {
        return Json.BOOLEAN;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}

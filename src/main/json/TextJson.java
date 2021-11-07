package main.json;

public class TextJson implements Json {

    private final String data;

    public TextJson(String data) {
        this.data = data.trim().replaceAll("^\"|\"$", "");
    }

    public String getText() {
        return data;
    }

    @Override
    public int getType() {
        return Json.TEXT;
    }

    @Override
    public String toString() {
        return "\"" + data + "\"";
    }
}

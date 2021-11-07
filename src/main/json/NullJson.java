package main.json;

public class NullJson implements Json {

    @Override
    public int getType() {
        return Json.NULL;
    }

    @Override
    public String toString() {
        return "null";
    }
}

package main.json;

public class NumberJson implements Json {

    private final String data;

    public NumberJson(String data) {
        this.data = data.trim();
    }

    public int getInteger() {
        return Integer.valueOf(data);
    }

    public double getDouble() {
        return Double.valueOf(data);
    }

    @Override
    public int getType() {
        return Json.NUMBER;
    }

    @Override
    public String toString() {
        return data;
    }
}

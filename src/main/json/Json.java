package main.json;

import java.util.List;

/**
 *
 * @author Bunnyspa
 */
public interface Json {

    public static final int NULL = 0;
    public static final int BOOLEAN = 1;
    public static final int NUMBER = 2;
    public static final int TEXT = 3;
    public static final int ARRAY = 4;
    public static final int OBJECT = 5;

    public int getType();

    public static Json parse(String data) {
        String dataTrim = data.trim();
        switch (dataTrim.charAt(0)) {
            // Object
            case '{':
                return new ObjectJson(data);
            // Array
            case '[':
                return new ArrayJson(data);
            // Text
            case '"':
                return new TextJson(data);
            // Boolean
            case 't':
            case 'f':
                return new BooleanJson(data);
            // Null
            case 'n':
                return new NullJson();
            // Number
            default:
                return new NumberJson(data);
        }
    }

    static int getEndIndex(String data, int i) {
        while (i < data.length()) {
            char next = data.charAt(i);
            if (!Character.isWhitespace(next)) {
                switch (next) {
                    // Object
                    case '{':
                        return getBracketEndIndex(data, i, OBJECT);
                    // Array
                    case '[':
                        return getBracketEndIndex(data, i, ARRAY);
                    // Text
                    case '"':
                        i++;
                        while (i < data.length()) {
                            if (data.charAt(i) == '"' && (i == 0 || data.charAt(i - 1) != '\\')) {
                                return i;
                            }
                            i++;
                        }
                        return -1;
                    // Boolean - True
                    case 't':
                        if (data.startsWith("true", i)) {
                            return i + 4;
                        }
                        return -1;
                    // Boolean - False
                    case 'f':
                        if (data.startsWith("false", i)) {
                            return i + 5;
                        }
                        return -1;
                    // Null
                    case 'n':
                        if (data.startsWith("null", i)) {
                            return i + 4;
                        }
                        return -1;
                    // Number
                    default:
                        i++;
                        while (i < data.length()) {
                            char c = data.charAt(i);
                            if (c != '.' && (c < '0' || '9' < c)) {
                                return i - 1;
                            }
                            i++;
                        }
                        return i - 1;
                }
            }
            i++;
        }
        return -1;
    }

    static int getBracketEndIndex(String data, int i, int type) {
        try {
            int bracketLevel = 0;
            boolean quoting = false;

            char openBracket = type == OBJECT ? '{' : '[';
            char closeBracket = type == OBJECT ? '}' : ']';

            do {
                char c = data.charAt(i);
                if (c == '"' && (i == 0 || data.charAt(i - 1) != '\\')) {
                    quoting = !quoting;
                }
                if (!quoting && c == openBracket) {
                    bracketLevel++;
                }
                if (!quoting && c == closeBracket) {
                    bracketLevel--;
                }
                i++;
            } while (bracketLevel > 0 && i < data.length());
            return i - 1;
        } catch (Exception ex) {
            return -1;
        }
    }

    static boolean getBoolean(Json data) throws ClassCastException {
        if (data.getType() != BOOLEAN) {
            throw new ClassCastException(getClassCastExceptionMessage(data, BooleanJson.class.getName()));
        }
        return ((BooleanJson) data).getBoolean();
    }

    static int getInteger(Json data) throws ClassCastException {
        if (data.getType() != NUMBER) {
            throw new ClassCastException(getClassCastExceptionMessage(data, NumberJson.class.getName()));
        }
        return ((NumberJson) data).getInteger();
    }

    static double getDouble(Json data) throws ClassCastException {
        if (data.getType() != NUMBER) {
            throw new ClassCastException(getClassCastExceptionMessage(data, NumberJson.class.getName()));
        }
        return ((NumberJson) data).getDouble();
    }

    static String getText(Json data) throws ClassCastException {
        if (data.getType() != TEXT) {
            throw new ClassCastException(getClassCastExceptionMessage(data, TextJson.class.getName()));
        }
        return ((TextJson) data).getText();
    }

    static List<Json> getList(Json data) throws ClassCastException {
        if (data.getType() != ARRAY) {
            throw new ClassCastException(getClassCastExceptionMessage(data, ArrayJson.class.getName()));
        }
        return ((ArrayJson) data).getList();
    }

    static ObjectJson getObjectJson(Json data) throws ClassCastException {
        if (data.getType() != OBJECT) {
            throw new ClassCastException(getClassCastExceptionMessage(data, ObjectJson.class.getName()));
        }
        return (ObjectJson) data;
    }

    static List<String> getObjectKeys(Json data) throws ClassCastException {
        if (data.getType() != OBJECT) {
            throw new ClassCastException(getClassCastExceptionMessage(data, ObjectJson.class.getName()));
        }
        return ((ObjectJson) data).getKeys();
    }

    static Json getObjectValue(Json data, String key) throws ClassCastException {
        if (data.getType() != OBJECT) {
            throw new ClassCastException(getClassCastExceptionMessage(data, ObjectJson.class.getName()));
        }
        return ((ObjectJson) data).getValue(key);
    }

    static String getClassCastExceptionMessage(Json data, String cast) {
        StringBuilder sb = new StringBuilder();
        sb.append(Json.class.getName()).append(" cannot be cast to ").append(cast).append("- It should be cast to ");
        switch (data.getType()) {
            case NULL:
                sb.append(NullJson.class.getName());
                break;
            case BOOLEAN:
                sb.append(BooleanJson.class.getName());
                break;
            case NUMBER:
                sb.append(NumberJson.class.getName());
                break;
            case TEXT:
                sb.append(TextJson.class.getName());
                break;
            case ARRAY:
                sb.append(ArrayJson.class.getName());
                break;
            case OBJECT:
                sb.append(ObjectJson.class.getName());
                break;
            default:
                throw new AssertionError();
        }
        return sb.toString();
    }
}

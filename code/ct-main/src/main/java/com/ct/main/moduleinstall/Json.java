package com.ct.main.moduleinstall;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {
    private final Reader reader;
    private int pending = -2;

    private Json(Reader reader) {
        this.reader = reader;
    }

    static Object parse(Reader reader) throws IOException {
        Json json = new Json(reader);
        json.skipWhitespace();
        Object value = json.readValue();
        json.skipWhitespace();
        if (json.peek() != -1) {
            throw new IOException("Unexpected trailing content in JSON.");
        }
        return value;
    }

    private Object readValue() throws IOException {
        int character = peek();
        switch (character) {
            case '{':
                return readObject();
            case '[':
                return readArray();
            case '"':
                return readString();
            case 't':
                expect("true");
                return Boolean.TRUE;
            case 'f':
                expect("false");
                return Boolean.FALSE;
            case 'n':
                expect("null");
                return null;
            case -1:
                throw new IOException("Unexpected end of JSON.");
            default:
                return readNumber();
        }
    }

    private Map<String, Object> readObject() throws IOException {
        Map<String, Object> object = new LinkedHashMap<>();
        read();
        skipWhitespace();
        if (peek() == '}') {
            read();
            return object;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            if (read() != ':') {
                throw new IOException("Expected ':' in JSON object.");
            }
            skipWhitespace();
            object.put(key, readValue());
            skipWhitespace();
            int character = read();
            if (character == '}') {
                return object;
            }
            if (character != ',') {
                throw new IOException("Expected ',' or '}' in JSON object.");
            }
        }
    }

    private List<Object> readArray() throws IOException {
        List<Object> array = new ArrayList<>();
        read();
        skipWhitespace();
        if (peek() == ']') {
            read();
            return array;
        }
        while (true) {
            skipWhitespace();
            array.add(readValue());
            skipWhitespace();
            int character = read();
            if (character == ']') {
                return array;
            }
            if (character != ',') {
                throw new IOException("Expected ',' or ']' in JSON array.");
            }
        }
    }

    private String readString() throws IOException {
        if (read() != '"') {
            throw new IOException("Expected a JSON string.");
        }
        StringBuilder text = new StringBuilder();
        while (true) {
            int character = read();
            if (character == -1) {
                throw new IOException("Unterminated JSON string.");
            }
            if (character == '"') {
                return text.toString();
            }
            if (character != '\\') {
                text.append((char) character);
                continue;
            }
            int escape = read();
            switch (escape) {
                case '"', '\\', '/' -> text.append((char) escape);
                case 'b' -> text.append('\b');
                case 'f' -> text.append('\f');
                case 'n' -> text.append('\n');
                case 'r' -> text.append('\r');
                case 't' -> text.append('\t');
                case 'u' -> {
                    StringBuilder hex = new StringBuilder();
                    for (int index = 0; index < 4; index++) {
                        hex.append((char) read());
                    }
                    text.append((char) Integer.parseInt(hex.toString(), 16));
                }
                default -> throw new IOException("Invalid escape in JSON string.");
            }
        }
    }

    private Object readNumber() throws IOException {
        StringBuilder text = new StringBuilder();
        while (true) {
            int character = peek();
            if (character == -1 || "-+.eE0123456789".indexOf(character) < 0) {
                break;
            }
            text.append((char) read());
        }
        String number = text.toString();
        if (number.isEmpty()) {
            throw new IOException("Invalid JSON value.");
        }
        try {
            return Double.valueOf(number);
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid JSON number: " + number);
        }
    }

    private void expect(String literal) throws IOException {
        for (int index = 0; index < literal.length(); index++) {
            if (read() != literal.charAt(index)) {
                throw new IOException("Invalid JSON literal.");
            }
        }
    }

    private void skipWhitespace() throws IOException {
        while (true) {
            int character = peek();
            if (character == ' ' || character == '\n' || character == '\r' || character == '\t') {
                read();
                continue;
            }
            return;
        }
    }

    private int peek() throws IOException {
        if (pending == -2) {
            pending = reader.read();
        }
        return pending;
    }

    private int read() throws IOException {
        int character = peek();
        pending = -2;
        return character;
    }
}

package xeliox.simplegate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonFileUtil {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static <T> T read(File file, Class<T> cls) throws IOException {
        return jsonMapper.readValue(file, cls);
    }

    public static <T> void write(File file, T value) throws IOException {
        jsonMapper.writeValue(file, value);
    }

    public static <T> T fromJson(String json, Class<T> cls) throws IOException {
        return jsonMapper.readValue(json, cls);
    }
}
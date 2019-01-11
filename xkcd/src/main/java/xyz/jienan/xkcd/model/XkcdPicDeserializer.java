package xyz.jienan.xkcd.model;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class XkcdPicDeserializer implements JsonDeserializer {
    @Override
    public XkcdPic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Gson gson = new Gson();
        String string = json.toString();

        String enString;
        try {
            enString = new String(string.getBytes("ISO-8859-1"), Charset.forName("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            enString = string.replaceAll("\\u00e2\\u0080\\u0099", "'")
                    .replaceAll("\\u00e2\\u0080\\u009c", "\"")
                    .replaceAll("\\u00e2\\u0080\\u009d", "\"")
                    .replaceAll("\\u00e2\\u0080\\u0093", "-")
                    .replaceAll("\\u00c3\\u00a9", "é");
        }
        return gson.fromJson(enString, XkcdPic.class);
    }
}
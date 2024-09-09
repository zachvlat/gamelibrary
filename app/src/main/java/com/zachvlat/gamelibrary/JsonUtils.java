package com.zachvlat.gamelibrary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {

    public static List<Game> parseJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Game>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}

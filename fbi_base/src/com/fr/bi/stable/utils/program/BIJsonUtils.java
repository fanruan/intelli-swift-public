package com.fr.bi.stable.utils.program;

import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;

/**
 * Created by Connery on 2015/12/4.
 */
public class BIJsonUtils {
    public static String[][] jsonArrayToStringArray(JSONArray ja) throws JSONException {
        String[][] res = new String[ja.length()][];
        for (int i = 0, len = ja.length(); i < len; i++) {
            JSONArray t = ja.optJSONArray(i);
            String[] tempString = new String[t.length()];
            for (int j = 0, jlen = t.length(); j < jlen; j++) {
                tempString[j] = t.optString(j);
            }
            res[i] = tempString;
        }
        return res;
    }

    public static String[] jsonArray2StringArray(JSONArray ja) throws JSONException {
        String[] res = new String[ja.length()];
        for (int i = 0, len = ja.length(); i < len; i++) {
            res[i] = ja.getString(i);
        }
        return res;
    }


    public static JSONObject jsonJoin(JSONObject left, JSONObject right) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONArray arr = left.names();
        for (int i = 0; i < arr.length(); i++) {
            jo.put(arr.optString(i), left.opt(arr.optString(i)));
        }
        arr = right.names();
        for (int i = 0; i < arr.length(); i++) {
            jo.put(arr.optString(i), right.opt(arr.optString(i)));
        }
        return jo;
    }

    public enum JSON_TYPE {
        JSON_TYPE_OBJECT,
        JSON_TYPE_ARRAY,
        JSON_TYPE_ERROR
    }

    public static BIJsonUtils.JSON_TYPE getJSONType(String str) {
        if (StringUtils.isEmpty(str)) {
            return BIJsonUtils.JSON_TYPE.JSON_TYPE_ERROR;
        }

        final char[] strChar = str.substring(0, 1).toCharArray();
        final char firstChar = strChar[0];

        if (firstChar == '{') {
            return BIJsonUtils.JSON_TYPE.JSON_TYPE_OBJECT;
        } else if (firstChar == '[') {
            return BIJsonUtils.JSON_TYPE.JSON_TYPE_ARRAY;
        } else {
            return BIJsonUtils.JSON_TYPE.JSON_TYPE_ERROR;
        }
    }


    public static boolean isKeyValueSet(String str) {
        try {
            new JSONObject(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean isArray(String str) {
        BIJsonUtils.JSON_TYPE type = getJSONType(str);
        return type == BIJsonUtils.JSON_TYPE.JSON_TYPE_ARRAY;
    }

    public static JSONArray arrayConcat(JSONArray left, JSONArray right) throws JSONException {
        JSONArray res = new JSONArray(left.toString());
        for (int i = 0; i < right.length(); i++) {
            res.put(right.get(i));
        }
        return res;
    }
}
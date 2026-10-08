package com.dogra.mustang;
import org.json.*;
import java.util.*;
public class Track {
    public String id=UUID.randomUUID().toString(),title="",artist="",language="Other",lyrics="",credit="";
    public boolean video;
    public final LinkedHashMap<String,String> sources=new LinkedHashMap<>();
    public JSONObject json() throws JSONException {
        JSONObject j=new JSONObject();j.put("id",id);j.put("title",title);j.put("artist",artist);j.put("language",language);j.put("video",video);j.put("lyrics",lyrics);j.put("credit",credit);j.put("sources",new JSONObject(sources));return j;
    }
    public static Track from(JSONObject j) throws JSONException {
        Track t=new Track();t.id=j.getString("id");t.title=j.optString("title");t.artist=j.optString("artist");t.language=j.optString("language","Other");t.video=j.optBoolean("video");t.lyrics=j.optString("lyrics");t.credit=j.optString("credit");JSONObject s=j.getJSONObject("sources");Iterator<String> it=s.keys();while(it.hasNext()){String k=it.next();t.sources.put(k,s.getString(k));}return t;
    }
}

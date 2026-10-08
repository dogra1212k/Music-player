package com.dogra.mustang;
import android.content.*;
import org.json.*;
import java.util.*;
public final class Library {
    private static Library instance;
    public final List<Track> tracks=new ArrayList<>();
    private final Context context;
    public static synchronized Library get(Context c){if(instance==null)instance=new Library(c.getApplicationContext());return instance;}
    private Library(Context c){context=c;String saved=c.getSharedPreferences("library",0).getString("tracks",null);if(saved!=null)try{JSONArray a=new JSONArray(saved);for(int i=0;i<a.length();i++)tracks.add(Track.from(a.getJSONObject(i)));}catch(JSONException ignored){} }
    public void save(){JSONArray a=new JSONArray();try{for(Track t:tracks)a.put(t.json());context.getSharedPreferences("library",0).edit().putString("tracks",a.toString()).apply();}catch(JSONException ignored){} }
    public Track find(String id){for(Track t:tracks)if(t.id.equals(id))return t;return null;}
    public void add(Track t){for(Track item:tracks)if(item.sources.equals(t.sources))return;tracks.add(t);save();}
}

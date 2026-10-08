package com.dogra.mustang;
import java.util.*;
import java.util.regex.*;
/** UTF-8 LRC including repeated timestamps and millisecond offsets. */
public final class Lyrics {
    public record Line(long time, String text) {}
    public final List<Line> lines = new ArrayList<>();
    public final String plain;
    public Lyrics(String source) {
        plain=source==null?"":source;
        long offset=0;
        Matcher o=Pattern.compile("\\[offset:([+-]?\\d+)\\]",Pattern.CASE_INSENSITIVE).matcher(plain);
        if(o.find())try{offset=Long.parseLong(o.group(1));}catch(NumberFormatException ignored){}
        Pattern stamp=Pattern.compile("\\[(\\d+):(\\d{1,2})(?:[.:](\\d{1,3}))?\\]");
        for(String row:plain.split("\\R")) {
            Matcher m=stamp.matcher(row); List<Long> times=new ArrayList<>();int end=0;
            while(m.find()) { try {
                int seconds=Integer.parseInt(m.group(2)); if(seconds>=60)continue;
                String fraction=m.group(3);long millis=fraction==null?0:Long.parseLong((fraction+"000").substring(0,3));
                times.add(Math.max(0,Long.parseLong(m.group(1))*60000+seconds*1000+millis+offset));end=m.end();
            }catch(NumberFormatException ignored){} }
            if(end>0)for(long t:times)lines.add(new Line(t,row.substring(end).trim()));
        }
        lines.sort(Comparator.comparingLong(Line::time));
    }
    public int at(long position) {
        int lo=0,hi=lines.size()-1,answer=-1;
        while(lo<=hi){int mid=(lo+hi)>>>1;if(lines.get(mid).time()<=position){answer=mid;lo=mid+1;}else hi=mid-1;}
        return answer;
    }
}

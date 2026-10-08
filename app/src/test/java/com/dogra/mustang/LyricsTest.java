package com.dogra.mustang;
import org.junit.Test;
import static org.junit.Assert.*;
public class LyricsTest {
    @Test public void repeatedTimestampsOffsetsAndUnicode(){Lyrics l=new Lyrics("[offset:-100]\n[00:01.25][00:02.500]नया सवेरा\n[00:04.0]ਪਿਆਰੀ ਧੁਨ");assertEquals(3,l.lines.size());assertEquals(1150,l.lines.get(0).time());assertEquals(2400,l.lines.get(1).time());assertEquals("नया सवेरा",l.lines.get(0).text());assertEquals(-1,l.at(1000));assertEquals(1,l.at(3000));assertEquals(2,l.at(9999));}
    @Test public void plainTextHasNoFalseTiming(){Lyrics l=new Lyrics("Plain lyrics\n[ar:Artist]\n[00:99]invalid");assertTrue(l.lines.isEmpty());assertEquals(-1,l.at(2000));}
    @Test public void outOfOrderAndSecondsAreSorted(){Lyrics l=new Lyrics("[01:03]last\n[00:00.01]first\n[00:05.123]middle");assertEquals(10,l.lines.get(0).time());assertEquals(5123,l.lines.get(1).time());assertEquals(63000,l.lines.get(2).time());}
}

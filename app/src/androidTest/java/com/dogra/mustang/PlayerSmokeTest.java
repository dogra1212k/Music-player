package com.dogra.mustang;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.*;
import android.widget.TextView;
@SuppressWarnings("deprecation")
public class PlayerSmokeTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public PlayerSmokeTest(){super(MainActivity.class);}
    private View find(View root,String text){if(root instanceof TextView&&text.contentEquals(((TextView)root).getText()))return root;if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++){View v=find(g.getChildAt(i),text);if(v!=null)return v;}}return null;}
    public void testHomeAndMultilingualLibrary(){MainActivity a=getActivity();getInstrumentation().waitForIdleSync();assertNotNull(find(a.getWindow().getDecorView(),"MUSTANG"));assertNotNull(find(a.getWindow().getDecorView(),"＋ Files"));assertEquals(4,Library.get(a).tracks.size());assertEquals("Hindi",Library.get(a).tracks.get(1).language);assertFalse(new Lyrics(Library.get(a).tracks.get(1).lyrics).lines.isEmpty());}
    public void testRealAudioPlaybackPauseSeekAndStop() throws Exception {MainActivity a=getActivity();Track t=Library.get(a).tracks.get(0);getInstrumentation().runOnMainSync(()->a.startForegroundService(new Intent(a,PlaybackService.class).setAction("play").putExtra("id",t.id)));
        long end=System.currentTimeMillis()+10000;while((PlaybackService.instance==null||!PlaybackService.instance.playing())&&System.currentTimeMillis()<end)Thread.sleep(100);assertNotNull(PlaybackService.instance);assertTrue(PlaybackService.instance.status,PlaybackService.instance.playing());assertTrue(PlaybackService.instance.duration()>15000);
        getInstrumentation().runOnMainSync(()->PlaybackService.instance.seek(5000));Thread.sleep(700);assertTrue(PlaybackService.instance.position()>=4500);
        getInstrumentation().runOnMainSync(()->PlaybackService.instance.pause());assertFalse(PlaybackService.instance.playing());getInstrumentation().runOnMainSync(()->PlaybackService.instance.resume());assertTrue(PlaybackService.instance.playing());int beforeBackground=PlaybackService.instance.position();getInstrumentation().runOnMainSync(a::finish);Thread.sleep(1000);assertTrue("Audio must continue after activity closes",PlaybackService.instance.playing());assertTrue(PlaybackService.instance.position()>beforeBackground);getInstrumentation().runOnMainSync(()->a.stopService(new Intent(a,PlaybackService.class)));end=System.currentTimeMillis()+3000;while(PlaybackService.instance!=null&&System.currentTimeMillis()<end)Thread.sleep(100);assertNull(PlaybackService.instance);
    }
    public void testVideoQualityKeepsPosition() throws Exception {MainActivity a=getActivity();Track t=Library.get(a).tracks.get(3);assertTrue(t.video);assertEquals(2,t.sources.size());getInstrumentation().runOnMainSync(()->a.startForegroundService(new Intent(a,PlaybackService.class).setAction("play").putExtra("id",t.id)));
        long end=System.currentTimeMillis()+10000;while((PlaybackService.instance==null||!PlaybackService.instance.playing())&&System.currentTimeMillis()<end)Thread.sleep(100);assertNotNull(PlaybackService.instance);assertTrue(PlaybackService.instance.status,PlaybackService.instance.playing());
        getInstrumentation().runOnMainSync(()->PlaybackService.instance.seek(4000));Thread.sleep(700);getInstrumentation().runOnMainSync(()->PlaybackService.instance.quality("360p"));end=System.currentTimeMillis()+10000;while(!PlaybackService.instance.playing()&&System.currentTimeMillis()<end)Thread.sleep(100);assertTrue(PlaybackService.instance.status,PlaybackService.instance.playing());assertEquals("360p",PlaybackService.instance.quality);assertTrue("Switch should preserve position",PlaybackService.instance.position()>=3500);getInstrumentation().runOnMainSync(()->a.stopService(new Intent(a,PlaybackService.class)));
    }
}

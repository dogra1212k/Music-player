package com.dogra.mustang;

import android.app.*;
import android.content.*;
import android.media.*;
import android.media.session.*;
import android.net.Uri;
import android.os.*;
import android.view.SurfaceHolder;
import java.util.*;

public class PlaybackService extends Service {
    public static PlaybackService instance;
    public Track track;
    public String quality="",status="Ready";
    public boolean prepared,autoplay=true;
    private MediaPlayer player;
    private MediaSession session;
    private AudioManager audio;
    private AudioFocusRequest focus;
    private boolean resumeOnFocus,wantsPlay;
    private SurfaceHolder display;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable timeout=()->fail("Stream timed out. Check the connection or choose another quality.");
    private final BroadcastReceiver noisy=new BroadcastReceiver(){public void onReceive(Context c,Intent i){pause();}};
    public void onCreate(){super.onCreate();instance=this;audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        autoplay=getSharedPreferences("settings",0).getBoolean("autoplay",true);
        NotificationChannel channel=new NotificationChannel("playback","Music and video",NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        session=new MediaSession(this,"Mustang");session.setCallback(new MediaSession.Callback(){public void onPlay(){resume();}public void onPause(){pause();}public void onSkipToNext(){next(1);}public void onSkipToPrevious(){next(-1);}public void onSeekTo(long p){seek((int)p);}public void onStop(){stopSelf();}});session.setActive(true);
        focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setOnAudioFocusChangeListener(change->{
            if(change==AudioManager.AUDIOFOCUS_LOSS){resumeOnFocus=false;pause();}
            else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){resumeOnFocus=playing();pauseWithoutAbandoningFocus();}
            else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){if(player!=null&&prepared)player.setVolume(.2f,.2f);}
            else if(change==AudioManager.AUDIOFOCUS_GAIN){if(player!=null&&prepared)player.setVolume(1,1);if(resumeOnFocus){resumeOnFocus=false;resume();}}
        }).build();
        if(Build.VERSION.SDK_INT>=33)registerReceiver(noisy,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(noisy,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }
    public int onStartCommand(Intent i,int flags,int id){
        if(i==null)return START_NOT_STICKY;
        startForeground(1,notification());
        String action=i.getAction();
        if("play".equals(action)){Track t=Library.get(this).find(i.getStringExtra("id"));if(t!=null)load(t,"",0,true);else fail("This item is no longer in the library.");}
        else if("toggle".equals(action)){if(playing())pause();else resume();}
        else if("next".equals(action))next(1);
        else if("previous".equals(action))next(-1);
        else if("stop".equals(action))stopSelf();
        return START_NOT_STICKY;
    }
    private void releasePlayer(){handler.removeCallbacks(timeout);prepared=false;if(player!=null){player.release();player=null;}}
    public void load(Track t,String selected,int position,boolean play){
        releasePlayer();track=t;quality=t.sources.containsKey(selected)?selected:t.sources.keySet().stream().findFirst().orElse("");wantsPlay=play;
        String uri=t.sources.get(quality);if(uri==null){fail("No media source available.");return;}
        status="Loading…";update();
        try {
            MediaPlayer p=new MediaPlayer();player=p;p.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(t.video?AudioAttributes.CONTENT_TYPE_MOVIE:AudioAttributes.CONTENT_TYPE_MUSIC).build());
            p.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);p.setDataSource(this,Uri.parse(uri));if(t.video&&display!=null)p.setDisplay(display);
            p.setOnPreparedListener(mp->{if(mp!=player)return;handler.removeCallbacks(timeout);prepared=true;int start=Math.min(Math.max(0,position),Math.max(0,mp.getDuration()-1));
                if(start>0){mp.setOnSeekCompleteListener(done->{if(done!=player)return;done.setOnSeekCompleteListener(null);finishPrepared();});mp.seekTo(start,MediaPlayer.SEEK_CLOSEST);}else finishPrepared();
            });
            p.setOnCompletionListener(mp->{if(mp!=player)return;status="Finished";wantsPlay=false;if(autoplay)next(1);else{update();stopForeground(STOP_FOREGROUND_DETACH);}});
            p.setOnErrorListener((mp,what,extra)->{if(mp==player)fail("Cannot play this source ("+what+"/"+extra+"). Check the link or file format.");return true;});
            p.prepareAsync();handler.postDelayed(timeout,30000);
        } catch(Exception e){fail("Cannot open this file. Re-add it if moved or deleted.");}
    }
    private void finishPrepared(){if(wantsPlay)resume();else{status="Paused";update();stopForeground(STOP_FOREGROUND_DETACH);}}
    private void fail(String message){releasePlayer();wantsPlay=false;status=message;audio.abandonAudioFocusRequest(focus);update();stopForeground(STOP_FOREGROUND_DETACH);}
    public void resume(){wantsPlay=true;if(player==null&&track!=null){startForeground(1,notification());load(track,quality,0,true);return;}if(!prepared)return;
        startForeground(1,notification());if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED){wantsPlay=false;status="Another app has audio focus.";update();stopForeground(STOP_FOREGROUND_DETACH);return;}
        try{player.start();status="Playing";update();}catch(IllegalStateException e){fail("Playback could not start.");}}
    public void pause(){wantsPlay=false;if(player!=null&&prepared)try{player.pause();}catch(IllegalStateException ignored){}status="Paused";audio.abandonAudioFocusRequest(focus);update();stopForeground(STOP_FOREGROUND_DETACH);}
    private void pauseWithoutAbandoningFocus(){wantsPlay=false;if(player!=null&&prepared)try{player.pause();}catch(IllegalStateException ignored){}status="Paused for another app";update();}
    public boolean playing(){try{return player!=null&&prepared&&player.isPlaying();}catch(IllegalStateException e){return false;}}
    public int position(){try{return prepared?player.getCurrentPosition():0;}catch(IllegalStateException e){return 0;}}
    public int duration(){try{return prepared?player.getDuration():0;}catch(IllegalStateException e){return 0;}}
    public void seek(int pos){if(prepared)player.seekTo(Math.max(0,Math.min(pos,duration())),MediaPlayer.SEEK_CLOSEST);}
    public void quality(String label){if(track!=null&&track.sources.containsKey(label)){int pos=position();boolean play=playing()||(!prepared&&wantsPlay);startForeground(1,notification());load(track,label,pos,play);}}
    public void attach(SurfaceHolder holder){display=holder;if(player!=null)try{player.setDisplay(track!=null&&track.video?holder:null);}catch(IllegalStateException ignored){}}
    public void next(int delta){List<Track> all=Library.get(this).tracks;if(all.isEmpty())return;int at=all.indexOf(track);int next=at+delta;if(next<0||next>=all.size()){pause();status="End of queue";update();return;}startForeground(1,notification());load(all.get(next),"",0,true);}
    private PendingIntent command(String action){return PendingIntent.getService(this,action.hashCode(),new Intent(this,PlaybackService.class).setAction(action),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private Notification notification(){PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(this,"playback").setSmallIcon(com.dogra.mustang.R.drawable.ic_mustang).setContentTitle(track==null?"Mustang Player":track.title).setContentText(status).setContentIntent(open).setOnlyAlertOnce(true).setVisibility(Notification.VISIBILITY_PUBLIC)
        .addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous,"Previous",command("previous")).build())
        .addAction(new Notification.Action.Builder(playing()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing()?"Pause":"Play",command("toggle")).build())
        .addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next,"Next",command("next")).build())
        .addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"Stop",command("stop")).build())
        .setStyle(new Notification.MediaStyle().setMediaSession(session.getSessionToken()).setShowActionsInCompactView(0,1,2)).build();}
    private void update(){if(session==null)return;session.setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_PLAY_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS|PlaybackState.ACTION_SEEK_TO|PlaybackState.ACTION_STOP).setState(playing()?PlaybackState.STATE_PLAYING:prepared?PlaybackState.STATE_PAUSED:player!=null?PlaybackState.STATE_BUFFERING:PlaybackState.STATE_STOPPED,position(),playing()?1:0).build());
        if(track!=null)session.setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,track.title).putString(MediaMetadata.METADATA_KEY_ARTIST,track.artist).putLong(MediaMetadata.METADATA_KEY_DURATION,duration()).build());
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,notification());}
    public IBinder onBind(Intent i){return null;}
    public void onDestroy(){releasePlayer();audio.abandonAudioFocusRequest(focus);unregisterReceiver(noisy);session.release();stopForeground(STOP_FOREGROUND_REMOVE);if(instance==this)instance=null;super.onDestroy();}
}

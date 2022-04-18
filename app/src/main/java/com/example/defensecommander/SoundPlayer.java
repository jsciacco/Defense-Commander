package com.example.defensecommander;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

public class SoundPlayer {

        private static final String TAG = "SoundPlayer";
        private static SoundPlayer instance;
        private final SoundPool soundPool;
        private static final int MAX_STREAMS = 10;
        private final HashSet<Integer> loaded = new HashSet<>();
        private final HashSet<String> loopList = new HashSet<>();
        private final HashMap<String, Integer> soundNameToResource = new HashMap<>();
        private final HashMap<String, Integer> soundNameToStream = new HashMap<>();

        static int loadCount;
        static int doneCount;

        private SoundPlayer() {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(MAX_STREAMS);
            this.soundPool = builder.build();
            this.soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                Log.d(TAG, "onLoadComplete: #" + sampleId + "  " + status);
                loaded.add(sampleId);
                doneCount++;
            });
        }

        static SoundPlayer getInstance() {
            if (instance == null)
                instance = new SoundPlayer();
            return instance;
        }

        void setupSound(Context context, String id, int resource, boolean loop) {

            int soundId = soundPool.load(context, resource, 1);

            soundNameToResource.put(id, soundId);

            if (loop)
                loopList.add(id);

            Log.d(TAG, "setupSound: " + id + ": #" + soundId);
            loadCount++;


        }

        void start(final String id) {

            if (!loaded.contains(soundNameToResource.get(id))) {
                Log.d(TAG, "start: SOUND NOT LOADED: " + id);
                return;
            }

            int loop = 0;
            if(loopList.contains(id))
                loop = -1;

            Integer resId = soundNameToResource.get(id);

            if (resId == null)
                return;

            int streamId = soundPool.play(resId, 1f, 1f, 1, loop, 1f);
            soundNameToStream.put(id, streamId);
        }

        void stop(String id) {

        Integer streamId = soundNameToStream.get(id);
        if (streamId == null)
            return;
        soundPool.stop(streamId);
    }

}

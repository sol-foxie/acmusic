package moe.foxie.sol.acmusic;

import java.lang.Thread;

import static moe.foxie.sol.acmusic.MusicManagerKt.getHour24;

public class FetcherThread extends Thread {
    private MusicManager music;
    private WeatherManager weather;

    private boolean shouldUpdate;

    FetcherThread(WeatherManager weather) {
        this.weather = weather;
    }

    public void setMusicManager(MusicManager manager) {
        this.music = manager;
    }

    @Override
    public void run() {
        super.run();
        try {
            while (true) {
                synchronized (this) {while (!shouldUpdate) wait(); }
                shouldUpdate = false;

                TrackInfo currentTrack = music.getCurrentlyPlaying();
                final int nextHour = getHour24();
                if (currentTrack != null && currentTrack.getHour() == nextHour) continue;
                music.changeTrackID(new TrackInfo(nextHour, weather.currentWeather()));

                if (isInterrupted()) throw new InterruptedException();
            }
        } catch (InterruptedException e) { return; }
    }

    public synchronized void shouldUpdate() {
        shouldUpdate = true;
        this.notify();
    }
}

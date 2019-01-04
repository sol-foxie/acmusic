package moe.foxie.sol.acmusic;

import kotlin.Pair;

import java.lang.Thread;

import static moe.foxie.sol.acmusic.MusicManagerKt.getHour24;

public class FetcherThread extends Thread {

    private MusicManager music;
    private WeatherManager weather;

    private boolean shouldUpdate;

    FetcherThread(MusicManager music, WeatherManager weather) {
        this.music = music;
        this.weather = weather;
    }

    @Override
    public void run() {
        super.run();
        try {
            while (true) {
                synchronized (this) { while (!shouldUpdate) wait(); }
                shouldUpdate = false;
                WeatherManager.Forecast f = weather.currentWeather();
                music.changeTrackID(new Pair<>(getHour24(),f.getWeather()));
                if (isInterrupted()) throw new InterruptedException();
            }
        } catch (InterruptedException ex) { return; }
    }

    public synchronized void shouldUpdate() {
        shouldUpdate = true;
        this.notify();
    }
}

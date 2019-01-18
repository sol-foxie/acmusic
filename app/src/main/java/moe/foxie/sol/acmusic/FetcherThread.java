package moe.foxie.sol.acmusic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import java.lang.Thread;

import static moe.foxie.sol.acmusic.GlobalKt.wolfFence;

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
                synchronized (this) {
                    while (!shouldUpdate) wait();
                    shouldUpdate = false;
                    music.changeTrackID(weather.currentWeather());
                    if (isInterrupted()) throw new InterruptedException();
                }
            }
        } catch (InterruptedException ex) { return; }
    }

    public synchronized void shouldUpdate() {
        shouldUpdate = true;
        this.notify();
    }
}

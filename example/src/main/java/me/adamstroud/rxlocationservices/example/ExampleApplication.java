package me.adamstroud.rxlocationservices.example;

import android.app.Application;

import timber.log.Timber;

/**
 * TODO
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree() {
            @Override
            protected void log(int priority, String tag, String message, Throwable t) {
                super.log(priority, tag, String.format("[%s] %s", Thread.currentThread().getName() , message), t);
            }
        });
    }
}

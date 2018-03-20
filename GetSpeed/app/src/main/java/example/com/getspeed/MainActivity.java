package example.com.getspeed;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
//    private final int SAMPLING_PEROID = 1000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensorManager();
    }

    private void initSensorManager() {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    public void onResume() {
        super.onResume();

        // register sensor
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );

    }

    @Override
    public void onPause() {
        super.onPause();

        // deregister sensor
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        handleAccelerometerEvent(sensorEvent);
    }

    private void handleAccelerometerEvent(SensorEvent sensorEvent) {

        float[] values = sensorEvent.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //TODO: handle this event


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    //  Called when the accuracy of the registered sensor has changed.
    }
}

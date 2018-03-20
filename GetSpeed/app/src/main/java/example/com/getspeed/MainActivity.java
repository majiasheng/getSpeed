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
    public void onSensorChanged(SensorEvent sensorEvent) {
    // Called when there is a new sensor event.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    //  Called when the accuracy of the registered sensor has changed.
    }
}

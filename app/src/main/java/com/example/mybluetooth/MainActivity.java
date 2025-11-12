package com.example.mybluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ScrollView page1;
    private TabLayout tabLayout;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private LinearLayout linear1;
    private Button button;
    private ArrayList<LinearLayoutClass> linearLayoutClasses = new ArrayList<>();
    public static BluetoothGatt bluetoothGatt = null;
    private LinearLayout toolBar2;
    private Integer selectIndex = null;
    private TextView connectedName;
    private TextView connectedMac;
    public static BluetoothDevice connectedDevice=null;
    private boolean isConnected=false;
    private List<UUID> connectedUuids=null;
    private List<BluetoothGattService> bluetoothGattServices=null;
    public static String strLog="";
    // private ScrollView scrollView;
    private TextView logView;
    private TextView disconnectButton;

    private LinearLayout page1LinearLayout;
    private LinearLayout page2LinearLayout;

    private DataType blueToothData=new DataType();

    private SensorManager sensorManager;
    private DataType imuData=new DataType();

    private TextView imuLogerView;
    private long imuTime=0;

    private File imu_file=null;
    private File bluetooth_file=null;
    private boolean isRecord=false;

    private TextView saveButton;

    private final int xLableMax=24;

    private MyList imuMagX=new MyList();
    private MyList imuMagY=new MyList();
    private MyList imuMagZ=new MyList();
    private MyList imuMagM=new MyList();

    private MyList blueAccX=new MyList();
    private MyList blueAccY=new MyList();
    private MyList blueAccZ=new MyList();

    private MyList blueAsX=new MyList();
    private MyList blueAsY=new MyList();
    private MyList blueAsZ=new MyList();

    private LineChart blue_tooth_acc_chart;
    private LineChart blue_tooth_as_chart;
    private LineChart imu_mag_chart;

    private final int disconnet_sleep_time_mill=1000;


    private String[] permissions=new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_SCAN
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* init the child */
        initChild();

        askForPermission();
        checkBLE();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)
                        !=PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请授予位置权限", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        initBlueTooth();

        // handler.postDelayed(runnable, 1000);

        // scan device
        addScanListner();

        initSensorManager();
    }

    private void initSensorManager(){
        sensorManager=(SensorManager) this.getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME );
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
    }

    private SensorEventListener sensorEventListener=new SensorEventListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            switch (sensorEvent.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    imuTime=System.currentTimeMillis();
                    imuData.acc_x=sensorEvent.values[0];
                    imuData.acc_y=sensorEvent.values[1];
                    imuData.acc_z=sensorEvent.values[2];
                    if(isRecord){
                        try {
                            FileOutputStream fileOutputStream=new FileOutputStream(imu_file,true);
                            fileOutputStream.write(String.format("ACC,%f,%f,%f,%f\n",
                                    System.currentTimeMillis()*1e-3,imuData.acc_x,imuData.acc_y,imuData.acc_z).getBytes());
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    imuData.gyr_x=sensorEvent.values[0];
                    imuData.gyr_y=sensorEvent.values[1];
                    imuData.gyr_z=sensorEvent.values[2];
                    if(isRecord){
                        try {
                            FileOutputStream fileOutputStream=new FileOutputStream(imu_file,true);
                            fileOutputStream.write(String.format("GYR,%f,%f,%f,%f\n",
                                    System.currentTimeMillis()*1e-3,imuData.gyr_x,imuData.gyr_y,imuData.gyr_z).getBytes());
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    imuData.mag_x=sensorEvent.values[0];
                    imuData.mag_y=sensorEvent.values[1];
                    imuData.mag_z=sensorEvent.values[2];

                    imuMagX.addItem(imuData.mag_x);
                    imuMagY.addItem(imuData.mag_y);
                    imuMagZ.addItem(imuData.mag_z);
                    imuMagM.addItem(Math.sqrt(
                            Math.pow(imuData.mag_x,2)+Math.pow(imuData.mag_y,2)+Math.pow(imuData.mag_z,2)));

                    if(imuMagX.items.length>xLableMax){
                        imuMagX.removeFirst();
                        imuMagY.removeFirst();
                        imuMagZ.removeFirst();
                        imuMagM.removeFirst();
                    }

                    if(isRecord){
                        try {
                            FileOutputStream fileOutputStream=new FileOutputStream(imu_file,true);
                            fileOutputStream.write(String.format("MAG,%f,%f,%f,%f\n",
                                    System.currentTimeMillis()*1e-3,imuData.mag_x,imuData.mag_y,imuData.mag_z).getBytes());
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };





    private void addScanListner(){
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
            @Override
            public void onClick(View view) {
                linearLayoutClasses = new ArrayList<LinearLayoutClass>();
                linear1.removeAllViews();
                // initBlueTooth();
                // Toast.makeText(MainActivity.this, "刷新中", Toast.LENGTH_LONG).show();
                BluetoothLeScanner mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
                mBLEScanner.startScan(mScanCallback);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        mBLEScanner.stopScan(mScanCallback);
                    }
                }, 1000);
                Toast.makeText(MainActivity.this, "刷新完成", Toast.LENGTH_LONG).show();
            }
        });
    }




    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        @Override
        public void run() {
            handler.postDelayed(this, 1000);
            if (linearLayoutClasses.size() == 0) {
                linearLayoutClasses = new ArrayList<LinearLayoutClass>();
                linear1.removeAllViews();
                // initBlueTooth();
                // Toast.makeText(MainActivity.this, "刷新中", Toast.LENGTH_LONG).show();
                BluetoothLeScanner mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
                mBLEScanner.startScan(mScanCallback);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        mBLEScanner.stopScan(mScanCallback);
                    }
                }, 1000);
            }
        }
    };

    private void initChild(){
        tabLayout = this.findViewById(R.id.tab);
        page1 = this.findViewById(R.id.page1);
        linear1 = this.findViewById(R.id.line1);
        button = this.findViewById(R.id.button);
        toolBar2=this.findViewById(R.id.toolBar2);
        connectedName=this.findViewById(R.id.connectName);
        connectedMac=this.findViewById(R.id.connectMac);
        // scrollView=this.findViewById(R.id.scrollView);
        logView=this.findViewById(R.id.logView);
        disconnectButton=this.findViewById(R.id.disconnect);
        page1LinearLayout=this.findViewById(R.id.myPage1);
        page2LinearLayout=this.findViewById(R.id.myPage2);
        imuLogerView=this.findViewById(R.id.imu_logView);

        saveButton=this.findViewById(R.id.save_button);

        blue_tooth_acc_chart=this.findViewById(R.id.drawAccBlueTooth);
        blue_tooth_as_chart=this.findViewById(R.id.drawAsBlueTooth);
        imu_mag_chart=this.findViewById(R.id.drawMagIMU);


        //log.addView(childLog);
        page1Select();
        /*set the tab select event*/
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                if (index == 0) {
                    page1Select();
                } else if (index == 1) {
                    page2Select();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void page1Select() {
        page1.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        toolBar2.setVisibility(View.GONE);
        // scrollView.setVisibility(View.GONE);
        page1LinearLayout.setVisibility(View.VISIBLE);

        page2LinearLayout.setVisibility(View.GONE);
    }

    private void page2Select() {
        page1.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        page1LinearLayout.setVisibility(View.GONE);

        if(isConnected){
            toolBar2.setVisibility(View.VISIBLE);
            // scrollView.setVisibility(View.VISIBLE);
            disconnectButton.setText("断开设备");
            page2LinearLayout.setVisibility(View.VISIBLE);
        }else if(strLog.length()>0){
            toolBar2.setVisibility(View.VISIBLE);
            // scrollView.setVisibility(View.VISIBLE);
            disconnectButton.setText("连接设备");
            page2LinearLayout.setVisibility(View.VISIBLE);
        }



    }

    private void initBlueTooth() {
        //init bluetooth service
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_LONG).show();
            System.exit(0);
        }
        //setup the bluetooth
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, 1);
            }
        }
        BluetoothLeScanner mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED){
            mBLEScanner.startScan(mScanCallback);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    mBLEScanner.stopScan(mScanCallback);
                }
            }, 1000);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
            // mBLEScanner.startScan(mScanCallback);
           // if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BL))
            // Toast.makeText(this,"no bluetooth scan permissions",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)
                ==PackageManager.PERMISSION_GRANTED){
                mBLEScanner.startScan(mScanCallback);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        mBLEScanner.stopScan(mScanCallback);
                    }
                }, 1000);
            }
        }

    }

    /*scan*/
    private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice bluetoothDevice = result.getDevice();
            boolean repetition = false;
            for (int i = 0; i < linearLayoutClasses.size(); i++) {
                if (bluetoothDevice.equals(linearLayoutClasses.get(i).mBluetoothDevice)) {
                    repetition = true;
                }
            }
            if (!repetition&&bluetoothDevice.getName()!=null) {
                int rssi = result.getRssi();
                LinearLayoutClass linearLayoutClass = new LinearLayoutClass(MainActivity.this, bluetoothDevice, rssi);
                linearLayoutClass.Index = linearLayoutClasses.size();
                List<ParcelUuid> parcelUuids=result.getScanRecord().getServiceUuids();
                if(parcelUuids!=null){
                    linearLayoutClass.serviceUUid=new ArrayList<>();
                    for(int i=0;i<parcelUuids.size();i++){
                        linearLayoutClass.serviceUUid.add(parcelUuids.get(i).getUuid());
                    }
                }
                linearLayoutClass.button.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View view) {
                        if(bluetoothGatt!=null){
                            bluetoothGatt.disconnect();
                            bluetoothGatt.close();
                            strLog="";
                        }
                        bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this,
                                false, bluetoothGattCallback);
                        selectIndex = linearLayoutClass.Index;
                        try{
                            Thread.sleep(disconnet_sleep_time_mill);
                            connected();
                        }catch (Exception e){}
                    }
                });
                linear1.addView(linearLayoutClass.mainLayout);
                linearLayoutClasses.add(linearLayoutClass);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void askForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this,Manifest.permission.MANAGE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                   permissions, 1);
        }
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "本机设备不支持低功耗蓝牙设备", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void reLoad(View view) {
        linearLayoutClasses = new ArrayList<LinearLayoutClass>();
        linear1.removeAllViews();
        //Toast.makeText(this, "刷新中", Toast.LENGTH_LONG).show();
        // initBlueTooth();
        BluetoothLeScanner mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
        mBLEScanner.startScan(mScanCallback);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                mBLEScanner.stopScan(mScanCallback);
            }
        }, 1000);
    }

    private void toastUtil(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
    private boolean gotoServiceDiscover=false;
    private final BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.e("debug","onPhyUpdate status:"+status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.e("debug","onPhyRead status:"+status);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);
            Log.e("debug","onConnectionStateChange status:"+newState);
            if(newState==BluetoothProfile.STATE_CONNECTED){
                isConnected=true;
                gatt.discoverServices();
                gatt.readRemoteRssi();
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){
                Log.e("debug","disconnected");
                Looper.prepare();
                toastUtil("连接断开！");
                isConnected=false;
                Looper.loop();
            }else if(newState==BluetoothProfile.STATE_CONNECTING){
                Looper.prepare();
                toastUtil("连接中...");
                Looper.loop();
            }else if(newState==BluetoothProfile.STATE_DISCONNECTING){
                Looper.prepare();
                toastUtil("连接断开中...");
                Looper.loop();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            gotoServiceDiscover=true;

            bluetoothGattServices=gatt.getServices();
            List<BluetoothGattCharacteristic> characteristics=new ArrayList<>();
            try{
                for(int i=0;i<bluetoothGattServices.size();i++){
                    Log.e("debug","service uuid:"+bluetoothGattServices.get(i).getUuid());
                    for(int j=0;j<bluetoothGattServices.get(i).getCharacteristics().size();j++){
                        Log.e("debug","character uuid:"+bluetoothGattServices.get(i).getCharacteristics().get(j).getUuid());
                        Log.e("debug","character write type:"+bluetoothGattServices.get(i).getCharacteristics().get(j).getWriteType());
                        characteristics.add(bluetoothGattServices.get(i).getCharacteristics().get(j));
                    }
                }
                //set up the data read
                for(int i=0;i<characteristics.size();i++){
                    gatt.setCharacteristicNotification(characteristics.get(i),true);
                    for(BluetoothGattDescriptor dp:characteristics.get(i).getDescriptors()){
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(dp);
                    }

                }
            }catch (Exception e){
                Log.e("debug","error:"+e.toString());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("debug","onCharacteristicRead data:"+characteristic.getValue().toString());

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e("debug","onCharacteristicWrite status:"+status);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            // Log.e("MMM",getCurrentTime()+">"+parse(characteristic));
            blueToothData=OnReceiveBle(characteristic.getValue());
            MainActivity.strLog=getCurrentTime()+">"
                    +String.format("[ACC] %.3f,%.3f,%.3f, [AS] %.3f,%.3f,%.3f, [Angle] %.3f,%.3f,%.3f, [E] %.3f\n",
                    blueToothData.acc_x,blueToothData.acc_y,blueToothData.acc_z,
                    blueToothData.as_x,blueToothData.as_y,blueToothData.as_z,
                    blueToothData.angle_x,blueToothData.angle_y,blueToothData.angle_z,
                    blueToothData.e);
//            Log.e("Debug",String.format("[ACC] %.3f,%.3f,%.3f, [AS] %.3f,%.3f,%.3f, [Angle] %.3f,%.3f,%.3f, [E] %.3f",
//                    blueToothData.acc_x,blueToothData.acc_y,blueToothData.acc_z,
//                    blueToothData.as_x,blueToothData.as_y,blueToothData.as_z,
//                    blueToothData.angle_x,blueToothData.angle_y,blueToothData.angle_z,
//                    blueToothData.e));
            blueAccX.addItem(blueToothData.acc_x);
            blueAccY.addItem(blueToothData.acc_y);
            blueAccZ.addItem(blueToothData.acc_z);
//            imuMagM.addItem(Math.sqrt(
//                    Math.pow(imuData.mag_x,2)+Math.pow(imuData.mag_y,2)+Math.pow(imuData.mag_z,2)));

            blueAsX.addItem(blueToothData.as_x);
            blueAsY.addItem(blueToothData.as_y);
            blueAsZ.addItem(blueToothData.as_z);


            if(blueAccX.items.length>xLableMax){
//                imuMagX.removeFirst();
//                imuMagY.removeFirst();
//                imuMagZ.removeFirst();
//                imuMagM.removeFirst();
                blueAccX.removeFirst();
                blueAccY.removeFirst();
                blueAccZ.removeFirst();

                blueAsX.removeFirst();
                blueAsY.removeFirst();
                blueAsZ.removeFirst();
            }

            if(isRecord){
                try {
                    FileOutputStream fileOutputStream=new FileOutputStream(bluetooth_file,true);
                    fileOutputStream.write(String.format("%f,[ACC],%f,%f,%f,[As],%f,%f,%f,[Angle],%f,%f,%f\n",
                            System.currentTimeMillis()*1e-3,blueToothData.acc_x,blueToothData.acc_y,blueToothData.acc_z,
                            blueToothData.as_x,blueToothData.as_y,blueToothData.as_z,
                            blueToothData.angle_x,blueToothData.angle_y,blueToothData.angle_z).getBytes());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //super.onDescriptorRead(gatt, descriptor, status);
            Log.e("debug","onDescriptorRead status:"+status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("debug","onDescriptorWriter status:"+status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            //super.onReliableWriteCompleted(gatt, status);
            Log.e("debug","onReliableWriteCompleted status:"+status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            //super.onReadRemoteRssi(gatt, rssi, status);
            Log.e("debug","onReadRemoteRssi rssi:"+rssi);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            //super.onMtuChanged(gatt, mtu, status);
            Log.e("debug","onMtuChanged mtu:"+mtu);
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            //super.onServiceChanged(gatt);
            Log.e("debug","onServiceChanged gatt:"+gatt.toString());
        }
    };
    @SuppressLint("MissingPermission")
    private void connected(){
        if(selectIndex!=null&&isConnected){
            toastUtil("连接成功！");
            connectedDevice=linearLayoutClasses.get(selectIndex).mBluetoothDevice;
            connectedName.setText((connectedDevice.getName()==null)?"N/A":connectedDevice.getName());
            connectedMac.setText(connectedDevice.getAddress());
            connectedUuids=linearLayoutClasses.get(selectIndex).serviceUUid;
            tabLayout.getTabAt(1).select();
            page2Select();
            reloadUI();
        }else{
            toastUtil("连接失败！");
        }
    }
    private Handler uiHandler=new Handler();
    private Runnable uiRunnable=new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            if(isConnected){
                handler.postDelayed(this,50);
//                scrollView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
//                    }
//                });
//                String temp="";
//                if(strLog.length()<=10000){
//                    temp=strLog;
//                }else{
//                    temp=strLog.substring(strLog.length()-10001);
//                }
                logView.setText(strLog);
                if(imuTime!=0){
                    if(isRecord){
                        imuLogerView.setText(String.format("fileName:%s,%s\n%d>[ACC] %.3f,%.3f,%.3f,[GYR] %.3f,%.3f,%.3f,[MAG] %.3f,%.3f,%.3f",
                                imu_file.getName(),bluetooth_file.getName(),imuTime,imuData.acc_x,imuData.acc_y,imuData.acc_z,
                                imuData.gyr_x,imuData.gyr_y,imuData.gyr_z,
                                imuData.mag_x,imuData.mag_y,imuData.mag_z));
                    }else{
                        imuLogerView.setText(String.format("%d>[ACC] %.3f,%.3f,%.3f,[GYR] %.3f,%.3f,%.3f,[MAG] %.3f,%.3f,%.3f",
                                imuTime,imuData.acc_x,imuData.acc_y,imuData.acc_z,
                                imuData.gyr_x,imuData.gyr_y,imuData.gyr_z,
                                imuData.mag_x,imuData.mag_y,imuData.mag_z));
                    }


//                    if(isRecord){
//                        try {
//                            FileOutputStream fileOutputStream=new FileOutputStream(imu_file,true);
//                            fileOutputStream.write(String.format("%d>[ACC] %.3f,%.3f,%.3f,[GYR] %.3f,%.3f,%.3f,[MAG],%.3f,%.3f,%.3f\n",
//                                    imuTime,imuData.acc_x,imuData.acc_y,imuData.acc_z,
//                                    imuData.gyr_x,imuData.gyr_y,imuData.gyr_z,
//                                    imuData.mag_x,imuData.mag_y,imuData.mag_z).getBytes());
//                        } catch (FileNotFoundException e) {
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        try {
//                            FileOutputStream fileOutputStream=new FileOutputStream(bluetooth_file,true);
//                            fileOutputStream.write(strLog.getBytes());
//                        } catch (FileNotFoundException e) {
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
                }

                DrawLines.drawChart(blue_tooth_acc_chart,blueAccX,blueAccY,blueAccZ,"BlueTooth-ACC-X","BlueTooth-ACC-Y","BlueTooth-ACC-Z");
                DrawLines.drawChart(blue_tooth_as_chart,blueAsX,blueAsY,blueAsZ,"BlueTooth-GYR-X","BlueTooth-GYR-Y","BlueTooth-GYR-Z");
                DrawLines.drawChart4(imu_mag_chart,imuMagX,imuMagY,imuMagZ,imuMagM,"IMU-MAG-X","IMU-MAG-Y","IMU-MAG-Z","IMU-MAG-M");
            }
        }
    };
    private void reloadUI(){
        uiHandler.postDelayed(uiRunnable,0);
    }
    private String getCurrentTime(){
        String res=null;
        res=String.format("%d",System.currentTimeMillis());
        return res;
    }
    @SuppressLint("MissingPermission")
    public void disconnected(View view){
        if(bluetoothGatt!=null){
            if(isConnected){
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                try{
                    Thread.sleep(disconnet_sleep_time_mill);
                    bluetoothGatt=null;
                    isConnected=false;
                    uiHandler.removeCallbacks(runnable);
                    disconnectButton.setText("连接设备");
                    Toast.makeText(this,"设备已断开",Toast.LENGTH_SHORT).show();
                }catch (Exception e){}

            }else{
                // bluetoothGatt=connectedDevice.connectGatt(this,false,bluetoothGattCallback);
                bluetoothGatt.connect();
                try{
                    Thread.sleep(disconnet_sleep_time_mill);
                    if(isConnected){
                        disconnectButton.setText("断开设备");
                        uiHandler.postDelayed(uiRunnable,0);
                        Toast.makeText(this,"设备已连接",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    // bluetoothGatt=connectedDevice.connectGatt(this,false,bluetoothGattCallback);
                    // bluetoothGatt.connect();
//                    if(isConnected){
//                        disconnectButton.setText("断开设备");
//                        uiHandler.postDelayed(uiRunnable,0);
//                        Toast.makeText(this,"设备已连接",Toast.LENGTH_SHORT).show();
//                    }
                    Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            bluetoothGatt=connectedDevice.connectGatt(this,false,bluetoothGattCallback);
            bluetoothGatt.connect();
            try{
                Thread.sleep(disconnet_sleep_time_mill);
                if(isConnected){
                    disconnectButton.setText("断开设备");
                    uiHandler.postDelayed(uiRunnable,0);
                    Toast.makeText(this,"设备已连接",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                // bluetoothGatt=connectedDevice.connectGatt(this,false,bluetoothGattCallback);
                // bluetoothGatt.connect();
//                if(isConnected){
//                    disconnectButton.setText("断开设备");
//                    uiHandler.postDelayed(uiRunnable,0);
//                    Toast.makeText(this,"设备已连接",Toast.LENGTH_SHORT).show();
//                }
                Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void clearAll(View view){
        strLog="";
        logView.setText("");
        toastUtil("日志清空成功");
    }
    public static Editable fileName=null;
    public void saveFile(View view){
        if(isRecord){
            isRecord=false;
            saveButton.setText("开始保存");
            return;
        }
        //open the file dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("提示：文件保存");
        builder.setMessage("保存至：文件/documents/myBle/");
        final EditText editText=new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do not disappear
                try{
                    Field field=builder.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(builder,false);
                }catch (Exception e){}
                //
                if(editText.getText().length()>0){
                   fileName=editText.getText();
                    //write the data
                    String rootPath="sdcard/documents";
                    File dir=new File(rootPath,"myBle");
                    if(!dir.exists()){
                        dir.mkdirs();
                    }

                    if(dir.exists()){
                        imu_file=new File(dir,fileName+"_imu.txt");
                        bluetooth_file=new File(dir,fileName+"_bluetooth.txt");
                        if(!imu_file.exists()){
                            try {
                                imu_file.createNewFile();
                            }catch (Exception e){

                            }
                        }
                        if(!bluetooth_file.exists()){
                            try {
                                bluetooth_file.createNewFile();
                            }catch (Exception e){

                            }
                        }
                        if(imu_file.exists()&&bluetooth_file.exists()){
                            isRecord=true;
                            saveButton.setText("停止保存");
                        }
                        toastUtil("save in the sdcard/documents/myBle/"+fileName);
//                        File file=new File(dir,fileName+".txt");
//                        if(!file.exists()){
//                            try{
//                                file.createNewFile();
//                                if(file.exists()){
//                                    FileOutputStream outputStream=new FileOutputStream(file);
//                                    outputStream.write(strLog.getBytes());
//                                    outputStream.close();
//                                    toastUtil("save in the sdcard/documents/myBle/"+fileName+".txt");
//                                }else {
//                                    toastUtil("no file");
//                                }
//                            }catch (Exception e){
//                                toastUtil(e.toString());
//                            }
//                        }
                    }else {
                        toastUtil("no directory");
                    }
                }else{
                    toastUtil("请输入文件名");
                }
            }
        });
        builder.show();

//        if(isRecord){
//            saveButton.setText("停止保存");
//        }

    }
    public static String parse(final BluetoothGattCharacteristic characteristic){
        final char[] HEX_ARRAY="0123456789ABCDEF".toCharArray();
        final byte[] data=characteristic.getValue();
        if(data==null){
            return "";
        }
        if(data.length==0){
            return "";
        }
        final char[] out=new char[data.length*3-1];
        for(int j=0;j<data.length;j++){
            int v=data[j]&0xFF;
            out[j*3]=HEX_ARRAY[v>>>4];
            out[j*3+1]=HEX_ARRAY[v&0x0F];
            if(j!=data.length-1){
                out[j*3+2]='\t';
            }
        }
        return new String(out);
    }

//    public static double[] imuData(final BluetoothGattCharacteristic characteristic){
//        double[] imu_datas=new double[9];
//
//
//
//        return imu_datas;
//    }

    public DataType OnReceiveBle(byte[] value){
        DataType res=new DataType();

        List<Byte> dataBuffer = new ArrayList<>();
        for (byte b : value) {
            dataBuffer.add(b);
        }

        // 不是55 61 或者 55 71
        while (dataBuffer.size() > 2 && (dataBuffer.get(0) != 0x55 || (dataBuffer.get(1) != 0x61 && dataBuffer.get(1) != 0x71))){
            dataBuffer.remove(0);
        }

        while (dataBuffer.size() >= 20){
            ArrayList<Byte> activeByteTemp = new ArrayList<>(dataBuffer.subList(0,20));
            dataBuffer = new ArrayList<>(dataBuffer.subList(20, dataBuffer.size()));
            if(activeByteTemp.get(0) == 0x55 && activeByteTemp.get(1) == 0x61){
                res.acc_x = DeviceModel.GetShortData(activeByteTemp.get(2), activeByteTemp.get(3)) / 32768.0 * 16*9.8;
                res.acc_y = DeviceModel.GetShortData(activeByteTemp.get(4), activeByteTemp.get(5)) / 32768.0 * 16*9.8;
                res.acc_z = DeviceModel.GetShortData(activeByteTemp.get(6), activeByteTemp.get(7)) / 32768.0 * 16*9.8;

                res.as_x = DeviceModel.GetShortData(activeByteTemp.get(8), activeByteTemp.get(9)) / 32768.0 * 2000;
                res.as_y = DeviceModel.GetShortData(activeByteTemp.get(10), activeByteTemp.get(11)) / 32768.0 * 2000;
                res.as_z = DeviceModel.GetShortData(activeByteTemp.get(12), activeByteTemp.get(13)) / 32768.0 * 2000;

                res.angle_x = DeviceModel.GetShortData(activeByteTemp.get(14), activeByteTemp.get(15)) / 32768.0 * 180;
                res.angle_y = DeviceModel.GetShortData(activeByteTemp.get(16), activeByteTemp.get(17)) / 32768.0 * 180;
                res.angle_z = DeviceModel.GetShortData(activeByteTemp.get(18), activeByteTemp.get(19)) / 32768.0 * 180;

//                SetData("AccX", Math.round(acc_x * 1000.0) / 1000.0);
//                SetData("AccY", Math.round(acc_y * 1000.0) / 1000.0);
//                SetData("AccZ", Math.round(acc_z * 1000.0) / 1000.0);
//                SetData("AsX", Math.round(as_x * 1000.0) / 1000.0);
//                SetData("AsY", Math.round(as_y * 1000.0) / 1000.0);
//                SetData("AsZ", Math.round(as_z * 1000.0) / 1000.0);
//                SetData("AngX", Math.round(angle_x * 100.0) / 100.0);
//                SetData("AngY", Math.round(angle_y * 100.0) / 100.0);
//                SetData("AngZ", Math.round(angle_z * 100.0) / 100.0);

                // 传感器数据回调
                // deviceManager.OnReceiveDevice(deviceName, GetDataDisplayLine());
            }
            else if(activeByteTemp.get(0) == 0x55 && activeByteTemp.get(1) == 0x71){
                // 磁场
                if(activeByteTemp.get(2) == 58){
                    res.h_x = DeviceModel.GetShortData(activeByteTemp.get(4), activeByteTemp.get(5)) / 120.0;
                    res.h_y = DeviceModel.GetShortData(activeByteTemp.get(6), activeByteTemp.get(7)) / 120.0;
                    res.h_z = DeviceModel.GetShortData(activeByteTemp.get(8), activeByteTemp.get(9)) / 120.0;
//                    SetData("HX", Math.round(h_x * 1000.0) / 1000.0);
//                    SetData("HY", Math.round(h_y * 1000.0) / 1000.0);
//                    SetData("HZ", Math.round(h_z * 1000.0) / 1000.0);
                }
                // 电量
                if(activeByteTemp.get(2) == 100){
                    res.e = DeviceModel.GetShortData(activeByteTemp.get(4), activeByteTemp.get(5)) / 100.0;
                    // SetData("Electricity", GetBatteryPercent(e));
                }
            }
        }

        return res;
    }
}


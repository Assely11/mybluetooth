package com.example.mybluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.UUID;

public class LinearLayoutClass {
    public LinearLayout mainLayout;
    private ImageView iconView;
    private TextView nameView;
    private LinearLayout childLayout;
    private TextView macView;
    private LinearLayout cChildLayout;
    private TextView bStateView;
    private TextView cStateView;
    private TextView qualityView;
    public TextView button;
    public BluetoothDevice mBluetoothDevice;
    public int Index;
    public List<UUID> serviceUUid=null;

    public LinearLayoutClass(Context context,BluetoothDevice bluetoothDevice,int i){
        this.mBluetoothDevice=bluetoothDevice;

        mainLayout = new LinearLayout(context);
        iconView = new ImageView(context);
        nameView = new TextView(context);
        childLayout = new LinearLayout(context);
        macView = new TextView(context);
        cChildLayout = new LinearLayout(context);
        bStateView = new TextView(context);
        cStateView = new TextView(context);
        qualityView = new TextView(context);
        button = new TextView(context);

        //set the child data
        setData2(bluetoothDevice,i);
        //set style
        setMainLayout(context);
    }
    public LinearLayoutClass(Context context, BluetoothDevice bluetoothDevice, Intent intent) {
        this.mBluetoothDevice=bluetoothDevice;

        mainLayout = new LinearLayout(context);
        iconView = new ImageView(context);
        nameView = new TextView(context);
        childLayout = new LinearLayout(context);
        macView = new TextView(context);
        cChildLayout = new LinearLayout(context);
        bStateView = new TextView(context);
        cStateView = new TextView(context);
        qualityView = new TextView(context);
        button = new TextView(context);
        //set the child data
        setData(bluetoothDevice,intent,context);
        //set style
        setMainLayout(context);
    }
    @SuppressLint("MissingPermission")
    private void setData2(BluetoothDevice bluetoothDevice, int i){
        if(bluetoothDevice.getBondState()==BluetoothDevice.BOND_BONDED){
            this.bStateView.setText("配对");
        }else{
            this.bStateView.setText("未配对");
        }
        this.nameView.setText(bluetoothDevice.getName());
        if(bluetoothDevice.getName()==null){
            this.nameView.setText("N/A");
        }
        this.macView.setText(bluetoothDevice.getAddress());
        switch (bluetoothDevice.getType()){
            case BluetoothDevice.DEVICE_TYPE_LE:
                this.cStateView.setText("LE");
                break;
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                this.cStateView.setText("CLASSIC");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                this.cStateView.setText("DUAL");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                this.cStateView.setText("UNKNOWN");
                break;
            default:
                this.cStateView.setText("UNKNOWN");
                break;
        }
        //int dbm=intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
        this.qualityView.setText(i+"dBm");
    }
    private void setMainLayout(Context context){
        //set the child style
        setIconView();
        setButton();
        setNameView();
        setOtherText();
        setCChildLayout(context);
        setChildLayout();

        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mainLayout.setLayoutParams(layoutParams);
        this.mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setGravity(Gravity.CENTER);

        this.mainLayout.addView(this.iconView);
        this.mainLayout.addView(this.childLayout);
        this.mainLayout.addView(this.qualityView);
        this.mainLayout.addView(this.button);
    }
    private void setData(BluetoothDevice bluetoothDevice,Intent intent,Context context){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
        if(bluetoothDevice.getBondState()==BluetoothDevice.BOND_BONDED){
            this.bStateView.setText("配对");
        }else{
            this.bStateView.setText("未配对");
        }
        this.nameView.setText(bluetoothDevice.getName());
        if(bluetoothDevice.getName()==null){
            this.nameView.setText("N/A");
        }
        this.macView.setText(bluetoothDevice.getAddress());
        switch (bluetoothDevice.getType()){
            case BluetoothDevice.DEVICE_TYPE_LE:
                this.cStateView.setText("LE");
                break;
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                this.cStateView.setText("CLASSIC");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                this.cStateView.setText("DUAL");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                this.cStateView.setText("UNKNOWN");
                break;
            default:
                this.cStateView.setText("UNKNOWN");
                break;
        }
        int dbm=intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
        this.qualityView.setText(dbm+"dBm");
    }
    private void setChildLayout(){
        this.childLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(450, 180);
        layoutParams.setMargins(5,5,10,5);
        this.childLayout.setLayoutParams(layoutParams);
        this.childLayout.addView(this.nameView);
        this.childLayout.addView(this.macView);
        this.childLayout.addView(this.cChildLayout);
        //return layoutParams;
    }
    private void setCChildLayout(Context context){
        LinearLayout left=new LinearLayout(context);
        LinearLayout right=new LinearLayout(context);

        left.setGravity(Gravity.LEFT);
        right.setGravity(Gravity.RIGHT);

        left.addView(this.bStateView);
        right.addView(this.cStateView);

        LinearLayout.LayoutParams leftLayout=new LinearLayout.LayoutParams(140, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftLayout.setMargins(0,5,0,5);
        LinearLayout.LayoutParams rightLayout=new LinearLayout.LayoutParams(160, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightLayout.setMargins(0,5,0,5);

        //LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.WRAP_CONTENT);
        //this.cChildLayout.setLayoutParams(layoutParams);
        this.cChildLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.cChildLayout.addView(left,leftLayout);
        this.cChildLayout.addView(right,rightLayout);
    }
    private void setOtherText(){
        ViewGroup.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.macView.setTextSize(12);
        this.macView.setGravity(Gravity.LEFT);
        this.macView.setLayoutParams(layoutParams);
        this.cStateView.setTextSize(10);
        this.cStateView.setGravity(Gravity.RIGHT);
        this.cStateView.setLayoutParams(layoutParams);
        this.bStateView.setTextSize(10);
        this.bStateView.setGravity(Gravity.LEFT);
        this.bStateView.setLayoutParams(layoutParams);
        this.qualityView.setTextSize(12);
        this.qualityView.setGravity(Gravity.CENTER);
    }
    private void setNameView(){
        this.nameView.setTextColor(Color.rgb(0,0,0));
        this.nameView.setTextSize(13);
        this.nameView.getPaint().setFakeBoldText(true);
        ViewGroup.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.nameView.setLayoutParams(layoutParams);
    }
    private void setButton(){
        this.button.setBackgroundResource(R.drawable.button_shape);
        this.button.setText("连接");
        this.button.setTextColor(Color.rgb(255,255,255));
        this.button.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(45,10,10,10);
        this.button.setLayoutParams(layoutParams);
    }
    private void setIconView(){
       this.iconView.setImageResource(R.drawable.icon);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(140,140);
        params.setMargins(20,10,20,10);
        this.iconView.setLayoutParams(params);
        //return params;
    }
}

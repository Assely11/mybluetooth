package com.example.mybluetooth;

public class DataType {
    public double acc_x,acc_y,acc_z;
    public double as_x,as_y,as_z;
    public double angle_x,angle_y,angle_z;
    public double h_x,h_y,h_z;
    public double e;

    public double gyr_x,gyr_y,gyr_z;
    public double mag_x,mag_y,mag_z;

    public DataType(){
        acc_x=0;acc_y=0;acc_z=0;
        as_x=0;as_y=0;as_z=0;
        angle_x=0;angle_y=0;angle_z=0;
        h_x=0;h_y=0;h_z=0;
        e=0;

        gyr_x=0;gyr_y=0;gyr_z=0;
        mag_x=0;mag_y=0;mag_z=0;
    }
}

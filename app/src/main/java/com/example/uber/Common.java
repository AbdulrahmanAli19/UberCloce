package com.example.uber;

import com.example.uber.Model.DriverInfoModel;

public class Common {
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String DRIVER_LOCATION_REFERENCE = "DriversLocation";

    public static DriverInfoModel currentUser;

    public static String buildWelocmeMessage() {
        if (currentUser != null)
            return new StringBuilder("Welocme ")
                    .append(currentUser.getFistName())
                    .append(" ")
                    .append(currentUser.getLastName())
                    .toString();
        else
            return "";
    }
}

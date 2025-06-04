package com.example.meinkochbuch.core;

import android.graphics.Bitmap;

import lombok.Data;

@Data
public class Weather {

    private String city;
    private double temperature;
    private Bitmap icon;
}

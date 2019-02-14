package com.example.kobayashi_satoru.miroyo;

public class milliSecond {
    public static String toTimeColonFormat(int milliSecond){
        int allSecond = milliSecond / 1000;
        if(allSecond < 10){
            return "00:0" + String.valueOf(allSecond);
        }else if(allSecond < 60){
            return "00:" + String.valueOf(allSecond);
        }else if(allSecond < 600){
            int second = allSecond % 60;
            int minute = allSecond / 60;
            if(second < 10){
                return "0" + String.valueOf(minute) + ":" + "0" + String.valueOf(second);
            }else{
                return "0" + String.valueOf(minute) + ":" + String.valueOf(second);
            }
        }else{
            int second = allSecond % 60;
            int minute = allSecond / 60;
            if(second < 10){
                return String.valueOf(minute) + ":" + "0" + String.valueOf(second);
            }else{
                return String.valueOf(minute) + ":" + String.valueOf(second);
            }
        }
    }
}

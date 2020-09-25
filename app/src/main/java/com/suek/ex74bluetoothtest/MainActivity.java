package com.suek.ex74bluetoothtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String permission= Manifest.permission.ACCESS_FINE_LOCATION;
        //Location permission 에 대한 동적퍼미션 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{permission}, 10);   //여기서 alert dialog 를 만들어서 띄어줌
            }
        }
    }

    //위의 다이얼로그에서 뭔가 선택했을때 : onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 10:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED ){   //뜬 다이얼로그를 거부했을때
                    Toast.makeText(this, "클라이언트로서 새로운 장치를 검색하는 기능이 제한됩니다.\n기존에 페어링된 장치는 접속가능합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void clickServer(View view) {
        Intent intent= new Intent(this, ServerActivity.class);    //명시적 인텐트
        startActivity(intent);
    }

    public void clickClient(View view) {
        Intent intent= new Intent(this, ClientActivity.class);    //명시적 인텐트
        startActivity(intent);
    }
}

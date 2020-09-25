package com.suek.ex74bluetoothtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ClientActivity extends AppCompatActivity {

    static final UUID BT_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //UUID는 있고, 그 다음 블루투스 Mac 주소(IP번호)가 필요함-> 이것은 탐색을 통해 BTList 에서 list 로 보여줌

    TextView tv;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;

    DataInputStream dis;
    DataOutputStream dos;

    ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        getSupportActionBar().setTitle("CLIENT");
        tv= findViewById(R.id.tv);

        //블루투스 관리자 객체 소환
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Toast.makeText(this, "이 기기에는 불루투스가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //블루투스가 켜져있는지
        if(bluetoothAdapter.isEnabled()){
            //켜져있으면 서버 불루투스 장치 탐색 및 그 탐색된 결과를 리스트로 보여주는 화면(activity- 원래 이런화면은 없는 화면이라 만들어야함) 실행
            discoveryBluetoothDevices();   //블루투스가 켜져있으면 찾아라
        }else {
            //블루투스 장치 On 하는 화면 실행
            Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 10);
        }

    }//onCreate()



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 10:
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "사용불가", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    //불루투스가 켜있으면, 서버 블루투스 장치 탐색 및 리스트 보는 화면 실행
                    discoveryBluetoothDevices();
                }
                break;

            case 20:
                if(resultCode == RESULT_OK){
                    //선택된 BT 디바이스의 Mac 주소 얻어오기

                    String address= data.getStringExtra("Address");

                    //선택된 mac 주소를 이용해서 Socket 생성
                    //통신작업은 별도 스레드가..
                    clientThread= new ClientThread(address);
                    clientThread.start();  //run 메소드 발동
                }
                break;
        }
    }

    //블루투스 장치 탐색화면(activity) 실행메소드
    void discoveryBluetoothDevices(){
        Intent intent= new Intent(this, BTListActivity.class);   //명시적 인텐트(명시적으로 클래스 이름을 씀)
        startActivityForResult(intent, 20);
    }

    public void clickClient(View view) {
        new Thread(){
            @Override
            public void run() {
                try {
                    dos.writeUTF("sdjf");
                    dos.writeInt(50);
                    dos.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //inner class
    class ClientThread extends Thread{

        String address;

        public ClientThread(String address) {
            this.address = address;
        }

        @Override
        public void run() {

            // mac 주소에 해당하는 BluetoothDevice 객체를 얻어오기
            BluetoothDevice device= bluetoothAdapter.getRemoteDevice(address);

            //원격디바이스와 소켓연결 작업 수행
            try {
                socket= device.createInsecureRfcommSocketToServiceRecord(BT_UUID);   //통신은 try & catch 문 해줘야함
                socket.connect();   //연결시도!  연결이 되면 try, 연결이 안되면 catch 문으로 빠짐

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.append("서버와 연결되었습니다.");
                    }
                });

                //접속된 Socket 을 통해 데이터를 주고받는 무지개롣 만들기
                dis= new DataInputStream(socket.getInputStream());
                dos= new DataOutputStream(socket.getOutputStream());

                //스트림을 통해 원하는 데이터 주고받기
                dos.writeUTF("안녕하세요.");   //UTF: 한글도 가능한 문자열 인코딩방식
                dos.writeInt(50);
                dos.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

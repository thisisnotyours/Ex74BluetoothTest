package com.suek.ex74bluetoothtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

public class ServerActivity extends AppCompatActivity {

    //블루투스 하드웨어 장치에 대한 식별자 UUID (제품의 식별변호 같은것)
    static final UUID BT_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView tv;

    BluetoothAdapter bluetoothAdapter;

    BluetoothServerSocket serverSocket;
    BluetoothSocket socket;

    //데이터를 주고받기 위한 스트림(자료형단위로 보낼 수 있는 Stream)
    DataInputStream dis;
    DataOutputStream dos;

    ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //제목줄 글씨변경
        getSupportActionBar().setTitle("SERVER");

        tv= findViewById(R.id.tv);

        //블루투스 관리자객체 소환 (블루투스는 매니저대신에 아답터라는 이름을 사용)
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){   //블루투스가 없으면 null 이면
            Toast.makeText(this, "이 기기에는 블루투스가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();   //finish 를 한다고 바로 이곳에서 액티비티가 종료되지 않음. 그래서 아래에 종료작없을 하는 return 을 써줘야 onCreate() 작업이 끝남
            return;
        }

        //위에서 return 되지 않았다면 불루투스가 있다는 의미.
        //블루투스 장치가 켜져있는지 체크 및 On 하도록 요청해야함!  (꺼저있으면 키라고 요청해야함)
        if(bluetoothAdapter.isEnabled()){
            //블루투스가 켜져있으면-> 서버소켓 생성작업 실행
            createServerSocket();   //블루투스가 켜져있으면 밑에있는 메소드 createServerSocket(); 실행
        }else {    //블루투스가 켜져있지않으면
            //블루투스 장치를 ON 선택하도록 하는 화면(액티비티- 다이얼로그 테마스타일)전환
            Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);   //this. 이런식으로 안하면 묵시적 인텐트
            startActivityForResult(intent, 100);
        }

    }//onCreate()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 100:
                if(resultCode == RESULT_CANCELED){   //블루투스를 껐다면
                    Toast.makeText(this, "블루투스를 허용하지 않았습니다.\n앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }else {   //블루투스를 허용했다면
                    //서버소켓 생성 작업
                    createServerSocket();
                    Toast.makeText(this, "블루투스를 허용했습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case 200:
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "블루투스 탐색을 허용하지 않았습니다.\n다른장치에서 이 장치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    //서버소켓 생성작업 메소드  (이렇게 따로 메소드를 만들어놓으면 이쪽 저쪽에서 사용가능- 내가 그냥 만든 이름)
    void createServerSocket(){
        //통신작업은 반드시 별도의 Thread 가 해야함.
        serverThread= new ServerThread();
        serverThread.start();   //스타트하면 자동으로 밑에있는 run() 메소드가 발동

        //이 기기를 다른 장치에서 검색할 수 있도록 허용하는 화면(Activity)실행
        Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);   //300초간 검색 허용
        startActivityForResult(intent, 200);
    }

    //서버소켓 작업 및 통신을 하는 별도의 Tread 클래스 : inner class
    class ServerThread extends Thread{
        @Override
        public void run() {
            //서버소켓 생성
            try {
                serverSocket= bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("SERVER", BT_UUID);  //"SERVER"-식별자/  UUID(포트번호역할)-블루투스장치(하드웨어)의 ID,식별번호.(제품의 식별변호 같은것)
                setUI("서버소켓이 생성되었습니다.\n");

                //클라리언트의 접속을 기다리기
                socket= serverSocket.accept();   //소켓연결
                setUI("클라이언트가 접속했습니다.\n");

                //접속된 Socket 을 이용하여 통신하기위해
                //무지개로드 만들기
                dis= new DataInputStream(socket.getInputStream());
                dos= new DataOutputStream(socket.getOutputStream());

                //스트림을 통해 원하는 데이터 전송하거나 받기
                String msg= dis.readUTF();    //input 데이터받기
                int num= dis.readInt();

                setUI("클라이언트 : " + msg + " ~~ " + num);

                dis.close();


            } catch (IOException e) {
                e.printStackTrace();
            }



        }//run method


        // UI Thread 로 메세지 출력하는 기능
        void setUI(final String msg){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.append(msg);    //글씨를 덧붙이다
                }
            });
        }

    }//ServerThread class


}

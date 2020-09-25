package com.suek.ex74bluetoothtest;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

//ArrayList= collection framework, and set, map.... 다시보기
public class BTListActivity extends AppCompatActivity {

    ArrayList<String> deviceList= new ArrayList<>();  //대량의 데이터
    ListView listView;
    ArrayAdapter adapter;

    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> devices;

    DiscoveryResultReceiver discoveryResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_t_list);

        listView= findViewById(R.id.listview);
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(adapter);

        //블루투스 아답터 소환
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        //이미 페어링 되어있는 디바이스들을 리스트에 추가
        devices= bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : devices){
            String name= device.getName();
            String address= device.getAddress();

            deviceList.add( name + "\n" + address );
        }
        //새로운 장치를 찾은 결과를 운영체제에서 BroadCast 를 함.
        //그러므로 이를 들으려면 Broadcast Receiver 가 필요함
        //Bluetooth 의 장치검색 결과는 동적(Java 언어에서 등록한) Receiver 만 가능함.
        discoveryResultReceiver= new DiscoveryResultReceiver();
        IntentFilter filter= new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);     //ex; kbs 수신 - 수신하려고 필터 -ACTION_FOUND: 장치를 찾는 필터
        registerReceiver(discoveryResultReceiver, filter);  //Manifest.xml 말고 자바에서 리시버 등록

        //탐색이 종료되었다는 방송듣는 필터
        IntentFilter filter2= new IntentFilter();
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryResultReceiver, filter2);

        //탐색 시작!!
        bluetoothAdapter.startDiscovery();  //startDiscovery()를 하면 핸드폰이 블루투스를 탐색/찾기시작.. -> 결과를 DiscoveryResultReceiver 가 받음


        //다이얼로그 스타일 일때..
        //아웃사이드를 터치했을때 cancel 되지 않도록.
        setFinishOnTouchOutside(false);

        //리스트뷰에서 원하는 디바이스를 선택했을때
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   //position: 리스트[0]의 0,1,2...번

                //더이상 탐색하지 않도록..
                bluetoothAdapter.cancelDiscovery();

                String s= deviceList.get(position);
                // s 문자열에 저장된 name 과 mac 주소중에 주소만 분리
                String[] ss= s.split("\n");   //이름과 주소가 분리됨 ex;
                String address= ss[1];  //두번째 것- 다시말해 주소

                //얻어온 address 를 이 액티비티를 실행했던
                //ClientActivity 에 전달
                //이 액티비티를 실행했던 택배기사 Intent 객체를 소환!
                Intent intent= getIntent();
                //이 택배기사에게 가지고 돌아갈 데이터를 추가
                intent.putExtra("Address", address);   //"Address": 소포의 라벨지/이름

                //이게 이 액티비티의 결과다..!
                setResult(RESULT_OK, intent);    //intent 가 결과(OK된 결과)를 가지고 돌아감

                finish();

            }
        });


    }//onCreate

    //inner class (이너클래스를 만들면 아웃터의 내것인양 쓸수있음)
    class  DiscoveryResultReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {  //방송을 수신하면 onReceive 가 발동

            String action= intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND)){   //ACTION_FOUND: ex)kbs 찾기
                //장치를 찾은 상황- 찾은 것 마다마다 Arraylist(기차열) 에 넣음
                BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    //intent 가 전달하는 데이터 ->getExtra
                deviceList.add(device.getName()+"\n"+device.getAddress());
                adapter.notifyDataSetChanged();
                /*//기존에있던 장치들을 중복되지 않게 가지고 있는 Set 객체
                boolean isAdd= devices.add(device);   //중복된 디바이스가 없다면 true 리턴
                if(isAdd){   //새로운 장치라는 것임
                    String name= device.getName();
                    String address= device.getAddress();

                    //리스트 뷰에 보여줄 데이터에 추가
                    deviceList.add(name+"\n"+address);
                }*/
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Toast.makeText(context, "블루투스 탐색을 완료했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    //액티비티가 화면에서 안보일때 리시버 등록 해제
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(discoveryResultReceiver);
    }
}//BTListActivity class

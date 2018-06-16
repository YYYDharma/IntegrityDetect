package com.example.yhj.integrityapp;

import android.Manifest;
import android.content.PeriodicSync;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public static final int PERMISSION_LOCATION=1;//定位权限请求
    private LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;//地图总控制器
    private boolean isFirstLocate=true;//第一次定位
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());//注册定位监听器，获取到定位信息时进行回调
        SDKInitializer.initialize(getApplicationContext());   //地图界面初始化
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//显示标题栏
        mapView=(MapView)findViewById(R.id.map_view);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//开启显示我的位置功能

        //权限获取
        getPermissions();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * 请求权限
     */
    public void getPermissions(){
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions, PERMISSION_LOCATION);
        }else
            requestLocation();//权限允许，进行定位
    }

    /**
     * 通过权限请求开启定位
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_LOCATION:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            //用户未同意权限时
                            Toast.makeText(MainActivity.this,"必须同意权限才能使用定位",Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    }
                    requestLocation();//开启定位
                }
                else {
                    Toast.makeText(MainActivity.this,"必发生未知错误",Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * 启动定位
     */
    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    /**
     * 初始化定位信息
     */
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);//设置定位信息更新时间间隔
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        mLocationClient.setLocOption(option);
    }

    /**
     * 定位到当前位置,并持续显示我的位置
     * @param bdLocation
     */
    private void navigateTo(BDLocation bdLocation){
        if(isFirstLocate){
            //第一次定位，定位到当前位置
            LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());//存放经纬度
           MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(latLng);
           baiduMap.animateMapStatus(update);
           update=MapStatusUpdateFactory.zoomTo(16f);//地图为16级
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        //显示我的位置
        MyLocationData.Builder builder=new MyLocationData.Builder();//封装当前设备的位置
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData=builder.build();
        baiduMap.setMyLocationData(myLocationData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();//停止定位
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    /**
     * 定位监听器对象
     */
    public class MyLocationListener extends BDAbstractLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            navigateTo(bdLocation);
        }
    }


    /**
     * 菜单栏
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

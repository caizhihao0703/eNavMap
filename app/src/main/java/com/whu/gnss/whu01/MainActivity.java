package com.whu.gnss.whu01;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.wms.WMSTileSource;


public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //虚拟机调试
//        mapView = findViewById(R.id.osmMapView);
//        mapView.setMultiTouchControls(true);
//        WMSTileSource wmsTileSource = new WMSTileSource( "OGC:WMS", new String[]{"http://202.114.122.22:2107/geoserver/whuXX/wms?service=WMS"},"whuXX:basemap","1.1.1","EPSG:900913","",256 );
//        mapView.setTileSource(wmsTileSource);
//        mapView.getController().setCenter(new GeoPoint(30.538,114.3618));
//        mapView.setMinZoomLevel(15.0);
//        mapView.setMaxZoomLevel(20.0);
//        mapView.getController().setZoom(15.0);


        // 华为手机调试
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.osmMapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setCenter(new GeoPoint(30.538, 114.3618));
        mapView.getController().setZoom(15.0);

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

}
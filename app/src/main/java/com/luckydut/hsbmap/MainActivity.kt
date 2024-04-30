package com.luckydut.hsbmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.LocationTrackingMode

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000 // 권한 요청을 위한 요청 코드
    private lateinit var locationSource: FusedLocationSource // 위치 정보 소스
    private lateinit var naverMap: NaverMap // 네이버 맵 객체
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 프래그먼트 매니저로부터 지도 프래그먼트를 찾거나 새로 생성
        val fm = supportFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit()
        }

        // 널이 아니면 지도가 준비될 때 onMapReady 콜백을 통해 네이버 맵 객체 얻기
        mapFragment?.getMapAsync(this)

        // FusedLocationSource를 초기화 (위치 정보와 관련된 객체)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onMapReady(naverMap: NaverMap) {
        // NaverMap 객체가 준비되면 여기에서 지도를 설정합니다.
        // 예를 들어, 지도의 초기 위치를 설정하거나, 줌 레벨을 조정할 수 있습니다.
        this.naverMap = naverMap
        naverMap.locationSource = locationSource // 위치 정보 소스 설정
        naverMap.uiSettings.isLocationButtonEnabled = true // 위치 버튼 활성화
        naverMap.locationTrackingMode = LocationTrackingMode.Follow // 위치 추적 모드 설정

        /*// 맵 타입 설정을 기본으로 설정
        naverMap.mapType = NaverMap.MapType.Basic
        // 자전거 레이어 활성화
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true)*/

        checkPermission() // 위치 서비스 권한 확인
    }
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                // 권한이 거부되었을 때의 처리
            }
        }
    }
}
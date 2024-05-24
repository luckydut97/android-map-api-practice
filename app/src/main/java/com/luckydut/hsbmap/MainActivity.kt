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

import android.location.Geocoder
import android.util.Log
import java.util.Locale
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.overlay.OverlayImage
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000 // 권한 요청을 위한 요청 코드
    private lateinit var locationSource: FusedLocationSource // 위치 정보 소스
    private lateinit var naverMap: NaverMap // 네이버 맵 객체
    private val marker = Marker() // 마커로 주소 및 위경도 표시하기- 마커 인스턴스를 클래스 레벨로 선언
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

    //지도 초기화 및 설정
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

        /*// 마커로 주소 및 위경도 표시하기-지도 클릭 이벤트 처리
        naverMap.setOnMapClickListener { _, coord ->
            // 마커 위치 설정 및 지도에 표시
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap

            // 클릭된 위치의 주소를 획득하고 토스트 메시지로 보여줌
            getAddress(coord.latitude, coord.longitude)
        }*/

        // 서울의 구별로 마커 추가
        addSeoulMarkers()


        // 자전거 레이어 활성화
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, true)
        /*// 마커들 간의 경로를 Polyline으로 연결
        connectMarkersWithPolyline()*/

        checkPermission() // 위치 서비스 권한 확인
    }

    //마커로 주소 및 위경도 표시하기
    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        // Geocoder를 통해 위도와 경도로부터 주소를 조회합니다.
        val addresses = try {
            geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        // 결과로 받은 주소 정보가 null이 아니고, 하나 이상의 주소가 있다면 그 중 첫 번째 주소 정보를 사용합니다.
        val addressInfo = if (addresses != null && addresses.isNotEmpty()) addresses[0].getAddressLine(0) else "주소를 찾을 수 없습니다."

        // 위도, 경도와 주소 정보를 포함한 토스트 메시지를 사용자에게 보여줍니다.
        val message = "위도: $latitude\n경도: $longitude\n주소: $addressInfo"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // 위,경도별 마커 추가
    private fun addSeoulMarkers() {
        val seoulDistricts = listOf(
            Pair("CakeByCase 가맹점", LatLng(37.5172363, 127.0473248)),
            Pair("CakeByCase 가맹점", LatLng(37.5729503, 126.9793579)),
            Pair("CakeByCase 서초연구소", LatLng(37.4837121, 127.0324117)),
            Pair("CakeByCase 가맹점", LatLng(37.566324, 126.901636)),
            Pair("CakeByCase 가맹점", LatLng(37.532600, 126.990341)),
            Pair("CakeByCase 흑석 사옥", LatLng(37.50852948615519, 126.9610305386155))
        )

        for (district in seoulDistricts) {
            val marker = Marker()
            marker.position = district.second
            marker.map = naverMap
            marker.captionText = district.first

            // 마커 아이콘 설정
            marker.icon = OverlayImage.fromResource(R.drawable.cake)

            marker.setOnClickListener { overlay ->
                val marker = overlay as Marker
                val infoWindow = marker.infoWindow
                if (infoWindow != null) {
                    infoWindow.close()
                } else {
                    showBottomSheetDialog(marker.captionText)
                }
                true
            }
        }
    }

    /*// 마커들을 연결하는 Polyline 추가
    private fun connectMarkersWithPolyline() {
        val seoulDistrictsCoordinates = listOf(
            LatLng(37.5172363, 127.0473248),
            LatLng(37.5729503, 126.9793579),
            LatLng(37.4837121, 127.0324117),
            LatLng(37.566324, 126.901636),
            LatLng(37.532600, 126.990341),
            LatLng(37.50852948615519, 126.9610305386155)
        )

        val polyline = PolylineOverlay()
        polyline.coords = seoulDistrictsCoordinates
        polyline.map = naverMap
    }*/

    // 팝업창을 표시하는 함수
    private fun showBottomSheetDialog(districtName: String) {
        Log.d("ShowBottomSheet", "Showing bottom sheet for $districtName")
        val bottomSheetFragment = CustomBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putString("district_name", districtName)
            }
        }
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    //위치 권한 요청 및 처리
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
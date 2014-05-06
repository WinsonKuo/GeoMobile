package com.geomobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapFragment;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends FragmentActivity implements LocationListener {

	
	
	LocationManager mLocationManager = null;
	
    private GoogleMap map;
    PolylineOptions polylineOpt = new PolylineOptions();
    
    public Marker marker, last_marker;
    
    public boolean show_first_gps_status = true;
    
    private View infoWindow;
    
    ProgressDialog psDialog;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		new LoadingDataAsyncTask().execute("http://www.tasty.com.tw/store.aspx");
				
		// 依指定layout檔，建立地標訊息視窗View物件
		infoWindow = this.getLayoutInflater().inflate(R.layout.my_infowindow, null);
		
		//map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    //Marker nkut = map.addMarker(new MarkerOptions().position(NKUT).title("南開科技大學").snippet("數位生活創意系"));
		//map.moveCamera(CameraUpdateFactory.newLatLngZoom(NKUT, 16));
		
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
    	Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	updateDisplayedInfo(lastLocation,false);
    	LatLng curLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    map.setInfoWindowAdapter(new MyInfoWindowAdapter());
		//map.clear();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 16));

	}

    public void onResume() {
    	super.onResume();
    	Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	updateDisplayedInfo(lastLocation,false);
    	LatLng curLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		//map.clear();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 16));
    	
    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	
    	
    }

    public void onPause() {
    	super.onPause();
    	mLocationManager.removeUpdates(this);
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		updateDisplayedInfo(location,true);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
    public void updateDisplayedInfo(Location location, boolean updateMarker) {
    	Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);
    	TextView lat_value = (TextView)findViewById(R.id.textView3);
    	TextView lon_value = (TextView)findViewById(R.id.textView4);
    	TextView addr = (TextView)findViewById(R.id.textView5);
    	
    	lat_value.setText(Double.toString(location.getLatitude()));
    	lon_value.setText(Double.toString(location.getLongitude()));
    	
    	StringBuffer addressString = new StringBuffer();
    	
		try {
			List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(!addressList.isEmpty()){
				Address address = addressList.get(0);
				String line = null;
				for (int i = 0; (line = address.getAddressLine(i)) != null; i++){
					addressString.append(line+"\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String newAddr = addressString.toString();
		newAddr = newAddr.replaceAll("\n", "");
	    String patternStr = "(^\\d+)(.*)";
	    Pattern pattern = Pattern.compile(patternStr);
	    Matcher matcher = pattern.matcher(newAddr);
	    while (matcher.find()) {
	      newAddr = matcher.group(2);
	    }
	    
    	LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		//map.clear();
		if(updateMarker) {
			marker.remove();
			marker = map.addMarker(new MarkerOptions()
					.position(curLatLng)
					.title("目前地址")
					.snippet(newAddr));
					//.draggable(true)
					//.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue))); //修改Marker圖示
					//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); //修改預設圖示顏色
			if(show_first_gps_status) {
				Toast.makeText(this, "衛星定位完成", Toast.LENGTH_SHORT).show();
				show_first_gps_status = false;
			}
		}
		
		//marker.showInfoWindow();
		if(!updateMarker) {
			marker = map.addMarker(new MarkerOptions()
					.position(curLatLng)
					.title("目前地址")
					.snippet(newAddr));
					//.draggable(true)
					//.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue))); //修改Marker圖示
					//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); //修改預設圖示顏色
			marker.remove();
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 12));
			addr.setText("衛星定位中…");
		}
	    
		
	    if(updateMarker) {
	    	polylineOpt.add(curLatLng);
	    	polylineOpt.color(Color.BLUE);
	    	Polyline polyline = map.addPolyline(polylineOpt);
	    	polyline.setWidth(10);
	    	addr.setText(newAddr);
	    }
		
    }
    
    class MyInfoWindowAdapter implements InfoWindowAdapter {
    	
    	@Override
    	public View getInfoContents(Marker marker) {
    		// TODO Auto-generated method stub
    		//return null;
    		displayView(marker);
            return infoWindow;
    	}

    	@Override
    	public View getInfoWindow(Marker marker) {
    		// TODO Auto-generated method stub
    	    return null;
    	    //displayView(marker);
            //return infoWindow;
    	}

        public void displayView(Marker arg0) {
        	// 顯示地標title
            TextView title = ((TextView)infoWindow.findViewById(R.id.txtTitle));
            title.setText(arg0.getTitle());
            // 顯示地標snippet
            TextView snippet = ((TextView)infoWindow.findViewById(R.id.txtSnippet));
            snippet.setText(arg0.getSnippet());

        }
    }
    
    class LoadingDataAsyncTask extends AsyncTask<String, Integer, AddressResult>{
    	
        ProgressDialog psDialog;
        private GoogleMap map;
        
		@Override
        protected AddressResult doInBackground(String... urls) {
            AddressResult addresult = new AddressResult();
            String result = "";
            String uriAPI = urls[0];
            HttpGet httpRequest = new HttpGet(uriAPI);
            DefaultHttpClient client = new DefaultHttpClient();
            try {
                HttpResponse response = client.execute(httpRequest);
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = toUTF8(resEntity.getContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.getConnectionManager().shutdown();
            }
            
            try {
                HtmlCleaner cleaner = new HtmlCleaner();
                TagNode node = cleaner.clean(result);
                
                Object [] ns_title = node.evaluateXPath("//body/form/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td/a[@class='R_title']");
                Object [] ns_addr = node.evaluateXPath("//body/form/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td[3][@class='store_line']");
                Object [] ns_tel = node.evaluateXPath("//body/form/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr/td[4][@class='store_line']");
            	
            	for(Object element : ns_title) {	
            		TagNode n = (TagNode) element;
                	addresult.setTitle(n.getText().toString());
                }
                
                for(Object element : ns_addr) {
                	TagNode n = (TagNode) element;
                	if(n.getAllChildren().size() > 1) {
                		addresult.setAddr(n.getAllChildren().get(0).toString());
                	} else {
                		addresult.setAddr(n.getText().toString());
                	}
                	
                }
                
                for(Object element : ns_tel) {
                	TagNode n = (TagNode) element;
                    addresult.setTel(n.getText().toString());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            return addresult;
        }
        
        @Override
        protected void onPostExecute(AddressResult addresult) {
            super.onPostExecute(addresult);
            ArrayList<String> title = addresult.getTitle();
            ArrayList<String> address = addresult.getAddr();
            ArrayList<String> tel = addresult.getTel();
            ArrayList<Double> latitude = addresult.getLatitude();
            ArrayList<Double> longitude = addresult.getLongitude();
            
            Object[] titleArr = title.toArray();
            Object[] addressArr = address.toArray();
            Object[] telArr = tel.toArray();
            Object[] latitudeArr = latitude.toArray();
            Object[] longitudeArr = longitude.toArray();
            
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    		//map.clear();
    		for(int i = 0; i < titleArr.length; i++) {
    	    	LatLng curLatLng = new LatLng((Double)latitudeArr[i], (Double)longitudeArr[i]);
    	    	String content = "地址："+addressArr[i]+"\n電話："+telArr[i];
    			Marker cur_location = map.addMarker(new MarkerOptions()
				.position(curLatLng)
				.title(titleArr[i].toString())
				.snippet(content)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue))); //修改Marker圖示
    			cur_location.isVisible();
    			//map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 8));
    		}
            psDialog.dismiss();
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            psDialog = ProgressDialog.show(MainActivity.this, "西堤牛排", "資料載入中，請稍候...");
        }
        
        private String toUTF8(InputStream is){
            //InputStream is = resEntity.getContent();
            InputStreamReader isr = null;
            StringBuffer buffer = new StringBuffer();
            try {
                isr = new InputStreamReader(is, "utf-8" );
                Reader in = new BufferedReader(isr);
                int ch;
                while((ch = in.read()) != -1){
                    buffer.append(( char)ch);
                }
                isr.close();
                is.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return buffer.toString();
        }

    }
    
    class AddressResult {

    	//儲存資料
    	ArrayList<String> title = new ArrayList<String>();
    	ArrayList<String> address = new ArrayList<String>();  
    	ArrayList<String> tel = new ArrayList<String>();
    	ArrayList<Double> latitude = new ArrayList<Double>();
    	ArrayList<Double> longitude = new ArrayList<Double>();
    	
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.TAIWAN);
    	    
    	public AddressResult() {
    		// TODO Auto-generated constructor stub
    	}
    	
    	public ArrayList<String> getTitle() {
    		return title;
    	}
    	
    	public ArrayList<String> getAddr() {
    		return address;
    	}
    	
    	public ArrayList<String> getTel() {
    		return tel;
    	}
    	
    	public ArrayList<Double> getLatitude() {
    		return latitude;
    	}
    	
    	public ArrayList<Double> getLongitude() {
    		return longitude;
    	}
    	
    	public void setTitle(String titleNow) {
    		this.title.add(titleNow);
    	}

    	public void setAddr(String addrNow) {
    		this.address.add(addrNow);
    		try {
	    		List<Address> addressList = geocoder.getFromLocationName(addrNow, 1);
	    		if(!addressList.isEmpty()){
					Address geo_address = addressList.get(0);
					this.latitude.add(geo_address.getLatitude());
					this.longitude.add(geo_address.getLongitude());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	public void setTel(String telNow) {
    		this.tel.add(telNow);
    	}
    	
    	public void setStore(String addrNow) {
    		String patternStr = "[市|縣]"; 
    	    Pattern pattern = Pattern.compile(patternStr);
    	    Matcher matcher = pattern.matcher(addrNow);
    	    if (matcher.find()){
    	    	this.address.add(addrNow);
    	    	try {
    	    		List<Address> addressList = geocoder.getFromLocationName(addrNow, 1);
    	    		if(!addressList.isEmpty()){
    					Address geo_address = addressList.get(0);
    					this.latitude.add(geo_address.getLatitude());
    					this.longitude.add(geo_address.getLongitude());
    				}
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    	    }
    	    String patternStr1 = "\\d+-\\d+-\\d+"; 
    	    Pattern pattern1 = Pattern.compile(patternStr1);
    	    Matcher matcher1 = pattern1.matcher(addrNow);
    	    if (matcher1.find()){
    	    	this.tel.add(addrNow);
    	    }
    	}


    }

    
    
}


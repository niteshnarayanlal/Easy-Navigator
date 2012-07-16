package easy.navigator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.location.Location;
import android.location.LocationManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import android.os.Bundle;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.LinearLayout;


@SuppressLint({ "ParserError", "ParserError" })
public class EasyNavigator extends MapActivity implements OnClickListener, OnInitListener,LocationListener {

	Button searchButton;
	TextView destinationTextView,sourceTextView;
	EditText destinationEditText,sourceEditText;
	MapView mapView;
	View zoomView;
	LocationManager locationManager;
	MapController mapController;
    GeoPoint sourceGeoPoint,destinationGeoPoint;
    double sourceLongitude,destinationLongitude;
    double sourceLatitude,destinationLatitude;
    String destination,source;
  	List<Address> address;
  	Address location;
  	ArrayList<String> placeMarks;
  	private TextToSpeech talker;
  	private int MY_DATA_CHECK_CODE = 0;
 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easy_navigator);
        //Checking if speaking is possible
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        //here we will initialize all the ui components
        init();
		//here we will set our location now
        initMyLocation();
        Log.i("HERE","FINDING DIRECTIONS");
     //   DrawPath(sourceGeoPoint, destinationGeoPoint, Color.GREEN, mapView);

       
    }
  
	

	  public void say(String text2say){
	    	talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);
	    }

		@Override
		public void onInit(int status) {
				
			    for(int i=0;i<placeMarks.size();i++)
			    {
			    final String text=placeMarks.get(i);
				 say(text);
				  try {
					  
					
					  Context context = getApplicationContext();
					  int duration =Toast.LENGTH_LONG;
					  Toast toast = Toast.makeText(context, text, duration);
				      toast.show();		      
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
}

		@Override
		public void onDestroy() {
			if (talker != null) {
				talker.stop();
				talker.shutdown();
			}

			super.onDestroy();
		}
	
	@SuppressWarnings("deprecation")
	void init()
    {
	
        destination=new String();
    	mapView=(MapView)findViewById(R.id.myGMap);
    	searchButton=(Button)findViewById(R.id.searchButton);
    	searchButton.setOnClickListener(this);
    	destinationTextView=(TextView)findViewById(R.id.destinationTextView);
    	destinationEditText=(EditText)findViewById(R.id.destinationEditText);    	
    	sourceTextView=(TextView)findViewById(R.id.sourceTextView);
    	sourceEditText=(EditText)findViewById(R.id.sourceEditText);    	
    	zoomView = mapView.getZoomControls();
    	LinearLayout myzoom = (LinearLayout) findViewById(R.id.myzoom);
		myzoom.addView(zoomView);
		mapView.displayZoomControls(true);
		mapView.setSatellite(true);
	    mapView.setStreetView(true);
		
		
	    
    }
    private void initMyLocation() {
		MyLocationOverlay myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		myLocOverlay.enableCompass();
		mapView.getOverlays().add(myLocOverlay);
		mapController  = mapView.getController();
		
		//Have to be removed while testing on the mobile devices with GPS
		sourceLatitude =28.47;
		sourceLongitude=77.01;
		GeoPoint srcGeoPoint = new GeoPoint((int) (sourceLatitude * 1E6),
				(int) (sourceLongitude * 1E6));
		sourceGeoPoint=srcGeoPoint;
		mapView.getController().animateTo(srcGeoPoint);
		mapView.getController().setZoom(15);

		
		
	  
	}
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.easy_navigator, menu);
        return true;
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
	
	if(v==searchButton)
	{
		destination=destinationEditText.getText().toString();
	    source=sourceEditText.getText().toString();   
	    //HERE WE WILL SHOW THE PATH FROM SOURCE TO DESTINATION
        showPath(source,destination);//will set the directions in placeMarks
        talker = new TextToSpeech(this, this);    
	    findDestinationCoordinates();
	    
	}		
}

private void findDestinationCoordinates()
{
	Geocoder geocoder = new Geocoder(this,Locale.getDefault());
	location=new Address(Locale.getDefault());
    try {
          List<Address> geoResults = geocoder.getFromLocationName(destination, 1);
           while (geoResults.size()==0) 
           {
             geoResults = geocoder.getFromLocationName(destination, 1);
           }
           if (geoResults.size()>0) 
           {
        	 Log.i("more","than one address");
             Address addr = geoResults.get(0);
             destinationLatitude=addr.getLatitude();
             destinationLongitude=addr.getLongitude();
             location.setLatitude(destinationLatitude);
             location.setLongitude(destinationLongitude);
             GeoPoint destGeoPoint = new GeoPoint((int) (destinationLatitude * 1E6),
     				(int) (destinationLongitude * 1E6));
     		 destinationGeoPoint=destGeoPoint;
	         Context context = getApplicationContext();
	         String longitudetext = Double.toString(destinationLongitude);
	         int duration = Toast.LENGTH_SHORT;
	         Toast longitudetoast = Toast.makeText(context, longitudetext, duration);
	         longitudetoast.show();
	         String latitudetext=Double.toString(destinationLatitude);
	         Toast latitudetoast = Toast.makeText(context, latitudetext, duration);
	         latitudetoast.show();
	         mapController.animateTo(destinationGeoPoint);
	         mapController.setZoom(17); 
	         mapView.invalidate();
            }//close if
          
          
	    }//end of try
    catch(Exception e)
    {
    	e.printStackTrace();
    }
   

}


private void showPath(String srcPlace, String destPlace) {

        String urlString = "http://maps.google.com/maps?f=d&hl=en&saddr="
                + srcPlace + "&daddr=" + destPlace
                + "&ie=UTF8&0&om=0&output=kml";
        Log.d("URL", urlString);
        Document doc = null;
        HttpURLConnection urlConnection = null;
        URL url = null;
        ArrayList<String> pathConent = new ArrayList<String>();
        try {

            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(urlConnection.getInputStream());

        } catch (Exception e) {
        }

        NodeList nl = doc.getElementsByTagName("LineString");
        for (int s = 0; s < nl.getLength(); s++) {
            Node rootNode = nl.item(s);
            NodeList configItems = rootNode.getChildNodes();
            for (int x = 0; x < configItems.getLength(); x++) {
                Node lineStringNode = configItems.item(x);
                NodeList path = lineStringNode.getChildNodes();
                pathConent.add(path.item(0).getNodeValue());
            }
        }
        placeMarks=new ArrayList<String>();
        NodeList place=doc.getElementsByTagName("Placemark");
        for(int i=0;i<place.getLength();i++){
            Node root=place.item(i);
            NodeList config=root.getChildNodes();
                Node placenode=config.item(0);
                NodeList name=placenode.getChildNodes();
                placeMarks.add(name.item(0).getNodeValue());
                Log.i("Node Value: ", ""+name.item(0).getNodeValue());

        }
        placeMarks.remove((placeMarks.size()-1));
        Log.i("LineString: ", pathConent.get(0));
        ArrayList<String> tmpcoords=new ArrayList<String>();
        for(int i=0;i<pathConent.size();i++){
            tmpcoords.addAll(Arrays.asList(pathConent.get(i).split(" ")));
        }
        //String[] tempContent = pathConent.split(" ");
       
    }


	@Override
	public void onLocationChanged(Location location) {
		sourceLongitude=location.getLongitude();
		sourceLatitude=location.getLatitude();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	/*private void DrawPath(GeoPoint src, GeoPoint dest, int color,
			MapView mMapView01) {

		// connect to map web service
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString((double) src.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString
				.append(Double.toString((double) src.getLongitudeE6() / 1.0E6));
		urlString.append("&daddr=");// to
		urlString
				.append(Double.toString((double) dest.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString.append(Double
				.toString((double) dest.getLongitudeE6() / 1.0E6));
		urlString.append("&ie=UTF8&0&om=0&output=kml");

		Log.d("xxx", "URL=" + urlString.toString());

		// get the kml (XML) doc. And parse it to get the coordinates(direction
		// route).
		Document doc = null;
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(urlString.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.connect();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(urlConnection.getInputStream());

			if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {

				// String path =
				// doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getNodeName();
				String path = doc.getElementsByTagName("GeometryCollection")
						.item(0).getFirstChild().getFirstChild()
						.getFirstChild().getNodeValue();

				Log.d("xxx", "path=" + path);

				String[] pairs = path.split(" ");
				String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude
														// lngLat[1]=latitude
														// lngLat[2]=height

				// src
				GeoPoint startGP = new GeoPoint((int) (Double
						.parseDouble(lngLat[1]) * 1E6), (int) (Double
						.parseDouble(lngLat[0]) * 1E6));
				mMapView01.getOverlays()
						.add(new MyOverLay(startGP, startGP, 1));

				GeoPoint gp1;
				GeoPoint gp2 = startGP;
				for (int i = 1; i < pairs.length; i++) // the last one would be
														// crash
				{
					lngLat = pairs[i].split(",");
					gp1 = gp2;
					// watch out! For GeoPoint, first:latitude, second:longitude
					gp2 = new GeoPoint(
							(int) (Double.parseDouble(lngLat[1]) * 1E6),
							(int) (Double.parseDouble(lngLat[0]) * 1E6));
					mMapView01.getOverlays().add(
							new MyOverLay(gp1, gp2, 2, color));

					Log.d("xxx", "pair:" + pairs[i]);

				}
				mMapView01.getOverlays().add(new MyOverLay(dest, dest, 3)); // use
																			// the
																			// default
																			// color
			}
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();

		} catch (SAXException e) {

			e.printStackTrace();
		}

	}
	 */
}

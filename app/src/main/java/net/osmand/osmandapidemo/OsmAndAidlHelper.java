package main.java.net.osmand.osmandapidemo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import net.osmand.aidl.IOsmAndAidlInterface;
import net.osmand.aidl.gpx.ASelectedGpxFile;
import net.osmand.aidl.gpx.HideGpxParams;
import net.osmand.aidl.gpx.ImportGpxParams;
import net.osmand.aidl.gpx.ShowGpxParams;
import net.osmand.aidl.map.ALatLon;
import net.osmand.aidl.map.SetMapLocationParams;
import net.osmand.aidl.maplayer.AMapLayer;
import net.osmand.aidl.maplayer.AddMapLayerParams;
import net.osmand.aidl.maplayer.RemoveMapLayerParams;
import net.osmand.aidl.maplayer.UpdateMapLayerParams;
import net.osmand.aidl.maplayer.point.AMapPoint;
import net.osmand.aidl.maplayer.point.AddMapPointParams;
import net.osmand.aidl.maplayer.point.RemoveMapPointParams;
import net.osmand.aidl.maplayer.point.UpdateMapPointParams;
import net.osmand.aidl.mapmarker.AMapMarker;
import net.osmand.aidl.mapmarker.AddMapMarkerParams;
import net.osmand.aidl.mapmarker.RemoveMapMarkerParams;
import net.osmand.aidl.mapmarker.UpdateMapMarkerParams;
import net.osmand.aidl.mapwidget.AMapWidget;
import net.osmand.aidl.mapwidget.AddMapWidgetParams;
import net.osmand.aidl.mapwidget.RemoveMapWidgetParams;
import net.osmand.aidl.mapwidget.UpdateMapWidgetParams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.java.net.osmand.osmandapidemo.OsmAndHelper.OnOsmandMissingListener;

public class OsmAndAidlHelper {

	private static final String OSMAND_FREE_PACKAGE_NAME = "net.osmand";
	private static final String OSMAND_PLUS_PACKAGE_NAME = "net.osmand.plus";
	private static final String OSMAND_PACKAGE_NAME = OSMAND_PLUS_PACKAGE_NAME;

	private final Application app;
	private final OnOsmandMissingListener mOsmandMissingListener;
	private IOsmAndAidlInterface mIOsmAndAidlInterface;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
									   IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mIOsmAndAidlInterface = IOsmAndAidlInterface.Stub.asInterface(service);
			Toast.makeText(app, "OsmAnd service connected", Toast.LENGTH_SHORT).show();
		}
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mIOsmAndAidlInterface = null;
			Toast.makeText(app, "OsmAnd service disconnected", Toast.LENGTH_SHORT).show();
		}
	};

	public OsmAndAidlHelper(Application application, OnOsmandMissingListener listener) {
		this.app = application;
		this.mOsmandMissingListener = listener;
		bindService();
	}

	private boolean bindService() {
		if (mIOsmAndAidlInterface == null) {
			Intent intent = new Intent("net.osmand.aidl.OsmandAidlService");
			intent.setPackage(OSMAND_PACKAGE_NAME);
			boolean res = app.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			if (res) {
				Toast.makeText(app, "OsmAnd service bind", Toast.LENGTH_SHORT).show();
				return true;
			} else {
				Toast.makeText(app, "OsmAnd service NOT bind", Toast.LENGTH_SHORT).show();
				mOsmandMissingListener.osmandMissing();
				return false;
			}
		} else {
			return true;
		}
	}

	public void cleanupResources() {
		if (mIOsmAndAidlInterface != null) {
			app.unbindService(mConnection);
		}
	}

	/**
	 * Add map marker at given location.
	 *
	 * @param lat  - latitude.
	 * @param lon  - longitude.
	 * @param name - name.
	 */
	public boolean addMapMarker(double lat, double lon, String name) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapMarker marker = new AMapMarker(new ALatLon(lat, lon), name);
				return mIOsmAndAidlInterface.addMapMarker(new AddMapMarkerParams(marker));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Update map marker at given location with name.
	 *
	 * @param latPrev  - latitude (current marker).
	 * @param lonPrev  - longitude (current marker).
	 * @param namePrev - name (current marker).
	 * @param latNew  - latitude (new marker).
	 * @param lonNew  - longitude (new marker).
	 * @param nameNew - name (new marker).
	 */
	public boolean updateMapMarker(double latPrev, double lonPrev, String namePrev,
								   double latNew, double lonNew, String nameNew) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapMarker markerPrev = new AMapMarker(new ALatLon(latPrev, lonPrev), namePrev);
				AMapMarker markerNew = new AMapMarker(new ALatLon(latNew, lonNew), nameNew);
				return mIOsmAndAidlInterface.updateMapMarker(new UpdateMapMarkerParams(markerPrev, markerNew));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Remove map marker at given location with name.
	 *
	 * @param lat  - latitude.
	 * @param lon  - longitude.
	 * @param name - name.
	 */
	public boolean removeMapMarker(double lat, double lon, String name) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapMarker marker = new AMapMarker(new ALatLon(lat, lon), name);
				return mIOsmAndAidlInterface.removeMapMarker(new RemoveMapMarkerParams(marker));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Add map widget to the right side of the main screen.
	 * Note: any specified icon should exist in OsmAnd app resources.
	 *
	 * @param id - widget id.
	 * @param menuIconName - icon name (configure map menu).
	 * @param menuTitle - widget name (configure map menu).
	 * @param lightIconName - icon name for the light theme (widget).
	 * @param darkIconName - icon name for the dark theme (widget).
	 * @param text - main widget text.
	 * @param description - sub text, like "km/h".
	 * @param order - order position in the widgets list.
	 * @param intentOnClick - onClick intent. Called after click on widget as startActivity(Intent intent).
	 */
	public boolean addMapWidget(String id, String menuIconName, String menuTitle,
								String lightIconName, String darkIconName, String text, String description,
								int order, Intent intentOnClick) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapWidget widget = new AMapWidget(id, menuIconName, menuTitle, lightIconName,
						darkIconName, text, description, order, intentOnClick);
				return mIOsmAndAidlInterface.addMapWidget(new AddMapWidgetParams(widget));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Update map widget.
	 * Note: any specified icon should exist in OsmAnd app resources.
	 *
	 * @param id - widget id.
	 * @param menuIconName - icon name (configure map menu).
	 * @param menuTitle - widget name (configure map menu).
	 * @param lightIconName - icon name for the light theme (widget).
	 * @param darkIconName - icon name for the dark theme (widget).
	 * @param text - main widget text.
	 * @param description - sub text, like "km/h".
	 * @param order - order position in the widgets list.
	 * @param intentOnClick - onClick intent. Called after click on widget as startActivity(Intent intent).
	 */
	public boolean updateMapWidget(String id, String menuIconName, String menuTitle,
								String lightIconName, String darkIconName, String text, String description,
								int order, Intent intentOnClick) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapWidget widget = new AMapWidget(id, menuIconName, menuTitle, lightIconName,
						darkIconName, text, description, order, intentOnClick);
				return mIOsmAndAidlInterface.updateMapWidget(new UpdateMapWidgetParams(widget));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Remove map widget.
	 *
	 * @param id - widget id.
	 */
	public boolean removeMapWidget(String id) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.removeMapWidget(new RemoveMapWidgetParams(id));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Add user layer on the map.
	 *
	 * @param id - layer id.
	 * @param name - layer name.
	 * @param zOrder - z-order position of layer. Default value is 5.5f
	 * @param points - initial list of points. Nullable.
	 */
	public boolean addMapLayer(String id, String name, float zOrder, List<AMapPoint> points) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapLayer layer = new AMapLayer(id, name, zOrder, points);
				return mIOsmAndAidlInterface.addMapLayer(new AddMapLayerParams(layer));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Update user layer.
	 *
	 * @param id - layer id.
	 * @param name - layer name.
	 * @param zOrder - z-order position of layer. Default value is 5.5f
	 * @param points - list of points. Nullable.
	 */
	public boolean updateMapLayer(String id, String name, float zOrder, List<AMapPoint> points) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapLayer layer = new AMapLayer(id, name, zOrder, points);
				return mIOsmAndAidlInterface.updateMapLayer(new UpdateMapLayerParams(layer));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Remove user layer.
	 *
	 * @param id - layer id.
	 */
	public boolean removeMapLayer(String id) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.removeMapLayer(new RemoveMapLayerParams(id));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Add point to user layer.
	 *
	 * @param layerId - layer id. Note: layer should be added first.
	 * @param pointId - point id.
	 * @param shortName - short name (single char). Displayed on the map.
	 * @param fullName - full name. Displayed in the context menu on first row.
	 * @param typeName - type name. Displayed in context menu on second row.
	 * @param color - color of circle's background.
	 * @param location - location of the point.
	 * @param details - list of details. Displayed under context menu.
	 */
	public boolean addMapPoint(String layerId, String pointId, String shortName, String fullName,
							   String typeName, int color, ALatLon location, List<String> details) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapPoint point = new AMapPoint(pointId, shortName, fullName, typeName, color, location, details);
				return mIOsmAndAidlInterface.addMapPoint(new AddMapPointParams(layerId, point));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Update point.
	 *
	 * @param layerId - layer id.
	 * @param pointId - point id.
	 * @param shortName - short name (single char). Displayed on the map.
	 * @param fullName - full name. Displayed in the context menu on first row.
	 * @param typeName - type name. Displayed in context menu on second row.
	 * @param color - color of circle's background.
	 * @param location - location of the point.
	 * @param details - list of details. Displayed under context menu.
	 */
	public boolean updateMapPoint(String layerId, String pointId, String shortName, String fullName,
								  String typeName, int color, ALatLon location, List<String> details) {
		if (mIOsmAndAidlInterface != null) {
			try {
				AMapPoint point = new AMapPoint(pointId, shortName, fullName, typeName, color, location, details);
				return mIOsmAndAidlInterface.updateMapPoint(new UpdateMapPointParams(layerId, point));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Remove point.
	 *
	 * @param layerId - layer id.
	 * @param pointId - point id.
	 */
	public boolean removeMapPoint(String layerId, String pointId) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.removeMapPoint(new RemoveMapPointParams(layerId, pointId));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Import GPX file to OsmAnd.
	 * OsmAnd must have rights to access location. Not recommended.
	 *
	 * @param file - File which represents GPX track.
	 * @param fileName - Destination file name. May contain dirs.
	 */
	public boolean importGpxFromFile(File file, String fileName) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.importGpx(new ImportGpxParams(file, fileName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Import GPX file to OsmAnd.
	 *
	 * @param gpxUri - URI created by FileProvider.
	 * @param fileName - Destination file name. May contain dirs.
	 */
	public boolean importGpxFromUri(Uri gpxUri, String fileName) {
		if (mIOsmAndAidlInterface != null) {
			try {
				app.grantUriPermission(OSMAND_PACKAGE_NAME, gpxUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
				return mIOsmAndAidlInterface.importGpx(new ImportGpxParams(gpxUri, fileName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Import GPX file to OsmAnd.
	 *
	 * @param data - Raw contents of GPX file. Sent as intent's extra string parameter.
	 * @param fileName - Destination file name. May contain dirs.
	 */
	public boolean importGpxFromData(String data, String fileName) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.importGpx(new ImportGpxParams(data, fileName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Show GPX file on map.
	 *
	 * @param fileName - file name to show. Must be imported first.
	 */
	public boolean showGpx(String fileName) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.showGpx(new ShowGpxParams(fileName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Hide GPX file.
	 *
	 * @param fileName - file name to hide.
	 */
	public boolean hideGpx(String fileName) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.hideGpx(new HideGpxParams(fileName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Get list of active GPX files.
	 *
	 * @return list of active gpx files.
	 */
	public List<ASelectedGpxFile> getActiveGpxFiles() {
		if (mIOsmAndAidlInterface != null) {
			try {
				List<ASelectedGpxFile> res = new ArrayList<>();
				if (mIOsmAndAidlInterface.getActiveGpx(res)) {
					return res;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Get list of active GPX files.
	 *
	 * @param latitude - latitude of new map center.
	 * @param longitude - longitude of new map center.
	 * @param zoom - map zoom level. Set 0 to keep zoom unchanged.
	 * @param animated - set true to animate changes.
	 */
	public boolean setMapLocation(double latitude, double longitude, int zoom, boolean animated) {
		if (mIOsmAndAidlInterface != null) {
			try {
				return mIOsmAndAidlInterface.setMapLocation(
						new SetMapLocationParams(latitude, longitude, zoom, animated));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}

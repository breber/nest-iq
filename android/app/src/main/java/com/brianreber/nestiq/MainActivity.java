package com.brianreber.nestiq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.ConnectIQ.ConnectIQListener;
import com.garmin.android.connectiq.ConnectIQ.IQApplicationEventListener;
import com.garmin.android.connectiq.ConnectIQ.IQDeviceEventListener;
import com.garmin.android.connectiq.ConnectIQ.IQMessageStatus;
import com.garmin.android.connectiq.ConnectIQ.IQSdkErrorStatus;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;
import com.nestapi.lib.API.AccessToken;
import com.nestapi.lib.API.Listener;
import com.nestapi.lib.API.NestAPI;
import com.nestapi.lib.API.Structure;
import com.nestapi.lib.API.Thermostat;
import com.nestapi.lib.AuthManager;
import com.nestapi.lib.ClientMetadata;

import java.util.List;

public class MainActivity extends Activity implements
        View.OnClickListener,
        NestAPI.AuthenticationListener,
        Listener.StructureListener,
        Listener.ThermostatListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String sTAG = "ConnectIQ";
    private static final String THERMOSTAT_KEY = "thermostat_key";
    private static final String STRUCTURE_KEY = "structure_key";

    private static final int AUTH_TOKEN_REQUEST_CODE = 101;

    private ConnectIQ mConnectIQ;
    private IQDevice mDevice;
    private IQApp    mApp;

    private TextView mAmbientTempText;
    private View mSingleControlContainer;
    private TextView mCurrentTempText;
    private TextView mStructureNameText;
    private View mThermostatView;
    private View mRangeControlContainer;
    private TextView mCurrentCoolTempText;
    private TextView mCurrentHeatTempText;
    private Button mStructureAway;

    private Listener mUpdateListener;
    private NestAPI mNestApi;
    private AccessToken mToken;
    private Thermostat mThermostat;
    private Structure mStructure;

    // Start ConnectIQ

    /**
     * Listener for SDK specific events.
     */
    ConnectIQListener listener = new ConnectIQListener() {

        /**
         * Received when the SDK is ready for additional method calls after calling initialize().
         */
        @Override
        public void onSdkReady() {
//            mSdkStatusText.setText(String.format(getString(R.string.initialized_format), mConnectIQ.getAdbPort()));

            /**
             * Retrieve a list of available (aka currently connected via Garmin Connect Mobile) to display
             * to the user.
             */
            List<IQDevice> devicelist = mConnectIQ.getAvailableDevices();

            if (devicelist.size() == 0) {
//                mDevicesText.setText(R.string.no_paired_devices);
            } else {
                StringBuilder builder = new StringBuilder();
                for (IQDevice device : devicelist) {
                    builder.append(device.getFriendlyName());
                    builder.append("\r\n");
                }
//                mDevicesText.setText(builder.toString());
            }

            /**
             * Retrieves a list of paired ConnectIQ devices.  This will return a device even if it is not
             * currently connected but is paired with the Garmin Connect Mobile application.  This allows
             * us to register for events to be notified when a device connects or disconnects.
             */
            List<IQDevice> deviceList = mConnectIQ.getPairedDevices();

            StringBuilder builder = new StringBuilder();
            for (IQDevice device : deviceList) {

                /**
                 * Register for event for each device.   This will allow us to receive connect / disconnect
                 * notifications for the devices.  This can be useful if wanting to display information
                 * regarding the currently connected device.
                 */
                mConnectIQ.registerForEvents(device, eventListener, mApp, appEventListener);

                builder.append(device.getFriendlyName());
                builder.append("\r\n");
            }

//            mDevicesText.setText(builder.toString());

            /**
             * Check the connection status.  This is necessary because our call
             * to registerForEvents will only notify us of changes from the devices
             * current state.  So if it is already connected when we register for
             * events, we will not be notified that it is connected.
             *
             * For this sample we are just going to deal with the first device
             * from the list, but it is probably better to look at the status
             * for each device if multiple and possibly display a UI for the
             * user to select which device they want to use if multiple are
             * connected.
             */
            IQDevice device = deviceList.get(0);
            try {
                IQDeviceStatus status = mConnectIQ.getStatus(device);
//                updateStatus(status);
//
//                mSendMessage.setEnabled(status == IQDeviceStatus.CONNECTED);
//                mMessageInput.setEnabled(status == IQDeviceStatus.CONNECTED);
            } catch (IllegalStateException e) {
                Log.e(sTAG, "Illegal state calling getStatus", e);
            } catch (ServiceUnavailableException e) {
                Log.e(sTAG, "Service Unavailable", e);
            }
        }

        /**
         * Called if the SDK failed to initialize.  Inspect IQSdkErrorStatus for specific
         * reason initialization failed.
         */
        @Override
        public void onInitializeError(IQSdkErrorStatus status) {
//            mSdkStatusText.setText(status.toString());
        }

        /**
         * Called when the ConnectIQ::shutdown() method is called.  ConnectIQ is a singleton so
         * any call to shutdown() will uninitialize the SDK for all references.
         */
        @Override
        public void onSdkShutDown() {
//            mSdkStatusText.setText("Shut Down");
        }
    };

    /**
     * Listener for receiving device specific events.
     */
    IQDeviceEventListener eventListener = new IQDeviceEventListener() {
        @Override
        public void onDeviceStatusChanged(IQDevice device, IQDeviceStatus newStatus) {
//            updateStatus(newStatus);
//            mSendMessage.setEnabled(newStatus == IQDeviceStatus.CONNECTED);
//            mMessageInput.setEnabled(newStatus == IQDeviceStatus.CONNECTED);
        }
    };

    /**
     * Listener for receiving events from applications on a device.
     */
    IQApplicationEventListener appEventListener = new IQApplicationEventListener() {
        @Override
        public void onMessageReceived(IQDevice device, IQApp fromApp, List<Object> messageData, IQMessageStatus status) {
            StringBuilder builder = new StringBuilder();
            for (Object obj : messageData) {
                if (obj instanceof String) {
                    builder.append((String)obj);
                } else {
                    builder.append("Non string object received");
                }
                builder.append("\r\n");
            }

            displayMessage(builder.toString());
        }
    };

    // END ConnectIQ

    /**
     * When the device sends us a message we will just display a toast notification
     */
    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mThermostatView = findViewById(R.id.thermostat_view);
        mSingleControlContainer = findViewById(R.id.single_control);
        mCurrentTempText = (TextView)findViewById(R.id.current_temp);
        mStructureNameText = (TextView)findViewById(R.id.structure_name);
        mAmbientTempText = (TextView)findViewById(R.id.ambient_temp);
        mStructureAway = (Button)findViewById(R.id.structure_away_btn);
        mRangeControlContainer = findViewById(R.id.range_control);
        mCurrentCoolTempText = (TextView)findViewById(R.id.current_cool_temp);
        mCurrentHeatTempText = (TextView)findViewById(R.id.current_heat_temp);

        mStructureAway.setOnClickListener(mStructureAwayClickListener);
        findViewById(R.id.heat).setOnClickListener(mModeClickListener);
        findViewById(R.id.cool).setOnClickListener(mModeClickListener);
        findViewById(R.id.heat_cool).setOnClickListener(mModeClickListener);
        findViewById(R.id.off).setOnClickListener(mModeClickListener);
        findViewById(R.id.temp_up).setOnClickListener(this);
        findViewById(R.id.temp_down).setOnClickListener(this);
        findViewById(R.id.temp_cool_up).setOnClickListener(this);
        findViewById(R.id.temp_cool_down).setOnClickListener(this);
        findViewById(R.id.temp_heat_up).setOnClickListener(this);
        findViewById(R.id.temp_heat_down).setOnClickListener(this);

        mNestApi = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(this);
        if (mToken != null) {
            authenticate(mToken);
        } else {
            obtainAccessToken();
        }

        if (savedInstanceState != null) {
            mThermostat = savedInstanceState.getParcelable(THERMOSTAT_KEY);
            mStructure = savedInstanceState.getParcelable(STRUCTURE_KEY);
            updateView();
        }

        // Get an instance of ConnectIQ that does BLE simulation over ADB to the simulator.
        mConnectIQ = ConnectIQ.getInstance(ConnectIQ.IQCommProtocol.SIMULATED_BLE);
        mApp = new IQApp("", "NestIQ", 1);
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Initializes the SDK.  This must be done before making any calls that will communicate with
         * a Connect IQ device.
         */
        mConnectIQ.initialize(this, true, listener);

        /**
         * We cannot do anything here to call any APIs.   We need to wait and do any additional things once the onSdkReady() call
         * is made on the listener.
         */
    }

    @Override
    public void onPause() {
        super.onPause();

        /**
         * Shutdown the SDK so resources and listeners can be released.
         */
        if (isFinishing()) {
            mConnectIQ.shutdown();
        } else {
            /**
             * Unregister for all events.  This is good practice to clean up to
             * allow the SDK to free resources and not listen for events that
             * no one is interested in.
             *
             * We do not call this if we are shutting down because the shutdown
             * method will call this for us during the clean up process.
             */
            mConnectIQ.unregisterAllForEvents();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(THERMOSTAT_KEY, mThermostat);
        outState.putParcelable(STRUCTURE_KEY, mStructure);
    }

    private void obtainAccessToken() {
        Log.v(TAG, "starting auth flow...");
        final ClientMetadata metadata = new ClientMetadata.Builder()
                .setClientID(Constants.CLIENT_ID)
                .setClientSecret(Constants.CLIENT_SECRET)
                .setRedirectURL(Constants.REDIRECT_URL)
                .build();
        AuthManager.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE, metadata);
    }

    private View.OnClickListener mStructureAwayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mStructure == null) {
                return;
            }

            Structure.AwayState awayState;

            switch (mStructure.getAwayState()) {
                case AUTO_AWAY:
                case AWAY:
                    awayState = Structure.AwayState.HOME;
                    mStructureAway.setText(R.string.away_state_home);
                    break;
                case HOME:
                    awayState = Structure.AwayState.AWAY;
                    mStructureAway.setText(R.string.away_state_away);
                    break;
                default:
                    return;
            }

            mNestApi.setStructureAway(mStructure.getStructureID(), awayState, null);
        }
    };

    private View.OnClickListener mModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mThermostat == null) {
                return;
            }

            final String thermostatID = mThermostat.getDeviceID();
            final Thermostat.HVACMode mode;

            switch (v.getId()) {
                case R.id.heat:
                    mode = Thermostat.HVACMode.HEAT;
                    break;
                case R.id.cool:
                    mode = Thermostat.HVACMode.COOL;
                    break;
                case R.id.heat_cool:
                    mode = Thermostat.HVACMode.HEAT_AND_COOL;
                    break;
                case R.id.off:
                default:
                    mode = Thermostat.HVACMode.OFF;
                    break;
            }

            mNestApi.setHVACMode(thermostatID, mode, null);
        }
    };

    @Override
    public void onClick(View v) {
        if (mThermostat == null) {
            return;
        }

        switch (mThermostat.getHVACmode()) {
            case HEAT_AND_COOL:
                updateTempRange(v);
                break;
            case OFF:
                //NO-OP
                break;
            default:
                updateTempSingle(v);
                break;
        }
    }

    private void updateOff() {
        mCurrentTempText.setText(R.string.thermostat_off);
        mThermostatView.setBackgroundResource(R.drawable.off_thermostat_drawable);
    }

    private void updateTempRange(View v) {
        String thermostatID = mThermostat.getDeviceID();
        long tempHigh = mThermostat.getTargetTemperatureHighF();
        long tempLow = mThermostat.getTargetTemperatureLowF();

        switch (v.getId()) {
            case R.id.temp_cool_down:
                tempLow -= 1;
                mNestApi.setTargetTemperatureLowF(thermostatID, tempLow, null);
                mCurrentCoolTempText.setText(Long.toString(tempLow));
                break;
            case R.id.temp_cool_up:
                tempLow += 1;
                mNestApi.setTargetTemperatureLowF(thermostatID, tempLow, null);
                mCurrentCoolTempText.setText(Long.toString(tempLow));
                break;
            case R.id.temp_heat_down:
                tempHigh -= 1;
                mNestApi.setTargetTemperatureHighF(thermostatID, tempHigh, null);
                mCurrentHeatTempText.setText(Long.toString(tempHigh));
                break;
            case R.id.temp_heat_up:
                tempHigh += 1;
                mNestApi.setTargetTemperatureHighF(thermostatID, tempHigh, null);
                mCurrentHeatTempText.setText(Long.toString(tempHigh));
                break;
        }
    }

    private void updateTempSingle(View v) {
        long temp = mThermostat.getTargetTemperatureF();

        switch (v.getId()) {
            case R.id.temp_up:
                temp += 1;
                break;
            case R.id.temp_down:
                temp -= 1;
                break;
        }

        mCurrentTempText.setText(Long.toString(temp));
        mNestApi.setTargetTemperatureF(mThermostat.getDeviceID(), temp, null);
    }

    private void updateView() {
        updateAmbientTempTextView();
        updateStructureViews();
        updateThermostatViews();
    }

    private void updateAmbientTempTextView() {
        if (mThermostat != null) {
            mAmbientTempText.setText(Long.toString(mThermostat.getAmbientTemperatureF()));
        }
    }

    private void updateStructureViews() {
        if (mStructure != null) {
            mStructureNameText.setText(mStructure.getName());
            mStructureAway.setText(mStructure.getAwayState().getKey());
        }
    }

    private void updateThermostatViews() {
        if (mThermostat == null || mStructure == null) {
            return;
        }

        Thermostat.HVACMode mode = mThermostat.getHVACmode();
        int singleControlVisibility;
        int rangeControlVisibility;
        Structure.AwayState state = mStructure.getAwayState();
        boolean isAway = state == Structure.AwayState.AWAY || state == Structure.AwayState.AUTO_AWAY;

        if(isAway) {
            mSingleControlContainer.setVisibility(View.VISIBLE);
            mRangeControlContainer.setVisibility(View.GONE);
            updateSingleControlView();
            return;
        }

        switch (mode) {
            case HEAT_AND_COOL:
                singleControlVisibility = View.GONE;
                rangeControlVisibility = View.VISIBLE;
                updateRangeControlView();
                break;
            case OFF:
                singleControlVisibility = View.VISIBLE;
                rangeControlVisibility = View.GONE;
                updateOff();
                break;
            default:
                singleControlVisibility = View.VISIBLE;
                rangeControlVisibility = View.GONE;
                updateSingleControlView();
                break;
        }

        mSingleControlContainer.setVisibility(singleControlVisibility);
        mRangeControlContainer.setVisibility(rangeControlVisibility);
    }

    private void updateRangeControlView() {
        mCurrentHeatTempText.setText(Long.toString(mThermostat.getTargetTemperatureHighF()));
        mCurrentCoolTempText.setText(Long.toString(mThermostat.getTargetTemperatureLowF()));

        final long tempDiffHigh = mThermostat.getTargetTemperatureHighF() - mThermostat.getAmbientTemperatureF();
        final long tempDiffLow = mThermostat.getTargetTemperatureLowF() - mThermostat.getAmbientTemperatureF();

        final int thermostatDrawable;
        if (tempDiffHigh < 0) {
            thermostatDrawable = R.drawable.cool_thermostat_drawable;
        } else if(tempDiffLow > 0) {
            thermostatDrawable = R.drawable.heat_thermostat_drawable;
        } else {
            thermostatDrawable = R.drawable.off_thermostat_drawable;
        }
        mThermostatView.setBackgroundResource(thermostatDrawable);
    }

    private void updateSingleControlView() {
        Structure.AwayState state = mStructure.getAwayState();
        if(state == Structure.AwayState.AWAY || state == Structure.AwayState.AUTO_AWAY) {
            mCurrentTempText.setText(R.string.thermostat_away);
            mThermostatView.setBackgroundResource(R.drawable.off_thermostat_drawable);
            return;
        }

        mCurrentTempText.setText(Long.toString(mThermostat.getTargetTemperatureF()));
        Log.v(TAG, "targetTempF: " + mThermostat.getTargetTemperatureF() + " ambientF: " + mThermostat.getAmbientTemperatureF());
        final long tempDiffF = mThermostat.getTargetTemperatureF() - mThermostat.getAmbientTemperatureF();

        final int thermostatDrawable;
        switch (mThermostat.getHVACmode()) {
            case HEAT:
                thermostatDrawable = tempDiffF > 0 ? R.drawable.heat_thermostat_drawable : R.drawable.off_thermostat_drawable;
                break;
            case COOL:
                thermostatDrawable = tempDiffF < 0 ? R.drawable.cool_thermostat_drawable : R.drawable.off_thermostat_drawable;
                break;
            case OFF:
            default:
                thermostatDrawable = R.drawable.off_thermostat_drawable;
                break;
        }
        mThermostatView.setBackgroundResource(thermostatDrawable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            return;
        }

        if (AuthManager.hasAccessToken(data)) {
            mToken = AuthManager.getAccessToken(data);
            Settings.saveAuthToken(this, mToken);
            Log.v(TAG, "Main Activity parsed auth token: " + mToken.getToken() + " expires: " + mToken.getExpiresIn());
            authenticate(mToken);
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }
    }

    private void authenticate(AccessToken token) {
        Log.v(TAG, "Authenticating...");
        NestAPI.getInstance().authenticate(token, this);
    }

    @Override
    public void onAuthenticationSuccess() {
        Log.v(TAG, "Authentication succeeded.");
        fetchData();
    }

    @Override
    public void onAuthenticationFailure(int errorCode) {
        Log.v(TAG, "Authentication failed with error: " + errorCode);
    }

    private void fetchData(){
        Log.v(TAG, "Fetching data...");

        mUpdateListener = new Listener.Builder()
                .setStructureListener(this)
                .setThermostatListener(this)
                .build();

        mNestApi.addUpdateListener(mUpdateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNestApi.removeUpdateListener(mUpdateListener);
    }

    @Override
    public void onThermostatUpdated(Thermostat thermostat) {
        Log.v(TAG, String.format("Thermostat (%s) updated.", thermostat.getDeviceID()));
        mThermostat = thermostat;
        updateView();
    }

    @Override
    public void onStructureUpdated(Structure structure) {
        Log.v(TAG, String.format("Structure (%s) updated.", structure.getStructureID()));
        mStructure = structure;
        updateView();
    }
}

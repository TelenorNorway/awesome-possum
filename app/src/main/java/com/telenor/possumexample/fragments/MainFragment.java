package com.telenor.possumexample.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.telenor.possumexample.MainActivity;
import com.telenor.possumexample.R;
import com.telenor.possumexample.views.TrustButton;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;
import com.telenor.possumlib.interfaces.IPossumMessage;
import com.telenor.possumlib.interfaces.IPossumTrust;
import com.telenor.possumlib.utils.Do;
import com.telenor.possumlib.utils.Send;

public class MainFragment extends Fragment implements IPossumTrust, IPossumMessage {
    private TrustButton trustButton;
    private TextView status;
    private long clientGatherStart;
    private long clientGatherEnd;
    private long clientSendStart;
    private long clientSendEnd;
    private long serverWaitStart;
    private long serverWaitEnd;
    private RelativeLayout graphContainer;
    private LinearLayout previewImageContainer;
    private static final boolean isGathering = false; // Set to false if it should do authentication
    private static final String tag = MainFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        AwesomePossum.addTrustListener(getContext(), this);
        status = (TextView) view.findViewById(R.id.status);
        graphContainer = (RelativeLayout)view.findViewById(R.id.graphContainer);
        AwesomePossum.addMessageListener(getContext(), this);
        previewImageContainer = (LinearLayout) view.findViewById(R.id.cameraResult);
        trustButton = (TrustButton) view.findViewById(R.id.trustWheel);
        trustButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGathering) {
                    if (AwesomePossum.isListening()) {
                        AwesomePossum.stopListening(getActivity());
                        AwesomePossum.startUpload(getActivity(), myId(), getString(R.string.identityPoolId));
                        Toast.makeText(getActivity(), "Stopping listening", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            AwesomePossum.startListening(getActivity(), myId(), getString(R.string.identityPoolId));
                            Toast.makeText(getActivity(), "Starting listening", Toast.LENGTH_SHORT).show();
                        } catch (GatheringNotAuthorizedException e) {
                            AwesomePossum.getAuthorizeDialog(getActivity(), myId(), getString(R.string.identityPoolId), "poc_consent/", "Authorize AwesomePossum", "We need permission from you", "Granted", "Denied").show();
                        }
                    }
                } else {
                    if (((MainActivity) getActivity()).validId(myId())) {
                        if (AwesomePossum.isAuthorized(getActivity(), myId())) {
                            if (trustButton.isAuthenticating()) {
                                trustButton.stopAuthenticate();
                            } else {
                                clientGatherStart = System.currentTimeMillis();
                                trustButton.authenticate(myId());
                            }
                        } else {
                            AwesomePossum.getAuthorizeDialog(getActivity(), myId(), getString(R.string.identityPoolId), "Authorize AwesomePossum", "We need permission from you", "Granted", "Denied").show();
                        }
                    } else {
                        ((MainActivity) getActivity()).showInvalidIdDialog();
                    }
                }
            }
        });
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        setHasOptionsMenu(true);
        updateStatus();
        if (!((MainActivity) getActivity()).validId(myId())) {
            Send.messageIntent(getContext(), Messaging.MISSING_VALID_ID, null);
        } else {
            Send.messageIntent(getContext(), Messaging.READY_TO_AUTH, null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.toggleBottom:
                toggleCamera(menuItem);
                return true;
        }
        return false;
    }

    private void toggleCamera(MenuItem menuItem) {
        if (previewImageContainer.getVisibility() == View.GONE) {
            menuItem.setTitle(R.string.show_graph);
            previewImageContainer.setVisibility(View.VISIBLE);
            graphContainer.setVisibility(View.GONE);
        } else {
            menuItem.setTitle(R.string.show_camera);
            previewImageContainer.setVisibility(View.GONE);
            graphContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AwesomePossum.sendDetectorStatus(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AwesomePossum.removeTrustListener(this);
        AwesomePossum.removeMessageListener(this);
    }

    private boolean updateStatus() {
        if (!((MainActivity) getActivity()).validId(myId())) {
            Send.messageIntent(getActivity(), Messaging.MISSING_VALID_ID, null);
            return false;
        }
        Send.messageIntent(getContext(), Messaging.READY_TO_AUTH, null);
        return true;
    }

    private String myId() {
        return ((MainActivity) getActivity()).myId();
    }


    @Override
    public void changeInCombinedTrust(final float combinedTrustScore, final String status) {
        Do.onMain(new Runnable() {
            @Override
            public void run() {
//                Log.i(tag, "Change in combined trust:"+combinedTrustScore+", Status:"+status);
//                trustButton.setTrustScore(combinedTrustScore * 100, "TRAINING".equals(status)?getString(R.string.training):null);
                trustButton.setTrustScore(combinedTrustScore * 100, null);

//                if ("TRAINING".equals(status)) {
//                    trustButton.setTrustScore(0, getContext().getString(R.string.training));
//                } else {
//                    trustButton.setTrustScore(combinedTrustScore * 100, null);
//                }
            }
        });
    }

    @Override
    public void changeInDetectorTrust(int detectorType, float newTrustScore, String status) {
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {
        Log.e(tag, "Failed to ascertain trust:", exception);
        Do.onMain(new Runnable() {
            @Override
            public void run() {
                trustButton.setTrustScore(0, "Failed");
            }
        });
    }

    @Override
    public void possumMessageReceived(final String msgType, final String message) {
        Do.onMain(new Runnable() {
            @Override
            public void run() {
                switch (msgType) {
                    case Messaging.GATHERING:
                        status.setText(getContext().getString(R.string.gathering));
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.MISSING_VALID_ID:
                        status.setText(R.string.error_too_short_id);
                        status.setTextColor(Color.RED);
                        trustButton.setEnabled(false);
                        break;
                    case Messaging.AUTH_STOP:
                        status.setText(R.string.stopped_auth);
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        trustButton.stopAuthenticate();
                        break;
                    case Messaging.AUTH_DONE:
                        serverWaitEnd = System.currentTimeMillis();
                        Log.d(tag, "TestAuth: Server response wait time: "+(serverWaitEnd-serverWaitStart)+" ms");
                        Log.d(tag, "TestAuth: Total roundTime:"+((clientGatherEnd-clientGatherStart)+(clientSendEnd-clientSendStart)+(serverWaitEnd-serverWaitStart))+" ms");
                        if (trustButton.isAuthenticating()) {
                            clientGatherStart = System.currentTimeMillis();
                            trustButton.authenticate(myId());
                        }
                        break;
                    case Messaging.READY_TO_AUTH:
                        status.setText(R.string.all_ok);
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.POSSUM_MESSAGE:
                        break;
                    case Messaging.FACE_FOUND:
                        break;
                    case Messaging.SENDING_RESULT:
                        status.setText(getContext().getString(R.string.sending_result));
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.START_SERVER_DATA_SEND:
                        clientGatherEnd = System.currentTimeMillis();
                        Log.d(tag, "TestAuth: Time spent gathering data:"+(clientGatherEnd-clientGatherStart)+" ms");
                        clientSendStart = Long.parseLong(message);
                        status.setText(getString(R.string.sending_data_to_server));
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.VERIFICATION_SUCCESS:
                        status.setText(getContext().getString(R.string.verification_success));
                        status.setTextColor(Color.BLACK);
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.WAITING_FOR_SERVER_RESPONSE:
                        clientSendEnd = System.currentTimeMillis();
                        status.setText(getString(R.string.waiting_for_server_response));
                        status.setTextColor(Color.BLACK);
                        serverWaitStart = Long.parseLong(message);
                        Log.d(tag, "TestAuth: Time spent processing and sending: "+(clientSendEnd-clientSendStart)+" ms");
                        trustButton.setEnabled(true);
                        break;
                    case Messaging.DETECTORS_STATUS:
                    case Messaging.POSSUM_PREVIEWS:
                    case Messaging.POSSUM_PREVIEWS_INVALID:
                    case Messaging.REQUEST_DETECTORS:
                        break;
                    default:
//                        status.setText(message);
//                        status.setTextColor(Color.RED);
//                        trustButton.setEnabled(false);
                        Log.e(tag, "Sending data: Unhandled possum message:" + msgType + ":" + message);
                }
            }
        });
    }

    @Override
    public void possumFaceFound(byte[] dataReceived) {
    }

    @Override
    public void possumImageSnapped(byte[] dataReceived) {

    }

    @Override
    public void possumFaceCoordsReceived(int[] xCoords, int[] yCoords) {
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 2:
                    return AllSensorsChartFragment.instantiate(getContext(), AllSensorsChartFragment.class.getName());
                case 1:
                    return CombinedTrustChart.instantiate(getContext(), CombinedTrustChart.class.getName());
                default:
                    return IndividualSensorTrustFragment.instantiate(getContext(), IndividualSensorTrustFragment.class.getName());
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
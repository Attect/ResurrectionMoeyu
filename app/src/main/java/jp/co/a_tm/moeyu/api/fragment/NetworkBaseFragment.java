package jp.co.a_tm.moeyu.api.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import jp.co.a_tm.moeyu.R;

public abstract class NetworkBaseFragment extends Fragment {
    protected View mIndicator;
    protected View mNetworkError;
    protected boolean mRetry = true;

    public abstract void execute();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container);
        this.mIndicator = view.findViewById(R.id.indicator);
        this.mNetworkError = view.findViewById(R.id.network_error);
        this.mNetworkError.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NetworkBaseFragment.this.mNetworkError.setVisibility(View.INVISIBLE);
                if (NetworkBaseFragment.this.mRetry) {
                    NetworkBaseFragment.this.execute();
                }
            }
        });
        return view;
    }

    public void setRetry(boolean retry) {
        this.mRetry = retry;
    }
}

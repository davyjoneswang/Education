package com.education;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class SmartRecomentFragment extends CommonFragment implements OnClickListener{

    private static final String TAG = SmartRecomentFragment.class.getSimpleName();

    private Button mStartBtn;
    private ImageView mGuang;
    ObjectAnimator animator3;
	/**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_smart, container, false);
        mStartBtn  = (Button) v.findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mGuang = (ImageView)v.findViewById(R.id.guang);
        animator3 = ObjectAnimator.ofFloat(mGuang, "rotation", 0F, 359F);
        //animator3.setRepeatCount(ValueAnimator.INFINITE);
        //animator3.setDuration(3000);
        //animator3.setRepeatMode(ValueAnimator.RESTART);
        animator3.start();
        return v;
    }
    
    @Override
    public void onDestroyView() {
    	animator3.cancel();
    	super.onDestroyView();
    }
	@Override
	protected String getLogTag() {
		return TAG;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.start_btn) {
			Intent intent = new Intent(getActivity(),SmartRecommentActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
}
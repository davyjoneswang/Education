package com.education;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.education.TestActivity.Item2;
import com.education.common.AppHelper;
import com.education.common.FastJsonRequest;
import com.education.common.VolleyErrorListener;
import com.education.common.VolleyResponseListener;
import com.education.entity.CollegeItem;
import com.education.entity.ErrorData;
import com.education.entity.MajorItem;
import com.education.entity.ShaiXuanConditionItem;
import com.education.entity.ShaiXuanJieGuo;
import com.education.entity.User;
import com.education.utils.LogUtil;
import com.education.widget.SimpleBlockedDialogFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ManualTimFragment extends CommonFragment implements PullToRefreshBase.OnRefreshListener2<ListView>,
		View.OnClickListener {
	private static final String TAG = ManualTimFragment.class.getSimpleName();
	private SimpleBlockedDialogFragment mBlockedDialogFragment = SimpleBlockedDialogFragment
			.newInstance();
	private PullToRefreshListView mSearchResulListView;
    private ListView mMajorResultListView;;
	private List<SchoolItem> mItemList = new ArrayList<SchoolItem>();

	protected LayoutInflater mInflater;
	protected static Resources mResources;
	private ItemAdapter mItemAdapter;
	private View mFilterLayout;
    private TextView mScoreRankText, mSchoolRankText,
			mUserScoreText, mUserRankText, mUserTypeText, mUserLocation,
			mSchoolNumbersText, mMajorNumbersText;
	private Intent mShaixuanIntent;
	private ImageView mScoreRankImg, mSchoolRankImg;
	private int clickPinyinNumbers, clickSchoolNumbers;
	private String mLuquQingkuang;
	private String back2ShaiXuanActivityLuquQingkuang;
    private List<CollegeItem> mSchoolList = new ArrayList<CollegeItem>();//用于记录分页
	private int mMajorNumbers,mSchoolNumbers;
	private ArrayList<ShaiXuanConditionItem> conditionItemList;
	private Activity mActivity;
	private User mUser;
	private EditText mTitleSearchEdit;
	private String mLuqupici, mLuquqingkuang;

    private static final int PAGE_START = 1;
    private int mNextPageNo = PAGE_START;
    private String sKey = "";

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResources = getResources();
		mActivity = getActivity();
		mInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUser = User.getInstance();
	}

	/**
	 * The Fragment's UI is just a simple text view showing its instance number.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_main_manual, container,
				false);
		initView(v);

		mItemAdapter = new ItemAdapter();
		mShaixuanIntent = new Intent(mActivity, ShaiXuanActivity.class);
		mSearchResulListView.setAdapter(mItemAdapter);
		
		displayCollege();
		mFilterLayout.setOnClickListener(this);
		mScoreRankText.setOnClickListener(this);
		mSchoolRankText.setOnClickListener(this);
		mLuqupici = getLuqupici(null);
		mLuquQingkuang = getLuquQingkuang(new Intent());

        mSearchResulListView.setRefreshing(true);
		return v;
	}

	private void initView(View v) {
		mSearchResulListView = (PullToRefreshListView) v
				.findViewById(R.id.filter_school_result_list);
        mSearchResulListView.setOnRefreshListener(this);
        mSearchResulListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

		mMajorResultListView = (ListView) v
				.findViewById(R.id.filter_major_result_list);
		mFilterLayout = v.findViewById(R.id.shaixuan_layout);
		mScoreRankText = (TextView) v.findViewById(R.id.paixu_pinyin);
		mSchoolRankText = (TextView) v.findViewById(R.id.paiming_school);
		mScoreRankImg = (ImageView) v.findViewById(R.id.paixu_pinyin_img);
		mSchoolRankImg = (ImageView) v.findViewById(R.id.paiming_school_img);
		mUserScoreText = (TextView) v.findViewById(R.id.user_score);
		mUserRankText = (TextView) v.findViewById(R.id.user_rank);
		mUserTypeText = (TextView) v.findViewById(R.id.user_type);
		mUserLocation = (TextView) v.findViewById(R.id.user_location);
		mSchoolNumbersText = (TextView) v.findViewById(R.id.school_numbers);
		mMajorNumbersText = (TextView) v.findViewById(R.id.major_numbers);
		mTitleSearchEdit = (EditText) getActivity().getActionBar()
				.getCustomView().findViewById(R.id.search_school_major_edit);

		mTitleSearchEdit
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == KeyEvent.ACTION_DOWN
								|| actionId == KeyEvent.KEYCODE_ENTER
                                || actionId == KeyEvent.KEYCODE_ENDCALL
                                || actionId == KeyEvent.KEYCODE_BACK) {
                            sKey = v.getText()
                                    .toString().trim();
                            mNextPageNo = 1;
							postData2Server(mNextPageNo, conditionItemList, sKey, false);
						}
						return false;
					}
				});
//		mTitleSearchEdit.addTextChangedListener(this);

		User user = User.getInstance();
		if (user != null) {
			Log.w("wutl", user.toString());
			mUserScoreText.setText(user.getKscj() + "");
			mUserRankText.setText(user.getKspw() + "");
			int kskl = user.getKskl();
			mUserTypeText.setText(kskl == 1 ? "理工" : "文史");
			mUserLocation.setText(user.getKskqName() + "");
		}
	}

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        mNextPageNo = 1;
        postData2Server(mNextPageNo , conditionItemList, sKey, true);
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        postData2Server(mNextPageNo , conditionItemList, sKey, false);
    }

	private void setDataSource(ShaiXuanJieGuo result,boolean isFirstInto) {
		mMajorNumbers = 0;
		mItemList.clear();
		for (int i = 0; i < mSchoolList.size(); i++) {
			SchoolItem localItem = new SchoolItem(mSchoolList.get(i).getZysl(),
                    mSchoolList.get(i).getYxmc());
			mMajorNumbers += Integer.valueOf(mSchoolList.get(i).getZysl())
					.intValue();
			setSchoolImg(localItem, i % 3);
			localItem.setYxdh(mSchoolList.get(i).getYxdh());
			mItemList.add(localItem);
		}
		mItemAdapter.notifyDataSetChanged();
		mSchoolNumbers=mItemAdapter.getCount();
		mSchoolNumbersText.setText(String.format(
				getResources().getString(R.string.school_numbers),
				String.valueOf(mSchoolNumbers)));
		mMajorNumbersText.setText(String.format(
				getResources().getString(R.string.major_numbers),
				String.valueOf(mMajorNumbers)));
	}

	private void setSchoolImg(SchoolItem localItem, int position) {
		switch (position) {
		case 0:
			localItem.setSchoolImg(R.drawable.xuexiao_1);
			break;
		case 1:
			localItem.setSchoolImg(R.drawable.xuexiao_2);
			break;
		case 2:
			localItem.setSchoolImg(R.drawable.xuexiao_3);
			break;
		default:
			localItem.setSchoolImg(R.drawable.xuexiao_2);
			break;
		}
	}

	private void displayCollege() {
		mSearchResulListView.setOnItemClickListener(mItemAdapter);
		mItemAdapter.notifyDataSetChanged();
	}

	private static class ViewHolder {
		TextView schoolNameTextView;
		TextView majorNumberTextView;
		ImageView schoolImg;
		View dividerView;
	}

	private class ItemAdapter extends BaseAdapter implements
			AdapterView.OnItemClickListener {
		public int getCount() {
			return mItemList.size();
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_college, null,
						false);
				holder = new ViewHolder();
				holder.dividerView = convertView.findViewById(R.id.divider);
				holder.schoolNameTextView = (TextView) convertView
						.findViewById(R.id.item_title);
				holder.majorNumberTextView = (TextView) convertView
						.findViewById(R.id.desc);
				holder.schoolImg = (ImageView) convertView
						.findViewById(R.id.icon);
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
						AbsListView.LayoutParams.MATCH_PARENT,
						mResources.getDimensionPixelSize(R.dimen.dimen_34_dip));
				convertView.setLayoutParams(lp);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			convertView.setTag(holder);

			SchoolItem schoolItem = mItemList.get(position);
			holder.schoolImg.setImageBitmap(BitmapFactory.decodeResource(
					mResources, schoolItem.getSchoolImg()));
			holder.schoolNameTextView.setText(schoolItem.getSchoolName());
			holder.majorNumberTextView.setText(schoolItem.getMarjorNumber());
			return convertView;
		}

		public Object getItem(int position) {
			return mItemList.get(position);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			//String yxdh = mItemList.get(position).getYxdh();
			String luqupic = mLuqupici;
			String lqqk = mLuquQingkuang;

			SchoolItem schoolItem = (SchoolItem) parent.getAdapter().getItem(
					position);
			
			Log.i("TAG", schoolItem.yxdh);
			Intent intent = new Intent(getActivity(), ManulSearchSchollDetailActivity.class);
			intent.putExtra("lqpc", mLuqupici);
			intent.putExtra("lqqk", mLuquQingkuang);
			intent.putExtra("yxdh", schoolItem.yxdh);
			startActivity(intent);
		}
	}

	private class SchoolItem {
		protected int schoolImg;
		protected String marjorNumber;
		protected String schoolName;
		protected String yxdh;

		public void setYxdh(String yxdh) {
			this.yxdh = yxdh;
		}

		public String getYxdh() {
			return yxdh;
		}

		public SchoolItem(String marjorNumber, String schoolName) {
			super();
			this.marjorNumber = marjorNumber;
			this.schoolName = schoolName;
		}

		public String getMarjorNumber() {
			return marjorNumber;
		}

		public void setSchoolName(String schoolName) {
			this.schoolName = schoolName;
		}

		public void setMarjorNumber(String marjorNumber) {
			this.marjorNumber = marjorNumber;
		}

		public void setSchoolImg(int schoolImg) {
			this.schoolImg = schoolImg;
		}

		public int getSchoolImg() {
			return schoolImg;
		}

		public String getSchoolName() {
			return schoolName;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		// 获取筛选条件
		if (requestCode == 1 && resultCode == 1) {
            sKey = "";
			Bundle bundle = data
					.getBundleExtra(ShaiXuanActivity.SHAIXUAN_RESULT_TAG);
			// ArrayList<ShaiXuanConditionItem> conditionItemList =
			// bundle.getParcelableArrayList(ShaiXuanActivity.SHAIXUAN_RESULT_TAG);
			conditionItemList = (ArrayList<ShaiXuanConditionItem>) bundle
					.getSerializable(ShaiXuanActivity.SHAIXUAN_RESULT_TAG);
			mLuquQingkuang = getLuquQingkuang(data);

			back2ShaiXuanActivityLuquQingkuang=data.getStringExtra("luquqingkuang_detial_condition");
			
            mNextPageNo = PAGE_START;
			postData2Server(mNextPageNo, conditionItemList, "", false);
			Log.w("wutl", "录取情况＝" + mLuquQingkuang);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String getLuquQingkuang(Intent data) {
		String year = data.getStringExtra("year_score");
		String low_score = data.getStringExtra("low_score");
		String high_score = data.getStringExtra("high_score");

		if (TextUtils.isEmpty(year)) {
			Calendar c = Calendar.getInstance(Locale.getDefault());
			year = String.valueOf(c.get(Calendar.YEAR) - 1);
		}
		if (TextUtils.isEmpty(low_score))
			low_score = String.valueOf(mUser.getKscj() - 20);

		if (TextUtils.isEmpty(high_score))
			high_score = String.valueOf(mUser.getKscj() + 20);

		mLuquQingkuang = year + "|" + data.getIntExtra("score_type", 1) + "|"
				+ low_score + "|" + high_score;
		Log.i(TAG, "getLuquQingkuang mLuquqingkuang=" + mLuquqingkuang);
		return mLuquQingkuang;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void postData2Server(final int pageNo,
			final ArrayList<ShaiXuanConditionItem> conditionlist,
			final String skey, final boolean isFirstInto) {
        Log.d("postData2Server", "pageNo: " + pageNo, new Throwable());

        //展示家在对话框
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
		mBlockedDialogFragment.show(ft, "block_dialog");
		
		final FastJsonRequest request = new FastJsonRequest(
				Request.Method.POST, Url.SHAI_XUAN, null,
				new VolleyResponseListener(mActivity) {
					@Override
					public void onSuccessfulResponse(JSONObject response,
							boolean success) {
                        if (mSearchResulListView.getVisibility() == View.VISIBLE) {
                            mSearchResulListView.onRefreshComplete();
                        }
						if (success) {
							mBlockedDialogFragment.dismissAllowingStateLoss();
							String datas = response.getString("datas");
							ShaiXuanJieGuo result = JSON.parseObject(datas,
									ShaiXuanJieGuo.class);
							if (result != null) {
                                List<CollegeItem> collegeItemList = result.getYxzydata();
                                if (pageNo == PAGE_START) {
                                    mSchoolList.clear();
                                }

                                if (collegeItemList.size() > 0) {
                                    mSchoolList.addAll(collegeItemList);
                                    mNextPageNo++;
                                    mSearchResulListView.setMode(PullToRefreshBase.Mode.BOTH);
                                } else {
                                    mSearchResulListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                                }

                                setDataSource(result,isFirstInto);
                                mItemAdapter.notifyDataSetChanged();
							}
							Log.d("wutl", "resutl=" + result.toString());
						} else {
							ErrorData errorData = AppHelper
									.getErrorData(response);
							Toast.makeText(mActivity, errorData.getText(),
									Toast.LENGTH_SHORT).show();
						}
					}
				}, new VolleyErrorListener() {
					@Override
					public void onVolleyErrorResponse(VolleyError volleyError) {
						mBlockedDialogFragment.dismissAllowingStateLoss();
                        if (mSearchResulListView.getVisibility() == View.VISIBLE) {
                            mSearchResulListView.onRefreshComplete();
                        }
						LogUtil.logNetworkResponse(volleyError, "wutl");
						Toast.makeText(
								mActivity,
								getResources().getString(
										R.string.internet_exception),
								Toast.LENGTH_SHORT).show();
					}
				}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				User user = User.getInstance();
				Map<String, String> map = new HashMap<String, String>();
				if (isFirstInto) {
					map.put("pageno", String.valueOf(1));
					map.put("skey", skey);
//					map.put("yxss", "");
//					map.put("yxlx", "");
//					map.put("yxxz", "");


                    map.put("yxss", getYxss(conditionlist));
                    map.put("yxlx", getYxlx(conditionlist));
                    map.put("yxxz", getYxxz(conditionlist));


					map.put("lqpc", mLuqupici);


//                    map.put("lqqk", getLuquQingkuang(new Intent()));
                    map.put("lqqk", mLuquQingkuang);


					map.put("kskl", String.valueOf(user.getKskl()));
					map.put("kqdh", String.valueOf(user.getKqdh()));
				} else {
					map.put("pageno", String.valueOf(pageNo));
					map.put("skey", skey);
					map.put("yxss", getYxss(conditionlist));
					map.put("yxlx", getYxlx(conditionlist));
					map.put("yxxz", getYxxz(conditionlist));
					map.put("lqpc", getLuqupici(conditionlist));
					map.put("lqqk", mLuquQingkuang);
					map.put("kskl", String.valueOf(user.getKskl()));
					map.put("kqdh", String.valueOf(user.getKqdh()));
				}
				Log.w("wutl", "map="
						+ AppHelper.makeSimpleData("search", map).toString());
				return AppHelper.makeSimpleData("search", map);
			}
		};
		EduApp.sRequestQueue.add(request);
	}

	public static String getYxss(ArrayList<ShaiXuanConditionItem> conditionlist) {
		StringBuffer buffer = new StringBuffer();
		if (conditionlist != null) {
			for (ShaiXuanConditionItem item : conditionlist) {
				if (item.getConditionName().equals("院校省份")) {
					if (item.getmSubDetailConditionItemList() != null) {
						for (int i = 0; i < item
								.getmSubDetailConditionItemList().size(); i++) {
							buffer.append(item.getmSubDetailConditionItemList()
									.get(i).getProviceId());
							if (i != item.getmSubDetailConditionItemList()
									.size() - 1)
								buffer.append("|");
						}
					}

					break;
				}
			}
		}
		Log.w("wutl", "院校省份＝" + buffer.toString());
		if (TextUtils.isEmpty(buffer.toString()))
			return "";
		if(conditionlist!=null&&conditionlist.size()==mResources.getStringArray(R.array.check_all_province_name).length-1)
			return "";
		return buffer.toString();
	}

    public static String getYxlx(ArrayList<ShaiXuanConditionItem> conditionlist) {
		StringBuffer buffer = new StringBuffer();
		if (conditionlist != null) {
			for (ShaiXuanConditionItem item : conditionlist) {
				if (item.getConditionName().equals("院校类型")) {
					if (item.getmSubDetailConditionItemList() != null) {
						for (int i = 0; i < item
								.getmSubDetailConditionItemList().size(); i++) {
							buffer.append(item.getmSubDetailConditionItemList()
									.get(i).getProviceId());
							if (i != item.getmSubDetailConditionItemList()
									.size() - 1)
								buffer.append("|");
						}
					}
					break;
				}
			}
		}
		Log.w("wutl", "院校类型＝" + buffer.toString());
		if (TextUtils.isEmpty(buffer.toString()))
			return "";
		if(conditionlist!=null&&conditionlist.size()==mResources.getStringArray(R.array.check_all_college_type_name).length-1)
			return "";
		return buffer.toString();
	}

    public static String getYxxz(ArrayList<ShaiXuanConditionItem> conditionlist) {
		StringBuffer buffer = new StringBuffer();
		if (conditionlist != null) {
			for (ShaiXuanConditionItem item : conditionlist) {
				if (item.getConditionName().equals("院校性质")) {
					if (item.getmSubDetailConditionItemList() != null) {
						for (int i = 0; i < item
								.getmSubDetailConditionItemList().size(); i++) {
							buffer.append(item.getmSubDetailConditionItemList()
									.get(i).getProviceId());
							if (i != item.getmSubDetailConditionItemList()
									.size() - 1)
								buffer.append("|");
						}
					}
					break;
				}
			}
		}
		Log.w("wutl", "院校性质＝" + buffer.toString());
		if (TextUtils.isEmpty(buffer.toString()))
			return "";
		if(conditionlist!=null&&conditionlist.size()==mResources.getStringArray(R.array.check_all_college_property_name).length-1)
			return "";
		return buffer.toString();
	}

	private String getLuqupici(ArrayList<ShaiXuanConditionItem> conditionlist) {
		StringBuffer buffer = new StringBuffer();
		if (conditionlist != null) {
			for (ShaiXuanConditionItem item : conditionlist) {
				if (item.getConditionName().equals("录取批次")) {
					if (item.getmSubDetailConditionItemList() != null) {
						for (int i = 0; i < item
								.getmSubDetailConditionItemList().size(); i++) {
							buffer.append(item.getmSubDetailConditionItemList()
									.get(i).getProviceId());
							if (i != item.getmSubDetailConditionItemList()
									.size() - 1)
								buffer.append("|");
						}
					}
					break;
				}
			}
		}
		Log.w("wutl", "录取批次＝" + buffer.toString());
		if (TextUtils.isEmpty(buffer.toString())) {
			mLuqupici = "3";
			return mLuqupici;
		}
		mLuqupici = buffer.toString();
		return mLuqupici;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.shaixuan_layout:
//			Bundle bundle = new Bundle();
//			bundle.putSerializable(ShaiXuanActivity.SHAIXUAN_RESULT_TAG, conditionItemList);
//			mShaixuanIntent.putExtra(ShaiXuanActivity.SHAIXUAN_RESULT_TAG, bundle);
			
//			mShaixuanIntent.putExtra(ShaiXuanActivity.SHAIXUAN_RESULT_TAG, (Serializable)conditionItemList);
			
			if (conditionItemList != null) {
				//放入省份
				if (conditionItemList.get(0) != null)
					mShaixuanIntent.putExtra(ShaiXuanActivity.SHENGFEN,
							(Serializable) conditionItemList.get(0));
				//放入院校类型
				if (conditionItemList.get(1) != null)
					mShaixuanIntent.putExtra(ShaiXuanActivity.YUANXIAO_LEIXING,
							(Serializable) conditionItemList.get(1));
				//放入院校性质
				if (conditionItemList.get(2) != null)
					mShaixuanIntent.putExtra(ShaiXuanActivity.YUANXIAO_XINGZHI,
							(Serializable) conditionItemList.get(2));
				//放入录取批次
				if (conditionItemList.get(3) != null)
					mShaixuanIntent.putExtra(ShaiXuanActivity.LUQU_PICI,
							(Serializable) conditionItemList.get(3));
				//放入历年录取情况
				if (conditionItemList.get(4) != null)
					mShaixuanIntent.putExtra(ShaiXuanActivity.LUQUQINGKUANG,
							(Serializable) conditionItemList.get(4));
			}

			mShaixuanIntent.putExtra("LU_QU_QING_KUANG", mLuquQingkuang);
			mShaixuanIntent.putExtra("luquqingkuang_detial_condition",
					back2ShaiXuanActivityLuquQingkuang);
			startActivityForResult(mShaixuanIntent, 1);
			break;
		case R.id.paixu_pinyin:
			clickPinyinNumbers++;
			if (clickPinyinNumbers > 2)
				clickPinyinNumbers = 0;
			switchPinyinImgState(clickPinyinNumbers);
			break;
		case R.id.paiming_school:
			clickSchoolNumbers++;
			if (clickSchoolNumbers > 2)
				clickSchoolNumbers = 0;
			switchSchoolImgState(clickSchoolNumbers);
			break;
		}
	}

	private void switchPinyinImgState(int number) {
		switch (number) {
		case 0:
			mScoreRankImg.setImageResource(R.drawable.paixu_normal);
			break;
		case 1:
			mScoreRankImg.setImageResource(R.drawable.paixu_up);
			break;
		case 2:
			mScoreRankImg.setImageResource(R.drawable.paixu_down);
			break;
		}
	}

	private void switchSchoolImgState(int number) {
		switch (number) {
		case 0:
			mSchoolRankImg.setImageResource(R.drawable.paixu_normal);
			break;
		case 1:
			mSchoolRankImg.setImageResource(R.drawable.paixu_up);
			break;
		case 2:
			mSchoolRankImg.setImageResource(R.drawable.paixu_down);
			break;
		}
	}

	@Override
	protected String getLogTag() {
		return TAG;
	}
}
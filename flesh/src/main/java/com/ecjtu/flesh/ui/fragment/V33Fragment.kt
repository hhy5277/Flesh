package com.ecjtu.flesh.ui.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ecjtu.flesh.R
import com.ecjtu.flesh.cache.impl.MenuListCacheHelper
import com.ecjtu.flesh.cache.impl.V33CacheHelper
import com.ecjtu.flesh.model.models.V33Model
import com.ecjtu.flesh.presenter.MainActivityDelegate
import com.ecjtu.flesh.ui.adapter.TabPagerAdapter
import com.ecjtu.flesh.ui.adapter.VideoTabPagerAdapter
import com.ecjtu.netcore.model.MenuModel
import com.ecjtu.netcore.network.AsyncNetwork
import com.ecjtu.netcore.network.IRequestCallback
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import kotlin.concurrent.thread

/**
 * Created by Ethan_Xiang on 2018/2/8.
 */
class V33Fragment : Fragment() {
    companion object {
        private const val TAG = "V33Fragment"
    }
    private var delegate: MainActivityDelegate? = null
    private var mViewPager: ViewPager? = null
    private var mTabLayout: TabLayout? = null

    private var mLoadingDialog: AlertDialog? = null
    private var mV33Menu: List<MenuModel>? = null
    private var mV33Cache: Map<String, List<V33Model>>? = null

    fun setDelegate(delegate: MainActivityDelegate) {
        this.delegate = delegate
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG,"onCreateView")
        return inflater?.inflate(R.layout.fragment_mzitu, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"onViewCreated")
        initView()
    }

    protected fun initView() {
        mViewPager = view!!.findViewById(R.id.view_pager) as ViewPager?
        mTabLayout = delegate?.getTabLayout()
        if(userVisibleHint){
            attachTabLayout()
        }
    }

    private fun attachTabLayout() {
        mTabLayout?.removeAllTabs()
        mTabLayout?.setupWithViewPager(mViewPager)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.i(TAG, "setUserVisibleHint " + isVisibleToUser)
        if (isVisibleToUser) {
            attachTabLayout()
            if (mV33Menu == null || mV33Menu?.size == 0) {
                val req = AsyncNetwork().apply {
                    request(com.ecjtu.flesh.Constants.V33_URL, null)
                    setRequestCallback(object : IRequestCallback {
                        override fun onSuccess(httpURLConnection: HttpURLConnection?, response: String) {
                            val menuModel = arrayListOf<MenuModel>()
                            val map = linkedMapOf<String, List<V33Model>>()
                            try {
                                val jObj = JSONArray(response)
                                for (i in 0 until jObj.length()) {
                                    val jTitle = jObj[i] as JSONObject
                                    val title = jTitle.optString("title")
                                    val list = jTitle.optJSONArray("list")
                                    val modelList = arrayListOf<V33Model>()
                                    for (j in 0 until list.length()) {
                                        val v33Model = V33Model()
                                        val jItem = list[j] as JSONObject
                                        v33Model.baseUrl = jItem.optString("baseUrl")
                                        v33Model.imageUrl = jItem.optString("imageUrl")
                                        v33Model.title = jItem.optString("title")
                                        v33Model.videoUrl = jItem.optString("videoUrl")
                                        modelList.add(v33Model)
                                    }
                                    map.put(title, modelList)
                                    val model = MenuModel(title, "")
                                    menuModel.add(model)
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }

                            mLoadingDialog?.cancel()
                            mLoadingDialog = null
                            activity.runOnUiThread {
                                if (mViewPager != null && mViewPager?.adapter == null) {
                                    mViewPager?.adapter = VideoTabPagerAdapter(menuModel, mViewPager!!)
                                    (mViewPager?.adapter as VideoTabPagerAdapter).setMenuChildList(map)
                                    (mViewPager?.adapter as VideoTabPagerAdapter?)?.notifyDataSetChanged(true)
                                } else {
                                    (mViewPager?.adapter as VideoTabPagerAdapter).menu = menuModel
                                    (mViewPager?.adapter as VideoTabPagerAdapter).setMenuChildList(map)
                                    (mViewPager?.adapter as VideoTabPagerAdapter?)?.notifyDataSetChanged(false)
                                }
                                mV33Menu = menuModel
                                mV33Cache = map
//                                delegate?.recoverTab(delegate?.getLastTabItem(VideoTabPagerAdapter::class) ?: 0,
//                                        delegate?.isAppbarLayoutExpand() ?: false)
                                if (userVisibleHint) {
                                    attachTabLayout()
                                }
                            }
                        }
                    })
                }
                if (mLoadingDialog == null) {
                    mLoadingDialog = AlertDialog.Builder(context).setTitle("加载中").setMessage("需要一小会时间")
                            .setNegativeButton("取消", { dialog, which ->
                                thread {
                                    req.cancel()
                                }
                            })
                            .setCancelable(false)
                            .setOnCancelListener {
                                mLoadingDialog = null
                            }.create()
                    mLoadingDialog?.show()
                }
                thread {
                    val helper = V33CacheHelper(context.filesDir.absolutePath)
                    val helper2 = MenuListCacheHelper(context.filesDir.absolutePath)
                    mV33Menu = helper2.get("v33menu")
                    mV33Cache = helper.get("v33cache")
                    val localMenu = mV33Menu
                    val localCache = mV33Cache
                    if (localMenu != null && localCache != null) {
                        mLoadingDialog?.cancel()
                        mLoadingDialog = null
                    }
                    activity.runOnUiThread {
                        if (localMenu != null && localCache != null) {
                            if (mViewPager != null && mViewPager?.adapter == null) {
                                mViewPager?.adapter = VideoTabPagerAdapter(localMenu, mViewPager!!)
                                (mViewPager?.adapter as VideoTabPagerAdapter).setMenuChildList(localCache as MutableMap<String, List<V33Model>>)
                                (mViewPager?.adapter as VideoTabPagerAdapter?)?.notifyDataSetChanged(true)
                            } else {
                                (mViewPager?.adapter as VideoTabPagerAdapter).menu = localMenu
                                (mViewPager?.adapter as VideoTabPagerAdapter).setMenuChildList(localCache as MutableMap<String, List<V33Model>>)
                                (mViewPager?.adapter as VideoTabPagerAdapter?)?.notifyDataSetChanged(false)
                            }
//                            delegate?.recoverTab(delegate?.getLastTabItem(VideoTabPagerAdapter::class) ?: 0,
//                                    delegate?.isAppbarLayoutExpand() ?: false)
                            if (userVisibleHint) {
                                attachTabLayout()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop tabIndex " + mTabLayout?.selectedTabPosition)
        mViewPager?.let {
            (mViewPager?.adapter as TabPagerAdapter?)?.onStop(context, mTabLayout?.selectedTabPosition ?: 0,
                    delegate?.isAppbarLayoutExpand() ?: false)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume " + mTabLayout?.selectedTabPosition)
        mViewPager?.adapter?.let {
            (mViewPager?.adapter as TabPagerAdapter).onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy " + mTabLayout?.selectedTabPosition)
        mViewPager?.let {
            (mViewPager?.adapter as TabPagerAdapter?)?.onDestroy()
        }
    }
}

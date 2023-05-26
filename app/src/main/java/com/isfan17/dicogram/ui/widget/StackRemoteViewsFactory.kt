package com.isfan17.dicogram.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.isfan17.dicogram.R

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<Bitmap>()

    override fun onDataSetChanged() {
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_d))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_i))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_c))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_o))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_g))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_r))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_a))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.dicogram_m))
    }

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])

        val extras = bundleOf(
            StoriesWidget.EXTRA_ITEM to position
        )

        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)

        return rv
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false

    override fun onCreate() {}

    override fun onDestroy() {}
}

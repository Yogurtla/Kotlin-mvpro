/*
 * Copyright (C) 2017 Ricky.yao https://github.com/vihuela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package com.github.kotlin_mvpro.ui.presenter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import com.github.kotlin_mvpro.api.Api
import com.github.kotlin_mvpro.api.ApiCacheProvider
import com.github.kotlin_mvpro.api.ApiUtils
import com.github.kotlin_mvpro.model.ImageItem
import com.github.kotlin_mvpro.ui.view.IImageFragment
import com.github.library.base.BasePresenter
import com.github.library.utils.defThread
import com.hitomi.glideloader.GlideImageLoader
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator
import com.hitomi.tilibrary.transfer.TransferConfig
import com.hitomi.tilibrary.transfer.Transferee
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import github.library.parser.ExceptionParseMgr
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictDynamicKey

class ImageFragmentPresenter : BasePresenter<IImageFragment>() {

    lateinit var transferConfig: TransferConfig
    lateinit var transferee: Transferee

    override fun onViewCreated(view: IImageFragment, arguments: Bundle?, savedInstanceState: Bundle?) {
        transferee = Transferee.getDefault(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        transferee.destroy()
    }

    fun openImageDetail(iv: ImageView, item: ImageItem, position: Int) {
        val imageViewList = arrayListOf<ImageView>().apply { add(iv) }
        val imagesList = arrayListOf<ImageItem>().apply { add(item) }

        transferConfig = TransferConfig.build()
                .setSourceImageList(imagesList.map { it.url })//SourceImageList\OriginImageList size一致
                .setOriginImageList(imageViewList)
                .setMissDrawable(ColorDrawable(Color.parseColor("#DCDDE1")))
                .setErrorDrawable(ColorDrawable(Color.parseColor("#DCDDE1")))
                .setProgressIndicator(ProgressPieIndicator())
                .setNowThumbnailIndex(0)//仅一张
                .setImageLoader(GlideImageLoader.with(context.applicationContext))
                .create()
        transferee.apply(transferConfig).show()
    }

    fun getImageList(page: Int = 1, loadMore: Boolean) {
        val api = Api.IMPL.getImageList(Api.pageSize, page)
        ApiCacheProvider.IMPL.getImageList(api, DynamicKey(page), EvictDynamicKey(ApiUtils.isRxCacheEvict))
                .defThread()
                .bindToLifecycle(this)
                .doOnSubscribe { view()!!.showLoading() }
                .observeOn(Schedulers.io())
                .flatMap {
                    val data_ok = !it.data.error && it.data.results.isNotEmpty()
                    if (data_ok) Observable.just(it) else Observable.error(IllegalArgumentException("server business error"))
                }
                .map { it.data.results.map { ImageItem(it.url) } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view()!!.setData(it, loadMore)
                },
                        {
                            ExceptionParseMgr.Instance.parseException(it, { error, message -> view()!!.setMessage(error, message) })
                        }, { view()!!.hideLoading() })
    }
}
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

package com.github.kotlin_mvpro.ui.base

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.github.kotlin_mvpro.R
import com.github.library.base.BaseBindingFragment
import com.github.library.base.BasePresenter
import com.kennyc.view.MultiStateView
import org.jetbrains.anko.findOptional

abstract class BaseFragment<T : BasePresenter<*>, B : ViewDataBinding> : BaseBindingFragment<T, B>() {

    var mMultiStateView: MultiStateView? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mMultiStateView = view.findOptional<MultiStateView>(R.id.multiStateView)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMultiStateView = null
    }

    override fun showLoading() {
        super.showLoading()
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_LOADING
    }

    override fun showMessageFromNet(error: Any, content: String) {
        super.showMessageFromNet(error, content)
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_ERROR
        val errorView = mMultiStateView?.getView(MultiStateView.VIEW_STATE_ERROR)?.findOptional<TextView>(R.id.tv)
        val retryButton = mMultiStateView?.getView(MultiStateView.VIEW_STATE_ERROR)?.findOptional<Button>(R.id.retry)
        errorView?.text = content
        retryButton?.setOnClickListener { onRetryListener() }
    }

    override fun showEmpty() {
        super.showEmpty()
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_EMPTY
    }

    override fun showContent() {
        super.showContent()
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_CONTENT
    }

    override fun showMessage(content: String) {
        super.showMessage(content)
        ToastUtils.showShortSafe(content)
    }

    override fun hideLoading() {
        super.hideLoading()
        showContent()
    }

    open fun onRetryListener() = Unit

}

package com.artworkspace.habittracker

import android.app.Application
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.fa.createFontAwesomeIconPack
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    var iconPack: IconPack? = null

    override fun onCreate() {
        super.onCreate()
        loadIconPack()
    }

    /**
     * Load icon pack for IconDialog
     */
    private fun loadIconPack() {
        val loader = IconPackLoader(this)
        val iconPack = createFontAwesomeIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        this.iconPack = iconPack
    }
}
package com.example.semsemgallery.activities.pictureview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.semsemgallery.R
import com.example.semsemgallery.activities.main.adapter.PictureAdapter
import com.example.semsemgallery.activities.pictureview.fragment.MetaDataBottomSheet
import com.example.semsemgallery.domain.PhotoEditorHelper.Companion.PESDK_RESULT
import com.example.semsemgallery.models.Picture
import com.google.android.material.appbar.MaterialToolbar
import ly.img.android.pesdk.PhotoEditorSettingsList
import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic
import ly.img.android.pesdk.assets.font.basic.FontPackBasic
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes
import ly.img.android.pesdk.backend.model.EditorSDKResult
import ly.img.android.pesdk.backend.model.constant.OutputMode
import ly.img.android.pesdk.backend.model.state.LoadSettings
import ly.img.android.pesdk.backend.model.state.PhotoEditorSaveSettings
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder
import ly.img.android.pesdk.ui.model.state.UiConfigFilter
import ly.img.android.pesdk.ui.model.state.UiConfigFrame
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay
import ly.img.android.pesdk.ui.model.state.UiConfigSticker
import ly.img.android.pesdk.ui.model.state.UiConfigText
import ly.img.android.pesdk.ui.panels.item.PersonalStickerAddItem
import ly.img.android.serializer._3.IMGLYFileWriter
import java.io.File
import java.io.IOException

class PictureViewActivity : AppCompatActivity() {
    companion object {
        const val PESDK_RESULT = 1;
    }
    private lateinit var topBar: MaterialToolbar
    private fun createPesdkSettingsList() =
        PhotoEditorSettingsList(false)
            .configure<UiConfigFilter> {
                it.setFilterList(FilterPackBasic.getFilterPack())
            }
            .configure<UiConfigText> {
                it.setFontList(FontPackBasic.getFontPack())
            }
            .configure<UiConfigFrame> {
                it.setFrameList(FramePackBasic.getFramePack())
            }
            .configure<UiConfigOverlay> {
                it.setOverlayList(OverlayPackBasic.getOverlayPack())
            }
            .configure<UiConfigSticker> {
                it.setStickerLists(
                    PersonalStickerAddItem(),
                    StickerPackEmoticons.getStickerCategory(),
                    StickerPackShapes.getStickerCategory()
                )
            }
            .configure<PhotoEditorSaveSettings> {
                it.setOutputToGallery()
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_view)
        val pictureList = intent.getParcelableArrayListExtra<Picture>("pictureList")
        val position = intent.getIntExtra("position", 0)

        topBar = findViewById(R.id.activity_picture_view_topAppBar)

        val viewPager: ViewPager2 = findViewById(R.id.vp_image)
        val adapter = PictureAdapter(this, pictureList, position)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(position, false)

        val filePath = pictureList!![position].path
        val fileName = pictureList[position].fileName
        val date = pictureList[position].dateAdded
        Log.e("FILEPATH", filePath)
        Log.e("DATE", date.toString())

        val infoBtn: ImageButton = findViewById(R.id.info_button)
        infoBtn.setOnClickListener {
            val botSheetFrag = MetaDataBottomSheet(filePath, fileName)
            botSheetFrag.show(supportFragmentManager, botSheetFrag.tag)
        }

        // Event share to other apps
        val shareBtn: ImageButton = findViewById(R.id.share_button)
        shareBtn.setOnClickListener {
            Log.d("Image Path", filePath)
            val imageFile = File(filePath)
            val imageUri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                imageFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission

            startActivity(Intent.createChooser(shareIntent, "Share image via..."))
        }

        // Event edit image
        val editBtn: ImageButton = findViewById(R.id.edit_picture_button)
        editBtn.setOnClickListener {
            val imageFile = File(filePath)
            val imageUri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                imageFile
            )
            openEditor(imageUri)
        }
    }
    private fun openEditor(imageUri: Uri) {
        // Implement your open editor functionality here
        val settingsList = createPesdkSettingsList()

        settingsList.configure<LoadSettings> {
            it.source = imageUri
        }

        PhotoEditorBuilder(this)
            .setSettingsList(settingsList)
            .startActivityForResult(this, PESDK_RESULT)

        settingsList.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        intent ?: return

        if (requestCode == PESDK_RESULT) {
            val result = EditorSDKResult(intent)
            when (result.resultStatus) {
                EditorSDKResult.Status.CANCELED -> showMessage("Editor cancelled")
                EditorSDKResult.Status.EXPORT_DONE -> {
                    val resultUri = result.resultUri
                    val resultIntent = Intent()
                    resultIntent.putExtra("editedImageUri", resultUri.toString())
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish() // Finish PictureViewActivity
                }
                else -> {
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
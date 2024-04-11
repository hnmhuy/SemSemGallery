package com.example.semsemgallery.activities.pictureview

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.provider.Settings

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.semsemgallery.R
import com.example.semsemgallery.activities.main.adapter.PictureAdapter
import com.example.semsemgallery.activities.pictureview.fragment.MetaDataBottomSheet
import com.example.semsemgallery.domain.PhotoActionsHandler
import com.example.semsemgallery.domain.Picture.PictureLoadMode
import com.example.semsemgallery.domain.Picture.PictureLoader
import com.example.semsemgallery.models.Picture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ly.img.android.pesdk.PhotoEditorSettingsList
import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic
import ly.img.android.pesdk.assets.font.basic.FontPackBasic
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes
import ly.img.android.pesdk.backend.model.EditorSDKResult
import ly.img.android.pesdk.backend.model.state.LoadSettings
import ly.img.android.pesdk.backend.model.state.PhotoEditorSaveSettings
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder
import ly.img.android.pesdk.ui.model.state.UiConfigFilter
import ly.img.android.pesdk.ui.model.state.UiConfigFrame
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay
import ly.img.android.pesdk.ui.model.state.UiConfigSticker
import ly.img.android.pesdk.ui.model.state.UiConfigText
import ly.img.android.pesdk.ui.panels.item.PersonalStickerAddItem
import java.io.File
import java.util.Collections
import java.util.Date
import java.util.TreeSet

class PictureViewActivity : AppCompatActivity() {
    companion object {
        const val PESDK_RESULT = 1
    }

    private lateinit var fragmentActivity: PictureViewActivity
    private lateinit var topBar: MaterialToolbar
    private lateinit var actions: MaterialToolbar
    private var filePath: String = ""
    private var position: Int = -1
    private var selectingPic: Picture? = null
    private lateinit var viewPager: ViewPager2
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

    //    private var pictureTree : TreeSet<Picture> = TreeSet(Comparator.reverseOrder<Picture>())
    private var pictureList: ArrayList<Picture> = ArrayList()
    private lateinit var adapter: PictureAdapter
    fun findIndexOf(pic: Picture, list: ArrayList<Picture>): Int {
        var low = 0
        var high = list.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val dataMid = list[mid]
            if (dataMid.pictureId == pic.pictureId) {
                return mid
            } else if (dataMid.compareTo(pic) < 0) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return -1
    }

    private var loader = object : PictureLoader(this) {
        override fun onProcessUpdate(vararg processes: Picture) {
            if (processes[0].pictureId == selectingPic!!.pictureId) return;
            var predictPos =
                Collections.binarySearch(pictureList, processes[0], Comparator.reverseOrder());
            if (predictPos < 0) predictPos = -predictPos - 1;
            pictureList.add(predictPos, processes[0]);
            //adapter.notifyItemInserted(predictPos);
//            if (predictPos < position) {
//                position++
//                viewPager.setCurrentItem(position, false);
//            }
        }

        override fun postExecute(res: Boolean?) {
            viewPager.setCurrentItem(findIndexOf(selectingPic!!, pictureList), false);
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_view)
        enableEdgeToEdge()
        fragmentActivity = this
        viewPager = findViewById(R.id.vp_image)
        selectingPic = intent.getParcelableExtra<Picture>("selectingPic")
        pictureList.add(selectingPic!!)
        adapter = PictureAdapter(fragmentActivity, pictureList, 0)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(0, false);
        loader.execute(PictureLoadMode.ALL.toString())
//        val pictureList = intent.getParcelableArrayListExtra<Picture>("pictureList")
//        val position = intent.getIntExtra("position", 0)

        topBar = findViewById(R.id.activity_picture_view_topAppBar)
        topBar.setOnMenuItemClickListener { menuItem -> onOptionsItemSelected(menuItem) }
//        actions = findViewById(R.id.more_photo_action);
//        actions.setOnMenuItemClickListener { menuItem ->
//            onOptionsItemSelected(menuItem)
//        }

        viewPager = findViewById(R.id.vp_image)
        viewPager.offscreenPageLimit = 4

//        filePath = pictureList!![position].path
//        var fileName = pictureList[position].fileName
        var fileName = ""
        var date: Date?
//        var isFavorite = pictureList[position].isFav;
        var isFavorite = false
        val favBtn: ImageButton = findViewById(R.id.favorite_button)
        val infoBtn: ImageButton = findViewById(R.id.info_button)
        val shareBtn: ImageButton = findViewById(R.id.share_button)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                filePath = pictureList[position].path
                fileName = pictureList[position].fileName
                date = pictureList[position].dateTaken
                isFavorite = pictureList[position].isFav
                Log.e("FILEPATH", filePath)
                Log.e("DATE", date.toString())

                toggleFavorite(isFavorite, favBtn)
            }
        })


        infoBtn.setOnClickListener {
            val botSheetFrag = MetaDataBottomSheet(filePath, fileName)
            botSheetFrag.show(supportFragmentManager, botSheetFrag.tag)
        }

        // Event share to other apps
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


        // Favorite Icon

        toggleFavorite(isFavorite, favBtn)

        favBtn.setOnClickListener {

            var isStorageManager = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                isStorageManager = Environment.isExternalStorageManager()
            }

            if (isStorageManager) {
                val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_FAVORITE, !isFavorite)
                }

                val contentResolver: ContentResolver = applicationContext.contentResolver
                contentResolver.update(
                    contentUri,
                    values,
                    "${MediaStore.Images.Media.DATA}=?",
                    arrayOf(filePath)
                )
                isFavorite = !isFavorite
                pictureList[position].isFav = isFavorite
                toggleFavorite(isFavorite, favBtn)
            } else {

                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        topBar.setNavigationOnClickListener { v: View? -> finish() }
    }

    private fun toggleFavorite(isFavorite: Boolean, favBtn: ImageButton) {
        if (isFavorite) {
            favBtn.setImageResource(R.drawable.ic_heart_fill);
            val color = ContextCompat.getColor(applicationContext, R.color.is_favorite)
            favBtn.setColorFilter(color)
        } else {
            favBtn.setImageResource(R.drawable.ic_heart)
            val color = ContextCompat.getColor(applicationContext, R.color.picture_view_bottom_bar_bg)
            favBtn.setColorFilter(color)
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
                    finish()
                }

                else -> {
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu resource
        menuInflater.inflate(R.menu.top_bar_picture_view_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        val handler = PhotoActionsHandler.getInstance(applicationContext)
        when (item.itemId) {
            R.id.copy_to_clipboard -> {
                // Handle copy to clipboard action
                handler.copyToClipboard(this, filePath)
                Log.e("Image Path", filePath)
                return true
            }

            R.id.copy_to_album -> {
                // Handle copy to album action
                handler.copyToAlbum()

                return true
            }

            R.id.move_to_album -> {
                // Handle move to album action
                handler.moveToAlbum()

                return true
            }

            R.id.set_as_wallpaper -> {
                // Handle set as wallpaper action
                val bottomSheetView =
                    layoutInflater.inflate(R.layout.fragment_picture_options, null)
                val dialog = BottomSheetDialog(this)
                dialog.setContentView(bottomSheetView)

                val lockScreenTextView = bottomSheetView.findViewById<TextView>(R.id.lock_screen)
                val homeScreenTextView = bottomSheetView.findViewById<TextView>(R.id.home_screen)
                val lockAndHomeScreensTextView =
                    bottomSheetView.findViewById<TextView>(R.id.lock_and_home_screens)
                lockScreenTextView.setOnClickListener {
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsLockScreen(filePath)
                        }

                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")

                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                }

                homeScreenTextView.setOnClickListener {
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsHomeScreen(filePath)
                        }

                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")

                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                }

                lockAndHomeScreensTextView.setOnClickListener {
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsHomeScreenAndLockScreen(filePath)
                        }

                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")

                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                }

                dialog.show()
                return true
            }

            R.id.print -> {
                // Handle print action
                handler.print()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Function to show a message on the main thread
    private suspend fun showMessageOnMainThread(message: String) {
        withContext(Dispatchers.Main) {
            showMessage(message)
        }
    }
}
package com.example.semsemgallery.activities.pictureview

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.semsemgallery.R
import com.example.semsemgallery.activities.pictureview.adapter.PictureAdapter
import com.example.semsemgallery.activities.pictureview.fragment.MetaDataBottomSheet
import com.example.semsemgallery.activities.pictureview.fragment.OCRStickyBottomSheet
import com.example.semsemgallery.domain.AIHandler
import com.example.semsemgallery.domain.Album.AlbumHandler
import com.example.semsemgallery.domain.PhotoActionsHandler
import com.example.semsemgallery.domain.Picture.PictureLoadMode
import com.example.semsemgallery.domain.Picture.PictureLoader
import com.example.semsemgallery.models.Picture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import java.util.TreeSet


class PictureViewActivity : AppCompatActivity() {
    companion object {
        const val PESDK_RESULT = 1
    }

    private lateinit var imageUri: Uri

    private lateinit var handler: PhotoActionsHandler

    private var albumName: String? = ""
    private var choice: String? = ""
    private var tagName: String? =""
    private val mHandler = Handler(Looper.getMainLooper())

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val returnedData = data?.getStringExtra("key")
                albumName = returnedData
                if (choice == "copy") {
                    // handler.copyToAlbum(this, imageUri, albumName)

                    val uris: ArrayList<Uri> = arrayListOf()
                    uris.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selectingPic.pictureId))

                    val loadingDialog = myLoadingDialog()
                    loadingDialog.show() // Show Loading Dialog
                    val dialogTitle = loadingDialog.findViewById<TextView>(R.id.component_loading_dialog_title)
                    if (dialogTitle != null) {
                        dialogTitle.text = "Copying to $albumName"
                    }

                    // Create a listener for completion & set progress
                    val loadingListener = object : AlbumHandler.OnLoadingListener {
                        override fun onLoadingComplete() {
                            mHandler.post {
                                loadingDialog.dismiss()
                            }
                        }

                        override fun onLoadingProgressUpdate(progress: Int) {
                            val progressBar = loadingDialog.findViewById<ProgressBar>(R.id.component_loading_dialog_progressBar)
                            mHandler.post {
                                if (progressBar != null) {
                                    progressBar.progress = progress
                                }
                            }
                        }

                        override fun onLoadingException() {
                            mHandler.post {
                                showExceptionHandlerDialog()
                            }
                        }
                    }

                    AlbumHandler.copyImagesToAlbumHandler(this, uris, albumName, loadingListener)
                }
                else if (choice == "move") {
                    // handler.moveToAlbum(this, imageUri, albumName)

                    val uris: ArrayList<Uri> = arrayListOf()
                    uris.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selectingPic.pictureId))

                    val loadingDialog = myLoadingDialog()
                    loadingDialog.show() // Show Loading Dialog
                    val dialogTitle = loadingDialog.findViewById<TextView>(R.id.component_loading_dialog_title)
                    if (dialogTitle != null) {
                        dialogTitle.text = "Copying to $albumName"
                    }

                    // Create a listener for completion & set progress
                    val loadingListener = object : AlbumHandler.OnLoadingListener {
                        override fun onLoadingComplete() {
                            mHandler.post {
                                loadingDialog.dismiss()
                            }
                        }

                        override fun onLoadingProgressUpdate(progress: Int) {
                            val progressBar = loadingDialog.findViewById<ProgressBar>(R.id.component_loading_dialog_progressBar)
                            mHandler.post {
                                if (progressBar != null) {
                                    progressBar.progress = progress
                                }
                            }
                        }

                        override fun onLoadingException() {
                            mHandler.post {
                                showExceptionHandlerDialog()
                            }
                        }
                    }

                    AlbumHandler.moveImagesToAlbumHandler(this, uris, albumName, loadingListener)
                }
                // Now you have the returned data from OtherActivity
                // Process it as needed
            }
        }
    }



    private fun showExceptionHandlerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.component_exception_handler_dialog, null)
        val dialogBuilder = MaterialAlertDialogBuilder(this).setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val applyCheckbox = dialogView.findViewById<CheckBox>(R.id.component_exception_handler_dialog_checkbox)
        val skipBtn = dialogView.findViewById<TextView>(R.id.component_exception_handler_dialog_skip)
        val replaceBtn = dialogView.findViewById<TextView>(R.id.component_exception_handler_dialog_replace)
        val description = dialogView.findViewById<TextView>(R.id.component_exception_handler_dialog_description)
        description.text = "There is already an item name ${AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri)} in the selected album"

        applyCheckbox.setOnClickListener {
            AlbumHandler.isApplyToAll = applyCheckbox.isChecked
        }

        // Listener for CancelButton in Delete Confirm Dialog clicked
        skipBtn.setOnClickListener {
            Log.d("AlbumsFM", "Skip ${AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri)}")
            dialog.dismiss()
            AlbumHandler.duplicateHandleChoice = "skip"
            AlbumHandler.isDuplicateHandling = false
        }

        // Listener for DeleteButton in Delete Confirm Dialog clicked
        replaceBtn.setOnClickListener {
            Log.d("AlbumsFM", "Replace ${AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri)}")
            dialog.dismiss()
            AlbumHandler.duplicateHandleChoice = "replace"
            AlbumHandler.isDuplicateHandling = false
        }
    }

    // Just only able to init Dialog in Fragment/Activity
    private fun myLoadingDialog(): androidx.appcompat.app.AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.component_loading_dialog, null)
        val dialogBuilder = MaterialAlertDialogBuilder(this).setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)

        val cancelButton = dialogView.findViewById<Button>(R.id.component_loading_dialog_cancelButton)
        cancelButton.setOnClickListener {
            AlbumHandler.stopHandling()
        }

        return dialog
    }

    private var albumId: String? = null
    private var loadMode: String? = null
    private lateinit var fragmentActivity: PictureViewActivity
    private lateinit var topBar: MaterialToolbar
    private lateinit var actions: MaterialToolbar
    private var position: Int = -1
    private lateinit var selectingPic: Picture
    private lateinit var viewPager: ViewPager2
    private lateinit var favBtn: ImageButton
    private lateinit var ocrBtn: ImageButton
    private var linesText: List<String> = emptyList()
    private val aiHandler: AIHandler = AIHandler.getInstance()
    private var index: Int = -1
    private val treeSet: TreeSet<Picture> = TreeSet(Comparator.reverseOrder<Picture>())
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

    private var loader = object : PictureLoader(this) {
        override fun onProcessUpdate(vararg processes: Picture) {
            treeSet.add(processes[0])
        }

        override fun postExecute(res: Boolean?) {
            pictureList.clear();
            pictureList = ArrayList(treeSet)
            adapter.pictures.clear()
            adapter.pictures = pictureList

            for ((index, value) in pictureList.withIndex()) {
                if (value.pictureId != selectingPic.pictureId) {
                    adapter.notifyItemInserted(index)
                }
            }

            if (treeSet.contains(selectingPic)) {
                viewPager.setCurrentItem(treeSet.headSet(selectingPic).size, false)
            }
        }

    }

    private fun processOCR(state: Int) {
        if (state == ViewPager2.SCROLL_STATE_IDLE) {
            Log.d("Pager", "Scroll idle")
            val textRecognitionTask =
                aiHandler.getTextRecognition(applicationContext, selectingPic.pictureId)
            textRecognitionTask.addOnSuccessListener { lines ->
                if (lines.isEmpty()) {
                    ocrBtn.visibility = View.INVISIBLE
                    Log.d("TEXT_RECOGNITION", "No text recognized")
                } else {
                    ocrBtn.visibility = View.VISIBLE
                    linesText = lines
                }
            }.addOnFailureListener { e ->
                Log.e("TEXT_RECOGNITION", "Text recognition failed", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_view)
        enableEdgeToEdge()
        handler = PhotoActionsHandler.getInstance(applicationContext)

        // Load data
        viewPager = findViewById(R.id.vp_image)
        selectingPic = intent.getParcelableExtra<Picture>("selectingPic")!!
        loadMode = intent.getStringExtra("loadMode")
        albumId = intent.getStringExtra("albumId")
        tagName = intent.getStringExtra("tagName")

        Log.d("PictureViewActivity", "AlbumID = " + albumId)
        pictureList.add(selectingPic)
        adapter =
            PictureAdapter(
                this,
                pictureList,
                0
            )
        processOCR(0)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(0, false);
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                super.onPageSelected(pos)
                index = pos
                if (pictureList.isNotEmpty()) {
                    val element = pictureList[0]
                    selectingPic = pictureList[pos];
                    toggleFavorite(selectingPic.isFav)
                    Log.d("TrashForPictureViewActivity", "Index = $pos")
                } else {
                    // Handle the case where the ArrayList is empty
                    Log.e("PictureViewActivity", "ArrayList is empty")
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                processOCR(state)
            }
        })

        if (loadMode != null) {
            if (loadMode == PictureLoadMode.BY_ALBUM.toString() && albumId != null) {
                loader.execute(loadMode, albumId, "");
            } else if(loadMode == PictureLoadMode.ID.toString()){
                loader.execute(loadMode, tagName);
            }
            else {
                loader.execute(loadMode);
            }
        } else {
            Toast.makeText(this, "Some thing went wrong :(", Toast.LENGTH_LONG);
        }
        // UI handler

        topBar = findViewById(R.id.activity_picture_view_topAppBar)
        favBtn = findViewById(R.id.favorite_button)
        ocrBtn = findViewById(R.id.ocr_button)

        val editBtn: ImageButton = findViewById(R.id.edit_picture_button)
        val infoBtn: ImageButton = findViewById(R.id.info_button)
        val shareBtn: ImageButton = findViewById(R.id.share_button)
        val deleteBtn: ImageButton = findViewById(R.id.delete_button)


        topBar.setOnMenuItemClickListener { menuItem -> onOptionsItemSelected(menuItem) }

        deleteBtn.setOnClickListener {
            val confirmDialog: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                .setTitle("Move this picture to Trash?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                    "Move to trash"
                ) { _, _ ->
                    val values: ContentValues = ContentValues()
                    values.put(MediaStore.Images.Media.IS_TRASHED, 1);
                    val imageUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selectingPic.pictureId
                    )

                    var res = this.contentResolver.update(imageUri, values, null, null)
                    if (res > 0) {
                        values.remove(MediaStore.Images.Media.IS_TRASHED)
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        this.contentResolver.update(imageUri, values, null, null)
                        if (index != -1) {
                            treeSet.remove(selectingPic)
                            if (pictureList.size == 0) {
                                Toast.makeText(this, "No picture!", Toast.LENGTH_LONG)
                            } else adapter.removePictureAt(index)
                        }
                    }
                };
            confirmDialog.show()
        }

        infoBtn.setOnClickListener {
            Log.d("PictureViewActivity", selectingPic.path)
            var temp =
                if (selectingPic.dateTaken.time != 0L) selectingPic.dateTaken else selectingPic.dateAdded
            val botSheetFrag = MetaDataBottomSheet(
                selectingPic.pictureId,
                selectingPic.path,
                selectingPic.fileName,
                temp,
                selectingPic.fileSize
            )
            botSheetFrag.setListner { isUpdateFileName ->
                if (isUpdateFileName) {
                    val imageUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selectingPic.pictureId
                    )
                    selectingPic.path = getRealPathFromURI(imageUri, this)
                    selectingPic.fileName = getFileNameFromURI(imageUri, this)
                }

            }
            botSheetFrag.show(supportFragmentManager, botSheetFrag.tag)
        }

        ocrBtn.setOnClickListener {
            val ocrBottomSheet = OCRStickyBottomSheet.newInstance(linesText);
            ocrBottomSheet.show(supportFragmentManager, ocrBottomSheet.tag)
        }

        // Event share to other apps
        shareBtn.setOnClickListener {
            val imageFile = File(selectingPic.path)
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
        editBtn.setOnClickListener {
            val imageFile = File(selectingPic.path)
            val imageUri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                imageFile
            )
            openEditor(imageUri)
        }

        favBtn.setOnClickListener {
            var isStorageManager = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                isStorageManager = Environment.isExternalStorageManager()
            }

            if (isStorageManager) {
                val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_FAVORITE, !selectingPic.isFav)
                }

                val contentResolver: ContentResolver = applicationContext.contentResolver
                contentResolver.update(
                    contentUri,
                    values,
                    "${MediaStore.Images.Media.DATA}=?",
                    arrayOf(selectingPic.path)
                )
                selectingPic.isFav = !selectingPic.isFav
                toggleFavorite(selectingPic.isFav)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }

        }
    }

    fun getFileNameFromURI(contentUri: Uri, context: Context): String? {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val displayNameIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                return c.getString(displayNameIndex)
            }
        }
        return null
    }
    fun getRealPathFromURI(contentUri: Uri, context: Context): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(column_index ?: -1)
        cursor?.close()
        return path
    }

    override fun onResume() {
        super.onResume()
        topBar.setNavigationOnClickListener { _: View? -> finish() }
    }

    private fun toggleFavorite(isFavorite: Boolean) {
        if (isFavorite) {
            favBtn.setImageResource(R.drawable.ic_heart_fill);
            val color = ContextCompat.getColor(applicationContext, R.color.is_favorite)
            favBtn.setColorFilter(color)
        } else {
            favBtn.setImageResource(R.drawable.ic_heart)
            favBtn.colorFilter = null
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

        when (item.itemId) {
            R.id.copy_to_clipboard -> {
                Log.e("Image Path", selectingPic.path)
                val imageFile = File(selectingPic.path)
                val imageUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    imageFile
                )
                handler.copyToClipboard(this, imageUri)
                Log.e("Image Path", selectingPic.path)
                return true
            }

            R.id.copy_to_album -> {
                // Handle copy to album action
                val imageFile = File(selectingPic.path)
                imageUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    imageFile
                )



                choice = "copy"
                pickAlbum()
                return true
            }

            R.id.move_to_album -> {
                val imageFile = File(selectingPic.path)
                imageUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    imageFile
                )
                choice = "move"
                pickAlbum()

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
                    // Dismiss the dialog
                    dialog.dismiss()
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsLockScreen(selectingPic.path)
                        }

                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")

                    }
                }

                homeScreenTextView.setOnClickListener {
                    // Dismiss the dialog
                    dialog.dismiss()
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsHomeScreen(selectingPic.path)
                        }



                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")
                    }
                }

                lockAndHomeScreensTextView.setOnClickListener {
                    // Dismiss the dialog
                    dialog.dismiss()
                    GlobalScope.launch {
                        // Start the background task in a coroutine
                        val job = launch {
                            handler.setAsHomeScreenAndLockScreen(selectingPic.path)
                        }

                        // Display "Processing" message on the main thread
                        showMessageOnMainThread("Processing...")

                        // Wait for the background task to complete
                        job.join()

                        // Display "Successfully" message on the main thread
                        showMessageOnMainThread("Successfully")
                    }
                }

                dialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun pickAlbum() {
        val myIntent = Intent(this, ChooseAlbumActivity::class.java)
        activityResultLauncher.launch(myIntent)
    }

    // Function to show a message on the main thread
    private suspend fun showMessageOnMainThread(message: String) {
        withContext(Dispatchers.Main) {
            showMessage(message)
        }
    }
}
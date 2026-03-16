package com.example.tiktok.ui.fragment


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.databinding.FragmentPersonalHomeBinding
import com.example.tiktok.ui.adapter.PersonalHomePagerAdapter
import com.example.tiktok.viewmodel.PersonalHomeViewModel
import com.example.tiktok.utils.ImageUtils
import com.example.tiktok.utils.Resource
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Locale
import kotlin.math.min


class PersonalHomeFragment : BaseBindingFragment<FragmentPersonalHomeBinding>({ FragmentPersonalHomeBinding.inflate(it) }) {
    private val viewModel: PersonalHomeViewModel by viewModels()

    //临时存储拍照后的照片 URI
    private var tempPhotoUri: Uri? = null
    private var appBarOffset = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupToolbar()              //设置返回按钮和更多按钮
        setupAvatarClick()          //设置头像点击事件
        setupViewPager()            //设置 ViewPager 和 TabLayout
        setupStretchEffect()
        observeViewModel()          //观察 ViewModel 数据变化
        viewModel.loadUserInfo()    // 加载个人主页用户数据
    }

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted ->
        if (granted) {
            openCamera()
        } else {
            showToast("需要相机权限才能拍照")
        }
    }

    // 存储权限请求
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openGallery()
        } else {
            showToast("需要存储权限才能选择图片")
        }
    }

    // 拍照结果发射器
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            startCrop(tempPhotoUri!!)
        } else {
            showToast("拍照失败")
        }
    }

    // 图片选择结果发射器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    )  { uri ->
        if (uri != null) {
            startCrop(uri)
        } else {
            showToast("未选择图片")
        }
    }

    // 裁剪结果发射器
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val croppedUri = result.data?.let { UCrop.getOutput(it) }
                if (croppedUri != null) {
                    viewModel.uploadAvatar(croppedUri)
                } else {
                    showToast("裁剪失败")
                }
            }
            UCrop.RESULT_ERROR -> {
                val error = result.data?.let { UCrop.getError(it) }
                showToast("裁剪失败: ${error?.message}")
            }
            else -> {
                android.util.Log.d("PersonalHome", "用户取消裁剪")
            }
        }
    }

    // 设置 ViewPager 和 TabLayout
    private fun setupViewPager() {
        val adapter = PersonalHomePagerAdapter(this)  // 传入 Fragment
        binding.viewPager.adapter = adapter

        // 使用 TabLayoutMediator 连接 TabLayout 和 ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    private fun setupStretchEffect() {
        binding.appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            appBarOffset = verticalOffset
        })
    }

    fun applyBackgroundStretch(distance: Float) {
        if (appBarOffset != 0 || distance <= 0f) {
            return
        }
        val progress = min(distance / 800f, 0.25f)
        val scale = 1f + progress
        binding.ivBg.pivotX = binding.ivBg.width / 2f
        binding.ivBg.pivotY = 0f
        binding.ivBg.scaleX = scale
        binding.ivBg.scaleY = scale
        binding.ivBg.translationY = distance * 0.35f
    }

    fun resetBackgroundStretch() {
        binding.ivBg.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(220)
            .start()
    }

    // 观察 ViewModel 数据变化
    private fun observeViewModel() {
        // 观察用户信息
        viewModel.userInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    resource.data?.let { updateUI(it) }
                }
                is Resource.Error -> {
                    showToast(resource.message ?: "加载失败")
                }
            }
        }

        // 观察头像上传状态
        viewModel.avatarUploadStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    showToast("头像上传成功")
                }
                is Resource.Error -> {
                    showToast(resource.message ?: "上传失败")
                }
            }
        }

    }

    // 更新 UI
    private fun updateUI(userInfo: com.example.tiktok.data.model.UserInfo) {
        with(binding.homeHeader) {
            // 昵称
            tvNickname.text = userInfo.nickname
            // 个性签名
            tvSign.text = userInfo.signature
            // 统计数据
            tvGetLikeCount.text = formatCount(userInfo.likesCount)
            tvFocusCount.text = formatCount(userInfo.followingCount)
            tvFansCount.text = formatCount(userInfo.fansCount)

            // 加载头像
            Glide.with(requireContext())
                .load(userInfo.avatarUrl.ifEmpty { R.mipmap.default_avatar })
                .circleCrop()
                .placeholder(R.mipmap.default_avatar)
                .error(R.mipmap.default_avatar)
                .into(ivHead)
        }

        // 加载背景图
        Glide.with(requireContext())
            .load(userInfo.backgroundUrl.ifEmpty { R.drawable.personal_home_background })
            .placeholder(R.drawable.personal_home_background)
            .into(binding.ivBg)
    }

    // 返回点击事件
    private fun setupToolbar() {
        val showBackButton = parentFragmentManager.backStackEntryCount > 0
        if (!showBackButton) {
            binding.ivReturn.visibility = View.GONE
        } else {
            binding.ivReturn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        binding.ivMore.setOnClickListener {
            showToast("更多功能待实现")
        }
    }

    // 设置头像点击事件
    private fun setupAvatarClick() {
        binding.homeHeader.ivHead.setOnClickListener {
            showAvatarChooseDialog()
        }
    }

    // 显示头像选择对话框
    private fun showAvatarChooseDialog() {
        val dialog = AvatarChooseDialog()

        dialog.setOnChooseListener(object : AvatarChooseDialog.OnChooseListener {
            override fun onCamera() {
                checkCameraPermission()
            }

            override fun onGallery() {
                checkStoragePermission()
            }
        })
        dialog.show(childFragmentManager, "AvatarChooseDialog")
    }

    // 检查相机权限
    private fun checkCameraPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 检查存储权限
    private fun checkStoragePermission() {
        //应对系统版本差异而写的 “动态权限适配”
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openGallery()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    // 打开相册
    private fun openGallery() {
        try {
            //启动之前注册好的图片选择器
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("打开相册失败: ${e.message}")
        }
    }

    // 打开相机
    private fun openCamera() {
        try {
            //创建临时图片文件
            val photoFile = ImageUtils.createTempImageFile(requireContext())

            //把一个私有文件路径 (File)，转换成了一个可共享的资源地址 (Uri)
            tempPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.FileProvider",
                photoFile
            )

            takePictureLauncher.launch(tempPhotoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("打开相机失败: ${e.message}")
        }
    }

    // 启动裁剪
    private fun startCrop(sourceUri: Uri) {
        try {
            val destinationUri = Uri.fromFile(
                File(requireContext().cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
            )

            val options = UCrop.Options().apply {
                setCompressionQuality(80)           //压缩质量
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
                setCircleDimmedLayer(true)          //圆形遮罩层
                setShowCropFrame(false)
                setShowCropGrid(false)
                setToolbarTitle("裁剪头像")
                setToolbarColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                setStatusBarColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                setActiveControlsWidgetColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                setToolbarWidgetColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }

            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)                //1:1 裁剪比例
                .withMaxResultSize(800, 800)     //最大输出尺寸
                .withOptions(options)

            cropImageLauncher.launch(uCrop.getIntent(requireContext()))

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("启动裁剪失败: ${e.message}")
        }
    }

    //格式化数字
    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> String.format(Locale.US,"%.1fw", count / 10000.0)
            count >= 1000 -> String.format(Locale.US,"%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.tiktok.ui.fragment


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.ui.adapter.CommPagerAdapter
import com.example.tiktok.databinding.FragmentPersonalHomeBinding
import com.example.tiktok.ui.fragment.MainFragment.Companion.curPage
import com.example.tiktok.ui.view.AvatarChooseDialog
import com.example.tiktok.ui.viewmodel.PersonalHomeViewModel
import com.example.tiktok.utils.IScrollToTop
import com.example.tiktok.utils.ImageUtils
import com.example.tiktok.utils.Resource
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import java.io.File


class PersonalHomeFragment : BaseBindingFragment<FragmentPersonalHomeBinding>({ FragmentPersonalHomeBinding.inflate(it) }) {
    private val viewModel: PersonalHomeViewModel by viewModels()

    private var tempPhotoUri: Uri? = null

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else showToast("需要相机权限才能拍照")
    }

    // 存储权限请求
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else showToast("需要存储权限才能选择图片")
    }

    // 拍照结果
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            startCrop(tempPhotoUri!!)
        }
    }

    // 图库选择结果
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { startCrop(it) }
    }

    // 裁剪结果
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = UCrop.getOutput(result.data!!)
            croppedUri?.let {
                // ✅ 通过 ViewModel 上传头像
                viewModel.uploadAvatar(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragment()
        setupToolbar()
        setupAvatarClick()
        observeViewModel()

        // ✅ 加载用户数据
        viewModel.loadUserInfo()
    }

    // ✅ 观察 ViewModel 数据变化
    private fun observeViewModel() {
        // 观察用户信息
        viewModel.userInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 显示加载状态
                }
                is Resource.Success -> {
                    resource.data?.let { userInfo ->
                        updateUI(userInfo)
                    }
                }
                is Resource.Error -> {
                    showToast(resource.message ?: "加载失败")
                }
            }
        }

        // 观察头像上传状态
        viewModel.avatarUploadStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 显示上传进度
                    showToast("正在上传头像...")
                }
                is Resource.Success -> {
                    // 上传成功，头像已在 userInfo 中更新
                }
                is Resource.Error -> {
                    showToast(resource.message ?: "上传失败")
                }
            }
        }

        // 观察 Toast 消息
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    // ✅ 更新 UI
    @SuppressLint("SuspiciousIndentation")
    private fun updateUI(userInfo: com.example.tiktok.data.model.UserInfo) {

            // 昵称
            binding.tvNickname.text = userInfo.nickname

            // 抖音号
            val douyinIdText = "抖音号: ${userInfo.douyinId}"
            // TODO: 设置抖音号到对应的 TextView

            // 个性签名
        binding.tvSign.text = userInfo.signature

            // 年龄和地区（这里需要使用 tools:text，实际数据通过代码设置）
            // tvAge.text = "${userInfo.age}岁"
            // tvLocation.text = userInfo.location

            // 统计数据
        binding.tvGetLikeCount.text = viewModel.formatCount(userInfo.likesCount)
        binding.tvFocusCount.text = viewModel.formatCount(userInfo.followingCount)
        binding.tvFansCount.text = viewModel.formatCount(userInfo.fansCount)

            // 加载头像
            Glide.with(requireContext())
                .load(userInfo.avatarUrl.ifEmpty { R.mipmap.default_avatar })
                .circleCrop()
                .placeholder(R.mipmap.default_avatar)
                .into(binding.ivHead)


        // 加载背景图
        Glide.with(requireContext())
            .load(userInfo.backgroundUrl.ifEmpty { R.drawable.personal_home_background })
            .placeholder(R.drawable.personal_home_background)
            .into(binding.ivBg)
    }

    private fun setupToolbar() {
        binding.ivReturn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivMore.setOnClickListener {
            showToast("更多功能待实现")
        }
    }

    private fun setupAvatarClick() {
        binding.ivHead.setOnClickListener {
            showAvatarChooseDialog()
        }
    }

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

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openCamera() {
        val photoFile = ImageUtils.createTempImageFile(requireContext())
        tempPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(tempPhotoUri)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false)
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
            setToolbarTitle("裁剪头像")
        }

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .withOptions(options)

        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

//个人主页下方作品，点赞，收藏界面逻辑
    val videoPlayStateLiveData = MutableLiveData<Boolean>()
    private var workFragment: WorkFragment? = null
    private val fragments=ArrayList<Fragment>()
    private var pagerAdapter:CommPagerAdapter? = null
    private var tabLayoutMediator:TabLayoutMediator? = null

    private fun setFragment(){

        workFragment= WorkFragment()

        fragments.add(workFragment!!)
        //设置适配器
        pagerAdapter= CommPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            fragments,
            arrayOf("作品","收藏","喜欢")
        )
        binding.viewPager.adapter=pagerAdapter

        //预加载所有页面
        binding.viewPager.offscreenPageLimit=1

        // 启用 ViewPager2 的滑动
        binding.viewPager.isUserInputEnabled = true


        //关联TabLayout与ViewPager2
        tabLayoutMediator= TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ){tab,position->
            tab.text=pagerAdapter?.getPageTitle(position)
        }
        tabLayoutMediator?.attach()

        //动态设置文字居中
        binding.viewPager.post {
            for (i in 0 until binding.tabLayout.tabCount) {
                val tab = binding.tabLayout.getTabAt(i)
                val tabView = (tab?.view as? ViewGroup)?.getChildAt(1) as? TextView
                tabView?.gravity = Gravity.CENTER
            }
            binding.viewPager.setCurrentItem(1, false)

            //默认选中推荐页
            videoPlayStateLiveData.value = true
        }


        //监听页面切换
        binding.viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                curPage=position

                when (position) {
                    0 -> {
                        // 同城页：暂停视频
                        videoPlayStateLiveData.value = false
                    }
                    1 -> {
                        // 推荐页：播放视频
                        videoPlayStateLiveData.value = true
                    }
                }
            }
        })

        //监听Tab重复点击
        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollToTop(tab?.position?:0)
            }
        })
    }
    private fun scrollToTop(position:Int){
        val tag = "f$position"
        val fragment = childFragmentManager.findFragmentByTag(tag)

        // 检查 Fragment 是否存在且已添加
        if (fragment is IScrollToTop && fragment.isAdded && !fragment.isDetached) {
            fragment.scrollToTop()
        }
    }
    //切换到指定 Tab
    fun switchTab(position: Int) {
        if (position in 0 until fragments.size) {
            binding.viewPager.setCurrentItem(position, true)  // true = 平滑滚动
        }
    }
}


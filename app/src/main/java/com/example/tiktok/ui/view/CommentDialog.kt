package com.example.tiktok.ui.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tiktok.R
import com.example.tiktok.databinding.DialogCommentBinding
import com.example.tiktok.ui.adapter.CommentAdapter
import com.example.tiktok.ui.viewmodel.CommentViewModel
import com.example.tiktok.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog

class CommentDialog(
    context: Context,
    private val videoId:Int,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val onCommentCountChanged: ((Int) -> Unit)? = null
) :BottomSheetDialog(context, R.style.BottomSheetDialogTheme), LifecycleOwner{

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var binding: DialogCommentBinding
    private lateinit var viewModel: CommentViewModel
    private var adapter: CommentAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CommentDialog", "onCreate 调用")
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        binding = DialogCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(viewModelStoreOwner)[CommentViewModel::class.java]

        setupDialog()
        setupWindowInsets()
        setupRecyclerView()
        setupInputArea()
        observeViewModel()


        // 加载评论列表
        Log.d("CommentDialog", "开始加载评论，videoId: $videoId")
        viewModel.loadComments(videoId)
    }

    override fun onStart() {
        super.onStart()
        // 设置生命周期状态为 STARTED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun show() {
        super.show()
        // 设置生命周期状态为 RESUMED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    //配置弹窗样式
    private fun setupDialog() {
        // 设置宽度为屏幕宽度，高度自适应
        window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 从底部弹出
            setGravity(Gravity.BOTTOM)
            // 背景透明（避免白色边框）
            setBackgroundDrawableResource(android.R.color.transparent)

            // 让内容延伸到系统栏下方
            WindowCompat.setDecorFitsSystemWindows(this, false)

            // 设置导航栏透明
            WindowCompat.getInsetsController(this, decorView).apply {
                // 设置导航栏为浅色模式（可选）
                isAppearanceLightNavigationBars = false
            }
        }

        // 设置弹窗完全展开
        behavior.apply {
            state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            isDraggable = true     //可拖拽关闭
            skipCollapsed = true   // 跳过折叠状态

            // 设置为屏幕高度
            maxHeight = context.resources.displayMetrics.heightPixels
        }
    }

    //设置评论列表
    private fun setupRecyclerView() {
        adapter = CommentAdapter(
            onLikeClick = { comment, position ->
                viewModel.toggleCommentLike(comment, position)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CommentDialog.adapter
            setHasFixedSize(true)  // RecyclerView 尺寸固定，优化性能
        }
    }

    //设置输入框
    private fun setupInputArea() {
        with(binding) {

            // 设置输入框提示文字
            etInput.setHint(R.string.comment_input_hint)

            // 输入框获得焦点时，自动滚动到底部
            etInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // 延迟执行，等待软键盘弹出
                    etInput.postDelayed({
                        // 如果有评论，滚动到最后一条
                        val itemCount = adapter?.itemCount ?: 0
                        if (itemCount > 0) {
                            recyclerView.smoothScrollToPosition(itemCount - 1)
                        }
                    }, 200)
                }
            }

            // 发送按钮点击
            tvSend.setOnClickListener {
                val content = etInput.text.toString()
                if (content.isNotBlank()) {
                    viewModel.publishComment(content)  // 发布评论
                    etInput.setText("")                // 清空输入框
                    hideKeyboard()                     // 隐藏键盘
                } else {
                    Toast.makeText(context, R.string.comment_empty, Toast.LENGTH_SHORT).show()
                }
            }

            // 键盘回车发送
            etInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    tvSend.performClick()           // 模拟点击发送按钮
                    true
                } else {
                    false
                }
            }
        }
    }

    //监听数据变化
    private fun observeViewModel() {
        viewModel.commentList.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 显示加载中
                    Log.d("CommentDialog", "加载中...")
                }

                is Resource.Success -> {
                    resource.data?.let { comments ->

                        adapter?.submitList(comments)
                        binding.tvTitle.text = context.getString(R.string.comment_count_format, comments.size)       // 更新标题
                        onCommentCountChanged?.invoke(comments.size)

                    }
                }

                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }

            }
        }

        viewModel.publishResult.observe(this) { resource ->

            when (resource) {
                is Resource.Loading -> {
                    // 显示发布中
                }
                is Resource.Success -> {
                    // 滚动到顶部，显示新评论
                    binding.recyclerView.postDelayed({
                        binding.recyclerView.smoothScrollToPosition(0)
                    }, 100)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 处理软键盘插入
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            // 获取系统栏和键盘的 insets
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // 使用最大值（键盘弹出时用键盘高度，否则用导航栏高度）
            val bottomInset = maxOf(systemBarsInsets.bottom, imeInsets.bottom)

            // 动态设置输入框底部内边距
            binding.layoutInput.updatePadding(bottom = bottomInset)

            // 键盘弹出时，自动滚动到底部
            if (imeInsets.bottom > 0) {
                binding.recyclerView.postDelayed({
                    val itemCount = adapter?.itemCount ?: 0
                    if (itemCount > 0) {
                        binding.recyclerView.smoothScrollToPosition(itemCount - 1)
                    }
                }, 100)
            }

            WindowInsetsCompat.CONSUMED
        }
    }


    //隐藏软键盘
    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)
    }

    //销毁时清理资源
    override fun dismiss() {
        super.dismiss()

        // 设置生命周期状态为 DESTROYED
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED

        adapter = null
    }
}
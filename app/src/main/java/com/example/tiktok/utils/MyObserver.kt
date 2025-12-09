// MyObserver.kt 完整代码（扩展函数必须放在顶层，不能嵌套在类里）
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

// ====================== 扩展函数（顶层！顶层！顶层！）======================
/**
 * 给所有 LifecycleOwner（Activity/Fragment）添加一键注册观察者的扩展函数
 * 必须放在顶层作用域（不能在 MyObserver 类内部）
 */
fun LifecycleOwner.registerMyObserver() {
    MyObserver(this).register()
}

// ====================== 通用生命周期观察者 ======================
class MyObserver(private val lifecycleOwner: LifecycleOwner) : LifecycleEventObserver {
    private val ownerWeakRef = WeakReference(lifecycleOwner)
    private val TAG = "LifecycleTracker"

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val owner = ownerWeakRef.get() ?: return
        val pageInfo = getPageInfo(owner)
        Log.d(TAG, "[${pageInfo}] → 生命周期事件：${event.name}")

        if (event == Lifecycle.Event.ON_DESTROY) {
            Log.d(TAG, "[${pageInfo}] → 页面销毁，释放Observer引用")
            ownerWeakRef.clear()
        }
    }

    private fun getPageInfo(owner: LifecycleOwner): String {
        val className = owner::class.java.simpleName
        val hash = owner.hashCode().toString(16)
        val type = when (owner) {
            is android.app.Activity -> "Activity"
            is androidx.fragment.app.Fragment -> {
                val tag = owner.tag ?: "无Tag"
                "Fragment(tag=$tag)"
            }
            else -> "UnknownLifecycleOwner"
        }
        return "${className}@${hash}(${type})"
    }

    fun register() {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun unregister() {
        lifecycleOwner.lifecycle.removeObserver(this)
    }
}
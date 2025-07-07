package expo.modules.router

import android.content.Context
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import java.lang.ref.WeakReference
import com.facebook.react.uimanager.PixelUtil

object PortalHostsRegistry {
  private val map = mutableMapOf<String, WeakReference<ModalPortalHostView>>()

  fun register(host: ModalPortalHostView) {
    map[host.getHostId()] = WeakReference(host)
  }

  fun unregister(hostId: String) {
    map.remove(hostId)
  }

  fun getHost(hostId: String): ModalPortalHostView? {
    return map[hostId]?.get()
  }
}

class ModalPortalHostView(context: Context, appContext: AppContext) :
  ExpoView(context, appContext) {
  private var hostId: String = ""
  private var isFluid: Boolean = false
  private var contentView: WeakReference<ModalPortalContentView>? = null

  fun setHostId(hostId: String) {
    if (this.hostId == hostId) {
      return;
    }
    PortalHostsRegistry.unregister(this.hostId)
    this.hostId = hostId
    PortalHostsRegistry.register(this)
  }

  fun getHostId(): String {
    return this.hostId
  }

  fun setIsFluid(isFluid: Boolean) {
    this.isFluid = isFluid
  }

  fun setSize(width: Int, height: Int) {
    if (!isFluid) {
      shadowNodeProxy.setViewSize(
        PixelUtil.toDIPFromPixel(width.toFloat()).toDouble(),
        PixelUtil.toDIPFromPixel(height.toFloat()).toDouble()
      )
    }
  }

  fun setContentView(contentView: ModalPortalContentView) {
    unmountContentView()

    this.contentView = WeakReference(contentView)
    contentView.setHostComponent(this)

    if (isFluid) {
      contentView.setSize(this.width, this.height)
    }

    addView(contentView)
  }

  fun unmountContentView() {
    contentView?.get()?.let { removeView(it) }
    contentView = null
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (changed) {
      contentView?.get()?.let {
        if (isFluid) {
          it.setSize(r - l, b - t)
        }
      }
    }
  }
}

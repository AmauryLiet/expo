package expo.modules.router

import android.content.Context
import android.view.View
import android.view.ViewGroup
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import java.lang.ref.WeakReference

class ModalPortalContentWrapperView(context: Context, appContext: AppContext) :
  ExpoView(context, appContext) {
  private var host: WeakReference<ModalPortalHostView>? = null
  private var contentView: WeakReference<ModalPortalContentView>? = null

  override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
    if (this.contentView != null) {
      print(
        "Warning: Multiple ModalPortalContentView components found. Only the first one will be used."
      )
    }
    (child as? ModalPortalContentView)?.let {
      this.host?.get()?.setContentView(it)
      this.contentView = WeakReference(it)
    } ?: {
      print("Mounting: Child component view must be of type ModalPortalContentView")
    }
  }

  override fun removeViewAt(index: Int) {
    if (this.contentView == null) {
      print("Warning: Removing child that does not exist from ModalPortalContentWrapperView")
    }
    this.contentView?.get()?.let {
      this.host?.get()?.unmountContentView()
      this.contentView = null
    } ?: {
      print("Unmounting: Child component view must be of type ModalPortalContentView")
    }
  }

  fun setHostId(hostId: String) {
    PortalHostsRegistry.getHost(hostId)?.let { hostView ->
      this.host = WeakReference(hostView)
      this.contentView?.get()?.let { contentView ->
        hostView.setContentView(contentView)
      }
    } ?: {
      print("Host view with id $hostId not found")
    }
  }
}

/*
 *  Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.plaidapp.util.glide

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition

/**
 * Like [ImageViewTarget] but will not automatically start playing its resource.
 */
abstract class NonAutoStartImageViewTarget<Z> : ViewTarget<ImageView, Z>, Transition.ViewAdapter {

  private var animatable: Animatable? = null
  private var runningWhenStopped = false

  constructor(view: ImageView) : super(view)

  constructor(view: ImageView, waitForLayout: Boolean) : super(view, waitForLayout)

  override fun getCurrentDrawable(): Drawable? {
    return view.drawable
  }
  override fun setDrawable(drawable: Drawable?) {
    view.setImageDrawable(drawable)
  }

  override fun onLoadStarted(placeholder: Drawable?) {
    super.onLoadStarted(placeholder)
    setResourceInternal(null)
    setDrawable(placeholder)
  }

  override fun onLoadFailed(errorDrawable: Drawable?) {
    super.onLoadFailed(errorDrawable)
    setResourceInternal(null)
    setDrawable(errorDrawable)
  }

  override fun onLoadCleared(placeholder: Drawable?) {
    super.onLoadCleared(placeholder)
    setResourceInternal(null)
    setDrawable(placeholder)
  }

  override fun onResourceReady(resource: Z, transition: Transition<in Z>?) {
    if (transition == null || !transition.transition(resource, this)) {
      setResourceInternal(resource)
    } else {
      maybeUpdateAnimatable(resource)
    }
  }

  override fun onStart() {
    if (runningWhenStopped) {
      animatable?.start()
    }
  }

  override fun onStop() {
    runningWhenStopped = animatable?.isRunning ?: false
    animatable?.stop()
  }

  private fun setResourceInternal(resource: Z?) {
    maybeUpdateAnimatable(resource)
    setResource(resource)
  }

  private fun maybeUpdateAnimatable(resource: Z?) {
    if (resource is Animatable) {
      animatable = resource
    } else {
      animatable = null
    }
  }

  protected abstract fun setResource(resource: Z?)
}

package com.example.app_doublekrestaurant.util

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryService {
    fun uploadImage(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = bytes.toDouble() / totalBytes.toDouble()
                    onProgress(progress)
                }
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError("Không tìm thấy URL trong kết quả")
                    }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    onError(error.description ?: "Lỗi tải ảnh lên Cloudinary")
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}

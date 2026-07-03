package com.jobguard.app.analyzer

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

/**
 * OCR 识别助手 - 使用 Google ML Kit 识别图片中的中文文字
 */
object OcrHelper {

    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * 从 Bitmap 中识别文字
     */
    suspend fun recognizeText(bitmap: Bitmap): Result<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val tasks = com.google.android.gms.tasks.Tasks.await(recognizer.process(image))
            val text = tasks.text
            if (text.isBlank()) {
                Result.failure(Exception("未能从图片中识别到文字，请确保图片清晰且包含招聘信息"))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(Exception("OCR识别失败：${e.localizedMessage ?: "未知错误"}"))
        }
    }

    /**
     * 释放资源
     */
    fun close() {
        recognizer.close()
    }
}

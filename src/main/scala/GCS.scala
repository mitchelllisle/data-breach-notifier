package io.github.mitchelllisle

import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage, StorageOptions}
import java.nio.channels.Channels
import scala.reflect.ClassTag
import scala.io.Source

case class GCS(bucket: String) {
  private val storage: Storage = StorageOptions.getDefaultInstance.getService

  def write(path: String, content: String, contentType: String = "application/json"): Blob = {
    val blobId = BlobId.of(bucket, path)
    val blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build()
    val blob = storage.create(blobInfo, content.getBytes("UTF-8"))
    blob
  }

  def read[T](filePath: String)(implicit ct: ClassTag[T], parser: String => T): T = {
    val blob = storage.get(bucket, filePath)
    val reader = Channels.newInputStream(blob.reader())
    val content = Source.fromInputStream(reader).mkString
    parser(content)
  }
}

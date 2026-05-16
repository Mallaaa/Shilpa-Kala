package com.shilpakala.app.data

enum class BrandBackground {
    CREAM,
    WOOD,
    TERRACOTTA
}

data class BrandOptions(
    val productName: String,
    val woodType: String,
    val price: String,
    val artisanName: String,
    val background: BrandBackground
)

data class GalleryItem(
    val id: String,
    val fileName: String,
    val productName: String,
    val price: String,
    val createdAt: Long
)

object Routes {
    const val Home = "home"
    const val Studio = "studio"
    const val Gallery = "gallery"
}

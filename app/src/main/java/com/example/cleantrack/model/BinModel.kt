package com.example.cleantrack.model

data class BinModel(
    var binId: String = "",
    var ownerUserId: String = "",      // who owns this bin (paid user)
    var label: String = "",            // e.g. "Kitchen Bin", "Front Gate"
    var category: String = "MIXED",  // ORGANIC / INORGANIC / TOXIC / MIXED
    var qrValue: String = "",          // store the string encoded in QR (usually binId or "BIN:<id>")
    var isActive: Boolean = true,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "binId" to binId,
            "ownerUserId" to ownerUserId,
            "label" to label,
            "category" to category,
            "qrValue" to qrValue,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }
}

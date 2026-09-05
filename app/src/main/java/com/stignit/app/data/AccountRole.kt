package com.stignit.app.data

/** Wire values match the backend's `role` column exactly — see stignit-api users.schema.ts. */
enum class AccountRole {
    CIVILIAN, MEDICAL_PERSONNEL, DRIVER_RESPONDER;

    companion object {
        fun fromWire(value: String?): AccountRole =
            entries.find { it.name == value } ?: CIVILIAN
    }
}

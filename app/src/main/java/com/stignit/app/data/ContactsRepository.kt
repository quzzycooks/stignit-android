package com.stignit.app.data

import com.stignit.app.data.net.EmergencyContactBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.apiCall
import retrofit2.HttpException

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int,
    val verified: Boolean,
)

class ContactsRepository(
    private val api: StignitApi,
    private val session: SessionStore,
) {
    suspend fun list(): ApiResult<List<EmergencyContact>> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            api.listContacts(bearer)
                .sortedBy { it.priority }
                .map { EmergencyContact(it.id, it.name, it.phoneNumber, it.relationship, it.priority, it.verified) }
        }
    }

    suspend fun add(name: String, phone: String, relationship: String): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            api.addContact(bearer, EmergencyContactBody(name = name, phone = phone, relationship = relationship))
            Unit
        }
    }

    suspend fun remove(contactId: String): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            val res = api.removeContact(bearer, contactId)
            if (!res.isSuccessful) throw HttpException(res)
        }
    }
}

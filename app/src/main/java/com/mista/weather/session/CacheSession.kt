package com.mista.weather.session

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class CacheSession(
    private val sharedPreferences: SharedPreferences,
    private val transientSession: TransientSession,
    private val moshi: Moshi,
) {

    var userId: CacheWrapper<Int> = createCache(KEY_USER_ID, Int::class.java)

    var searchHistory: CacheWrapper<List<String>> = createCache(
        KEY_SEARCH_HISTORY,
        Types.newParameterizedType(List::class.java, String::class.java),
    )

    /** Whether the location permission has ever been requested, surviving process/app restarts. */
    var locationPermissionRequested: CacheWrapper<Boolean> = createCache(KEY_LOCATION_PERMISSION_REQUESTED, Boolean::class.java)

    fun clearAll() {
        userId.clear()
        searchHistory.clear()
        locationPermissionRequested.clear()
        transientSession.clearAll()
    }

    private inline fun <reified T> createCache(key: String, clazz: Class<T>): CacheWrapper<T> {
        return PersistentCacheWrapper(sharedPreferences, key, clazz, moshi)
    }

    private fun <T> createCache(key: String, type: Type): CacheWrapper<T> {
        return PersistentCacheWrapper(sharedPreferences, key, type, moshi)
    }

    companion object {
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_SEARCH_HISTORY = "key_search_history"
        private const val KEY_LOCATION_PERMISSION_REQUESTED = "key_location_permission_requested"
    }
}

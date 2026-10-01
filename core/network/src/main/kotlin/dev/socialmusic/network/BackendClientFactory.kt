package dev.socialmusic.network

import dev.socialmusic.common.BackendConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp

/** No client is created or contacted until explicit public configuration exists. */
class BackendClientFactory {
    fun create(config: BackendConfig?): SupabaseClient? {
        if (config == null) return null
        return createSupabaseClient(config.url, config.publishableKey) {
            httpEngine = OkHttp.create()
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}

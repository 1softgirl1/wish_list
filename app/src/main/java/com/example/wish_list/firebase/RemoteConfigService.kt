package com.example.wish_list.firebase

interface RemoteConfigService {
    suspend fun fetchAndActivate(isDebug: Boolean): RemoteConfigSnapshot
}

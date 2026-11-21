package com.example.nb_frontend.backend.data.repository

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.text.Collator
import java.util.Locale

/**
 * 설치된 앱 목록(InstalledAppsRepository)을 실제 기기·에뮬레이터에서 확인하는 테스트.
 * Logcat에서 "InstalledAppsTest"로 검색하면 가져온 앱 목록 전체를 볼 수 있다.
 */
@RunWith(AndroidJUnit4::class)
class InstalledAppsRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo = InstalledAppsRepository(context)

    @Test
    fun printsAppListToLogcat() {
        val apps = runBlocking { repo.getLaunchableApps() }
        val all = runBlocking { repo.getLaunchableApps(includeSystemApps = true) }
        apps.forEach { Log.d(TAG, "${it.name} | ${it.packageName} | icon=${it.icon != null}") }
        Log.d(TAG, "기본 목록 ${apps.size}개 / 시스템 앱 포함 ${all.size}개")
        Log.d(TAG, "유튜브($YOUTUBE) 기본 목록 포함: ${apps.any { it.packageName == YOUTUBE }}")
        assertTrue("가져온 앱이 하나도 없음", apps.isNotEmpty())
    }

    @Test
    fun keepsYoutubeWhenInstalled() {
        val apps = runBlocking { repo.getLaunchableApps() }.map { it.packageName }.toSet()
        val all = runBlocking { repo.getLaunchableApps(includeSystemApps = true) }.map { it.packageName }.toSet()
        if (YOUTUBE in all) assertTrue("유튜브가 설치돼 있는데 기본 목록에서 빠짐", YOUTUBE in apps)
    }

    @Test
    fun excludesNyangbreakItself() {
        val apps = runBlocking { repo.getLaunchableApps(includeSystemApps = true) }
        assertFalse(apps.any { it.packageName == context.packageName })
    }

    @Test
    fun hasNoDuplicatePackages() {
        val apps = runBlocking { repo.getLaunchableApps(includeSystemApps = true) }
        assertEquals(apps.size, apps.map { it.packageName }.toSet().size)
    }

    @Test
    fun isSortedByName() {
        val apps = runBlocking { repo.getLaunchableApps() }
        val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.SECONDARY }
        apps.zipWithNext().forEach { (a, b) ->
            assertTrue("정렬 순서가 틀림: ${a.name} > ${b.name}", collator.compare(a.name, b.name) <= 0)
        }
    }

    @Test
    fun loadsIcons() {
        val apps = runBlocking { repo.getLaunchableApps() }
        val missing = apps.filter { it.icon == null }.map { it.name }
        assertTrue("아이콘을 못 가져온 앱: $missing", missing.isEmpty())
    }

    @Test
    fun hidesPreinstalledSystemAppsByDefault() {
        val apps = runBlocking { repo.getLaunchableApps() }.map { it.packageName }.toSet()
        val all = runBlocking { repo.getLaunchableApps(includeSystemApps = true) }.map { it.packageName }.toSet()
        assertTrue("기본 목록이 전체 목록에 포함되어야 함", all.containsAll(apps))
        if (SETTINGS in all) assertFalse("설정 앱이 기본 목록에 보임", SETTINGS in apps)
    }

    companion object {
        private const val TAG = "InstalledAppsTest"
        private const val SETTINGS = "com.android.settings"
        private const val YOUTUBE = "com.google.android.youtube"
    }
}

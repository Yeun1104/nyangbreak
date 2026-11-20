package com.example.nb_frontend.backend.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

/**
 * 감시 대상으로 고를 수 있는 앱 하나.
 *
 * @param packageName 앱 패키지명 (예: com.google.android.youtube)
 * @param name 화면에 보여줄 앱 이름
 * @param icon 목록에 그릴 앱 아이콘. 가져오지 못하면 null
 */
data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: ImageBitmap? = null,
)

/**
 * 폰에 설치된 앱 중 감시 대상으로 고를 수 있는 앱 목록을 가져온다.
 *
 * - 홈 화면에서 실행할 수 있는 앱(런처 아이콘이 있는 앱)만 가져온다
 * - 냥브레이크 자신은 뺀다
 * - 기본으로 깔려 있고 한 번도 업데이트되지 않은 시스템 앱(설정, 전화 등)은 뺀다.
 *   유튜브·크롬처럼 미리 깔려 있어도 스토어에서 업데이트된 앱은 남긴다
 * - 이름을 기기 언어 기준 가나다/ABC 순으로 정렬한다 (대소문자 구분 없음)
 *
 * AndroidManifest.xml의 <queries> 선언이 있어야 안드로이드 11 이상에서 전체 목록이 보인다.
 */
class InstalledAppsRepository(private val context: Context) {

    suspend fun getLaunchableApps(includeSystemApps: Boolean = false): List<InstalledApp> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val iconSizePx = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
            val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.SECONDARY }

            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
                .map { it.activityInfo.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != context.packageName }
                .filter { includeSystemApps || !isPreinstalledSystemApp(it) }
                .map { info ->
                    InstalledApp(
                        packageName = info.packageName,
                        name = labelOf(pm, info),
                        icon = iconOf(pm, info, iconSizePx),
                    )
                }
                .sortedWith(compareBy<InstalledApp, String>(collator) { it.name }.thenBy { it.packageName })
        }

    /** 기본 탑재 시스템 앱이면서 업데이트된 적 없는 앱인지 */
    private fun isPreinstalledSystemApp(info: ApplicationInfo): Boolean {
        val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdated = (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return isSystem && !isUpdated
    }

    private fun labelOf(pm: PackageManager, info: ApplicationInfo): String =
        pm.getApplicationLabel(info).toString().trim().ifEmpty { info.packageName }

    private fun iconOf(pm: PackageManager, info: ApplicationInfo, sizePx: Int): ImageBitmap? = try {
        pm.getApplicationIcon(info).toBitmap(sizePx, sizePx).asImageBitmap()
    } catch (e: Exception) {
        null
    }

    companion object {
        /** 목록에 그릴 아이콘 크기 */
        private const val ICON_SIZE_DP = 48
    }
}

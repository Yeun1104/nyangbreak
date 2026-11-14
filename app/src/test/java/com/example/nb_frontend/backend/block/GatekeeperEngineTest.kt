package com.example.nb_frontend.backend.block

import com.example.nb_frontend.backend.block.GatekeeperEngine.EndReason
import com.example.nb_frontend.backend.block.GatekeeperEngine.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 상태 전이 규칙 테스트 */
class GatekeeperEngineTest {
    private val yt = "com.google.android.youtube"
    private val insta = "com.instagram.android"
    private val kakao = "com.kakao.talk"
    private val limits = mapOf(yt to 60_000L, insta to 120_000L)
    private val breakMs = 30_000L

    private var now = 0L
    private val e = GatekeeperEngine()

    private fun use(pkg: String?, seconds: Int, paused: Boolean = false, lim: Map<String, Long> = limits) =
        (1..seconds).map { now += 1000; e.tick(now, pkg, lim, breakMs, paused) }

    private fun List<GatekeeperEngine.TickResult>.events() = flatMap { it.events }

    @Test
    fun 감시앱을_쓰면_그_앱_누적시간만_늘어난다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 30)
        assertEquals(30_000L, e.accumulatedOf(yt))
        assertEquals(0L, e.accumulatedOf(insta))
    }

    @Test
    fun 감시대상이_아닌_앱은_세지_않는다() {
        e.tick(now, kakao, limits, breakMs, false)
        use(kakao, 30)
        assertEquals(0L, e.accumulatedOf(kakao))
    }

    @Test
    fun 다른_앱에_다녀와도_누적이_이어진다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 40)
        use(kakao, 100)
        val r = use(yt, 20)
        assertTrue(e.isOnBreak(yt))
        assertTrue(r.events().any { it is Event.BreakStarted && it.packageName == yt })
    }

    @Test
    fun 제한에_도달하면_그_앱_위에만_고양이가_뜬다() {
        e.tick(now, yt, limits, breakMs, false)
        val r = use(yt, 60)
        assertEquals(yt, r.last().overlayPackage)
        assertNull(use(insta, 1).last().overlayPackage)
        assertNull(use(kakao, 1).last().overlayPackage)
    }

    @Test
    fun 휴식중_앱을_나가면_숨고_다시_들어오면_남은시간만큼_보인다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        use(kakao, 10)
        val back = use(yt, 1).last()
        assertEquals(yt, back.overlayPackage)
        assertEquals(19_000L, back.breakRemainingMillis)
    }

    @Test
    fun 휴식시간이_끝나면_해제되고_누적이_0이_된다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        val r = use(yt, 30)
        assertFalse(e.isOnBreak(yt))
        val ended = r.events().filterIsInstance<Event.BreakEnded>().single()
        assertEquals(EndReason.TIMEOUT, ended.reason)
        assertNull(r.last().overlayPackage)
        assertTrue(e.accumulatedOf(yt) <= 1_000L)
    }

    @Test
    fun 휴식시간은_사용시간으로_세지_않는다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        use(yt, 20)
        assertEquals(60_000L, e.accumulatedOf(yt))
    }

    @Test
    fun 고양이를_누르면_바로_해제된다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        val ev = e.tapDismiss(yt, now)
        assertTrue(ev is Event.BreakEnded && ev.reason == EndReason.TAPPED)
        assertNull(use(yt, 1).last().overlayPackage)
        assertNull(e.tapDismiss(insta, now))
    }

    @Test
    fun 일시정지_중에는_시간을_세지_않는다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 10)
        use(yt, 100, paused = true)
        assertEquals(10_000L, e.accumulatedOf(yt))
        assertFalse(e.isOnBreak(yt))
    }

    @Test
    fun 화면이_꺼져있으면_세지_않는다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 10)
        use(null, 100)
        assertEquals(10_000L, e.accumulatedOf(yt))
    }

    @Test
    fun 앱을_나가거나_고양이가_나오면_세션이_저장된다() {
        e.tick(now, yt, limits, breakMs, false)
        val r1 = use(yt, 20) + use(kakao, 1)
        val s1 = r1.events().filterIsInstance<Event.SessionEnded>().single()
        assertEquals(yt, s1.packageName)
        assertEquals(20_000L, s1.durationMillis)

        val r2 = use(yt, 40)
        val s2 = r2.events().filterIsInstance<Event.SessionEnded>().single()
        assertTrue(s2.durationMillis in 40_000L..41_000L)
        assertTrue(r2.events().last() is Event.BreakStarted)
    }

    @Test
    fun 감시를_끈_앱의_휴식은_취소된다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        val r = use(yt, 1, lim = mapOf(insta to 120_000L))
        assertEquals(EndReason.CANCELLED, r.events().filterIsInstance<Event.BreakEnded>().single().reason)
        assertNull(r.last().overlayPackage)
    }

    @Test
    fun 서비스가_오래_멈췄다_재개되면_최대_5초만_더한다() {
        e.tick(now, yt, limits, breakMs, false)
        now += 40_000
        e.tick(now, yt, limits, breakMs, false)
        assertEquals(5_000L, e.accumulatedOf(yt))
    }

    @Test
    fun 저장한_상태로_복원하면_휴식과_누적이_이어진다() {
        e.tick(now, yt, limits, breakMs, false)
        use(yt, 60)
        use(insta, 20)
        val restored = GatekeeperEngine(e.state.copy(lastTickAt = null, session = null))
        assertTrue(restored.isOnBreak(yt))
        assertEquals(20_000L, restored.accumulatedOf(insta))
    }
}

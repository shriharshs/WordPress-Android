package org.wordpress.android.ui.stats.refresh

import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.experimental.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.R
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.stats.VisitsModel
import org.wordpress.android.fluxc.store.InsightsStore
import org.wordpress.android.fluxc.store.InsightsStore.OnInsightsFetched
import org.wordpress.android.fluxc.store.InsightsStore.StatsError
import org.wordpress.android.fluxc.store.InsightsStore.StatsErrorType.GENERIC_ERROR
import org.wordpress.android.test
import org.wordpress.android.ui.stats.refresh.BlockListItem.Title
import org.wordpress.android.ui.stats.refresh.BlockListItem.Type.ITEM
import org.wordpress.android.ui.stats.refresh.BlockListItem.Type.TITLE
import org.wordpress.android.ui.stats.refresh.InsightsItem.Type.FAILED
import org.wordpress.android.ui.stats.refresh.InsightsItem.Type.LIST_INSIGHTS

class TodayStatsUseCaseTest : BaseUnitTest() {
    @Mock lateinit var insightsStore: InsightsStore
    @Mock lateinit var site: SiteModel
    private lateinit var useCase: TodayStatsUseCase
    private val views = 10
    private val visitors = 15
    private val likes = 20
    private val comments = 30
    @Before
    fun setUp() {
        useCase = TodayStatsUseCase(Dispatchers.Unconfined, insightsStore)
    }

    @Test
    fun `maps full stats item to UI model`() = test {
        val forced = false
        val refresh = true
        whenever(insightsStore.fetchTodayInsights(site, forced)).thenReturn(
                OnInsightsFetched(
                        VisitsModel("2018-10-02", views, visitors, likes, 0, comments, 0)
                )
        )

        val result = loadTodayStats(refresh, forced)

        assertThat(result.type).isEqualTo(LIST_INSIGHTS)
        (result as ListInsightItem).apply {
            assertThat(this.items).hasSize(5)
            assertTitle(this.items[0])
            assertViews(this.items[1], showDivider = true)
            assertVisitors(this.items[2], showDivider = true)
            assertLikes(this.items[3], showDivider = true)
            assertComments(this.items[4], showDivider = false)
        }
    }

    @Test
    fun `maps partial stats item to UI model`() = test {
        val forced = false
        val refresh = true
        whenever(insightsStore.fetchTodayInsights(site, forced)).thenReturn(
                OnInsightsFetched(
                        VisitsModel("2018-10-02", 0, visitors, likes, 0, 0, 0)
                )
        )

        val result = loadTodayStats(refresh, forced)

        assertThat(result.type).isEqualTo(LIST_INSIGHTS)
        (result as ListInsightItem).apply {
            assertThat(this.items).hasSize(3)
            assertTitle(this.items[0])
            assertVisitors(this.items[1], showDivider = true)
            assertLikes(this.items[2], showDivider = false)
        }
    }

    @Test
    fun `maps empty stats item to UI model`() = test {
        val forced = false
        val refresh = true
        whenever(insightsStore.fetchTodayInsights(site, forced)).thenReturn(
                OnInsightsFetched(
                        VisitsModel("2018-10-02", 0, 0, 0, 0, 0, 0)
                )
        )

        val result = loadTodayStats(refresh, forced)

        assertThat(result.type).isEqualTo(LIST_INSIGHTS)
        (result as ListInsightItem).apply {
            assertThat(this.items).hasSize(2)
            assertTitle(this.items[0])
            assertThat(this.items[1]).isEqualTo(BlockListItem.Empty)
        }
    }

    @Test
    fun `maps error item to UI model`() = test {
        val forced = false
        val refresh = true
        val message = "Generic error"
        whenever(insightsStore.fetchTodayInsights(site, forced)).thenReturn(
                OnInsightsFetched(
                        StatsError(GENERIC_ERROR, message)
                )
        )

        val result = loadTodayStats(refresh, forced)

        assertThat(result.type).isEqualTo(FAILED)
        (result as Failed).apply {
            assertThat(this.failedType).isEqualTo(R.string.stats_insights_today_stats)
            assertThat(this.errorMessage).isEqualTo(message)
        }
    }

    private fun assertTitle(item: BlockListItem) {
        assertThat(item.type).isEqualTo(TITLE)
        assertThat((item as Title).text).isEqualTo(R.string.stats_insights_today_stats)
    }

    private fun assertViews(blockListItem: BlockListItem, showDivider: Boolean = false) {
        assertThat(blockListItem.type).isEqualTo(ITEM)
        val item = blockListItem as BlockListItem.Item
        assertThat(item.text).isEqualTo(R.string.stats_views)
        assertThat(item.showDivider).isEqualTo(showDivider)
        assertThat(item.icon).isEqualTo(R.drawable.ic_visible_on_grey_dark_24dp)
        assertThat(item.value).isEqualTo(views.toString())
    }

    private fun assertVisitors(blockListItem: BlockListItem, showDivider: Boolean = false) {
        assertThat(blockListItem.type).isEqualTo(ITEM)
        val item = blockListItem as BlockListItem.Item
        assertThat(item.text).isEqualTo(R.string.stats_visitors)
        assertThat(item.showDivider).isEqualTo(showDivider)
        assertThat(item.icon).isEqualTo(R.drawable.ic_user_grey_dark_24dp)
        assertThat(item.value).isEqualTo(visitors.toString())
    }

    private fun assertLikes(blockListItem: BlockListItem, showDivider: Boolean = false) {
        assertThat(blockListItem.type).isEqualTo(ITEM)
        val item = blockListItem as BlockListItem.Item
        assertThat(item.text).isEqualTo(R.string.stats_likes)
        assertThat(item.showDivider).isEqualTo(showDivider)
        assertThat(item.icon).isEqualTo(R.drawable.ic_star_grey_dark_24dp)
        assertThat(item.value).isEqualTo(likes.toString())
    }

    private fun assertComments(blockListItem: BlockListItem, showDivider: Boolean = false) {
        assertThat(blockListItem.type).isEqualTo(ITEM)
        val item = blockListItem as BlockListItem.Item
        assertThat(item.text).isEqualTo(R.string.stats_comments)
        assertThat(item.showDivider).isEqualTo(showDivider)
        assertThat(item.icon).isEqualTo(R.drawable.ic_comment_grey_dark_24dp)
        assertThat(item.value).isEqualTo(comments.toString())
    }

    private suspend fun loadTodayStats(refresh: Boolean, forced: Boolean): InsightsItem {
        var result: InsightsItem? = null
        useCase.liveData.observeForever { result = it }
        useCase.fetch(site, refresh, forced)
        return checkNotNull(result)
    }
}

package recloudstream

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import java.net.URLEncoder

class InvidiousProvider : MainAPI() {
    override var mainUrl = "https://www.binged.com"
    override var name = "Invidious"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "en"
    override val hasMainPage = true

    
    suspend fun getData(titled: String): List<MovieSearchResponse> {
    val response = app.post(
        "$mainUrl/wp-admin/admin-ajax.php",
        data = mapOf(
            "filters[search]" to "",
            "filters[recommend]" to "false",
            "filters[date-from]" to "",
            "filters[date-to]" to "",
            "filters[mode]" to "streaming-soon",
            "filters[page]" to "0",
            "action" to "mi_events_load_data",
            "mode" to "$titled",
            "start" to "0",
            "length" to "20",
            "customcatalog" to "0"
        ),
        headers = mapOf(
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Accept" to "*/*",
            "X-Requested-With" to "XMLHttpRequest",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
            "Referer" to "$mainUrl"
        )
    ).text

    val json = tryParseJson<Map<String, Any>>(response)
    val dataList = json?.get("data") as? List<Map<String, Any>>

    val movies = dataList?.map { entry ->
        newMovieSearchResponse(
            name = entry["title"].toString(),
            url = entry["link"].toString(),
            type = TvType.Movie
        ) {
            this.posterUrl = entry["image"].toString()
            //this.plot = entry["review"].toString()
        }
    } ?: emptyList()

    return movies
    }
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val stsoon = getData("streaming-soon")
        val stnow = getData("streaming-now")
        return newHomePageResponse(
            listOf(
                HomePageList("Streaming Soon", stsoon, false),
                HomePageList("Streaming Now", stnow, false),
                
            ), true
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url, cacheTime = 60).document
        val title = doc.select("h1").first()!!.text()
        val dt = doc.select("div.single-mevents-meta").text()
        val dtsplit = dt.split("|")
        val imageUrl = doc.select("meta")[15].attr("content").toString()

val tags = listOf(
    doc.select("span.single-mevents-platforms-row-date").text().toString(),
    doc.select("span.rating-span").first().text().toString(),
    if (dtsplit.size > 1) dtsplit[1] else "",
    if (dtsplit.size > 2) dtsplit[2] else "",
    if (dtsplit.size > 3) dtsplit[3] else ""
)
                          
                          
        //val imageUrl = app.select("meta[property=og:image]").first().text()
        val plot = doc.select("p").first()!!.text()
        val year= dtsplit[0].toIntOrNull()
        return newMovieLoadResponse(title, url, TvType.Movie,' ') {
                this.posterUrl = imageUrl
                this.year = year
                this.plot = plot
                this.tags= tags
        }
      
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return false
    }

    companion object {
        fun String.encodeUri() = URLEncoder.encode(this, "utf8")
    }
}

package com.example

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

data class ResolvedPoseMedia(
    val imageUrl: String,
    val suggestedTitle: String,
    val sourceHost: String
)

object LinkResolver {
    private const val BROWSER_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    private const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    // Social link-expanding crawler User-Agents permitted by Instagram, Facebook, and Pinterest for OG meta tags
    private const val CRAWLER_FB_USER_AGENT =
        "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"

    private const val CRAWLER_TELEGRAM_USER_AGENT =
        "TelegramBot (like TwitterBot)"

    private const val CRAWLER_WHATSAPP_USER_AGENT =
        "WhatsApp/2.24.16.78 A"

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    private val ogImageRegexes = listOf(
        Regex("""<meta[^>]+property=["']og:image(?::secure_url|:url)?["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image(?::secure_url|:url)?["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+name=["']twitter:image(?::src)?["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+name=["']twitter:image(?::src)?["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+itemprop=["']image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+itemprop=["']image["']""", RegexOption.IGNORE_CASE),
        Regex("""<link[^>]+rel=["']image_src["'][^>]+href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    )

    private val ogTitleRegexes = listOf(
        Regex("""<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:title["']""", RegexOption.IGNORE_CASE),
        Regex("""<meta[^>]+name=["']twitter:title["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE)
    )

    // Matches Pinterest CDN high-res images (i.pinimg.com only — excludes s.pinimg.com generic marketing gradients)
    private val pinimgRegex = Regex(
        """https://i\.pinimg\.com/(?:originals|1200x|736x|564x|474x)/[a-zA-Z0-9/_.-]+\.(?:jpg|jpeg|png|webp)""",
        RegexOption.IGNORE_CASE
    )

    private val pinterestPinIdRegex = Regex("""pinterest\.[a-z.]+/pin/(?:[a-zA-Z0-9_-]*?--)?(\d+)""", RegexOption.IGNORE_CASE)

    private val instagramShortcodeRegex = Regex(
        """instagram\.com/(?:[a-zA-Z0-9_.-]+/)?(?:p|reel|reels|tv)/([A-Za-z0-9_-]+)""",
        RegexOption.IGNORE_CASE
    )

    private val instagramEmbedImgRegex = listOf(
        Regex("""class=["']EmbeddedMediaImage["'][^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""src=["']([^"']+)["'][^>]+class=["']EmbeddedMediaImage["']""", RegexOption.IGNORE_CASE),
        Regex(""""display_url"\s*:\s*"([^"]+)""""),
        Regex(""""thumbnail_src"\s*:\s*"([^"]+)"""")
    )

    private val fbAndInstaCdnRegex = Regex(
        """https:(?:\\/|/){2}scontent[^\s"'<>\\]+?\.(?:cdninstagram\.com|fbcdn\.net)(?:\\/|/)[^\s"'<>\\]+""",
        RegexOption.IGNORE_CASE
    )

    private val googleImgUrlParamRegex = Regex("""[?&](?:imgurl|mediaurl)=([^&"'\s<>]+)""", RegexOption.IGNORE_CASE)
    private val gstaticThumbRegex = Regex("""https://encrypted-tbn\d*\.gstatic\.com/images\?q=[^"'\s<>\\]+""", RegexOption.IGNORE_CASE)

    /**
     * Extracts the first valid HTTP/HTTPS URL from shared plain text.
     */
    fun extractUrlFromText(sharedText: String): String? {
        return urlRegex.find(sharedText.trim())?.value?.trimEnd('.', ',', ')', ']', ';')
    }

    /**
     * Resolves a shared URL from Pinterest (pin.it / pinterest.com), Instagram, Facebook,
     * Google Search Images (images.app.goo.gl / google.com/imgres), Snapchat, or any web page.
     * If [context] is provided, downloads and caches the resolved image locally so CDNs with
     * signed headers/referers or expiring tokens always render in Coil and WindowManager overlays.
     */
    suspend fun resolveSharedUrl(
        rawTextOrUrl: String,
        context: Context? = null
    ): Result<ResolvedPoseMedia> = withContext(Dispatchers.IO) {
        runCatching {
            val initialUrl = extractUrlFromText(rawTextOrUrl)
                ?: throw IllegalArgumentException("No valid HTTP/HTTPS link found in shared text.")

            val candidates = mutableListOf<String>()
            var resolvedTitle = ""
            var finalHost = runCatching { java.net.URI(initialUrl).host.orEmpty() }.getOrDefault("Web")

            // 1. Check if initial URL already contains Google Images ?imgurl= parameter
            extractGoogleImgUrlParam(initialUrl)?.let { candidates.add(it) }

            // 2. Follow initial redirects to discover canonical URL (crucial for pin.it, images.app.goo.gl, share.google, instagram.com/share, fb.watch)
            val redirectedUrl = resolveFinalRedirectUrl(initialUrl) ?: initialUrl
            finalHost = runCatching { java.net.URI(redirectedUrl).host.orEmpty() }.getOrDefault(finalHost)

            extractGoogleImgUrlParam(redirectedUrl)?.let { candidates.add(it) }

            val lowerRedirected = redirectedUrl.lowercase()

            // 3. Platform-Specific Resolution Strategies
            when {
                // --- PINTEREST (pin.it / pinterest.com) ---
                lowerRedirected.contains("pinterest.") || initialUrl.lowercase().contains("pin.it") -> {
                    val pinResult = resolvePinterestUrl(initialUrl, redirectedUrl)
                    candidates.addAll(pinResult.first)
                    if (pinResult.second.isNotBlank()) resolvedTitle = pinResult.second
                }

                // --- INSTAGRAM (instagram.com / ig.me) ---
                lowerRedirected.contains("instagram.com") || lowerRedirected.contains("ig.me") -> {
                    val igResult = resolveInstagramUrl(redirectedUrl)
                    candidates.addAll(igResult.first)
                    if (igResult.second.isNotBlank()) resolvedTitle = igResult.second
                }

                // --- FACEBOOK (facebook.com / fb.com / fb.watch) ---
                lowerRedirected.contains("facebook.com") || lowerRedirected.contains("fb.com") || lowerRedirected.contains("fb.watch") -> {
                    val fbResult = resolveFacebookUrl(redirectedUrl)
                    candidates.addAll(fbResult.first)
                    if (fbResult.second.isNotBlank()) resolvedTitle = fbResult.second
                }

                // --- GOOGLE IMAGES / GENERIC WEB ---
                else -> {
                    val webResult = resolveGenericOrGoogleUrl(redirectedUrl)
                    candidates.addAll(webResult.first)
                    if (webResult.second.isNotBlank()) resolvedTitle = webResult.second
                }
            }

            // Deduplicate and clean all candidate image URLs
            val distinctCandidates = candidates
                .map { cleanUrlString(it) }
                .filter { it.startsWith("http://") || it.startsWith("https://") }
                .filterNot { isGenericPlaceholderOrBanner(it) }
                .distinct()

            if (distinctCandidates.isEmpty()) {
                throw IllegalStateException("Could not extract a public reference image from $finalHost. If the post is private, try sharing the image directly.")
            }

            // If Context is available, download and verify the first working image into local storage
            // so Instagram/Facebook/Google hotlink-protected CDNs always display cleanly.
            val finalImageUri = if (context != null) {
                distinctCandidates.firstNotNullOfOrNull { candidateUrl ->
                    downloadAndCacheImageLocally(context, candidateUrl, redirectedUrl)
                } ?: distinctCandidates.first()
            } else {
                distinctCandidates.first()
            }

            val cleanTitle = sanitizeSuggestedTitle(resolvedTitle, finalHost)

            ResolvedPoseMedia(
                imageUrl = finalImageUri,
                suggestedTitle = cleanTitle,
                sourceHost = finalHost
            )
        }
    }

    /**
     * Resolves Pinterest links (`pin.it/...` and `pinterest.com/pin/...`).
     * Crucial fix: strips `/sent/` and `?invite_code=...&sfo=1` parameters that cause Pinterest
     * to return the "Check out this Pin from <User>" generic pink/orange gradient banner!
     */
    private fun resolvePinterestUrl(initialUrl: String, redirectedUrl: String): Pair<List<String>, String> {
        val foundUrls = mutableListOf<String>()
        var title = ""

        // Step 1: Find the numeric Pin ID from redirectedUrl, or fetch initialUrl HTML to find /pin/<id>
        var pinId = pinterestPinIdRegex.find(redirectedUrl)?.groupValues?.getOrNull(1)

        if (pinId.isNullOrBlank()) {
            val html = fetchPageHtml(initialUrl, CRAWLER_FB_USER_AGENT)
                ?: fetchPageHtml(redirectedUrl, DESKTOP_USER_AGENT)
                ?: ""
            pinId = pinterestPinIdRegex.find(html)?.groupValues?.getOrNull(1)
            extractPinimgCandidatesFromText(html, foundUrls)
        }

        // Step 2: Construct clean canonical Pin URL without ?invite_code or /sent/
        val canonicalPinUrl = if (!pinId.isNullOrBlank()) {
            "https://www.pinterest.com/pin/$pinId/"
        } else {
            redirectedUrl.substringBefore('?').replace("/sent/", "/")
        }

        // Step 3: Query Pinterest public oEmbed endpoint (returns thumbnail_url on i.pinimg.com)
        val oembedUrl = "https://www.pinterest.com/oembed.json?url=$canonicalPinUrl"
        fetchPageHtml(oembedUrl, DESKTOP_USER_AGENT)?.let { oembedJson ->
            Regex(""""thumbnail_url"\s*:\s*"([^"]+)"""").find(oembedJson)?.groupValues?.getOrNull(1)?.let { thumb ->
                val cleanThumb = cleanUrlString(thumb)
                foundUrls.add(upgradePinterestResolution(cleanThumb))
                foundUrls.add(cleanThumb)
            }
            Regex(""""title"\s*:\s*"([^"]+)"""").find(oembedJson)?.groupValues?.getOrNull(1)?.let { t ->
                title = decodeHtmlAndJsonEscapes(t)
            }
        }

        // Step 4: Fetch the clean canonical Pin page with Crawler & Desktop User-Agents
        for (ua in listOf(CRAWLER_FB_USER_AGENT, DESKTOP_USER_AGENT)) {
            val html = fetchPageHtml(canonicalPinUrl, ua) ?: continue
            extractPinimgCandidatesFromText(html, foundUrls)
            extractOgImageFromHtml(html)?.let { og ->
                if (og.contains("i.pinimg.com")) {
                    foundUrls.add(upgradePinterestResolution(og))
                    foundUrls.add(og)
                }
            }
            if (title.isBlank()) {
                title = extractTitleFromHtml(html).orEmpty()
            }
            if (foundUrls.any { it.contains("i.pinimg.com") }) break
        }

        return foundUrls to title.ifBlank { "Pinterest Pose Reference" }
    }

    private fun extractPinimgCandidatesFromText(html: String, target: MutableList<String>) {
        val unescaped = decodeHtmlAndJsonEscapes(html)
        val matches = pinimgRegex.findAll(unescaped)
            .map { it.value }
            .filterNot { it.contains("/75x75") || it.contains("/50x50") || it.contains("avatar") }
            .sortedByDescending {
                when {
                    it.contains("/originals/") -> 4
                    it.contains("/1200x/") -> 3
                    it.contains("/736x/") -> 2
                    else -> 1
                }
            }
            .toList()
        matches.forEach { m ->
            if (m.contains("/236x/") || m.contains("/474x/") || m.contains("/564x/")) {
                target.add(upgradePinterestResolution(m))
            }
            target.add(m)
        }
    }

    /**
     * Resolves Instagram posts/reels (`instagram.com/p/...`, `/reel/...`, `/share/...`).
     * Uses both crawler link-expansion User-Agents and Instagram's public `/embed/captioned/` endpoint.
     */
    private fun resolveInstagramUrl(redirectedUrl: String): Pair<List<String>, String> {
        val foundUrls = mutableListOf<String>()
        var title = ""

        // Extract shortcode if present
        val shortcode = instagramShortcodeRegex.find(redirectedUrl)?.groupValues?.getOrNull(1)
        val canonicalPostUrl = if (!shortcode.isNullOrBlank()) {
            "https://www.instagram.com/p/$shortcode/"
        } else {
            redirectedUrl.substringBefore('?')
        }

        // Strategy 1: Public Instagram /embed/captioned/ endpoint (works without login!)
        if (!shortcode.isNullOrBlank()) {
            for (embedPath in listOf("embed/captioned/", "embed/")) {
                val embedUrl = "https://www.instagram.com/p/$shortcode/$embedPath"
                val embedHtml = fetchPageHtml(embedUrl, DESKTOP_USER_AGENT) ?: continue
                instagramEmbedImgRegex.forEach { regex ->
                    regex.findAll(embedHtml).forEach { match ->
                        match.groupValues.getOrNull(1)?.let { raw ->
                            val cleaned = cleanUrlString(raw)
                            if (!cleaned.contains("s150x150") && !cleaned.contains("profile_pic")) {
                                foundUrls.add(cleaned)
                            }
                        }
                    }
                }
                fbAndInstaCdnRegex.findAll(embedHtml).forEach { match ->
                    val cleaned = cleanUrlString(match.value)
                    if (!cleaned.contains("s150x150") && !cleaned.contains("profile_pic")) {
                        foundUrls.add(cleaned)
                    }
                }
                if (foundUrls.isNotEmpty()) break
            }
        }

        // Strategy 2: Crawler User-Agents (TelegramBot, facebookexternalhit, WhatsApp) for og:image
        for (crawlerUa in listOf(CRAWLER_TELEGRAM_USER_AGENT, CRAWLER_FB_USER_AGENT, CRAWLER_WHATSAPP_USER_AGENT)) {
            val html = fetchPageHtml(canonicalPostUrl, crawlerUa) ?: continue
            extractOgImageFromHtml(html)?.let { og ->
                foundUrls.add(cleanUrlString(og))
            }
            fbAndInstaCdnRegex.findAll(html).forEach { match ->
                val cleaned = cleanUrlString(match.value)
                if (!cleaned.contains("s150x150")) {
                    foundUrls.add(cleaned)
                }
            }
            if (title.isBlank()) {
                title = extractTitleFromHtml(html).orEmpty()
            }
            if (foundUrls.isNotEmpty()) break
        }

        // Strategy 3: Direct media redirect endpoint fallback
        if (!shortcode.isNullOrBlank()) {
            foundUrls.add("https://www.instagram.com/p/$shortcode/media/?size=l")
        }

        return foundUrls to title.ifBlank { "Instagram Pose Reference" }
    }

    /**
     * Resolves Facebook photo/post links (`facebook.com/...`, `fb.watch/...`, `fb.com/...`).
     */
    private fun resolveFacebookUrl(redirectedUrl: String): Pair<List<String>, String> {
        val foundUrls = mutableListOf<String>()
        var title = ""

        for (ua in listOf(CRAWLER_FB_USER_AGENT, CRAWLER_WHATSAPP_USER_AGENT, CRAWLER_TELEGRAM_USER_AGENT, DESKTOP_USER_AGENT)) {
            val html = fetchPageHtml(redirectedUrl, ua) ?: continue
            extractOgImageFromHtml(html)?.let { og ->
                val cleaned = cleanUrlString(og)
                if (!cleaned.contains("fb_icon") && !cleaned.contains("rsrc.php")) {
                    foundUrls.add(cleaned)
                }
            }
            fbAndInstaCdnRegex.findAll(html).forEach { match ->
                val cleaned = cleanUrlString(match.value)
                if (!cleaned.contains("p50x50") && !cleaned.contains("s100x100")) {
                    foundUrls.add(cleaned)
                }
            }
            if (title.isBlank()) {
                title = extractTitleFromHtml(html).orEmpty()
            }
            if (foundUrls.isNotEmpty()) break
        }

        return foundUrls to title.ifBlank { "Facebook Pose Reference" }
    }

    /**
     * Resolves Google Search Images (`images.app.goo.gl`, `share.google`, `google.com/imgres`)
     * and general web pages.
     */
    private fun resolveGenericOrGoogleUrl(targetUrl: String): Pair<List<String>, String> {
        val foundUrls = mutableListOf<String>()
        var title = ""

        val lowerUrl = targetUrl.lowercase().substringBefore('?')
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".webp")) {
            foundUrls.add(targetUrl)
            return foundUrls to "Direct Image Pose"
        }

        for (ua in listOf(BROWSER_USER_AGENT, DESKTOP_USER_AGENT, CRAWLER_FB_USER_AGENT)) {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", ua)
                .header("Accept", "text/html,application/xhtml+xml,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            runCatching {
                httpClient.newCall(request).execute().use { response ->
                    val finalUrl = response.request.url.toString()
                    extractGoogleImgUrlParam(finalUrl)?.let { foundUrls.add(it) }

                    val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                    if (contentType.startsWith("image/")) {
                        foundUrls.add(finalUrl)
                        return@use
                    }

                    val html = response.body?.string().orEmpty()
                    if (html.isBlank()) return@use

                    // 1. Check for embedded Google Images ?imgurl= inside page scripts/links
                    googleImgUrlParamRegex.findAll(html).forEach { m ->
                        m.groupValues.getOrNull(1)?.let { encoded ->
                            runCatching { URLDecoder.decode(cleanUrlString(encoded), "UTF-8") }
                                .getOrNull()
                                ?.let { decoded ->
                                    if (decoded.startsWith("http")) foundUrls.add(decoded)
                                }
                        }
                    }

                    // 2. Check OpenGraph / Twitter / Schema.org image tags
                    extractOgImageFromHtml(html)?.let { og ->
                        foundUrls.add(resolveRelativeUrl(finalUrl, cleanUrlString(og)))
                    }

                    // 3. Check Pinterest CDN images if the page embeds pins
                    extractPinimgCandidatesFromText(html, foundUrls)

                    // 4. Check JSON-LD "contentUrl" or "image"
                    Regex(""""(?:contentUrl|image)"\s*:\s*"(https?://[^"]+\.(?:jpg|jpeg|png|webp)[^"]*)"""", RegexOption.IGNORE_CASE)
                        .findAll(html)
                        .forEach { m ->
                            m.groupValues.getOrNull(1)?.let { foundUrls.add(cleanUrlString(it)) }
                        }

                    // 5. Check Google gstatic thumbnails (`encrypted-tbn0.gstatic.com`)
                    gstaticThumbRegex.findAll(decodeHtmlAndJsonEscapes(html)).forEach { m ->
                        foundUrls.add(cleanUrlString(m.value))
                    }

                    // 6. Check standard <img src="https://..."> tags on the page
                    Regex("""<img[^>]+(?:data-src|src)=["'](https?://[^"']+)["']""", RegexOption.IGNORE_CASE)
                        .findAll(html)
                        .mapNotNull { it.groupValues.getOrNull(1) }
                        .filterNot {
                            val l = it.lowercase()
                            l.endsWith(".svg") || l.endsWith(".gif") || l.contains("logo") || l.contains("icon") || l.contains("avatar") || l.contains("1x1")
                        }
                        .take(4)
                        .forEach { foundUrls.add(cleanUrlString(it)) }

                    if (title.isBlank()) {
                        title = extractTitleFromHtml(html).orEmpty()
                    }
                }
            }
            if (foundUrls.isNotEmpty()) break
        }

        return foundUrls to title.ifBlank { "Web Pose Reference" }
    }

    private fun resolveFinalRedirectUrl(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val finalUrl = response.request.url.toString()
                // Also check if Google Dynamic Links / Firebase shortlinks embed a ?link= parameter
                val linkParam = Regex("""[?&]link=([^&]+)""").find(finalUrl)?.groupValues?.getOrNull(1)
                if (!linkParam.isNullOrBlank()) {
                    URLDecoder.decode(linkParam, "UTF-8")
                } else {
                    finalUrl
                }
            }
        }.getOrNull()
    }

    private fun fetchPageHtml(url: String, userAgent: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 200) return@use null
                response.body?.string()
            }
        }.getOrNull()
    }

    /**
     * Downloads candidate image bytes with proper User-Agent & Referer headers, verifies
     * that it decodes as a real Bitmap (at least 64x64px), and caches it to internal storage.
     */
    private fun downloadAndCacheImageLocally(
        context: Context,
        imageUrl: String,
        refererUrl: String
    ): String? {
        val request = Request.Builder()
            .url(imageUrl)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .header("Referer", refererUrl)
            .build()

        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val bytes = response.body?.bytes() ?: return@use null
                if (bytes.size < 1024) return@use null // Ignore tiny 1x1 tracking pixels

                // Verify bytes decode to a real bitmap with reasonable dimensions
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                if (bounds.outWidth < 60 || bounds.outHeight < 60) return@use null

                val dir = File(context.filesDir, "shared_poses").apply { mkdirs() }
                val destFile = File(dir, "resolved_${System.currentTimeMillis()}.jpg")
                FileOutputStream(destFile).use { out ->
                    out.write(bytes)
                }
                Uri.fromFile(destFile).toString()
            }
        }.getOrNull()
    }

    private fun extractGoogleImgUrlParam(url: String): String? {
        val raw = googleImgUrlParamRegex.find(url)?.groupValues?.getOrNull(1) ?: return null
        return runCatching {
            URLDecoder.decode(cleanUrlString(raw), "UTF-8")
        }.getOrNull()?.takeIf { it.startsWith("http") }
    }

    private fun extractOgImageFromHtml(html: String): String? {
        return ogImageRegexes.firstNotNullOfOrNull { regex ->
            regex.find(html)?.groupValues?.getOrNull(1)?.let { cleanUrlString(it) }
        }
    }

    private fun extractTitleFromHtml(html: String): String? {
        return ogTitleRegexes.firstNotNullOfOrNull { regex ->
            regex.find(html)?.groupValues?.getOrNull(1)?.let { decodeHtmlAndJsonEscapes(it).trim() }
        }
    }

    private fun isGenericPlaceholderOrBanner(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("s.pinimg.com/images/") ||
            lower.contains("facebook_share_image") ||
            lower.contains("default_avatar") ||
            lower.contains("rsrc.php") ||
            lower.contains("static.cdninstagram.com/rsrc.php") ||
            lower.contains("googlelogo") ||
            lower.endsWith(".svg")
    }

    private fun resolveRelativeUrl(baseUrl: String, candidate: String): String {
        if (candidate.startsWith("http://") || candidate.startsWith("https://")) return candidate
        if (candidate.startsWith("//")) return "https:$candidate"
        return runCatching {
            java.net.URI(baseUrl).resolve(candidate).toString()
        }.getOrDefault(candidate)
    }

    private fun upgradePinterestResolution(url: String): String {
        return if (url.contains("i.pinimg.com/")) {
            url.replace(Regex("""/(?:236x|474x|564x)/"""), "/736x/")
        } else {
            url
        }
    }

    private fun sanitizeSuggestedTitle(rawTitle: String, host: String): String {
        val cleaned = rawTitle
            .replace(Regex("""\s*\|\s*Pinterest.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Pinterest\s*[-–—:]\s*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Check out this Pin.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^www\.(?:instagram|facebook|pinterest|google)\.com$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Instagram$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^Login\s*•\s*Instagram.*$""", RegexOption.IGNORE_CASE), "")
            .trim()
            .take(42)

        return if (cleaned.isNotBlank()) {
            cleaned
        } else {
            when {
                host.contains("pin") -> "Pinterest Pose Reference"
                host.contains("instagram") -> "Instagram Pose Reference"
                host.contains("facebook") || host.contains("fb.") -> "Facebook Pose Reference"
                host.contains("google") -> "Google Image Reference"
                host.contains("snapchat") -> "Snapchat Pose Reference"
                else -> "Web Pose Reference"
            }
        }
    }

    private fun cleanUrlString(input: String): String {
        return decodeHtmlAndJsonEscapes(input).trim()
    }

    private fun decodeHtmlAndJsonEscapes(input: String): String {
        return input
            .replace("""\/""", "/")
            .replace("""\u0026""", "&")
            .replace("""\u003d""", "=")
            .replace("""\u003D""", "=")
            .replace("""\u0025""", "%")
            .replace("&amp;", "&")
            .replace("&#38;", "&")
            .replace("&#x26;", "&")
            .replace("&#x3a;", ":")
            .replace("&#x2f;", "/")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }
}

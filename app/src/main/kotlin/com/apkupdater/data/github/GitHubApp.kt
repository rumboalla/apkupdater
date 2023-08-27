package com.apkupdater.data.github

data class GitHubApp(
    val packageName: String,
    val user: String,
    val repo: String
)

val GitHubApps = listOf(
    GitHubApp("com.apkupdater", "rumboalla", "apkupdater"),
    GitHubApp("org.schabi.newpipe", "TeamNewPipe", "NewPipe"),
    GitHubApp("eu.faircode.netguard", "M66B", "NetGuard"),
    GitHubApp("org.adaway", "AdAway", "AdAway"),
    GitHubApp("com.duckduckgo.mobile.android", "duckduckgo", "Android"),
    GitHubApp("com.foobnix.pro.pdf.reader", "foobnix", "LibreraReader"),
    GitHubApp("com.kunzisoft.keepass.free", "Kunzisoft", "KeePassDX"),
    GitHubApp("dev.imranr.obtainium", "ImranR98", "Obtainium"),
    GitHubApp("org.proninyaroslav.libretorrent", "proninyaroslav", "libretorrent"),
    GitHubApp("eu.faircode.email", "M66B", "FairEmail"),
    GitHubApp("com.fsck.k9", "thundernest", "k-9"),
    GitHubApp("de.tutao.tutanota", "tutao", "tutanota"),
    GitHubApp("com.shabinder.spotiflyer", "Shabinder", "SpotiFlyer"),
    GitHubApp("org.koreader.launcher", "koreader", "koreader"),
    GitHubApp("com.amaze.filemanager", "TeamAmaze", "AmazeFileManager"),
    GitHubApp("me.zhanghai.android.files", "zhanghai", "MaterialFiles"),
    GitHubApp("dev.ukanth.ufirewall", "ukanth", "afwall"),
    GitHubApp("com.ichi2.anki", "ankidroid", "Anki-Android"),
    GitHubApp("com.simplemobiletools.flashlight", "SimpleMobileTools", "Simple-Flashlight"),
    GitHubApp("ru.tech.imageresizershrinker", "T8RIN", "ImageToolbox"),
    GitHubApp("com.zhenxiang.superimage", "Lucchetto", "SuperImage"),
    GitHubApp("me.rosuh.easywatermark", "rosuH", "EasyWatermark"),
    GitHubApp("deckers.thibault.aves", "deckerst", "aves"),
    GitHubApp("com.simplemobiletools.gallery.pro", "SimpleMobileTools", "Simple-Gallery"),
    GitHubApp("org.cromite.cromite", "uazo", "cromite"),
    GitHubApp("de.grobox.liberario", "grote", "Transportr"),
    GitHubApp("app.organicmaps", "organicmaps", "organicmaps"),
    GitHubApp("com.kylecorry.trail_sense", "kylecorry31", "Trail-Sense")
    GitHubApp("com.arjanvlek.oxygenupdater", "oxygen-updater", "oxygen-updater")
    GitHubApp("com.beemdevelopment.aegis", "beemdevelopment", "Aegis")
    GitHubApp("code.name.monkey.retromusic", "RetroMusicPlayer", "RetroMusicPlayer")
    GitHubApp("org.blokada.origin.alarm", "blokadaorg", "blokada")
)

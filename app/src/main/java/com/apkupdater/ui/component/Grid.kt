package com.apkupdater.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyGridScope
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import com.apkupdater.prefs.Prefs
import org.koin.androidx.compose.get

@Composable
fun InstalledGrid(content: LazyGridScope.() -> Unit) = LazyVerticalGrid(
    columns =  GridCells.Fixed(getNumColumns(LocalConfiguration.current.orientation)),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content
)

@Composable
fun TvInstalledGrid(content: TvLazyGridScope.() -> Unit) = TvLazyVerticalGrid(
    columns = TvGridCells.Fixed(getTvNumColumns()),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content
)

@Composable
fun getNumColumns(orientation: Int): Int {
    val prefs = get<Prefs>()
    return if(orientation == Configuration.ORIENTATION_PORTRAIT)
        prefs.portraitColumns.get()
    else
        prefs.landscapeColumns.get()
}

@Composable
fun getTvNumColumns(): Int {
    return if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
        1
    else
        2
}

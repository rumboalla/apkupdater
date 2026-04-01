package com.durcheins.apkupdater.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.durcheins.apkupdater.prefs.Prefs
import org.koin.compose.koinInject

@Composable
fun LoadingGrid() = ShimmeringGrid()

@Composable
fun ShimmeringGrid() = InstalledGrid(false) {
    items(16) {
        Box(Modifier.height(170.dp).shimmering(true))
    }
}

@Composable
fun EmptyGrid(
    text: String = ""
) = Box(Modifier.fillMaxSize()) {
    if (text.isNotEmpty()) {
        MediumTitle(text, Modifier.align(Alignment.Center))
    }
    LazyColumn(Modifier.fillMaxSize()) {}
}

@Composable
fun InstalledGrid(
    scroll: Boolean = true,
    content: LazyGridScope.() -> Unit
) = LazyVerticalGrid(
    columns = GridCells.Fixed(getNumColumns(LocalConfiguration.current.orientation)),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content,
    userScrollEnabled = scroll,
    modifier = Modifier.fillMaxSize()
)

@Composable
fun getNumColumns(orientation: Int): Int {
    val prefs = koinInject<Prefs>()
    return if(orientation == Configuration.ORIENTATION_PORTRAIT)
        prefs.portraitColumns.get()
    else
        prefs.landscapeColumns.get()
}

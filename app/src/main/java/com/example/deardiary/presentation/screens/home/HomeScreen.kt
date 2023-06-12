@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)

package com.example.deardiary.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deardiary.R
import com.example.deardiary.domain.model.Diary
import com.example.deardiary.presentation.components.ClickableIcon
import com.example.deardiary.presentation.screens.home.components.DateHeader
import com.example.deardiary.presentation.screens.home.components.DiaryHolder
import java.time.LocalDate

@Composable
fun HomeScreen(
    homeScreenState: HomeScreenState,
    drawerState: DrawerState,
    signingOut: Boolean,
    deletingDiaries: Boolean,
    onSignOutClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onMenuClick: () -> Unit,
    navigateToWrite: (String?) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState,
        signingOut = signingOut,
        deletingDiaries = deletingDiaries,
        onSignOutClick = onSignOutClick,
        onDeleteAllClick = onDeleteAllClick
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { HomeTopAppBar(scrollBehavior = scrollBehavior, onMenuClick = onMenuClick) },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigateToWrite(null) }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Diary")
                }
            }
        ) {

            AnimatedContent(
                targetState = homeScreenState,
                transitionSpec = { fadeIn(tween(500)) with fadeOut() },
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) { targetHomeState ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    when (targetHomeState) {
                        is HomeScreenState.DataLoaded -> {
                            HomeBody(
                                items = targetHomeState.items,
                                onClick = navigateToWrite
                            )
                        }

                        is HomeScreenState.Error -> {
                            EmptyPage(
                                title = stringResource(id = R.string.an_error_occurred),
                                subtitle = targetHomeState.message,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        HomeScreenState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    items: Map<LocalDate, List<Diary>>,
    onClick: (String) -> Unit
) {
    Box(modifier = modifier) {
        if (items.isEmpty()) EmptyPage(modifier = Modifier.align(Alignment.Center))
        else {
            LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
                items.forEach { (localeDate, diaries) ->
                    stickyHeader(localeDate) {
                        DateHeader(
                            localeDate = localeDate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 14.dp)
                        )
                    }

                    items(
                        items = diaries,
                        key = { diary -> diary._id.toString() }
                    ) { diary ->
                        DiaryHolder(diary = diary, onClick = onClick)
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    signingOut: Boolean,
    deletingDiaries: Boolean,
    onSignOutClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Image(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo"
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google logo",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.sign_out),
                                modifier = Modifier.weight(1f)
                            )

                            if (signingOut) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                    },
                    selected = false,
                    onClick = onSignOutClick
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete all icon",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.delete_all_diaries),
                                modifier = Modifier.weight(1f)
                            )

                            if (deletingDiaries) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                    },
                    selected = false,
                    onClick = onDeleteAllClick
                )
            }
        },
        content = content
    )
}

@Composable
fun HomeTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onMenuClick: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            ClickableIcon(
                imageVector = Icons.Default.Menu,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onMenuClick
            )
        },
        title = { Text(text = stringResource(id = R.string.diary)) },
        actions = {
            ClickableIcon(
                imageVector = Icons.Default.DateRange,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {}
            )
        },
        scrollBehavior = scrollBehavior
    )
}


@Composable
fun EmptyPage(
    modifier: Modifier = Modifier,
    title: String = "Empty Diary",
    subtitle: String = "Write Something"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
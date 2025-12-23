package com.example.myapplication

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rssuygulamasi.BlogViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val viewModel: BlogViewModel = viewModel()
                       
                        val posts by viewModel.blogPosts.collectAsState()
                        val favorites by viewModel.favoritePosts.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()
                        val selectedCategory by viewModel.selectedCategory.collectAsState()
                        val isShowingFavorites by viewModel.isShowingFavorites.collectAsState()

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(if (isShowingFavorites) "Favorilerim" else "Haber Akışı")
                                    },
                                    actions = {
                                        
                                        IconButton(onClick = { viewModel.showFavorites(!isShowingFavorites) }) {
                                            Icon(
                                                imageVector = if (isShowingFavorites) Icons.Default.List else Icons.Default.Favorite,
                                                contentDescription = "Favoriler",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        ) { innerPadding ->
                            Column(modifier = Modifier.padding(innerPadding)) {

                                
                                if (!isShowingFavorites) {
                                    CategoryBar(
                                        categories = viewModel.categories,
                                        selectedCategory = selectedCategory,
                                        onCategorySelected = { viewModel.onCategorySelected(it) }
                                    )
                                }

                                
                                val listToShow = if (isShowingFavorites) favorites else posts

                                if (isShowingFavorites && favorites.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Henüz favori eklemediniz.")
                                    }
                                } else {
                                    
                                    if (!isShowingFavorites) {
                                        PullToRefreshBox(
                                            isRefreshing = isLoading,
                                            onRefresh = { viewModel.fetchRssData() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            BlogList(
                                                posts = listToShow,
                                                favorites = favorites, 
                                                modifier = Modifier.fillMaxSize(),
                                                onPostClick = { url ->
                                                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                                    navController.navigate("detail/$encodedUrl")
                                                },
                                                onFavoriteClick = { post -> viewModel.toggleFavorite(post) }
                                            )
                                        }
                                    } else {
                                        
                                        BlogList(
                                            posts = listToShow,
                                            favorites = favorites,
                                            modifier = Modifier.weight(1f),
                                            onPostClick = { url ->
                                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                                navController.navigate("detail/$encodedUrl")
                                            },
                                            onFavoriteClick = { post -> viewModel.toggleFavorite(post) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    composable(
                        route = "detail/{url}",
                        arguments = listOf(navArgument("url") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val url = backStackEntry.arguments?.getString("url") ?: ""
                        val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
                        ArticleScreen(url = decodedUrl)
                    }
                }
            }
        }
    }
}



@Composable
fun CategoryBar(
    categories: List<Pair<String, String>>,
    selectedCategory: Pair<String, String>,
    onCategorySelected: (Pair<String, String>) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = (category == selectedCategory)
            if (isSelected) {
                Button(onClick = {}) { Text(category.first) }
            } else {
                OutlinedButton(onClick = { onCategorySelected(category) }) { Text(category.first) }
            }
        }
    }
}

@Composable
fun BlogList(
    posts: List<BlogItem>,
    favorites: List<BlogItem>, 
    modifier: Modifier = Modifier,
    onPostClick: (String) -> Unit,
    onFavoriteClick: (BlogItem) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            
            val isFavorite = favorites.any { it.link == post.link }

            BlogCard(
                post = post,
                isFavorite = isFavorite,
                onClick = { onPostClick(post.link) },
                onFavoriteClick = { onFavoriteClick(post) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogCard(
    post: BlogItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            
            Box {
                
                val imageToShow = post.imageUrl ?: "https://images.unsplash.com/photo-1611974765270-ca1258634369?q=80&w=800&auto=format&fit=crop"

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageToShow)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )

                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoriye Ekle",
                        tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.description.removeHtmlTags(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ArticleScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    }, update = { it.loadUrl(url) })
}

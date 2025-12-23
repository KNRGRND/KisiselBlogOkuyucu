package com.example.rssuygulamasi

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Değişti
import androidx.lifecycle.viewModelScope
import com.example.myapplication.AppDatabase
import com.example.myapplication.BlogItem
import com.example.myapplication.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URL

// ViewModel yerine AndroidViewModel yaptık (Database için Context lazım)
class BlogViewModel(application: Application) : AndroidViewModel(application) {

    // Veritabanı bağlantısı
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.favoriteDao()

    // İNTERNETTEN GELENLER
    private val _blogPosts = MutableStateFlow<List<BlogItem>>(emptyList())
    val blogPosts = _blogPosts.asStateFlow()

    // FAVORİLER (Veritabanından otomatik güncellenir)
    // stateIn: Flow'u StateFlow'a çevirir
    val favoritePosts = dao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Kategoriler
    val categories = listOf(
        "Teknoloji" to "https://www.wired.com/feed/rss",
        "Bilim" to "https://www.sciencealert.com/feed",
        "Dünya" to "https://feeds.bbci.co.uk/news/world/rss.xml",
        "Oyun" to "https://feeds.ign.com/ign/news",
        "Ekonomi" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664"
    )

    private val _selectedCategory = MutableStateFlow(categories[0])
    val selectedCategory = _selectedCategory.asStateFlow()

    // Hangi sekmedeyiz? (Haberler mi, Favoriler mi?)
    private val _isShowingFavorites = MutableStateFlow(false)
    val isShowingFavorites = _isShowingFavorites.asStateFlow()

    init {
        fetchRssData()
    }

    // Favori Ekleme/Çıkarma İşlemi
    fun toggleFavorite(post: BlogItem) {
        viewModelScope.launch {
            val currentFavs = favoritePosts.value
            // Eğer listede varsa sil, yoksa ekle
            // Linkler üzerinden kontrol ediyoruz
            val exists = currentFavs.any { it.link == post.link }

            if (exists) {
                dao.removeFavorite(post)
            } else {
                dao.addFavorite(post)
            }
        }
    }

    // Sekme Değiştirme
    fun showFavorites(show: Boolean) {
        _isShowingFavorites.value = show
    }

    fun onCategorySelected(category: Pair<String, String>) {
        _selectedCategory.value = category
        _isShowingFavorites.value = false // Kategori seçince haberlere dön
        fetchRssData()
    }

    fun fetchRssData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _blogPosts.value = emptyList()

            val url = _selectedCategory.value.second
            try {
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connect()

                val stream = connection.inputStream
                val parsedItems = RssParser().parse(stream)
                _blogPosts.value = parsedItems
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
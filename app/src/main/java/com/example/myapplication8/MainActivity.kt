
package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SearchScreen()
            }
        }
    }
}

class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _suggestions =
        MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> =
        _suggestions.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val products = listOf(
        "Apple iPhone",
        "Samsung Galaxy",
        "Laptop",
        "Wireless Headphones",
        "Smart Watch",
        "Bluetooth Speaker",
        "Mobile Charger"
    )

    init {
        viewModelScope.launch {
            query.collectLatest { text ->
                if (text.isBlank()) {
                    _suggestions.value = emptyList()
                    _loading.value = false
                } else {
                    delay(400.milliseconds)
                    _loading.value = true

                    try {
                        val result = withContext(Dispatchers.IO) {
                            fakeApiSearch(text)
                        }
                        _suggestions.value = result
                    } finally {
                        _loading.value = false
                    }
                }
            }
        }
    }

    fun onQueryChange(text: String) {
        _query.value = text
    }

    private suspend fun fakeApiSearch(
        text: String
    ): List<String> {
        delay(1.seconds)

        return products.filter {
            it.contains(text, ignoreCase = true)
        }
    }
}

@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val vm = remember(activity) {
        ViewModelProvider(activity)[SearchViewModel::class.java]
    }

    val query by vm.query.collectAsState()
    val suggestions by vm.suggestions.collectAsState()
    val loading by vm.loading.collectAsState()

    SearchContent(
        query = query,
        suggestions = suggestions,
        loading = loading,
        onQueryChange = vm::onQueryChange
    )
}

@Composable
fun SearchContent(
    query: String,
    suggestions: List<String>,
    loading: Boolean,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Product Search",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search products") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn {
            items(suggestions) { product ->
                Text(
                    text = product,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQueryChange(product) }
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                HorizontalDivider()
            }
        }

        if (!loading && query.isNotBlank() && suggestions.isEmpty()) {
            Text(
                text = "No products found",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductSearchPreview() {
    MaterialTheme {
        SearchContent(
            query = "lap",
            suggestions = listOf("Laptop"),
            loading = false,
            onQueryChange = {}
        )
    }
}

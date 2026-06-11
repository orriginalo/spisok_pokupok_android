package com.example.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.list.ui.theme.ListTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ShoppingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ListTheme(darkTheme = uiState.isDarkTheme) {
                ShoppingScreen(
                    uiState = uiState,
                    onTextChanged = viewModel::updateInput,
                    onAddClick = viewModel::addItem,
                    onRemoveClick = viewModel::removeItem,
                    onThemeChanged = viewModel::setDarkTheme
                )
            }
        }
    }
}

@Composable
fun ShoppingScreen(
    uiState: ShoppingUiState,
    onTextChanged: (String) -> Unit,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Список покупок",
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Тёмная тема")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.isDarkTheme,
                        onCheckedChange = onThemeChanged
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = onTextChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Новый товар") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onAddClick) {
                    Text("Добавить")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.items.isEmpty()) {
                Text("Список пока пуст")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.items) { index, item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item)
                                TextButton(onClick = { onRemoveClick(index) }) {
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
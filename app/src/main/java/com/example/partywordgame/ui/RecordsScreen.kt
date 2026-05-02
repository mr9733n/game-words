package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.partywordgame.models.GameRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    records: List<GameRecord>,
    onBackClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            Text(
                text = "Records",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Button(
                onClick = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }

        if (records.isEmpty()) {
            item {
                Text("No finished games yet.")
            }
        } else {
            items(records, key = { it.id }) { record ->
                RecordCard(record)
            }
        }
    }
}

@Composable
private fun RecordCard(record: GameRecord) {
    val dateText = SimpleDateFormat(
        "dd.MM.yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(record.finishedAt))
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (record.isTie) {
                    "Result: Tie"
                } else {
                    "Winner: ${record.winnerName}"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            record.scores.forEach { score ->
                Text("${score.teamName}: ${score.score}")
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Words:",
                    fontWeight = FontWeight.Bold
                )

                record.usedWords.forEach { word ->
                    Text("• ${word.text}")
                }
            }
        }
    }
}
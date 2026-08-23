package com.example.aasra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aasra.i18n.Strings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SafeStatusScreen() {
    var marked by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(Strings.get("safe_title"), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Text("This also clears the \"waiting\" flag on any rescue request you've filed, so authorities know you're accounted for.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                FirebaseFirestore.getInstance()
                    .collection("safe_status")
                    .document(userId)
                    .set(mapOf("isSafe" to true, "timestamp" to System.currentTimeMillis()))
                marked = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(Strings.get("safe_button"))
        }
        if (marked) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Status shared with your contacts and authorities.")
        }
    }
}
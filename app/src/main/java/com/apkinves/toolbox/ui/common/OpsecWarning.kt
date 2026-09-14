package com.apkinves.toolbox.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.ui.theme.CyberColors

/**
 * Aviso de OPSEC: estas herramientas contactan directamente con la
 * infraestructura del objetivo (su DNS, su registrador, su servidor...) desde
 * la IP real del dispositivo, a diferencia de las que solo consultan
 * terceros (RDAP públicos, crt.sh...). Si el objetivo controla sus propios
 * logs, puede ver esa consulta.
 */
@Composable
fun OpsecWarning(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberColors.NeonAmber.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, CyberColors.NeonAmber.copy(alpha = 0.4f)),
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Text(
                "⚠ OPSEC: esta consulta puede llegar directamente a la infraestructura del objetivo (su DNS, servidor, registrador...) desde la IP real de este dispositivo. Si el objetivo revisa sus propios logs, puede detectarla.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

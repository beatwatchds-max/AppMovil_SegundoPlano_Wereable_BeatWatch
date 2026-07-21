package com.bitwatch.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DataLayerManager @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope
) {

    companion object {
        private const val TAG = "DataLayerManager"
        const val CAPABILITY_NAME = "bitwatch_companion"
        const val PATH_PAIRING = "/pairing"
        const val PATH_HEART_RATE = "/heart_rate"
        const val PATH_ACTIVITY = "/activity"
        const val PATH_ARRHYTHMIA = "/arrhythmia"
        const val PATH_RISK_FACTORS = "/risk_factors"
        const val PATH_SYNC_REQUEST = "/sync_request"
        const val KEY_NODE_ID = "node_id"
        const val KEY_PAIRED = "paired"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_BPM = "bpm"
        const val KEY_INTENSITY = "intensity"
        const val KEY_DURATION = "duration"
        const val KEY_CONFIDENCE = "confidence"
        const val KEY_RISK_DATA = "risk_data"
    }

    private val _connectedNode = MutableStateFlow<Node?>(null)
    val connectedNode: StateFlow<Node?> = _connectedNode

    private val _isPaired = MutableStateFlow(false)
    val isPaired: StateFlow<Boolean> = _isPaired

    private val _pairingChannel = Channel<PairingResult>()
    val pairingChannel = _pairingChannel

    private var messageListener: MessageClient.OnMessageReceivedListener? = null
    private var dataListener: DataClient.OnDataChangedListener? = null
    private var capabilityListener: CapabilityClient.OnCapabilityChangedListener? = null

    init {
        initWearableClient()
    }

    private fun initWearableClient() {
        val messageClient = Wearable.getMessageClient(context)
        val dataClient = Wearable.getDataClient(context)
        val capabilityClient = Wearable.getCapabilityClient(context)

        // Advertise capability so wearables can find us
        capabilityClient.addListener(
            CapabilityClient.OnCapabilityChangedListener { _ -> },
            CAPABILITY_NAME
        )

        // Listener de mensajes entrantes
        messageListener = MessageClient.OnMessageReceivedListener { event ->
            handleMessage(event)
        }
        messageClient.addListener(messageListener!!)

        // Listener de cambios en DataItems
        dataListener = DataClient.OnDataChangedListener { events ->
            handleDataEvents(events)
        }
        dataClient.addListener(dataListener!!)

        // Listener de capacidades (para detectar wearables conectados)
        capabilityListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            handleCapabilityChanged(capabilityInfo)
        }
        capabilityClient.addListener(capabilityListener!!, CAPABILITY_NAME)

        // Verificar capacidades existentes al inicio
        checkExistingCapabilities()
    }

    private fun checkExistingCapabilities() {
        Wearable.getCapabilityClient(context)
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                handleCapabilityChanged(capabilityInfo)
            }
    }

    private fun handleCapabilityChanged(capabilityInfo: CapabilityInfo) {
        val nodes = capabilityInfo.nodes
        if (nodes.isNotEmpty()) {
            val node = nodes.first()
            _connectedNode.value = node
            Log.d(TAG, "Wearable conectado: ${node.displayName} (${node.id})")
        } else {
            _connectedNode.value = null
            _isPaired.value = false
            Log.d(TAG, "Wearable desconectado")
        }
    }

    private fun handleMessage(event: MessageEvent) {
        when (event.path) {
            PATH_PAIRING -> {
                val dataMap = DataMap.fromByteArray(event.data)
                val paired = dataMap.getBoolean(KEY_PAIRED)
                if (paired) {
                    _isPaired.value = true
                    _pairingChannel.trySend(PairingResult.Success)
                    Log.d(TAG, "Emparejamiento confirmado con ${event.sourceNodeId}")
                    // TODO: Registrar dispositivo en backend
                    // POST /api/dispositivos/emparejar
                } else {
                    _isPaired.value = false
                    _pairingChannel.trySend(PairingResult.Failure("El reloj canceló el emparejamiento"))
                }
            }
            else -> Log.d(TAG, "Mensaje recibido en path desconocido: ${event.path}")
        }
    }

    private fun handleDataEvents(events: DataEventBuffer) {
        for (event in events) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val path = dataItem.uri.path
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                Log.d(TAG, "DataItem recibido: $path")

                when (path) {
                    PATH_HEART_RATE -> {
                        val bpm = dataMap.getInt(KEY_BPM)
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP)
                        Log.d(TAG, "Frecuencia cardíaca: $bpm bpm")
                        // TODO: Enviar a backend POST /api/salud/arritmia
                    }
                    PATH_ACTIVITY -> {
                        val intensity = dataMap.getString(KEY_INTENSITY)
                        val duration = dataMap.getInt(KEY_DURATION)
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP)
                        Log.d(TAG, "Actividad: $intensity, $duration seg")
                        // TODO: Enviar a backend POST /api/salud/actividad
                    }
                    PATH_ARRHYTHMIA -> {
                        val bpm = dataMap.getInt(KEY_BPM)
                        val confidence = dataMap.getFloat(KEY_CONFIDENCE)
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP)
                        Log.d(TAG, "Arritmia: $bpm bpm, confianza: $confidence")
                        // TODO: Enviar a backend POST /api/salud/arritmia
                    }
                    PATH_RISK_FACTORS -> {
                        val riskData = dataMap.getString(KEY_RISK_DATA)
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP)
                        Log.d(TAG, "Factores de riesgo: $riskData")
                        // TODO: Enviar a backend POST /api/salud/factores-riesgo
                    }
                    PATH_SYNC_REQUEST -> {
                        val timestamp = dataMap.getLong(KEY_TIMESTAMP)
                        Log.d(TAG, "Solicitud de sincronización")
                        // TODO: Sincronizar datos pendientes con backend
                    }
                }
            }
        }
    }

    fun confirmPairing(nodeId: String) {
        val dataMap = DataMap().apply {
            putBoolean(KEY_PAIRED, true)
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }

        Wearable.getMessageClient(context)
            .sendMessage(nodeId, PATH_PAIRING, dataMap.toByteArray())
            .addOnSuccessListener {
                Log.d(TAG, "Confirmación de emparejamiento enviada")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error enviando confirmación", e)
                _pairingChannel.trySend(PairingResult.Failure("Error de red: ${e.message}"))
            }
    }

    fun sendDataItem(path: String, dataMap: DataMap, nodeId: String) {
        val putDataMapRequest = PutDataMapRequest.create("wear://$nodeId$path").apply {
            dataMap.putAll(dataMap)
        }
        Wearable.getDataClient(context)
            .putDataItem(putDataMapRequest.asPutDataRequest())
            .addOnSuccessListener { Log.d(TAG, "DataItem enviado: $path") }
            .addOnFailureListener { e -> Log.e(TAG, "Error enviando DataItem: $path", e) }
    }

    sealed class PairingResult {
        object Pending : PairingResult()
        object Success : PairingResult()
        data class Failure(val message: String) : PairingResult()
    }

    fun close() {
        val messageClient = Wearable.getMessageClient(context)
        val dataClient = Wearable.getDataClient(context)
        val capabilityClient = Wearable.getCapabilityClient(context)

        messageListener?.let { messageClient.removeListener(it) }
        dataListener?.let { dataClient.removeListener(it) }
        capabilityListener?.let { capabilityClient.removeListener(it) }
        _pairingChannel.close()
    }
}
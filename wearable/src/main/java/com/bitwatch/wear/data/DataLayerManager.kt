package com.bitwatch.wear.data

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

        // Listener de capacidades (para detectar cuando el teléfono se conecta/desconecta)
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
            // Tomar el primer nodo disponible (el teléfono)
            val node = nodes.first()
            _connectedNode.value = node
            _isPaired.value = true
            Log.d(TAG, "Compañero conectado: ${node.displayName} (${node.id})")
        } else {
            _connectedNode.value = null
            _isPaired.value = false
            Log.d(TAG, "Compañero desconectado")
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
                } else {
                    _pairingChannel.trySend(PairingResult.Failure("El teléfono rechazó el emparejamiento"))
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
                Log.d(TAG, "DataItem cambiado: $path")
            }
        }
    }

    fun startPairing(): PairingResult {
        val node = _connectedNode.value
            ?: return PairingResult.Failure("No hay teléfono conectado. Activa Bluetooth en ambos dispositivos.")

        // Enviar solicitud de emparejamiento
        val dataMap = DataMap().apply {
            putBoolean(KEY_PAIRED, true)
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }

        Wearable.getMessageClient(context)
            .sendMessage(node.id, PATH_PAIRING, dataMap.toByteArray())
            .addOnSuccessListener {
                Log.d(TAG, "Solicitud de emparejamiento enviada a ${node.displayName}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error enviando solicitud de emparejamiento", e)
                _pairingChannel.trySend(PairingResult.Failure("Error de red: ${e.message}"))
            }

        return PairingResult.Pending
    }

    fun sendHeartRate(bpm: Int, timestamp: Long = System.currentTimeMillis()) {
        val node = _connectedNode.value ?: return
        val dataMap = DataMap().apply {
            putInt(KEY_BPM, bpm)
            putLong(KEY_TIMESTAMP, timestamp)
        }
        sendDataItem(PATH_HEART_RATE, dataMap, node.id)
    }

    fun sendActivityData(intensity: String, durationSeconds: Int, timestamp: Long = System.currentTimeMillis()) {
        val node = _connectedNode.value ?: return
        val dataMap = DataMap().apply {
            putString(KEY_INTENSITY, intensity)
            putInt(KEY_DURATION, durationSeconds)
            putLong(KEY_TIMESTAMP, timestamp)
        }
        sendDataItem(PATH_ACTIVITY, dataMap, node.id)
    }

    fun sendArrhythmiaData(bpm: Int, confidence: Float, timestamp: Long = System.currentTimeMillis()) {
        val node = _connectedNode.value ?: return
        val dataMap = DataMap().apply {
            putInt(KEY_BPM, bpm)
            putFloat("confidence", confidence)
            putLong(KEY_TIMESTAMP, timestamp)
        }
        sendDataItem(PATH_ARRHYTHMIA, dataMap, node.id)
    }

    fun sendRiskFactors(riskData: String) {
        val node = _connectedNode.value ?: return
        val dataMap = DataMap().apply {
            putString("risk_data", riskData)
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }
        sendDataItem(PATH_RISK_FACTORS, dataMap, node.id)
    }

    fun requestSync() {
        val node = _connectedNode.value ?: return
        val dataMap = DataMap().apply {
            putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }
        sendDataItem(PATH_SYNC_REQUEST, dataMap, node.id)
    }

    private fun sendDataItem(path: String, dataMap: DataMap, nodeId: String) {
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
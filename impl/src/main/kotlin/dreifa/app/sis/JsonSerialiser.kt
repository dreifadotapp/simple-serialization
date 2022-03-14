package dreifa.app.sis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class JsonSerialiser {
    private val mapper: ObjectMapper = ObjectMapper()

    init {
        val module = KotlinModule()
        //module.addSerializer(SerialisationPacketWireFormat::class.java, XX())
        mapper.registerModule(module)
        //mapper.reg
    }

    @Deprecated(message = "use fromPacket")
    fun deserialiseData(serialised: String) = fromPacket(serialised)

    fun fromPacket(serialised: String): SerialisationPacket {
        val raw = mapper.readValue(serialised, SerialisationPacketWireFormat::class.java)
        val clazz = ReflectionsSupport.forClass(raw.clazzName)

        return when {
            raw.scalar != null -> {
                val scalar = ReflectionsSupport.deserialiseScalar(raw.scalar, clazz)
                SerialisationPacket.create(scalar)
            }
            raw.data != null -> {
                val data = mapper.readValue(raw.data, clazz.java)
                SerialisationPacket.create(data)
            }
            raw.map != null -> {
                val data = mapper.readValue(raw.map, clazz.java)
                SerialisationPacket.create(data)
            }
            raw.list != null -> {
                val list = mapper.readValue(raw.list, clazz.java)
                SerialisationPacket.create(list)
            }
            raw.exception != null -> {
                val exception = mapper.readValue(raw.exception, clazz.java)
                SerialisationPacket.create(exception)
            }
            else -> {
                // only option left is one of the "nothing" types
                val nothing = ReflectionsSupport.deserialiseNothing(raw.clazzName)
                SerialisationPacket.create(nothing)
            }
        }
    }

    fun toPacket(data: Any): String {
        val packet = SerialisationPacket.create(data)
        val wire = packetToWireFormat(packet)
        return mapper.writeValueAsString(wire)
    }

    fun toPacketData(data: Any): String {
        val packet = SerialisationPacket.create(data)
        val wire = packetToWireFormat(packet)
        return mapper.writeValueAsString(wire.any())
    }

    @Deprecated(message = "Use toPacket()")
    fun serialiseData(data: Any): String = toPacket(data)

    private fun packetToWireFormat(packet: SerialisationPacket): SerialisationPacketWireFormat {
        return when {
            packet.scalar != null -> {
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), scalar = packet.scalar.toString())
            }
            packet.data != null -> {
                val json = mapper.writeValueAsString(packet.data)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), data = json)
            }
            packet.map != null -> {
                val json = mapper.writeValueAsString(packet.map)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), map = json)
            }
            packet.list != null -> {
                val json = mapper.writeValueAsString(packet.list)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), list = json)
            }
            packet.exception != null -> {
                val json = mapper.writeValueAsString(packet.exception)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), exception = json)
            }
            packet.nothingClazz != null -> {
                SerialisationPacketWireFormat(clazzName = packet.clazzName())
            }
            else -> {
                throw java.lang.RuntimeException("Cannot map SerialisationPacket: $packet")
            }
        }
    }
}


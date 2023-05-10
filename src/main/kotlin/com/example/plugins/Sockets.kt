package com.example.plugins

import com.example.*
import io.ktor.server.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import java.lang.Exception
import java.util.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.count()} users here.")
                send("Please enter your name.")
                val nameFrame = incoming.receive() as? Frame.Text
                val name = nameFrame?.readText() ?: thisConnection.name
                connections.forEach {
                    it.session.send("$name joined!")
                }
                for(frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    if(receivedText.first() == '/') {
                        if(receivedText.takeWhile { it != ' ' } == "/whisper") {
                            send("Valid Command")
                        } else {
                            send("Command Not Supported.")
                            continue
                        }
                    }
                    val textWithUsername = "[${name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}

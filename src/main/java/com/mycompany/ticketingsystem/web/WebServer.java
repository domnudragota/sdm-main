package com.mycompany.ticketingsystem.web;

import com.mycompany.ticketingsystem.service.MessageService;
import static spark.Spark.*;
import java.util.List;
import java.util.stream.Collectors;

public class WebServer {
    public static void main(String[] args) {
        // Set the server port (default is 4567)
        port(4567);

        // Define the main route that serves an HTML page with the stored messages.
        get("/", (req, res) -> {
            res.type("text/html");
            List<String> messages = MessageService.getInstance().getMessages();
            String messagesHtml = messages.stream()
                    .map(msg -> "<li>" + msg + "</li>")
                    .collect(Collectors.joining());
            return "<html>" +
                    "<head><title>Ticketing System Status</title></head>" +
                    "<body>" +
                    "<h1>Live MQTT Ticket Messages</h1>" +
                    "<ul>" + messagesHtml + "</ul>" +
                    "</body>" +
                    "</html>";
        });


    }
}

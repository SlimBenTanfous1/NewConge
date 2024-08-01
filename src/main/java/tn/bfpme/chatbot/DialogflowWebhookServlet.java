package tn.bfpme.chatbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.bfpme.services.ServiceUserSolde;
import tn.bfpme.utils.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/webhook")
public class DialogflowWebhookServlet extends HttpServlet {

    private ServiceUserSolde serviceUserSolde;

    @Override
    public void init() throws ServletException {
        serviceUserSolde = new ServiceUserSolde();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
        String action = jsonObject.getAsJsonObject("queryResult").get("action").getAsString();

        JsonObject responseJson = new JsonObject();
        JsonObject fulfillmentText = new JsonObject();

        if ("get_leave_balance".equals(action)) {
            int userId = SessionManager.getInstance().getUser().getIdUser();
            double totalSolde = serviceUserSolde.getTotalSoldeForUser(userId);
            fulfillmentText.addProperty("fulfillmentText", "Your total leave balance is " + totalSolde + " days.");
        } else {
            fulfillmentText.addProperty("fulfillmentText", "Sorry, I didn't understand that.");
        }

        responseJson.add("fulfillmentText", fulfillmentText);

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println(responseJson.toString());
    }
}

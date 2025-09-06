package connection;

import com.google.gson.Gson;
import exception.ResponseException;
import requests.*;
import results.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {

    private String serverURL;
    private HttpClient client = HttpClient.newHttpClient();
    //private WebSocketFacade wsFacade;

    public ServerFacade(String serverURL, ServerMessageObserver observer) {
        this.serverURL = serverURL;
        //wbFacade = new WebSocketFacade(observer);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("POST", "/user", "", request);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("POST", "/session", "", request);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("DELETE", "/session", request.authToken(), null);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        handleResponse(httpResponse, null);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("POST", "/game", request.authToken(), request);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, CreateGameResult.class);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("GET", "/game", request.authToken(), null);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, ListGamesResult.class);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        HttpRequest httpRequest = buildRequest("PUT", "/game", request.authToken(), request);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        handleResponse(httpResponse, null);
    }

    public void clear() throws ResponseException {
        HttpRequest httpRequest = buildRequest("DELETE", "/db", "", null);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        handleResponse(httpResponse, null);
    }


    private HttpRequest buildRequest(String method, String path, String authToken, Object body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(serverURL + path));
        builder.setHeader("Authorization", authToken);
        if (body != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)));
            builder.setHeader("Content-Type", "application/json");
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(" So sorry, we are currently having issues with sending messages to the CGI server."
                    + "\n " + ex.getMessage(), 0);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        if (response.statusCode() != 200) {
            String jsonBody = response.body();
            if (jsonBody != null) {
                throw new ResponseException(new Gson().fromJson(jsonBody, ErrorResult.class).message(), 0);
            }
            throw new ResponseException(" Failed to get error message", 0);
        }
        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }
}

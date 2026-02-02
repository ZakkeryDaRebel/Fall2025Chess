package facade;

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

    public ServerFacade(String serverURL) {
        this.serverURL = serverURL;
    }

    public record RequestRecord(String method, String path, String authToken, Object body){};

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return requestProcess(new RequestRecord("POST", "/user", "", request), RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return requestProcess(new RequestRecord("POST", "/session", "", request), LoginResult.class);
    }

    public void logout(LogoutRequest request) throws ResponseException {
        requestProcess(new RequestRecord("DELETE", "/session", request.authToken(), null), null);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ResponseException {
        return requestProcess(new RequestRecord("POST", "/game", request.authToken(), request), CreateGameResult.class);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        return requestProcess(new RequestRecord("GET", "/game", request.authToken(), null), ListGamesResult.class);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        requestProcess(new RequestRecord("PUT", "/game", request.authToken(), request), null);
    }

    public void clear() throws ResponseException {
        requestProcess(new RequestRecord("DELETE", "/db", "", null), null);
    }

    private <T> T requestProcess(RequestRecord req, Class<T> response) throws ResponseException {
        HttpRequest httpRequest = buildRequest(req);
        HttpResponse<String> httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, response);
    }


    private HttpRequest buildRequest(RequestRecord req) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(serverURL + req.path));
        builder.setHeader("Authorization", req.authToken);
        if (req.body != null) {
            builder.method(req.method, HttpRequest.BodyPublishers.ofString(new Gson().toJson(req.body)));
            builder.setHeader("Content-Type", "application/json");
        } else {
            builder.method(req.method, HttpRequest.BodyPublishers.noBody());
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

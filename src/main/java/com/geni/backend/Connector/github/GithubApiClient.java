package com.geni.backend.Connector.github;

import com.geni.backend.common.BaseApiClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// GithubApiClient.java
@Component
public class GithubApiClient extends BaseApiClient {

    public GithubApiClient(RestClient.Builder builder) {
        super(builder, "https://api.github.com");
    }

    @Override protected String baseUrlHost() { return "api.github.com"; }

    public String getInstallationAccount(String jwt, String installationId) {
        GithubInstallation inst = get(
                "/app/installations/" + installationId, jwt, GithubInstallation.class
        );
        return inst.getAccount().getLogin();
    }

    public String getInstallationToken(String jwt, String installationId) {
        GithubAccessToken token = post(
                "/app/installations/" + installationId + "/access_tokens",
                jwt, Map.of(), GithubAccessToken.class
        );
        return token.getToken();
    }

//    public GithubComment createComment(String accessToken, String owner,
//                                       String repo, int issue, String body) {
//        return post(
//                "/repos/" + owner + "/" + repo + "/issues/" + issue + "/comments",
//                accessToken, Map.of("body", body), GithubComment.class
//        );
//    }
}

package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUtil {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String USER_AGENT = buildUserAgent();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .executor(Util.nonCriticalIoPool())
            .connectTimeout(TIMEOUT)
            .build();

    private HttpUtil() {
    }

    public static <T> HttpResponse<T> request(
            HttpRequest.Builder builder,
            HttpResponse.BodyHandler<T> handler
    ) throws IOException, InterruptedException {
        builder.setHeader("User-Agent", USER_AGENT);
        builder.timeout(TIMEOUT);
        return HTTP_CLIENT.send(builder.build(), handler);
    }

    private static String buildUserAgent() {
        String env = false ? "/development" : "";
        String loader = false ? "quilt" : "fabric";

        var modMenuString = getModMenuString();
        var minecraftString = SharedConstants.getCurrentString().name();

        // -> TerraformersMC/ModMenu/9.1.0 (1.20.3/quilt/development)
        return "%s/%s (%s/%s%s)".formatted(ModMenu.GITHUB_REF, modMenuString, minecraftString, loader, env);
    }

    private static String getModMenuString() {
        var container = null.getObject(ModMenu.MOD_ID);
        if (container.isEmpty()) {
            throw new RuntimeException("Unable to find Modmenu's own mod container!");
        }

        return StringUtil.removeBuildMetadata(container.get().getMetadata().getString().getFriendlyString());
    }
}

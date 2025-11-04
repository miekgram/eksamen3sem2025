package app.security.routes;

import app.security.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.utils.Utils;
import app.security.controllers.SecurityController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To handle security in the API
 *  Author: Thomas Hartmann
 */
public class SecurityRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    public static EndpointGroup getSecurityRoutes() {
        return ()->{
            path("/auth", ()->{
                get("/healthcheck", securityController::healthCheck, User.Role.ANYONE);
                get("/test", ctx->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello U")), User.Role.ANYONE);
                post("/login", securityController.login(), User.Role.ANYONE);
                post("/register", securityController.register(), User.Role.ANYONE);
                post("/user/addrole", securityController.addRole(), User.Role.USER);
            });
        };
    }
    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                get("/user_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")), User.Role.USER);
                get("/admin_demo", (ctx)->ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), User.Role.ADMIN);
            });
        };
    }
}

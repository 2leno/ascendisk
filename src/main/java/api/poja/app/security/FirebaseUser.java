package api.poja.app.security;

public record FirebaseUser(
    String uid, String email, String displayName, String pictureUrl, boolean emailVerified) {}
